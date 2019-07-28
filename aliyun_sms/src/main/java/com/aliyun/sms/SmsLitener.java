package com.aliyun.sms;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.aliyuncs.exceptions.ClientException;

@Component
public class SmsLitener {
	@Autowired
	private SmsUtil smsUtil;
	// 发生短信 从activeMQ里面接收消息 调用smsUtil

	@JmsListener(destination = "sms")
	public void sendSms(Map<String, String> map) {
		String sms;
		try {
			sms = smsUtil.sendSms(map.get("mobile"), map.get("template_code"), map.get("sign_name"), map.get("param"));
			System.out.println("短信response: " + sms);
		} catch (ClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
