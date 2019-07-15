package com.pinyougou.manager.controller;

import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

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

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination queueTextDestination;   // 添加索引库

    @Autowired
    private Destination queueDeleteDestination; // 删除索引库

    @Autowired
    private Destination topicTextDestination;   // 生成静态页

    @Autowired
    private Destination topicDeletePageDestination; // 删除静态页

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
    public Result delete(final Long[] ids) {
        try {
            goodsService.delete(ids);

            // 删除spu商品，同时删除索引库中的sku信息
            //searchService.deleList(Arrays.asList(ids));
            jmsTemplate.send(queueDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });

            // 删除spu，同时删除sku静态页
            jmsTemplate.send(topicDeletePageDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });

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
//                    searchService.saveList(tbItems);
                    final String itemsStr = JSON.toJSONString(tbItems);
                    jmsTemplate.send(queueTextDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(itemsStr);
                        }
                    });
                } else {
                    System.out.println("没有sku商品。");
                }

                // 审核通过时，生成静态html
                for (final Long id : goodsId) {
                    //pageService.genItemPage(id);
                    jmsTemplate.send(topicTextDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(id + "");
                        }
                    });
                }
            }
            result = new Result(true, "成功");
        } catch (Exception e) {
            e.printStackTrace();
            result = new Result(false, "失败");
        }
        return result;
    }

    /**
     * 生成静态页，测试
     *
     * @param goodsId
     */
    @RequestMapping("/genHtml")
    public void genHtml(Long goodsId) {
        //pageService.genItemPage(goodsId);
    }

}
