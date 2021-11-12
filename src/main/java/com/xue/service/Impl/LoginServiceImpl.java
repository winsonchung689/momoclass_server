package com.xue.service.Impl;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.xue.entity.model.Schedule;
import com.xue.entity.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xue.entity.model.Message;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;

@Service
public class LoginServiceImpl implements LoginService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private UserMapper dao;

	@Override
	public int push(Message message) {
		int result = 0;
		FileInputStream in = null;
		System.out.println(message);
		try {
			result = dao.push(message);
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
			List <Message> list = dao.getDetails(id);
			for(int i=0;i<list.size();i++){
				JSONObject jsonObject = new JSONObject();
				Message line = list.get(i);
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
			List <Message> list = dao.getSearch(student_name);
			for(int i=0;i<list.size();i++){
				JSONObject jsonObject = new JSONObject();
				Message line = list.get(i);
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
	public int insertSchedule(Schedule schedule) {
		int result = 0;
		FileInputStream in = null;
		System.out.println(schedule);
		try {
			result = dao.insertSchedule(schedule);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List getSchedule(String date_time) {
		String add_date = null;
		String age = null;
		String student_name = null;
		String duration = null;
		String create_time = null;
		String id = null;
		List<JSONObject> resul_list = new ArrayList<>();

		try {
			List <Schedule> list = dao.getSchedule(date_time);
			for(int i=0;i<list.size();i++){
				JSONObject jsonObject = new JSONObject();
				Schedule line = list.get(i);
				//获取字段
				add_date = line.getAdd_date();
				age = line.getAge();
				student_name = line.getStudent_name();
				duration = line.getDuration();
				id = line.getId();
				create_time= line.getCreate_time();
				//json
				jsonObject.put("add_date",add_date);
				jsonObject.put("age",age);
				jsonObject.put("student_name",student_name);
				jsonObject.put("duration",duration);
				jsonObject.put("create_time",create_time);
				jsonObject.put("id",id);
				resul_list.add(jsonObject);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resul_list;
	}

	@Override
	public int deleteComment(Integer id) {
		try {
			dao.deleteComment(id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@Override
	public int deleteSchedule(Integer id) {
		try {
			dao.deleteSchedule(id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@Override
	public int insertUser(User user) {
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
	public List getUser(String openid) {
		String role = null;
		String student_name = null;
		String avatarurl = null;
		List<JSONObject> resul_list = new ArrayList<>();
		try {

			List <User> list = dao.getUser(openid);
			for(int i=0;i<list.size();i++){
				JSONObject jsonObject = new JSONObject();
				User line = list.get(i);
				//获取字段
				role = line.getRole();
				student_name = line.getStudent_name();
				avatarurl = line.getAvatarurl();
				//json
				jsonObject.put("role",role);
				jsonObject.put("student_name",student_name);
				jsonObject.put("avatarurl",avatarurl);
				resul_list.add(jsonObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resul_list;
	}

	@Override
	public List getAdvertise() {
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
			List <Message> list = dao.getAdvertise();
			for(int i=0;i<list.size();i++){
				JSONObject jsonObject = new JSONObject();
				Message line = list.get(i);
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
			List <Message> list = dao.getMessage();
			for(int i=0;i<list.size();i++){
				JSONObject jsonObject = new JSONObject();
				Message line = list.get(i);
				//获取字段
				student_name = line.getStudent_name();
				class_name = line.getClass_name();
				comment = line.getComment();
//				photo = line.getPhoto();
				class_target = line.getClass_target();
				id = line.getId();
				create_time= line.getCreate_time();
				//json
				jsonObject.put("student_name",student_name);
				jsonObject.put("class_name",class_name);
				jsonObject.put("comment",comment);
//				jsonObject.put("photo",photo);
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
	public List getMessageClient(String student_name) {
		byte[] photo = null;
		InputStream inputStream_photo = null;
		String comment = null;
		String class_name = null;
		String class_target = null;
		String id = null;
		String create_time = null;
		List<JSONObject> resul_list = new ArrayList<>();

		try {
			List <Message> list = dao.getMessageClient(student_name);
			for(int i=0;i<list.size();i++){
				JSONObject jsonObject = new JSONObject();
				Message line = list.get(i);
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

	@Override
	public List getModel() {
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
			List <Message> list = dao.getModel();
			for(int i=0;i<list.size();i++){
				JSONObject jsonObject = new JSONObject();
				Message line = list.get(i);
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
