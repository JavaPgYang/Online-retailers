package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {

        Map<String, Object> map = new HashMap<String, Object>();
        // 关键字搜索且高亮
        map.putAll(searchList(searchMap));
        // 查询分类名称列表
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);
        // 查询品牌及规格
        String category = (String) searchMap.get("category");
        if (!"".equals(category)) {
            map.putAll(searchBrandAndSpecList(category));
        } else {
            if (categoryList.size() > 0) {
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }
        return map;
    }

    /**
     * 根据关键字搜索，且高亮显示关键字，过滤查询
     *
     * @param searchMap 搜索Map
     * @return map
     */
    private Map searchList(Map searchMap) {
        Map<String, Object> map = new HashMap<>();

        /*Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> tbItems = solrTemplate.queryForPage(query, TbItem.class);
        map.put("rows", tbItems.getContent());*/

        // 高亮显示条件构建
        HighlightQuery query = new SimpleHighlightQuery();
        // 高亮选项
        HighlightOptions options = new HighlightOptions();
        options.addField("item_title"); // 高亮字段
        options.setSimplePrefix("<em style='color:red'>");// 前缀
        options.setSimplePostfix("</em>");// 后缀
        query.setHighlightOptions(options);
        // 关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        // 根据商品分类进行过滤
        if (!"".equals(searchMap.get("category"))) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria categoryCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(categoryCriteria);
            query.addFilterQuery(filterQuery);
        }
        // 根据品牌进行过滤
        if (!"".equals(searchMap.get("brand"))) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria brandCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(brandCriteria);
            query.addFilterQuery(filterQuery);
        }
        // 根据规格过滤
        if (searchMap.get("spec") != null) {
            Map<String, String> spec = (Map<String, String>) searchMap.get("spec");
            for (String key : spec.keySet()) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria specCriteria = new Criteria("item_spec_" + key).is(spec.get(key));
                filterQuery.addCriteria(specCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        /***************** 开始获取结果 *****************/
        // 高亮页对象
        HighlightPage<TbItem> tbItems = solrTemplate.queryForHighlightPage(query, TbItem.class);
        // 高亮入口集合
        List<HighlightEntry<TbItem>> highlighted = tbItems.getHighlighted();
        for (HighlightEntry<TbItem> tbItemHighlightEntry : highlighted) {
            // 高亮集合，每个字段的高亮，此时只有item_keywords
            List<HighlightEntry.Highlight> highlights = tbItemHighlightEntry.getHighlights();
            if (highlights.size() > 0 && highlights.get(0).getSnipplets().size() > 0) {
                // 高亮列，例 复制域有多值
                String s = highlights.get(0).getSnipplets().get(0);
//                System.out.println(s);
                tbItemHighlightEntry.getEntity().setTitle(s);
            }
        }

        map.put("rows", tbItems.getContent());

        return map;
    }

    /**
     * 查询分类列表
     *
     * @param searchMap 搜索Map
     * @return 分类集合
     */
    private List searchCategoryList(Map searchMap) {
        List<String> categoryList = new ArrayList<String>();

        Query query = new SimpleQuery();
        // 根据关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria); // 相当于where
        // 设置分组选项
        GroupOptions options = new GroupOptions();
        options.addGroupByField("item_category");
        query.setGroupOptions(options);

        // 分组页对象
        GroupPage<TbItem> tbItems = solrTemplate.queryForGroupPage(query, TbItem.class);
        // 取得按照某个域分组的对象
        GroupResult<TbItem> groupResult = tbItems.getGroupResult("item_category");
        // 分组数据入口对象
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        // 分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> tbItemGroupEntry : content) {
            categoryList.add(tbItemGroupEntry.getGroupValue());
        }

        return categoryList;
    }

    /**
     * 查询品牌和规格列表
     *
     * @param category 分类名称
     * @return 品牌和规格列表Map
     */
    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        // 根据分类名称查询其模板id
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);

        if (typeId != null) {
            // 根据模板id查询其品牌列表
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
            // 根据模板id查询其规格列表
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);

            map.put("brandList", brandList);
            map.put("specList", specList);
        }
        return map;
    }


}
