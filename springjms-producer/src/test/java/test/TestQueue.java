package test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.xia.demo.QueueProducer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:applicationContext-jms-producer.xml")
public class TestQueue {
	@Autowired  //前面加了包扫描了 相当于一个bean 
	private QueueProducer queueProducer;
	
	@Test
	public void testSend() {
		queueProducer.sendTextMessage("SpringJMS 点对点");
	}

}
