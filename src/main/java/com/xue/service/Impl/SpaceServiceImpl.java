package com.xue.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.xue.config.Constants;
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



}