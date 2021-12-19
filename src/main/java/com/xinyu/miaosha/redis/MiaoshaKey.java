package com.xinyu.miaosha.redis;

public class MiaoshaKey extends BasePrefix{

    public MiaoshaKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static MiaoshaKey isOver = new MiaoshaKey(0, "over");
    public static MiaoshaKey getMiaoshaPath = new MiaoshaKey(60, "mp");
    public static MiaoshaKey getVerifyCode = new MiaoshaKey(300, "vc");
}
