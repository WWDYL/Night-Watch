package cn.turingmoon.detectors;

import cn.turingmoon.LocalStorage;
import cn.turingmoon.constants.AttackType;
import cn.turingmoon.constants.FlowType;
import cn.turingmoon.models.AttackRecord;
import cn.turingmoon.models.Flow;
import cn.turingmoon.utilities.MongoDbUtils;
import cn.turingmoon.utilities.RedisUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.gt;


public class FlowHeaderDetector {

    private static final Logger logger = LogManager.getLogger("FlowHeaderDetector");
    private ScheduledExecutorService scheduExec;
    private Date begin_time = new Date(0);
    private long flows_sum = 0;
    private long flows_psum = 0;
    private long flows_psize = 0;

    private int cycle = LocalStorage.CYCLE_TIME;

    public FlowHeaderDetector() {
        scheduExec = Executors.newScheduledThreadPool(1);
    }

    private boolean pcIsLarge(int num) {
        logger.info("Packet Count: {} {}", num, num / (float) flows_psum);
        return num > 0.5 * flows_psum;
    }

    private boolean fsIsLarge(int num) {
        logger.info("Flow Size: {} {}", num, num / (float) flows_psize);
        return num > 0.5 * flows_psize;
    }

    private boolean percentIsLarge(float num) {
        /* TODO: get the normal percentage. */
        logger.info("Packet Size / Packet Num: {}", num);
        return num > 1;
    }

    private boolean isReflectingPort(String sPort) {
        int port = Integer.parseInt(sPort);
        return port == 7 || port == 13 || port == 17 || port == 19;
    }

    private boolean isBroadcastAddr(String ip) {
        /* TODO: implement the correct function. */
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
        if (FlowType.isTCP(flow.getType())) {
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
        } else if (FlowType.isICMP(flow.getType())) {
            if (flow.getType().equals(FlowType.ICMP_Echo_Request) && isBroadcastAddr(flow.getdIP())) {
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

    private void hasDetect(ObjectId id) {
        MongoDbUtils utils = MongoDbUtils.getInstance();
        utils.storeHasDetect(id, 1);
    }

    public void run() {
        scheduExec.scheduleWithFixedDelay(() -> {
            logger.info("Start Flow Header Detection! ");
            MongoDbUtils utils = MongoDbUtils.getInstance();
            List<Document> flows = utils.getFlowRecords(new Document("FHDetect", false));
            /*
             * TODO: add filter to reduce repeat work.
             * ORIGIN: gt("BeginTime", begin_time)
             */
            int num = 0;
            /*
             *  This method is silly because it will cost some extra resources, but I haven't
             *  find a better way to implement the statistics. Remove this when you find a better
             *  way.
             *  TODO: find a better method to implement statistics function.
             */
            for (Document document : flows) {
                flows_sum++;
                flows_psum += document.getInteger("PacketNum");
                flows_psize += document.getInteger("PacketSize");
                hasDetect(document.getObjectId("_id"));
            }
            for (Document document : flows) {
                num++;
                Flow tempFlow = Flow.parseDocument(document);
                detect(tempFlow);
                begin_time = tempFlow.getbTime();
            }
            logger.info("Flow Header Detect {} records", num);
        }, 2 * cycle, cycle, TimeUnit.SECONDS);
    }

}
