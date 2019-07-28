package com.pinyougou.user.service;

import java.util.List;

import com.pinyougou.pojo.TbAddress;

public interface AddressService {

	List<TbAddress> findListByUserId(String username);
	
}
