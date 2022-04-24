package com.xue.controller;
import java.io.*;
import java.sql.PseudoColumnUsage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.plaf.IconUIResource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.jndi.ldap.sasl.LdapSasl;
import com.xue.entity.model.*;
import com.xue.repository.dao.UserMapper;
import com.xue.util.HttpUtil;
import com.xue.util.Imageutil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.xue.service.LoginService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
public class LoginController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String tample1 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"xwY-9Dx1udclJoPVNna583hd25fZmBl8AtgcOj7jSN0\",\"data\":{\"thing2\":{\"value\": \"classname\"},\"thing4\":{\"value\": \"studentname\"},\"thing1\":{\"value\": \"来看看小朋友今天的表现吧~~\"},\"time3\":{\"value\": \"mytime\"}}}";
	private static final String tample2 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"X4-OA7Aj-Ayn5exDPAk28GiSRJQ5-C827ekUyQH5hA8\",\"data\":{\"thing1\":{\"value\": \"一起总结一下最近的成果吧\"},\"thing2\":{\"value\": \"process\"}}}";

	@Autowired
	private LoginService loginService;

	@Autowired
	private UserMapper dao;

	//	获取token
	@RequestMapping("/sendSubscribe")
	@ResponseBody
	public String sendSubscribe(String token, String openid, String classname,String studentname, String mytime){
		String result = null;
		String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
		JSONObject queryJson = JSONObject.parseObject(tample1);
		queryJson.put("touser",openid);
		queryJson.getJSONObject("data").getJSONObject("thing2").put("value",classname);
		queryJson.getJSONObject("data").getJSONObject("thing4").put("value",studentname);
		queryJson.getJSONObject("data").getJSONObject("time3").put("value",mytime);

		String param="access_token="+ token +"&data=" + queryJson.toJSONString();
		System.out.printf("param:"+param);
		try {
			result = HttpUtil.sendPostJson(url	,queryJson.toJSONString());
			System.out.printf("res:" + result);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}


	//	获取token
	@RequestMapping("/sendRemind")
	@ResponseBody
	public String sendRemind(String token, String openid, String total, String left){
		String result = null;
		String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
		JSONObject queryJson = JSONObject.parseObject(tample2);
		queryJson.put("touser",openid);
		String process = "总课时：" + total + ";  余课时：" + left;
		queryJson.getJSONObject("data").getJSONObject("thing2").put("value",process);

		String param="access_token="+ token +"&data=" + queryJson.toJSONString();
		System.out.printf("param:"+param);
		try {
			result = HttpUtil.sendPostJson(url	,queryJson.toJSONString());
			System.out.printf("res:" + result);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

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
	public List getMessage(String studio,Integer page,String comment_style,String openid,String role){
		List list = null;
		try {
			list = loginService.getMessage(studio,page,comment_style,openid,role);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取学生的课程数
	@RequestMapping("/getLessonByName")
	@ResponseBody
	public List getLessonByName(String student_name,String studio){
		List list = null;
		try {
			list = loginService.getLessonByName(student_name,studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/getLesson")
	@ResponseBody
	public List getLesson(String studio,String student_name){
		List list = null;
		try {
			list = loginService.getLesson(studio,student_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/getRating")
	@ResponseBody
	public List getRating(String studio,String student_name){
		List list = null;
		try {
			list = loginService.getRating(studio,student_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取广告
	@RequestMapping("/getAdvertise")
	@ResponseBody
	public List getAdvertise(String studio){
		List list = null;
		try {
			list = loginService.getAdvertise(studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取奖状
	@RequestMapping("/getCertificate")
	@ResponseBody
	public List getCertificate(String studio,String student_name){
		List list = null;
		try {
			list = loginService.getCertificate(studio,student_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取奖状
	@RequestMapping("/getPaycode")
	@ResponseBody
	public List getPaycode(String student_name){
		List list = null;
		try {
			list = loginService.getPaycode(student_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取课程体系
	@RequestMapping("/getClassSys")
	@ResponseBody
	public List getClassSys(String class_target,String studio){
		List list = null;
		try {
			list = loginService.getClassSys(class_target,studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取主页
	@RequestMapping("/getHome")
	@ResponseBody
	public List getHome(String studio){
		List list = null;
		try {
			list = loginService.getHome(studio);
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
	public List getSchedule(String date_time,String studio){
		List list = null;
		try {
			list = loginService.getSchedule(date_time,studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取范画
	@RequestMapping("/getModel")
	@ResponseBody
	public List getModel(String studio,Integer page){
		List list = null;
		try {
			list = loginService.getModel(studio,page);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取PPT名字
	@RequestMapping("/getPpt")
	@ResponseBody
	public List getPpt(Integer page){
		List list = null;
		try {
			list = loginService.getPpt(page);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取教程列表
	@RequestMapping("/getCourseList")
	@ResponseBody
	public List getCourseList(String studio,Integer page){
		List list = null;
		try {
			list = loginService.getCourseList(studio,page);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取教程列表
	@RequestMapping("/getCourseDetail")
	@ResponseBody
	public List getCourseDetail(String studio,String class_name){
		List list = null;
		try {
			list = loginService.getCourseDetail(studio,class_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}


	//	获取全部
	@RequestMapping("/getSearch")
	@ResponseBody
	public List getSearch(String student_name,String studio){
		List list = null;
		try {
			list = loginService.getSearch(student_name,studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/getSignUp")
	@ResponseBody
	public List getSignUp(String student_name,String studio){
		List list = null;
		try {
			list = loginService.getSignUp(student_name,studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/getGift")
	@ResponseBody
	public List getGift(String student_name,String studio){
		List list = null;
		try {
			list = loginService.getGift(student_name,studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取详情页
	@RequestMapping("/getDetails")
	@ResponseBody
	public List getDetails(Integer id,String studio){
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
	public List getOpenidByNick(String student_name,String studio){
		List list = null;
		try {
			list = loginService.getOpenidByNick(student_name,studio);
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

	//	获取用户
	@RequestMapping("/getUserByNickName")
	@ResponseBody
	public List getUserByNickName(String nickName){
		List list = null;
		try {
			list = loginService.getUserByNickName(nickName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取工作室列表
	@RequestMapping("/getStudio")
	@ResponseBody
	public List getStudio(){
		List list = null;
		try {
			list = loginService.getStudio();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取奖状名
	@RequestMapping("/getCertificateModelName")
	@ResponseBody
	public List getCertificateModel(){
		List list = null;
		try {
			list = loginService.getCertificateModelName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取奖状模板
	@RequestMapping("/getCertificateModel")
	@ResponseBody
	public List getCertificateModel(String class_name){
		System.out.printf("class:" + class_name);
		List list = null;
		try {
			list = loginService.getCertificateModel(class_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取详情页
	@RequestMapping("/deleteComment")
	@ResponseBody
	public int deleteComment(Integer id,String role){
		try {
			loginService.deleteComment(id,role);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	//	获取详情页
	@RequestMapping("/deleteSignUpRecord")
	@ResponseBody
	public int deleteSignUpRecord(Integer id,String role){
		try {
			loginService.deleteSignUpRecord(id,role);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	//	获取详情页
	@RequestMapping("/deleteGiftRecord")
	@ResponseBody
	public int deleteGiftRecord(Integer id,String role){
		try {
			loginService.deleteGiftRecord(id,role);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@RequestMapping("/deleteSchedule")
	@ResponseBody
	public int deleteSchedule(Integer id,String role){
		try {
			loginService.deleteSchedule(id,role);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@RequestMapping("/signUpSchedule")
	@ResponseBody
	public int signUpSchedule(String student_name,String studio,String date_time,String mark,String class_count){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String update_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
		try {
			Schedule schedule =new Schedule();
			SignUp signUp = new SignUp();
			schedule.setStudent_name(student_name);
			schedule.setStudio(studio);
			schedule.setUpdate_time(update_time);
			loginService.updateSchedule(schedule);

			signUp.setStudent_name(student_name);
			signUp.setStudio(studio);
			signUp.setSign_time(update_time);
			signUp.setCreate_time(date_time + " 00:00:00");
			signUp.setMark(mark);
			loginService.insertSignUp(signUp);

			Float count = Float.parseFloat(class_count);
			loginService.updateMinusLesson(student_name,studio,count);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/deleteLesson")
	@ResponseBody
	public int deleteLesson(Integer id,String role){
		try {
			loginService.deleteLesson(id,role);
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

	//	推送文件
	@RequestMapping("/push_file")
	@ResponseBody
	public String push_file(HttpServletRequest request, HttpServletResponse response){

		//获取图片
		MultipartHttpServletRequest req = (MultipartHttpServletRequest)request;
		MultipartFile multipartFile = req.getFile("file");
		String file_name =  request.getParameter("file_name");

		//获取类路径
		String path = System.getProperty("user.dir");
		String p_path = path +"/uploadfiles/"+ file_name;

		//保存图片
		try {
			multipartFile.transferTo(new File(p_path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p_path;
	}

	@RequestMapping("/get_file")
	@ResponseBody
	public ResponseEntity<byte[]> EIToolDownloads(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String file_name =  request.getParameter("file_name");
		String path = System.getProperty("user.dir");
		String p_path = path +"/uploadfiles/"+ file_name;
		File file = new File(p_path);
		if(file.exists()){
			org.springframework.http.HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", file.getName());
			return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),headers, HttpStatus.OK);
		}else{
			System.out.println("文件不存在,请重试...");
			return null;
		}
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

		String studio = request.getParameter("studio");

		FileInputStream in = null;
		try {
			Message message =new Message();

			message.setComment(comment);
			message.setStudent_name(student_name);
			message.setCreate_time(create_time);
			message.setClass_name(class_name);
			message.setClass_target(class_target);
			message.setStudio(studio);

			if(!"奖状".equals(class_target)){
				in = Imageutil.readImage(photo);
				message.setPhoto(FileCopyUtils.copyToByteArray(in));
				loginService.push(message);
				if(!"范画".equals(class_target)){
//					loginService.updateMinusLesson(student_name,studio);
					loginService.updateAddPoints(student_name,studio);
				}
			}

			if("奖状".equals(class_target)){
				String path = System.getProperty("user.dir");
				String p_path = path +"/uploadimages/"+ photo + ".png";

				FileInputStream file = Imageutil.readImage(p_path );

				message.setPhoto(FileCopyUtils.copyToByteArray(file));
				loginService.push(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}


	//	推送
	@RequestMapping("/insertShedule")
	@ResponseBody
	public String insertShedule(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		//获取日期
		String add_date = request.getParameter("add_date");
		//获年龄段
		String age = request.getParameter("age");
		//获取名字
		String student_name = request.getParameter("student_name");
		//获取时间段
		String duration = request.getParameter("duration");

		String studio = request.getParameter("studio");
		Schedule schedule =new Schedule();
		List<String> list = Arrays.asList(student_name.split(" "));
		try {
			for (int i=0; i < list.size();i++){
				String list_student = list.get(i);
				schedule.setAdd_date(add_date);
				schedule.setAge(age);
				schedule.setStudent_name(list_student);
				schedule.setDuration(duration);
				schedule.setCreate_time(create_time);
				schedule.setUpdate_time(create_time);
				schedule.setStudio(studio);
				loginService.insertSchedule(schedule);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}


	@RequestMapping("/insertUser")
	@ResponseBody
	public String insertUser(HttpServletRequest request, HttpServletResponse response){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
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
		//获取学生名
		String student_name = request.getParameter("student_name");
		if(student_name == null || student_name.isEmpty() || "undefined".equals(student_name)){
			student_name = "no_name";
		}
		//获年角色
		String role = request.getParameter("role");
		if(role == null || role.isEmpty() || "undefined".equals(role)){
			role = "client";
		}
        //获取 openid
		String openid = request.getParameter("openid");
		//获取 avatarurl
		String avatarurl = request.getParameter("avatarurl");

		String studio = request.getParameter("studio");
        //获取 comment_style
		String comment_style = "public";
		try {
			List<User> list_u = dao.getComentStyle(studio);
			comment_style = list_u.get(0).getComment_style();
		} catch (Exception e) {
			e.printStackTrace();
		}


		try {
			User user =new User();
			user.setNick_name(nick_name);
			user.setStudent_name(student_name);
			user.setRole(role);
			user.setOpenid(openid);
			user.setCreate_time(create_time);
			user.setAvatarurl(avatarurl);
			user.setStudio(studio);
			user.setExpired_time(expired_time);
			int res = loginService.updateUser(user);
			if (0==res){
				user.setUser_type("新用户");
				user.setComment_style(comment_style);
				loginService.insertUser(user);
			}
			if(res>0&&!student_name.equals("no_name")){
				List<User> list= dao.getUser(openid);
				String user_type_get = list.get(0).getUser_type();
				String role_get = list.get(0).getRole();
				user.setUser_type(user_type_get);
				user.setRole(role_get);
				user.setComment_style(comment_style);
				loginService.insertUser(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	@RequestMapping("/updateRole")
	@ResponseBody
	public String updateRole(HttpServletRequest request, HttpServletResponse response){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		try {
			cal.setTime(df.parse(create_time));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		cal.add(cal.DATE,30);
		String expired_time = df.format(cal.getTime());

		//获取openid
		String openid = request.getParameter("openid");

		User user_get= dao.getUser(openid).get(0);
		String role_get = user_get.getRole();
		//定义role
		String role =null;
		if (role_get.equals("boss")){
			role = "client";
		} else {
			role = "boss";
		}

        //获取用户类型
		String user_type = "老用户";

		try {
			User user =new User();
			user.setOpenid(openid);
			user.setRole(role);
			user.setUser_type(user_type);
			user.setCreate_time(create_time);
			user.setExpired_time(expired_time);
			loginService.updateUsertype(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	@RequestMapping("/updateComentStyle")
	@ResponseBody
	public String updateComentStyle(HttpServletRequest request, HttpServletResponse response){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		try {
			cal.setTime(df.parse(create_time));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		cal.add(cal.DATE,30);
		String expired_time = df.format(cal.getTime());

		//获取openid
		String openid = request.getParameter("openid");

		String studio = request.getParameter("studio");

		User user_get= dao.getUser(openid).get(0);
		String comment_style_get = user_get.getComment_style();
		//定义comment_style
		String comment_style =null;
		if (comment_style_get.equals("public")){
			comment_style = "self";
		} else {
			comment_style = "public";
		}

		//获取用户类型

		try {
			User user =new User();
			user.setComment_style(comment_style);
			user.setStudio(studio);
			loginService.updateComentStyle(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	@RequestMapping("/updateLesson")
	@ResponseBody
	public String updateLesson(HttpServletRequest request, HttpServletResponse response){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
		try {
			cal.setTime(df.parse(create_time));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		//获取用户名
		String student_name = request.getParameter("student_name");
		//获取学生名
		Float total_amount = Float.valueOf(request.getParameter("total_amount"));
		//获年角色
		String left_amount_get = request.getParameter("left_amount");
		Float left_amount =null;
		if(left_amount_get.isEmpty()){
			left_amount = -1.0f;
		}else {
			left_amount = Float.parseFloat(left_amount_get);
		}

		// 获取工作室
		String studio = request.getParameter("studio");

		// 新增课程
		Lesson lesson =new Lesson();
		try {
			lesson.setStudent_name(student_name);
			lesson.setTotal_amount(total_amount);
			lesson.setLeft_amount(left_amount);
			lesson.setCreate_time(create_time);
			lesson.setStudio(studio);

			int res = loginService.updateLesson(lesson);
			if (0==res){
				Integer point = 0;
				lesson.setPoints(point);
				loginService.insertLesson(lesson);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 发放开课礼物
		Gift gift = new Gift();
		try {
			String gift_name = request.getParameter("gift_name");
			if (!gift_name.isEmpty()){
				String gift_amount = request.getParameter("gift_amount");
				String expired_days = request.getParameter("expired_days");
				cal.add(cal.DATE,Integer.parseInt(expired_days));
				String expired_time = df.format(cal.getTime());

				gift.setStudent_name(student_name);
				gift.setGift_name(gift_name);
				gift.setGift_amount(Integer.parseInt(gift_amount));
				gift.setCreate_time(create_time);
				gift.setExpired_time(expired_time);
				gift.setStudio(studio);
				gift.setStatus(0);
				loginService.insertGift(gift);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}


		return "push massage successfully";
	}


	@RequestMapping("/updateLessonPoints")
	@ResponseBody
	public String updateLessonPoints(HttpServletRequest request, HttpServletResponse response){
		//获取用户名
		String student_name = request.getParameter("student_name");
		String studio = request.getParameter("studio");

		try {
			loginService.updateAddPoints(student_name,studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}


	@RequestMapping("/updateCoins")
	@ResponseBody
	public String updateCoins(HttpServletRequest request, HttpServletResponse response){
		//获取用户名
		String type = request.getParameter("type");
		String openid = request.getParameter("openid");

		try {
			loginService.updateCoins(openid,type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	@RequestMapping("/updateGift")
	@ResponseBody
	public String updateGift(HttpServletRequest request, HttpServletResponse response){
		//获取用户名
		String id = request.getParameter("id");

		try {
			loginService.updateGift(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}


	@RequestMapping("/deletePoints")
	@ResponseBody
	public String deletePoints(HttpServletRequest request, HttpServletResponse response){
		//获取用户名
		String student_name = request.getParameter("student_name");
		String studio = request.getParameter("studio");
		String points_get = request.getParameter("points");
		Integer points = Integer.parseInt(points_get);

		try {
			loginService.deletePoints(student_name,studio,points);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

}
	
	


