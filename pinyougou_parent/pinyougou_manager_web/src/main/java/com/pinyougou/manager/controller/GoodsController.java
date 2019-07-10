package com.pinyougou.manager.controller;

import java.util.Arrays;
import java.util.List;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;
    @Reference
    private ItemSearchService searchService;

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }


    /**
     * 修改
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            goodsService.update(goods);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.delete(ids);

            // 删除spu商品，同时删除索引库中的sku信息
            searchService.deleList(Arrays.asList(ids));

            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }

    /**
     * 更新审核状态
     *
     * @param goodsId
     * @param status
     * @return
     */
    @RequestMapping("/updateAuditStatus")
    public Result updateAuditStatus(Long[] goodsId, String status) {
        Result result = null;
        try {
            goodsService.updateAuditStatus(goodsId, status);

            // 审核通过时，更新索引库
            if ("1".equals(status)) {
                // 根据spuID查询出所有sku列表
                List<TbItem> tbItems = goodsService.findItemListByGoodsIdandStatus(goodsId, status);
                if (tbItems.size() > 0) {
                    searchService.saveList(tbItems);
                } else {
                    System.out.println("没有sku商品。");
                }
            }

            result = new Result(true, "成功");
        } catch (Exception e) {
            e.printStackTrace();
            result = new Result(false, "失败");
        }
        return result;
    }

}
