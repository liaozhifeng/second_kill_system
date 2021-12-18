package com.xinyu.miaosha.redis;

public class MiaoshaKey extends BasePrefix{

    public MiaoshaKey(String prefix) {
        super(prefix);
    }

    public static MiaoshaKey isOver = new MiaoshaKey("over");
}
