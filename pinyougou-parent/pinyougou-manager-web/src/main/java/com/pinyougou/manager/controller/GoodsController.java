package com.pinyougou.manager.controller;

import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.Goods;
import entity.PageResult;
import entity.Result;

/**
 * controller
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	@Autowired
	private Destination queueSolrDestination;// 用于发送solr导入的消息

	@Autowired
	private Destination queueSolrDeleteDestination;// 用户在索引库中删除记录

	@Autowired
	private Destination topicPageDestination;// 生成页面
	
	@Autowired
	private Destination topicPageDeleteDestination;//用于删除静态网页的消息

	@Autowired
	private JmsTemplate jmsTemplate;

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll() {
		return goodsService.findAll();
	}

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows) {
		return goodsService.findPage(page, rows);
	}

	/**
	 * 增加
	 * 
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods) {
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}

	/**
	 * 修改
	 * 
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods) {
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}

	/**
	 * 获取实体
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbGoods findOne(Long id) {
		return goodsService.findOne(id);
	}

	/**
	 * 获取实体
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/findGoodsById")
	public Goods findGoodsById(Long id) {
		return goodsService.findGoodsById(id);
	}

	/**
	 * 批量删除
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long[] ids) {
		try {
			goodsService.delete(ids);
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					// Long类型 其实是可序列化的对象
					return session.createObjectMessage(ids);
				}
			});
			//删除页面
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {        
				@Override
				public Message createMessage(Session session) throws JMSException {    
					return session.createObjectMessage(ids);
				}
			});    
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	/**
	 * 查询+分页
	 * 
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
		return goodsService.findPage(goods, page, rows);
	}

	/**
	 * 批量更新状态
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping("/updateGoodsStatus")
	public Result updateGoodsStatus(Long[] ids, String status) {
		try {
			goodsService.updateGoodsStatus(ids, status);
			if (status.equals("1")) {
				List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);
				if (itemList != null && itemList.size() > 0) {
					// 传输json 字符串
					String jsonString = JSON.toJSONString(itemList);
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(jsonString);
						}
					});
				}
				// 静态页生成
				for (final Long goodsId : ids) {
					jmsTemplate.send(topicPageDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(goodsId + "");
						}
					});
				}

			}
			return new Result(true, "批量更新状态成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "批量更新状态失败");
		}
	}

}
