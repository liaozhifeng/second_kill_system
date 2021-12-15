package com.xinyu.miaosha.controller;

import com.xinyu.miaosha.domain.MiaoshaUser;
import com.xinyu.miaosha.service.GoodsService;
import com.xinyu.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    GoodsService goodsService;

    @RequestMapping("/to_list")
    public String toList(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user) throws ServletException, IOException {
        if (user == null) {
            //request.getRequestDispatcher("/login/to_login").forward(request, response);
            response.sendRedirect("/login/to_login");
            return null;
        }
        List<GoodsVo> goodsVos = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsVos);
        return "goods_list";
    }

    @RequestMapping("/to_detail/{goodsId}")
    public String detail(HttpServletResponse response, Model model, MiaoshaUser user,
                         @PathVariable("goodsId")long goodsId) throws IOException {
        if (user == null) {
            //request.getRequestDispatcher("/login/to_login").forward(request, response);
            response.sendRedirect("/login/to_login");
            return null;
        }
        model.addAttribute("user", user);
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goods);

        long startDate = goods.getStartDate().getTime();
        long endDate = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int miaoshaStatus;
        int remainSeconds;
        if (now < startDate) {
            miaoshaStatus = 0;
            remainSeconds = (int)((startDate - now)/1000);
        } else if (now > endDate) {
            miaoshaStatus = 2;
            remainSeconds = -1;
        } else {
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("miaoshaStatus", miaoshaStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        return "goods_detail";
    }

}
