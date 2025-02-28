package com.xue.controller;

import com.xue.entity.model.Merchant;
import com.xue.entity.model.User;
import com.xue.repository.dao.UserMapper;
import com.xue.service.WechatPayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class WechatPayController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private WechatPayService wechatPayService;

	@Autowired
	private UserMapper dao;

	@RequestMapping("/insertMerchant")
	@ResponseBody
	public String insertMerchant(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String mchid = request.getParameter("mchid");
		String appid = request.getParameter("appid");
		String openid = request.getParameter("openid");
		String sub_mchid = request.getParameter("sub_mchid");
		String sub_appid = request.getParameter("sub_appid");
		List<User> users = dao.getUser(openid);
		User user = users.get(0);
		String studio = user.getStudio();
		String campus = user.getCampus();

		try {
			Merchant merchant = new Merchant();
			merchant.setAppid(appid);
			merchant.setMchid(mchid);
			merchant.setOpenid(openid);
			merchant.setStudio(studio);
			merchant.setCampus(campus);
			merchant.setSub_appid(sub_appid);
			merchant.setSub_mchid(sub_mchid);
			merchant.setCreate_time(create_time);

			dao.insertMerchant(merchant);
		} catch (Exception e) {
//					e.printStackTrace();
		}

		return "push massage successfully";
	}


	@RequestMapping("/getWeChatPayNotify")
	@ResponseBody
	public void getWeChatPayNotify(){
		System.out.println("pay sucessfully");
	}

	@RequestMapping("/weChatPayDirect")
	@ResponseBody
	public String weChatPayDirect(HttpServletRequest request, HttpServletResponse response){

		String appid = request.getParameter("appid");
		String description = request.getParameter("description");
		String total = request.getParameter("total");
		String openid = request.getParameter("openid");

		// 查询 merchant
		List<Merchant> merchants =dao.getMerchant("桃园艺术","桃园艺术",appid);
		Merchant merchant = merchants.get(0);
		String mchid = merchant.getMchid();

		String result = wechatPayService.weChatPayDirect(openid,mchid,appid,description,Integer.parseInt(total));;

		return result;
	}

	@RequestMapping("/weChatPayPartner")
	@ResponseBody
	public String weChatPayPartner(HttpServletRequest request, HttpServletResponse response){

		String description = request.getParameter("description");
		String total = request.getParameter("total");
		String openid = request.getParameter("openid");
		String appid = request.getParameter("appid");
		// 查询校区
		List<User> users = dao.getUser(openid);
		User user = users.get(0);
		String studio = user.getStudio();
		String campus = user.getCampus();
		// 查询 merchant
		List<Merchant> merchants =dao.getMerchant(studio,campus,appid);
		Merchant merchant = merchants.get(0);
		String mchid = merchant.getMchid();
		String sub_mchid = merchant.getSub_mchid();

		String result = wechatPayService.weChatPayPartner(openid,mchid,sub_mchid,appid,description,Integer.parseInt(total));;

		return result;
	}




}
	
	


