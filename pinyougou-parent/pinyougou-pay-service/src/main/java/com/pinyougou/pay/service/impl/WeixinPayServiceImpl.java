package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;

import util.HttpClient;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

	@Value("${appid}")
	private String appid;
	@Value("${partner}")
	private String partner;
	@Value("${partnerkey}")
	private String partnerkey;

	@Override
	public Map createNative(String out_trade_no, String total_fee) throws Exception {
		// 1.创建参数
		Map<String, String> param = new HashMap<>();
		param.put("appid", appid); // 公众号
		param.put("mch_id", partner); // 商户号
		param.put("nonce_str", WXPayUtil.generateNonceStr());// 随机字符串
		param.put("body", "品优购");// 商品描述
		param.put("out_trade_no", out_trade_no);// 商户订单号
		param.put("total_fee", total_fee);// 总金额（分）
		param.put("spbill_create_ip", "127.0.0.1");// IP 终端ip 就是发出请求的ip 但是没有什么用 只是做了给记录
		param.put("notify_url", "http://test.xia.cn");// 回调地址(随便写)
		param.put("trade_type", "NATIVE");// 交易类型
		// 2.生成要发送的xml 签名在这里生成 我们要将这个发送给微信
		String generateSignedXml = WXPayUtil.generateSignedXml(param, partnerkey);// 随机字符串
		System.out.println(generateSignedXml);
		// 3.利用httpClient 发送请求
		HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
		httpClient.setHttps(true);
		httpClient.setXmlParam(generateSignedXml);
		httpClient.post();
		// 3.获得结果
		String result = httpClient.getContent();
		System.out.println(result);
		// 4.得到支付地址 返回给用户支付
		Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
		Map<String, String> map = new HashMap<>(); // 有些敏感数据不能给前端 所以需要再封装一个
		map.put("code_url", resultMap.get("code_url"));
		map.put("total_fee", total_fee);// 总金额
		map.put("out_trade_no", out_trade_no);// 订单号
		return map;
	}

	@Override
	public Map queryPayStatus(String out_trade_no) throws Exception {
		Map param = new HashMap();
		param.put("appid", appid);// 公众账号ID
		param.put("mch_id", partner);// 商户号
		param.put("out_trade_no", out_trade_no);// 订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr());// 随机字符串
		String url = "https://api.mch.weixin.qq.com/pay/orderquery";
		String generateSignedXml = WXPayUtil.generateSignedXml(param, partnerkey);
		HttpClient client = new HttpClient(url);
		client.setHttps(true);
		client.setXmlParam(generateSignedXml);
		client.post();
		String result = client.getContent();
		Map<String, String> map = WXPayUtil.xmlToMap(result);
		System.out.println(map);
		return map;
	}

	@Override
	public Map<String, String> closePay(String out_trade_no) throws Exception {
		Map param = new HashMap();
		param.put("appid", appid);// 公众账号ID
		param.put("mch_id", partner);// 商户号
		param.put("out_trade_no", out_trade_no);// 订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr());// 随机字符串
		String url = "https://api.mch.weixin.qq.com/pay/closeorder";
		String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
		HttpClient client = new HttpClient(url);
		client.setHttps(true);
		client.setXmlParam(xmlParam);
		client.post();
		String result = client.getContent();
		Map<String, String> map = WXPayUtil.xmlToMap(result);
		System.out.println(map);
		return map;
	}

}
