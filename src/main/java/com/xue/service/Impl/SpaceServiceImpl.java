package com.xue.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.xue.config.Constants;
import com.xue.entity.model.BookUser;
import com.xue.entity.model.Merchant;
import com.xue.entity.model.SpaceTeacher;
import com.xue.entity.model.User;
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
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");//设置日期格式
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
                String book_name = line.getBook_name();
                openid = line.getOpenid();
                String logo = line.getLogo();
                int id = line.getId();
                String location = line.getLocation();

                String openid_qr = line.getOpenid_qr();
                if(!"boss".equals(role)){
                    List<BookUser> bookUsers = dao.getBookUser(openid_qr);
                    BookUser bookUser = bookUsers.get(0);
                    logo = bookUser.getLogo();
                    nick_name = bookUser.getNick_name();
                    location = bookUser.getLocation();
                    book_name = bookUser.getBook_name();
                }


                //json
                jsonObject.put("id", id);
                jsonObject.put("role", role);
                jsonObject.put("location", location);
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("create_time", create_time);
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("openid",openid);
                jsonObject.put("logo",logo);
                jsonObject.put("book_name",book_name);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }


}