package com.xinyu.miaosha.controller;

import com.xinyu.miaosha.domain.MiaoshaUser;
import com.xinyu.miaosha.redis.GoodsKey;
import com.xinyu.miaosha.result.Result;
import com.xinyu.miaosha.service.GoodsService;
import com.xinyu.miaosha.service.RedisService;
import com.xinyu.miaosha.vo.GoodsDetailVo;
import com.xinyu.miaosha.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

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

    @Autowired
    RedisService redisService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;

    @RequestMapping(value = "/to_list", produces = "text/html")
    @ResponseBody
    public String toList(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user)
            throws ServletException, IOException {
        if (user == null) {
            //request.getRequestDispatcher("/login/to_login").forward(request, response);
            response.sendRedirect("/login/to_login");
            return null;
        }
        //取缓存
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isBlank(html)) {
            return html;
        }
        //获取数据，手动渲染
        List<GoodsVo> goodsVos = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsVos);
        WebContext context = new WebContext(request, response, request.getServletContext(),
                request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", context);
        if (StringUtils.isNotBlank(html)) {
            redisService.set(GoodsKey.getGoodsList, "", html);
        }
        return html;
    }

    @RequestMapping(value = "/to_detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, MiaoshaUser user,
                                        @PathVariable("goodsId")long goodsId) throws IOException {
        if (user == null) {
            //request.getRequestDispatcher("/login/to_login").forward(request, response);
            response.sendRedirect("/login/to_login");
            return null;
        }
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);

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
        GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
        goodsDetailVo.setGoods(goods);
        goodsDetailVo.setMiaoshaStatus(miaoshaStatus);
        goodsDetailVo.setRemainSeconds(remainSeconds);
        goodsDetailVo.setUser(user);

        return Result.success(goodsDetailVo);
    }

    @RequestMapping(value = "/to_detail2/{goodsId}", produces = "text/html")
    @ResponseBody
    public String detail2(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user,
                         @PathVariable("goodsId")long goodsId) throws IOException {
        if (user == null) {
            //request.getRequestDispatcher("/login/to_login").forward(request, response);
            response.sendRedirect("/login/to_login");
            return null;
        }
        model.addAttribute("user", user);

        String html = redisService.get(GoodsKey.getGoodsDetail, "" + goodsId, String.class);
        if (StringUtils.isNotBlank(html)) {
            return html;
        }

        //手动渲染
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

        WebContext context = new WebContext(request, response, request.getServletContext(),
                request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", context);
        if (StringUtils.isNotBlank(html)) {
            redisService.set(GoodsKey.getGoodsDetail, "" + goodsId, html);
        }
        return html;
    }

}
