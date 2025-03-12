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
import com.xue.entity.model.Menu;
import com.xue.entity.model.RestaurantOrder;
import com.xue.entity.model.RestaurantUser;
import com.xue.repository.dao.UserMapper;
import com.xue.service.RestaurantService;
import com.xue.service.WebPushService;
import com.xue.service.WechatPayService;
import com.xue.util.WechatPayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserMapper dao;

    @Autowired
    private WebPushService webPushService;

    @Override
    public List getRestaurantUserAll(String openid) {
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

            if("ougOI60Jjf6PkDHSI0mJDQ_129YM".equals(openid)){
                list= dao.getRestaurantUserAll();
            }else {
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
                String status_cn = "去付款";
                if(status==1){
                    status_cn = "去发货";
                }else if(status == 2){
                    status_cn = "已完成";
                }

                String nick_name = null;
                String food_image = null;
                String openid_get = line.getOpenid();
                List<RestaurantUser> restaurantUser_get = dao.getRestaurantUser(openid_get);
                if(restaurantUser_get.size()>0){
                    nick_name = restaurantUser_get.get(0).getNick_name();
                }
                String id = line.getId();
                Float total_price = num * price ;
                String goods_id = line.getGoods_id();
                List<Menu> menus = dao.getRestaurantMenuById(goods_id);
                if(menus.size()>0) {
                    food_image = menus.get(0).getFood_image();
                }

                //json
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
        List<Menu> list= null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getRestaurantMenu(restaurant);
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

                //json
                jsonObject.put("category", category);
                jsonObject.put("food_name", food_name);
                jsonObject.put("food_image", food_image);
                jsonObject.put("introduce", introduce);
                jsonObject.put("price", price);
                jsonObject.put("id", id);
                jsonObject.put("num", 0);
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
    public List getRestaurantUser(String openid) {
        List<RestaurantUser> list= null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getRestaurantUser(openid);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                RestaurantUser line = list.get(i);
                //获取字段
                String role = line.getRole();
                String avatarurl = line.getAvatarurl();
                String nick_name = line.getNick_name();
                String restaurant = line.getRestaurant();
                String create_time = line.getCreate_time();
                String expired_time = line.getExpired_time();
                openid = line.getOpenid();
                String logo = line.getLogo();
                Integer id = line.getId();
                String role_name = "普通";
                if("boss".equals(role)){
                    role_name = "群主";
                }
                String phone_number = line.getPhone_number();
                String location = line.getLocation();

                //json
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
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }



}
