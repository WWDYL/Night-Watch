package cn.turingmoon.detectors;

import cn.turingmoon.LocalStorage;
import cn.turingmoon.constants.AttackType;
import cn.turingmoon.constants.FlowType;
import cn.turingmoon.models.AttackRecord;
import cn.turingmoon.models.Flow;
import cn.turingmoon.utilities.MongoDbUtils;
import cn.turingmoon.utilities.RedisUtils;
import org.bson.Document;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.gt;


public class FlowHeaderDetector {

    private ScheduledExecutorService scheduExec;
    private Date begin_time = new Date(0);
    private long flows_sum = 0;
    private long flows_psum = 0;
    private long flows_psize = 0;

    private int cycle = LocalStorage.CYCLE_TIME;

    public FlowHeaderDetector() {
        scheduExec = Executors.newScheduledThreadPool(8);
    }

    private boolean pcIsLarge(int num) {
        return num > 0.5 * flows_psum;
    }

    private boolean fsIsLarge(int num) {
        return num > 0.5 * flows_psize;
    }

    private boolean percentIsLarge(float num) {
        /* TODO: get the normal percentage. */
        return num > 1;
    }

    private boolean isReflectingPort(String sPort) {
        int port = Integer.parseInt(sPort);
        return port == 7 || port == 13 || port == 17 || port == 19;
    }

    private boolean isBroadcastAddr(String ip) {
        return ip.equals(LocalStorage.BroadcastAddr);
    }

    private void recordAttackType(Flow flow, AttackType type) {
        RedisUtils utils = RedisUtils.getInstance();
        Jedis jedis = utils.getJedis();
        Long id = jedis.incr("fh_attack:");
        String attack_id = "fh_attack:" + id;
        jedis.hset(attack_id, "Attacker", flow.getsIP());
        jedis.hset(attack_id, "Victim", flow.getdIP());
        jedis.hset(attack_id, "Protocol", flow.getType());
        jedis.hset(attack_id, "Description", type.name());
    }

    private void detect(Flow flow) {
        if (flow.getType().equals(FlowType.TCP)) {
            /* 检测是否是land attack */
            if (flow.getsIP().equals(flow.getdIP()) && flow.getsPort().equals(flow.getdPort())) {
                recordAttackType(flow, AttackType.Land);
                AttackRecorder.record(new AttackRecord(flow, AttackType.Land));
            }
            /* 检测是否是TCP flooding */
            if (pcIsLarge(flow.getpNum()) && fsIsLarge(flow.getpSize())) {
                recordAttackType(flow, AttackType.TCP_flooding);
            }
        } else if (flow.getType().equals(FlowType.UDP)) {
            if (isReflectingPort(flow.getdPort())) {
                if (isReflectingPort(flow.getsPort())) {
                    recordAttackType(flow, AttackType.ping_pong);
                    AttackRecorder.record(new AttackRecord(flow, AttackType.ping_pong));
                }
                if (isBroadcastAddr(flow.getdIP())) {
                    recordAttackType(flow, AttackType.Fraggle);
                    AttackRecorder.record(new AttackRecord(flow, AttackType.Fraggle));
                }
            }
            if (pcIsLarge(flow.getpNum()) && fsIsLarge(flow.getpSize())) {
                recordAttackType(flow, AttackType.UDP_flooding);
            }
        } else if (flow.getType().equals(FlowType.ICMP_Echo_Request)) {
            if (isBroadcastAddr(flow.getdIP())) {
                recordAttackType(flow, AttackType.Smurf);
                AttackRecorder.record(new AttackRecord(flow, AttackType.Smurf));
            }
            if (percentIsLarge(flow.getpSize() / flow.getpNum())) {
                recordAttackType(flow, AttackType.Ping_of_death);
                AttackRecorder.record(new AttackRecord(flow, AttackType.Ping_of_death));
            }
            if (fsIsLarge(flow.getpSize()) && pcIsLarge(flow.getpNum())) {
                recordAttackType(flow, AttackType.ICMP_flooding);
            }
        }
    }

    public void run() {
        scheduExec.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                MongoDbUtils utils = MongoDbUtils.getInstance();
                List<Document> flows = utils.getFlowRecords(gt("BeginTime", begin_time));
                int num = 0;
                /**
                 *  This method is bad because it will cost some extra resources, but I haven't
                 *  find a better way to implement the statistics. Remove this when you find a better
                 *  way.
                 *  TODO: find a better method to implement statistics function.
                 */
                for (Document document : flows) {
                    flows_sum++;
                    flows_psum += document.getInteger("PacketNum");
                    flows_psize += document.getInteger("PacketSize");
                }
                for (Document document : flows) {
                    num++;
                    Flow tempflow = Flow.parseDocument(document);
                    detect(tempflow);
                    begin_time = tempflow.getbTime();
                }
                System.err.println("NUM: " + num);
            }
        }, cycle, cycle, TimeUnit.SECONDS);
    }

}
