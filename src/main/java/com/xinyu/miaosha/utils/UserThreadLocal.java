package com.xinyu.miaosha.utils;

import com.xinyu.miaosha.domain.MiaoshaUser;

public class UserThreadLocal {
    private static final ThreadLocal<MiaoshaUser> userThreadLocal = new ThreadLocal<>();

    public static void set(MiaoshaUser user) {
        userThreadLocal.set(user);
    }

    public static MiaoshaUser get() {
        return userThreadLocal.get();
    }
}
