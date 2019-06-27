package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll")
    public List<TbBrand> findAll() {
        return brandService.findAll();
    }

    @RequestMapping("/findByPage")
    public PageInfo findPage(Integer currentPage, Integer pageSize) {
        return brandService.findByPage(currentPage, pageSize);
    }

    @RequestMapping("/save")
    public Result save(@RequestBody TbBrand tbBrand) {
        Result result = null;
        try {
            brandService.save(tbBrand);
            result = new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            result = new Result(false, "添加失败");
        }
        return result;
    }

    @RequestMapping("/findById")
    public TbBrand findById(Long id) {
        return brandService.findById(id);
    }

    @RequestMapping("/updateBrand")
    public Result updateBrand(@RequestBody TbBrand tbBrand) {
        Result result = null;
        try {
            brandService.updateBrand(tbBrand);
            result = new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            result = new Result(false, "修改失败");
        }
        return result;
    }

    @RequestMapping("/deleteBrand")
    public Result deleteBrand(@RequestBody Long[] ids) {
        Result result = null;
        try {
            brandService.deleteBrand(ids);
            result = new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            result = new Result(false, "删除失败");
        }
        return result;
    }

    @RequestMapping("/findPage")
    public PageInfo findPage(@RequestBody TbBrand tbBrand, Integer currentPage, Integer pageSize) {
        return brandService.findByPage(tbBrand, currentPage, pageSize);
    }

    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList() {
        return brandService.selectOptionList();
    }

}
