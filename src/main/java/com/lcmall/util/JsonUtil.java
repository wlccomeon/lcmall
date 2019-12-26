package com.lcmall.util;

import com.google.common.collect.Lists;
import com.lcmall.po.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Json工具类
 * @author wlc
 */
@Slf4j
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();
    static{
        //对象的所有字段全部列入
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);

        //取消默认转换timestamps形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,false);

        //忽略空Bean转json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,false);

        //所有的日期格式都统一为以下的样式，即yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        //忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }


    /**
     * 对象转换成字符串类型
     * @return
     */
    public static <T> String obj2String(T obj){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String)obj :  objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse Object to String error",e);
            return null;
        }
    }

    /**
     * 对象转换成格式化版的字符串类型
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> String obj2StringPretty(T obj){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String)obj :  objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse Object to String error",e);
            return null;
        }
    }

    /**
     * 将字符串转换为指定的类
     * @param str
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T string2Obj(String str,Class<T> clazz){
        if(StringUtils.isEmpty(str) || clazz == null){
            return null;
        }

        try {
            return clazz.equals(String.class)? (T)str : objectMapper.readValue(str,clazz);
        } catch (Exception e) {
            log.warn("Parse String to Object error",e);
            return null;
        }
    }

    /**
     * 将字符串转换成指定类型的对象
     * @param str 字符串
     * @param typeReference 指定类型的对象
     * @param <T>
     * @return
     */
    public static <T> T string2Obj(String str, TypeReference<T> typeReference){
        if(StringUtils.isEmpty(str) || typeReference == null){
            return null;
        }
        try {
            return (T)(typeReference.getType().equals(String.class)? str : objectMapper.readValue(str,typeReference));
        } catch (Exception e) {
            log.warn("Parse String to Object error",e);
            return null;
        }
    }

    /**
     * 将字符串转换成指定的泛型集合 -- 同上面的方法类似，最后两个参数相当于TypeReference<T>的合成版
     * @param str 待转换的字符串
     * @param collectionClass 集合类型
     * @param elementClasses 对象类型
     * @param <T>
     * @return
     */
    public static <T> T string2Obj(String str,Class<?> collectionClass,Class<?>... elementClasses){
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass,elementClasses);
        try {
            return objectMapper.readValue(str,javaType);
        } catch (Exception e) {
            log.warn("Parse String to Object error",e);
            return null;
        }
    }

    //测试
    public static void main(String[] args) {
        TestPo testPojo = new TestPo();
        testPojo.setName("lc");
        testPojo.setId(666);

        //{"name":"Geely","id":666}
        String json = "{\"name\":\"lc\",\"color\":\"blue\",\"id\":666}";
        TestPo testPojoObject = JsonUtil.string2Obj(json,TestPo.class);
        String testPojoJson = JsonUtil.obj2String(testPojo);
        log.info("testPojoJson:{}",testPojoJson);
        log.info("testPojoObject:{}",testPojoObject);

        User u1 = new User();
        u1.setId(1);
        u1.setEmail("lc@mall.com");
        u1.setCreateTime(new Date());
        String user1Json = JsonUtil.obj2String(u1);
        String user1JsonPretty = JsonUtil.obj2StringPretty(u1);

        log.info("user1Json:{}",user1Json);
        log.info("user1JsonPretty:{}",user1JsonPretty);

        User user = JsonUtil.string2Obj(user1Json,User.class);
        log.info("user:{}",user.toString());

        List<User> userList = Lists.newArrayList();
        User u2 = new User();
        u2.setId(2);
        u2.setEmail("lcU2@mall.com");

        userList.add(u1);
        userList.add(u2);

        String userListStr = JsonUtil.obj2StringPretty(userList);
        log.info("userListStrPretty:{}",userListStr);

        List<User> userListObj1 = JsonUtil.string2Obj(userListStr, new TypeReference<List<User>>() {
        });
        log.info("userListObj1:{}",userListObj1);

        List<User> userListObj2 = JsonUtil.string2Obj(userListStr,List.class,User.class);
        log.info("userListObj2:{}",userListObj2);

        Set<User> userSet = new HashSet<>();
        userSet.add(u1);
        userSet.add(u2);
        String userSetStr = JsonUtil.obj2String(userSet);
        Set<User> userSetObj = JsonUtil.string2Obj(userSetStr,Set.class,User.class);
        log.info("userSetObj:{}",userSetObj);

    }



}
