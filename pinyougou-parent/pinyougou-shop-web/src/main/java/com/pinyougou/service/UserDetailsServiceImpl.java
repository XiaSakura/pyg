package com.pinyougou.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;

/**
 * @author q9826 认证类
 */
public class UserDetailsServiceImpl implements UserDetailsService {

	// 利用spring原生注入 sellerService 而且注入的是dubbox远程调用
	private SellerService sellerService;

	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("经过了UserDetailsServiceImpl");
		// 构建角色列表
		List<GrantedAuthority> grantAuths = new ArrayList();
		grantAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
		// 得到商家对象
		try {
			TbSeller seller = sellerService.findOne(username);
			if (seller != null) {
				if (seller.getStatus().equals("1")) {
					return new User(username, seller.getPassword(), grantAuths);
				} else {
					return null;
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
