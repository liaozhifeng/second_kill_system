package com.xinyu.miaosha.controller;

import com.xinyu.miaosha.domain.MiaoshaOrder;
import com.xinyu.miaosha.domain.MiaoshaUser;
import com.xinyu.miaosha.domain.OrderInfo;
import com.xinyu.miaosha.result.CodeMsg;
import com.xinyu.miaosha.service.GoodsService;
import com.xinyu.miaosha.service.MiaoshaService;
import com.xinyu.miaosha.service.OrderService;
import com.xinyu.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;


    @RequestMapping("/do_miaosha")
    public String doMiaosha(Model model, MiaoshaUser user,
                            @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return "login";
        }
        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        Integer stock = goods.getStockCount();
        if (stock <= 0) {
            model.addAttribute("errMsg", CodeMsg.MIAO_SHA_OVER.getMsg());
            return "miaosha_fail";
        }
        //判断是否已经做过秒杀了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            model.addAttribute("errMsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
            return "miaosha_fail";
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = miaoshaService.doMiaosha(user, goods);
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("goods", goods);

        return "order_detail";
    }
}
