package com.lcmall.util.redis;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.ShardedJedis;

/**
 * 分布式redis工具类
 * @author wlc
 */
@Slf4j
public class RedisShardedUtil {
    /**
     * 设置key的有效期，单位是秒
     * @param key
     * @param exTime
     * @return
     */
    public static Long expire(String key,int exTime){
        ShardedJedis jedis = null;
        Long result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.expire(key,exTime);
        } catch (Exception e) {
            log.error("redis expire exception:{}",e.toString());
            RedisShardedPool.returnResource(jedis);
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    /**
     * 添加并设置key的有效期，exTime的单位是秒
     * @param key
     * @param value
     * @param exTime
     * @return
     */
    public static String setEx(String key,String value,int exTime){
        ShardedJedis jedis = null;
        String result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.setex(key,exTime,value);
        } catch (Exception e) {
            log.error("redis setEx exception:{}",e.toString());
            RedisShardedPool.returnResource(jedis);
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    /**
     * 添加字符串键值对
     * @param key
     * @param value
     * @return
     */
    public static String set(String key,String value){
        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.set(key,value);
        } catch (Exception e) {
            log.error("redis set exception:{}",e.toString());
            RedisShardedPool.returnResource(jedis);
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    /**
     * 使用value覆盖key中原来的值，并返回原来的值
     * @param key
     * @param value
     * @return
     */
    public static String getSet(String key,String value){
        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.getSet(key,value);
        } catch (Exception e) {
            log.error("redis getSet exception:{}",e.toString());
            RedisShardedPool.returnResource(jedis);
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    /**
     * 根据字符串key获取value
     * @param key
     * @return
     */
    public static String get(String key){
        ShardedJedis jedis = null;
        String result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("redis get exception:{}",e.toString());
            RedisShardedPool.returnResource(jedis);
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    /**
     * 根据key删除value
     * @param key
     * @return
     */
    public static Long del(String key){
        ShardedJedis jedis = null;
        Long result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("redis del exception:{}",e.toString());
            RedisShardedPool.returnResource(jedis);
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    /**
     * 添加键值对之前检查，若已存在则不添加
     * @param key
     * @param value
     * @return
     */
    public static Long setnx(String key,String value){
        ShardedJedis jedis = null;
        Long result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.setnx(key,value);
        } catch (Exception e) {
            log.error("redis setnx exception:{}",e.toString());
            RedisShardedPool.returnResource(jedis);
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }
}
