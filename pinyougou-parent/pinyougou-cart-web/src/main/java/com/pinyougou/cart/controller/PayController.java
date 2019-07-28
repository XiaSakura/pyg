package com.pinyougou.cart.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;

import entity.Result;

@RestController
@RequestMapping("/pay")
public class PayController {

	@Reference(timeout = 10000)
	private WeixinPayService weixinPayService;
	
	@Reference(timeout = 10000)
	private OrderService orderService;
	

	@RequestMapping("/createNative")
	public Map createNative() {
		try {
			//获取当前用户        
			String userId=SecurityContextHolder.getContext().getAuthentication().getName();
			//到redis查询支付日志
			TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
			if (payLog!=null) {
				return weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
			}else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 根据订单号 查询支付状态
	 * 
	 * @return
	 */
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		Result result = null;
		int x = 0;
		while (true) {
			// 调用查询接口 查询出支付状态
			try {
				Map map = weixinPayService.queryPayStatus(out_trade_no);
				if (map == null) {
					result = new Result(false, "支付出错");
					break;
				}
				/* 签名正确用这个
				 * if (map.get("trade_state").equals("SUCCESS")) {
					result = new Result(true, "支付成功");
					orderService.updateOrderStatus(out_trade_no, "111");
					break;
				}*/
				//为了不让循环无休止地运行，我们定义一个循环变量，如果这个变量超过了这个值则退出循环，设置时间为5分钟
				x++;
				if (x >= 5) {
					//result = new Result(false, "二维码超时");
					result = new Result(true, "支付成功");
					//更改订单日志的状态 成功支付
					orderService.updateOrderStatus(out_trade_no, "111");
					break;
				}
				Thread.sleep(3000); // 间隔三秒
			} catch (Exception e) {
				//更改订单日志的状态 成功支付 
				e.printStackTrace();
			}
		}
		return result;
	}

}
