package com.xue.controller;

import com.alibaba.fastjson.JSONObject;
import com.xue.entity.model.Merchant;
import com.xue.entity.model.User;
import com.xue.repository.dao.UserMapper;
import com.xue.service.WechatPayService;
import com.xue.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URISyntaxException;
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

	@RequestMapping("/merchantUploadImage")
	@ResponseBody
	public String merchantUploadImage(HttpServletRequest request, HttpServletResponse response) throws URISyntaxException {

		//获取图片
		String uuid =  request.getParameter("uuid");
		String path = "/data/disk/uploadimages/" + uuid + ".png";

		String media_id = HttpUtil.merchantUploadImage(path);

		return media_id;
	}

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

	@RequestMapping("/weChatPay")
	@ResponseBody
	public JSONObject weChatPay(HttpServletRequest request, HttpServletResponse response){
		String appid = request.getParameter("appid");
		String description = request.getParameter("description");
		String total = request.getParameter("total");
		String openid = request.getParameter("openid");
		String type = request.getParameter("type");

		// 查询工作室
		List<User> users = dao.getUserByOpenid(openid);
		User user = users.get(0);
		String studio = user.getStudio();
		String campus = user.getCampus();

		// 查询 merchant
		List<Merchant> merchants =dao.getMerchant(studio,campus,appid);
		Merchant merchant = merchants.get(0);
		String mchid = merchant.getMchid();
		String sub_mchid = merchant.getSub_mchid();

		// 判断支付方式
		JSONObject result = null;
		if(type.equals("直接")){
			mchid = "";
			result = wechatPayService.weChatPayDirect(openid,mchid,appid,description,Integer.parseInt(total)*100);;
		}else if(type.equals("服务")){
			result = wechatPayService.weChatPayPartner(openid,mchid,sub_mchid,appid,description,Integer.parseInt(total)*100);;
		}

		return result;
	}


}
	
	


