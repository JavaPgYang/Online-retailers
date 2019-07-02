package com.pinyougou.sellergoods.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbBrandMapper brandMapper;
    @Autowired
    private TbSellerMapper sellerMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        goods.getGoods().setAuditStatus("0");
        goodsMapper.insert(goods.getGoods());
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        goodsDescMapper.insert(goods.getGoodsDesc());

        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            for (TbItem item : goods.getItemList()) {
                // 商品标题
                String tital = goods.getGoods().getGoodsName();
                Map<String, Object> specMap = JSON.parseObject(item.getSpec());
                for (String key : specMap.keySet()) {
                    tital += " " + specMap.get(key);
                }
                item.setTitle(tital);
                // 其他信息
                setItemValus(goods, item);
                // 保存
                itemMapper.insert(item);
            }
        } else {
            TbItem item = new TbItem();
            // 商品标题
            item.setTitle(goods.getGoods().getGoodsName());
            // 商品价格
            item.setPrice(goods.getGoods().getPrice());
            // 商品状态
            item.setStatus("1");
            // 是否默认
            item.setIsDefault("1");
            // 库存
            item.setNum(99999);
            // sku具体列
            item.setSpec("{}");
            // 其他信息
            setItemValus(goods, item);
            // 保存
            itemMapper.insert(item);
        }
    }

    private void setItemValus(Goods goods, TbItem item) {
        // 商品图片
        List<Map> itemImagesList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (itemImagesList.size() > 0) {
            item.setImage((String) itemImagesList.get(0).get("url"));
        }
        // 商品所属分类（三级分类）
        item.setCategoryid(goods.getGoods().getCategory3Id());
        // 商品录入日期
        item.setCreateTime(new Date());
        // 商品更新日期
        item.setUpdateTime(new Date());
        // 商品id
        item.setGoodsId(goods.getGoods().getId());
        // 商家id
        item.setSellerId(goods.getGoods().getSellerId());
        // 商品所属分类名称
        item.setCategory(itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id()).getName());
        // 商品的品牌
        item.setBrand(brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId()).getName());
        // 商品所属商家
        item.setSeller(sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId()).getNickName());
    }


    /**
     * 修改
     */
    @Override
    public void update(TbGoods goods) {
        goodsMapper.updateByPrimaryKey(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbGoods findOne(Long id) {
        return goodsMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            goodsMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

}
