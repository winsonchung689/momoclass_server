package com.xue.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xue.config.Constants;
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

	@RequestMapping("/inviteUser")
	@ResponseBody
	public String inviteUser(HttpServletRequest request, HttpServletResponse response){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		try {
			cal.setTime(df.parse(create_time));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		cal.add(cal.DATE,365);
		String expired_time = df.format(cal.getTime());

		//获取用户名
		String id = request.getParameter("id");
		String openid = request.getParameter("openid");

		List<RestaurantUser> restaurantUsers = dao.getRestaurantUserById(id);
		RestaurantUser restaurantUser_get = restaurantUsers.get(0);
		String restaurant = restaurantUser_get.getRestaurant();


		RestaurantUser restaurantUser = new RestaurantUser();
		restaurantUser.setNick_name("微信用户");
		restaurantUser.setPhone_number("未录入");
		restaurantUser.setRole("client");
		restaurantUser.setOpenid(openid);
		restaurantUser.setRestaurant(restaurant);
		restaurantUser.setCreate_time(create_time);
		restaurantUser.setExpired_time(expired_time);

		List<RestaurantUser> restaurantUsers1 = dao.getRestaurantUserByOpenid(openid);
		if(restaurantUsers1.size()>0){
			RestaurantUser restaurantUser1 = restaurantUsers1.get(0);
			String role_get = restaurantUser1.getRole();
			if(!"boss".equals(role_get)){
				dao.updateRestaurantByOpenid(restaurantUser);
			}
		}else {
			restaurantService.insertRestaurantUser(restaurantUser);
		}

		return "push massage successfully";
	}

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
		if(id == null || id.isEmpty() || "undefined".equals(id)){
			id = "no_id";
		}

		String openid = request.getParameter("openid");

		String uuid = request.getParameter("uuid");
		if(uuid == null || uuid.isEmpty() || "undefined".equals(uuid)){
			id = "no_id";
		}

		String restaurant = request.getParameter("restaurant");
		if(restaurant == null || restaurant.isEmpty() || "undefined".equals(restaurant)){
			restaurant = "请录入商铺";
		}

		String phone_number = request.getParameter("phone_number");
		if(phone_number == null || phone_number.isEmpty() || "undefined".equals(phone_number)){
			phone_number = "未录入";
		}

		String nick_name = request.getParameter("boss_name");
		if(nick_name == null || nick_name.isEmpty() || "undefined".equals(nick_name)){
			nick_name = "微信用户";
		}

		RestaurantUser restaurantUser = new RestaurantUser();
		String role ="boss";
		if(!"no_id".equals(id)){
			List<RestaurantUser> restaurantUsers = dao.getRestaurantUserById(id);
			restaurantUser = restaurantUsers.get(0);
			restaurant = restaurantUser.getRestaurant();
			role = "client";
		}

		restaurantUser.setNick_name(nick_name);
		restaurantUser.setPhone_number(phone_number);
		restaurantUser.setRole(role);
		restaurantUser.setOpenid(openid);
		restaurantUser.setRestaurant(restaurant);
		restaurantUser.setCreate_time(create_time);
		restaurantUser.setExpired_time(expired_time);
		restaurantUser.setLogo(uuid);

		List<RestaurantUser> restaurantUsers1 = dao.getRestaurantUserByOpenid(openid);
		if(restaurantUsers1.size()>0){
			dao.updateRestaurantByOpenid(restaurantUser);
		}else {
			restaurantService.insertRestaurantUser(restaurantUser);
		}

		return "push massage successfully";
	}

	@RequestMapping("/shareShopQrCode")
	@ResponseBody
	public JSONObject getQrCode(String id){
		JSONObject jsonObject = new JSONObject();
		String token = loginService.getToken("ORDER");
		String scene = "id=" + id;

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
	public List getRestaurantUserAll(String openid,String type){
		List list = null;
		try {
			list = restaurantService.getRestaurantUserAll(openid,type);
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

	@RequestMapping("/getRestaurantOrderByDay")
	@ResponseBody
	public List getRestaurantOrderByDay(String date_time){
		List list = null;
		try {
			list = restaurantService.getRestaurantOrderByDay(date_time);
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

	@RequestMapping("/getRestaurantMenuById")
	@ResponseBody
	public List getRestaurantMenuById(String id){
		List list = null;
		try {
			list = dao.getRestaurantMenuById(id);
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
				dao.updateRestaurantByBoss(restaurantUser);
			}else if("restaurant".equals(type)){
				String old_name = restaurantUser.getRestaurant();
				dao.updateRestaurantByAll(old_name,content);
			}else if("role".equals(type)){
				if("boss".equals(content)){
					content = "client";
				}else {
					content = "boss";
				}
				String restaurant = restaurantUser.getRestaurant();
				List<RestaurantUser> restaurantUsers1 = dao.getRestaurantBossByShop(restaurant);
				RestaurantUser restaurantUser1 = restaurantUsers1.get(0);

				restaurantUser.setRole(content);
				restaurantUser.setOpenid(openid);
				restaurantUser.setIs_free(restaurantUser1.getIs_free());
				dao.updateRestaurantUser(restaurantUser);
			}else if("phone_number".equals(type)){
				restaurantUser.setPhone_number(content);
				restaurantUser.setOpenid(openid);
				dao.updateRestaurantUser(restaurantUser);
			}else if("location".equals(type)){
				restaurantUser.setLocation(content);
				restaurantUser.setOpenid(openid);
				dao.updateRestaurantUser(restaurantUser);
			}else if("info".equals(type)){
				restaurantUser.setInfo(content);
				restaurantUser.setOpenid(openid);
				dao.updateRestaurantUser(restaurantUser);
			}else if("expired_time".equals(type)){
				restaurantUser.setExpired_time(content);
				dao.updateRestaurantByBoss(restaurantUser);
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
			List<Menu> menus = dao.getRestaurantMenuById(id);
			Menu menu = menus.get(0);
			if("food_image".equals(type)){
				menu.setFood_image(content);
				dao.updateRestaurantMenu(menu);
			}else if("delete".equals(type)){
				dao.deleteRestaurantFood(Integer.parseInt(id));
			}else if("price".equals(type)){
				menu.setPrice(Float.parseFloat(content));
				dao.updateRestaurantMenu(menu);
			}else if("introduce".equals(type)){
				menu.setIntroduce(content);
				dao.updateRestaurantMenu(menu);
			}else if("vuuid".equals(type)){
				menu.setVuuid(content);
				dao.updateRestaurantMenu(menu);
			}else if("category".equals(type)){
				menu.setCategory(content);
				dao.updateRestaurantMenu(menu);
			}else if("unit".equals(type)){
				menu.setUnit(content);
				dao.updateRestaurantMenu(menu);
			}else if("food_name".equals(type)){
				menu.setFood_name(content);
				dao.updateRestaurantMenu(menu);
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

	@RequestMapping("/updateRestaurantOrderDetail")
	@ResponseBody
	public int updateRestaurantOrderDetail(HttpServletRequest request, HttpServletResponse response){

		// 字段
		String id = request.getParameter("id");
		String uuid = request.getParameter("uuid");
		int res = 0;
		try {
			List<RestaurantOrder> restaurantOrders = dao.getRestaurantOrderById(id);
			RestaurantOrder restaurantOrder = restaurantOrders.get(0);
			restaurantOrder.setOrder_img(uuid);
			res = dao.updateRestaurantOrderDetail(restaurantOrder);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	@RequestMapping("/freeOpenShop")
	@ResponseBody
	public int freeOpenShop(String openid){

		List<RestaurantUser> restaurantUsers = dao.getRestaurantUser(openid);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(df.parse(create_time));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		int days = 365;
		cal.add(cal.DATE,days);
		String expired_time = df.format(cal.getTime());

		RestaurantUser restaurantUser = restaurantUsers.get(0);
		String restaurant = restaurantUser.getRestaurant();

		// 商户入驻
		Merchant merchant = new Merchant();
		merchant.setAppid(Constants.order_appid);
		merchant.setMchid(Constants.MCH_ID);
		merchant.setOpenid(openid);
		merchant.setStudio(restaurant);
		merchant.setCampus(restaurant);
		merchant.setSub_appid(Constants.order_appid);
		merchant.setSub_mchid(Constants.MCH_ID);
		merchant.setCreate_time(create_time);
		merchant.setType("商户平台");
		dao.insertMerchant(merchant);

		// 开通权限
		restaurantUser.setExpired_time(expired_time);
		restaurantUser.setMember("免费会员");
		restaurantUser.setRole("boss");
		restaurantUser.setIs_free(1);
		restaurantUser.setDays(days);

		// 老用户续费
		dao.updateRestaurantUserByBoss(restaurantUser);

		// 通知管理员
		loginService.sendFeedback(Constants.order_admin_openid,restaurant,expired_time,"365","蓝桃续费");
		// 通知客户
		loginService.sendFeedback(openid,restaurant,expired_time,"365","蓝桃续费");

		return 1;
	}


}
	
	


