package com.xue.controller;

import com.xue.entity.model.Menu;
import com.xue.entity.model.RestaurantOrder;
import com.xue.entity.model.RestaurantUser;
import com.xue.entity.model.User;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class RestaurantController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LoginService loginService;

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
		String nick_name = request.getParameter("nick_name");

		String restaurant = request.getParameter("restaurant");

		//获年角色
		String role = request.getParameter("role");
		if(role == null || role.isEmpty() || "undefined".equals(role)){
			role = "client";
		}
		//获取 openid
		String openid = request.getParameter("openid");

		//获取 avatarurl
		String avatarurl = request.getParameter("avatarurl");
		if(avatarurl == null || avatarurl.isEmpty() || "undefined".equals(avatarurl)){
			avatarurl = "https://thirdwx.qlogo.cn/mmopen/vi_32/y667SLJ40Eic5fMnHdibjO4vLG7dmqgjeuwjQbRN5ZJj6uZfl06yA7P9wwl7oYjNRFzBzwcheZtK8zvkibyfamfBA/132";
		}

		RestaurantUser restaurantUser =new RestaurantUser();
		restaurantUser.setNick_name(nick_name);
		restaurantUser.setRole(role);
		restaurantUser.setOpenid(openid);
		restaurantUser.setAvatarurl(avatarurl);
		restaurantUser.setRestaurant(restaurant);
		restaurantUser.setCreate_time(create_time);
		restaurantUser.setExpired_time(expired_time);

		loginService.insertRestaurantUser(restaurantUser);
		return "push massage successfully";
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

		RestaurantOrder restaurantOrder =new RestaurantOrder();
		restaurantOrder.setRestaurant(restaurant);
		restaurantOrder.setFood_name(food_name);
		restaurantOrder.setCategory(category);
		restaurantOrder.setNum(Integer.parseInt(num));
		restaurantOrder.setPrice(Float.parseFloat(price));
		restaurantOrder.setCreate_time(create_time);
		restaurantOrder.setOpenid(openid);
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
			list = loginService.getRestaurantUser(openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getRestaurantOrder")
	@ResponseBody
	public List getRestaurantOrder(String openid,String type){
		List list = null;
		try {
			list = loginService.getRestaurantOrder(openid,type);
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
			list = loginService.getRestaurantCategory(restaurant);
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
			list = loginService.getRestaurantMenu(restaurant);
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
			if("avatarurl".equals(type)){
				RestaurantUser restaurantUser =new RestaurantUser();
				restaurantUser.setAvatarurl(content);
				restaurantUser.setOpenid(openid);
				dao.updateRestaurantAvatar(restaurantUser);
			}else if("nickName".equals(type)){
				RestaurantUser restaurantUser =new RestaurantUser();
				restaurantUser.setNick_name(content);
				restaurantUser.setOpenid(openid);
				dao.updateRestaurantNickName(restaurantUser);
			}else if("logo".equals(type)){
				RestaurantUser restaurantUser =new RestaurantUser();
				restaurantUser.setLogo(content);
				restaurantUser.setOpenid(openid);
				dao.updateRestaurantLogo(restaurantUser);
			}else if("restaurant".equals(type)){
				RestaurantUser restaurantUser =new RestaurantUser();
				restaurantUser.setRestaurant(content);
				restaurantUser.setOpenid(openid);
				dao.updateRestaurantName(restaurantUser);
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
		String status_get = request.getParameter("status");
		int status = 1;
		if("1".equals(status_get)){
			status =0;
		}

		try {
			dao.updateRestaurantOrderStatus(id,status);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;
	}

}
	
	


