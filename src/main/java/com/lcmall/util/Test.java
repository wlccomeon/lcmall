package com.lcmall.util;

import com.lcmall.util.redis.RedisShardedPool;
import com.lcmall.util.redis.RedisShardedUtil;
import redis.clients.jedis.ShardedJedis;

public class Test {

    public static void main(String[] args) {
        //测试分布式redis
        ShardedJedis jedis = RedisShardedPool.getJedis();
        try {
//            for (int i = 0; i <10 ; i++) {
//                jedis.set("key"+i,"value"+i);
//            }
            RedisShardedUtil.setEx("name", "lc", 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
