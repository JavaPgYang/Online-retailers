package com.pinyougou.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.SnowFlake;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper orderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SnowFlake snowFlake;

    @Autowired
    private TbOrderItemMapper orderItemMapper;

    @Autowired
    private TbPayLogMapper payLogMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbOrder> findAll() {
        return orderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbOrder order) {
        // 得到购物车列表，用来提交订单
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

        // 定义支付总金额（支付日志表）
        double total_fee = 0;

        // 定义订单编号集合（支付日志表）
        List<Long> orderIdList = new ArrayList<Long>();

        // 每个购物车对象代表一个订单，因此需要循环购物车列表
        for (Cart cart : cartList) {
            // 新建订单对象
            TbOrder tbOrder = new TbOrder();
            long orderId = snowFlake.nextId();
            tbOrder.setOrderId(orderId);    // 订单ID
            tbOrder.setPaymentType(order.getPaymentType());     // 支付类型
            tbOrder.setStatus("1");     // 订单状态
            tbOrder.setCreateTime(new Date());      // 创建订单的时间
            tbOrder.setUpdateTime(new Date());      // 更新订单的时间
            tbOrder.setUserId(order.getUserId());       // 用户
            tbOrder.setReceiverAreaName(order.getReceiverAreaName());       // 收货人地址
            tbOrder.setReceiverMobile(order.getReceiverMobile());       // 收货人手机
            tbOrder.setReceiver(order.getReceiver());       // 收货人
            tbOrder.setSourceType(order.getSourceType());       // 订单来源
            tbOrder.setSellerId(cart.getSellerId());        // 商家ID

            // 初始化实付金额
            double money = 0;

            // 循环购物车明细列表，存入TbOrderItem
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                // 计算总金额
                money += orderItem.getTotalFee().doubleValue();

                orderItem.setId(snowFlake.nextId());
                orderItem.setOrderId(orderId);
                orderItem.setSellerId(cart.getSellerId());
                orderItemMapper.insert(orderItem);
            }
            tbOrder.setPayment(new BigDecimal(money));   // 实付金额
            orderMapper.insert(tbOrder);
            // 订单总金额
            total_fee += money;
            // 订单ID集合
            orderIdList.add(orderId);
        }

        // 如果使用的微信支付，添加支付日志信息
        if ("1".equals(order.getPaymentType())) {
            TbPayLog payLog = new TbPayLog();
            payLog.setOutTradeNo(snowFlake.nextId() + "");    // 支付订单号
            payLog.setCreateTime(new Date());   // 下单时间
            payLog.setTotalFee((long) (total_fee * 100));   // 订单总金额
            payLog.setUserId(order.getUserId());    // 用户ID
            payLog.setTradeState("0");  // 交易状态设置为未付款
            String orderIdStr = orderIdList.toString().replace("[", "").replace("]", "").replace(" ", "");
            payLog.setOrderList(orderIdStr);    // 订单ID集合
            payLog.setPayType("1");     // 支付类型
            payLogMapper.insert(payLog);
            // 支付日志放入缓存
            redisTemplate.boundHashOps("payLog").put(payLog.getUserId(), payLog);
        }

        // 清除redis中的购物车
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());

    }


    /**
     * 修改
     */
    @Override
    public void update(TbOrder order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbOrder findOne(Long id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            orderMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbOrderExample example = new TbOrderExample();
        Criteria criteria = example.createCriteria();

        if (order != null) {
            if (order.getPaymentType() != null && order.getPaymentType().length() > 0) {
                criteria.andPaymentTypeLike("%" + order.getPaymentType() + "%");
            }
            if (order.getPostFee() != null && order.getPostFee().length() > 0) {
                criteria.andPostFeeLike("%" + order.getPostFee() + "%");
            }
            if (order.getStatus() != null && order.getStatus().length() > 0) {
                criteria.andStatusLike("%" + order.getStatus() + "%");
            }
            if (order.getShippingName() != null && order.getShippingName().length() > 0) {
                criteria.andShippingNameLike("%" + order.getShippingName() + "%");
            }
            if (order.getShippingCode() != null && order.getShippingCode().length() > 0) {
                criteria.andShippingCodeLike("%" + order.getShippingCode() + "%");
            }
            if (order.getUserId() != null && order.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + order.getUserId() + "%");
            }
            if (order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0) {
                criteria.andBuyerMessageLike("%" + order.getBuyerMessage() + "%");
            }
            if (order.getBuyerNick() != null && order.getBuyerNick().length() > 0) {
                criteria.andBuyerNickLike("%" + order.getBuyerNick() + "%");
            }
            if (order.getBuyerRate() != null && order.getBuyerRate().length() > 0) {
                criteria.andBuyerRateLike("%" + order.getBuyerRate() + "%");
            }
            if (order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0) {
                criteria.andReceiverAreaNameLike("%" + order.getReceiverAreaName() + "%");
            }
            if (order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + order.getReceiverMobile() + "%");
            }
            if (order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0) {
                criteria.andReceiverZipCodeLike("%" + order.getReceiverZipCode() + "%");
            }
            if (order.getReceiver() != null && order.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + order.getReceiver() + "%");
            }
            if (order.getInvoiceType() != null && order.getInvoiceType().length() > 0) {
                criteria.andInvoiceTypeLike("%" + order.getInvoiceType() + "%");
            }
            if (order.getSourceType() != null && order.getSourceType().length() > 0) {
                criteria.andSourceTypeLike("%" + order.getSourceType() + "%");
            }
            if (order.getSellerId() != null && order.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + order.getSellerId() + "%");
            }

        }

        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public TbPayLog searchPayLogFromRedis(String userId) {
        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
    }

    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
        // 更新日志记录
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        payLog.setPayTime(new Date());  // 支付完成时间
        payLog.setTransactionId(transaction_id);    // 完成交易流水号
        payLog.setTradeState("1");      // 交易状态设置为已付款

        // 得到订单id数组
        String[] orderIds = payLog.getOrderList().split(",");
        for (String orderId : orderIds) {
            // 循环查询每一条订单
            TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
            if (tbOrder != null) {
                // 更新订单状态为已付款
                tbOrder.setStatus("2");
                orderMapper.updateByPrimaryKey(tbOrder);    // 保存订单修改
            }
        }
        // 保存日志修改
        payLogMapper.updateByPrimaryKey(payLog);

        // 清除缓存中日志记录
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());

    }

}
