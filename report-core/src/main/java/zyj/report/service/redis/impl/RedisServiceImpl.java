package zyj.report.service.redis.impl;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.query.SortCriterion;
import org.springframework.data.redis.core.query.SortQuery;
import org.springframework.data.redis.core.query.SortQueryBuilder;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import zyj.report.common.util.SerializeUtil;
import zyj.report.service.redis.RedisService;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/29
 */
@Service(value = "redisService")
public class RedisServiceImpl implements RedisService {

    private static String redisCode = "utf-8";
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * @param keys
     */
    public long del(final String... keys) {
        return redisTemplate.execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                long result = 0;
                for (int i = 0; i < keys.length; i++) {
                    result = connection.del(keys[i].getBytes());
                }
                return result;
            }
        });
    }
    /**
     * @param keys
     */
    public long delWithPipline(final String... keys) {

        final AtomicInteger count = new AtomicInteger(0);
         redisTemplate.executePipelined(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                for (int i = 0; i < keys.length; i++) {
                    connection.del(keys[i].getBytes());
                    count.getAndAdd(1);
                }
                return null;
            }
        });
         return count.get();
    }


    public <T extends Serializable> void  set(final String key, final T object) {
        redisTemplate.execute(new RedisCallback() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    connection.set(key.getBytes(redisCode), SerializeUtil.serialize(object));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
               /* if (liveTime > 0) {
                    connection.expire(key, liveTime);
                }*/
                return 1L;
            }
        });
    }


    /**
     * @param key
     * @param value
     * @param liveTime
     */
    public void set(final byte[] key, final byte[] value, final long liveTime) {
        redisTemplate.execute(new RedisCallback() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                connection.set(key, value);
                if (liveTime > 0) {
                    connection.expire(key, liveTime);
                }
                return 1L;
            }
        });
    }

    /**
     * @param key
     * @param value
     * @param liveTime
     */
    public void set(String key, String value, long liveTime) {
        this.set(key.getBytes(), value.getBytes(), liveTime);
    }

    /**
     * @param key
     * @param value
     */
    public void set(String key, String value) {
        this.set(key, value, 0L);
    }

    /**
     * @param key
     * @param value
     */
    public void set(byte[] key, byte[] value) {
        this.set(key, value, 0L);
    }

    /**
     * @param key
     * @return
     */
    public String get(final String key) {
        return redisTemplate.execute(new RedisCallback<String>() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    return new String(connection.get(key.getBytes()), redisCode);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return "";
            }
        });
    }

    public <T> T get(final String key, Class<T> clazz ) {
        return redisTemplate.execute(new RedisCallback<T>() {
            public T doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    return (T) SerializeUtil.unserialize(connection.get(key.getBytes()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    /**
     * @param pattern
     * @return
     */
    public Set keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.exists(key.getBytes());
            }
        });
    }

    /**
     * @return
     */
    public String flushDB() {
        return redisTemplate.execute(new RedisCallback<String>() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                connection.flushDb();
                return "ok";
            }
        });
    }

    /**
     * @return
     */
    public long dbSize() {
        return redisTemplate.execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.dbSize();
            }
        });
    }

    /**
     * @return
     */
    public String ping() {
        return redisTemplate.execute(new RedisCallback<String>() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.ping();
            }
        });
    }

    public <T extends Serializable > void hmsetWithPipline(Map<String,Map<String,T>> kv){
        RedisSerializer keySerializer = redisTemplate.getKeySerializer();
        RedisSerializer hashKeySerializer =  redisTemplate.getHashKeySerializer();
        RedisSerializer hashValueSerializer =  redisTemplate.getHashValueSerializer();
        redisTemplate.execute(new RedisCallback() {

            public Long doInRedis(RedisConnection connection) throws DataAccessException {

                connection.openPipeline();
                boolean pipelinedClosed = false;
                try {

                    for (Map.Entry<String, Map<String, T>> entry : kv.entrySet()) {
                        final LinkedHashMap hashes = new LinkedHashMap(entry.getValue().size());
                        Iterator var = entry.getValue().entrySet().iterator();

                        while (var.hasNext()) {
                            Map.Entry entry1 = (Map.Entry) var.next();
//                            hashes.put(rawHashKey(entry1.getKey()), rawHashValue(entry1.getValue()));
                            hashes.put(Objects.isNull(hashKeySerializer)? rawHashKey(entry1.getKey()):hashKeySerializer.serialize(entry1.getKey()),Objects.isNull(hashValueSerializer)?rawHashValue(entry1.getValue()):hashValueSerializer.serialize(entry1.getValue()));
                        }
                        connection.hMSet(entry.getKey().getBytes(), hashes);
                    }
                    connection.closePipeline();
                    pipelinedClosed = true;
                } finally {
                    if (!pipelinedClosed) {
                        connection.closePipeline();
                    }
                }
                return 1L;
            }
        });
    }

    public List<Map<String, String>>  hgetWithPipline(String field,String... keys ){
        RedisSerializer keySerializer = redisTemplate.getKeySerializer();
        RedisSerializer hashKeySerializer =  redisTemplate.getHashKeySerializer();
        RedisSerializer hashValueSerializer =  redisTemplate.getHashValueSerializer();
       List<Object> result =  redisTemplate.executePipelined(new RedisCallback<Map<String, String>>() {

            @Override
            public Map<String, String> doInRedis(RedisConnection redisConnection) throws DataAccessException {
                for (String key : keys) {
                    redisConnection.hGet(keySerializer.serialize(key), hashKeySerializer.serialize(field));
                }
                return null;
            }
        }, hashValueSerializer);
        return result.stream().filter(o-> !Objects.isNull(o)).map(o->(HashMap<String, String>)o).collect(Collectors.toList());
    }

  /*  public  Map<String,Map<String,String>>  hgetallWithPipline(String... keys ){
        RedisSerializer keySerializer = redisTemplate.getKeySerializer();
        RedisSerializer hashKeySerializer =  redisTemplate.getHashKeySerializer();
        RedisSerializer hashValueSerializer =  redisTemplate.getHashValueSerializer();

        return redisTemplate.execute(new RedisCallback<Map<String,Map<String,String>>>() {

            public Map<String,Map<String,String>> doInRedis(RedisConnection connection) throws DataAccessException {
                connection.openPipeline();
                boolean pipelinedClosed = false;
                try{
                    Map<String,Map<String,String>> result = new HashMap<String,Map<String,String>>();
                    for (String key : keys){
//                        byte[] bytes = connection.hGet(key.getBytes(), field.getBytes());
                        Map<byte[], byte[]>  bytes = connection.hGetAll(Objects.isNull(keySerializer)?rawHashKey(key):keySerializer.serialize(key));
//                        result.put(key,(HashMap) (Objects.isNull(hashValueSerializer) ?  SerializeUtil.unserialize(bytes) : hashValueSerializer.deserialize(bytes)) );
                    }
                    connection.closePipeline();
                    pipelinedClosed = true;
                    return result;
                }finally {
                    if(!pipelinedClosed) {
                        connection.closePipeline();
                    }
                }
            }
        });
    }*/

    public <T> List<T> sortAndGet(String sortKey, String by, String... getPatterns){
//        SortQuery<String> query = SortQueryBuilder.sort("test-user-1").noSort().get("#").get("test-map-*->uid").get("test-map-*->content").build();
        SortCriterion tmp = null;
        if (StringUtils.isBlank(by)){
            tmp =  SortQueryBuilder.sort(sortKey).noSort();
        }
        else tmp =  SortQueryBuilder.sort(sortKey).by(by);

        for (String pattern : getPatterns)
            tmp  = tmp.get(pattern);

        SortQuery<String> query = tmp.build();
        return ((List<T> )redisTemplate.sort(query,redisTemplate.getHashValueSerializer())).stream().filter(o->!Objects.isNull(o)).collect(Collectors.toList());
    }

    public Object hmget(String key , String field){
        return redisTemplate.opsForHash().get(key, field);
    }
    public void hmset(String key, String field, Object object){
        redisTemplate.opsForHash().put(key,field,object);
    }

    public void sadd(String key, String... values){
        redisTemplate.opsForSet().add(key,values);
    }
    public Set<String> smembers(String key){
        return redisTemplate.opsForSet().members(key);
    }
    public Set<String> sunion(List<String> keys){
        return redisTemplate.opsForSet().union(keys.get(0),keys.subList(1,keys.size()));
    }


    byte[] rawHashKey(Object key) {
        Assert.notNull(key, "non null key required");
        return SerializeUtil.serialize(key);
    }
    byte[] rawHashValue(Object value) {
        Assert.notNull(value, "non null value required");
        return SerializeUtil.serialize(value);
    }
    private RedisServiceImpl() {

    }



}
