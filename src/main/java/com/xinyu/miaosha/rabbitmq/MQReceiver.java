package com.xinyu.miaosha.rabbitmq;

import com.xinyu.miaosha.domain.MiaoshaMessage;
import com.xinyu.miaosha.domain.MiaoshaOrder;
import com.xinyu.miaosha.domain.MiaoshaUser;
import com.xinyu.miaosha.service.GoodsService;
import com.xinyu.miaosha.service.MiaoshaService;
import com.xinyu.miaosha.service.OrderService;
import com.xinyu.miaosha.service.RedisService;
import com.xinyu.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {

    public static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @RabbitListener(queues = MQConfig.QUEUE)
    public void receive(String message) {
        log.info("receive message:" + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message) {
        log.info("receive topic queue1 message:" + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message) {
        log.info("receive topic queue2 message:" + message);
    }

    @RabbitListener(queues = MQConfig.HEADERS_QUEUE)
    public void receiveHeaderQueue(byte[] message) {
        log.info("receive headers queue message:" + new String(message));
    }

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receiveMiaoshaQueue(String message) {
        log.info("receive message" + message);
        MiaoshaMessage msg = RedisService.stringToBean(message, MiaoshaMessage.class);
        if (msg == null)
            return;
        MiaoshaUser user = msg.getUser();
        long goodsId = msg.getGoodsId();

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock < 0) {
            return;
        }
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return;
        }

        miaoshaService.doMiaosha(user, goods);
    }
}
