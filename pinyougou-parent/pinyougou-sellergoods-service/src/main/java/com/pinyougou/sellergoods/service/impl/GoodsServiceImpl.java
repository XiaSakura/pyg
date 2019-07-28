package com.pinyougou.sellergoods.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.Goods;
import entity.PageResult;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbSellerMapper sellerMapper;
	@Autowired
	private TbItemMapper itemMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		TbGoods tbGoods = goods.getGoods();
		tbGoods.setAuditStatus("0");// 设置未审核状态
		goodsMapper.insert(tbGoods);
		// 插入后就可以得到 id 注意修改mapper代码
		goods.getGoodsDesc().setGoodsId(tbGoods.getId());
		goodsDescMapper.insert(goods.getGoodsDesc());// 插入商品扩展数据
		// 插入item表
		saveItemList(goods);
	}

	// 下面抽取了上面 启用规格和不启用规格的共性
	private void setItemValus(Goods goods, TbItem item) {
		item.setGoodsId(goods.getGoods().getId());// 商品SPU编号
		item.setSellerId(goods.getGoods().getSellerId());// 商家编号
		item.setCategoryid(goods.getGoods().getCategory3Id());// 商品分类编号（3级）
		item.setCreateTime(new Date());// 创建日期
		item.setUpdateTime(new Date());// 修改日期
		// 品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		// 分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());
		// 商家名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());
		// 图片地址（取spu的第一个图片）
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (imageList.size() > 0) {
			item.setImage((String) imageList.get(0).get("url"));
		}

	}

	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods) {
		goodsMapper.updateByPrimaryKeySelective(goods.getGoods());
		goodsDescMapper.updateByPrimaryKeySelective(goods.getGoodsDesc());// 保存商品扩展表
		// 删除原有的sku列表数据
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);
		// 添加新的sku列表数据
		saveItemList(goods);

	}

	private void saveItemList(Goods goods) {
		// 循环 item 一个SPU对应多个SKU
		List<TbItem> list = goods.getItemList();
		if (list != null && list.size() > 0) {
			// 启动规格
			if ("1".equals(goods.getGoods().getIsEnableSpec())) {
				for (TbItem item : goods.getItemList()) {
					// 标题
					String title = goods.getGoods().getGoodsName();
					Map<String, Object> specMap = JSON.parseObject(item.getSpec());
					for (String key : specMap.keySet()) {
						title += " " + specMap.get(key);
					}
					item.setTitle(title);
					setItemValus(goods, item);
					itemMapper.insert(item);
				}
			} else {
				// 不启动规格 默认只有一个SKU
				TbItem item = new TbItem();
				item.setTitle(goods.getGoods().getGoodsName());// 标题直接是SPU的名称 因为没有启用规格
				item.setPrice(goods.getGoods().getPrice());// 价格
				item.setStatus("1");// 状态
				item.setIsDefault("1");// 是否默认
				item.setNum(99999);// 库存数量
				item.setSpec("{}");
				setItemValus(goods, item);
				itemMapper.insert(item);
			}

		}
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public TbGoods findOne(Long id) {
		return goodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			// 注意这里是逻辑删除 而不是物理删除
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);
		}
	}

	@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbGoodsExample example = new TbGoodsExample();
		Criteria criteria = example.createCriteria();

		criteria.andIsDeleteIsNull();// 排除被删除的 没有删除的值是空

		if (goods != null) {
			if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
				criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
			}
			if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
				criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
			}
			if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
				criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
			}
			if (goods.getCaption() != null && goods.getCaption().length() > 0) {
				criteria.andCaptionLike("%" + goods.getCaption() + "%");
			}
			if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
				criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
			}
			if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
				criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
			}
			if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
				criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
			}

		}

		Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public Goods findGoodsById(Long id) {
		Goods goods = new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);
		// 查询SKU商品列表
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);// 查询条件：商品ID
		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemList(itemList);

		return goods;
	}

	@Override
	public void updateGoodsStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods record = new TbGoods();
			record.setId(id);
			record.setAuditStatus(status);
			goodsMapper.updateByPrimaryKeySelective(record);
		}
	}

	@Override
	public void updateGoodsMarketable(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods record = new TbGoods();
			record.setId(id);
			record.setIsMarketable(status);
			goodsMapper.updateByPrimaryKeySelective(record);
		}
	}

	@Override
	public List<TbItem> findItemListByGoodsIdandStatus(Long[] ids, String status) {
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(ids));
		criteria.andStatusEqualTo(status);
		return itemMapper.selectByExample(example);
	}

}
