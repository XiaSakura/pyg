package com.pinyougou.portal.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.pojo.TbContent;

@RestController
@RequestMapping("/content")
public class ContentController {
	
	@Reference(timeout=10000)
	private ContentService contentService;
	
	/** 根据广告分类ID查询广告列表
	 * @return
	 */
	@RequestMapping("/findByCategoryId")
	public List<TbContent> findByCategoryId(Long categoryId){
		List<TbContent> list = contentService.findByCategoryId(categoryId);
		return list;
		
	}
}
