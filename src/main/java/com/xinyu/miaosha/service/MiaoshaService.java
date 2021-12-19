package com.xinyu.miaosha.service;

import com.xinyu.miaosha.domain.MiaoshaOrder;
import com.xinyu.miaosha.domain.MiaoshaUser;
import com.xinyu.miaosha.domain.OrderInfo;
import com.xinyu.miaosha.redis.MiaoshaKey;
import com.xinyu.miaosha.utils.Calculator;
import com.xinyu.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class MiaoshaService {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    //事务
    @Transactional
    public OrderInfo doMiaosha(MiaoshaUser user, GoodsVo goods) {
        //减库存 下订单 写入秒杀订单, 减库存是否成功
        boolean success = goodsService.reduceStock(goods);
        if (success) {
            //写入两张表 order_info miaosha_order
            return orderService.createOrder(user, goods);
        } else {
            setGoodsOver(goods.getId());
            return null;
        }

    }

    public long getMiaoshaResult(Long userId, long goodsId) {
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
        if (order != null) {
            //成功
            return order.getOrderId();
        } else {
            boolean isOver =  getGoodsOver(goodsId);
            if (isOver) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private void setGoodsOver(Long goodsId) {
        redisService.set(MiaoshaKey.isOver, "" + goodsId, true);
    }

    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(MiaoshaKey.isOver, "" + goodsId);
    }

    public boolean checkPath(String path, Long id, long goodsId) {
        if (id == null || path == null)
            return false;
        String oldPath = redisService.get(MiaoshaKey.getMiaoshaPath, "" + id + "_" + goodsId, String.class);
        if (oldPath == null)
            return false;
        return oldPath.equals(path);
    }

    public BufferedImage createVerifyCodeImg(MiaoshaUser user, long goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }
        int width = 80;
        int height = 32;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, width - 1, height - 1);
        Random random = new Random();

        //生成干扰点
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }

        String verifyCode = createVerifyCode(random);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();

        int ans = calc(verifyCode);
        redisService.set(MiaoshaKey.getVerifyCode, user.getId() + "," + goodsId, ans);
        return image;
    }

    public int calc(String exp) {
        try {
//            ScriptEngineManager manager = new ScriptEngineManager();
//            ScriptEngine engine = manager.getEngineByName("nashorn");
//             return (Integer) engine.eval(exp);
            return (int) Calculator.conversion(exp);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String createVerifyCode(Random random) {
        int num1 = random.nextInt(10);
        int num2 = random.nextInt(10);
        int num3 = random.nextInt(10);

        char[] ops = new char[] {'+', '-', '*'};
        char op1 = ops[random.nextInt(3)];
        char op2 = ops[random.nextInt(3)];

        return "" + num1 + op1 + num2 + op2 + num3;
    }
}
