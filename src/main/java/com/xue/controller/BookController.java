package com.xue.controller;

import com.xue.entity.model.*;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;
import com.xue.service.SpaceService;
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
	private SpaceService spaceService;

	@Autowired
	private UserMapper dao;


	@RequestMapping("/insertBookUser")
	@ResponseBody
	public String insertBookUser(HttpServletRequest request, HttpServletResponse response){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		//获取 openid
		String openid = request.getParameter("openid");

		BookUser bookUser =new BookUser();
		bookUser.setNick_name("微信用户");
		bookUser.setRole("client");
		bookUser.setOpenid(openid);
		bookUser.setAvatarurl("525addcc-03e8-427f-944a-ac4ff38383b3.png");
		bookUser.setCreate_time(create_time);
		bookUser.setExpired_time(create_time);

		loginService.insertBookUser(bookUser);

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
	public List getBBookDetail(String openid,String duration,String book_name){
		List list = null;
		try {
			list = loginService.getBBookDetail(openid,duration,book_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getBookDetailByMonth")
	@ResponseBody
	public List getBookDetailByMonth(String openid,String book_name,String date_time){
		List list = null;
		try {
			list = loginService.getBookDetailByMonth(openid,book_name,date_time);
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
		String id = request.getParameter("id");
		String type = request.getParameter("type");

		try {
			List<BookUser> bookUsers = dao.getBookUserById(id);
			BookUser bookUser = bookUsers.get(0);

			if("avatarurl".equals(type)){
				bookUser.setAvatarurl(content);
			}else if("nick_name".equals(type)){
				bookUser.setNick_name(content);
			}else if("logo".equals(type)){
				bookUser.setLogo(content);
			}else if("role".equals(type)){
				String role = "boss";
				if("boss".equals(content)){
					role = "client";
				}
				bookUser.setRole(role);
			}else if("book_name".equals(type)){
				bookUser.setBook_name(content);
			}else if("location".equals(type)){
				bookUser.setLocation(content);
			}

			dao.updateBookUserDetail(bookUser);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 1;
	}

	@RequestMapping("/updateSpaceTeacher")
	@ResponseBody
	public int updateSpaceTeacher(HttpServletRequest request, HttpServletResponse response){
		//获取文字
		String openid = request.getParameter("openid");
		String type = request.getParameter("type");
		String content = request.getParameter("content");

		try {
			List<SpaceTeacher> spaceTeachers = dao.getSpaceTeacher(openid);
			SpaceTeacher spaceTeacher = spaceTeachers.get(0);

			if("intro".equals(type)){
				spaceTeacher.setIntro(content);
			}else if("uuids".equals(type)){
				spaceTeacher.setUuids(content);
			}

			dao.updateSpaceTeacher(spaceTeacher);
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
		String book_name = request.getParameter("book_name");

		BookDetail bookDetail =new BookDetail();
		bookDetail.setType(type);
		bookDetail.setMark(mark);
		bookDetail.setAmount(Float.parseFloat(amount));
		bookDetail.setCreate_time(create_time);
		bookDetail.setItem(item);
		bookDetail.setOpenid(openid);
		bookDetail.setUpdate_time(update_time);
		bookDetail.setBook_name(book_name);

		try {
			dao.insertBookDetail(bookDetail);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return "push massage successfully";
	}

	@RequestMapping("/deleteBBookeDetail")
	@ResponseBody
	public int deleteBBookeDetail(Integer id){
		try {
			dao.deleteBBookeDetail(id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@RequestMapping("/insertSpaceTeacher")
	@ResponseBody
	public String insertSpaceTeacher(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String openid = request.getParameter("openid");
		String uuids = request.getParameter("uuids");
		String intro = request.getParameter("intro");

		SpaceTeacher spaceTeacher =new SpaceTeacher();
		spaceTeacher.setOpenid(openid);
		spaceTeacher.setUuids(uuids);
		spaceTeacher.setIntro(intro);
		spaceTeacher.setCreate_time(create_time);


		try {
			dao.insertSpaceTeacher(spaceTeacher);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return "push massage successfully";
	}

	@RequestMapping("/getBookUser")
	@ResponseBody
	public List getSpaceTeacher(String openid){
		List list = null;
		try {
			list = spaceService.getSpaceTeacher(openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}


}
	
	


