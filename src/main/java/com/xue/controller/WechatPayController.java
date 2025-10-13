package com.xue.controller;

import com.alibaba.fastjson.JSONObject;
import com.xue.config.Constants;
import com.xue.entity.model.*;
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

import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
		String path = "/data/disk/uploadimages/" + uuid;

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
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
		int res = 0;
		try {

			List<Wallet> wallets = dao.getWalletByOrderNo(order_no);
			Wallet wallet = wallets.get(0);
			String studio = wallet.getStudio();
			String campus = wallet.getCampus();
			String mark = wallet.getDescription();
			Integer amount = wallet.getAmount();
			String appid = wallet.getAppid();

			Book book =new Book();
			book.setStudio(studio);
			book.setCampus(campus);
			book.setMark(mark);
			book.setAmount((float)amount/100);
			book.setType("收入");
			book.setCreate_time(create_time);

			dao.updateWallet(order_no,status,type);
			if(appid.equals(Constants.appid)){
				if("退款".equals(type)){
					dao.updateStatusByOrderNo(order_no,3);
					book.setType("支出");
				}

				if (mark.contains("小桃子续费")) {
					book.setType("支出");
				}
				dao.insertBook(book);
			} else if (appid.equals(Constants.order_appid)) {
				if("退款".equals(type)){
					res = dao.updateRestaurantOrderByOrderNo(order_no,3);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	@RequestMapping("/getWalletByStudio")
	@ResponseBody
	public List getWalletByStudio(String studio,String appid,String duration){
		List list = null;
		try {
			list = wechatPayService.getWalletByStudio(studio,appid,duration);
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
		Float total_float = Float.parseFloat(total);
		int total_int = Math.round(total_float * 100);


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
		}else if(appid.equals(Constants.book_appid)){
			List<BookUser> bookUsers = dao.getBookUser(openid);
			BookUser bookUser = bookUsers.get(0);
			studio = bookUser.getOpenid_qr();
			campus = bookUser.getOpenid_qr();
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
		if(type.equals("商户平台") || "0".equals(is_client)){
			result = wechatPayService.weChatPayDirect(openid,mchid,appid,description,total_int,is_client);
		}else if(type.equals("服务商平台") && "1".equals(is_client)){
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

	@RequestMapping("/wechatPayApplymentForSub")
	@ResponseBody
	public String wechatPayApplymentForSub(HttpServletRequest request, HttpServletResponse response) throws IOException, IllegalBlockSizeException {

		JSONObject jsonObject = new JSONObject();
		// 法人信息
		String contact_name = request.getParameter("contact_name");
		String mobile_phone = request.getParameter("mobile_phone");
		String contact_email = request.getParameter("contact_email");

		JSONObject contact_info = new JSONObject();
		contact_info.put("contact_name",contact_name);
		contact_info.put("mobile_phone",mobile_phone);
		contact_info.put("contact_email",contact_email);

		// 主体资料
		String id_card_name = request.getParameter("id_card_name");
		String id_card_number = request.getParameter("id_card_number");
		String card_period_begin = request.getParameter("card_period_begin");
		String card_period_end = request.getParameter("card_period_end");
		String id_card_copy = request.getParameter("id_card_copy");
		String id_card_national = request.getParameter("id_card_national");

		JSONObject identity_info = new JSONObject();
		JSONObject id_card_info = new JSONObject();
		id_card_info.put("id_card_name",id_card_name);
		id_card_info.put("id_card_number",id_card_number);
		id_card_info.put("card_period_begin",card_period_begin);
		id_card_info.put("card_period_end",card_period_end);
		id_card_info.put("id_card_copy",id_card_copy);
		id_card_info.put("id_card_national",id_card_national);
		identity_info.put("identity_info",id_card_info);

		// 经营资料
		String merchant_shortname = request.getParameter("merchant_shortname");
		String service_phone = request.getParameter("service_phone");

		JSONObject business_info = new JSONObject();
		business_info.put("merchant_shortname",merchant_shortname);
		business_info.put("service_phone",service_phone);
		business_info.put("business_info",business_info);

		// 结算银行账户
		String bank_account_type = request.getParameter("bank_account_type");
		String account_name = request.getParameter("account_name");
		String account_bank = request.getParameter("account_bank");
		String account_number = request.getParameter("account_number");

		JSONObject bank_account_info = new JSONObject();
		bank_account_info.put("bank_account_type",bank_account_type);
		bank_account_info.put("account_name",account_name);
		bank_account_info.put("account_bank",account_bank);
		bank_account_info.put("account_number",account_number);

		// 汇总参数
		jsonObject.put("contact_info",contact_info);
		jsonObject.put("identity_info",identity_info);
		jsonObject.put("business_info",business_info);
		jsonObject.put("bank_account_info",bank_account_info);

		String url = "https://api.mch.weixin.qq.com/v3/applyment4sub/applyment/";
		String result = HttpUtil.applymentForSubPost(url,jsonObject.toString());

		return result;
	}

	@RequestMapping("/updateMerchantStatus")
	@ResponseBody
	public String updateMerchantStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String applyment_id = request.getParameter("applyment_id");
		String result = null;
		try {
			result = HttpUtil.updateMerchantStatus(applyment_id);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		return result;
	}
}
	
	


