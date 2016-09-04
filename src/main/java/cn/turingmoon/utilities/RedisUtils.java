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
        if (jedis != null) {
            System.out.println("Server is running: " + jedis.ping());
        }
    }

    public Jedis getJedis() {
        return jedis;
    }

    public static void main(String[] args) {
        RedisUtils utils = new RedisUtils();
    }
}
