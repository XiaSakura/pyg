package com.pinyougou.seckill.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillGoodsService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillGoods> findAll() {
		return seckillGoodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbSeckillGoods> page = (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillGoods seckillGoods) {
		seckillGoodsMapper.insert(seckillGoods);
	}

	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillGoods seckillGoods) {
		seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillGoods findOne(Long id) {
		return seckillGoodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			seckillGoodsMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();

		if (seckillGoods != null) {
			if (seckillGoods.getTitle() != null && seckillGoods.getTitle().length() > 0) {
				criteria.andTitleLike("%" + seckillGoods.getTitle() + "%");
			}
			if (seckillGoods.getSmallPic() != null && seckillGoods.getSmallPic().length() > 0) {
				criteria.andSmallPicLike("%" + seckillGoods.getSmallPic() + "%");
			}
			if (seckillGoods.getSellerId() != null && seckillGoods.getSellerId().length() > 0) {
				criteria.andSellerIdLike("%" + seckillGoods.getSellerId() + "%");
			}
			if (seckillGoods.getStatus() != null && seckillGoods.getStatus().length() > 0) {
				criteria.andStatusLike("%" + seckillGoods.getStatus() + "%");
			}
			if (seckillGoods.getIntroduction() != null && seckillGoods.getIntroduction().length() > 0) {
				criteria.andIntroductionLike("%" + seckillGoods.getIntroduction() + "%");
			}

		}

		Page<TbSeckillGoods> page = (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbSeckillGoods> findList() {
		// 注意这里是用hash来存放的 values是直接获取value 因为之后秒杀商品详细页还会使用id 来获取商品信息 就可以直接从缓存里面读取了
		List<TbSeckillGoods> list = redisTemplate.boundHashOps("seckillGoods").values();
		if (list == null || list.size() <= 0) {
			TbSeckillGoodsExample example = new TbSeckillGoodsExample();
			Criteria criteria = example.createCriteria();
			criteria.andStatusEqualTo("1"); // 商品审核通过
			criteria.andStockCountGreaterThan(0);// 剩余库存大于0
			criteria.andStartTimeLessThanOrEqualTo(new Date());// 开始时间小于等于当前时间
			criteria.andEndTimeGreaterThan(new Date());// 结束时间大于当前时间
			list = seckillGoodsMapper.selectByExample(example);
			System.out.println("将秒杀商品列表装入缓存");
			if (list != null && list.size() > 0) {
				for (TbSeckillGoods seckillGoods : list) {
					redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods); // 根据id 利用hash
				}
			}
		}
		return list;
	}

	@Override
	public TbSeckillGoods findOneFromRedis(Long id) {
		return (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);
	}

	@Override
	public void submitOrder(String username, Long seckillId) {
		// 从缓存中提取秒杀商品
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
		if (seckillGoods == null) { // 如果商品不存在 因为有可能恶意输入链接id
			throw new RuntimeException("商品不存在");
		}
		if (seckillGoods.getStockCount() <= 0) {
			throw new RuntimeException("商品已卖完");
		}
		// 扣减（redis）库存
		seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
		redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);// 放回缓存
		// 如果商品已经被秒光了的情况 删除缓存中的商品 并同步到数据库里面
		if (seckillGoods.getStockCount() == 0) {
			seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
			redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
		}
		// 保存（redis）订单 这里不向数据库存储 只有当用户支付的时候才存储
		long orderId = idWorker.nextId();
		TbSeckillOrder record = new TbSeckillOrder();
		record.setId(orderId);
		record.setSeckillId(seckillId);
		record.setCreateTime(new Date());
		record.setMoney(seckillGoods.getCostPrice());
		record.setUserId(username);
		record.setSellerId(seckillGoods.getSellerId());
		record.setStatus("0");// 状态 未支付
		redisTemplate.boundHashOps("seckillOrder").put(username, record);
	}

	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String username) {
		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(username);
	}

	@Override
	public void saveOrderFromRedisToDb(String username, String out_trade_no, String transactionId) {
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(username);
		if (seckillOrder == null) {
			throw new RuntimeException("订单不存在");
		}
		// 如果与传递过来的订单号不符
		if ((seckillOrder.getId().longValue()) != (Long.valueOf(out_trade_no))) {
			throw new RuntimeException("订单不相符");
		}
		seckillOrder.setTransactionId(transactionId);// 交易流水号
		seckillOrder.setPayTime(new Date());// 支付时间
		seckillOrder.setStatus("1");// 状态
		seckillOrderMapper.insert(seckillOrder);// 保存到数据库
		redisTemplate.boundHashOps("seckillOrder").delete(username);// 从redis中清除
	}

	@Override
	public void deleteOrderFromRedis(String username, String out_trade_no) {
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(username);
		if (seckillOrder == null) {
			throw new RuntimeException("订单不存在");
		}
		// 如果与传递过来的订单号不符
		if ((seckillOrder.getId().longValue()) != (Long.valueOf(out_trade_no))) {
			throw new RuntimeException("订单不相符");
		}
		redisTemplate.boundHashOps("seckillOrder").delete(username);// 删除缓存中的订单
		// 恢复库存
		// 读取redis里面的商品
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods")
				.get(seckillOrder.getSeckillId());
		// 注意还有seckillGoods 已经为空的情况下 我们需要重新设置属性
		if (seckillGoods != null) {
			seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
			redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
		} else {
			//从数据库里面读取属性
			TbSeckillGoods seckillGoods2 = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
			seckillGoods2.setStockCount(seckillGoods2.getStockCount()+1);
			redisTemplate.boundHashOps("seckillGoods").put(seckillGoods2.getId(), seckillGoods2);
		}

	}

}
