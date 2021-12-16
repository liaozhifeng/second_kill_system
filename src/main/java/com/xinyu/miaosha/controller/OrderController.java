package com.xinyu.miaosha.controller;

import com.xinyu.miaosha.domain.MiaoshaUser;
import com.xinyu.miaosha.domain.OrderInfo;
import com.xinyu.miaosha.result.CodeMsg;
import com.xinyu.miaosha.result.Result;
import com.xinyu.miaosha.service.GoodsService;
import com.xinyu.miaosha.service.OrderService;
import com.xinyu.miaosha.vo.GoodsVo;
import com.xinyu.miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    @RequestMapping("detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, MiaoshaUser user,
                                    @RequestParam("orderId") long orderId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OrderInfo order = orderService.getOrderById(orderId);
        if (order == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo vo = new OrderDetailVo();
        vo.setOrder(order);
        vo.setGoods(goodsVo);
        return Result.success(vo);
    }
}
