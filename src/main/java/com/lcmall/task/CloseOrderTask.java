package com.lcmall.task;

import com.lcmall.common.Const;
import com.lcmall.service.IOrderService;
import com.lcmall.util.PropertyUtil;
import com.lcmall.util.redis.RedisShardedUtil;
import com.lcmall.util.redis.RedissonManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * 说明：
 * 1.这个定时任务问题其实是在tomcat集群环境下引起的，如果将该定时任务作为一个单独的服务，也就没有这个问题了。
 * 2.如果定时任务做一个分布式集群，可以使用分布式调度平台xxlJob、elasticJob
 *      等，预先将不同的数据分给不同的分片，这样也就很少会产生操作同一条记录导致锁的问题。
 * 3.正式因为这里的定时任务没有作为一个单独的服务，所以会出现并发执行同一条数据的问题
 */
@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private RedissonManager redissonManager;

    /**
     * 当tomcat关闭的时候使用的shutdown的形式，这个方法会调用。
     * 当使用 kill 的方式关闭tomcat的时候，这个方法不会被调用。这个情况下，redis的分布式锁不会被删除，也没有有效时间，导致死锁。
     * 被@PreDestroy修饰的方法会在服务器卸载Servlet的时候运行，并且只会被服务器调用一次，类似于Servlet的destroy()方法。
     * 被@PreDestroy修饰的方法会在destroy()方法之后运行，在Servlet被彻底卸载之前。
     */
    @PreDestroy
    public void delLock() {
        RedisShardedUtil.del(Const.RedisCache.CLOSE_ORDER_TASK_LOCK);

    }

    /**
     * 这个版本的弊端：控制不了集群的并发情况
     */
//    @Scheduled(cron="0 */1 * * * ?")//每1分钟(每个1分钟的整数倍)
    public void closeOrderTaskV1() {
        log.info("关闭订单定时任务启动");
        int hour = Integer.parseInt(PropertyUtil.getProperty("close.order.task.time.hour", "2"));
//        iOrderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }

//    @Scheduled(cron="0 */1 * * * ?")

    public void closeOrderTaskV2() {
        log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertyUtil.getProperty("lock.timeout", "5000"));

        Long setnxResult = RedisShardedUtil.setnx(Const.RedisCache.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
        if (setnxResult != null && setnxResult.intValue() == 1) {
            //如果返回值是1，代表设置成功，获取锁
            closeOrder(Const.RedisCache.CLOSE_ORDER_TASK_LOCK);
        } else {
            log.info("没有获得分布式锁:{}", Const.RedisCache.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("关闭订单定时任务结束");
    }

    /**
     * 这个版本是自己写的redis分布式锁，也能防止进程的意外关闭导致的混乱
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV3() {
        log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertyUtil.getProperty("lock.timeout", "5000"));
        Long setnxResult = RedisShardedUtil.setnx(Const.RedisCache.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
        if (setnxResult != null && setnxResult.intValue() == 1) {
            closeOrder(Const.RedisCache.CLOSE_ORDER_TASK_LOCK);
        } else {
            //未获取到锁，继续判断，判断时间戳，看是否可以重置并获取到锁
            String lockValueStr = RedisShardedUtil.get(Const.RedisCache.CLOSE_ORDER_TASK_LOCK);
            if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)) {
                String getSetResult = RedisShardedUtil.getSet(Const.RedisCache.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
                //再次用当前时间戳getset。
                //返回给定的key的旧值，->旧值判断，是否可以获取锁
                //当key没有旧值时，即key不存在时，返回nil ->获取锁
                //这里我们set了一个新的value值，获取旧的值。
                if (getSetResult == null || (getSetResult != null && StringUtils.equals(lockValueStr, getSetResult))) {
                    //真正获取到锁
                    closeOrder(Const.RedisCache.CLOSE_ORDER_TASK_LOCK);
                } else {
                    log.info("没有获取到分布式锁:{}", Const.RedisCache.CLOSE_ORDER_TASK_LOCK);
                }
            } else {
                log.info("没有获取到分布式锁:{}", Const.RedisCache.CLOSE_ORDER_TASK_LOCK);
            }
        }
        log.info("关闭订单定时任务结束");
    }

    /**
     * 这个版本使用的redisson框架，代码更加优雅，功能更加强大。
     */
//    @Scheduled(cron="0 */1 * * * ?")
    public void closeOrderTaskV4() {
        RLock lock = redissonManager.getRedisson().getLock(Const.RedisCache.CLOSE_ORDER_TASK_LOCK);
        boolean getLock = false;
        try {
            //第1个参数waittime，最好设置成0，否则第2个进程执行的时候，可能设置的等待时间根本就？？？？
            if (getLock = lock.tryLock(0, 50, TimeUnit.SECONDS)) {
                log.info("Redisson获取到分布式锁:{},ThreadName:{}", Const.RedisCache.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertyUtil.getProperty("close.order.task.time.hour", "2"));
//                iOrderService.closeOrder(hour);
            } else {
                log.info("Redisson没有获取到分布式锁:{},ThreadName:{}", Const.RedisCache.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            log.error("Redisson分布式锁获取异常", e);
        } finally {
            if (!getLock) {
                return;
            }
            lock.unlock();
            log.info("Redisson分布式锁释放锁");
        }
    }


    private void closeOrder(String lockName) {
        //有效期50秒，防止死锁。因为是自己测试，需要debug，所以设置成了50秒，线上肯定不能设置这么长时间，一般5秒就够了。
        RedisShardedUtil.expire(lockName, 50);
        log.info("获取{},ThreadName:{}", Const.RedisCache.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertyUtil.getProperty("close.order.task.time.hour", "2"));
        iOrderService.closeOrder(hour);
        RedisShardedUtil.del(Const.RedisCache.CLOSE_ORDER_TASK_LOCK);
        log.info("释放{},ThreadName:{}", Const.RedisCache.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        log.info("===============================");
    }


}
