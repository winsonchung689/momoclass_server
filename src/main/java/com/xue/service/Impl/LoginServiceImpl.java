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
	public List getPhoto() {
		String path = "D:/";
		byte[] photo = null;
		InputStream inputStream_photo = null;
		String comment = null;
		String student_name = null;
		String class_name = null;
		JSONObject jsonObject = new JSONObject();
		List<JSONObject> resul_list = new ArrayList<>();

		try {

			List <User> list = dao.selectUser();
			System.out.printf("list:" + list);
			for(int i=0;i<list.size();i++){
				User line = list.get(i);
				//获取字段
				student_name = line.getStudent_name();
				class_name = line.getClass_name();
				comment = line.getComment();

				//图片获取
				photo = line.getPhoto();
				String photo_path = path+photo+".png";
				inputStream_photo = new ByteArrayInputStream(photo);
				Imageutil.readBin2Image(inputStream_photo, photo_path);

				//json
				jsonObject.put("student_name",student_name);
				jsonObject.put("class_name",class_name);
				jsonObject.put("comment",comment);
				jsonObject.put("photo_path",photo_path);
				resul_list.add(jsonObject);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resul_list;
	}


}
