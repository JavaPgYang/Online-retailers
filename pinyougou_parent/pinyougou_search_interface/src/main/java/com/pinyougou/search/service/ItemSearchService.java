package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     * 搜索
     *
     * @param searchMap
     * @return
     */
    Map<String, Object> search(Map searchMap);

    /**
     * 新增商品
     *
     * @param list
     */
    void saveList(List list);

    /**
     * 选择性删除索引库
     *
     * @param ids
     */
    void deleList(List ids);

}
