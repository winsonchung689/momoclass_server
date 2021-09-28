package com.xue.controller;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import javax.swing.plaf.IconUIResource;

import com.alibaba.fastjson.JSON;
import com.sun.xml.internal.messaging.saaj.packaging.mime.MultipartDataSource;
import com.xue.util.Imageutil;
import org.hibernate.engine.jdbc.BinaryStream;
import org.hibernate.event.spi.SaveOrUpdateEvent;
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

	//	获取图片
	@RequestMapping("/getphoto")
	@ResponseBody
	public String getPhoto(String targePath){
		byte[] photo = null;
		try {
			loginService.getPhoto();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "get photo successfully";
	}


	//	推送
	@RequestMapping("/push_photo")
	@ResponseBody
	public String push_photo(HttpServletRequest request, HttpServletResponse response){

		//获取图片
		MultipartHttpServletRequest req = (MultipartHttpServletRequest)request;
		MultipartFile multipartFile = req.getFile("photo");

		//获取类路径
		File path = new File(this.getClass().getResource("/").getPath());
		UUID uuid = UUID.randomUUID();
		String p_path = path +"/"+ uuid + ".png";
		System.out.printf("p:" + p_path);

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
		System.out.printf("photo:" + photo + "\n");
		//获取文字
		String comment = request.getParameter("comment");
		System.out.printf("comment:" + comment + "\n");
		//获取学生名字
		String student_name = request.getParameter("student_name");
		System.out.printf("student_name:" + student_name + "\n");
		//获取课堂名称
		String class_name = request.getParameter("class_name");
		System.out.printf("class_name:" + class_name + "\n");

		FileInputStream in = null;
		try {
			User user =new User();
			in = Imageutil.readImage(photo);
			user.setPhoto(FileCopyUtils.copyToByteArray(in));
			user.setComment(comment);
			user.setStudent_name(student_name);
			user.setCreate_time(create_time);
			user.setClass_name(class_name);
			loginService.push(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push photo successfully";
	}

}
	
	


