package com.xue.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import com.xue.config.Constants;
import com.xue.entity.model.*;
import com.xue.repository.dao.UserMapper;
import com.xue.service.RestaurantService;
import com.xue.service.WebPushService;
import com.xue.service.WechatPayService;
import com.xue.util.WechatPayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserMapper dao;

    @Autowired
    private WebPushService webPushService;

    @Override
    public List getRestaurantUserAll(String openid,String type) {
        String role = null;
        String avatarurl = null;
        String nick_name = null;
        String user_type = null;
        String create_time = null;
        String expired_time = null;
        List<RestaurantUser> list= null;
        int id = 0;
        String logo = null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<RestaurantUser> restaurantUsers = dao.getRestaurantUser(openid);
            RestaurantUser restaurant_get = restaurantUsers.get(0);
            String restaurant = restaurant_get.getRestaurant();

            if("platform".equals(type)){
                list= dao.getRestaurantUserAll();
            }else if("shop".equals(type)){
                list = dao.getRestaurantUserByShop(restaurant);
            }


            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                RestaurantUser line = list.get(i);
                //获取字段
                role = line.getRole();
                avatarurl = line.getAvatarurl();
                nick_name = line.getNick_name();
                restaurant = line.getRestaurant();
                create_time = line.getCreate_time();
                expired_time = line.getExpired_time();
                openid = line.getOpenid();
                logo = line.getLogo();
                id = line.getId();
                String role_name = "客户";
                if("boss".equals(role)){
                    role_name = "店长";
                }
                String phone_number = line.getPhone_number();
                String location = line.getLocation();
                String member = line.getMember();

                //json
                jsonObject.put("id", id);
                jsonObject.put("role", role);
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("restaurant", restaurant);
                jsonObject.put("user_type", user_type);
                jsonObject.put("create_time", create_time);
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("openid",openid);
                jsonObject.put("logo",logo);
                jsonObject.put("role_name",role_name);
                jsonObject.put("phone_number",phone_number);
                jsonObject.put("location",location);
                jsonObject.put("member",member);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getRestaurantOrder(String openid, String type) {
        List<RestaurantOrder> list= null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<RestaurantUser> restaurantUser = dao.getRestaurantUser(openid);
            String restaurant = restaurantUser.get(0).getRestaurant();
            String role = restaurantUser.get(0).getRole();
            if ("boss".equals(role)) {
                list = dao.getRestaurantOrderByShop(restaurant);
            }else {
                list = dao.getRestaurantOrderByOpenid(openid);
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                RestaurantOrder line = list.get(i);
                //获取字段
                String food_name = line.getFood_name();
                restaurant = line.getRestaurant();
                String category = line.getCategory();
                int num = line.getNum();
                Float price = line.getPrice();
                String create_time = line.getCreate_time();
                int status = line.getStatus();
                String status_cn = "去发货";
                if(status==1){
                    status_cn = "去完成";
                }else if(status == 2){
                    status_cn = "已完成";
                }else if(status == 3){
                    status_cn = "已退款";
                }

                String nick_name = null;
                String phone_number = null;
                String location = null;
                String openid_get = line.getOpenid();
                List<RestaurantUser> restaurantUser_get = dao.getRestaurantUser(openid_get);
                if(restaurantUser_get.size()>0){
                    RestaurantUser restaurantUser1 = restaurantUser_get.get(0);
                    nick_name = restaurantUser1.getNick_name();
                    phone_number = restaurantUser1.getPhone_number();
                    location = restaurantUser1.getLocation();
                }
                String id = line.getId();
                Float total_price = num * price ;
                String goods_id = line.getGoods_id();
                List<Menu> menus = dao.getRestaurantMenuById(goods_id);

                String food_image = null;
                if(menus.size()>0) {
                    food_image = menus.get(0).getFood_image();
                }
                String order_no = line.getOrder_no();
                String order_img = line.getOrder_img();
                int shop_status = line.getShop_status();
                List<Wallet> wallets = dao.getWalletByOrderNo(order_no);
                Integer amount = 0;
                if(wallets.size()>0){
                    Wallet wallet = wallets.get(0);
                    amount = wallet.getAmount();
                }

                //json
                jsonObject.put("amount", amount);
                jsonObject.put("shop_status", shop_status);
                jsonObject.put("order_img", order_img);
                jsonObject.put("food_image", food_image);
                jsonObject.put("food_name", food_name);
                jsonObject.put("restaurant", restaurant);
                jsonObject.put("category", category);
                jsonObject.put("num", num);
                jsonObject.put("price", price);
                jsonObject.put("create_time", create_time);
                jsonObject.put("status", status);
                jsonObject.put("status_cn", status_cn);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("id", id);
                jsonObject.put("total_price", total_price);
                jsonObject.put("order_no", order_no);
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("location", location);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getRestaurantOrderCmByOrderNo(String order_no) {
        List<RestaurantOrder> list= null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<RestaurantOrderCm> restaurantOrderCms = dao.getRestaurantOrderCmByOrderNo(order_no);
            for (int i = 0; i < restaurantOrderCms.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                RestaurantOrderCm line = restaurantOrderCms.get(i);
                //获取字段
                String comment = line.getComment();
                String uuids = line.getUuids();
                String create_time = line.getCreate_time();
                String openid = line.getOpenid();
                String nick_name = null;
                List<RestaurantUser> restaurantUser_get = dao.getRestaurantUser(openid);
                if(restaurantUser_get.size()>0){
                    RestaurantUser restaurantUser1 = restaurantUser_get.get(0);
                    nick_name = restaurantUser1.getNick_name();
                }
                String id = line.getId();


                //json
                jsonObject.put("id", id);
                jsonObject.put("comment", comment);
                jsonObject.put("uuids", uuids);
                jsonObject.put("openid", openid);
                jsonObject.put("create_time", create_time);
                jsonObject.put("order_no", order_no);
                jsonObject.put("nick_name", nick_name);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getRestaurantOrderCmByGoodsId(String goods_id) {
        List<RestaurantOrder> list= null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<RestaurantOrderCm> restaurantOrderCms = dao.getRestaurantOrderCmByGoodsId(goods_id);
            for (int i = 0; i < restaurantOrderCms.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                RestaurantOrderCm line = restaurantOrderCms.get(i);
                //获取字段
                String comment = line.getComment();
                String uuids = line.getUuids();
                String create_time = line.getCreate_time();
                String order_no = line.getOrder_no();
                String openid = line.getOpenid();
                String nick_name = null;
                List<RestaurantUser> restaurantUser_get = dao.getRestaurantUser(openid);
                if(restaurantUser_get.size()>0){
                    RestaurantUser restaurantUser1 = restaurantUser_get.get(0);
                    nick_name = restaurantUser1.getNick_name();
                }
                String id = line.getId();

                //json
                jsonObject.put("id", id);
                jsonObject.put("comment", comment);
                jsonObject.put("uuids", uuids);
                jsonObject.put("openid", openid);
                jsonObject.put("create_time", create_time);
                jsonObject.put("order_no", order_no);
                jsonObject.put("nick_name", nick_name);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getRestaurantOrderByDay(String date_time) {
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<RestaurantOrder> list = dao.getRestaurantOrderByDay(date_time);

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                RestaurantOrder line = list.get(i);
                //获取字段
                String food_name = line.getFood_name();
                String restaurant = line.getRestaurant();
                String category = line.getCategory();
                int num = line.getNum();
                Float price = line.getPrice();
                String create_time = line.getCreate_time();
                int status = line.getStatus();
                String status_cn = "去发货";
                if(status==1){
                    status_cn = "去完成";
                }else if(status == 2){
                    status_cn = "已完成";
                }else if(status == 3){
                    status_cn = "已退款";
                }

                String nick_name = null;
                String phone_number = null;
                String location = null;
                String openid_get = line.getOpenid();
                List<RestaurantUser> restaurantUser_get = dao.getRestaurantUser(openid_get);
                if(restaurantUser_get.size()>0){
                    RestaurantUser restaurantUser1 = restaurantUser_get.get(0);
                    nick_name = restaurantUser1.getNick_name();
                    phone_number = restaurantUser1.getPhone_number();
                    location = restaurantUser1.getLocation();
                }
                String id = line.getId();
                Float total_price = num * price ;
                String goods_id = line.getGoods_id();
                List<Menu> menus = dao.getRestaurantMenuById(goods_id);
                String food_image = null;
                if(menus.size()>0) {
                    food_image = menus.get(0).getFood_image();
                }

                String order_no = line.getOrder_no();
                String order_img = line.getOrder_img();
                int shop_status = line.getShop_status();

                //json
                jsonObject.put("shop_status", shop_status);
                jsonObject.put("order_img", order_img);
                jsonObject.put("food_image", food_image);
                jsonObject.put("food_name", food_name);
                jsonObject.put("restaurant", restaurant);
                jsonObject.put("category", category);
                jsonObject.put("num", num);
                jsonObject.put("price", price);
                jsonObject.put("create_time", create_time);
                jsonObject.put("status", status);
                jsonObject.put("status_cn", status_cn);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("id", id);
                jsonObject.put("total_price", total_price);
                jsonObject.put("order_no", order_no);
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("location", location);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getRestaurantCategory(String restaurant) {
        List<Menu> list= null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getRestaurantCategory(restaurant);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Menu line = list.get(i);
                //获取字段
                String category = line.getCategory();

                //json
                jsonObject.put("category", category);
                jsonObject.put("rank", i);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getRestaurantMenu(String restaurant) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<Menu> list = dao.getRestaurantMenu(restaurant);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Menu line = list.get(i);
                //获取字段
                String category = line.getCategory();
                String food_name = line.getFood_name();
                String food_image = line.getFood_image();
                String introduce = line.getIntroduce();
                Float price = line.getPrice();
                String id = line.getId();
                String vuuid = line.getVuuid();
                String unit = line.getUnit();
                Integer group_buy = line.getGroup_buy();
                Float group_price = line.getGroup_price();

                //json
                jsonObject.put("category", category);
                jsonObject.put("food_name", food_name);
                jsonObject.put("food_image", food_image);
                jsonObject.put("introduce", introduce);
                jsonObject.put("price", price);
                jsonObject.put("id", id);
                jsonObject.put("num", 0);
                jsonObject.put("vuuid", vuuid);
                jsonObject.put("unit", unit);
                jsonObject.put("group_buy", group_buy);
                jsonObject.put("group_price", group_price);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public int insertRestaurantUser(RestaurantUser restaurantUser) {
        int result = 0;
        try {
            result = dao.insertRestaurantUser(restaurantUser);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }

    @Override
    public List getRestaurantOrderByGoodsId(String goods_id, Integer group_buy) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<RestaurantOrder> list = dao.getRestaurantOrderByGoodsId(goods_id,group_buy);

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                RestaurantOrder line = list.get(i);
                //获取字段
                String food_name = line.getFood_name();
                String restaurant = line.getRestaurant();
                String category = line.getCategory();
                int num = line.getNum();
                Float price = line.getPrice();
                String create_time = line.getCreate_time();
                int status = line.getStatus();
                String status_cn = "去发货";
                if(status==1){
                    status_cn = "去完成";
                }else if(status == 2){
                    status_cn = "已完成";
                }else if(status == 3){
                    status_cn = "已退款";
                }

                String nick_name = null;
                String phone_number = null;
                String location = null;
                String avatarurl = null;
                String openid_get = line.getOpenid();
                List<RestaurantUser> restaurantUser_get = dao.getRestaurantUser(openid_get);
                if(restaurantUser_get.size()>0){
                    RestaurantUser restaurantUser1 = restaurantUser_get.get(0);
                    nick_name = restaurantUser1.getNick_name();
                    phone_number = restaurantUser1.getPhone_number();
                    location = restaurantUser1.getLocation();
                    avatarurl = restaurantUser1.getAvatarurl();
                }
                String id = line.getId();
                Float total_price = num * price ;
                goods_id = line.getGoods_id();
                List<Menu> menus = dao.getRestaurantMenuById(goods_id);
                String food_image = null;
                if(menus.size()>0) {
                    food_image = menus.get(0).getFood_image();
                }

                String order_no = line.getOrder_no();
                String order_img = line.getOrder_img();

                //json
                jsonObject.put("order_img", order_img);
                jsonObject.put("food_image", food_image);
                jsonObject.put("food_name", food_name);
                jsonObject.put("restaurant", restaurant);
                jsonObject.put("category", category);
                jsonObject.put("num", num);
                jsonObject.put("price", price);
                jsonObject.put("create_time", create_time);
                jsonObject.put("status", status);
                jsonObject.put("status_cn", status_cn);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("id", id);
                jsonObject.put("total_price", total_price);
                jsonObject.put("order_no", order_no);
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("location", location);
                jsonObject.put("avatarurl", avatarurl);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getRestaurantUser(String openid) {
        List<RestaurantUser> list= null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getRestaurantUser(openid);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                RestaurantUser line = list.get(i);

                //获取字段
                String expired_time = line.getExpired_time();
                String role = line.getRole();
                String create_time = line.getCreate_time();
                openid = line.getOpenid();
                String avatarurl = line.getAvatarurl();
                String nick_name = line.getNick_name();
                String restaurant = line.getRestaurant();
                String logo = line.getLogo();
                String boss_name = null;
                String boss_phone = null;
                String boss_info = null;
                String boss_promise = null;
                List<RestaurantUser> restaurantUsers = dao.getRestaurantBossByShop(restaurant);
                if(restaurantUsers.size()>0){
                    RestaurantUser restaurantUser = restaurantUsers.get(0);
                    logo = restaurantUser.getLogo();
                    boss_name = restaurantUser.getNick_name();
                    boss_phone = restaurantUser.getPhone_number();
                    boss_info = restaurantUser.getInfo();
                    boss_promise = restaurantUser.getPromise();
                }

                int is_merchant = 0;
                List<Merchant> merchants = dao.getMerchant(restaurant,restaurant,Constants.order_appid);
                if(merchants.size()>0){
                    is_merchant = 1;
                }

                int is_free = line.getIs_free();
                Integer id = line.getId();
                String phone_number = line.getPhone_number();
                String location = line.getLocation();
                String role_name = "顾客";
                long diff = 0;
                if("boss".equals(role)){
                    role_name = "店长";
                    // 判断权限
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
                    String now_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

                    try {
                        Date date1 = df.parse(now_time);
                        Date date2 = df.parse(expired_time);
                        diff = date2.getTime() - date1.getTime();
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    if(diff < 0){
                        line.setRole("client");
                        line.setDays(0);
                        dao.updateRestaurantUserByBoss(line);
                    }
                }

                Integer days = line.getDays();



                //json
                jsonObject.put("boss_promise", boss_promise);
                jsonObject.put("diff", diff);
                jsonObject.put("days", days);
                jsonObject.put("boss_info", boss_info);
                jsonObject.put("id", id);
                jsonObject.put("role", role);
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("restaurant", restaurant);
                jsonObject.put("create_time", create_time);
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("openid",openid);
                jsonObject.put("logo",logo);
                jsonObject.put("role_name",role_name);
                jsonObject.put("phone_number",phone_number);
                jsonObject.put("location",location);
                jsonObject.put("boss_name",boss_name);
                jsonObject.put("boss_phone",boss_phone);
                jsonObject.put("is_merchant",is_merchant);
                jsonObject.put("is_free",is_free);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }



}
