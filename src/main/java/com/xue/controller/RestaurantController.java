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
import javax.xml.stream.Location;
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
		cal.add(cal.DATE,30);
		String expired_time = df.format(cal.getTime());

		//获取用户名
		String id = request.getParameter("id");
		String[] id_list = id.split("_");
		String gift_id = "no_id";
		if(id_list.length>1){
			id = id_list[0];
			gift_id = id_list[1];
		}

		String openid = request.getParameter("openid");
		String inviter_openid = "no_id";
		String restaurant = "荔乡杂货铺";

		List<RestaurantUser> restaurantUsers = dao.getRestaurantUserById(id);
		if(restaurantUsers.size()>0){
			RestaurantUser restaurantUser_get = restaurantUsers.get(0);
			restaurant = restaurantUser_get.getRestaurant();
			inviter_openid = restaurantUser_get.getOpenid();
		}


		RestaurantUser restaurantUser = new RestaurantUser();
		restaurantUser.setNick_name("微信用户");
		restaurantUser.setPhone_number("未录入");
		restaurantUser.setRole("client");
		restaurantUser.setOpenid(openid);
		restaurantUser.setRestaurant(restaurant);
		restaurantUser.setCreate_time(create_time);
		restaurantUser.setExpired_time(expired_time);
		restaurantUser.setInviter_openid(inviter_openid);
		restaurantUser.setShop_history(restaurant);

		List<RestaurantUser> restaurantUsers1 = dao.getRestaurantUserByOpenid(openid);
		if(restaurantUsers1.size()>0){
			RestaurantUser restaurantUser1 = restaurantUsers1.get(0);
			String role = restaurantUser1.getRole();
			String shop_history = restaurantUser1.getShop_history();
			String nick_name = restaurantUser1.getNick_name();
			String phone_number = restaurantUser1.getPhone_number();
			String location = restaurantUser1.getLocation();
			String logo = restaurantUser1.getLogo();
			String inviter_openid_get = restaurantUser1.getInviter_openid();
			if(!"boss".equals(role)){
				String[] shop_history_list =shop_history.split(",");
				List<String> shop_history_arrays = Arrays.asList(shop_history_list);
				String shop_history_new = shop_history;
				if(!shop_history_arrays.contains(restaurant)){
					shop_history_new = shop_history + "," + restaurant;

				}

				restaurantUser.setShop_history(shop_history_new);
				restaurantUser.setNick_name(nick_name);
				restaurantUser.setPhone_number(phone_number);
				restaurantUser.setLocation(location);
				restaurantUser.setLogo(logo);
				restaurantUser.setInviter_openid(inviter_openid_get);
				dao.updateRestaurantByOpenid(restaurantUser);
			}
		}else {
			restaurantUser.setShop_history(restaurant);
			restaurantService.insertRestaurantUser(restaurantUser);
			// 邀请赠券
			if(!"no_id".equals(inviter_openid)){
				List<RestaurantUser> restaurantUsers_get = dao.getRestaurantBossByShop(restaurant);
				RestaurantUser restaurantUser1 = restaurantUsers_get.get(0);
				Integer coupon_id = restaurantUser1.getCoupon_id();
				List<GiftList> giftLists = dao.getGiftListById(coupon_id.toString());
				if(giftLists.size() > 0){
					GiftList giftList = giftLists.get(0);

					Gift gift = new Gift();
					gift.setPrice(giftList.getPrice());
					gift.setUuids(giftList.getUuids());
					gift.setStudent_name(restaurantUser1.getNick_name());
					gift.setGift_name(giftList.getGift_name());
					gift.setGift_amount(1);
					gift.setCreate_time(create_time);
					gift.setExpired_time(create_time);
					gift.setStudio(restaurant);
					gift.setCampus(restaurant);
					gift.setStatus(0);
					gift.setGift_id(giftList.getId());
					gift.setOpenid(inviter_openid);
					gift.setType("邀请券");
					loginService.insertGift(gift);
				}
			}
		}

		// 扫码赠券
		if(!"no_id".equals(gift_id)){
			List<RestaurantUser> restaurantUsers2 = dao.getRestaurantUserByOpenid(openid);
			RestaurantUser restaurantUser2 = restaurantUsers2.get(0);
			String nick_name_get  = restaurantUser2.getNick_name();
			String restaurant_get = restaurantUser2.getRestaurant();
			List<GiftList> giftLists = dao.getGiftListById(gift_id);
			if(giftLists.size() > 0){
				GiftList giftList = giftLists.get(0);

				Gift gift = new Gift();
				gift.setPrice(giftList.getPrice());
				gift.setUuids(giftList.getUuids());
				gift.setStudent_name(nick_name_get);
				gift.setGift_name(giftList.getGift_name());
				gift.setGift_amount(1);
				gift.setCreate_time(create_time);
				gift.setExpired_time(create_time);
				gift.setStudio(restaurant_get);
				gift.setCampus(restaurant_get);
				gift.setStatus(0);
				gift.setGift_id(giftList.getId());
				gift.setOpenid(openid);
				gift.setType("扫码券");
				List<Gift> gifts = dao.getGiftByOpenidGiftid(openid,gift_id);
				if(gifts.size() == 0){
					loginService.insertGift(gift);
					Integer amount = giftList.getAmount();
					giftList.setAmount(amount + 1);
					dao.updateGiftDetail(giftList);
				}
			}
		}
		return "push massage successfully";
	}

	@RequestMapping("/insertRestaurantGift")
	@ResponseBody
	public String insertRestaurantGift(HttpServletRequest request, HttpServletResponse response){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String openid = request.getParameter("openid");
		String gift_id = request.getParameter("gift_id");

		try {
			List<RestaurantUser> restaurantUsers = dao.getRestaurantUserByOpenid(openid);
			RestaurantUser restaurantUser = restaurantUsers.get(0);

			List<GiftList> giftLists = dao.getGiftListById(gift_id);
			GiftList giftList = giftLists.get(0);

			Gift gift = new Gift();
			gift.setPrice(giftList.getPrice());
			gift.setUuids(giftList.getUuids());
			gift.setStudent_name(restaurantUser.getNick_name());
			gift.setGift_name(giftList.getGift_name());
			gift.setGift_amount(1);
			gift.setCreate_time(create_time);
			gift.setExpired_time(create_time);
			gift.setStudio(restaurantUser.getRestaurant());
			gift.setCampus(restaurantUser.getRestaurant());
			gift.setStatus(0);
			gift.setGift_id(giftList.getId());
			gift.setOpenid(openid);
			gift.setType(giftList.getType());
			loginService.insertGift(gift);
		} catch (Exception e) {
			throw new RuntimeException(e);
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
			List<RestaurantUser> restaurantUsers = dao.getRestaurantUserByOpenid("ougOI60Jjf6PkDHSI0mJDQ_129YM");
			RestaurantUser restaurantUser = restaurantUsers.get(0);
			restaurant = restaurantUser.getRestaurant();
		}

		String phone_number = request.getParameter("phone_number");
		if(phone_number == null || phone_number.isEmpty() || "undefined".equals(phone_number)){
			phone_number = "未录入";
		}

		String nick_name = request.getParameter("boss_name");
		if(nick_name == null || nick_name.isEmpty() || "undefined".equals(nick_name)){
			nick_name = "微信用户";
		}

		String role ="boss";
		if(!"no_id".equals(id)){
			List<RestaurantUser> restaurantUsers = dao.getRestaurantUserById(id);
			RestaurantUser restaurantUser = restaurantUsers.get(0);
			restaurant = restaurantUser.getRestaurant();
			role = "client";
		}

		RestaurantUser restaurantUser = new RestaurantUser();
		restaurantUser.setNick_name(nick_name);
		restaurantUser.setPhone_number(phone_number);
		restaurantUser.setRole(role);
		restaurantUser.setOpenid(openid);
		restaurantUser.setRestaurant(restaurant);
		restaurantUser.setCreate_time(create_time);
		restaurantUser.setExpired_time(expired_time);
		restaurantUser.setLogo(uuid);
		restaurantUser.setAvatarurl("525addcc-03e8-427f-944a-ac4ff38383b3.png");
		restaurantUser.setShop_history(restaurant);

		List<RestaurantUser> restaurantUsers = dao.getRestaurantUserByOpenid(openid);
		if(restaurantUsers.size()>0){
			dao.updateRestaurantByOpenid(restaurantUser);
		}else {
			restaurantService.insertRestaurantUser(restaurantUser);
		}

		return "push massage successfully";
	}

	@RequestMapping("/shareShopQrCode")
	@ResponseBody
	public JSONObject shareShopQrCode(String id){
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
		String unit = request.getParameter("unit");


		Menu menu =new Menu();
		menu.setRestaurant(restaurant);
		menu.setFood_name(food_name);
		menu.setFood_image(food_image);
		menu.setCategory(category);
		menu.setIntroduce(introduce);
		menu.setPrice(Float.parseFloat(price));
		menu.setCreate_time(create_time);
		menu.setUnit(unit);
		try {
			dao.insertRestaurantMenu(menu);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return "push massage successfully";
	}

	@RequestMapping("/insertShippingFee")
	@ResponseBody
	public String insertShippingFee(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String region = request.getParameter("region");
		String first_weight_1 = request.getParameter("first_weight_1");
		String first_weight_2 = request.getParameter("first_weight_2");
		String first_weight_5 = request.getParameter("first_weight_5");
		String additional_weight = request.getParameter("additional_weight");
		String preservation_fee = request.getParameter("preservation_fee");
		String restaurant = request.getParameter("restaurant");
		String type = request.getParameter("type");

		try {

			ShippingFee shippingFee =new ShippingFee();
			shippingFee.setRegion(region);
			shippingFee.setFirst_weight_1(Integer.parseInt(first_weight_1));
			shippingFee.setFirst_weight_2(Integer.parseInt(first_weight_2));
			shippingFee.setFirst_weight_5(Integer.parseInt(first_weight_5));
			shippingFee.setAdditional_weight(Integer.parseInt(additional_weight));
			shippingFee.setPreservation_fee(Integer.parseInt(preservation_fee));
			shippingFee.setRestaurant(restaurant);
			shippingFee.setCreate_time(create_time);
			shippingFee.setType(type);

			dao.insertShippingFee(shippingFee);
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
		String group_buy = request.getParameter("group_buy");
		if(group_buy == null || group_buy.isEmpty() || "undefined".equals(group_buy)){
			group_buy = "0";
		}
		String region = request.getParameter("region");
		if(region == null || region.isEmpty() || "undefined".equals(region)){
			region = "未录入";
		}
		String shipping_fee = request.getParameter("shipping_fee");
		if(shipping_fee == null || shipping_fee.isEmpty() || "undefined".equals(shipping_fee)){
			shipping_fee = "0";
		}
		String discount_ids = request.getParameter("discount_ids");
		if(discount_ids == null || discount_ids.isEmpty() || "undefined".equals(discount_ids)){
			discount_ids = "no_id";
		}


		String location = null;
		String phone_number = null;
		String nick_name = null;
		List<RestaurantUser> restaurantUsers = dao.getRestaurantUserByOpenid(openid);
		RestaurantUser restaurantUser = restaurantUsers.get(0);
		Integer location_id = restaurantUser.getLocation_id();
		nick_name = restaurantUser.getNick_name();
		phone_number = restaurantUser.getPhone_number();
		location = restaurantUser.getLocation();

		if(location_id != 0){
			List<RestaurantLocation> restaurantLocations = dao.getRestaurantLocationById(location_id);
			RestaurantLocation restaurantLocation = restaurantLocations.get(0);
			location = restaurantLocation.getLocation();
			phone_number = restaurantLocation.getPhone_number();
			nick_name = restaurantLocation.getNick_name();
		}

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
		restaurantOrder.setGroup_buy(Integer.parseInt(group_buy));
		restaurantOrder.setRegion(region);
		restaurantOrder.setShipping_fee(Float.parseFloat(shipping_fee));
		restaurantOrder.setDiscount_ids(discount_ids);
		restaurantOrder.setLocation_id(location_id);
		restaurantOrder.setNick_name(nick_name);
		restaurantOrder.setPhone_number(phone_number);
		restaurantOrder.setLocation(location);

		try {
			// 插入订单
			dao.insertRestaurantOrder(restaurantOrder);

			// 更新库存
			List<Menu> menus = dao.getRestaurantMenuById(goods_id);
			Menu menu = menus.get(0);
			Integer inventory = menu.getInventory();
			if(inventory > 0){
				inventory = inventory - Integer.parseInt(num);
				menu.setInventory(inventory);
				dao.updateRestaurantMenu(menu);
			}

			// 赠券
			List<Wallet> wallets = dao.getWalletByOrderNo(order_no);
			if(wallets.size() == 1){
				String inviter_openid = restaurantUser.getInviter_openid();
				if(!"no_id".equals(inviter_openid)){
					List<RestaurantUser> restaurantUsers_get = dao.getRestaurantUserByOpenid(inviter_openid);
					RestaurantUser restaurantUser1 = restaurantUsers_get.get(0);
					List<GiftList> giftLists = dao.getGiftListByType(restaurant,restaurant,4,"回赠券",20);
					if(giftLists.size() > 0){
						GiftList giftList = giftLists.get(0);

						Gift gift = new Gift();
						gift.setPrice(giftList.getPrice());
						gift.setUuids(giftList.getUuids());
						gift.setStudent_name(restaurantUser1.getNick_name());
						gift.setGift_name(giftList.getGift_name());
						gift.setGift_amount(1);
						gift.setCreate_time(create_time);
						gift.setExpired_time(create_time);
						gift.setStudio(restaurant);
						gift.setCampus(restaurant);
						gift.setStatus(0);
						gift.setGift_id(giftList.getId());
						gift.setOpenid(inviter_openid);
						gift.setType("回赠券");
						loginService.insertGift(gift);
					}
				}
			}

			// 验卷
			if("no_id".equals(discount_ids)){
				String[] discount_ids_list = discount_ids.split(",");
				for (int i = 0; i < discount_ids_list.length; i++) {
					String coupon_id = discount_ids_list[i];
					List<Gift> gifts = dao.getGiftById(coupon_id);
					if(gifts.size()>0){
						Gift gift = gifts.get(0);
						gift.setStatus(2);
						dao.updateGift(gift);
					}
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return "push massage successfully";
	}

	@RequestMapping("/insertRestaurantLocation")
	@ResponseBody
	public String insertRestaurantLocation(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String openid = request.getParameter("openid");
		String nick_name = request.getParameter("nick_name");
		String phone_number = request.getParameter("phone_number");
		String location = request.getParameter("location");

		RestaurantLocation restaurantLocation =new RestaurantLocation();
		restaurantLocation.setOpenid(openid);
		restaurantLocation.setNick_name(nick_name);
		restaurantLocation.setPhone_number(phone_number);
		restaurantLocation.setLocation(location);
		restaurantLocation.setCreate_time(create_time);

		try {
			// 插入订单
			dao.insertRestaurantLocation(restaurantLocation);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return "push massage successfully";
	}

	@RequestMapping("/insertRestaurantOrderCm")
	@ResponseBody
	public String insertRestaurantOrderCm(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String order_no = request.getParameter("order_no");
		String openid = request.getParameter("openid");
		String goods_id = request.getParameter("goods_id");
		String comment = request.getParameter("comment");
		String uuids = request.getParameter("uuids");


		RestaurantOrderCm restaurantOrderCm =new RestaurantOrderCm();
		restaurantOrderCm.setOrder_no(order_no);
		restaurantOrderCm.setOpenid(openid);
		restaurantOrderCm.setGoods_id(goods_id);
		restaurantOrderCm.setComment(comment);
		restaurantOrderCm.setUuids(uuids);
		restaurantOrderCm.setCreate_time(create_time);

		try {
			dao.insertRestaurantOrderCm(restaurantOrderCm);
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

	@RequestMapping("/getRestaurantShippingFee")
	@ResponseBody
	public List getRestaurantShippingFee(String restaurant){
		List list = null;
		try {
			list = dao.getRestaurantShippingFee(restaurant,"荔枝");
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

	@RequestMapping("/deleteRestaurantLocation")
	@ResponseBody
	public int deleteRestaurantLocation(Integer id){
		try {
			dao.deleteRestaurantLocation(id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@RequestMapping("/deleteShippingFee")
	@ResponseBody
	public int deleteShippingFee(Integer id){
		try {
			dao.deleteShippingFee(id);
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

	@RequestMapping("/deleteRestaurantOrderCm")
	@ResponseBody
	public int deleteRestaurantOrderCm(Integer id){
		try {
			dao.deleteRestaurantOrderCm(id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@RequestMapping("/getRestaurantOrder")
	@ResponseBody
	public List getRestaurantOrder(String openid,Integer page,String status){
		List list = null;
		try {
			list = restaurantService.getRestaurantOrder(openid,page,status);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getRestaurantLocation")
	@ResponseBody
	public List getRestaurantLocation(String openid){
		List list = null;
		try {
			list = restaurantService.getRestaurantLocation(openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getRestaurantOrderCmByOrderNo")
	@ResponseBody
	public List getRestaurantOrderCmByOrderNo(String order_no){
		List list = null;
		try {
			list = restaurantService.getRestaurantOrderCmByOrderNo(order_no);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getRestaurantOrderCmByGoodsId")
	@ResponseBody
	public List getRestaurantOrderCmByGoodsId(String goods_id){
		List list = null;
		try {
			list = restaurantService.getRestaurantOrderCmByGoodsId(goods_id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getRestaurantOrderByGoodsId")
	@ResponseBody
	public List getRestaurantOrderByGoodsId(String goods_id,Integer group_buy){
		List list = null;
		try {
			list = restaurantService.getRestaurantOrderByGoodsId(goods_id,group_buy);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getRestaurantOrderByDay")
	@ResponseBody
	public List getRestaurantOrderByDay(String date_time,String openid){
		List list = null;
		try {
			list = restaurantService.getRestaurantOrderByDay(date_time,openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getRestaurantOrderByLike")
	@ResponseBody
	public List getRestaurantOrderByLike(String restaurant,String content){
		List list = null;
		try {
			list = restaurantService.getRestaurantOrderByLike(restaurant,content);
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
	public List getRestaurantMenu(String restaurant,String openid){
		List list = null;
		try {
			list = restaurantService.getRestaurantMenu(restaurant,openid);
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
		String location_id = request.getParameter("location_id");
		if(location_id == null || location_id.isEmpty() || "undefined".equals(location_id)){
			location_id = "0";
		}

		try {
			List<RestaurantUser> restaurantUsers = dao.getRestaurantUser(openid);
			RestaurantUser restaurantUser = restaurantUsers.get(0);
			if("avatarurl".equals(type)){
				restaurantUser.setAvatarurl(content);
				dao.updateRestaurantUser(restaurantUser);
			}else if("nickName".equals(type)){
				if("0".equals(location_id)){
					restaurantUser.setNick_name(content);
					dao.updateRestaurantUser(restaurantUser);
				}else {
					List<RestaurantLocation> restaurantLocations = dao.getRestaurantLocation(location_id);
					RestaurantLocation restaurantLocation = restaurantLocations.get(0);
					restaurantLocation.setNick_name(content);
					dao.updateRestaurantLocationDetail(restaurantLocation);
				}
			}else if("logo".equals(type)){
				restaurantUser.setLogo(content);
				dao.updateRestaurantByBoss(restaurantUser);
			}else if("coupon_id".equals(type)){
				restaurantUser.setCoupon_id(Integer.parseInt(content));
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
				if("0".equals(location_id)){
					restaurantUser.setPhone_number(content);
					dao.updateRestaurantUser(restaurantUser);
				}else {
					List<RestaurantLocation> restaurantLocations = dao.getRestaurantLocationById(Integer.parseInt(location_id));
					RestaurantLocation restaurantLocation = restaurantLocations.get(0);
					restaurantLocation.setPhone_number(content);
					dao.updateRestaurantLocationDetail(restaurantLocation);
				}
			}else if("location".equals(type)){
				if("0".equals(location_id)){
					restaurantUser.setLocation(content);
					dao.updateRestaurantUser(restaurantUser);
				}else {
					List<RestaurantLocation> restaurantLocations = dao.getRestaurantLocationById(Integer.parseInt(location_id));
					RestaurantLocation restaurantLocation = restaurantLocations.get(0);
					restaurantLocation.setLocation(content);
					dao.updateRestaurantLocationDetail(restaurantLocation);
				}
			}else if("info".equals(type)){
				restaurantUser.setInfo(content);
				dao.updateRestaurantByBoss(restaurantUser);
			}else if("expired_time".equals(type)){
				restaurantUser.setExpired_time(content);
				dao.updateRestaurantByBoss(restaurantUser);
			}else if("promise".equals(type)){
				restaurantUser.setPromise(content);
				dao.updateRestaurantByBoss(restaurantUser);
			}else if("restaurant_change".equals(type)){
				restaurantUser.setRestaurant(content);
				dao.updateRestaurantUser(restaurantUser);
			}else if("location_id".equals(type)){
				restaurantUser.setLocation_id(Integer.parseInt(content));
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
			}else if("group_buy".equals(type)){
				int new_group = 0;
				Integer group_buy = menu.getGroup_buy();
				if(group_buy == 0){
					new_group = 1;
				}
				menu.setGroup_buy(new_group);
				dao.updateRestaurantMenu(menu);
			}else if("group_price".equals(type)){
				menu.setGroup_price(Float.parseFloat(content));
				dao.updateRestaurantMenu(menu);
			}else if("group_limit".equals(type)){
				menu.setGroup_limit(Integer.parseInt(content));
				dao.updateRestaurantMenu(menu);
			}else if("open_time".equals(type)){
				menu.setOpen_time(content);
				dao.updateRestaurantMenu(menu);
			}else if("inventory".equals(type)){
				menu.setInventory(Integer.parseInt(content));
				dao.updateRestaurantMenu(menu);
			}else if("discount".equals(type)){
				menu.setDiscount(Float.parseFloat(content));
				dao.updateRestaurantMenu(menu);
			}else if("for_coupon".equals(type)){
				Integer for_coupon_get = menu.getFor_coupon();
				Integer for_coupon = 1;
				if(for_coupon_get == 1){
					for_coupon = 0;
				}
				menu.setFor_coupon(for_coupon);
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
		String type = request.getParameter("type");

		try {
			List<RestaurantOrder> restaurantOrders = dao.getRestaurantOrderById(id);
			RestaurantOrder restaurantOrder = restaurantOrders.get(0);
			if("订单状态".equals(type)){
				int status = restaurantOrder.getStatus();
				if(status == 0){
					status = 1;
				}else if(status == 1){
					status =2;
				}
				restaurantOrder.setStatus(status);
			}

			if("接单状态".equals(type)){
				int shop_status = restaurantOrder.getShop_status();
				if(shop_status == 0){
					shop_status = 1;
				}else {
					shop_status = 0;
				}
				restaurantOrder.setShop_status(shop_status);
			}

			dao.updateRestaurantOrderStatus(restaurantOrder);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;
	}

	@RequestMapping("/updateBookDetailById")
	@ResponseBody
	public int updateBookDetailById(HttpServletRequest request, HttpServletResponse response){

		//获取文字
		String id = request.getParameter("id");
		String type = request.getParameter("type");
		String content = request.getParameter("content");

		try {
			List<BookDetail> bookDetails = dao.getBookDetailById(id);
			BookDetail bookDetail = bookDetails.get(0);
			if("mark".equals(type)){
				bookDetail.setMark(content);
			}else if("amount".equals(type)){
				bookDetail.setAmount(Float.parseFloat(content));
			}
			dao.updateBookDetailById(bookDetail);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;
	}

	@RequestMapping("/updateRestaurantLocationId")
	@ResponseBody
	public int updateRestaurantLocationId(HttpServletRequest request, HttpServletResponse response){

		//获取文字
		String openid = request.getParameter("openid");
		String location_id = request.getParameter("location_id");

		try {
			dao.updateRestaurantLocationId(openid,location_id);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;
	}

	@RequestMapping("/getRestaurantOrderById")
	@ResponseBody
	public List getRestaurantOrderById(HttpServletRequest request, HttpServletResponse response){

		//获取文字
		String id = request.getParameter("id");

		List<RestaurantOrder> restaurantOrders = null;
		try {
			restaurantOrders = dao.getRestaurantOrderById(id);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return restaurantOrders;
	}

	@RequestMapping("/updateRestaurantOrderDetail")
	@ResponseBody
	public int updateRestaurantOrderDetail(HttpServletRequest request, HttpServletResponse response){

		// 字段
		String id = request.getParameter("id");
		String content = request.getParameter("content");
		String type = request.getParameter("type");

		int res = 0;
		try {
			List<RestaurantOrder> restaurantOrders = dao.getRestaurantOrderById(id);
			RestaurantOrder restaurantOrder = restaurantOrders.get(0);
			if("order_img".equals(type)){
				restaurantOrder.setOrder_img(content);
			}else if("goods_id".equals(type)){
				List<Menu> menus = dao.getRestaurantMenuById(content);
				Menu menu = menus.get(0);
				String category = menu.getCategory();
				restaurantOrder.setCategory(category);
				restaurantOrder.setGoods_id(content);
			}else if("mark".equals(type)){
				restaurantOrder.setMark(content);
			}else if("sf_order_no".equals(type)){
				restaurantOrder.setSf_order_no(content);
			}

			res = dao.updateRestaurantOrderDetail(restaurantOrder);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	@RequestMapping("/updateRestaurantOrderCm")
	@ResponseBody
	public int updateRestaurantOrderCm(HttpServletRequest request, HttpServletResponse response){

		// 字段
		String id = request.getParameter("id");
		String content = request.getParameter("content");
		String type = request.getParameter("type");

		int res = 0;
		try {
			List<RestaurantOrderCm> restaurantOrderCms = dao.getRestaurantOrderCmById(id);
			RestaurantOrderCm restaurantOrderCm = restaurantOrderCms.get(0);
			if("comment".equals(type)){
				restaurantOrderCm.setComment(content);
			}
			res = dao.updateRestaurantOrderCm(restaurantOrderCm);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	@RequestMapping("/freeOpenShop")
	@ResponseBody
	public int freeOpenShop(String openid){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(df.parse(create_time));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		int days = 0;
		cal.add(cal.DATE,days);
		String expired_time = df.format(cal.getTime());

		List<RestaurantUser> restaurantUsers = dao.getRestaurantUser(openid);
		RestaurantUser restaurantUser = restaurantUsers.get(0);
		String restaurant = restaurantUser.getRestaurant();

		if(!"请录入商铺".equals(restaurant) && !"未录入".equals(restaurant)){
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
			List<Merchant> merchants = dao.getMerchant(restaurant,restaurant,Constants.order_appid);
			if(merchants.size() == 0){
				dao.insertMerchant(merchant);
			}

			// 开通权限
			restaurantUser.setExpired_time(expired_time);
			restaurantUser.setMember("免费会员");
			restaurantUser.setRole("boss");
			restaurantUser.setIs_free(1);
			restaurantUser.setDays(days);

			// 老用户续费
			dao.updateRestaurantUserByBoss(restaurantUser);

			// 通知管理员
			loginService.sendFeedback(Constants.order_admin_openid,restaurant,expired_time,"0","蓝桃续费");
			// 通知客户
			loginService.sendFeedback(openid,restaurant,expired_time,"0","蓝桃续费");
		}

		return 1;
	}


}
	
	


