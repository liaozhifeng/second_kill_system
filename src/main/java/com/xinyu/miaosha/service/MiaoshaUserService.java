package com.xinyu.miaosha.service;

import com.xinyu.miaosha.dao.MiaoshaUserDao;
import com.xinyu.miaosha.domain.MiaoshaUser;
import com.xinyu.miaosha.exception.GlobalException;
import com.xinyu.miaosha.redis.MiaoshaUserKey;
import com.xinyu.miaosha.result.CodeMsg;
import com.xinyu.miaosha.result.Result;
import com.xinyu.miaosha.utils.Md5Util;
import com.xinyu.miaosha.utils.UUIDUtil;
import com.xinyu.miaosha.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoshaUserService {

    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    MiaoshaUserDao miaoshaUserDao;

    @Autowired
    RedisService redisService;

    public MiaoshaUser getById(Long id) {
        return miaoshaUserDao.getById(id);
    }

    public MiaoshaUser getByToken(HttpServletResponse response, String token) {
        if (!StringUtils.hasLength(token)) {
            return null;    //抛出异常 ?
        }
        MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        //延迟有效期， 注意addCookie函数同时也会把user存放进Redis中
        if (user != null) {
            addCookie(response, token, user);
        }
        return user;    //如果在redis中过期怎么办 ？ 这里没有解决唉
    }

    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPassword = loginVo.getPassword();
        MiaoshaUser user = getById(Long.parseLong(mobile));
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String dbPass = user.getPassword();
        String salt = user.getSalt();
        String calcPass = Md5Util.formPassToDBPass(formPassword, salt);
        if (!calcPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成token
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return true;
    }

    public void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
        //存放进redis中
        redisService.set(MiaoshaUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
