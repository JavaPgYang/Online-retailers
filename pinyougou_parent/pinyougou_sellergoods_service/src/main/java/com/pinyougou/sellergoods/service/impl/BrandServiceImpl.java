package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper tbBrandMapper;

    @Override
    public List<TbBrand> findAll() {
        return tbBrandMapper.selectByExample(null);
    }

    @Override
    public PageInfo findByPage(Integer currentPage, Integer pageSize) {
        PageHelper.startPage(currentPage, pageSize);
        List<TbBrand> tbBrands = tbBrandMapper.selectByExample(null);
        return new PageInfo(tbBrands);
    }

    @Override
    public void save(TbBrand tbBrand) {
        tbBrandMapper.insert(tbBrand);
    }

    @Override
    public TbBrand findById(Long id) {
        return tbBrandMapper.selectByPrimaryKey(id);
    }

    @Override
    public void updateBrand(TbBrand tbBrand) {
        tbBrandMapper.updateByPrimaryKey(tbBrand);
    }

    @Override
    public void deleteBrand(Long[] ids) {
        for (Long id : ids) {
            tbBrandMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageInfo findByPage(TbBrand tbBrand, Integer currentPage, Integer pageSize) {

        PageHelper.startPage(currentPage,pageSize);

        TbBrandExample example = new TbBrandExample();
        TbBrandExample.Criteria criteria = example.createCriteria();
        if (tbBrand.getName() != null && tbBrand.getName().length() > 0) {
            criteria.andNameLike("%" + tbBrand.getName() + "%");
        }
        if (tbBrand.getFirstChar() != null && tbBrand.getFirstChar().length() > 0) {
            criteria.andFirstCharEqualTo(tbBrand.getFirstChar());
        }
        List<TbBrand> tbBrandList = tbBrandMapper.selectByExample(example);
        PageInfo pageInfo = new PageInfo(tbBrandList);
        return pageInfo;
    }

    @Override
    public List<Map> selectOptionList() {
        return tbBrandMapper.selectOptionList();
    }
}
