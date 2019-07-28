package com.pinyougou.cart.controller;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.user.service.AddressService;

@RestController
@RequestMapping("/address")
public class AddressController {
	
	@Reference(timeout=10000)
	private AddressService addressService;
	
	@RequestMapping("/findAddressListByUser")
	public List<TbAddress> findAddressListByUser(){
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return addressService.findListByUserId(username);
	}
	
}
