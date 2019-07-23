package com.pinyougou.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillTask {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 秒杀商品放入缓存的增量更新
     */
    @Scheduled(cron = "0 * * * * ?")
    public void refreshSeckillGoods() {
        // 所有已存在缓存的秒杀商品id
        List keys = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
        System.out.println("已经缓存的商品ID：" + keys + "____" + new Date());

        // 从数据库中读取秒杀商品列表
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");           // 已审核
        criteria.andStockCountGreaterThan(0);     // 剩余库存大于0
        criteria.andStartTimeLessThanOrEqualTo(new Date());      // 当前时间需要大于开始秒杀时间
        criteria.andEndTimeGreaterThan(new Date());     // 当前时间需要小于结束秒杀时间
        if (keys.size() > 0) {
            criteria.andIdNotIn(keys);      // 没有缓存的商品id
        }
        List<TbSeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);

        // 放入缓存
        for (TbSeckillGoods seckillGood : seckillGoods) {
            redisTemplate.boundHashOps("seckillGoods").put(seckillGood.getId(), seckillGood);
            System.out.println("开始缓存了：" + seckillGood.getId());
        }
    }

    /**
     * 过期秒杀商品的缓存移除
     */
    @Scheduled(cron = "* * * * * ?")
    public void removeSeckillGoods() {
        // 获取缓存中的秒杀商品
        List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps("seckillGoods").values();
        for (TbSeckillGoods seckillGood : seckillGoods) {
            if (seckillGood.getEndTime().getTime() < new Date().getTime()) {
                // 同步到DB
                seckillGoodsMapper.updateByPrimaryKey(seckillGood);
                // 清除缓存中的过期商品
                redisTemplate.boundHashOps("seckillGoods").delete(seckillGood.getId());
                System.out.println("移除了过期商品：" + seckillGood.getId());
            }
        }
    }
}
