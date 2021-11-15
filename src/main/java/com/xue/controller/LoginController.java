package com.xue.controller;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xue.entity.model.Schedule;
import com.xue.entity.model.User;
import com.xue.util.HttpUtil;
import com.xue.util.Imageutil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.xue.entity.model.Message;
import com.xue.service.LoginService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
public class LoginController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private LoginService loginService;

	//	获取Openid
	@RequestMapping("/getOpenid")
	@ResponseBody
	public String getOpenid(String code){
		String result = null;
		String openid = null;
		String url = "https://api.weixin.qq.com/sns/jscode2session";
		String param="appid=wx3f5dc09cc495429b&secret=ac693c65ae57020643224561ac102dce&js_code="+ code +"&grant_type=authorization_code";
		try {
			result = HttpUtil.sendPost(url	,param);
			JSONObject jsonObject = JSON.parseObject(result);
			openid = jsonObject.getString("openid");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return openid;
	}

	//	获取Openid
	@RequestMapping("/getToken")
	@ResponseBody
	public String getToken(){
		String result = null;
		String token = null;
		String url = "https://api.weixin.qq.com/cgi-bin/token";
		String param="appid=wx3f5dc09cc495429b&secret=ac693c65ae57020643224561ac102dce&grant_type=client_credential";
		try {
			result = HttpUtil.sendPost(url	,param);
			JSONObject jsonObject = JSON.parseObject(result);
			token = jsonObject.getString("access_token");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return token;
	}

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

	//	获取广告
	@RequestMapping("/getAdvertise")
	@ResponseBody
	public List getAdvertise(){
		List list = null;
		try {
			list = loginService.getAdvertise();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}


	//	获取全部
	@RequestMapping("/getMessageClient")
	@ResponseBody
	public List getMessageClient(String student_name){
		List list = null;
		try {
			list = loginService.getMessageClient(student_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取课程表
	@RequestMapping("/getSchedule")
	@ResponseBody
	public List getSchedule(String date_time){
		List list = null;
		try {
			list = loginService.getSchedule(date_time);
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

	//	获取用户
	@RequestMapping("/getOpenidByNick")
	@ResponseBody
	public List getOpenidByNick(String nick_name){
		List list = null;
		try {
			list = loginService.getOpenidByNick(nick_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取用户
	@RequestMapping("/getUser")
	@ResponseBody
	public List getUser(String openid){
		List list = null;
		try {
			list = loginService.getUser(openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}


	//	获取详情页
	@RequestMapping("/deleteComment")
	@ResponseBody
	public int deleteComment(Integer id){
		try {
			loginService.deleteComment(id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@RequestMapping("/deleteSchedule")
	@ResponseBody
	public int deleteSchedule(Integer id){
		try {
			loginService.deleteSchedule(id);
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
			Message message =new Message();
			in = Imageutil.readImage(photo);
			message.setPhoto(FileCopyUtils.copyToByteArray(in));
			message.setComment(comment);
			message.setStudent_name(student_name);
			message.setCreate_time(create_time);
			message.setClass_name(class_name);
			message.setClass_target(class_target);
			loginService.push(message);
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


	@RequestMapping("/insertUser")
	@ResponseBody
	public String insertUser(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		//获取用户名
		String nick_name = request.getParameter("nick_name");
		//获取学生名
		String student_name = request.getParameter("student_name");
		//获年角色
		String role = request.getParameter("role");
        //获取openid
		String openid = request.getParameter("openid");
		//获取openid
		String avatarurl = request.getParameter("avatarurl");


		try {
			User user =new User();
			user.setNick_name(nick_name);
			user.setStudent_name(student_name);
			user.setRole(role);
			user.setOpenid(openid);
			user.setCreate_time(create_time);
			user.setAvatarurl(avatarurl);
			loginService.insertUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

}
	
	


