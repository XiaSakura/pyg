package com.pinyougou.sellergoods.service;
import java.util.List;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;

import entity.Goods;
import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(Goods goods);
	
	
	/**
	 * 修改
	 */
	public void update(Goods goods);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbGoods findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long [] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbGoods goods, int pageNum,int pageSize);
	
	/**根据id获取 goods实体
	 * @param id
	 * @return
	 */
	public Goods findGoodsById(Long id);
	
	/**批量修改商品状态
	 * @param ids
	 * @param status
	 */
	public void updateGoodsStatus(Long[] ids,String status);
	
	/**批量修改商品上架状态
	 * @param ids
	 * @param status 1表示上架、0表示下架。
	 */
	public void updateGoodsMarketable(Long[] ids,String status);
	
	/**根据商品ID和状态查询Item表信息  
	 * @param ids
	 * @param status
	 * @return
	 */
	public List<TbItem> findItemListByGoodsIdandStatus(Long[] ids,String status);
	
}
