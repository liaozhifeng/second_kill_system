package com.xinyu.miaosha.controller;

import com.xinyu.miaosha.access.AccessLimit;
import com.xinyu.miaosha.domain.MiaoshaMessage;
import com.xinyu.miaosha.domain.MiaoshaOrder;
import com.xinyu.miaosha.domain.MiaoshaUser;
import com.xinyu.miaosha.domain.OrderInfo;
import com.xinyu.miaosha.rabbitmq.MQSender;
import com.xinyu.miaosha.redis.AccessKey;
import com.xinyu.miaosha.redis.GoodsKey;
import com.xinyu.miaosha.redis.MiaoshaKey;
import com.xinyu.miaosha.result.CodeMsg;
import com.xinyu.miaosha.result.Result;
import com.xinyu.miaosha.service.GoodsService;
import com.xinyu.miaosha.service.MiaoshaService;
import com.xinyu.miaosha.service.OrderService;
import com.xinyu.miaosha.service.RedisService;
import com.xinyu.miaosha.utils.Md5Util;
import com.xinyu.miaosha.utils.UUIDUtil;
import com.xinyu.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender mqSender;

    private Map<Long, Boolean> localOverMap = new HashMap<>();

    public static Logger log = LoggerFactory.getLogger(MiaoshaController.class);

    @PostMapping("/{path}/do_miaosha")
    @ResponseBody
    public Result<Integer> doMiaosha(Model model, MiaoshaUser user,
                            @RequestParam("goodsId")long goodsId,
                                     @PathVariable("path") String path) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        boolean check = miaoshaService.checkPath(path, user.getId(), goodsId);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //判断是否已经做过秒杀了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            model.addAttribute("errMsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }

        //预减库存
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //入队
        MiaoshaMessage message = new MiaoshaMessage();
        message.setUser(user);
        message.setGoodsId(goodsId);
        mqSender.sendMiaoshaMessage(message);
        return Result.success(0); //排队中
        /*
        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        Integer stock = goods.getStockCount();
        if (stock <= 0) {
            model.addAttribute("errMsg", CodeMsg.MIAO_SHA_OVER.getMsg());
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经做过秒杀了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            model.addAttribute("errMsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = miaoshaService.doMiaosha(user, goods);
        return Result.success(orderInfo);
         */
    }

    /**
     * @param model
     * @param user
     * @param goodsId
     * @return orderId 成功, -1失败, 0排队中
     */
    @GetMapping("/result")
    @ResponseBody
    public Result<Long> MiaoshaResult(Model model, MiaoshaUser user,
                                     @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
        return Result.success(result);
    }

    /**
     * 系统初始化
     * @throws Exception 异常
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList == null)
            return;
        for (GoodsVo goods : goodsList) {
            localOverMap.put(goods.getId(), false);
            log.info(String.valueOf(goods));
            redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId(), goods.getStockCount());
        }
    }

    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @GetMapping("/path")
    @ResponseBody
    public Result<String> getPath(MiaoshaUser user,
                                  @RequestParam("goodsId") long goodsId,
                                  @RequestParam("verifyCode") int verifyCode) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        Integer ans = redisService.get(MiaoshaKey.getVerifyCode, user.getId() + "," + goodsId, Integer.class);
        if (ans == null || ans != verifyCode) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        redisService.delete(MiaoshaKey.getVerifyCode, user.getId() + "," + goodsId);
        String path = Md5Util.md5(UUIDUtil.uuid() + "123456");
        redisService.set(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, path);
        return Result.success(path);
    }

    @GetMapping("/verifyCode")
    @ResponseBody
    public Result<String> getVerifyCode(HttpServletResponse response, MiaoshaUser user,
                                        @RequestParam("goodsId") long goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        BufferedImage image = miaoshaService.createVerifyCodeImg(user, goodsId);
        try {
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }
}
