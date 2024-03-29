package com.pinyougou.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;

import entity.Cart;
import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private TbPayLogMapper payLogMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 根据购物车 进行增加 这里是直接从 redis里面获取到的
	 */
	@Override
	public void add(TbOrder order) { // 这个order是获取user的 收件人地址等等 其它的还是要从购物车里面获得
		String userId = order.getUserId();
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		// 这个为了 获得订单id列表 存在订单日志表里面
		List<String> orderIdList = new ArrayList();// 订单ID列表
		double total_money = 0;// 总金额 （元）
		if (cartList != null && cartList.size() > 0) {
			for (Cart cart : cartList) {
				long orderId = idWorker.nextId(); // 这个后面还会用 订单明细表的时候 也会使用
				TbOrder tbOrder = new TbOrder();
				tbOrder.setOrderId(orderId);
				tbOrder.setUserId(userId);
				tbOrder.setPaymentType(order.getPaymentType());// 支付类型
				tbOrder.setStatus("1"); // 这里只是下订单 未付款
				tbOrder.setCreateTime(new Date());// 订单创建日期
				tbOrder.setUpdateTime(new Date());// 订单更新日期 后面的四个时间都没写 物流也没有考虑 买家留言那些还没有 这只是下单
				tbOrder.setReceiver(order.getReceiver());// 收货人
				tbOrder.setReceiverMobile(order.getReceiverMobile()); // 收件人 手机
				tbOrder.setReceiverAreaName(order.getReceiverAreaName());// 地址
				tbOrder.setSourceType(order.getSourceType());// 订单来源
				tbOrder.setSellerId(cart.getSellerId());// 商家ID
				double money = 0;
				// 循环购物车明细
				for (TbOrderItem orderItem : cart.getOrderItemList()) {
					orderItem.setId(idWorker.nextId());
					orderItem.setOrderId(orderId);
					orderItem.setSellerId(cart.getSellerId());
					money += orderItem.getTotalFee().doubleValue();
					orderItemMapper.insert(orderItem);
				}
				tbOrder.setPayment(new BigDecimal(money));
				orderMapper.insert(tbOrder);
				orderIdList.add(orderId + "");// 添加到订单列表
				total_money += money;// 累加到总金额 为了保存到paylog里面
			}
			// 订单生成之后 添加日志
			if ("1".equals(order.getPaymentType())) { // 如果是微信支付
				TbPayLog payLog = new TbPayLog();
				String outTradeNo = idWorker.nextId() + "";// 支付订单号
				payLog.setOutTradeNo(outTradeNo);
				payLog.setCreateTime(new Date());// 创建时间
				// 订单号列表，逗号分隔 先要去掉 []
				String ids = orderIdList.toString().replace("[", "").replace("]", "").replace(" ", "");
				payLog.setOrderList(ids);// 订单号列表，逗号分隔
				payLog.setPayType("1");// 支付类型
				payLog.setTotalFee((long) (total_money * 100));// 总金额(分) 所有金额的累加 分为单位
				payLog.setTradeState("0");// 支付状态 未支付
				payLog.setUserId(order.getUserId());// 用户ID
				payLogMapper.insert(payLog);// 插入到支付日志表
				redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);// 放入缓存 便于生成支付验证码 但是之后肯定是从数据库 里面查出来的
			}
			redisTemplate.boundHashOps("cartList").delete(userId);
		}
	}

	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order) {
		orderMapper.updateByPrimaryKey(order);
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id) {
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			orderMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbOrderExample example = new TbOrderExample();
		Criteria criteria = example.createCriteria();

		if (order != null) {
			if (order.getPaymentType() != null && order.getPaymentType().length() > 0) {
				criteria.andPaymentTypeLike("%" + order.getPaymentType() + "%");
			}
			if (order.getPostFee() != null && order.getPostFee().length() > 0) {
				criteria.andPostFeeLike("%" + order.getPostFee() + "%");
			}
			if (order.getStatus() != null && order.getStatus().length() > 0) {
				criteria.andStatusLike("%" + order.getStatus() + "%");
			}
			if (order.getShippingName() != null && order.getShippingName().length() > 0) {
				criteria.andShippingNameLike("%" + order.getShippingName() + "%");
			}
			if (order.getShippingCode() != null && order.getShippingCode().length() > 0) {
				criteria.andShippingCodeLike("%" + order.getShippingCode() + "%");
			}
			if (order.getUserId() != null && order.getUserId().length() > 0) {
				criteria.andUserIdLike("%" + order.getUserId() + "%");
			}
			if (order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0) {
				criteria.andBuyerMessageLike("%" + order.getBuyerMessage() + "%");
			}
			if (order.getBuyerNick() != null && order.getBuyerNick().length() > 0) {
				criteria.andBuyerNickLike("%" + order.getBuyerNick() + "%");
			}
			if (order.getBuyerRate() != null && order.getBuyerRate().length() > 0) {
				criteria.andBuyerRateLike("%" + order.getBuyerRate() + "%");
			}
			if (order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0) {
				criteria.andReceiverAreaNameLike("%" + order.getReceiverAreaName() + "%");
			}
			if (order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0) {
				criteria.andReceiverMobileLike("%" + order.getReceiverMobile() + "%");
			}
			if (order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0) {
				criteria.andReceiverZipCodeLike("%" + order.getReceiverZipCode() + "%");
			}
			if (order.getReceiver() != null && order.getReceiver().length() > 0) {
				criteria.andReceiverLike("%" + order.getReceiver() + "%");
			}
			if (order.getInvoiceType() != null && order.getInvoiceType().length() > 0) {
				criteria.andInvoiceTypeLike("%" + order.getInvoiceType() + "%");
			}
			if (order.getSourceType() != null && order.getSourceType().length() > 0) {
				criteria.andSourceTypeLike("%" + order.getSourceType() + "%");
			}
			if (order.getSellerId() != null && order.getSellerId().length() > 0) {
				criteria.andSellerIdLike("%" + order.getSellerId() + "%");
			}

		}

		Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

	@Override
	public void updateOrderStatus(String out_trade_no, String transactionId) {
		// 1.修改支付日志状态
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setPayTime(new Date());
		payLog.setTradeState("1");// 已支付
		payLog.setTransactionId(transactionId);// 交易号
		payLogMapper.updateByPrimaryKeySelective(payLog);
		// 2.修改订单状态
		String orderList = payLog.getOrderList();// 获取订单号列表
		String[] orderIds = orderList.split(",");// 获取订单号数组
		for (String orderId : orderIds) {
			TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
			if (order != null) {
				order.setStatus("2");// 已付款
				orderMapper.updateByPrimaryKey(order);
			}
		}
		// 清除redis缓存数据
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());

	}

}
