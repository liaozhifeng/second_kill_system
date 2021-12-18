package com.xinyu.miaosha.service;

import com.xinyu.miaosha.dao.GoodsDao;
import com.xinyu.miaosha.domain.MiaoshaGoods;
import com.xinyu.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Autowired
    GoodsDao goodsDao;

    public List<GoodsVo> listGoodsVo() {
        return goodsDao.listGoodsVo();
    }
    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }
    public boolean reduceStock(GoodsVo goodsVo) {
        MiaoshaGoods goods = new MiaoshaGoods();
        goods.setGoodsId(goodsVo.getId());
        //为什么不直接传一个id就好
        int ret = goodsDao.reduceStock(goods);
        return ret > 0;
    }
}
