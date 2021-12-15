package com.xinyu.miaosha.redis;

public class BasePrefix implements KeyPrefix{

    private final int expireSeconds;
    private final String prefix;

    public BasePrefix(String prefix) {this(0, prefix);}
    public BasePrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }
    @Override
    public int expireSeconds() {
        return expireSeconds;
    }

    @Override
    public String getPrefix() {
        String classname = getClass().getSimpleName();
        return classname + ":" + prefix;
    }
}
