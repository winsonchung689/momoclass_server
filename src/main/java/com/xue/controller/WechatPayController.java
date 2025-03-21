package com.xue.controller;

import com.alibaba.fastjson.JSONObject;
import com.xue.config.Constants;
import com.xue.entity.model.Merchant;
import com.xue.entity.model.RestaurantUser;
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
		String type = request.getParameter("type");

		String studio = null;
		String campus = null;
		if(appid.equals(Constants.appid)){
			List<User> users = dao.getUser(openid);
			User user = users.get(0);
			studio = user.getStudio();
			campus = user.getCampus();
		}else if(appid.equals(Constants.order_appid)){
			List<RestaurantUser> restaurantUsers = dao.getRestaurantUser(openid);
			RestaurantUser restaurantUser = restaurantUsers.get(0);
			studio = restaurantUser.getRestaurant();
			campus = restaurantUser.getRestaurant();
		}


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
			merchant.setType(type);

			dao.insertMerchant(merchant);
		} catch (Exception e) {
//					e.printStackTrace();
		}

		return "push massage successfully";
	}

	@RequestMapping("/updateWallet")
	@ResponseBody
	public int updateWallet(String order_no,Integer status,String type){
		try {
			dao.updateWallet(order_no,status,type);
			if("退款".equals(type)){
				dao.updateStatusByOrderNo(order_no,3);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@RequestMapping("/getWalletByStudio")
	@ResponseBody
	public List getWalletByStudio(String studio){
		List list = null;
		try {
			list = wechatPayService.getWalletByStudio(studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
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
		String is_client = request.getParameter("is_client");
		Float total_float = Float.parseFloat(total) * 100;
		int total_int = (int)Math.floor(total_float);


		// 查询工作室
		String studio = null;
		String campus = null;
		if(appid.equals(Constants.appid)){
			List<User> users = dao.getUserByOpenid(openid);
			User user = users.get(0);
			studio = user.getStudio();
			campus = user.getCampus();
		}else if(appid.equals(Constants.order_appid)){
			List<RestaurantUser> restaurantUsers = dao.getRestaurantUser(openid);
			RestaurantUser restaurantUser = restaurantUsers.get(0);
			studio = restaurantUser.getRestaurant();
			campus = restaurantUser.getRestaurant();
		}


		// 判断支付模式
		String type = "商户平台";
		String mchid = "1710485765";
		String sub_mchid = "1710485765";
		// 查询 merchant
		List<Merchant> merchants =dao.getMerchant(studio,campus,appid);
		if(merchants.size()>0){
			Merchant merchant = merchants.get(0);
			mchid = merchant.getMchid();
			sub_mchid = merchant.getSub_mchid();
			type = merchant.getType();
		}

		// 判断支付方式
		JSONObject result = null;
		if(type.equals("商户平台")){
			result = wechatPayService.weChatPayDirect(openid,mchid,appid,description,total_int,is_client);
		}else if(type.equals("服务商平台")){
			result = wechatPayService.weChatPayPartner(openid,mchid,sub_mchid,appid,description,total_int);
		}

		return result;
	}

	@RequestMapping("/weChatPayRefund")
	@ResponseBody
	public JSONObject weChatPayRefund(HttpServletRequest request, HttpServletResponse response){
		String appid = request.getParameter("appid");
		String openid = request.getParameter("openid");
		String order_no = request.getParameter("order_no");

		String total_refund = request.getParameter("total_refund");
		Float total_re_float = Float.parseFloat(total_refund) * 100;
		int total_re_int = (int)Math.floor(total_re_float);


		// 查询工作室
		String studio = null;
		String campus = null;
		if(appid.equals(Constants.appid)){
			List<User> users = dao.getUserByOpenid(openid);
			User user = users.get(0);
			studio = user.getStudio();
			campus = user.getCampus();
		}else if(appid.equals(Constants.order_appid)){
			List<RestaurantUser> restaurantUsers = dao.getRestaurantUser(openid);
			RestaurantUser restaurantUser = restaurantUsers.get(0);
			studio = restaurantUser.getRestaurant();
			campus = restaurantUser.getRestaurant();
		}


		// 判断支付模式
		String type = "商户平台";
		String mchid = "1710485765";
		String sub_mchid = "1710485765";
		// 查询 merchant
		List<Merchant> merchants =dao.getMerchant(studio,campus,appid);
		if(merchants.size()>0){
			Merchant merchant = merchants.get(0);
			mchid = merchant.getMchid();
			sub_mchid = merchant.getSub_mchid();
			type = merchant.getType();
		}

		// 判断支付方式
		JSONObject result = null;
		if(type.equals("商户平台")){
			result = wechatPayService.weChatPayDirectRefund(openid,mchid,appid,order_no);
		}

		return result;
	}
}
	
	


