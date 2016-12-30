package zyj.report.service.redis;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/29
 */
public interface RedisService {

    /**
     * 通过key删除
     *
     * @param keys
     */
    public abstract long del(String... keys);


    public abstract  <T extends Serializable> void  set(final String key, final T object);

    /**
     * 添加key value 并且设置存活时间(byte)
     *
     * @param key
     * @param value
     * @param liveTime
     */
    public abstract void set(byte[] key, byte[] value, long liveTime);

    /**
     * 添加key value 并且设置存活时间
     *
     * @param key
     * @param value
     * @param liveTime
     *            单位秒
     */
    public abstract void set(String key, String value, long liveTime);

    /**
     * 添加key value
     *
     * @param key
     * @param value
     */
    public abstract void set(String key, String value);

    /**
     * 添加key value (字节)(序列化)
     *
     * @param key
     * @param value
     */
    public abstract void set(byte[] key, byte[] value);

    /**
     * 获取redis value (String)
     *
     * @param key
     * @return
     */
    public abstract String get(String key);


    public abstract <T> T get(final String key, Class<T> clazz );

    /**
     * 通过正则匹配keys
     *
     * @param pattern
     * @return
     */
    public abstract Set keys(String pattern);

    /**
     * 检查key是否已经存在
     *
     * @param key
     * @return
     */
    public abstract boolean exists(String key);

    /**
     * 清空redis 所有数据
     *
     * @return
     */
    public abstract String flushDB();

    /**
     * 查看redis里有多少数据
     */
    public abstract long dbSize();

    /**
     * 检查是否连接成功
     *
     * @return
     */
    public abstract String ping();


    public <T extends Serializable > void hmsetWithPipline(Map<String,Map<String,T>> kv);

    public List<Map<String, String>>  hgetWithPipline(String field,String... keys );

    public Object hmget(String key , String field);

    public void hmset(String key, String field, Object object);

    public <T> List<T> sortAndGet(String sortKey, String by, String... getPatterns);


    public void sadd(String key, String... values);

    public Set<String> smembers(String key);

    public Set<String> sunion(List<String> keys);
}
