package com.xinyu.miaosha.service;

import com.xinyu.miaosha.domain.MiaoshaUser;
import com.xinyu.miaosha.domain.OrderInfo;
import com.xinyu.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MiaoshaService {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    //事务
    @Transactional
    public OrderInfo doMiaosha(MiaoshaUser user, GoodsVo goods) {
        //减库存 下订单 写入秒杀订单
        goodsService.reduceStock(goods);

        //写入两张表 order_info miaosha_order
        return orderService.createOrder(user, goods);
    }
}
