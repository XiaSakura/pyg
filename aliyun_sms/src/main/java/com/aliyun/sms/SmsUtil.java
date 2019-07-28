package com.aliyun.sms;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;

@Component
public class SmsUtil {

	// 产品名称:云通信短信API产品,开发者无需替换
	static final String product = "Dysmsapi";
	// 产品域名,开发者无需替换
	static final String domain = "dysmsapi.aliyuncs.com";
	@Autowired
	private Environment env; // 这个就可以获取 application.properties 里面的配置

	public String sendSms(String mobile, String template_code, String sign_name, String param) throws ClientException{
		String accessKeyId = env.getProperty("accessKeyId");
		String accessKeySecret = env.getProperty("accessKeySecret");
		DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
		DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
		IAcsClient client = new DefaultAcsClient(profile);
		
		CommonRequest request = new CommonRequest();
		// request.setProtocol(ProtocolType.HTTPS);
		request.setMethod(MethodType.POST);
		request.setDomain("dysmsapi.aliyuncs.com");
		request.setAction("SendSms");
		request.setVersion("2017-05-25");
		request.putQueryParameter("OutId", "yourOutId");
		request.putQueryParameter("PhoneNumbers", mobile);
		request.putQueryParameter("SignName", sign_name);
		request.putQueryParameter("TemplateCode", template_code);
		request.putQueryParameter("TemplateParam", param);
		try {
			CommonResponse response = client.getCommonResponse(request);
			System.out.println(response.getData());
			return response.getData();
		} catch (ServerException e) {
			e.printStackTrace();
		} catch (ClientException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//根据手机号和业务号 可以查询短信记录
	public String querySendDetails(String mobile,String bizId) {
		String accessKeyId = env.getProperty("accessKeyId");
		String accessKeySecret = env.getProperty("accessKeySecret");
		 DefaultProfile profile = DefaultProfile.getProfile("default", accessKeyId, accessKeySecret);
	        IAcsClient client = new DefaultAcsClient(profile);
	        CommonRequest request = new CommonRequest();
	        //request.setProtocol(ProtocolType.HTTPS);
	        request.setMethod(MethodType.POST);
	        request.setDomain("dysmsapi.aliyuncs.com");
	        request.setAction("QuerySendDetails");
	        request.putQueryParameter("PhoneNumber", mobile);
	        //必填-发送日期 支持30天内记录查询，格式yyyyMMdd
	        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
	        request.putQueryParameter("SendDate", ft.format(new Date()));
	        request.putQueryParameter("PageSize", "10");
	        request.putQueryParameter("CurrentPage", "1");
	        request.putQueryParameter("BizId", bizId);
	        try {
	            CommonResponse response = client.getCommonResponse(request);
	            System.out.println(response.getData());
	            return response.getData();
	        } catch (ServerException e) {
	            e.printStackTrace();
	        } catch (ClientException e) {
	            e.printStackTrace();
	        }
		return null;
		
	}

}
