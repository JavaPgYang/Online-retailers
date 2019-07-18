package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        // 1，根据skuID查询sku明细
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!"1".equals(item.getStatus())) {
            throw new RuntimeException("商品状态不合法");
        }
        // 2，根据sku查询商家id，商家名称
        String sellerId = item.getSellerId();
        String seller = item.getSeller();
        // 3，根据商家id查询购物车列表中是否存在该商家的购物车对象
        Cart cart = searchCartBySellerId(cartList, sellerId);
        // 4，不存在该商家购物车
        if (cart == null) {
            // 4.1 新建该商家购物车对象填入信息
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(seller);
            List<TbOrderItem> orderItemList = new ArrayList<TbOrderItem>();
            // 新建明细对象
            TbOrderItem orderItem = createOrderItem(item, num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            // 4.2 放入购物车列表
            cartList.add(cart);
        } else {    // 5，存在该商家购物车
            // 5.1 根据skuID查询该商品有没有存在该商家购物车对象的商品明细中
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            // 5.2 不存在该商家购物车对象的商品明细，新建该商品明细对象，添加进该商家购物车对象的商品明细列表
            if (orderItem == null) {
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            } else {
                // 5.3 存在该商家购物车对象的商品明细，添加数量，更新价钱
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(orderItem.getPrice().multiply(new BigDecimal(orderItem.getNum())));
                // 5.3.1 如果用户减少商品到0，删除该商品明细
                if (orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);
                }
                // 5.3.2 如果用户减少商品到0，并且该商家购物车对象明细列表无数据，删除该商家购物车对象
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    @Override
    public List<Cart> findCartListToRedis(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null) {
            cartList = new ArrayList<Cart>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(List<Cart> cartList, String username) {
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }

    /**
     * 根据商家ID在购物车列表中查询该商家的购物车对象
     *
     * @param cartList
     * @param sellerId
     * @return 该商家的购物车对象
     */
    public Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    /**
     * 根据skuID在该商家购物车明细列表中查询该sku
     *
     * @param orderItemList
     * @param itemID
     * @return
     */
    public TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemID) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue() == itemID.longValue()) {
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 新建明细对象
     *
     * @param item
     * @param num
     * @return TbOrderItem
     */
    public TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(item.getPrice().multiply(new BigDecimal(num)));
        return orderItem;
    }

}
