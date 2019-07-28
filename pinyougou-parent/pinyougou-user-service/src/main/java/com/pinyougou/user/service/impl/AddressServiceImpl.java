package com.pinyougou.user.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbAddressMapper;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.pojo.TbAddressExample;
import com.pinyougou.user.service.AddressService;

@Service
public class AddressServiceImpl implements AddressService {

	@Autowired
	private TbAddressMapper AddressMapper; 
	
	@Override
	public List<TbAddress> findListByUserId(String username) {
		TbAddressExample example=new TbAddressExample();
		example.createCriteria().andUserIdEqualTo(username);
		return AddressMapper.selectByExample(example);
	}
	
}
