package com.pinyougou.page.service;

/**
 * @author q9826 商品详细页生成
 */
public interface ItemPageService {
	/**
	 * 根据SPUID生成 商品详细页 因为SKU可以共用详细页
	 * 
	 * @param goodsId
	 * @return
	 */
	public boolean genItemHtml(Long goodsId);

	/**
	 * 删除商品详细页
	 * 
	 * @param goodsId
	 * @return
	 */
	public boolean deleteItemHtml(Long[] goodsIds);
}
