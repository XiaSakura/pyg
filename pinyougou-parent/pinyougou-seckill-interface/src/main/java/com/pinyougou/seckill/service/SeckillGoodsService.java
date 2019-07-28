package com.pinyougou.seckill.service;
import java.util.List;

import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillGoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillGoods seckillGoods);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillGoods seckillGoods);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillGoods findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long [] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum,int pageSize);


	/**获取正在参与秒杀的秒杀商品
	 * @return
	 */
	public List<TbSeckillGoods> findList();


	/**根据秒杀商品id获取 秒杀商品详细 从redis里面获取
	 * @param id
	 * @return
	 */
	public TbSeckillGoods findOneFromRedis(Long id);


	public void submitOrder(String username, Long seckillId);


	/**根据用户查询出 秒杀订单
	 * @param username
	 * @return
	 */
	public TbSeckillOrder searchOrderFromRedisByUserId(String username);


	/**先从redis里面取出订单 根据username
	 * 然后修改订单状态
	 * 
	 * @param userId
	 * @param out_trade_no
	 * @param string
	 */
	public void saveOrderFromRedisToDb(String username, String out_trade_no, String transactionId);


	/**当用户超过 一定时间未支付的情况 删除在缓存中的订单
	 * @param userId
	 * @param out_trade_no
	 */
	public void deleteOrderFromRedis(String userId, String out_trade_no);


	
}
