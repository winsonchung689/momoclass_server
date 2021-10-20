package com.xue.controller;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xue.entity.model.Schedule;
import com.xue.util.Imageutil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.xue.entity.model.User;
import com.xue.service.LoginService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.alibaba.fastjson.JSONObject;

@Controller
public class LoginController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private LoginService loginService;

	//	获取全部
	@RequestMapping("/getMessage")
	@ResponseBody
	public List getMessage(){
		List list = null;
		try {
			 list = loginService.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取课程表
	@RequestMapping("/getSchedule")
	@ResponseBody
	public List getSchedule(){
		List list = null;
		try {
			list = loginService.getSchedule();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取范画
	@RequestMapping("/getModel")
	@ResponseBody
	public List getModel(){
		List list = null;
		try {
			list = loginService.getModel();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/getSearch")
	@ResponseBody
	public List getSearch(String student_name){
		List list = null;
		try {
			list = loginService.getSearch(student_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取详情页
	@RequestMapping("/getDetails")
	@ResponseBody
	public List getDetails(Integer id){
		List list = null;
		try {
			list = loginService.getDetails(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取详情页
	@RequestMapping("/deleteUser")
	@ResponseBody
	public int deletePost(Integer id){
		try {
			loginService.deleteUser(id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	//	推送图片
	@RequestMapping("/push_photo")
	@ResponseBody
	public String push_photo(HttpServletRequest request, HttpServletResponse response){

		//获取图片
		MultipartHttpServletRequest req = (MultipartHttpServletRequest)request;
		MultipartFile multipartFile = req.getFile("photo");

		//获取类路径
		String path = System.getProperty("user.dir");
		UUID uuid = UUID.randomUUID();
		String p_path = path +"/uploadimages/"+ uuid + ".png";

		//保存图片
		try {
			multipartFile.transferTo(new File(p_path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p_path;
	}

	//	推送
	@RequestMapping("/push")
	@ResponseBody
	public String push(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		//获取图片路径
		String photo = request.getParameter("photo");
		//获取文字
		String comment = request.getParameter("comment");
		//获取学生名字
		String student_name = request.getParameter("student_name");
		//获取课堂名称
		String class_name = request.getParameter("class_name");
		//获取课堂目标
		String class_target = request.getParameter("class_target");

		FileInputStream in = null;
		try {
			User user =new User();
			in = Imageutil.readImage(photo);
			user.setPhoto(FileCopyUtils.copyToByteArray(in));
			user.setComment(comment);
			user.setStudent_name(student_name);
			user.setCreate_time(create_time);
			user.setClass_name(class_name);
			user.setClass_target(class_target);
			loginService.push(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}


	//	推送
	@RequestMapping("/insertShedule")
	@ResponseBody
	public String insertShedule(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		//获取日期
		String add_date = request.getParameter("add_date");
		//获年龄段
		String age = request.getParameter("age");
		//获取名字
		String student_name = request.getParameter("student_name");
		//获取时间段
		String duration = request.getParameter("duration");

		try {
			Schedule schedule =new Schedule();
			schedule.setAdd_date(add_date);
			schedule.setAge(age);
			schedule.setStudent_name(student_name);
			schedule.setDuration(duration);
			schedule.setCreate_time(create_time);
			loginService.insertSchedule(schedule);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

}
	
	


