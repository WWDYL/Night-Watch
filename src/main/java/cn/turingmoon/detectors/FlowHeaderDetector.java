package cn.turingmoon.detectors;

import cn.turingmoon.LocalStorage;
import cn.turingmoon.constants.AttackType;
import cn.turingmoon.constants.FlowType;
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
    private long begin_time = 0;

    public FlowHeaderDetector() {
        scheduExec = Executors.newScheduledThreadPool(2);
    }

    private boolean isSmall(int num) {
        /* TODO: implement this function. */
        return true;
    }

    private boolean isLarge(int num) {
        /* TODO: implement this function. */
        return true;
    }

    private boolean isReflectingPort(String sPort) {
        int port = Integer.parseInt(sPort);
        return port == 7 || port == 13 || port == 13 || port == 17;
    }

    private boolean isBroadcastAddr(String ip) {
        return false;
    }

    private void recordAttackType(Flow flow, AttackType type) {
        RedisUtils utils = RedisUtils.getInstance();
        Jedis jedis = utils.getJedis();
        Long id = jedis.incr("attack:");
        String attack_id = "attack:" + id;
        jedis.hset(attack_id, "Type", type.name());
    }

    private void detect(Flow flow) {
        if (flow.getType().equals(FlowType.TCP)) {
            /* 检测是否是land attack */
            if (flow.getsIP().equals(flow.getdIP()) && flow.getsPort().equals(flow.getdPort())) {
                recordAttackType(flow, AttackType.Land);
            }
            /* 检测是否是TCP flooding */
            if (isLarge(flow.getpNum()) && isLarge(flow.getpSize())) {
                recordAttackType(flow, AttackType.TCP_flooding);
            }
        } else if (flow.getType().equals(FlowType.UDP)) {
            if (isReflectingPort(flow.getdPort())) {
                if (isReflectingPort(flow.getsPort())) {
                    recordAttackType(flow, AttackType.ping_pong);
                }
                if (isBroadcastAddr(flow.getdIP())) {
                    recordAttackType(flow, AttackType.Fraggle);
                }
            }
            if (isLarge(flow.getpNum()) && isLarge(flow.getpSize())) {
                recordAttackType(flow, AttackType.UDP_flooding);
            }
        } else if (flow.getType().equals(FlowType.ICMP_Echo)) {
            if (isBroadcastAddr(flow.getdIP())) {
                recordAttackType(flow, AttackType.Smurf);
            }
            if (isLarge(flow.getpSize() / flow.getpNum())) {
                recordAttackType(flow, AttackType.Ping_of_death);
            }
            if (isLarge(flow.getpSize()) && isLarge(flow.getpNum())) {
                recordAttackType(flow, AttackType.ICMP_flooding);
            }
        }
    }

    public void run() {
        scheduExec.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                MongoDbUtils utils = MongoDbUtils.getInstance();
                List<Document> flows = utils.getFlowRecords(new Document());
                int num = 0;
                for (Document document : flows) {
                    num++;
                    Flow tempflow = Flow.parseDocument(document);
                    detect(tempflow);
                }
                System.out.println("NUM: " + num);
            }
        }, 60000, 60000, TimeUnit.MILLISECONDS);
    }

}
