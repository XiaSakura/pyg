package com.pinyougou.cart.controller;

import java.security.Security;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;

import entity.Cart;
import entity.Result;
import util.CookieUtil;

@RestController
@RequestMapping("/cart")
public class CartController {

	@Reference(timeout = 10000)
	private CartService cartService;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private HttpServletResponse response;

	/**
	 * 购物车列表 从cookie里面 取出购物车列表 注意用json进行转换
	 * 
	 * @return
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		// 读取cookie
		String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		// 防止转json报错
		if (cartListString == null || cartListString.equals("")) {
			cartListString = "[]"; // 防止转json报错
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
		if (username.equals("anonymousUser")) { // 匿名用户 未登录
			return cartList_cookie;
		} else {
			// 已经登录 从redis里面读取
			List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
			if (cartList_cookie.size() > 0) {// 如果本地存在购物车 有才去合并 否则效率低
				// 合并购物车
				cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
				CookieUtil.deleteCookie(request, response, "cartList");
				//将合并后的数据存入redis 
				cartService.saveCartListToRedis(username, cartList_redis); 
			}
			return cartList_redis;
		}
	}

	/**
	 * 添加商品到购物车
	 * 
	 * @param itemId
	 * @param num
	 * @return
	 */
	@RequestMapping("/addGoodsToCartList")
	@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
	public Result addGoodsToCartList(Long itemId, Integer num) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登录用户：" + username);
		try {
			List<Cart> cartList = findCartList();// 获取购物车列表
			List<Cart> cartList2 = cartService.addGoodsToCartList(cartList, itemId, num);
			if (username.equals("anonymousUser")) { // 如果是未登录，保存到cookie
				util.CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList2), 3600 * 24,
						"UTF-8");
				System.out.println("向cookie存入数据");
			} else {// 如果是已登录，保存到redis
				cartService.saveCartListToRedis(username, cartList2);
			}
			return new Result(true, "添加成功");
		} catch (RuntimeException e) {
			e.printStackTrace();
			return new Result(false, e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}

}
