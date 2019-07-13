package com.pinyougou.page.service;

/**
 * 商品详细页接口
 */
public interface ItemPageService {

    /**
     * 生成商品详细页
     *
     * @param goodsId 商品id
     * @return 是否成功
     */
    boolean genItemPage(Long goodsId);
}
