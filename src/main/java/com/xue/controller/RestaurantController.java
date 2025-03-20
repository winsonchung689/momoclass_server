package com.xue.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xue.entity.model.*;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;
import com.xue.service.RestaurantService;
import com.xue.util.HttpUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class RestaurantController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LoginService loginService;

	@Autowired
	private RestaurantService restaurantService;

	@Autowired
	private UserMapper dao;


	@RequestMapping("/insertRestaurantUser")
	@ResponseBody
	public String insertRestaurantUser(HttpServletRequest request, HttpServletResponse response){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		try {
			cal.setTime(df.parse(create_time));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		cal.add(cal.DATE,30);
		String expired_time = df.format(cal.getTime());

		//获取用户名
		String id = request.getParameter("id");
		String openid = request.getParameter("openid");

		List<RestaurantUser> restaurantUsers = dao.getRestaurantUserById(id);
		RestaurantUser restaurantUser = restaurantUsers.get(0);
		String restaurant = restaurantUser.getRestaurant();

		restaurantUser.setNick_name("微信用户");
		restaurantUser.setRole("client");
		restaurantUser.setOpenid(openid);
		restaurantUser.setRestaurant(restaurant);
		restaurantUser.setCreate_time(create_time);
		restaurantUser.setExpired_time(expired_time);

		restaurantService.insertRestaurantUser(restaurantUser);
		return "push massage successfully";
	}

	@RequestMapping("/shareShopQrCode")
	@ResponseBody
	public JSONObject getQrCode(String id){
		JSONObject jsonObject = new JSONObject();
		String token = loginService.getToken("ORDER");
		String scene = "&id=" + id;

		List<RestaurantUser> restaurantUsers = dao.getRestaurantUserById(id);
		RestaurantUser restaurantUser = restaurantUsers.get(0);
		String restaurant = restaurantUser.getRestaurant();

		try {
			String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + token;
			Map<String,String> param = new HashMap<>() ;
			param.put("scene",scene);
			param.put("page","pages/welcome/welcome");
			String json = JSON.toJSONString(param) ;
			ByteArrayInputStream inputStream = HttpUtil.sendBytePost(url, json);
			byte[] bytes = new byte[inputStream.available()];
			inputStream.read(bytes);
			String imageString = Base64.getEncoder().encodeToString(bytes);

			// 上传二维码
			String restaurant_md5 = DigestUtils.md5Hex(restaurant + "shop" + id );
			String serverPath = "/data/uploadRr";
			String fileName = restaurant_md5 + ".png";
			File file = new File(serverPath, fileName);
			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(bytes);
			}

			jsonObject.put("imageString", imageString);
			jsonObject.put("fileName", fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jsonObject;
	}

	@RequestMapping("/insertRestaurantMenu")
	@ResponseBody
	public String insertRestaurantMenu(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String restaurant = request.getParameter("restaurant");
		String food_name = request.getParameter("food_name");
		String food_image = request.getParameter("food_image");
		String category = request.getParameter("category");
		String introduce = request.getParameter("introduce");
		String price = request.getParameter("price");


		Menu menu =new Menu();
		menu.setRestaurant(restaurant);
		menu.setFood_name(food_name);
		menu.setFood_image(food_image);
		menu.setCategory(category);
		menu.setIntroduce(introduce);
		menu.setPrice(Float.parseFloat(price));
		menu.setCreate_time(create_time);
		try {
			dao.insertRestaurantMenu(menu);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return "push massage successfully";
	}

	@RequestMapping("/insertRestaurantOrder")
	@ResponseBody
	public String insertRestaurantOrder(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String restaurant = request.getParameter("restaurant");
		String food_name = request.getParameter("food_name");
		String category = request.getParameter("category");
		String num = request.getParameter("num");
		String price = request.getParameter("price");
		String openid = request.getParameter("openid");
		String goods_id = request.getParameter("goods_id");
		String order_no = request.getParameter("order_no");

		RestaurantOrder restaurantOrder =new RestaurantOrder();
		restaurantOrder.setRestaurant(restaurant);
		restaurantOrder.setFood_name(food_name);
		restaurantOrder.setCategory(category);
		restaurantOrder.setNum(Integer.parseInt(num));
		restaurantOrder.setPrice(Float.parseFloat(price));
		restaurantOrder.setCreate_time(create_time);
		restaurantOrder.setOpenid(openid);
		restaurantOrder.setGoods_id(goods_id);
		restaurantOrder.setOrder_no(order_no);
		try {
			dao.insertRestaurantOrder(restaurantOrder);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return "push massage successfully";
	}

	@RequestMapping("/getRestaurantUser")
	@ResponseBody
	public List getRestaurantUser(String openid){
		List list = null;
		try {
			list = restaurantService.getRestaurantUser(openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getRestaurantUserAll")
	@ResponseBody
	public List getRestaurantUserAll(String openid){
		List list = null;
		try {
			list = restaurantService.getRestaurantUserAll(openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/deleteRestaurantUser")
	@ResponseBody
	public int deleteRestaurantUser(Integer id){
		try {
			dao.deleteRestaurantUser(id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@RequestMapping("/deleteRestaurantOrder")
	@ResponseBody
	public int deleteRestaurantOrder(Integer id){
		try {
			dao.deleteRestaurantOrder(id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@RequestMapping("/getRestaurantOrder")
	@ResponseBody
	public List getRestaurantOrder(String openid,String type){
		List list = null;
		try {
			list = restaurantService.getRestaurantOrder(openid,type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getRestaurantCategory")
	@ResponseBody
	public List getRestaurantCategory(String restaurant){
		List list = null;
		try {
			list = restaurantService.getRestaurantCategory(restaurant);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getRestaurantMenu")
	@ResponseBody
	public List getRestaurantMenu(String restaurant){
		List list = null;
		try {
			list = restaurantService.getRestaurantMenu(restaurant);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/updateRestaurantUser")
	@ResponseBody
	public int updateRestaurantUser(HttpServletRequest request, HttpServletResponse response){

		//获取文字
		String content = request.getParameter("content");

		String openid = request.getParameter("openid");

		String type = request.getParameter("type");

		try {
			List<RestaurantUser> restaurantUsers = dao.getRestaurantUser(openid);
			RestaurantUser restaurantUser = restaurantUsers.get(0);
			if("avatarurl".equals(type)){
				restaurantUser.setAvatarurl(content);
				restaurantUser.setOpenid(openid);
				dao.updateRestaurantUser(restaurantUser);
			}else if("nickName".equals(type)){
				restaurantUser.setNick_name(content);
				restaurantUser.setOpenid(openid);
				dao.updateRestaurantUser(restaurantUser);
			}else if("logo".equals(type)){
				restaurantUser.setLogo(content);
				restaurantUser.setOpenid(openid);
				dao.updateRestaurantUser(restaurantUser);
			}else if("restaurant".equals(type)){
				restaurantUser.setRestaurant(content);
				restaurantUser.setOpenid(openid);
				dao.updateRestaurantUser(restaurantUser);
			}else if("role".equals(type)){
				if("boss".equals(content)){
					content = "client";
				}else {
					content = "boss";
				}
				restaurantUser.setRole(content);
				restaurantUser.setOpenid(openid);
				dao.updateRestaurantUser(restaurantUser);
			}else if("phone_number".equals(type)){
				restaurantUser.setPhone_number(content);
				restaurantUser.setOpenid(openid);
				dao.updateRestaurantUser(restaurantUser);
			}else if("location".equals(type)){
				restaurantUser.setLocation(content);
				restaurantUser.setOpenid(openid);
				dao.updateRestaurantUser(restaurantUser);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/updateRestaurantMenu")
	@ResponseBody
	public int updateRestaurantMenu(HttpServletRequest request, HttpServletResponse response){

		//获取文字
		String content = request.getParameter("content");

		String id = request.getParameter("id");

		String type = request.getParameter("type");

		try {
			if("food_image".equals(type)){
				Menu menu =new Menu();
				menu.setId(id);
				menu.setFood_image(content);
				dao.updateRestaurantMenuImage(menu);
			}else if("delete".equals(type)){
				dao.deleteRestaurantFood(Integer.parseInt(id));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/updateRestaurantOrderStatus")
	@ResponseBody
	public int updateRestaurantOrderStatus(HttpServletRequest request, HttpServletResponse response){

		//获取文字
		String id = request.getParameter("id");

		try {
			List<RestaurantOrder> restaurantOrders = dao.getRestaurantOrderById(id);
			RestaurantOrder restaurantOrder = restaurantOrders.get(0);
			int status = restaurantOrder.getStatus();
			if(status == 0){
				status = 1;
			}else if(status == 1){
				status =2;
			}

			dao.updateRestaurantOrderStatus(id,status);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;
	}

}
	
	


