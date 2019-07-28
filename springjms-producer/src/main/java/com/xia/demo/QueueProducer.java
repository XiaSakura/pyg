package com.xia.demo;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

/**
 * @author q9826
 *点对点 生产者测试类
 */
@Component
public class QueueProducer {
	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Autowired
	private Destination queueTextDestination;
	
	/**发生文本信息
	 * @param text
	 */
	public void sendTextMessage(String text) {
		jmsTemplate.send(queueTextDestination,new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(text);
			}
		});
	}
}
