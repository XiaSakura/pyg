package com.pinyougou.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;

@Component
public class SeckillTask {

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	/**
	 * 刷新秒杀商品 每分钟一次 增量更新
	 */
	@Scheduled(cron = "0 * * * * ?")
	public void refreshSeckillGoods() {
		// 查询正在秒杀的商品列表 从mysql数据库里面
		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");
		criteria.andStockCountGreaterThan(0);
		criteria.andStartTimeLessThanOrEqualTo(new Date());// 开始时间小于等于当前时间
		criteria.andEndTimeGreaterThan(new Date());// 结束时间大于当前时间
		List<Long> ids = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
		if (ids != null && ids.size() > 0) {
			criteria.andIdNotIn(ids);
		}
		List<TbSeckillGoods> list = seckillGoodsMapper.selectByExample(example);
		// 存入缓存中
		for (TbSeckillGoods tbSeckillGoods : list) {
			redisTemplate.boundHashOps("seckillGoods").put(tbSeckillGoods.getId(), tbSeckillGoods);
		}
	}

	/**
	 * 删除过期的秒杀产品
	 */
	@Scheduled(cron = "0 * * * * ?")
	public void removeSeckillGoods() {
		List<TbSeckillGoods> list = redisTemplate.boundHashOps("seckillGoods").values();
		for (TbSeckillGoods tbSeckillGoods : list) {
			// 如果结束日期小于当前日期，则表示过期
			if ((tbSeckillGoods.getEndTime().getTime()) < (new Date().getTime())) {
				seckillGoodsMapper.updateByPrimaryKey(tbSeckillGoods);// 向数据库保存记录
				redisTemplate.boundHashOps("seckillGoods").delete(tbSeckillGoods.getId());// 移除缓存数据
			}
		}
	}

}
