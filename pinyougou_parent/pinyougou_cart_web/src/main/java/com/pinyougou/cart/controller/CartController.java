package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    /**
     * 查询购物车列表
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {

        // 获取登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // System.out.println(username);

        // 从cookie中读取购物车列表
        String cartStr = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if ("".equals(cartStr) || cartStr == null) {
            cartStr = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartStr, Cart.class);

        // 匿名用户anonymousUser
        if ("anonymousUser".equals(username)) {     // 未登录，返回cookie中购物车列表
            System.out.println("从cookie中读取购物车列表");
            return cartList_cookie;
        } else {    // 已登录，从redis中读取购物车列表
            System.out.println("从redis中读取购物车列表");
            List<Cart> cartList_redis = cartService.findCartListToRedis(username);
            // 登录后，如果cookie中有购物车列表，合并到redis中
            if (cartList_cookie.size() > 0) {
                // 合并购物车
                List<Cart> cartList = cartService.mergeCartList(cartList_cookie, cartList_redis);
                // 将合并后的购物车存入redis
                cartService.saveCartListToRedis(cartList, username);
                // 清楚cookie中的购物车
                CookieUtil.deleteCookie(request, response, "cartList");
                // 返回合并后的购物车列表，进行展示
                return cartList;
            }
            return cartList_redis;
        }
    }

    /**
     * 添加商品进购物车
     *
     * @param itemID
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105", allowCredentials = "true")      // allowCredentials默认为true，可省略
    public Result addGoodsToCartList(Long itemID, Integer num) {

        // 允许跨域资源访问
//        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        // 允许cookie操作
//        response.setHeader("Access-Control-Allow-Credentials", "true");

        // 获取登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            // 查询购物车列表
            List<Cart> cartList = findCartList();
            // 操作购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemID, num);

            // 匿名用户anonymousUser
            if ("anonymousUser".equals(username)) {     // 未登录，往cookie中存入用户的购物车列表
                // cookie存入新的购物车对象
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
                System.out.println("cookie中存入用户的购物车列表");
            } else {    // 已登录，存入远端（redis）
                cartService.saveCartListToRedis(cartList, username);
                System.out.println("redis中存入用户的购物车列表");
            }
            // 返回结果
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }

}
