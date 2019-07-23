package com.pinyougou.seckill.service.impl;

import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.SnowFlake;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private SnowFlake snowFlake;

    /**
     * 查询全部
     */
    @Override
    public List<TbSeckillOrder> findAll() {
        return seckillOrderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.insert(seckillOrder);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillOrder findOne(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            seckillOrderMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSeckillOrderExample example = new TbSeckillOrderExample();
        Criteria criteria = example.createCriteria();

        if (seckillOrder != null) {
            if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + seckillOrder.getUserId() + "%");
            }
            if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + seckillOrder.getSellerId() + "%");
            }
            if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
                criteria.andStatusLike("%" + seckillOrder.getStatus() + "%");
            }
            if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
                criteria.andReceiverAddressLike("%" + seckillOrder.getReceiverAddress() + "%");
            }
            if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + seckillOrder.getReceiverMobile() + "%");
            }
            if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + seckillOrder.getReceiver() + "%");
            }
            if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
                criteria.andTransactionIdLike("%" + seckillOrder.getTransactionId() + "%");
            }

        }

        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 提交秒杀订单
     *
     * @param seckillId
     * @param userId
     */
    @Override
    public void submitOrder(Long seckillId, String userId) {
        // 从缓存中读取秒杀商品
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
        if (seckillGoods == null) {
            throw new RuntimeException("没有此商品");
        }
        if (seckillGoods.getStockCount() == 0) {
            throw new RuntimeException("被抢购一空了。");
        }
        // 减少缓存中的商品库存
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        // 放回缓存，更新缓存商品库存
        redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);

        // 减完库存如果还剩0件，需同步数据到DB，并从缓存中删除此商品
        if (seckillGoods.getStockCount() == 0) {
            // 同步到数据库
            seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
            // 删除缓存
            redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
        }

        // 保存用户的订单信息到redis
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setId(snowFlake.nextId()); // 订单ID
        seckillOrder.setSeckillId(seckillId);   // 秒杀商品ID
        seckillOrder.setMoney(seckillGoods.getCostPrice()); // 支付金额
        seckillOrder.setUserId(userId); // 用户ID
        seckillOrder.setSellerId(seckillGoods.getSellerId());   // 商家ID
        seckillOrder.setCreateTime(new Date()); // 下单时间
        seckillOrder.setStatus("0");    // 订单状态未付款
        redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);   // 存入缓存
    }

    /**
     * 从缓存中读取用户订单信息
     *
     * @param userId
     * @return
     */
    @Override
    public TbSeckillOrder findOderFromRedis(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    /**
     * 支付成功后保存订单，此方法用来保存订单到DB
     *
     * @param userId
     * @param orderId
     * @param transaction_id
     */
    @Override
    public void saveOrderFromRedisToDb(String userId, Long orderId, String transaction_id) {
        // 从缓存中读取订单数据
        TbSeckillOrder seckillOrder = findOderFromRedis(userId);
        if (seckillOrder == null) {
            throw new RuntimeException("订单不存在");
        }
        if (seckillOrder.getId().longValue() != orderId.longValue()) {
            throw new RuntimeException("订单号与已存在订单不符");
        }

        // 修改订单信息
        seckillOrder.setStatus("1");    // 订单状态
        seckillOrder.setPayTime(new Date());    // 支付时间
        seckillOrder.setTransactionId(transaction_id);  // 微信支付完成后返回的交易流水号

        // 保存订单到DB
        seckillOrderMapper.insert(seckillOrder);

        // 清除缓存中的订单信息
        redisTemplate.boundHashOps("seckillOrder").delete(userId);

    }

    /**
     * 支付超时后的处理（删除缓存中的订单，释放库存）
     *
     * @param userId
     * @param orderId
     */
    @Override
    public void deleteOrderFromRedis(String userId, Long orderId) {
        // 从缓存中读取用户订单
        TbSeckillOrder seckillOrder = findOderFromRedis(userId);
        if (seckillOrder != null && seckillOrder.getId().longValue() == orderId.longValue()) {
            // 读取用户下单的商品信息
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());

            // 缓存中有商品，更新库存
            if (seckillGoods != null) {
                // 返回商品的库存
                seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
                // 更新缓存库存
                redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);

            } else {    // 没商品，重新添加一条数据
                TbSeckillGoods tbSeckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
                tbSeckillGoods.setStockCount(1);
                // 加入缓存
                redisTemplate.boundHashOps("seckillGoods").put(tbSeckillGoods.getId(), tbSeckillGoods);
            }
            // 删除用户的订单
            redisTemplate.boundHashOps("seckillOrder").delete(userId);
        }
    }
}
