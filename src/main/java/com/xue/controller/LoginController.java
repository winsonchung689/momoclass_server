package com.xue.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xue.entity.model.*;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;
import com.xue.util.HttpUtil;
import com.xue.util.Imageutil;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.commons.io.FileUtils;
import org.aspectj.weaver.ast.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class LoginController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String tample1 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"5kASxPkT-RY5K-ti-iFh924Xgd97858juXOjynsTWVo\",\"data\":{\"thing6\":{\"value\": \"classname\"},\"name3\":{\"value\": \"studentname\"},\"thing4\":{\"value\": \"来看看小朋友的表现吧\"},\"date5\":{\"value\": \"mytime\"}}}";
	private static final String tample2 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"wi0QTuLQiYFUbCWGk2eP_KJIviqnDVh8XVq364Q3704\",\"data\":{\"thing5\":{\"value\": \"AA\"},\"date2\":{\"value\": \"AA\"},\"thing3\":{\"value\": \"AA\"},\"thing6\":{\"value\": \"process\"},\"thing8\":{\"value\": \"process\"}}}";
	private static final String tample3 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"3BPMQuajTekT04oI8rCTKMB2iNO4XWdlDiMqR987TQk\",\"data\":{\"date1\":{\"value\": \"2022-11-01 10:30-11:30\"},\"thing2\":{\"value\": \"A1\"},\"name3\":{\"value\": \"小明\"},\"thing5\":{\"value\": \"记得来上课哦\"}}}";

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
		queryJson.getJSONObject("data").getJSONObject("thing6").put("value",classname);
		queryJson.getJSONObject("data").getJSONObject("name3").put("value",studentname);
		queryJson.getJSONObject("data").getJSONObject("date5").put("value",mytime);

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
	@RequestMapping("/sendSignUpRemind")
	@ResponseBody
	public String sendSignUpRemind(String token, String openid, String total, String left,String student_name,String date_time,String class_count,String studio){
		String result = null;
		String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
		JSONObject queryJson = JSONObject.parseObject(tample2);

		List<Lesson> lessons = dao.getLessonByName(student_name, studio);
		Float count = 0.0f;
		if(lessons.size()>0){
			count = lessons.get(0).getMinus();
		}

		if(Float.parseFloat(class_count) != 100){
			count = Float.parseFloat(class_count);
		}

		String thing8 = "本次扣课" + count + "课时，总课时" + total + "课时";

		queryJson.put("touser",openid);
		queryJson.getJSONObject("data").getJSONObject("thing5").put("value",student_name);
		queryJson.getJSONObject("data").getJSONObject("date2").put("value",date_time);
		queryJson.getJSONObject("data").getJSONObject("thing3").put("value","签到成功");
		queryJson.getJSONObject("data").getJSONObject("thing6").put("value",left + "课时");
		queryJson.getJSONObject("data").getJSONObject("thing8").put("value",thing8);


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
	@RequestMapping("/sendClassRemind")
	@ResponseBody
	public String sendClassRemind(String token, String openid, String duration, String studentname,String remindDay,String class_number){
		String result = null;
		String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
		JSONObject queryJson = JSONObject.parseObject(tample3);
		queryJson.put("touser",openid);
		queryJson.getJSONObject("data").getJSONObject("date1").put("value",remindDay+" " + duration.split("-")[0]);
		queryJson.getJSONObject("data").getJSONObject("thing2").put("value",class_number);
		queryJson.getJSONObject("data").getJSONObject("name3").put("value",studentname);

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
	public String getOpenid(String code,String app){
		String result = null;
		String openid = null;
		String param = null;
		String url = "https://api.weixin.qq.com/sns/jscode2session";

		String MOMO2C_param="appid=wx3f5dc09cc495429b&secret=ac693c65ae57020643224561ac102dce&js_code="+ code +"&grant_type=authorization_code";
		String MOMO2B_param = "appid=wxc61d8f694d20f083&secret=ed083522ff79ac7dad24e115aecfbc08&js_code="+ code +"&grant_type=authorization_code";
		if ("MOMO2B".equals(app)){
			param = MOMO2B_param;
		}else if ("MOMO2C".equals(app)){
			param = MOMO2C_param;
		}
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
	public String getToken(String app){
		String result = null;
		String token = null;
		String param = null;
		String url = "https://api.weixin.qq.com/cgi-bin/token";

		String MOMO2C_param="appid=wx3f5dc09cc495429b&secret=ac693c65ae57020643224561ac102dce&grant_type=client_credential";
		String MOMO2B_param = "appid=wxc61d8f694d20f083&secret=ed083522ff79ac7dad24e115aecfbc08&grant_type=client_credential";
		if ("MOMO2B".equals(app)){
			param = MOMO2B_param;
		}else if ("MOMO2C".equals(app)){
			param = MOMO2C_param;
		}
		try {
			result = HttpUtil.sendPost(url,param);
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

	//	获取全部
	@RequestMapping("/getGrowthRecord")
	@ResponseBody
	public List getGrowthRecord(String studio,Integer page,String student_name){
		List list = null;
		try {
			list = loginService.getGrowthRecord(studio,page,student_name);
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
	public List getLesson(String studio,String student_name,String subject){
		List list = null;
		try {
			list = loginService.getLesson(studio,student_name,subject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/getTipsDataUrl")
	@ResponseBody
	public List getTipsDataUrl(String studio,Integer left_amount,String subject){
		List list = null;
		try {
			list = loginService.getTipsDataUrl(studio,left_amount,subject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/getLessonInName")
	@ResponseBody
	public List getLessonInName(String studio,String student_name,Integer page){
		List list = null;
		try {
			list = loginService.getLessonInName(studio,student_name,page);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取商品列表
	@RequestMapping("/getGoodsList")
	@ResponseBody
	public List getGoodsList(String studio,Integer page){
		List list = null;
		try {
			list = loginService.getGoodsList(studio,page);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/getRating")
	@ResponseBody
	public List getRating(String studio,String student_name,Integer page,String subject){
		List list = null;
		try {
			list = loginService.getRating(studio,student_name,page,subject);
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
	public List getClassSys(String class_target,String studio,Integer page){
		List list = null;
		try {
			list = loginService.getClassSys(class_target,studio,page);
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
	public List getSchedule(String date_time,String studio,String subject,String openid){
		List list = null;
		try {
			list = loginService.getSchedule(date_time,studio,subject,openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取课程表
	@RequestMapping("/getScheduleDetail")
	@ResponseBody
	public List getScheduleDetail(String weekDay,String duration,String studio,String class_number,String subject){
		List list = null;
		try {
			list = loginService.getScheduleDetail(Integer.parseInt(weekDay),duration,studio,class_number,subject);
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

	//	获取妈妈分享
	@RequestMapping("/getMamaShare")
	@ResponseBody
	public List getMamaShare(Integer page){
		List list = null;
		try {
			list = loginService.getMamaShare(page);
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
	public List getCourseDetail(String studio,String class_name,Integer page){
		List list = null;
		try {
			list = loginService.getCourseDetail(studio,class_name,page);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}


	//	获取全部
	@RequestMapping("/getSearch")
	@ResponseBody
	public List getSearch(String student_name,String studio,Integer page){
		List list = null;
		try {
			list = loginService.getSearch(student_name,studio,page);
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

	//	获取请假记录
	@RequestMapping("/getLeaveRecord")
	@ResponseBody
	public List getLeaveRecord(String student_name,String studio,String leave_type){
		List list = null;
		try {
			list = loginService.getLeaveRecord(student_name,studio,leave_type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/getArrangement")
	@ResponseBody
	public List getArrangement(String studio,Integer dayofweek,String date,String subject,String openid){
		List list = null;
		try {
			list = loginService.getArrangement(studio,dayofweek,date,subject,openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/changeClass")
	@ResponseBody
	public String changeClass(String studio,String changeday,String duration,String class_number,String weekday,String subject){
		String result=null;
		try {
			 result = loginService.changeClass(studio,Integer.parseInt(changeday),duration,class_number,Integer.parseInt(weekday),subject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	//	获取全部
	@RequestMapping("/modifyMark")
	@ResponseBody
	public String modifyMark(String id,String studio,String mark){
		try {
			dao.modifyMark(id,studio,mark);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
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

	//	获取详情页
	@RequestMapping("/getDetailsUrlByDate")
	@ResponseBody
	public List getDetailsUrlByDate(String studio, String duration, String student_name, String date_time){
		List list = null;
		try {
			list = loginService.getDetailsUrlByDate(studio,duration,student_name,date_time);
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
	@RequestMapping("/getUserByOpenid")
	@ResponseBody
	public List getUserByOpenid(String openid){
		List list = null;
		try {
			list = loginService.getUserByOpenid(openid);
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

	//	获取工作室列表
	@RequestMapping("/getClassNumbers")
	@ResponseBody
	public List getClassNumbers(String studio){
		List list = null;
		try {
			list = loginService.getClassNumbers(studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取课程列表
	@RequestMapping("/getArrangements")
	@ResponseBody
	public List getArrangements(String studio){
		List list = null;
		try {
			list = loginService.getArrangements(studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}


	//	获取课程列表
	@RequestMapping("/getArrangementsByDate")
	@ResponseBody
	public List getArrangementsByDate(String studio,String date_time){
		List list = null;
		try {
			list = loginService.getArrangementsByDate(studio,date_time);
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

	//	获取奖状模板
	@RequestMapping("/getFrameModel")
	@ResponseBody
	public List getFrameModel(String studio,Integer page){
		List list = null;
		try {
			list = loginService.getFrameModel(studio,page);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取订单
	@RequestMapping("/getMyOrder")
	@ResponseBody
	public List getMyOrder(String studio,String openid){
		List list = null;
		try {
			list = loginService.getMyOrder(studio,openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}


	//	获取详情页
	@RequestMapping("/deleteComment")
	@ResponseBody
	public int deleteComment(Integer id,String role,String studio,String openid){
		try {
			loginService.deleteComment(id,role,studio,openid);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	//	获取详情页
	@RequestMapping("/deleteMyOrder")
	@ResponseBody
	public int deleteMyOrder(Integer id){
		try {
			dao.deleteMyOrder(id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	//	获取详情页
	@RequestMapping("/deliverMyOrder")
	@ResponseBody
	public int deliverMyOrder(Integer id){
		try {
			dao.deliverMyOrder(id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}



	//	获取详情页
	@RequestMapping("/deleteGoodsList")
	@ResponseBody
	public int deleteGoodsList(Integer id,String role,String studio,String openid){
		try {
			loginService.deleteGoodsList(id,role,studio,openid);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	//	获取详情页
	@RequestMapping("/deleteArrangement")
	@ResponseBody
	public int deleteArrangement(Integer id,String role,String studio,String openid,String duration,String weekday,String class_number){
		try {
			List<User> list = dao.getUser(openid);
			String studio_get = list.get(0).getStudio();

			if ("boss".equals(role) && studio_get.equals(studio)) {
				loginService.deleteArrangement(id,role,studio,openid);
				loginService.deleteScheduleByDate(Integer.parseInt(weekday),duration,studio,class_number,role,openid);
			}else {
				logger.error("it's not your studio, could not delete!");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	//	获取详情页
	@RequestMapping("/changeClassName")
	@ResponseBody
	public int changeClassName(String id,String role,String studio,String openid,String class_number,String change_title){
		try {
			loginService.changeClassName(id,role,studio,openid,class_number,change_title);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}


	//	获取详情页
	@RequestMapping("/deleteSignUpRecord")
	@ResponseBody
	public int deleteSignUpRecord(Integer id,String role,String studio,String openid){
		try {
			List<User> list = dao.getUser(openid);
			String studio_get = list.get(0).getStudio();

			if ("boss".equals(role) && studio_get.equals(studio)) {
				loginService.deleteSignUpRecord(id,role,studio,openid);
			}else {
				logger.error("it's not your studio, could not delete!");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	//	清空签到记录
	@RequestMapping("/deleteSignUpAllRecord")
	@ResponseBody
	public int deleteSignUpAllRecord(String name,String role,String studio,String openid){
		try {
			List<User> list = dao.getUser(openid);
			String studio_get = list.get(0).getStudio();

			if ("boss".equals(role) && studio_get.equals(studio)) {
				loginService.deleteSignUpAllRecord(name,role,studio,openid);
			}else {
				logger.error("it's not your studio, could not delete!");
			}


		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	//	清空请假记录
	@RequestMapping("/deleteLeaveAllRecord")
	@ResponseBody
	public int deleteLeaveAllRecord(String name,String studio,String role,String openid,String leave_type){
		try {
			List<User> list = dao.getUser(openid);
			String studio_get = list.get(0).getStudio();

			if ("boss".equals(role) && studio_get.equals(studio)) {
				dao.deleteLeaveAllRecord(name, studio,leave_type);
			}else {
				logger.error("it's not your studio, could not delete!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	//	获取详情页
	@RequestMapping("/deleteGiftRecord")
	@ResponseBody
	public int deleteGiftRecord(Integer id,String role,String studio,String openid){
		try {
			List<User> list = dao.getUser(openid);
			String studio_get = list.get(0).getStudio();

			if ("boss".equals(role) && studio_get.equals(studio)) {
				loginService.deleteGiftRecord(id,role,studio,openid);
			}else {
				logger.error("it's not your studio, could not delete!");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	//	获取详情页
	@RequestMapping("/deleteLeaveRecord")
	@ResponseBody
	public int deleteLeaveRecord(Integer id,String role,String studio,String openid){
		try {
			List<User> list = dao.getUser(openid);
			String studio_get = list.get(0).getStudio();
			if ("boss".equals(role) && studio_get.equals(studio)) {
				dao.deleteLeaveRecord(id, studio);
			}else {
				logger.error("it's not your studio, could not delete!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@RequestMapping("/deleteSchedule")
	@ResponseBody
	public int deleteSchedule(Integer id,String role,String studio,String openid){
		try {
			List<User> list = dao.getUser(openid);
			String studio_get = list.get(0).getStudio();

			if ("boss".equals(role) && studio_get.equals(studio)) {
				loginService.deleteSchedule(id,role,studio,openid);
			}else {
				logger.error("it's not your studio, could not delete!");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@RequestMapping("/confirmSchedule")
	@ResponseBody
	public int confirmSchedule(Integer id,String role,String studio,String openid){
		try {
			loginService.confirmSchedule(id,role,studio,openid);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@RequestMapping("/signUpSchedule")
	@ResponseBody
	public int signUpSchedule(String student_name,String studio,String date_time,String mark,String class_count,String duration,String class_number,String subject){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String update_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
		try {
			Schedule schedule =new Schedule();
			SignUp signUp = new SignUp();
			schedule.setStudent_name(student_name);
			schedule.setStudio(studio);
			schedule.setUpdate_time(update_time);
			loginService.updateSchedule(schedule);

			List<Lesson> lessons = dao.getLessonByName(student_name, studio);
			Float count = 0.0f;
			Integer coins = 0;
			if(lessons.size()>0){
				count = lessons.get(0).getMinus();
				Float coins_get = lessons.get(0).getCoins();
				coins = Math.round(coins_get);
			}

			if(Float.parseFloat(class_count) != 100){
				count = Float.parseFloat(class_count);
			}

			signUp.setStudent_name(student_name);
			signUp.setStudio(studio);
			signUp.setSign_time(update_time);
			signUp.setCreate_time(date_time + " 00:00:00");
			signUp.setMark(mark);
			signUp.setDuration(duration);
			signUp.setCount(count);
			signUp.setSubject(subject);
			if(class_number == null || class_number.isEmpty() || "undefined".equals(class_number)){
				class_number = "无班号";
			}
			signUp.setClass_number(class_number);

			int insert_res = loginService.insertSignUp(signUp);
			if(insert_res>0){
				loginService.updateMinusLesson(student_name,studio,count);
				loginService.updateAddPoints(student_name,studio,coins);
			}


		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/signUpScheduleClass")
	@ResponseBody
	public int signUpScheduleClass(String studio,String date_time,String duration,String class_number,String subject){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String update_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		Date d = null;
		String student_name = null;
		String mark = "无备注";


		try {
			d = fmt.parse(date_time);
			Calendar cal = Calendar.getInstance();
			cal.setTime(d);
			Integer weekDay = cal.get(Calendar.DAY_OF_WEEK);

			List<Schedule> schedules = dao.getScheduleDetail(weekDay,duration,studio,class_number,subject);
			for(int i = 0;i < schedules.size(); i++){
				student_name = schedules.get(i).getStudent_name();
				Schedule schedule =new Schedule();
				SignUp signUp = new SignUp();
				schedule.setStudent_name(student_name);
				schedule.setStudio(studio);
				schedule.setUpdate_time(update_time);
				loginService.updateSchedule(schedule);

				List<Lesson> lessons = dao.getLessonByName(student_name, studio);
				Float count = 0.0f;
				Integer coins = 0;
				if(lessons.size()>0){
					count = lessons.get(0).getMinus();
					Float coins_get = lessons.get(0).getCoins();
					coins = Math.round(coins_get);
				}

				signUp.setStudent_name(student_name);
				signUp.setStudio(studio);
				signUp.setSign_time(update_time);
				signUp.setCreate_time(date_time + " 00:00:00");
				signUp.setMark(mark);
				signUp.setDuration(duration);
				signUp.setCount(count);
				signUp.setSubject(subject);
				if(class_number == null || class_number.isEmpty() || "undefined".equals(class_number)){
					class_number = "无班号";
				}
				signUp.setClass_number(class_number);

				int insert_res = loginService.insertSignUp(signUp);
				if(insert_res>0){
					loginService.updateMinusLesson(student_name,studio,count);
					loginService.updateAddPoints(student_name,studio,coins);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/leaveRecord")
	@ResponseBody
	public int leaveRecord(String student_name,String studio,String date_time,String duration,String leave_type,String mark_leave){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
		try {
			Leave leave =new Leave();
			leave.setStudent_name(student_name);
			leave.setStudio(studio);
			leave.setDate_time(date_time);
			leave.setDuration(duration);
			leave.setCreate_time(create_time);
			leave.setMark_leave(mark_leave);
			if(leave_type == null || leave_type.isEmpty() || "undefined".equals(leave_type)){
				leave_type = "请假";
			}
			leave.setLeave_type(leave_type);
			dao.insertLeave(leave);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/updateComment")
	@ResponseBody
	public int updateComment(HttpServletRequest request, HttpServletResponse response){

		//获取文字
		String comment = request.getParameter("comment");
		//获取课堂目标
		String class_target = request.getParameter("class_target");

		String studio = request.getParameter("studio");

		String id = request.getParameter("id");

		try {
			List<Message> messages_get = dao.getDetails(Integer.parseInt(id));
			Message message_get = messages_get.get(0);
			String commemt_get = message_get.getComment();
			String target_get = message_get.getClass_target();

			Message message =new Message();
			if(comment.isEmpty()){
				comment =commemt_get;
			}
			if(class_target.isEmpty()){
				class_target =target_get;
			}
			message.setComment(comment);
			message.setClass_target(class_target);
			message.setId(id);
			message.setStudio(studio);
			loginService.updateComment(message);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/updateLocation")
	@ResponseBody
	public int updateLocation(HttpServletRequest request, HttpServletResponse response){

		//获取电话
		String phone_number = request.getParameter("phone_number");
		//获取地址
		String location = request.getParameter("location");

		String studio = request.getParameter("studio");

		String openid = request.getParameter("openid");

		try {
			dao.updateLocation(studio,openid,phone_number,location);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/updateDetailPhoto")
	@ResponseBody
	public int updateDetailPhoto(HttpServletRequest request, HttpServletResponse response){

		//获取文字
		String photo = request.getParameter("photo");

		String studio = request.getParameter("studio");

		String id = request.getParameter("id");

		FileInputStream in = null;

		try {
			Message message =new Message();
			in = Imageutil.readImage(photo);
			message.setPhoto(FileCopyUtils.copyToByteArray(in));
			message.setId(id);
			message.setStudio(studio);
			loginService.updateDetailPhoto(message);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/insertArrangement")
	@ResponseBody
	public int insertArrangement(String dayofweek,String class_number,String duration,String limits,String studio,String subject,String student_name){

		try {

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");//设置日期格式
			String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
			if (class_number == null || class_number.isEmpty() || "undefined".equals(class_number)){
				class_number = "无班号";
			}

			Arrangement arrangement =new Arrangement();
			arrangement.setDayofweek(dayofweek);
			arrangement.setClass_number(class_number);
			arrangement.setDuration(duration);
			arrangement.setLimits(limits);
			arrangement.setStudio(studio);
			arrangement.setSubject(subject);
			loginService.insertArrangement(arrangement);
			if (student_name != null && !student_name.isEmpty() & !"undefined".equals(student_name)) {
				String add_date = null;
				if (dayofweek.equals("1")) {
					add_date = "2022-05-02";
				} else if (dayofweek.equals("2")) {
					add_date = "2022-05-03";
				} else if (dayofweek.equals("3")) {
					add_date = "2022-05-04";
				} else if (dayofweek.equals("4")) {
					add_date = "2022-05-05";
				} else if (dayofweek.equals("5")) {
					add_date = "2022-05-06";
				} else if (dayofweek.equals("6")) {
					add_date = "2022-05-07";
				} else if (dayofweek.equals("7")) {
					add_date = "2022-05-08";
				}

				Schedule schedule = new Schedule();
				List<String> list = Arrays.asList(student_name.split(" "));
				for (int i = 0; i < list.size(); i++) {
					String list_student = list.get(i);
					schedule.setAdd_date(add_date);
					schedule.setAge("3-6");
					schedule.setStudent_name(list_student);
					schedule.setDuration(duration);
					schedule.setCreate_time(create_time);
					schedule.setUpdate_time(create_time);
					schedule.setStudio(studio);
					schedule.setClass_number(class_number);
					schedule.setStudent_type("ordinary");
					schedule.setStatus(1);
					schedule.setSubject(subject);
					loginService.insertSchedule(schedule);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/insertOrder")
	@ResponseBody
	public int insertOrder(String nick_name,String openid,String phone_number,String location,String goods_name,String goods_intro ,String goods_price,String studio){

		try {

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
			String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

			Order order = new Order();
			order.setNick_name(nick_name);
			order.setOpenid(openid);
			order.setPhone_number(phone_number);
			order.setLocation(location);
			order.setGoods_name(goods_name);
			order.setGoods_intro(goods_intro);
			order.setGoods_price(Float.parseFloat(goods_price));
			order.setStudio(studio);
			order.setCreate_time(create_time);

			loginService.insertOrder(order);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 1;
	}

	@RequestMapping("/deleteLesson")
	@ResponseBody
	public int deleteLesson(Integer id,String role,String studio,String openid,String student_name){
		try {
			loginService.deleteLesson(id,role,studio,openid,student_name);
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
		String class_name =  request.getParameter("class_name");
		String class_target =  request.getParameter("class_target");



		//获取类路径
		String p_path = null;
		String path = System.getProperty("user.dir");
		UUID uuid = UUID.randomUUID();
		if("相框模板".equals(class_target)){
			 p_path = path +"/uploadimages/"+ class_name + ".png";
		}else if("录音文件".equals(class_target)){
			p_path = path +"/uploadMP3/"+ uuid + ".mp3";
		}else {
			p_path = path +"/uploadimages/"+ uuid + ".png";
		}

		//保存图片
		try {
			multipartFile.transferTo(new File(p_path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p_path;
	}

	//	推送图片
	@RequestMapping("/push_video")
	@ResponseBody
	public String push_video(HttpServletRequest request, HttpServletResponse response){
		String path = System.getProperty("user.dir");

		//获取图片
		MultipartHttpServletRequest req = (MultipartHttpServletRequest)request;
		MultipartFile multipartFile = req.getFile("video");
		String studio =  request.getParameter("studio");

		String d_path = path +"/uploadVideo/"+ studio + "/" ;
		File file = new File(d_path);

		if (!file.exists()){ //如果不存在
			boolean dr = file.mkdirs(); //创建目录
		}


		String[] content = file.list();//取得当前目录下所有文件和文件夹
		for(String name : content){
			File temp = new File(d_path, name);
			temp.delete();
		}


		//获取类路径
		String p_path = null;
		UUID uuid = UUID.randomUUID();
		p_path = path +"/uploadVideo/"+ studio + "/" + uuid + ".mp4";

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

	//	推送文件
	@RequestMapping("/push_excel")
	@ResponseBody
	public String push_excel(HttpServletRequest request, HttpServletResponse response){

		//获取图片
		MultipartHttpServletRequest req = (MultipartHttpServletRequest)request;
		MultipartFile multipartFile = req.getFile("file");
		String file_name =  request.getParameter("file_name");
		String studio =  request.getParameter("studio");

		//获取类路径
		String path = System.getProperty("user.dir");
		String path_1 = path + "/uploadexcel/" + studio;
		String p_path = path +"/uploadexcel/" + studio +"/"+ file_name;


		try {
			java.io.File myFilePath = new java.io.File(path_1);
			String[] tempList = myFilePath.list();
			File temp = null;
			if(!myFilePath.exists()){
				myFilePath.mkdir();
			}else {
				for (int i = 0; i < tempList.length; i++) {
					temp = new File(path_1 + "/" + tempList[i]);
					temp.delete();
				}
			}

			multipartFile.transferTo(new File(p_path));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return p_path;
	}

	@RequestMapping("/get_file")
	@ResponseBody
	public ResponseEntity<byte[]> get_file(HttpServletRequest request, HttpServletResponse response) throws IOException{
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

	@RequestMapping("/get_video")
	@ResponseBody
	public String get_video(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String file_name = null;
		String studio =  request.getParameter("studio");
		String path = System.getProperty("user.dir");
		String p_path = path +"/uploadVideo/"+ studio + "/";
		System.out.printf("path:" + p_path);
		File file = new File(p_path);
		if(file.exists()){
			 String[] content = file.list();
			 file_name = content[0];
			return file_name;
		}else{
			return "文件不存在";
		}
	}

	@RequestMapping("/get_frame")
	@ResponseBody
	public ResponseEntity<byte[]> get_frame(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String class_name =  request.getParameter("class_name");
		String path = System.getProperty("user.dir");
		String p_path = path +"/uploadimages/"+ class_name;
		File file = new File(p_path);
		if(file.exists()){
			org.springframework.http.HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("class_name", file.getName());
			return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),headers, HttpStatus.OK);
		}else{
			System.out.println("文件不存在,请重试...");
			return null;
		}
	}

	@RequestMapping("/get_MP3")
	@ResponseBody
	public ResponseEntity<byte[]> get_MP3(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String file_name =  request.getParameter("file_name");
		String path = System.getProperty("user.dir");
		String p_path = path +"/uploadMP3/"+ file_name;
		File file = new File(p_path);
		if(file.exists()){
			org.springframework.http.HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("file_name", file.getName());
			return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),headers, HttpStatus.OK);
		}else{
			System.out.println("文件不存在,请重试...");
			return null;
		}
	}

	@RequestMapping("/submit_batch")
	@ResponseBody
	public String submit_batch(String studio) throws IOException{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
		try {
			cal.setTime(df.parse(create_time));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		String subject =null;
		String student_name =null;
		String total_amount= null;
		String left_amount= null;
		String gift_name =null;
		String gift_amount = null;
		String path = System.getProperty("user.dir");
		String path_1 = path +"/uploadexcel/" + studio ;
		java.io.File myFilePath = new java.io.File(path_1);
		String[] tempList = myFilePath.list();
		File temp = new File(path_1 + "/" + tempList[0]);
		try {
			Workbook book=Workbook.getWorkbook(temp);
			Sheet sheet=book.getSheet(0);
			for(int i=1;i<sheet.getRows();i++){
				Gift gift = new Gift();
				gift.setCreate_time(create_time);
				gift.setStudio(studio);
				gift.setStatus(0);

				Lesson lesson =new Lesson();
				lesson.setCreate_time(create_time);
				lesson.setStudio(studio);
				lesson.setMinus(1.0f);
				lesson.setCoins(0.0f);
				lesson.setPoints(0);
				Cell cell_get=sheet.getCell(0, i);
				if(!cell_get.getContents().isEmpty()){
					for(int j=0;j<sheet.getColumns();j++){
						Cell cell=sheet.getCell(j, i);
						if(0==j){
							subject = cell.getContents();
							lesson.setSubject(subject);
						}else if(1==j){
							student_name = cell.getContents();
							lesson.setStudent_name(student_name);
							gift.setStudent_name(student_name);
						}else if(2==j){
							total_amount =cell.getContents();
							if(!total_amount.isEmpty()){
								lesson.setTotal_amount(Float.parseFloat(total_amount));
							}
						}else if(3==j){
							left_amount = cell.getContents();
							if(!left_amount.isEmpty()){
								lesson.setLeft_amount(Float.parseFloat(left_amount));
							}
						}else if (4==j){
							gift_name = cell.getContents();
							gift.setGift_name(gift_name);
						}else if(5==j){
							gift_amount =cell.getContents();
							if(!gift_amount.isEmpty()){
								gift.setGift_amount(Integer.parseInt(gift_amount));
							}
						}else if(6==j){
							String expired_days = null;
							expired_days = cell.getContents();
							if(!expired_days.isEmpty()){
								Calendar cal_get = Calendar.getInstance();
								SimpleDateFormat df_get = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
								cal_get.add(cal_get.DATE,Integer.parseInt(expired_days));
								String expired_time = df_get.format(cal_get.getTime());
								gift.setExpired_time(expired_time);
							}
						}
					}
				}

				try {
					List<Lesson> lessons_get = dao.getLessonByName(student_name,studio);
					if(lessons_get.isEmpty()){
						loginService.insertLesson(lesson);
					}else {
						loginService.updateLesson(lesson,0.0f);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (!gift.getGift_name().isEmpty()){
					loginService.insertGift(gift);
				}
			}

		} catch (BiffException e) {
			e.printStackTrace();
		}

		return "push massage successfully";
	}

	//	推送
	@RequestMapping("/push")
	@ResponseBody
	public String push(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		//获取积极度
		String positive_get = request.getParameter("positive");
		if(positive_get == null || positive_get.isEmpty() || "undefined".equals(positive_get)){
			positive_get = "1";
		}
		Integer positive = Integer.parseInt(positive_get);


		//获取纪律性
		String discipline_get = request.getParameter("discipline");
		if(discipline_get == null || discipline_get.isEmpty() || "undefined".equals(discipline_get)){
			discipline_get = "1";
		}
		Integer discipline = Integer.parseInt(discipline_get);

		//获取开心值
		String happiness_get = request.getParameter("happiness");
		if(happiness_get == null || happiness_get.isEmpty() || "undefined".equals(happiness_get)){
			happiness_get = "1";
		}
		Integer happiness = Integer.parseInt(happiness_get);


		//获取音频路径
		String mp3_url = request.getParameter("mp3_url");
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
		//获取课堂时间
		String duration = request.getParameter("duration");
		if(duration == null || duration.isEmpty() || "undefined".equals(duration)){
			duration = "00:00-00:00";
		}

		//获取时间
		String date_time = request.getParameter("date_time");
		if(date_time == null || date_time.isEmpty() || "undefined".equals(date_time)){
			date_time = create_time;
		}else {
			date_time = date_time + " 00:00:00";
		}

		String class_target_bak = class_target;
		try {
			String class_target_bak_get = request.getParameter("class_target_bak");
			if(!class_target_bak_get.isEmpty()){
				class_target_bak = class_target_bak_get;
			}
		} catch (Exception e) {
//			e.printStackTrace();
		}

		String studio = request.getParameter("studio");

		FileInputStream in = null;
		try {
			Message message =new Message();

			message.setComment(comment);
			message.setStudent_name(student_name);
			message.setCreate_time(date_time);
			message.setClass_name(class_name);
			message.setClass_target(class_target);
			message.setClass_target_bak(class_target_bak);
			message.setStudio(studio);
			message.setDuration(duration);
			message.setPositive(positive);
			message.setDiscipline(discipline);
			message.setHappiness(happiness);
			message.setMp3_url(mp3_url);

			if(!"奖状".equals(class_target)){
				in = Imageutil.readImage(photo);
				message.setPhoto(FileCopyUtils.copyToByteArray(in));
				if("礼品乐园".equals(class_target)){
					dao.deleteStudentPhoto(student_name,studio);
				}
				if("主页".equals(class_target)){
					dao.deleteHome(studio);
				}
				loginService.push(message);
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
	@RequestMapping("/insertSchedule")
	@ResponseBody
	public String insertSchedule(HttpServletRequest request, HttpServletResponse response){
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

		//获取学生类型
		String student_type = request.getParameter("student_type");

		String studio = request.getParameter("studio");

		String class_number = request.getParameter("class_number");
		if(class_number == null || class_number.isEmpty() || "undefined".equals(class_number)){
			class_number = "无班号";
		}

		String subject = request.getParameter("subject");
		if(subject == null || subject.isEmpty() || "undefined".equals(subject)){
			subject = "美术";
		}

		Integer status = 1;

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
				schedule.setStudent_type(student_type);
				schedule.setStatus(status);
				schedule.setClass_number(class_number);
				schedule.setSubject(subject);
				loginService.insertSchedule(schedule);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	//	推送
	@RequestMapping("/insertGoodsList")
	@ResponseBody
	public String insertGoodsList(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		//获取商品名称
		String goods_name = request.getParameter("goods_name");
		//获取商品简介
		String goods_intro = request.getParameter("goods_intro");
		//获取商品价格
		String goods_price = request.getParameter("goods_price");

		String studio = request.getParameter("studio");

		String photo = request.getParameter("photo");
		FileInputStream in = null;


		GoodsList goodsList =new GoodsList();
		try {
			in = Imageutil.readImage(photo);
			goodsList.setPhoto(FileCopyUtils.copyToByteArray(in));
			goodsList.setGoods_name(goods_name);
			goodsList.setGoods_intro(goods_intro);
			goodsList.setGoods_price(Float.parseFloat(goods_price));
			goodsList.setStudio(studio);
			goodsList.setCreate_time(create_time);

			loginService.insertGoodsList(goodsList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	//	推送
	@RequestMapping("/arrangeClass")
	@ResponseBody
	public String arrangeClass(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String add_date = null;
		String class_select = request.getParameter("class_select");
		String[] list_get = class_select.split(",");
		String weekofday = list_get[0];

		if(weekofday.equals("星期1")){
			add_date = "2022-05-02";
		}else if(weekofday.equals("星期2")){
			add_date = "2022-05-03";
		}else if(weekofday.equals("星期3")){
			add_date = "2022-05-04";
		}else if(weekofday.equals("星期4")){
			add_date = "2022-05-05";
		}else if(weekofday.equals("星期5")){
			add_date = "2022-05-06";
		}else if(weekofday.equals("星期6")){
			add_date = "2022-05-07";
		}else if(weekofday.equals("星期7")){
			add_date = "2022-05-08";
		}

		String class_number = list_get[1];
		String duration = list_get[2];
		String subject = list_get[3];

		String studio = request.getParameter("studio");

		//获取名字
		String student_name = request.getParameter("student_name");


		String status =request.getParameter("status") ;
		if(status == null || status.isEmpty() || "undefined".equals(status)){
			status = "1";
		}

		Schedule schedule =new Schedule();
		List<String> list = Arrays.asList(student_name.split(" "));
		try {
			for (int i=0; i < list.size();i++){
				String list_student = list.get(i);
				schedule.setAdd_date(add_date);
				schedule.setAge("3-6");
				schedule.setStudent_name(list_student);
				schedule.setDuration(duration);
				schedule.setCreate_time(create_time);
				schedule.setUpdate_time(create_time);
				schedule.setStudio(studio);
				schedule.setClass_number(class_number);
				schedule.setStudent_type("ordinary");
				schedule.setStatus(Integer.parseInt(status));
				schedule.setSubject(subject);
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
		if(openid.length()!=28){
			return "invalid openid！！";
		}
		//获取 avatarurl
		String avatarurl = request.getParameter("avatarurl");

		String studio = request.getParameter("studio");
		List<User> list_send = dao.getUserSendTime(studio);
		String send_time = "12:00:00";
		if(list_send.size()>0){
			send_time = list_send.get(0).getSend_time();
		}

        //获取 comment_style
		String comment_style = "public";
		List<User> list_u = dao.getComentStyle(studio);
		if(list_u.size()>0){
			comment_style = list_u.get(0).getComment_style();
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
			user.setSend_time(send_time);
			int res = loginService.updateUser(user);
			if(0==res){
				user.setUser_type("新用户");
				user.setComment_style(comment_style);
				user.setSend_time(send_time);
				loginService.insertUser(user);
			}else if(res>0&&!student_name.equals("no_name")){
				List<User> list= dao.getUser(openid);
				String user_type_get = list.get(0).getUser_type();
				String role_get = list.get(0).getRole();
				user.setUser_type(user_type_get);
				user.setRole(role_get);
				user.setComment_style(comment_style);
				user.setSend_time(send_time);
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
		cal.add(cal.DATE,31);
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

	@RequestMapping("/updateSubject")
	@ResponseBody
	public String updateSubject(HttpServletRequest request, HttpServletResponse response){

		//获取openid
		String openid = request.getParameter("openid");
		String subjects = request.getParameter("subjects");

		//获取用户类型

		try {
			User user =new User();
			user.setOpenid(openid);
			user.setSubjects(subjects);
			dao.updateSubject(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	@RequestMapping("/updateTheme")
	@ResponseBody
	public String updateTheme(String theme, String openid){

		try {
			dao.updateTheme(theme,openid);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "push massage successfully";
	}

	@RequestMapping("/updatVideoDisplay")
	@ResponseBody
	public String updatVideoDisplay(String studio,Integer display){
		try {
			dao.updatVideoDisplay(studio,display);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "push massage successfully";
	}

	@RequestMapping("/updatCoverDisplay")
	@ResponseBody
	public String updatCoverDisplay(String studio,Integer cover){
		try {
			dao.updatCoverDisplay(studio,cover);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "push massage successfully";
	}

	@RequestMapping("/updateComentStyle")
	@ResponseBody
	public String updateComentStyle(HttpServletRequest request, HttpServletResponse response){
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

	@RequestMapping("/updateSendTime")
	@ResponseBody
	public String updateSendTime(HttpServletRequest request, HttpServletResponse response){
		//获取openid
		String openid = request.getParameter("openid");

		String studio = request.getParameter("studio");

		String send_time = request.getParameter("send_time");


		try {
			User user =new User();
			user.setSend_time(send_time);
			user.setStudio(studio);
			dao.updateSendTime(user);
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


		// 获取工作室
		String studio = request.getParameter("studio");

		//获取用户名
		String student_name = request.getParameter("student_name");

		//获取用户名
		String subject = request.getParameter("subject");

		// 发放开课礼物
		String gift_name = request.getParameter("gift_name");
		if (!gift_name.isEmpty()){
			Gift gift = new Gift();
			try {
				gift_name = request.getParameter("gift_name");
				if (!gift_name.equals("nobody")){
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
				} catch (Exception e) {
//					e.printStackTrace();
					logger.error("sent gift error");
				}
			}

		try {
			// 获取单次积分
			String coins_amount_get_1 = request.getParameter("coins_amount");
			Float coins_amount_get = 0.0f;
			if (!coins_amount_get_1.isEmpty()){
				coins_amount_get = Float.valueOf(coins_amount_get_1);
			}
			// 获取单次扣课
			String minus_amount_get_1 = request.getParameter("minus_amount");
			Float minus_amount_get = 0.0f;
			if (!minus_amount_get_1.isEmpty()){
				minus_amount_get = Float.valueOf(minus_amount_get_1);
			}
			// 获取新名字
			String student_name_new = request.getParameter("student_name_new");
			//获取总课时
			String total_amount_1 = request.getParameter("total_amount");
			Float total_amount = 0.0f;
			if (!total_amount_1.isEmpty()){
				total_amount = Float.valueOf(total_amount_1);
			}
			// 获取type
			String modify_type = request.getParameter("modify_type");

			// 获取type
			String lessons_amount_1 = request.getParameter("lessons_amount");
			Float lessons_amount = 0.0f;
			if (!lessons_amount_1.isEmpty()){
				lessons_amount = Float.valueOf(lessons_amount_1);
			}

			Float minus_amount=0.0f;
			Float coins_amount=0.0f;
			List<Lesson> lessons_get = dao.getLessonByName(student_name,studio);
			if(!lessons_get.isEmpty()){
				// 获取单扣课
				minus_amount = lessons_get.get(0).getMinus();
				coins_amount = lessons_get.get(0).getCoins();
			}
			if(minus_amount_get != 100){
				minus_amount = minus_amount_get;
			}
			if(coins_amount_get != 100){
				coins_amount = coins_amount_get;
			}

			//获年剩余课时
			String left_amount_get = request.getParameter("left_amount");
			Float left_amount =null;
			if(left_amount_get.isEmpty()){
				left_amount = -1.0f;
			}else {
				left_amount = Float.parseFloat(left_amount_get);
			}

			Lesson lesson =new Lesson();
			lesson.setStudent_name(student_name);
			lesson.setTotal_amount(total_amount);
			lesson.setLeft_amount(left_amount);
			lesson.setCreate_time(create_time);
			lesson.setStudio(studio);
			lesson.setMinus(minus_amount);
			lesson.setCoins(coins_amount);
			lesson.setSubject(subject);

			List<Lesson> lessons = dao.getLessonByName(student_name, studio);
			if(!student_name_new.isEmpty()){
				dao.updateScheduleName(student_name_new,student_name,studio);
				dao.updateCommentName(student_name_new,student_name,studio);
				dao.updateGiftRecordName(student_name_new,student_name,studio);
				dao.updateLessonName(student_name_new,student_name,studio);
				dao.updateSignUpRecordName(student_name_new,student_name,studio);
				dao.updateUserStudent(student_name_new,student_name,studio);
			}else if(lessons.size()>0){
				if("coins_modify_all".equals(modify_type)){
					dao.updateLessonAll(coins_amount,studio);
				}else {
					loginService.updateLesson(lesson,lessons_amount);
				}

			}else {
				Integer point = 0;
				Float minus_amount_t = 1.0f;
				Float coins_amount_t = 0.0f;
				lesson.setPoints(point);
				lesson.setMinus(minus_amount_t);
				lesson.setCoins(coins_amount_t);
				loginService.insertLesson(lesson);
			}
		} catch (Exception e) {
//			e.printStackTrace();
			logger.error("update lesson error");
		}

		return "push massage successfully";
	}


	@RequestMapping("/updateLessonPoints")
	@ResponseBody
	public String updateLessonPoints(HttpServletRequest request, HttpServletResponse response){
		//获取用户名
		String student_name = request.getParameter("student_name");
		String studio = request.getParameter("studio");
		String points = request.getParameter("points");
		Integer points_int = Integer.parseInt(points);

		try {
			loginService.updateAddPoints(student_name,studio,points_int);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	@RequestMapping("/updateUserPay")
	@ResponseBody
	public String updateUserPay(HttpServletRequest request, HttpServletResponse response){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		//获取用户名
		String expired_time = request.getParameter("expired_time");
		String studio = request.getParameter("studio");

		User user=new User();
		user.setExpired_time(expired_time);
		user.setStudio(studio);
		user.setCreate_time(create_time);
		user.setUser_type("老用户");
		user.setRole("boss");

		try {
			dao.updateUserPay(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	@RequestMapping("/updateBossLessons")
	@ResponseBody
	public String updateBossLessons(HttpServletRequest request, HttpServletResponse response){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		//获取用户名
		String inputdefault = request.getParameter("inputdefault");
		String openid = request.getParameter("openid");
		String chooselesson = request.getParameter("chooselesson");

		try {
			User user_get= dao.getUser(openid).get(0);
			String lessons = user_get.getLessons();
			HashSet<String> hs = new HashSet<>();
			if(lessons != null){
				String[] list_1 =lessons.split("\\|");
				List<String> list_2 = Arrays.asList(list_1);
				for(String lesson :list_2){
					if(!inputdefault.equals(lesson)){
						hs.add(lesson);
					}
				}
				if(chooselesson.equals("未选")){
					for (String i : hs){
						inputdefault = inputdefault + "|" + i;
					}

				}else if(chooselesson.equals("已选")){
					inputdefault = "1";
					for (String i : hs){
						inputdefault = inputdefault + "|" + i;
					}
					if(inputdefault.length()>2){
						inputdefault =inputdefault.substring(2);
					}else {
						inputdefault=null;
					}

				}

			}

			User user=new User();
			user.setOpenid(openid);
			user.setLessons(inputdefault);
			loginService.updateBossLessons(user);
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

	@RequestMapping("/updateStudentName")
	@ResponseBody
	public String updateStudentName(HttpServletRequest request, HttpServletResponse response){
		//获取用户名
		String student_name_new = request.getParameter("student_name_new");
		String student_name = request.getParameter("student_name");
		String studio = request.getParameter("studio");

		try {
			dao.updateScheduleName(student_name_new,student_name,studio);
			dao.updateCommentName(student_name_new,student_name,studio);
			dao.updateGiftRecordName(student_name_new,student_name,studio);
			dao.updateLessonName(student_name_new,student_name,studio);
			dao.updateSignUpRecordName(student_name_new,student_name,studio);
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

	@RequestMapping("/cancelSignUp")
	@ResponseBody
	public String cancelSignUp(HttpServletRequest request, HttpServletResponse response){
		//获取用户名

		String id = request.getParameter("id");
		String role = request.getParameter("role");
		String studio = request.getParameter("studio");
		String openid = request.getParameter("openid");
		String student_name = request.getParameter("student_name");
		String count = request.getParameter("count");

		List<User> list = dao.getUser(openid);
		String studio_get = list.get(0).getStudio();

		if ("boss".equals(role) && studio_get.equals(studio)) {
			try {
				loginService.deleteSignUpRecord(Integer.parseInt(id),role,studio,openid);
				loginService.updateMinusLesson(student_name,studio,-Float.parseFloat(count));

				List<Lesson> list1 = dao.getLessonByName(student_name, studio);
				Float coins = list1.get(0).getCoins();
				loginService.updateAddPoints(student_name,studio,-Math.round(coins));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}else {
			logger.error("it's not your studio, could not delete!");
		}

		return "push massage successfully";
	}

}
	
	


