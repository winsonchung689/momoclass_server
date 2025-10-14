package com.xue.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.xue.config.Constants;
import com.xue.entity.model.*;
import com.xue.repository.dao.UserMapper;
import com.xue.service.SpaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SpaceServiceImpl implements SpaceService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserMapper dao;


    @Override
    public List getSpaceTeacher(String openid) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<SpaceTeacher> spaceTeachers = dao.getSpaceTeacher(openid);
            for (int i = 0; i < spaceTeachers.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                SpaceTeacher line = spaceTeachers.get(i);
                //获取字段
                String id = line.getId();
                String uuids = line.getUuids();
                String intro = line.getIntro();
                String create_time = line.getCreate_time();
                openid = line.getOpenid();

                //json
                jsonObject.put("id", id);
                jsonObject.put("openid", openid);
                jsonObject.put("uuids", uuids);
                jsonObject.put("intro", intro);
                jsonObject.put("create_time", create_time);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getBookUser(String openid) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        String date_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<BookUser>  list = dao.getBookUser(openid);

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                BookUser line = list.get(i);
                //获取字段
                String role = line.getRole();
                String avatarurl = line.getAvatarurl();
                String nick_name = line.getNick_name();
                String create_time = line.getCreate_time();
                String expired_time = line.getExpired_time();

                // 过期回收权限
                String today_time = df.format(new Date());
                Date today_dt = df.parse(today_time.substring(0,10));
                Date expired_dt = df.parse(expired_time.substring(0,10));
                int compare = today_dt.compareTo(expired_dt);
                if(role.equals("boss") && compare > 0){
                    line.setRole("client");
                    dao.updateBookUserDetail(line);
                }

                String book_name = line.getBook_name();
                openid = line.getOpenid();
                String logo = line.getLogo();
                int id = line.getId();
                String location = line.getLocation();

                // 获取广告主信息
                String openid_qr = line.getOpenid_qr();
                if(!"boss".equals(role)){
                    List<BookUser> bookUsers = dao.getBookUser(openid_qr);
                    BookUser bookUser = bookUsers.get(0);
                    logo = bookUser.getLogo();
                    nick_name = bookUser.getNick_name();
                    location = bookUser.getLocation();
                    book_name = bookUser.getBook_name();
                    avatarurl = bookUser.getAvatarurl();
                    id = bookUser.getId();
                }
                // 获取私人信息
                String student_name = line.getStudent_name();
                String phone_number = line.getPhone_number();
                String my_nick = line.getNick_name();
                String my_avatarurl = line.getAvatarurl();
                int my_id = line.getId();


                //json
                jsonObject.put("my_id", my_id);
                jsonObject.put("id", id);
                jsonObject.put("role", role);
                jsonObject.put("student_name", student_name);
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("my_nick", my_nick);
                jsonObject.put("my_avatarurl", my_avatarurl);
                jsonObject.put("location", location);
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("create_time", create_time);
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("openid",openid);
                jsonObject.put("logo",logo);
                jsonObject.put("book_name",book_name);
                jsonObject.put("openid_qr",openid_qr);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getSpaceCases(String openid) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<SpaceCases> spaceCases = dao.getSpaceCases(openid);
            for (int i = 0; i < spaceCases.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                SpaceCases line = spaceCases.get(i);
                //获取字段
                String id = line.getId();
                String uuids = line.getUuids();
                String intro = line.getIntro();
                String create_time = line.getCreate_time();
                openid = line.getOpenid();
                String student_name = line.getStudent_name();
                String subject = line.getSubject();

                //json
                jsonObject.put("id", id);
                jsonObject.put("openid", openid);
                jsonObject.put("uuids", uuids);
                jsonObject.put("intro", intro);
                jsonObject.put("create_time", create_time);
                jsonObject.put("student_name", student_name);
                jsonObject.put("subject", subject);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getSpaceLesson(String openid) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<SpaceLesson> spaceLessons = dao.getSpaceLesson(openid);
            for (int i = 0; i < spaceLessons.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                SpaceLesson line = spaceLessons.get(i);
                //获取字段
                String id = line.getId();
                String name = line.getName();
                String subject = line.getSubject();
                String price = line.getPrice();
                String create_time = line.getCreate_time();
                openid = line.getOpenid();

                //json
                jsonObject.put("id", id);
                jsonObject.put("openid", openid);
                jsonObject.put("name", name);
                jsonObject.put("price", price);
                jsonObject.put("create_time", create_time);
                jsonObject.put("subject", subject);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getSpaceOrder(String openid) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<SpaceOrder> spaceOrders = dao.getSpaceOrderByOpenid(openid);
            List<BookUser> bookUsers = dao.getBookUser(openid);
            BookUser bookUser = bookUsers.get(0);
            String role = bookUser.getRole();
            if("boss".equals(role)){
                spaceOrders = dao.getSpaceOrderByOpenidQr(openid);
            }

            for (int i = 0; i < spaceOrders.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                SpaceOrder line = spaceOrders.get(i);
                //获取字段
                Integer status = line.getStatus();
                String create_time = line.getCreate_time();
                String id = line.getId();
                String lesson_id = line.getLesson_id();
                List<SpaceLesson> spaceLessons = dao.getSpaceLessonById(lesson_id);
                SpaceLesson spaceLesson = spaceLessons.get(0);
                String name = spaceLesson.getName();
                String subject = spaceLesson.getSubject();
                String price = spaceLesson.getPrice();
                String openid_get = line.getOpenid();
                List<BookUser> bookUsers1 = dao.getBookUser(openid_get);
                BookUser bookUser1 = bookUsers1.get(0);
                String nick_name = bookUser1.getNick_name();
                String student_name = bookUser1.getStudent_name();
                String phone_number = bookUser1.getPhone_number();
                String status_cn = "未支付";
                if(status == 1){
                    status_cn = "已支付";
                }

                //json
                jsonObject.put("id", id);
                jsonObject.put("status_cn", status_cn);
                jsonObject.put("openid", openid_get);
                jsonObject.put("name", name);
                jsonObject.put("subject", subject);
                jsonObject.put("price", price);
                jsonObject.put("create_time", create_time);
                jsonObject.put("status", status);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("student_name", student_name);
                jsonObject.put("phone_number", phone_number);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getSpaceGoodsList(String openid) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<SpaceGoodsList> spaceGoodsLists = dao.getSpaceGoodsListByOpenid(openid);
            for (int i = 0; i < spaceGoodsLists.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                SpaceGoodsList line = spaceGoodsLists.get(i);
                //获取字段
                String create_time = line.getCreate_time();
                String id = line.getId();
                String goods_name = line.getGoods_name();
                String goods_intro = line.getGoods_intro();
                String price_list = line.getPrice_list();
                String uuids = line.getUuids();
                String vuids = line.getVuids();


                //json
                jsonObject.put("id", id);
                jsonObject.put("goods_name", goods_name);
                jsonObject.put("goods_intro", goods_intro);
                jsonObject.put("price_list", price_list);
                jsonObject.put("uuids", uuids);
                jsonObject.put("vuids", vuids);
                jsonObject.put("create_time", create_time);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getSpaceGoodsListById(String goods_id) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<SpaceGoodsList> spaceGoodsLists = dao.getSpaceGoodsListById(goods_id);
            for (int i = 0; i < spaceGoodsLists.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                SpaceGoodsList line = spaceGoodsLists.get(i);
                //获取字段
                String create_time = line.getCreate_time();
                String id = line.getId();
                String goods_name = line.getGoods_name();
                String goods_intro = line.getGoods_intro();
                String price_list = line.getPrice_list();
                String uuids = line.getUuids();
                String vuids = line.getVuids();


                //json
                jsonObject.put("id", id);
                jsonObject.put("goods_name", goods_name);
                jsonObject.put("goods_intro", goods_intro);
                jsonObject.put("price_list", price_list);
                jsonObject.put("uuids", uuids);
                jsonObject.put("vuids", vuids);
                jsonObject.put("create_time", create_time);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getWorkingDetail(String openid, String date_time) {
        List<BookDetail> list= null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<BookDetail> bookDetails = dao.getBookDetailByMonth(openid,date_time.substring(0,7));
            Float month_hours = 0.0f;
            Float month_amount = 0.0f;
            for (int i = 0; i < bookDetails.size(); i++) {
                BookDetail bookDetail = bookDetails.get(i);
                Float hours = bookDetail.getHours();
                Float amount = bookDetail.getAmount();
                month_hours = month_hours + hours;
                month_amount = month_amount + amount;
            }
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("month_hours", month_hours);
            jsonObject1.put("month_amount", month_amount);
            resul_list.add(jsonObject1);

            list = dao.getWorkingDetail(openid,date_time);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                BookDetail line = list.get(i);
                //获取字段
                String type = line.getType();
                String item = line.getItem();
                String mark = line.getMark();
                Float amount = line.getAmount();
                String create_time = line.getCreate_time();
                String id = line.getId();
                Float hours = line.getHours();

                //json
                jsonObject.put("month_hours", month_hours);
                jsonObject.put("month_amount", month_amount);
                jsonObject.put("hours", hours);
                jsonObject.put("type", type);
                jsonObject.put("item", item);
                jsonObject.put("mark", mark);
                jsonObject.put("amount", amount);
                jsonObject.put("create_time", create_time);
                jsonObject.put("openid", openid);
                jsonObject.put("id", id);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getSpaceGoodsOrderByGoodsId(String goods_id) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<SpaceGoodsOrder> spaceGoodsOrders = dao.getSpaceGoodsOrderByGoodsId(goods_id);
            for (int i = 0; i < spaceGoodsOrders.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                SpaceGoodsOrder line = spaceGoodsOrders.get(i);
                //获取字段
                String create_time = line.getCreate_time();
                String id = line.getId();
                String openid = line.getOpenid();
                String leader_openid = line.getLeader_openid();
                List<BookUser> bookUsers = dao.getBookUser(leader_openid);
                BookUser bookUser = bookUsers.get(0);
                String leader_name = bookUser.getNick_name();
                String avatarurl = bookUser.getAvatarurl();

                String group_price = line.getGroup_price();
                String group_number = line.getGroup_number();
                // 团角色
                String role="团员";
                if(openid.equals(leader_openid)){
                    role = "团长";
                }
                // 判断是否满团
                int is_full = 0;
                List<SpaceGoodsOrder> leader_number = dao.getSpaceGoodsOrderByGoodsIdLeader(goods_id,leader_openid);
                if (leader_number.size() >= Integer.parseInt(group_number)){
                    is_full = 1;
                }


                //json
                jsonObject.put("leader_number", leader_number.size());
                jsonObject.put("leader_name", leader_name);
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("goods_id", goods_id);
                jsonObject.put("is_full", is_full);
                jsonObject.put("role", role);
                jsonObject.put("id", id);
                jsonObject.put("openid", openid);
                jsonObject.put("leader_openid", leader_openid);
                jsonObject.put("group_price", group_price);
                jsonObject.put("group_number", group_number);
                jsonObject.put("create_time", create_time);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getSpaceGoodsOrderByGoodsIdOpenid(String goods_id, String openid) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<SpaceGoodsOrder> spaceGoodsOrders = dao.getSpaceGoodsOrderByGoodsIdOpenid(goods_id,openid);
            for (int i = 0; i < spaceGoodsOrders.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                SpaceGoodsOrder line = spaceGoodsOrders.get(i);
                //获取字段
                String create_time = line.getCreate_time();
                String id = line.getId();
                String openid_get = line.getOpenid();
                String openid_qr = line.getOpenid_qr();
                String order_no = line.getOrder_no();

                String leader_openid = line.getLeader_openid();
                List<BookUser> bookUsers = dao.getBookUser(leader_openid);
                BookUser bookUser = bookUsers.get(0);
                String leader_name = bookUser.getNick_name();
                String avatarurl = bookUser.getAvatarurl();


                String group_price = line.getGroup_price();
                String group_number = line.getGroup_number();
                // 团角色
                String role="团员";
                if(openid.equals(leader_openid)){
                    role = "团长";
                }
                // 判断是否满团
                int is_full = 0;
                List<SpaceGoodsOrder> leader_number = dao.getSpaceGoodsOrderByGoodsIdLeader(goods_id,leader_openid);
                if (leader_number.size() >= Integer.parseInt(group_number)){
                    is_full = 1;
                }


                //json
                jsonObject.put("openid_qr", openid_qr);
                jsonObject.put("order_no", order_no);
                jsonObject.put("leader_number", leader_number.size());
                jsonObject.put("leader_name", leader_name);
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("goods_id", goods_id);
                jsonObject.put("is_full", is_full);
                jsonObject.put("role", role);
                jsonObject.put("id", id);
                jsonObject.put("openid", openid_get);
                jsonObject.put("leader_openid", leader_openid);
                jsonObject.put("group_price", group_price);
                jsonObject.put("group_number", group_number);
                jsonObject.put("create_time", create_time);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getSpaceGoodsOrderByOpenid(String openid) {
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<BookUser> bookUsers_me = dao.getBookUser(openid);
            BookUser bookUser_me = bookUsers_me.get(0);
            String my_role = bookUser_me.getRole();

            List<SpaceGoodsOrder> spaceGoodsOrders = dao.getSpaceGoodsOrderByOpenid(openid);
            if("boss".equals(my_role)){
                spaceGoodsOrders = dao.getSpaceGoodsOrderByOpenidQr(openid);
            }

            for (int i = 0; i < spaceGoodsOrders.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                SpaceGoodsOrder line = spaceGoodsOrders.get(i);
                //获取字段
                String create_time = line.getCreate_time();
                String id = line.getId();
                String openid_qr = line.getOpenid_qr();
                String order_no = line.getOrder_no();
                String openid_get = line.getOpenid();
                List<BookUser> bookUsers_follower = dao.getBookUser(openid_get);
                BookUser bookUser_follower = bookUsers_follower.get(0);
                String follower_name = bookUser_follower.getNick_name();
                String follower_phone = bookUser_follower.getPhone_number();


                String leader_openid = line.getLeader_openid();
                List<BookUser> bookUsers_leader = dao.getBookUser(leader_openid);
                BookUser bookUser_leader = bookUsers_leader.get(0);
                String leader_name = bookUser_leader.getNick_name();
                String avatarurl = bookUser_leader.getAvatarurl();

                String group_price = line.getGroup_price();
                String group_number = line.getGroup_number();
                // 团角色
                String role="团员";
                if(openid.equals(leader_openid)){
                    role = "团长";
                }

                String goods_id = line.getGoods_id();
                // 团信息
                List<SpaceGoodsList> spaceGoodsLists = dao.getSpaceGoodsListById(goods_id);
                SpaceGoodsList spaceGoodsList = spaceGoodsLists.get(0);
                String goods_name= spaceGoodsList.getGoods_name();
                // 判断是否满团
                int is_full = 0;
                List<SpaceGoodsOrder> leader_number = dao.getSpaceGoodsOrderByGoodsIdLeader(goods_id,leader_openid);
                if (leader_number.size() >= Integer.parseInt(group_number)){
                    is_full = 1;
                }
                Integer status = line.getStatus();

                //json
                jsonObject.put("status", status);
                jsonObject.put("follower_name", follower_name);
                jsonObject.put("follower_phone", follower_phone);
                jsonObject.put("goods_name", goods_name);
                jsonObject.put("openid_qr", openid_qr);
                jsonObject.put("order_no", order_no);
                jsonObject.put("leader_number", leader_number.size());
                jsonObject.put("leader_name", leader_name);
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("goods_id", goods_id);
                jsonObject.put("is_full", is_full);
                jsonObject.put("role", role);
                jsonObject.put("id", id);
                jsonObject.put("openid", openid_get);
                jsonObject.put("leader_openid", leader_openid);
                jsonObject.put("group_price", group_price);
                jsonObject.put("group_number", group_number);
                jsonObject.put("create_time", create_time);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }


}