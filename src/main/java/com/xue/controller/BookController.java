package com.xue.controller;

import com.xue.entity.model.*;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class BookController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LoginService loginService;

	@Autowired
	private UserMapper dao;


	@RequestMapping("/insertBookUser")
	@ResponseBody
	public String insertBookUser(HttpServletRequest request, HttpServletResponse response){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		try {
			cal.setTime(df.parse(create_time));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		cal.add(cal.DATE,30);
		String expired_time = df.format(cal.getTime());

		//获取用户名
		String nick_name = request.getParameter("nick_name");

		//获年角色
		String role = request.getParameter("role");
		if(role == null || role.isEmpty() || "undefined".equals(role)){
			role = "client";
		}
		//获取 openid
		String openid = request.getParameter("openid");

		//获取 avatarurl
		String avatarurl = request.getParameter("avatarurl");
		if(avatarurl == null || avatarurl.isEmpty() || "undefined".equals(avatarurl)){
			avatarurl = "https://thirdwx.qlogo.cn/mmopen/vi_32/y667SLJ40Eic5fMnHdibjO4vLG7dmqgjeuwjQbRN5ZJj6uZfl06yA7P9wwl7oYjNRFzBzwcheZtK8zvkibyfamfBA/132";
		}

		BookUser bookUser =new BookUser();
		bookUser.setNick_name(nick_name);
		bookUser.setRole(role);
		bookUser.setOpenid(openid);
		bookUser.setAvatarurl(avatarurl);
		bookUser.setCreate_time(create_time);
		bookUser.setExpired_time(expired_time);

		loginService.insertBookUser(bookUser);
		try {
			List<BookUser> bookUsers = dao.getBookUser(openid);
			if(bookUsers.size()>0){
				BookUser bookUser1 = bookUsers.get(0);
				int id = bookUser1.getId();
				if(id<100){
					BookUser bookUser2 = new BookUser();
					bookUser2.setRole("boss");
					bookUser2.setOpenid(openid);
					dao.updateBookRole(bookUser2);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return "push massage successfully";
	}

	@RequestMapping("/getBookUser")
	@ResponseBody
	public List getBookUser(String openid){
		List list = null;
		try {
			list = loginService.getBookUser(openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getBBookDetail")
	@ResponseBody
	public List getBBookDetail(String openid){
		List list = null;
		try {
			list = loginService.getBBookDetail(openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/updateBookUser")
	@ResponseBody
	public int updateBookUser(HttpServletRequest request, HttpServletResponse response){

		//获取文字
		String content = request.getParameter("content");

		String openid = request.getParameter("openid");

		String type = request.getParameter("type");

		try {
			if("avatarurl".equals(type)){
				BookUser bookUser =new BookUser();
				bookUser.setAvatarurl(content);
				bookUser.setOpenid(openid);
				dao.updateBookAvatar(bookUser);
			}else if("nickName".equals(type)){
				BookUser bookUser =new BookUser();
				bookUser.setNick_name(content);
				bookUser.setOpenid(openid);
				dao.updateBookNickName(bookUser);
			}else if("logo".equals(type)){
				BookUser bookUser =new BookUser();
				bookUser.setLogo(content);
				bookUser.setOpenid(openid);
				dao.updateBookLogo(bookUser);
			}else if("role".equals(type)){
				BookUser bookUser =new BookUser();
				if("boss".equals(content)){
					content = "client";
				}else {
					content = "boss";
				}
				bookUser.setRole(content);
				bookUser.setOpenid(openid);
				dao.updateBookRole(bookUser);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 1;
	}

	@RequestMapping("/insertBookDetail")
	@ResponseBody
	public String insertBookDetail(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String update_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String mark = request.getParameter("mark");
		String type = request.getParameter("type");
		String item = request.getParameter("item");
		String amount = request.getParameter("amount");
		String openid = request.getParameter("openid");
		String create_time = request.getParameter("create_time");

		BookDetail bookDetail =new BookDetail();
		bookDetail.setType(type);
		bookDetail.setMark(mark);
		bookDetail.setAmount(Float.parseFloat(amount));
		bookDetail.setCreate_time(create_time);
		bookDetail.setItem(item);
		bookDetail.setOpenid(openid);
		bookDetail.setUpdate_time(update_time);

		try {
			dao.insertBookDetail(bookDetail);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return "push massage successfully";
	}

}
	
	


