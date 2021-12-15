package com.xinyu.miaosha.controller;

import com.xinyu.miaosha.result.CodeMsg;
import com.xinyu.miaosha.result.Result;
import com.xinyu.miaosha.service.MiaoshaUserService;
import com.xinyu.miaosha.utils.UserUtil;
import com.xinyu.miaosha.utils.ValidatorUtil;
import com.xinyu.miaosha.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;


@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private MiaoshaUserService userService;

    @Autowired
    UserUtil util;

    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping("/to_login")
    public String toLogin() {
        return "login";
    }

    @ResponseBody
    @RequestMapping("/do_login")
    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) throws IOException {
        log.info(loginVo.toString());
        //参数校验
//        String mobile = loginVo.getMobile();
//        String loginPassword = loginVo.getPassword();
//        if (!StringUtils.hasLength(loginPassword)) {
//            return Result.error(CodeMsg.PASSWORD_EMPTY);
//        }
//        if (!StringUtils.hasLength(mobile)) {
//            return Result.error(CodeMsg.MOBILE_EMPTY);
//        }
//        if (!ValidatorUtil.isMobile(mobile)) {
//            return Result.error(CodeMsg.MOBILE_ERROR);
//        }
        //登录, 如果login出现异常, 会直接被拦截, 然后在页面显示了
        userService.login(response, loginVo);
        return Result.success(true);
    }
}
