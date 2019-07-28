package com.pinyougou.seckill.controller;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillGoodsService;

import entity.PageResult;
import entity.Result;

/**
 * controller
 * @author Administrator
 *
 */
/**
 * @author q9826
 *
 */
@RestController
@RequestMapping("/seckillGoods")
public class SeckillGoodsController {

	@Reference(timeout = 10000)
	private SeckillGoodsService seckillGoodsService;
	
	

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbSeckillGoods> findAll() {
		return seckillGoodsService.findAll();
	}

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows) {
		return seckillGoodsService.findPage(page, rows);
	}

	/**
	 * 增加
	 * 
	 * @param seckillGoods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbSeckillGoods seckillGoods) {
		try {
			seckillGoodsService.add(seckillGoods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}

	/**
	 * 修改
	 * 
	 * @param seckillGoods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbSeckillGoods seckillGoods) {
		try {
			seckillGoodsService.update(seckillGoods);
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
	public TbSeckillGoods findOne(Long id) {
		return seckillGoodsService.findOne(id);
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
			seckillGoodsService.delete(ids);
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	/**
	 * 查询+分页
	 * 
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbSeckillGoods seckillGoods, int page, int rows) {
		return seckillGoodsService.findPage(seckillGoods, page, rows);
	}

	// 前端方法

	/**
	 * 当前正在参与秒杀的商品
	 * 
	 * @return
	 */
	@RequestMapping("/findSeckillGoods")
	public List<TbSeckillGoods> findSeckillGoods() {
		return seckillGoodsService.findList();
	}

	/**
	 * 获取实体
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOneFromRedis")
	public TbSeckillGoods findOneFromRedis(Long id) {
		return seckillGoodsService.findOneFromRedis(id);
	}

	/**
	 * 提交订单
	 * 
	 * @param seckillId
	 * @return
	 */
	@RequestMapping("/submitOrder")
	public Result submitOrder(Long id) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		if("anonymousUser".equals(username)){//如果未登录
			return new Result(false, "用户未登录,请你登录");
		}
		try {
			seckillGoodsService.submitOrder(username,id);
			return new Result(true, "提交订单成功");
		} 
		catch (RuntimeException e) {
			e.printStackTrace();
			return new Result(false, e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "提交订单失败");
		}
	}
	
	
	
	

}
