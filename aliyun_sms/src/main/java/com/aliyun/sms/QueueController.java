package com.aliyun.sms;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueueController {
	
	@Autowired
	private JmsMessagingTemplate jmsMessagingTemplate;
	
	@RequestMapping("/sendsms")
	public void sendSms() {
		Map map=new HashMap<>();
		map.put("mobile", "17725021702");
		map.put("template_code", "SMS_166868038");    
		map.put("sign_name", "品优购");
		map.put("param", "{\"code\":\"102931\"}");
		jmsMessagingTemplate.convertAndSend("sms", map);
	}
}
