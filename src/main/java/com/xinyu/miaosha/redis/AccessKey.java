package com.xinyu.miaosha.redis;

public class AccessKey extends BasePrefix{

    public AccessKey(String prefix) {super(0, prefix);}
    public AccessKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    public static AccessKey accessKey(int expireSeconds) {
        return new AccessKey(expireSeconds, "access");
    }
}
