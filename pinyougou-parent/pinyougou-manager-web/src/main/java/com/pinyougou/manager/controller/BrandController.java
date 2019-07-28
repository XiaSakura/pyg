package com.pinyougou.manager.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

@RestController
@RequestMapping("/brand")
public class BrandController {

	@Reference(timeout = 10000)
	private BrandService brandService;

	@RequestMapping("/findAll")
	public Object findAll() {
		return brandService.findAll();
	}

	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows) {
		return brandService.findPage(page, rows);
	}

	@RequestMapping("/search")
	public PageResult search(int page, int rows, @RequestBody TbBrand brand) {
		return brandService.findPage(page, rows, brand);
	}

	/**
	 * 增加
	 * 
	 * @param brand @RequestBody 前端是post提交的 json对象 所以要加上@RequestBody
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand brand) {
		try {
			brandService.add(brand);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(true, "增加失败");
		}
	}

	@RequestMapping("findOne")
	public Object findOne(Long id) {
		return brandService.findOne(id);
	}

	/**
	 * 增加
	 * 
	 * @param brand @RequestBody 前端是post提交的 json对象 所以要加上@RequestBody
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand) {
		try {
			brandService.update(brand);
			return new Result(true, "更新成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(true, "更新失败");
		}
	}

	/**
	 * 增加
	 * 
	 * @param brand @RequestBody 前端是post提交的 json对象 所以要加上@RequestBody
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long[] ids) {
		try {
			brandService.delete(ids);
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(true, "删除失败");
		}
	}

	@RequestMapping("/selectBrandList")
	public List<Map> selectBrandList() {

		List<Map> list = new ArrayList<>();
		List<TbBrand> list2 = brandService.findAll();
		if (list2!=null&list2.size()>0) {
			for (TbBrand tbBrand : list2) {
				Map map = new HashMap<>();
				map.put("id", tbBrand.getId());
				map.put("text", tbBrand.getName());
				list.add(map);
			}
		}

		return list;
	}

}
