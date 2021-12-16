package com.xinyu.miaosha.service;

import com.alibaba.fastjson.JSON;
import com.xinyu.miaosha.redis.KeyPrefix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        String realKey = prefix.getPrefix() + key;
        String str = (String) redisTemplate.opsForValue().get(realKey);
        return stringToBean(str, clazz);
    }

    public <T> boolean set(KeyPrefix prefix, String key, T value) {
        String str = beanToString(value);
        if (!StringUtils.hasLength(str)) {
            return false;
        }
        String realKey = prefix.getPrefix() + key;
        int seconds = prefix.expireSeconds();
        if (seconds <= 0) {
            redisTemplate.opsForValue().set(realKey, str);
        } else  {
            redisTemplate.opsForValue().set(realKey, str, seconds, TimeUnit.SECONDS);
        }
        return true;
    }

    public <T> boolean exists(KeyPrefix prefix, String key) {
        String realKey = prefix.getPrefix() + key;
        return Boolean.TRUE.equals(redisTemplate.hasKey(realKey));
    }

    public <T> Long incr(KeyPrefix prefix, String key) {
        String realKey = prefix.getPrefix() + key;
        return redisTemplate.opsForValue().increment(realKey);
    }

    public <T> Long decr(KeyPrefix prefix, String  key) {
        String realKey = prefix.getPrefix() + key;
        return redisTemplate.opsForValue().decrement(realKey);
    }

    private <T> String beanToString(T value) {
        if (value == null)
            return null;
        Class<?> clazz = value.getClass();
        if (clazz == Integer.class) {
            return "" + value;
        } else if (clazz == Long.class) {
            return "" + value;
        } else if (clazz == String.class) {
            return (String) value;
        } else {
            return JSON.toJSONString(value);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T stringToBean(String str, Class<T> clazz) {
        if (!StringUtils.hasLength(str) || clazz == null) {
            return null;
        } else if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == String.class) {
            return (T)str;
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str);
        } else {
            return JSON.parseObject(str, clazz);
        }
    }
}
