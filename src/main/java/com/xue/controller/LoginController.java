package com.xue.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.swing.plaf.IconUIResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
	public String getPhoto(){
		String photo = loginService.getPhoto();
		return photo;

	}

	//	获取评论
	@RequestMapping("/getcomment")
	@ResponseBody
	public String getComment(){
		System.out.printf("start get comment");
		String comment = loginService.getMessage();
		System.out.printf("res:" + comment);
		return comment;

	}

	//	推送
	@RequestMapping("/push")
	@ResponseBody
	public String push(String student_name,String photo,String comment,String create_time ){
		System.out.printf("start push comment" + comment+student_name+photo+create_time);
		try {
			User user =new User();
			user.setComment(comment);
			user.setPhoto(photo);
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
	
	


