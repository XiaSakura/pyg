package com.pinyougou.solrutil;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

/**
 * @author q9826 用于导入商品数据 初始化商品搜索solr数据
 */
@Component
public class SolrUtil {

	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private SolrTemplate solrTemplate;

	/**
	 * 导入商品数据
	 */
	public void importItemData() {
		TbItemExample example = new TbItemExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");// 已审核
		List<TbItem> itemList = itemMapper.selectByExample(example);
		System.out.println("===商品列表===");
		for (TbItem item : itemList) {
			//注意这里还有个作用 将spec转换成specMap 动态域 这里需要fastJson
			Map map = JSON.parseObject(item.getSpec(), Map.class);
			item.setSpecMap(map);
			System.out.println(item.getTitle());
		}
		solrTemplate.saveBeans(itemList);
		solrTemplate.commit();
		System.out.println("===结束===");
	}
	

	public static void main(String[] args) {
		// 因为我们需要 找到dao里面的配置文件 所以classpath* 加了*可以找jar包里面的配置文件 不然只会找本工程里面的

		ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");
		SolrUtil solrUtil=(SolrUtil) context.getBean("solrUtil");
		solrUtil.importItemData();

	}

}
