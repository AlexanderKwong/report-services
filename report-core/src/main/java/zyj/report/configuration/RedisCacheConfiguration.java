package zyj.report.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.*;
import redis.clients.jedis.JedisPoolConfig;
import zyj.report.aop.BaseDataAop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/29
 */
@Configuration
@EnableCaching
@EnableAspectJAutoProxy
@PropertySource("classpath:/config/redis_configuration.properties")
public class RedisCacheConfiguration extends CachingConfigurerSupport {

    @Autowired
    Environment env;

    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory();
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(env.getRequiredProperty("jedispool.maxIdle", Integer.class));
        jedisPoolConfig.setMaxTotal(env.getRequiredProperty("jedispool.maxTotal", Integer.class));
        jedisPoolConfig.setTestOnBorrow(env.getRequiredProperty("jedispool.testOnBorrow", Boolean.class));

        // Defaults
        redisConnectionFactory.setHostName(env.getProperty("jedispool.hostName"));
        redisConnectionFactory.setPort(env.getRequiredProperty("jedispool.port", Integer.class));
//        redisConnectionFactory.setPassword(env.getProperty("jedispool.password"));
        redisConnectionFactory.setUsePool(env.getRequiredProperty("jedispool.usePool", Boolean.class));

        redisConnectionFactory.setPoolConfig(jedisPoolConfig);
        return redisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
//        RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
        RedisTemplate<String, String> redisTemplate = new StringRedisTemplate(); //用上面那个的话keys()为空
        redisTemplate.setConnectionFactory(cf);
        redisTemplate.setKeySerializer(new StringRedisSerializer());//key的序列化适配器
        redisTemplate.setValueSerializer(new StringRedisSerializer());//value的序列化适配器，也可以自己编写，大部分场景StringRedisSerializer足以满足需求了。
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager(RedisTemplate redisTemplate) {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);

        // Number of seconds before expiration. Defaults to unlimited (0)
        cacheManager.setDefaultExpiration(3000); // Sets the default expire time (in seconds)
        return cacheManager;
    }

    @Bean
    public BaseDataAop aop() {
        return new BaseDataAop();
    }

}
