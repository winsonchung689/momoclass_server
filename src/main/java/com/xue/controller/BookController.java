package com.xue.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xue.entity.model.*;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;
import com.xue.service.SpaceService;
import com.xue.util.HttpUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
		bookUser.setOpenid_qr(openid);

		dao.insertBookUser(bookUser);

		return "push massage successfully";
	}

	@RequestMapping("/getBookUser")
	@ResponseBody
	public List getBookUser(String openid){
		List list = null;
		try {
			list = spaceService.getBookUser(openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getBookUserByOpenidQr")
	@ResponseBody
	public List getBookUserByOpenidQr(String openid){
		List list = null;
		try {
			list = dao.getBookUserByOpenidQr(openid);
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
			}else if("phone_number".equals(type)){
				bookUser.setPhone_number(content);
			}else if("student_name".equals(type)){
				bookUser.setStudent_name(content);
			}else if("expired_time".equals(type)){
				bookUser.setExpired_time(content);
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

	@RequestMapping("/updateSpaceCases")
	@ResponseBody
	public int updateSpaceCases(HttpServletRequest request, HttpServletResponse response){
		//获取文字
		String id = request.getParameter("id");
		String type = request.getParameter("type");
		String content = request.getParameter("content");

		try {
			List<SpaceCases> spaceCasess = dao.getSpaceCasesById(id);
			SpaceCases SpaceCases = spaceCasess.get(0);

			if("intro".equals(type)){
				SpaceCases.setIntro(content);
			}else if("uuids".equals(type)){
				SpaceCases.setUuids(content);
			}else if("student_name".equals(type)){
				SpaceCases.setStudent_name(content);
			}else if("subject".equals(type)){
				SpaceCases.setSubject(content);
			}

			dao.updateSpaceCases(SpaceCases);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 1;
	}

	@RequestMapping("/updateSpaceOrder")
	@ResponseBody
	public int updateSpaceOrder(HttpServletRequest request, HttpServletResponse response){
		//获取文字
		String id = request.getParameter("id");
		String type = request.getParameter("type");
		String content = request.getParameter("content");

		try {
			List<SpaceOrder> spaceOrders = dao.getSpaceOrderId(id);
			SpaceOrder spaceOrder = spaceOrders.get(0);

			if("status".equals(type)){
				spaceOrder.setStatus(Integer.parseInt(content));
			}

			dao.updateSpaceOrder(spaceOrder);
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

	@RequestMapping("/insertSpaceOrder")
	@ResponseBody
	public String insertSpaceOrder(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String openid = request.getParameter("openid");
		String openid_qr = request.getParameter("openid_qr");
		String lesson_id = request.getParameter("lesson_id");

		SpaceOrder spaceOrder =new SpaceOrder();
		spaceOrder.setOpenid(openid);
		spaceOrder.setOpenid_qr(openid_qr);
		spaceOrder.setLesson_id(lesson_id);
		spaceOrder.setCreate_time(create_time);

		try {
			dao.insertSpaceOrder(spaceOrder);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return "push massage successfully";
	}

	@RequestMapping("/insertSpaceCases")
	@ResponseBody
	public String insertSpaceCases(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String openid = request.getParameter("openid");
		String student_name = request.getParameter("student_name");
		String uuids = request.getParameter("uuids");
		String intro = request.getParameter("intro");
		String subject = request.getParameter("subject");

		SpaceCases spaceCases =new SpaceCases();
		spaceCases.setOpenid(openid);
		spaceCases.setStudent_name(student_name);
		spaceCases.setUuids(uuids);
		spaceCases.setIntro(intro);
		spaceCases.setCreate_time(create_time);
		spaceCases.setSubject(subject);

		try {
			dao.insertSpaceCases(spaceCases);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return "push massage successfully";
	}

	@RequestMapping("/insertSpaceLesson")
	@ResponseBody
	public String insertSpaceLesson(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String openid = request.getParameter("openid");
		String name = request.getParameter("name");
		String subject = request.getParameter("subject");
		String price = request.getParameter("price");

		SpaceLesson spaceLesson =new SpaceLesson();
		spaceLesson.setOpenid(openid);
		spaceLesson.setName(name);
		spaceLesson.setPrice(price);
		spaceLesson.setSubject(subject);
		spaceLesson.setCreate_time(create_time);

		try {
			dao.insertSpaceLesson(spaceLesson);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return "push massage successfully";
	}

	@RequestMapping("/getSpaceTeacher")
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

	@RequestMapping("/getSpaceCases")
	@ResponseBody
	public List getSpaceCases(String openid){
		List list = null;
		try {
			list = spaceService.getSpaceCases(openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getSpaceOrder")
	@ResponseBody
	public List getSpaceOrder(String openid){
		List list = null;
		try {
			list = spaceService.getSpaceOrder(openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getSpaceLesson")
	@ResponseBody
	public List getSpaceLesson(String openid){
		List list = null;
		try {
			list = spaceService.getSpaceLesson(openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/shareSpaceQrCode")
	@ResponseBody
	public JSONObject shareSpaceQrCode(String id){
		JSONObject jsonObject = new JSONObject();
		String token = loginService.getToken("BOOK");
		String scene = "id=" + id;

		List<BookUser> bookUsers = dao.getBookUserById(id);
		BookUser bookUser = bookUsers.get(0);
		String book_name = bookUser.getBook_name();

		try {
			String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + token;
			Map<String,String> param = new HashMap<>() ;
			param.put("scene",scene);
			param.put("page","pages/welcome/welcome");
			String json = JSON.toJSONString(param) ;
			ByteArrayInputStream inputStream = HttpUtil.sendBytePost(url, json);
			byte[] bytes = new byte[inputStream.available()];
			inputStream.read(bytes);
			String imageString = Base64.getEncoder().encodeToString(bytes);

			// 上传二维码
			String book_name_md5 = DigestUtils.md5Hex(book_name + "space" + id );
			String serverPath = "/data/uploadRr";
			String fileName = book_name_md5 + ".png";
			File file = new File(serverPath, fileName);
			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(bytes);
			}

			jsonObject.put("imageString", imageString);
			jsonObject.put("fileName", fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jsonObject;
	}

	@RequestMapping("/inviteSpaceUser")
	@ResponseBody
	public String inviteSpaceUser(HttpServletRequest request, HttpServletResponse response){
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
		String id = request.getParameter("id");

		String openid = request.getParameter("openid");
		String openid_qr = "no_id";
		String book_name = "未录入";

		List<BookUser> bookUsers = dao.getBookUserById(id);
		if(bookUsers.size()>0){
			BookUser bookUser_get = bookUsers.get(0);
			book_name = bookUser_get.getBook_name();
			openid_qr = bookUser_get.getOpenid();
		}


		BookUser bookUser = new BookUser();
		bookUser.setNick_name("微信用户");
		bookUser.setRole("client");
		bookUser.setOpenid(openid);
		bookUser.setBook_name(book_name);
		bookUser.setCreate_time(create_time);
		bookUser.setOpenid_qr(openid_qr);
		bookUser.setAvatarurl("525addcc-03e8-427f-944a-ac4ff38383b3.png");

		dao.insertBookUser(bookUser);
		return "push massage successfully";
	}

	@RequestMapping("/deleteSpaceCases")
	@ResponseBody
	public int deleteSpaceCases(Integer id){
		try {
			dao.deleteSpaceCases(id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@RequestMapping("/deleteSpaceLesson")
	@ResponseBody
	public int deleteSpaceLesson(Integer id){
		try {
			dao.deleteSpaceLesson(id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

}
	
	


