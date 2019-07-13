package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    @Value("${pagedir}")
    private String filepath;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public boolean genItemPage(Long goodsId) {
        try {
            // 获取配置类
            Configuration configuration = freeMarkerConfig.getConfiguration();
            //加载模板
            Template template = configuration.getTemplate("item.ftl");
            // 数据
            Map dataModel = new HashMap();
            // 读取goods表
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            // 读取goodsDesc表
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            // 读取商品三级分类名称
            String category1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String category2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String category3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            // 读取SKU列表
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");
            criteria.andGoodsIdEqualTo(goodsId);
            example.setOrderByClause("is_default desc");
            List<TbItem> itemList = itemMapper.selectByExample(example);
            // 封装数据进行返回显示
            dataModel.put("goods", goods);
            dataModel.put("goodsDesc", goodsDesc);
            dataModel.put("category1", category1);
            dataModel.put("category2", category2);
            dataModel.put("category3", category3);
            dataModel.put("itemList", itemList);

            // 创建Writer生成页面对象
            Writer writer = null;
            for (TbItem item : itemList) {
                writer = new FileWriter(new File(filepath + item.getId() + ".html"));
                // 生成页面
                template.process(dataModel, writer);
            }
            // 关闭资源
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
