package com.xue.service.Impl;

import java.beans.PropertyEditorSupport;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.xue.util.Imageutil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xue.entity.model.User;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;

import javax.annotation.Resource;

@Service
public class LoginServiceImpl implements LoginService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private UserMapper dao;

	@Override
	public int push(User user) {
		int result = 0;
		FileInputStream in = null;
		System.out.println(user);
		try {
			result = dao.insertUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List getDetails(Integer id) {
		byte[] photo = null;
		InputStream inputStream_photo = null;
		String comment = null;
		String student_name = null;
		String class_name = null;
		String class_target = null;
//		String id = null;
		List<JSONObject> resul_list = new ArrayList<>();

		try {
			List <User> list = dao.selectDetails(id);
			for(int i=0;i<list.size();i++){
				JSONObject jsonObject = new JSONObject();
				User line = list.get(i);
				//获取字段
				student_name = line.getStudent_name();
				class_name = line.getClass_name();
				comment = line.getComment();
				photo = line.getPhoto();
				class_target = line.getClass_target();
//				id = line.getId();
				//json
				jsonObject.put("student_name",student_name);
				jsonObject.put("class_name",class_name);
				jsonObject.put("comment",comment);
				jsonObject.put("photo",photo);
				jsonObject.put("class_target",class_target);
				jsonObject.put("id",id);
				resul_list.add(jsonObject);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resul_list;
	}

	@Override
	public List getSearch(String student_name) {
		byte[] photo = null;
		InputStream inputStream_photo = null;
		String comment = null;
		String class_name = null;
		String class_target = null;
		String id = null;
		String create_time = null;
		List<JSONObject> resul_list = new ArrayList<>();

		try {
			List <User> list = dao.selectSearch(student_name);
			for(int i=0;i<list.size();i++){
				JSONObject jsonObject = new JSONObject();
				User line = list.get(i);
				//获取字段
				student_name = line.getStudent_name();
				class_name = line.getClass_name();
				comment = line.getComment();
				photo = line.getPhoto();
				class_target = line.getClass_target();
				id = line.getId();
				create_time=line.getCreate_time();

				//json
				jsonObject.put("student_name",student_name);
				jsonObject.put("class_name",class_name);
				jsonObject.put("comment",comment);
				jsonObject.put("photo",photo);
				jsonObject.put("class_target",class_target);
				jsonObject.put("id",id);
				jsonObject.put("create_time",create_time);
				resul_list.add(jsonObject);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resul_list;
	}


	@Override
	public List getMessage() {
		byte[] photo = null;
		InputStream inputStream_photo = null;
		String comment = null;
		String student_name = null;
		String class_name = null;
		String class_target = null;
		String id = null;
		String create_time = null;
		List<JSONObject> resul_list = new ArrayList<>();

		try {
			List <User> list = dao.selectUser();
			for(int i=0;i<list.size();i++){
				JSONObject jsonObject = new JSONObject();
				User line = list.get(i);
				//获取字段
				student_name = line.getStudent_name();
				class_name = line.getClass_name();
				comment = line.getComment();
				photo = line.getPhoto();
				class_target = line.getClass_target();
				id = line.getId();
				create_time= line.getCreate_time();
				//json
				jsonObject.put("student_name",student_name);
				jsonObject.put("class_name",class_name);
				jsonObject.put("comment",comment);
				jsonObject.put("photo",photo);
				jsonObject.put("class_target",class_target);
				jsonObject.put("id",id);
				jsonObject.put("create_time",create_time);
				resul_list.add(jsonObject);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resul_list;
	}


}
