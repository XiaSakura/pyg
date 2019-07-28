package test;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.xia.demo.MyMessageListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-jms-consumer.xml")
public class TestQueue {

	@Autowired
	private MyMessageListener myMessageListener;

	@Test
	public void testQueue() {
		// 消息监听容器 只需要启动了spring加载spring环境 就会自动启动消息监听容器
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
