package com.pinyougou.cart.service;

import java.util.List;

import entity.Cart;

public interface CartService {
	
	/**需要传入上次的cartList列表 并将itemId和数量 传入到列表中 并返回新的购物车列表
	 * @param cartList
	 * @param itemId
	 * @param num
	 * @return
	 */
	public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);
	
	/**
	 * 从redis中查询购物车 根据用户名
	 * @param username
	 * @return
	 */
	public List<Cart> findCartListFromRedis(String username);
	
	public void saveCartListToRedis(String username,List<Cart> cartList);

	public List<Cart> mergeCartList(List<Cart> cartList_redis, List<Cart> cartList_cookie);
	

	
}
