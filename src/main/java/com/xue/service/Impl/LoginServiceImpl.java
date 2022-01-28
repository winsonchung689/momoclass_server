package com.xue.service.Impl;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import antlr.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.xue.entity.model.Lesson;
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
	public int insertLesson(Lesson lesson) {
		int result = 0;
		System.out.println(lesson);
		try {
			result = dao.insertLesson(lesson);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public int updateLesson(Lesson lesson) {
		int result = 0;
		try {
			String student_name = lesson.getStudent_name();
			Integer total = lesson.getTotal_amount();
			Integer left = lesson.getLeft_amount();
			Integer total_amount = 0;
			Integer left_amount = 0;
			if(student_name!=null) {
				List<Lesson> lessons = dao.getLessonByName(student_name);
				Lesson lesson_get = lessons.get(0);
				total_amount = lesson_get.getTotal_amount();
				if(total > 0){
					total_amount =total;
				}
				left_amount = lesson_get.getLeft_amount();
				if(left > 0){
					left_amount = left;
				}
			}
			lesson.setStudent_name(student_name);
			lesson.setTotal_amount(total_amount);
			lesson.setLeft_amount(left_amount);
			result = dao.updateLesson(lesson);
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
		String comment = null;
		String class_name = null;
		String class_target = null;
		String id = null;
		String create_time = null;
		List<JSONObject> resul_list = new ArrayList<>();

		try {
			List <Message> list = dao.getSearch(student_name);
			for(int i=0;i<list.size();i++){
				Integer percent = 0;
				JSONObject jsonObject = new JSONObject();
				Message line = list.get(i);
				//获取字段
				student_name = line.getStudent_name();
				class_name = line.getClass_name();
				comment = line.getComment();
				class_target = line.getClass_target();
				id = line.getId();
				create_time=line.getCreate_time();

				try {
					List<Lesson> lessons = dao.getLessonByName(student_name);
					Lesson lesson = lessons.get(0);
					Integer left = lesson.getLeft_amount();
					Integer total = lesson.getTotal_amount();
					if(left>0 || total>0){
						percent = left*100/total;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}


				//json
				jsonObject.put("student_name",student_name);
				jsonObject.put("class_name",class_name);
				jsonObject.put("comment",comment);
				jsonObject.put("class_target",class_target);
				jsonObject.put("id",id);
				jsonObject.put("create_time",create_time);
				jsonObject.put("percent",percent);
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
		String nick_name = null;
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
				nick_name = line.getNick_name();
				//json
				jsonObject.put("role",role);
				jsonObject.put("student_name",student_name);
				jsonObject.put("avatarurl",avatarurl);
				jsonObject.put("nick_name",nick_name);
				resul_list.add(jsonObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resul_list;
	}

	@Override
	public List getOpenidByNick(String student_name) {
		String openid = null;
		List<JSONObject> resul_list = new ArrayList<>();
		try {

			List <User> list = dao.getOpenidByNick(student_name);
			for(int i=0;i<list.size();i++){
				JSONObject jsonObject = new JSONObject();
				User line = list.get(i);
				//获取字段
				openid = line.getOpenid();
				//json
				jsonObject.put("openid",openid);
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
	public List getClassSys() {
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
			List <Message> list = dao.getClassSys();
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
	public List getHome() {
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
			List <Message> list = dao.getHome();
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
	public int updateMinusLesson(String student_name) {
		int result = 0;
		Integer total_amount = 0;
		Integer left_amount = 0;
		Integer new_left = 0;
		System.out.println(student_name);

		List <Lesson> list = dao.getLessonByName(student_name);
		try {
			for(int i=0;i<list.size();i++){
				Lesson line = list.get(i);
				total_amount = line.getTotal_amount();
				left_amount = line.getLeft_amount();
				new_left = left_amount -1;
				Lesson lesson =new Lesson();
				lesson.setStudent_name(student_name);
				lesson.setLeft_amount(new_left);
				lesson.setTotal_amount(total_amount);
				result = dao.updateLesson(lesson);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public int updateAddPoints(String student_name) {
		int result = 0;
		Integer points = 0;
		Integer new_points = 0;
		System.out.println(student_name);

		List <Lesson> list = dao.getLessonByName(student_name);
		try {
			for(int i=0;i<list.size();i++){
				Lesson line = list.get(i);
				points = line.getPoints();
				if(points==null){
					points =0;
				}
				new_points = points + 1;
				Lesson lesson =new Lesson();
				lesson.setStudent_name(student_name);
				lesson.setPoints(new_points);
				result = dao.updateLessonPoint(lesson);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}


	@Override
	public int deletePoints(String student_name) {
		int result = 0;
		Integer points = 0;
		System.out.println(student_name);
		try {
				Lesson lesson =new Lesson();
				lesson.setStudent_name(student_name);
				lesson.setPoints(points);
				result = dao.updateLessonPoint(lesson);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}


	@Override
	public List getLessonByName(String student_name) {
		Integer total_amount = 0;
		Integer left_amount = 0;
		List<JSONObject> resul_list = new ArrayList<>();
		try {

			List <Lesson> list = dao.getLessonByName(student_name);
			for(int i=0;i<list.size();i++){
				JSONObject jsonObject = new JSONObject();
				Lesson line = list.get(i);
				//获取字段
				total_amount = line.getTotal_amount();
				left_amount = line.getLeft_amount();
				//json
				jsonObject.put("total_amount",total_amount);
				jsonObject.put("left_amount",left_amount);
				resul_list.add(jsonObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resul_list;
	}


	@Override
	public List getMessage() {
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
				Integer percent = 0;
				JSONObject jsonObject = new JSONObject();
				Message line = list.get(i);
				//获取字段
				student_name = line.getStudent_name();
				class_name = line.getClass_name();
				comment = line.getComment();
				class_target = line.getClass_target();
				id = line.getId();
				create_time= line.getCreate_time();

				try {
					List<Lesson> lessons = dao.getLessonByName(student_name);
					Lesson lesson = lessons.get(0);
					Integer left = lesson.getLeft_amount();
					Integer total = lesson.getTotal_amount();
					if(left>0 || total>0){
						percent = left*100/total;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				//json
				jsonObject.put("student_name",student_name);
				jsonObject.put("class_name",class_name);
				jsonObject.put("comment",comment);
				jsonObject.put("percent",percent);
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

	@Override
	public List getLesson() {
		String student_name = null;
		Integer total_amount = null;
		Integer left_amount = null;
		String create_time = null;
		String id = null;
		Integer points = null;
		double percent = 0;
		List<JSONObject> resul_list = new ArrayList<>();

		try {
			List <Lesson> list = dao.getLesson();
			for(int i=0;i<list.size();i++){
				JSONObject jsonObject = new JSONObject();
				Lesson line = list.get(i);
				//获取字段
				student_name = line.getStudent_name();
				total_amount = line.getTotal_amount();
				left_amount = line.getLeft_amount();
				percent = left_amount*100/total_amount;
				id = line.getId();
				create_time= line.getCreate_time();
				points = line.getPoints();
				//json
				jsonObject.put("student_name",student_name);
				jsonObject.put("total_amount",total_amount);
				jsonObject.put("left_amount",left_amount);
				jsonObject.put("id",id);
				jsonObject.put("create_time",create_time);
				jsonObject.put("percent",percent);
				jsonObject.put("points",points);
				jsonObject.put("rank",i+1);
				resul_list.add(jsonObject);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resul_list;
	}


}
