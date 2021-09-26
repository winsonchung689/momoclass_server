package com.xue.controller;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.swing.plaf.IconUIResource;

import com.xue.util.Imageutil;
import org.hibernate.engine.jdbc.BinaryStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xue.entity.model.User;
import com.xue.service.LoginService;

@Controller
public class LoginController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private LoginService loginService;

	//	获取图片
	@RequestMapping("/getphoto")
	@ResponseBody
	public byte[] getPhoto(String targePath){
		byte[] photo = null;
		try {
			System.out.printf("get photo");
			loginService.getPhoto(targePath);
			System.out.printf("save photo");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return photo;

	}

	//	获取评论
	@RequestMapping("/getcomment")
	@ResponseBody
	public String getComment(){
		String comment = null;
		try {
			System.out.printf("start get comment");
			comment = loginService.getMessage();
			System.out.printf("res:" + comment);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return comment;

	}

	//	推送
	@RequestMapping("/push")
	@ResponseBody
	public String push(String student_name,String photo_path,String comment){
		System.out.printf("start push comment" + comment+student_name+photo_path);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		FileInputStream in = null;
		try {
			User user =new User();
			in = Imageutil.readImage(photo_path);
			System.out.printf("winson: "+ in);
			user.setPhoto(FileCopyUtils.copyToByteArray(in));
			user.setComment(comment);
			user.setStudent_name(student_name);
			user.setCreate_time(create_time);
			loginService.push(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push the photo from data database!";

	}

	//	推送评论
	@RequestMapping("/pushcomment")
	@ResponseBody
	public String pushComment(){
		return "push the comment from data database!";

	}

		
	}
	
	


