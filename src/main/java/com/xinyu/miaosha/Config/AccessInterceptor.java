package com.xinyu.miaosha.Config;

import com.alibaba.fastjson.JSON;
import com.xinyu.miaosha.access.AccessLimit;
import com.xinyu.miaosha.domain.MiaoshaUser;
import com.xinyu.miaosha.redis.AccessKey;
import com.xinyu.miaosha.result.CodeMsg;
import com.xinyu.miaosha.result.Result;
import com.xinyu.miaosha.service.MiaoshaUserService;
import com.xinyu.miaosha.service.RedisService;
import com.xinyu.miaosha.utils.UserThreadLocal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Component
public class AccessInterceptor implements HandlerInterceptor {

    @Autowired
    MiaoshaUserService userService;

    @Autowired
    RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (handler instanceof HandlerMethod) {
            MiaoshaUser user = getUser(request, response);
            UserThreadLocal.set(user);
            HandlerMethod method = (HandlerMethod) handler;
            AccessLimit access = method.getMethodAnnotation(AccessLimit.class);
            if (access == null)
                return true;
            else {
                int seconds = access.seconds();
                int maxCount = access.maxCount();
                boolean needLogin = access.needLogin();
                String key = request.getRequestURI();
                if (needLogin) {
                    if (user == null) {
                        render(response, CodeMsg.SERVER_ERROR);
                        return false;
                    } else {
                        key += "_" + user.getId();
                    }
                }
                AccessKey accessKey = AccessKey.accessKey(seconds);
                Integer count = redisService.get(accessKey, key, Integer.class);
                if (count == null) {
                    redisService.set(accessKey, key, 1);
                } else if (count < maxCount) {
                    redisService.incr(accessKey, key);
                } else {
                    render(response, CodeMsg.ACCESS_Limit);
                    return false;
                }
            }
        }
        return true;
    }

    private void render(HttpServletResponse response, CodeMsg cm)throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str  = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
    }

    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
        String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, MiaoshaUserService.COOKIE_NAME_TOKEN);

        //
        if (StringUtils.isBlank(cookieToken) && StringUtils.isBlank(paramToken)) {
            return null;
        }
        String token = StringUtils.isBlank(cookieToken) ? paramToken : cookieToken;
        return userService.getByToken(response, token);
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
