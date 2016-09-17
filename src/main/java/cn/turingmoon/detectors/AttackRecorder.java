package cn.turingmoon.detectors;

import cn.turingmoon.models.AttackRecord;
import cn.turingmoon.utilities.RedisUtils;
import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;

public class AttackRecorder {
    private static RedisUtils utils = RedisUtils.getInstance();

    public static void record(AttackRecord record) {
        Jedis jedis = utils.getJedis();
        Long id = jedis.incr("attack:");
        String attack_id = "attack:" + id;
        jedis.hset(attack_id, "BeginTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(record.BeginTime));
        jedis.hset(attack_id, "Duration", Long.toString(record.Duration));
        jedis.hset(attack_id, "Attacker", record.Src);
        jedis.hset(attack_id, "Victim", record.Dst);
        jedis.hset(attack_id, "Protocol", record.Protocol);
        jedis.hset(attack_id, "Description", record.Description);
    }
}
