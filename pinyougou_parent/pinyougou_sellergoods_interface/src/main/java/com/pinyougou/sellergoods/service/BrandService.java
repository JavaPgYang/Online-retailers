package com.pinyougou.sellergoods.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;

import java.util.List;
import java.util.Map;

/**
 * 品牌接口
 */
public interface BrandService {
    /**
     * 查询所有品牌
     *
     * @return List<TbBrand>
     */
    List<TbBrand> findAll();

    /**
     * 品牌分页
     *
     * @param currentPage 当前页码
     * @param pageSize    每页记录数
     * @return PageInfo对象
     */
    PageInfo findByPage(Integer currentPage, Integer pageSize);

    /**
     * 添加品牌
     *
     * @param TbBrand
     */
    void save(TbBrand tbBrand);

    /**
     * 根据id查询品牌信息
     *
     * @param Integer
     * @return TbBrand
     */
    TbBrand findById(Long id);

    /**
     * 修改品牌信息
     *
     * @param TbBrand
     */
    void updateBrand(TbBrand tbBrand);

    /**
     * 根据id删除品牌
     *
     * @param Long[]
     */
    void deleteBrand(Long[] ids);

    /**
     * 根据条件查询品牌信息
     * @param TbBrand
     * @param Integer
     * @param Integer
     * @return PageInfo
     */
    PageInfo findByPage(TbBrand tbBrand, Integer currentPage, Integer pageSize);

    /**
     * 返回所有品牌信息
     * @return [{id:1,text:“华为”},{},...]
     */
    List<Map> selectOptionList();

}
