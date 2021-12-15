package com.xinyu.miaosha.redis;

public interface KeyPrefix {

    //缓存存在的时间
    public int expireSeconds();
    //key的前缀
    public String getPrefix();
}
