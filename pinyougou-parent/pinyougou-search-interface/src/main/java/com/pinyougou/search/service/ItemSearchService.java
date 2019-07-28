package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

/**
 * @author q9826
 * 商品搜索接口
 */
public interface ItemSearchService {
	/**searchMap 里面有
	 * keywords:"" 关键字 商家,商品,标题等等
	 * 
	 * @param searchMap
	 * @return
	 */
	public Map<String,Object> search(Map searchMap);
	
	/**导入item集合
	 * @param list
	 */
	public void importList(List list);
	
	/**
	 * 删除数据 根据goodsId 商品删除在商家管理后台
	 */
	public void deleteByGoodsIds(List ids);
}
