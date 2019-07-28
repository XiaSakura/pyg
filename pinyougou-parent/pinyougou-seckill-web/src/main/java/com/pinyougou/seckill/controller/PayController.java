package com.pinyougou.seckill.controller;

import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillGoodsService;

import entity.Result;

/**
 * @author q9826 支付控制器
 */
@RequestMapping("/pay")
@RestController
public class PayController {
	
	@Reference(timeout = 10000)
	private WeixinPayService weixinPayService;
	@Reference(timeout = 10000)
	private SeckillGoodsService seckillGoodsService;

	/**
	 * 生成二维码 这里可以直接从reids里面获取订单 可不用传参
	 * 
	 * @return
	 */
	@RequestMapping("/createNative")
	public Map createNative() {
		// 获取用户
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		// 到redis查询秒杀订单 根据用户查询订单 用户最多同时拥有一个秒杀订单
		TbSeckillOrder seckillOrder = seckillGoodsService.searchOrderFromRedisByUserId(username);
		if (seckillOrder != null) {
			try {
				// 将金额转换成分
				long fen = (long) (seckillOrder.getMoney().doubleValue() * 100);// 金额（分）
				Map map = weixinPayService.createNative(seckillOrder.getId() + "", fen + "");
				return map;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 根据订单号 查询支付状态
	 * 
	 * @return
	 */
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		//获取当前用户        
		String userId=SecurityContextHolder.getContext().getAuthentication().getName();
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
				/* 1.签名正确用这个
				 * if (map.get("trade_state").equals("SUCCESS")) {
					result = new Result(true, "支付成功");
					orderService.updateOrderStatus(out_trade_no,(String)map.get("transaction_id"));
					break;
				}*/
				
				//为了不让循环无休止地运行，我们定义一个循环变量，如果这个变量超过了这个值则退出循环，设置时间为5分钟
				x++;
				if (x >= 5) {
					//result = new Result(false, "二维码超时");
					//1.调用微信的关闭订单接口 因为当超时的时候我们需要让微信关闭订单 防止再次支付
				/*	Map<String,String> payresult = weixinPayService.closePay(out_trade_no);
					//2.但是如果在关闭订单的时候 如果订单已经被支付了 就不能删除订单   
					if( !"SUCCESS".equals(payresult.get("result_code")) ){//如果返回结果是正常关闭
						if("ORDERPAID".equals(payresult.get("err_code"))){ //已经被支付了
							result=new Result(true, "支付成功");    
							seckillGoodsService.saveOrderFromRedisToDb(userId, out_trade_no,(String)map.get("transaction_id"));
						}
					}*/
					
					//签名正确用这个 当二维码超时的时候从redis里面删除该订单
					//这里防止上面已经触发了
					/*if(result.isSuccess()==false){
						System.out.println("超时，取消订单");
						//2.调用删除
						seckillGoodsService.deleteOrderFromRedis(userId, out_trade_no);    
					}*/
					
					// 成功支付 这个去掉
					result = new Result(true, "支付成功");
					seckillGoodsService.saveOrderFromRedisToDb(userId,out_trade_no, "111");
					
					break;
				}
				Thread.sleep(3000); // 间隔三秒
			}catch (RuntimeException e) {
				result = new Result(false, e.getMessage());
				e.printStackTrace();
				break;
			} catch (Exception e) {
				//更改订单日志的状态 成功支付 
				e.printStackTrace();
				break;
			}
		}
		return result;
	}


}
