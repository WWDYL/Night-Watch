package cn.turingmoon.utilities;

import redis.clients.jedis.Jedis;

public class RedisUtils {
    private static RedisUtils utils = null;

    private Jedis jedis = null;

    public static RedisUtils getInstance() {
        if (utils == null) {
            utils = new RedisUtils();
        }
        return utils;
    }

    RedisUtils() {
        jedis = new Jedis("localhost");
    }

    public Jedis getJedis() {
        return jedis;
    }
}
