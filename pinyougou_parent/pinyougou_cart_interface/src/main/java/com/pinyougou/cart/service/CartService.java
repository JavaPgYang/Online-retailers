package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 添加商品到购物车
     *
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 根据用户名，从redis读取购物车列表
     *
     * @param username
     * @return
     */
    List<Cart> findCartListToRedis(String username);

    /**
     * 保护用户的购物车列表
     *
     * @param cartList
     * @param username
     */
    void saveCartListToRedis(List<Cart> cartList, String username);

    /**
     * 合并购物车列表
     *
     * @param cartList1
     * @param cartList2
     * @return
     */
    List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2);

}
