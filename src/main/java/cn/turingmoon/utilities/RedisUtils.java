package cn.turingmoon.utilities;

import redis.clients.jedis.Jedis;

/**
 * cn.turingmoon.utilities.RedisUtils 连接Redis
 * Created by Deng Li on 2016/8/20.
 */

public class RedisUtils {
    private Jedis jedis = null;

    RedisUtils() {
        jedis = new Jedis("localhost");
        if (jedis != null) {
            System.out.println("Server is running: " + jedis.ping());
        }
    }

    public static void main(String[] args) {
        RedisUtils utils = new RedisUtils();
    }
}
