package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;

import entity.Cart;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {

		// 1.利用itemId 得到item 其中有seller 商家的信息
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if (item == null) {
			throw new RuntimeException("商品不存在");
		}
		if (!item.getStatus().equals("1")) {
			throw new RuntimeException("商品状态无效");
		}
		String sellerId = item.getSellerId();
		String sellerName = item.getSeller();
		// 2.根据sellerid 判断购物车列表中是否存在该商家的购物车 抽取出私有方法来实现
		Cart cart = searchCartBySellerId(cartList, sellerId);
		// 3.如果购物车列表里面没有有该商家的购物车
		if (cart == null) {
			// 3.1 新建购物车对象 对该对象赋值 存入购物车列表中
			cart = new Cart();
			cart.setSellerId(sellerId);
			cart.setSellerName(sellerName);
			List<TbOrderItem> orderItemList = new ArrayList<>();
			TbOrderItem orderItem = createOrderItem(item, num); // 调用方法
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			cartList.add(cart);
		}
		else {// 4.如果有该商家的购物车
				// 4.1 查询购物车明细列表中 是否存在该商品
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
			// 4.1.1 如果存在 只需要对该商品的数量进行修改 并修改金额
			if (orderItem!=null) {
				orderItem.setNum(orderItem.getNum()+num);
				orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));
				//如果数量操作后小于等于0，则移除此商品明细 因为有可能减少商品
				if (orderItem.getNum()<=0) {
					cart.getOrderItemList().remove(orderItem);
				}
			}
			// 4.1.2 如果不存在 新增商品明细
			else {
				orderItem=createOrderItem(item,num);
				cart.getOrderItemList().add(orderItem);
			}
		}

		return cartList;
	}

	/**
	 * 查询购物车明细列表中 是否存在该商品
	 * 
	 * @param orderItemList
	 * @param itemId
	 * @return
	 */
	private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
		for (TbOrderItem tbOrderItem : orderItemList) {
			if (tbOrderItem.getItemId().longValue() == itemId.longValue()) {
				return tbOrderItem;
			}
		}

		return null;
	}

	/**
	 * 根据item创建订单明细
	 * 
	 * @param item
	 * @param num
	 * @return
	 */
	private TbOrderItem createOrderItem(TbItem item, Integer num) {
		if (num <= 0) {
			throw new RuntimeException("数量非法");
		}
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
		return orderItem;
	}

	/**
	 * 在cartList里面查找 是否有该商家的购物车对象
	 * 
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
		for (Cart cart : cartList) {
			if (cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;
	}

	@Override
	public List<Cart> findCartListFromRedis(String username) {
		System.out.println("从redis中提取购物车数据....."+username);
		List<Cart> cartList=(List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		if(cartList==null){
			cartList=new ArrayList();
		}
		return cartList;
	}

	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		System.out.println("向redis存入购物车数据....."+username);
		redisTemplate.boundHashOps("cartList").put(username, cartList);
	}

	@Override
	public List<Cart> mergeCartList(List<Cart> cartList_redis, List<Cart> cartList_cookie) {
		for (Cart cart : cartList_cookie) {
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				cartList_redis = addGoodsToCartList(cartList_redis,orderItem.getItemId(),orderItem.getNum());
			}
		}
		return cartList_redis;
	}

}
