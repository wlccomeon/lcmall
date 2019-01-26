package com.lcmall.dynamicdbsource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 动态切换数据源，基于AOP实现
 * 由于DynamicDataSource是单例的，线程不安全的，所以采用ThreadLocal保证线程安全，由DynamicDataSourceHolder完成。
 * @author wlc
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDataSource.class);

    private Integer slaveCount;

    /**轮询计数,初始为-1,AtomicInteger是线程安全的*/
    private AtomicInteger counter = new AtomicInteger(-1);

    /**记录读库的key,默认为4个*/
    private List<Object> slaveDataSources = new ArrayList<>(4);

    @Override
    protected Object determineCurrentLookupKey() {
        // 使用DynamicDataSourceHolder保证线程安全，并且得到当前线程中的数据源key
        if (DynamicDataSourceHolder.isMaster()) {
            Object key = DynamicDataSourceHolder.getDataSourceKey();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("当前DataSource的key为: " + key);
            }
            return key;
        }
        Object key = getSlaveKey();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("当前DataSource的key为: " + key);
        }
        return key;

    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        // 由于父类的resolvedDataSources属性是私有的子类获取不到，需要使用反射获取
        Field field = ReflectionUtils.findField(AbstractRoutingDataSource.class, "resolvedDataSources");
        // 设置可访问
        field.setAccessible(true);

        try {
            Map<Object, DataSource> resolvedDataSources = (Map<Object, DataSource>) field.get(this);
            // 读库的数据量等于数据源总数减去写库的数量
            this.slaveCount = resolvedDataSources.size() - 1;
            for (Map.Entry<Object, DataSource> entry : resolvedDataSources.entrySet()) {
                if (DynamicDataSourceHolder.MASTER.equals(entry.getKey())) {
                    continue;
                }
                slaveDataSources.add(entry.getKey());
            }
        } catch (Exception e) {
            LOGGER.error("afterPropertiesSet error! ", e);
        }
    }

    /**
     * 轮询算法实现从库的使用
     *
     * @return
     */
    public Object getSlaveKey() {
        // 得到的下标为：0、1、2、3……
        Integer index = counter.incrementAndGet() % slaveCount;
        // 以免超出Integer范围
        if (counter.get() > 9999) {
            // 还原
            counter.set(-1);
        }
        return slaveDataSources.get(index);
    }



}
