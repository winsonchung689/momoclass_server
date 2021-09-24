package com.xue.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
		String comment = loginService.getMessage();
		return comment;

	}

	//	推送
	@RequestMapping("/push")
	@ResponseBody
	public String push(User user){
		loginService.push(user);
		return "push the photo from data database!";

	}

	//	推送评论
	@RequestMapping("/pushcomment")
	@ResponseBody
	public String pushComment(){
		return "push the comment from data database!";

	}

		
	}
	
	


