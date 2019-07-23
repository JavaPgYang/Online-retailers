package com.pinyougou.seckill.service;

import java.util.List;

import com.pinyougou.pojo.TbSeckillOrder;

import entity.PageResult;

/**
 * 服务层接口
 *
 * @author Administrator
 */
public interface SeckillOrderService {

    /**
     * 返回全部列表
     *
     * @return
     */
    public List<TbSeckillOrder> findAll();


    /**
     * 返回分页列表
     *
     * @return
     */
    public PageResult findPage(int pageNum, int pageSize);


    /**
     * 增加
     */
    public void add(TbSeckillOrder seckillOrder);


    /**
     * 修改
     */
    public void update(TbSeckillOrder seckillOrder);


    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    public TbSeckillOrder findOne(Long id);


    /**
     * 批量删除
     *
     * @param ids
     */
    public void delete(Long[] ids);

    /**
     * 分页
     *
     * @param pageNum  当前页 码
     * @param pageSize 每页记录数
     * @return
     */
    public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize);

    /**
     * 提交秒杀订单
     *
     * @param seckillId
     * @param userId
     */
    void submitOrder(Long seckillId, String userId);

    /**
     * 从缓存中读取用户订单信息
     *
     * @param userId
     * @return
     */
    TbSeckillOrder findOderFromRedis(String userId);

    /**
     * 支付成功后保存订单，此方法用来保存订单到DB
     *
     * @param userId
     * @param orderId
     * @param transaction_id
     */
    void saveOrderFromRedisToDb(String userId, Long orderId, String transaction_id);

    /**
     * 支付超时后的处理（删除缓存中的订单，释放库存）
     *
     * @param userId
     * @param orderId
     */
    void deleteOrderFromRedis(String userId, Long orderId);

}
