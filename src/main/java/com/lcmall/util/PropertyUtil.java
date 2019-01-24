package com.lcmall.util;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * properties文件读取文件读取工具
 * @author wlc
 */
public class PropertyUtil {
    private static Properties props;

    /**
     * 加载配置文件内容
     */
    static {
        String fileName = "lcmall.properties";
        props = new Properties();
        try {
            props.load(new InputStreamReader(PropertyUtil.class.getClassLoader().getResourceAsStream(fileName),"UTF-8"));
        } catch (IOException e) {
            System.out.println("配置文件读取异常"+e.toString());
        }
    }

    /**
     * 根据key获取value
     * @param key
     * @return
     */
    public static String getProperty(String key){
        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            return null;
        }
        return value.trim();
    }

    /**
     * 根据key获取value，如获取的value为空，则返回默认值
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getProperty(String key,String defaultValue){

        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            value = defaultValue;
        }
        return value.trim();
    }

}
