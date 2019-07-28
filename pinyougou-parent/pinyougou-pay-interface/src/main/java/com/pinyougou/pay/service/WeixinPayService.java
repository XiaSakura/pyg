package com.pinyougou.pay.service;

import java.util.Map;

/**
 * @author q9826 微信支付接口
 */
public interface WeixinPayService {
	/**
	 * 生成微信支付二维码 本地支付Native
	 * 
	 * @param out_trade_no 我们系统的订单号 相对于微信支付系统是外部订单号
	 * @param total_fee    支付金额
	 * @return 返回不仅仅是url 所以用map 便于扩展
	 */
	public Map createNative(String out_trade_no, String total_fee) throws Exception;

	public Map queryPayStatus(String out_trade_no)  throws Exception;

	/**关闭微信支付订单
	 * @param out_trade_no
	 * @return
	 */
	public Map<String, String> closePay(String out_trade_no) throws Exception;
}
