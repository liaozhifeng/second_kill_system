package com.xinyu.miaosha.exception;

import com.xinyu.miaosha.result.CodeMsg;

public class GlobalException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    private CodeMsg msg;

    public GlobalException(CodeMsg msg) {
        super(msg.toString());
        this.msg = msg;
    }

    public CodeMsg getMsg() {
        return msg;
    }
}
