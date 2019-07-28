package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;
	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public Map<String, Object> search(Map searchMap) {
		// 关键字空格处理
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		Map<String, Object> map = new HashMap<>();
		Query query = new SimpleQuery();
		// 添加查询条件
		// 1.查询列表
		map.putAll(searchList(searchMap));
		// 2.根据关键字查询商品分类
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		// 3.查询品牌和规格列表
		String categoryName = (String) searchMap.get("category");
		if (!"".equals(categoryName)) {// 如果有分类名称
			map.putAll(searchBrandAndSpecList(categoryName));
		} else {// 如果没有分类名称，按照第一个查询
			if (categoryList.size() > 0) {
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}

		return map;
	}

	/**
	 * 根据关键字搜索列表 关键字高亮显示
	 * 
	 * @return
	 */
	private Map<String, Object> searchList(Map searchMap) {
		Map<String, Object> map = new HashMap<>();
		// 1.按照关键字查询(高亮显示)
		HighlightQuery query = new SimpleHighlightQuery();
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");// 设置高亮的域
		highlightOptions.setSimplePrefix("<em style='color:red'>");// 高亮前缀 就是找到关键字 在前后添加标签
		highlightOptions.setSimplePostfix("</em>");// 高亮后缀
		query.setHighlightOptions(highlightOptions);
		// 添加查询条件
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		// 2.按照分类进行筛选
		if (!"".equals(searchMap.get("category"))) {
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		// 3.按照品牌进行筛选
		if (!"".equals(searchMap.get("brand"))) {
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		// 4.按照规格过滤
		if (searchMap.get("spec") != null) {
			Map<String, String> specMap = (Map) searchMap.get("spec");
			for (String key : specMap.keySet()) {
				Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
				FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		// 5.按照价格进行筛选
		if (!"".equals(searchMap.get("price"))) {
			// 例如 0-500 1000-1500 3000-*
			String[] price = ((String) searchMap.get("price")).split("-");
			// 进行判断 上限和下限
			// 判断起点 不为0的话
			if (!price[0].equals("0")) {
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
				FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
			// 判断终点 不为*的话 为*不做限制
			if (!price[1].equals("*")) {// 如果区间终点不等于*
				Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
				FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		// 6.分页查询
		Integer pageNo = (Integer) searchMap.get("pageNo");// 提取页码
		Integer pageSize = (Integer) searchMap.get("pageSize");// 每页记录数
		if (pageNo == null) {
			pageNo = 1;// 默认第一页
		}
		if (pageSize == null) {
			pageSize = 20;// 默认20
		}
		query.setOffset((pageNo - 1) * pageSize); // 从第几条记录查询
		query.setRows(pageSize); // 设置查询行数
		// 7 按照价钱进行排序 前端会传来sort和 sortField
		String sortValue = (String) searchMap.get("sort");// ASC DESC
		String sortField = (String) searchMap.get("sortField");// 排序字段
		if (sortValue != null && !sortValue.equals("")) {
			if (sortValue.equals("ASC")) { //升序
				Sort sort=new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}
			if (sortValue.equals("DESC")) { //降序
				Sort sort=new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);
			}
		}
		// .返回一个高亮页对象 但是还需要循环高亮显示 而不是直接返回content
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		// page.getHighlighted()获得高亮的结果 高亮入口集合 每条记录的高亮入口
		List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();
		// 获取入口
		for (HighlightEntry<TbItem> h : highlighted) {
			TbItem item = h.getEntity();// 获取原实体类 就能够给content结果集里面 设置高亮 因为两个的引用都是同一个
			// 因为我们这里只有一个字段需要设置高亮 所以第一个
			if (h.getHighlights().size() > 0 && h.getHighlights().get(0).getSnipplets().size() > 0) {
				item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));// 设置高亮的结果
			}
		}

		map.put("rows", page.getContent());
		map.put("totalPages", page.getTotalPages()); // 返回总页数
		map.put("total", page.getTotalElements()); // 返回总条数

		return map;
	}

	/**
	 * 根据关键字 查询分组列表
	 * 
	 * @param searchMap
	 * @return
	 */
	private List searchCategoryList(Map searchMap) {
		List<String> list = new ArrayList();

		Query query = new SimpleQuery();
		// 1.按照关键字查询 还是先要通过关键字来进行搜索 相当于where条件
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		// 2.设置分组域
		GroupOptions groupOptions = new GroupOptions();
		groupOptions.addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		// 3.得到分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		// 4.根据列得到分组结果集 和之前的差不多 因为可能有多个分组选项 一定是上面addGroupByField出现的
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		// 5.得到分组结果入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		// 6.得到分组入口集合 这个content才能够获得分组数据 直接的content是不能获得数据的
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		for (GroupEntry<TbItem> entry : content) {
			list.add(entry.getGroupValue());// 将分组结果的名称封装到返回值中 我们只需要分类就可以了 string
		}

		return list;

	}

	/**
	 * 根据商品分类名称 查询品牌和规格列表
	 * 
	 * @param category 分类名称
	 * @return
	 */
	private Map searchBrandAndSpecList(String category) {
		Map map = new HashMap();
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);// 获取模板ID
		if (typeId != null) {
			// 根据模板ID查询品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
			map.put("brandList", brandList);// 返回值添加品牌列表
			// 根据模板ID查询规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
			map.put("specList", specList);
		}
		return map;
	}

	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	@Override
	public void deleteByGoodsIds(List ids) {
		System.out.println("删除商品ID"+ids);
		SolrDataQuery query=new SimpleQuery();
		Criteria criteria=new Criteria("item_goodsid").in(ids);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}

}
