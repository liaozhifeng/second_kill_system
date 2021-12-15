package com.xinyu.miaosha.exception;


import com.xinyu.miaosha.result.CodeMsg;
import com.xinyu.miaosha.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
    //会对异常进行拦截
    @ExceptionHandler(value = BindException.class)
    public Result<String> bindExceptionHandler(BindException e) {
        List<ObjectError> errors = e.getAllErrors();
        ObjectError error = errors.get(0);
        String message = error.getDefaultMessage();
        return Result.error(CodeMsg.BIND_ERROR.fillArgs(message));
    }

    @ExceptionHandler(value = GlobalException.class)
    public Result<String> globalExceptionHandler(GlobalException e) {
        return Result.error(e.getMsg());
    }
}
