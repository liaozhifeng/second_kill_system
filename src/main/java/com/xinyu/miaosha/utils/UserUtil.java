package com.xinyu.miaosha.utils;

import com.xinyu.miaosha.dao.MiaoshaUserDao;
import com.xinyu.miaosha.domain.MiaoshaUser;
import com.xinyu.miaosha.redis.MiaoshaUserKey;
import com.xinyu.miaosha.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class UserUtil {

    @Autowired
    MiaoshaUserDao userDao;

    @Autowired
    RedisService redisService;

    public void createUser(int count) throws IOException {
        File file = new File("D:/token.txt");
        if(!file.exists()){
            file.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8));
        for (int i = 0; i < count; i++) {
            MiaoshaUser user = new MiaoshaUser();
            user.setId(13000000000L+i);
            user.setLoginCount(1);
            user.setNickname("user"+i);
            user.setRegisterDate(new Date());
            user.setSalt("1a2b3c");
            user.setPassword(Md5Util.inputPassToDbPass("123456" + i, user.getSalt()));
            userDao.insertMiaoshaUser(user);

            String token = UUIDUtil.uuid();
            redisService.set(MiaoshaUserKey.token, token, user);
            writer.write(user.getId() + "," + token + "\n");
        }
        writer.close();
    }
}
