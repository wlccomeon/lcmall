package com.lcmall.util.redis;

import com.lcmall.util.PropertyUtil;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

/**
 * 分布式Redis连接池
 * @author lc
 */
public class RedisShardedPool {
    /**分布式连接池*/
    private static ShardedJedisPool pool;
    /**最大连接数*/
    private static Integer maxTotal=Integer.parseInt(PropertyUtil.getProperty("redis.max.total"));
    /**最大空闲连接数*/
    private static Integer maxIdle=Integer.parseInt(PropertyUtil.getProperty("redis.max.idle"));
    /**最小空闲连接数*/
    private static Integer minIdle=Integer.parseInt(PropertyUtil.getProperty("redis.min.idle"));
    /**从jedis连接池获取连接时，校验并返回可用的连接*/
    private static Boolean testOnBorrow=Boolean.parseBoolean(PropertyUtil.getProperty("redis.test.borrow"));
    /**归还jedis连接池中的连接时，校验并返回可用的连接*/
    private static Boolean testOnReturn=Boolean.parseBoolean(PropertyUtil.getProperty("redis.test.return"));
    /**redis1节点的ip*/
    private static String redis1Ip = PropertyUtil.getProperty("redis1.ip");
    /**redis2节点的ip*/
    private static String redis2Ip = PropertyUtil.getProperty("redis2.ip");
    /**redis1节点的端口号*/
    private static Integer redis1Port = Integer.parseInt(PropertyUtil.getProperty("redis1.port"));
    /**redis1节点的端口号*/
    private static Integer redis2Port = Integer.parseInt(PropertyUtil.getProperty("redis2.port"));
    private static String redis1pwd = PropertyUtil.getProperty("redis1.pwd");
    private static String redis2pwd = PropertyUtil.getProperty("redis2.pwd");

    static {
        initPool();
    }
    /**
     * 初始化连接池
     */
    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        //当连接耗尽的时候是否阻塞等待获取，false会抛出异常，true则阻塞直到超时
        config.setBlockWhenExhausted(true);

        JedisShardInfo info1 = new JedisShardInfo(redis1Ip,redis1Port,1000*2);
        info1.setPassword(redis1pwd);
        JedisShardInfo info2 = new JedisShardInfo(redis2Ip,redis2Port,1000*2);
        info2.setPassword(redis2pwd);

        List<JedisShardInfo> jedisList = new ArrayList<>(2);
        jedisList.add(info1);
        jedisList.add(info2);

        //Hashing.MURMUR_HASH就是使用的Hash一致性算法,最后一个参数为分片方式
        pool = new ShardedJedisPool(config,jedisList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);

    }

    /**
     * 获取分布式jedis连接池中的连接
     * @return
     */
    public static ShardedJedis getJedis(){
        return pool.getResource();
    }

    /**
     * 返还连接
     * @param jedis
     */
    public static void returnResource(ShardedJedis jedis){
        //jedis2.9以后，close方法已经代替了returnBrokenResource和returnResource方法
        jedis.close();
    }

}
