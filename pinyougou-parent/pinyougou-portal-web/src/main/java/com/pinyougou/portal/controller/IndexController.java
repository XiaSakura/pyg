package com.pinyougou.portal.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.ItemCatService;

@RestController
@RequestMapping("/index")
public class IndexController {

	@Reference
	private ItemCatService itemCatService;

	/**
	 * 找到所有的商品分类
	 * 
	 * @return
	 */
	@RequestMapping("/findAllItemCat")
	public List<TbItemCat> findAllItemCat() {
		List<TbItemCat> list = new ArrayList<TbItemCat>();
		List<TbItemCat> itemCatList = itemCatService.findAll();
		// 利用Map存放
		Map<Long, TbItemCat> map = new HashMap<>();
		for (TbItemCat tbItemCat : itemCatList) {
			map.put(tbItemCat.getId(), tbItemCat);
		}
		for (TbItemCat t : itemCatList) {
			TbItemCat child = t;
			// 代表根节点
			if (child.getParentId() == 0) {
				list.add(t);
			} else {
				// 这个就代表是子节点
				// 子节点获取父节点
				TbItemCat parent = map.get(child.getParentId());
				if (parent!=null) {
					// 通过父节点
					if (parent.getChildren() == null) {
						parent.setChildren(new ArrayList<>());
					} else {
						parent.getChildren().add(child);
					}
				}
			}
		}

		return list;
	}

}
