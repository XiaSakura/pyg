package com.pinyougou.sellergoods.service;

import java.util.List;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/**
 * @author q9826
 * 品牌服务层接口
 */
public interface BrandService {
	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbBrand> findAll();
	
	/**返回分页列表
	 * @return
	 */
	public	PageResult findPage(int pageNum,int pageSize);
	
	/**返回分页列表 并实现条件查询
	 * @return
	 */
	public	PageResult findPage(int pageNum,int pageSize,TbBrand brand);
	
	/**添加
	 * @param tbBrand
	 */
	public void add(TbBrand tbBrand);
	
	/**修改
	 * @param tbBrand
	 */
	public void update(TbBrand tbBrand);
	
	
	/**根据id获取实体
	 * @param id
	 */
	public TbBrand findOne(Long id);
	
	/**批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

}
