package com.xue.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSONArray;
import com.google.gson.Gson;
import com.xue.JsonUtils.JsonUtils;
import com.xue.entity.model.*;
//import com.xue.entity.model.Subscription;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;
import com.xue.service.WebPushService;
import com.xue.util.HttpUtil;
import com.xue.util.Imageutil;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.aspectj.weaver.ast.Or;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.BouncyCastleProviderHelp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.Security;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.commons.codec.digest.DigestUtils;
import nl.martijndwars.webpush.*;

@Controller
public class LoginController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String tample1 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"5kASxPkT-RY5K-ti-iFh924Xgd97858juXOjynsTWVo\",\"data\":{\"thing6\":{\"value\": \"classname\"},\"name3\":{\"value\": \"studentname\"},\"thing4\":{\"value\": \"来看看小朋友的表现吧\"},\"date5\":{\"value\": \"mytime\"}}}";
	private static final String tample2 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"wi0QTuLQiYFUbCWGk2eP_KJIviqnDVh8XVq364Q3704\",\"data\":{\"thing5\":{\"value\": \"AA\"},\"date2\":{\"value\": \"AA\"},\"thing3\":{\"value\": \"AA\"},\"thing6\":{\"value\": \"process\"},\"thing8\":{\"value\": \"process\"}}}";
	private static final String tample3 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"3BPMQuajTekT04oI8rCTKMB2iNO4XWdlDiMqR987TQk\",\"data\":{\"date1\":{\"value\": \"2022-11-01 10:30-11:30\"},\"thing2\":{\"value\": \"A1\"},\"name3\":{\"value\": \"小明\"},\"thing5\":{\"value\": \"记得来上课哦\"}}}";
	private static final String tample4 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"2-SXAUTnbKpaHkY-xZCzFlgdPL9iKW3Bc9Z0HuB79UE\",\"data\":{\"thing1\":{\"value\": \"AA\"},\"thing5\":{\"value\": \"A1\"},\"thing4\":{\"value\": \"A1\"},\"time3\":{\"value\": \"time\"}}}";
	private static final String tample5 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"vKZCYXHkl8p4vcE6A8CmjwBbRNYcUa2oJ29gfKMEOEk\",\"data\":{\"thing1\":{\"value\": \"AA\"},\"short_thing2\":{\"value\": \"A1\"},\"short_thing3\":{\"value\": \"A1\"},\"thing5\":{\"value\": \"time\"},\"time4\":{\"value\": \"time\"}}}";
	private static final String tample6 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"JMbp5CzEiKiI7Xt7vxOO4RU8DSD8L3lm5sjlqtjv_Ys\",\"data\":{\"thing12\":{\"value\": \"AA\"},\"thing16\":{\"value\": \"A1\"},\"amount13\":{\"value\": \"A1\"},\"thing20\":{\"value\": \"time\"}}}";
	private static final String tample7 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"UlD842xsgrqPJsmr5jfPi3tA_wUcosee-YPFer0QcE0\",\"data\":{\"thing1\":{\"value\": \"AA\"},\"thing2\":{\"value\": \"A1\"},\"time3\":{\"value\": \"A1\"},\"thing5\":{\"value\": \"time\"}}}";
	private static final String tample8 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"RToa1-R_dkD_V-xaUGGpp0zPRE_hG2pRaKyX5Rq_Pi8\",\"data\":{\"thing1\":{\"value\": \"AA\"},\"phone_number5\":{\"value\": \"A1\"},\"thing2\":{\"value\": \"A1\"},\"time3\":{\"value\": \"time\"}}}";
	private static final String tample9 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"sUmPrHqHMJ4r_areM2Dwpyi-sK-A6ehYwrMyS9JS-Qw\",\"data\":{\"thing5\":{\"value\": \"AA\"},\"name11\":{\"value\": \"A1\"},\"thing8\":{\"value\": \"A1\"},\"date6\":{\"value\": \"time\"}}}";

	@Autowired
	private LoginService loginService;

	@Autowired
	private WebPushService webPushService;

	@Autowired
	private UserMapper dao;

	@RequestMapping("/sendConsumeLesson")
	@ResponseBody
	public String sendConsumeLesson(String token, String openid,String studio, String consume_lesson_amount,String student_name, String mytime,String mark,String subject){
		String result = null;
		String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
		JSONObject queryJson = JSONObject.parseObject(tample5);
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();

		List<Lesson> lessons = dao.getLessonByName(student_name, studio,campus);
		int left_amount = 0;
		if(lessons.size()>0){
			Float left_amount_get = lessons.get(0).getLeft_amount();
			left_amount = Math.round(left_amount_get);
		}

		queryJson.put("touser",openid);
		queryJson.getJSONObject("data").getJSONObject("thing1").put("value",subject+"_"+student_name);
		queryJson.getJSONObject("data").getJSONObject("short_thing2").put("value",consume_lesson_amount+"课时");
		queryJson.getJSONObject("data").getJSONObject("short_thing3").put("value",left_amount+"课时");
		queryJson.getJSONObject("data").getJSONObject("thing5").put("value",mark);
		queryJson.getJSONObject("data").getJSONObject("time4").put("value",mytime);

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
	@RequestMapping("/sendPostRemind")
	@ResponseBody
	public String sendPostRemind(String token, String openid, String classname,String studentname, String mytime){
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


	@RequestMapping("/chat")
	@ResponseBody
	public static String chat(String question){
		Map<String, String> header = new HashMap<String, String>();
		header.put("Content-Type", "application/json");
		header.put("Authorization", "Bearer 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");
		JSONObject params = new JSONObject();
		params.put("model", "text-davinci-003");
		params.put("prompt", question);
		params.put("temperature", 0.9);
		params.put("max_tokens", 2048);
		params.put("top_p", 1);
		params.put("frequency_penalty", 0.0);
		params.put("presence_penalty", 0.6);

		JSONArray stop = new JSONArray();
		stop.add("<br>");
		params.put("stop", stop);

		return JsonUtils.doPost("https://api.openai.com/v1/completions", header, params);
	}


	//	获取token
	@RequestMapping("/sendSignUpRemind")
	@ResponseBody
	public String sendSignUpRemind(String token, String openid, String total, String left,String student_name,String date_time,String class_count,String studio,String subject){
		String result = null;
		String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
		JSONObject queryJson = JSONObject.parseObject(tample2);
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();

		List<Lesson> lessons = dao.getLessonByNameSubject(student_name, studio,subject,campus);
		Float count = 0.0f;
		if(lessons.size()>0){
			count = lessons.get(0).getMinus();
		}

		if(Float.parseFloat(class_count) != 100){
			count = Float.parseFloat(class_count);
		}

		String thing8 = "本次扣课" + count + "课时，总课时" + total + "课时";

		queryJson.put("touser",openid);
		queryJson.getJSONObject("data").getJSONObject("thing5").put("value",subject+"_"+student_name);
		queryJson.getJSONObject("data").getJSONObject("date2").put("value",date_time);
		queryJson.getJSONObject("data").getJSONObject("thing3").put("value","签到成功");
		queryJson.getJSONObject("data").getJSONObject("thing6").put("value",left + "课时");
		queryJson.getJSONObject("data").getJSONObject("thing8").put("value",thing8);
		queryJson.put("page","/pages/signuprecord/signuprecord?student_name=" + student_name + "&studio=" + studio + "&subject=" + subject);


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
	@RequestMapping("/sendNotice")
	@ResponseBody
	public String sendNotice(String token, String openid, String studio, String title,String content,String mytime){
		String result = null;
		String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
		JSONObject queryJson = JSONObject.parseObject(tample4);

		queryJson.put("touser",openid);
		queryJson.getJSONObject("data").getJSONObject("thing1").put("value",title);
		queryJson.getJSONObject("data").getJSONObject("thing5").put("value",content);
		queryJson.getJSONObject("data").getJSONObject("thing4").put("value",studio);
		queryJson.getJSONObject("data").getJSONObject("time3").put("value",mytime);
		queryJson.put("page","/pages/event/event?share_studio=" + studio);


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
	@RequestMapping("/sendPaymentNotice")
	@ResponseBody
	public String sendPaymentNotice(String token, String openid, String studio, String amount,String days){
		String result = null;
		String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
		JSONObject queryJson = JSONObject.parseObject(tample6);

		List<User> list = dao.getUser(openid);
		String nick_name = list.get(0).getNick_name();

		queryJson.put("touser","oRRfU5TCmjXtbw9WsxnekwJAa72M");
		queryJson.getJSONObject("data").getJSONObject("thing12").put("value",studio);
		queryJson.getJSONObject("data").getJSONObject("thing16").put("value",nick_name);
		queryJson.getJSONObject("data").getJSONObject("amount13").put("value",amount);
		queryJson.getJSONObject("data").getJSONObject("thing20").put("value","续费"+ days +"天");

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

	//续费成功通知
	@RequestMapping("/sendFeedback")
	@ResponseBody
	public String sendFeedback(String token, String openid, String studio, String expired_time,String days){
		String result = null;
		String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
		JSONObject queryJson = JSONObject.parseObject(tample7);

		queryJson.put("touser",openid);
		queryJson.getJSONObject("data").getJSONObject("thing1").put("value",studio);
		queryJson.getJSONObject("data").getJSONObject("thing2").put("value",days);
		queryJson.getJSONObject("data").getJSONObject("time3").put("value",expired_time);
		queryJson.getJSONObject("data").getJSONObject("thing5").put("value","续费成功，请刷新状态即可！");

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

	//客户下单通知
	@RequestMapping("/sendOrderNotice")
	@ResponseBody
	public String sendOrderNotice(String token, String openid, String studio, String nick_name,String phone_number,String goods_name,String mytime){
		String result = null;
		String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
		JSONObject queryJson = JSONObject.parseObject(tample8);

		queryJson.put("touser",openid);
		queryJson.getJSONObject("data").getJSONObject("thing1").put("value",nick_name);
		queryJson.getJSONObject("data").getJSONObject("phone_number5").put("value",phone_number);
		queryJson.getJSONObject("data").getJSONObject("thing2").put("value",goods_name);
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

	//客户下单通知
	@RequestMapping("/sendDeliverNotice")
	@ResponseBody
	public String sendDeliverNotice(String token, String openid, String studio, String nick_name,String phone_number,String goods_name,String mytime,String location){
		String result = null;
		String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
		JSONObject queryJson = JSONObject.parseObject(tample9);

		queryJson.put("touser",openid);
		queryJson.getJSONObject("data").getJSONObject("thing5").put("value",goods_name);
		queryJson.getJSONObject("data").getJSONObject("name11").put("value",nick_name);
		queryJson.getJSONObject("data").getJSONObject("thing8").put("value",location);
		queryJson.getJSONObject("data").getJSONObject("date6").put("value",mytime);

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
	public List getMessage(String studio,Integer page,String comment_style,String openid,String role,String class_target){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getMessage(studio,page,comment_style,openid,role,class_target,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getPost")
	@ResponseBody
	public List getPost(String studio,Integer page,String openid,String type){
		List list = null;
		try {
//			List<User> list_user = dao.getUser(openid);
//			String campus = list_user.get(0).getCampus();
			list = loginService.getPost(studio,page,openid,type);
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
	public List getLessonByName(String student_name,String studio,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getLessonByName(student_name,studio,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取学生的课程数
	@RequestMapping("/getLessonByNameSubject")
	@ResponseBody
	public List getLessonByNameSubject(String student_name,String studio,String subject,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getLessonByNameSubject(student_name,studio,subject,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/getLesson")
	@ResponseBody
	public List getLesson(String studio,String student_name,String subject,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getLesson(studio,student_name,subject,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/getLessonByPage")
	@ResponseBody
	public List getLessonByPage(String studio,String student_name,String subject,String openid,Integer page){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getLessonByPage(studio,student_name,subject,campus,page);
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
	public List getLessonInName(String studio,String student_name,Integer page,String subject,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getLessonInName(studio,student_name,page,subject,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取商品列表
	@RequestMapping("/getGoodsList")
	@ResponseBody
	public List getGoodsList(String studio,Integer page,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getGoodsList(studio,page,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取团购列表
	@RequestMapping("/getGroupBuy")
	@ResponseBody
	public List getGroupBuy(String studio,String goods_id){
		List list = null;
		try {
			list = loginService.getGroupBuy(studio,goods_id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/getRating")
	@ResponseBody
	public List getRating(String studio,String student_name,Integer page,String subject,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getRating(studio,student_name,page,subject,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取广告
	@RequestMapping("/getAdvertise")
	@ResponseBody
	public List getAdvertise(String class_target,String studio,Integer page){
		List list = null;
		try {
			list = loginService.getAdvertise(class_target,studio,page);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取账本
	@RequestMapping("/getBook")
	@ResponseBody
	public List getBook(String studio,String dimension,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getBook(studio,dimension,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取账本明细
	@RequestMapping("/getBookDetail")
	@ResponseBody
	public List getBookDetail(String studio, String create_time, String type,String start_date){
		List list = null;
		try {
			list = loginService.getBookDetail(studio,create_time,type,start_date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/searchBookDetail")
	@ResponseBody
	public List searchBookDetail(String studio, String value, String type){
		List list = null;
		try {
			list = loginService.searchBookDetail(studio,value,type);
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
	public List getClassSys(String class_target,String studio,Integer page,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getClassSys(class_target,studio,page,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取主页
	@RequestMapping("/getHome")
	@ResponseBody
	public List getHome(String studio,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getHome(studio,campus);
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
	public List getSchedule(String date_time,String studio,String subject,String openid,String test){
		List list = null;
		try {
			list = loginService.getSchedule(date_time,studio,subject,openid,test);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取课程表
	@RequestMapping("/getScheduleByClass")
	@ResponseBody
	public List getScheduleByClass(String date_time,String duration,String studio,String class_number,String subject,String openid){
		List list = null;
		try {
			list = loginService.getScheduleByClass(date_time,duration,studio,class_number,subject,openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取课程表
	@RequestMapping("/updateRemind")
	@ResponseBody
	public int updateRemind(String subject_class,String studio,String type,String dayofweek,String openid){
		Integer remind = 1;
		if("cancel".equals(type)){
			remind = 0;
		}else if("reremind".equals(type)){
			remind = 1;
		}
		String[] list = subject_class.split(",");
		String subject = list[0];
		String class_number = list[1];
		String duration = list[2];

		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();

		int result = 0;
		try {
			result =dao.updateRemind(remind,subject,studio,duration,class_number,dayofweek,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	//	获取课程表
	@RequestMapping("/getScheduleDetail")
	@ResponseBody
	public List getScheduleDetail(String weekDay,String duration,String studio,String class_number,String subject,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getScheduleDetail(Integer.parseInt(weekDay),duration,studio,class_number,subject,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取范画
	@RequestMapping("/getModel")
	@ResponseBody
	public List getModel(String studio,Integer page,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getModel(studio,page,campus);
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
	public List getSearch(String student_name,String studio,Integer page,String class_target,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getSearch(student_name,studio,page,class_target,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/getSignUp")
	@ResponseBody
	public List getSignUp(String student_name,String studio,String subject){
		List list = null;
		try {
			list = loginService.getSignUp(student_name,studio,subject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/getStudentByTeacher")
	@ResponseBody
	public List getStudentByTeacher(String studio,String openid,String date_start,String date_end){
		List list = null;
		try {
			if(date_end.equals("undefined")){
				list = dao.getStudentByTeacher(studio,openid);
			}else {
				list = dao.getStudentByTeacherByDuration(studio,openid,date_start,date_end);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getAnalyzeDetail")
	@ResponseBody
	public List getAnalyzeDetail(String studio,String dimension,String openid,String date_time){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getAnalyzeDetail(studio,dimension,campus,date_time);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getClassNote")
	@ResponseBody
	public List getClassNote(String subject,String studio,String student_name){
		List list = null;
		try {
			list = dao.getClassNote(subject,studio,student_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getAnnouncement")
	@ResponseBody
	public List getAnnouncement(String studio){
		List list = null;
		try {
			list = dao.getAnnouncement(studio);
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
	public List getLeaveRecord(String student_name,String studio,String leave_type,String subject){
		List list = null;
		try {
			list = loginService.getLeaveRecord(student_name,studio,leave_type,subject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getLeaveByDateDuration")
	@ResponseBody
	public List getLeaveByDateDuration(String student_name,String studio,String date_time,String duration){
		List list = null;
		try {
			list = dao.getLeaveByDateDuration(student_name,studio,date_time,duration);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getSignUpByDateDuration")
	@ResponseBody
	public List getSignUpByDateDuration(String student_name,String studio,String date_time,String duration,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getSignUpByDateDuration(student_name,studio,date_time,duration,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getCommentByDateDuration")
	@ResponseBody
	public List getCommentByDateDuration(String student_name,String studio,String date_time,String duration,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = dao.getCommentByDateDuration(student_name,studio,date_time,duration,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getPostComment")
	@ResponseBody
	public List getPostComment(String post_id){
		List list = null;
		try {
			list = loginService.getPostComment(post_id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getPostLikeByOpenid")
	@ResponseBody
	public List getPostLike(String post_id,String openid){
		List list = null;
		try {
			list = dao.getPostLikeByOpenid(post_id,openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/getArrangement")
	@ResponseBody
	public List getArrangement(String studio,Integer dayofweek,String date,String subject,String openid,String student_name){
		List list = null;
		try {
			list = loginService.getArrangement(studio,dayofweek,date,subject,openid,student_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getTodaySchedule")
	@ResponseBody
	public List getTodaySchedule(String studio,Integer dayofweek,String date,String subject,String openid){
		List list = null;
		try {
			list = loginService.getTodaySchedule(studio,dayofweek,date,subject,openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/changeClass")
	@ResponseBody
	public String changeClass(String studio,String changeday,String duration,String class_number,String weekday,String subject,String openid){
		String result=null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			 result = loginService.changeClass(studio,Integer.parseInt(changeday),duration,class_number,Integer.parseInt(weekday),subject,campus);
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

	@RequestMapping("/modifyGoodsIntro")
	@ResponseBody
	public String modifyGoodsIntro(String goods_id,String goods_intro_modify,String openid,String type){
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			String studio = list_user.get(0).getStudio();
			if("intro".equals(type)){
				dao.modifyGoodsIntro(goods_id,studio,campus,goods_intro_modify);
			}else if("goodsName".equals(type)){
				dao.modifyGoodsName(goods_id,studio,campus,goods_intro_modify);
			}else if("price".equals(type)){
				dao.modifyGoodsPrice(goods_id,studio,campus,goods_intro_modify);
			}

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
	public List getDetailsUrlByDate(String studio, String duration, String student_name, String date_time,String openid){
		List list = null;
		try {
			list = loginService.getDetailsUrlByDate(studio,duration,student_name,date_time,openid);
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

	//	获取用户
	@RequestMapping("/getUserByStudio")
	@ResponseBody
	public List getUserByStudio(String studio,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getUserByStudio(studio,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取用户
	@RequestMapping("/getLessonByStudio")
	@ResponseBody
	public List getLessonByStudio(String studio){
		List list = null;
		try {
			list = loginService.getLessonByStudio(studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getAllUserByStudio")
	@ResponseBody
	public List getAllUserByStudio(String studio){
		List list = null;
		try {
			list = dao.getAllUserByStudio(studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取用户
	@RequestMapping("/getUserByStudent")
	@ResponseBody
	public List getUserByStudent(String student_name,String studio){
		List list = null;
		try {
			list = dao.getUserByStudent(student_name,studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getBossByStudio")
	@ResponseBody
	public List getBossByStudio(String studio){
		List list = null;
		try {
			list = dao.getBossByStudio(studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取用户
	@RequestMapping("/getUserByNickStudio")
	@ResponseBody
	public List getUserByNickStudio(String nick_name,String studio){
		List list = null;
		try {
			list = dao.getUserByNickStudio(nick_name,studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getUserByNickStudioEq")
	@ResponseBody
	public List getUserByNickStudioEq(String nick_name,String studio){
		List list = null;
		try {
			list = dao.getUserByNickStudioEq(nick_name,studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取工作室列表
	@RequestMapping("/getStudio")
	@ResponseBody
	public List getStudio(String role){
		List list = null;
		try {
			list = loginService.getStudio(role);
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
	public List getFrameModel(String studio,Integer page,String class_target,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getFrameModel(studio,page,class_target,campus);
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
			List<User> list = dao.getUser(openid);
			String studio_get = list.get(0).getStudio();

			if ( studio_get.equals(studio)) {
				loginService.deleteComment(id,role,studio,openid);
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
	@RequestMapping("/deleteBookDetail")
	@ResponseBody
	public int deleteBookDetail(Integer id,String role,String studio,String openid){
		try {
			List<User> list = dao.getUser(openid);
			String studio_get = list.get(0).getStudio();

			if (studio_get.equals(studio)) {
				dao.deleteBookDetail(id,studio);
			}else {
				logger.error("it's not your studio, could not delete!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@RequestMapping("/deletePost")
	@ResponseBody
	public int deletePost(Integer id){
		try {
			dao.deletePost(id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	//	获取详情页
	@RequestMapping("/deleteUuids")
	@ResponseBody
	public int deleteUuids(Integer id,String role,String studio,String openid,String uuid){
		try {
			List<User> list = dao.getUser(openid);
			String studio_get = list.get(0).getStudio();

			if (studio_get.equals(studio)) {
				loginService.deleteUuids(id,role,studio,openid,uuid);
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
	@RequestMapping("/deleteNote")
	@ResponseBody
	public int deleteNote(Integer id){
		try {
			dao.deleteNote(id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}


	//	获取详情页
	@RequestMapping("/deleteUser")
	@ResponseBody
	public int deleteUser(Integer id){
		try {
			dao.deleteUser(id);
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

	@RequestMapping("/deleteGroupBuy")
	@ResponseBody
	public int deleteGroupBuy(String goods_id,String nick_name,String studio){
		try {
			dao.deleteGroupBuy(goods_id,nick_name,studio);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	//	获取详情页
	@RequestMapping("/deleteArrangement")
	@ResponseBody
	public int deleteArrangement(Integer id,String role,String studio,String openid,String duration,String weekday,String class_number,String subject){
		try {
			List<User> list = dao.getUser(openid);
			String studio_get = list.get(0).getStudio();

			if (studio_get.equals(studio)) {
				loginService.deleteArrangement(id,role,studio,openid);
				loginService.deleteScheduleByDate(Integer.parseInt(weekday),duration,studio,class_number,role,openid,subject);
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
	public int changeClassName(String id,String role,String studio,String openid,String class_number,String change_title,String limit_number){
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			loginService.changeClassName(id,role,studio,openid,class_number,change_title,limit_number,campus);
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

			if ( studio_get.equals(studio)) {
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

			if (studio_get.equals(studio)) {
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
	public int deleteLeaveAllRecord(String student_name,String studio,String role,String openid,String leave_type,String subject){
		try {
			List<User> list = dao.getUser(openid);
			String studio_get = list.get(0).getStudio();

			if (studio_get.equals(studio)) {
				dao.deleteLeaveAllRecord(student_name, studio,leave_type,subject);
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

			if ( studio_get.equals(studio)) {		
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
			if ( studio_get.equals(studio)) {
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

			if ( studio_get.equals(studio)) {
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

	@RequestMapping("/insertSignUp")
	@ResponseBody
	public int insertSignUp(String openid,String studio, String consume_lesson_amount,String student_name,String mark,String subject){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String update_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		SignUp signUp = new SignUp();
		signUp.setStudent_name(student_name);
		signUp.setStudio(studio);
		signUp.setSign_time(update_time);
		signUp.setMark("划课_"+mark);
		signUp.setCount(Float.parseFloat(consume_lesson_amount));
		signUp.setSubject(subject);
		signUp.setTeacher(openid);
		signUp.setCreate_time(update_time);
		signUp.setDuration("00:00:00");
		signUp.setClass_number("无班号");
		try {
			loginService.insertSignUp(signUp);
		} catch (Exception e) {
//			throw new RuntimeException(e);
		}
		return 1;
	}

	@RequestMapping("/insertAnnouncement")
	@ResponseBody
	public int insertAnnouncement(String studio,String content){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		Announcement announcement = new Announcement();
		announcement.setCreate_time(create_time);
		announcement.setStudio(studio);
		announcement.setContent(content);

		try {
			dao.insertAnnouncement(announcement);
		} catch (Exception e) {
//			throw new RuntimeException(e);
		}

		return 1;

	}


	@RequestMapping("/signUpSchedule")
	@ResponseBody
	public int signUpSchedule(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String update_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String student_name = request.getParameter("student_name");
		String studio = request.getParameter("studio");
		String date_time = request.getParameter("date_time");
		String mark = request.getParameter("mark");
		String class_count = request.getParameter("class_count");
		String duration = request.getParameter("duration");
		String class_number = request.getParameter("class_number");
		String subject = request.getParameter("subject");
		String openid = request.getParameter("openid");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();


		try {
			Schedule schedule =new Schedule();
			SignUp signUp = new SignUp();
			schedule.setStudent_name(student_name);
			schedule.setStudio(studio);
			schedule.setUpdate_time(update_time);
			loginService.updateSchedule(schedule);

			List<Lesson> lessons = dao.getLessonByNameSubject(student_name, studio,subject,campus);
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
			signUp.setTeacher(openid);
			signUp.setCampus(campus);
			if(class_number == null || class_number.isEmpty() || "undefined".equals(class_number)){
				class_number = "无班号";
			}
			signUp.setClass_number(class_number);

			int insert_res = loginService.insertSignUp(signUp);
			if(insert_res>0){
				loginService.updateMinusLesson(student_name,studio,count,subject,campus);
				loginService.updateAddPoints(student_name,studio,coins,subject,campus);
			}


		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/signUpScheduleClass")
	@ResponseBody
	public int signUpScheduleClass(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String update_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		Date d = null;
		String student_name = null;
		String mark = "无备注";

		String studio = request.getParameter("studio");
		String date_time = request.getParameter("date_time");
		String class_count = request.getParameter("class_count");
		String duration = request.getParameter("duration");
		String class_number = request.getParameter("class_number");
		String subject = request.getParameter("subject");
		String openid = request.getParameter("openid");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();


		try {
			d = fmt.parse(date_time);
			Calendar cal = Calendar.getInstance();
			cal.setTime(d);
			Integer weekDay = cal.get(Calendar.DAY_OF_WEEK);

			List<Schedule> schedules = dao.getScheduleDetail(weekDay,duration,studio,class_number,subject,campus);
			for(int i = 0;i < schedules.size(); i++){
				student_name = schedules.get(i).getStudent_name();
				Schedule schedule =new Schedule();
				SignUp signUp = new SignUp();
				schedule.setStudent_name(student_name);
				schedule.setStudio(studio);
				schedule.setUpdate_time(update_time);
				schedule.setCampus(campus);
				loginService.updateSchedule(schedule);

				List<Lesson> lessons = dao.getLessonByNameSubject(student_name, studio,subject,campus);
				Float count = 0.0f;
				Integer coins = 0;
				if(lessons.size()>0){
					count = lessons.get(0).getMinus();
					Float coins_get = lessons.get(0).getCoins();
					coins = Math.round(coins_get);
				}

				List<User> users = dao.getUser(openid);
				String teacher = "无";
				if(users.size()>0){
					teacher = users.get(0).getNick_name();
				}

				signUp.setStudent_name(student_name);
				signUp.setStudio(studio);
				signUp.setSign_time(update_time);
				signUp.setCreate_time(date_time + " 00:00:00");
				signUp.setMark(mark);
				signUp.setDuration(duration);
				signUp.setCount(count);
				signUp.setSubject(subject);
				signUp.setTeacher(teacher);
				signUp.setCampus(campus);
				if(class_number == null || class_number.isEmpty() || "undefined".equals(class_number)){
					class_number = "无班号";
				}
				signUp.setClass_number(class_number);

				int insert_res = loginService.insertSignUp(signUp);
				if(insert_res>0){
					loginService.updateMinusLesson(student_name,studio,count,subject,campus);
					loginService.updateAddPoints(student_name,studio,coins,subject,campus);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/leaveRecord")
	@ResponseBody
	public int leaveRecord(String student_name,String studio,String date_time,String duration,String leave_type,String mark_leave,String subject,String makeup_date){
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
			leave.setSubject(subject);
			leave.setMakeup_date(makeup_date);
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

	@RequestMapping("/singleAdd")
	@ResponseBody
	public int singleAdd(HttpServletRequest request, HttpServletResponse response){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
		try {
			cal.setTime(df.parse(create_time));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		String subject = request.getParameter("subject");
		String studio = request.getParameter("studio");
		String student_name = request.getParameter("student_name");
		String total_amount = request.getParameter("total_amount");
		String left_amount = request.getParameter("left_amount");
		String openid = request.getParameter("openid");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();

		try {
			Lesson lesson =new Lesson();
			lesson.setSubject(subject);
			lesson.setStudio(studio);
			lesson.setStudent_name(student_name);
			lesson.setTotal_amount(Float.parseFloat(total_amount));
			lesson.setLeft_amount(Float.parseFloat(left_amount));
			lesson.setCreate_time(create_time);
			lesson.setCoins(1.00f);
			lesson.setPoints(0);
			lesson.setMinus(1.00f);
			lesson.setCampus(campus);
			dao.insertLesson(lesson);
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

		String new_name = request.getParameter("new_name");

		try {
			if(new_name.trim().length() <= 0){
				dao.updateLocation(studio,openid,phone_number,location);
			}else {
				dao.updateNewName(openid,new_name);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/updateDetailPhoto")
	@ResponseBody
	public int updateDetailPhoto(HttpServletRequest request, HttpServletResponse response){

		//获取文字
		String class_target = request.getParameter("class_target");

		String studio = request.getParameter("studio");

		String id = request.getParameter("id");
		if(id == null || id.isEmpty() || "undefined".equals(id)){
			id = "noid";
		}

		String uuids = request.getParameter("uuids");

		List<String> list_new = new ArrayList<>();
		String[] uuids_get_list = null;
		String[] uuids_c_get_list = null;
		String[] uuids_add_list = null;

		String uuids_get =null;
		String uuids_c_get =null;
		String uuids_add =null;
		try {
			List<Message> list = dao.getUuidById(studio,Integer.parseInt(id));
			if(list.size()>0){
				uuids_get = list.get(0).getUuids();
				uuids_c_get = list.get(0).getUuids_c();

				if(uuids.length()>0){
					uuids_add = uuids.replace("\"","").replace("[","").replace("]","");
					uuids_add_list = uuids_add.split(",");
					for(int i =0;i<uuids_add_list.length;i++){
						list_new.add(uuids_add_list[i]);
					}
				}

				if("课评".equals(class_target)){
					if(uuids_get.length()>0){
						uuids_get = uuids_get.replace("\"","").replace("[","").replace("]","");
						uuids_get_list = uuids_get.split(",");
						for(int i =0;i<uuids_get_list.length;i++){
							list_new.add(uuids_get_list[i]);
						}
					}
					dao.updateUuids(Integer.parseInt(id),studio,list_new.toString().replace(" ",""));
				}

				if("课后作业".equals(class_target)){
					if(uuids_c_get != null){
						uuids_c_get = uuids_c_get.replace("\"","").replace("[","").replace("]","");
						uuids_c_get_list = uuids_c_get.split(",");
						for(int i =0;i<uuids_c_get_list.length;i++){
							list_new.add(uuids_c_get_list[i]);
						}
					}
					dao.updateUuids_c(Integer.parseInt(id),studio,list_new.toString().replace(" ",""));
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/updateAvatar")
	@ResponseBody
	public int updateAvatar(HttpServletRequest request, HttpServletResponse response){

		//获取文字
		String avatarurl = request.getParameter("avatarurl");

		String openid = request.getParameter("openid");

		try {
			User user =new User();
			user.setAvatarurl(avatarurl);
			user.setOpenid(openid);
			dao.updateAvatar(user);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/insertArrangement")
	@ResponseBody
	public int insertArrangement(String dayofweek,String class_number,String duration,String limits,String studio,String subject,String student_name,String openid){

		try {

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");//设置日期格式
			String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

			if (class_number == null || class_number.isEmpty() || "undefined".equals(class_number)){
				class_number = "无班号";
			}

			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();


			Arrangement arrangement =new Arrangement();
			arrangement.setDayofweek(dayofweek);
			arrangement.setClass_number(class_number);
			arrangement.setDuration(duration);
			arrangement.setLimits(limits);
			arrangement.setStudio(studio);
			arrangement.setSubject(subject);
			arrangement.setCampus(campus);
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
					schedule.setCampus(campus);
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

	@RequestMapping("/insertNote")
	@ResponseBody
	public int insertNote(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String studio =  request.getParameter("studio");
		String student_name =  request.getParameter("student_name");
		String note_content =  request.getParameter("note_content");
		String subject =  request.getParameter("subject");

		try {
			Note note = new Note();
			note.setStudio(studio);
			note.setSubject(subject);
			note.setStudent_name(student_name);
			note.setCreate_time(create_time);
			note.setNote_content(note_content);
			dao.insertNote(note);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 1;
	}

	@RequestMapping("/deleteLesson")
	@ResponseBody
	public int deleteLesson(Integer id,String role,String studio,String openid,String student_name){
		try {
			List<User> list = dao.getUser(openid);
			String studio_get = list.get(0).getStudio();

			if ( studio_get.equals(studio)) {
				loginService.deleteLesson(id,role,studio,openid,student_name);
			}else {
				logger.error("it's not your studio, could not delete!");
			}


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

	@RequestMapping("/downloadLesson")
	@ResponseBody
	public String downloadLesson(String studio,String openid){
		String path = System.getProperty("user.dir");
		String d_path = path +"/downloadLesson/"+ studio + "/" ;
		File file = new File(d_path);

		if (!file.exists()){ //如果不存在
			file.mkdirs(); //创建目录
		}

		String[] content = file.list();//取得当前目录下所有文件和文件夹
		for(String name : content){
			File temp = new File(d_path, name);
			temp.delete();
		}

		//获取类路径
		String p_path = null;
		p_path = path +"/downloadLesson/"+ studio + "/" + studio + ".xls";
		BufferedWriter bw = null;

		//保存csv
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			List<Lesson> lessons = dao.getLesson(studio,campus);
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(p_path),"UTF-8"));
			bw.write("科目,学生名,总课时,余课时");
			bw.newLine();
			for(int i=0; i<lessons.size(); i++){
				String subject = lessons.get(i).getSubject();
				String student_name = lessons.get(i).getStudent_name();
				String total_amount = lessons.get(i).getTotal_amount().toString();
				String left_amount = lessons.get(i).getLeft_amount().toString();
				bw.write(subject+","+student_name+","+total_amount+","+left_amount);
				bw.newLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if(bw != null){
					bw.flush();
					bw.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
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

	@RequestMapping("/get_download")
	@ResponseBody
	public ResponseEntity<byte[]> get_download(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String file_name =  request.getParameter("file_name");
		String studio =  request.getParameter("studio");
		String path = System.getProperty("user.dir");
		String p_path = path +"/downloadLesson/"+ studio+"/" +file_name;
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
	public String submit_batch(String studio,String openid) throws IOException{
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
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			Workbook book=Workbook.getWorkbook(temp);
			Sheet sheet=book.getSheet(0);
			for(int i=1;i<sheet.getRows();i++){
				Gift gift = new Gift();
				gift.setCreate_time(create_time);
				gift.setStudio(studio);
				gift.setStatus(0);
				gift.setCampus(campus);
				gift.setExpired_time(create_time);

				Lesson lesson =new Lesson();
				lesson.setCreate_time(create_time);
				lesson.setStudio(studio);
				lesson.setMinus(1.0f);
				lesson.setCoins(0.0f);
				lesson.setPoints(0);
				lesson.setCampus(campus);
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
							}else{
								lesson.setTotal_amount(0.0f);
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
					List<Lesson> lessons_get = dao.getLessonByNameSubject(student_name,studio,subject,campus);
					if(lessons_get.size()==0){
						loginService.insertLesson(lesson);
					}else if(lessons_get.size()>0){
						loginService.updateLesson(lesson,0.0f,0.0f,"全科目",campus);
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
		}finally {
			for(String name : tempList){
				File temp_delete = new File(path_1 + "/" + name);
				temp_delete.delete();
			}
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
		if(mp3_url == null || mp3_url.isEmpty() || "undefined".equals(mp3_url)){
			mp3_url = "no_mp3_url";
		}

		//获取图片路径
		String photo = request.getParameter("photo");

		//获取文字
		String comment = request.getParameter("comment");
		if(comment == null || comment.isEmpty() || "undefined".equals(comment)){
			comment = "no_comment";
		}

		//获取学生名字
		String student_name = request.getParameter("student_name");
		if(student_name == null || student_name.isEmpty() || "undefined".equals(student_name)){
			student_name = "no_student_name";
		}
		//获取课堂名称
		String class_name = request.getParameter("class_name");
		if(class_name == null || class_name.isEmpty() || "undefined".equals(class_name)){
			class_name = "no_class_name";
		}
		//获取课堂目标
		String class_target = request.getParameter("class_target");

		String id = request.getParameter("id");
		if(id == null || id.isEmpty() || "undefined".equals(id)){
			id = "noid";
		}

		String uuids = request.getParameter("uuids");
		if(uuids == null || uuids.isEmpty() || "undefined".equals(uuids)){
			uuids = "no_uuids";
		}

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

		String openid = request.getParameter("openid");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();

		FileInputStream in = null;
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
		message.setUuids(uuids);
		message.setCampus(campus);


		if("课程体系".equals(class_target) || "环境".equals(class_target) || "广告".equals(class_target) ){
			if("noid".equals(id)){
				try {
//					in = Imageutil.readImage(photo);
//					message.setPhoto(FileCopyUtils.copyToByteArray(in));
					loginService.push(message);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}else{
				List<Message> list = dao.getUuidById(studio,Integer.parseInt(id));
				String uuids_get = list.get(0).getUuids().replace("\"","").replace("[","").replace("]","");
				String uuids_add = uuids.replace("\"","").replace("[","").replace("]","");
				String[] result1 = uuids_get.split(",");
				String[] result2 = uuids_add.split(",");
				List<String> list_new = new ArrayList<>();
				for(int i =0;i<result1.length;i++){
					list_new.add(result1[i]);
				}
				for(int i =0;i<result2.length;i++){
					list_new.add(result2[i]);
				}
				dao.updateUuids(Integer.parseInt(id),studio,list_new.toString().replace(" ",""));
			}
		}else{
			try {
				if(!"奖状".equals(class_target)){
//					in = Imageutil.readImage(photo);
//					message.setPhoto(FileCopyUtils.copyToByteArray(in));
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

		}

		return "push massage successfully";
	}


	//	推送
	@RequestMapping("/insertPost")
	@ResponseBody
	public String insertPost(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		//获取
		String openid = request.getParameter("openid");
		String uuids = request.getParameter("uuids");
		String content = request.getParameter("content");
		String studio = request.getParameter("studio");
		String is_private = request.getParameter("is_private");

		Post post = new Post();
		post.setOpenid(openid);
		post.setUuids(uuids);
		post.setContent(content);
		post.setStudio(studio);
		post.setCreate_time(create_time);
		post.setIs_private(is_private);

		try {
			dao.insertPost(post);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return "push massage successfully";
	}

	@RequestMapping("/insertPostComment")
	@ResponseBody
	public String insertPostComment(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		//获取
		String openid = request.getParameter("openid");
		String post_id = request.getParameter("post_id");
		String content = request.getParameter("content");
		String studio = request.getParameter("studio");

		PostComment postComment = new PostComment();
		postComment.setOpenid(openid);
		postComment.setPost_id(post_id);
		postComment.setContent(content);
		postComment.setStudio(studio);
		postComment.setCreate_time(create_time);

		try {
			dao.insertPostComment(postComment);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return "push massage successfully";
	}

	@RequestMapping("/insertPostLike")
	@ResponseBody
	public String insertPostLike(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		//获取
		String openid = request.getParameter("openid");
		String post_id = request.getParameter("post_id");
		String studio = request.getParameter("studio");

		PostLike postLike = new PostLike();
		postLike.setOpenid(openid);
		postLike.setPost_id(post_id);
		postLike.setStudio(studio);
		postLike.setCreate_time(create_time);

		try {
			dao.insertPostLike(postLike);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return "push massage successfully";
	}

	//	推送
	@RequestMapping("/bookkeeping")
	@ResponseBody
	public String bookkeeping(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String studio = request.getParameter("studio");
		String mark = request.getParameter("mark");
		String type = request.getParameter("type");
		String amount = request.getParameter("amount");
		String openid = request.getParameter("openid");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();

		Book book =new Book();
		book.setStudio(studio);
		book.setType(type);
		book.setMark(mark);
		book.setAmount(Float.parseFloat(amount));
		book.setCreate_time(create_time);
		book.setCampus(campus);

		try {
			dao.insertBook(book);
		} catch (Exception e) {
			throw new RuntimeException(e);
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

		String uuids = request.getParameter("uuids");

		String is_group = request.getParameter("is_group");

		String openid = request.getParameter("openid");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();

		GoodsList goodsList =new GoodsList();
		try {

			goodsList.setGoods_name(goods_name);
			goodsList.setGoods_intro(goods_intro);
			goodsList.setGoods_price(Float.parseFloat(goods_price));
			goodsList.setStudio(studio);
			goodsList.setCreate_time(create_time);
			goodsList.setCampus(campus);
			goodsList.setUuids(uuids);
			goodsList.setIs_group(Integer.parseInt(is_group));

			loginService.insertGoodsList(goodsList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	@RequestMapping("/insertGroupBuy")
	@ResponseBody
	public String insertGroupBuy(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		//获取商品名称
		String goods_name = request.getParameter("goods_name");
		//获取商品ID
		String goods_id = request.getParameter("goods_id");
		//获取商品价格
		String nick_name = request.getParameter("nick_name");

		String studio = request.getParameter("studio");

		String openid = request.getParameter("openid");

		GroupBuy groupBuy =new GroupBuy();
		try {
			groupBuy.setGoods_id(goods_id);
			groupBuy.setGoods_name(goods_name);
			groupBuy.setNick_name(nick_name);
			groupBuy.setOpenid(openid);
			groupBuy.setCreate_time(create_time);
			groupBuy.setStudio(studio);

			dao.insertGroupBuy(groupBuy);
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
		String student_type = "ordinary";

		try {
			student_type = request.getParameter("student_type");
			add_date = request.getParameter("add_date");
		} catch (Exception e) {
//			throw new RuntimeException(e);
		}

		String class_select = request.getParameter("class_select");
		String[] list_get = class_select.split(",");
		String weekofday = list_get[0];

		if("ordinary".equals(student_type)){
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
		}

		String class_number = list_get[1];
		String duration = list_get[2];
		String subject = list_get[3];

		String studio = request.getParameter("studio");

		String openid = request.getParameter("openid");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();

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
				schedule.setStudent_type(student_type);
				schedule.setStatus(Integer.parseInt(status));
				schedule.setSubject(subject);
				schedule.setCampus(campus);
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

		String studio = request.getParameter("studio");

		String campus = request.getParameter("campus");
		if(campus == null || campus.isEmpty() || "undefined".equals(campus)){
			campus = studio;
		}

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
		if(openid == null || openid.isEmpty() || "undefined".equals(openid)){
//			openid = nick_name + "_" + studio;
			openid = DigestUtils.md5Hex(nick_name + studio);
		}
		//获取 avatarurl
		String avatarurl = request.getParameter("avatarurl");
		if(avatarurl == null || avatarurl.isEmpty() || "undefined".equals(avatarurl)){
			avatarurl = "https://thirdwx.qlogo.cn/mmopen/vi_32/y667SLJ40Eic5fMnHdibjO4vLG7dmqgjeuwjQbRN5ZJj6uZfl06yA7P9wwl7oYjNRFzBzwcheZtK8zvkibyfamfBA/132";
		}

		List<User> list_send = dao.getUserSendTime(studio);
		String send_time = "12:00:00";
		Integer display = 1;
		Integer cover = 1;

		if(list_send.size()>0){
			send_time = list_send.get(0).getSend_time();
			display = list_send.get(0).getDisplay();
			cover = list_send.get(0).getCover();
		}

        //获取 comment_style
		String comment_style = "public";
		List<User> list_u = dao.getComentStyle(studio);
		if(list_u.size()>0){
			comment_style = list_u.get(0).getComment_style();
		}



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
		user.setCampus(campus);
		int res = loginService.updateUser(user);
		if(0==res){
			user.setUser_type("新用户");
			user.setComment_style(comment_style);
			user.setSend_time(send_time);
			user.setDisplay(display);
			user.setCover(cover);
			try {
				int update_res = dao.updateUserDelete(user);
				if(update_res==0){
					loginService.insertUser(user);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(res>0&&!student_name.equals("no_name")){
			List<User> list= dao.getUser(openid);
			String user_type_get = list.get(0).getUser_type();
			String role_get = list.get(0).getRole();
			user.setUser_type(user_type_get);
			user.setRole(role_get);
			user.setComment_style(comment_style);
			user.setSend_time(send_time);
			user.setDisplay(display);
			user.setCover(cover);
			try {
				int update_res = dao.updateUserDelete(user);
				if(update_res==0){
					loginService.insertUser(user);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		String role = request.getParameter("role");

		User user_get= dao.getUser(openid).get(0);
		String role_get = user_get.getRole();
		String studio_get = user_get.getStudio();

		try {
			User user_get_b = dao.getBossByStudio(studio_get).get(0);
			if (user_get_b.getExpired_time().length() >0){
				expired_time = user_get_b.getExpired_time();
			}
		} catch (Exception e) {
//			throw new RuntimeException(e);
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

	@RequestMapping("/updateCampus")
	@ResponseBody
	public String updateCampus(HttpServletRequest request, HttpServletResponse response){
		//获取openid
		String openid_get = request.getParameter("openid");
		String campus = request.getParameter("campus");
		String type = request.getParameter("type");

		try {

			if("校区".equals(type)){
				User user =new User();
				user.setOpenid(openid_get);
				user.setCampus(campus);
				dao.updateUserCampus(user);
			}else if("学生名".equals(type)){
				String openid = openid_get.split("_")[0];
				String id = openid_get.split("_")[1];
				dao.updateUserStudentByOpenid(campus,openid,id);
			}

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

		String openid = request.getParameter("openid");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();

		//获取用户名
		String student_name = request.getParameter("student_name");

		//获取用户名
		String subject = request.getParameter("subject");

		//默认值必填 全科目
		String subject_new = request.getParameter("subject_new");

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
					gift.setCampus(campus);
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

			String campus_new = request.getParameter("campus_new");

			//获取总课时
			String total_amount_1 = request.getParameter("total_amount");
			Float total_amount = 0.0f;
			if (!total_amount_1.isEmpty()){
				total_amount = Float.valueOf(total_amount_1);
			}
			// 获取type
			String modify_type = request.getParameter("modify_type");
			if("campus_modify".equals(modify_type)){
				dao.updateLessonCampus(studio,student_name,campus_new);
				dao.updateSignUpCampus(studio,student_name,campus_new);
				dao.updateGiftCampus(studio,student_name,campus_new);
				dao.updateNoteCampus(studio,student_name,campus_new);
				dao.updateLeaveCampus(studio,student_name,campus_new);
				return "campus changed" ;
			}

			// 获取type
			String lessons_amount_1 = request.getParameter("lessons_amount");
			Float lessons_amount = 0.0f;
			if (!lessons_amount_1.isEmpty()){
				lessons_amount = Float.valueOf(lessons_amount_1);
			}

			String consume_lesson_amount_1 = request.getParameter("consume_lesson_amount");
			Float consume_lesson_amount = 0.0f;
			if (!consume_lesson_amount_1.isEmpty()){
				consume_lesson_amount = Float.valueOf(consume_lesson_amount_1);
			}

			Float minus_amount=0.0f;
			Float coins_amount=0.0f;
			List<Lesson> lessons_get = dao.getLessonByNameSubject(student_name,studio,subject,campus);
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
			Float left_amount = 0.0f;
			if(!left_amount_get.isEmpty()){
				left_amount = Float.parseFloat(left_amount_get);
			}

			Lesson lesson =new Lesson();
			lesson.setStudent_name(student_name);
			lesson.setTotal_amount(total_amount);
			lesson.setLeft_amount(left_amount);
			lesson.setCreate_time(create_time);
			lesson.setStudio(studio);
			lesson.setCampus(campus);
			lesson.setMinus(minus_amount);
			lesson.setCoins(coins_amount);
			lesson.setSubject(subject);

			List<Lesson> lessons = dao.getLessonByNameSubject(student_name, studio,subject,campus);
			if(!student_name_new.isEmpty()){
				dao.updateScheduleName(student_name_new,student_name,studio,campus);
				dao.updateCommentName(student_name_new,student_name,studio,campus);
				dao.updateGiftRecordName(student_name_new,student_name,studio,campus);
				dao.updateLessonName(student_name_new,student_name,studio,campus);
				dao.updateSignUpRecordName(student_name_new,student_name,studio,campus);
				dao.updateUserStudent(student_name_new,student_name,studio,campus);
			}else if(lessons.size()>0){
				if("coins_modify_all".equals(modify_type)){
					dao.updateLessonAll(coins_amount,studio,campus);
				}else {
					loginService.updateLesson(lesson,lessons_amount,consume_lesson_amount,subject_new,campus);
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
			e.printStackTrace();
//			logger.error("update lesson error");
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
		String subject = request.getParameter("subject");
		String openid = request.getParameter("openid");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();

		Integer points_int = Integer.parseInt(points);
		if(subject==null){
			subject="美术";
		}

		try {
			loginService.updateAddPoints(student_name,studio,points_int,subject,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	@RequestMapping("/updateUserPay")
	@ResponseBody
	public String updateUserPay(HttpServletRequest request, HttpServletResponse response){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		//获取用户名
		String expired_time = request.getParameter("expired_time");
		String studio = request.getParameter("studio");

		List<User> list = dao.getBossByStudio(studio);
        String expire_time_get = list.get(0).getExpired_time();
		Long days = 0L;

		try {
			Date expired_time_dt = df.parse(expired_time);
			Date expire_time_get_dt = df.parse(expire_time_get);
			Long day2 = expired_time_dt.getTime();
			Long day1 = expire_time_get_dt.getTime();
			days = (day2 - day1)/(24*3600*1000);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}


		User user=new User();
		user.setExpired_time(expired_time);
		user.setStudio(studio);
		user.setCreate_time(create_time);
		user.setUser_type("老用户");
		user.setRole("boss");
		user.setDays(Math.toIntExact(days));

		try {
			dao.updateUserPay(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	@RequestMapping("/updateSubscription")
	@ResponseBody
	public String updateSubscription(HttpServletRequest request, HttpServletResponse response){
		String openid = request.getParameter("openid");
		String subscription = request.getParameter("subscription");

		User user=new User();
		user.setOpenid(openid);
		user.setSubscription(subscription);

		try {
			dao.updateSubscription(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	@RequestMapping("/updateUserMember")
	@ResponseBody
	public String updateUserMember(HttpServletRequest request, HttpServletResponse response){
		String studio = request.getParameter("studio");
		String member = request.getParameter("member");

		try {
			dao.updateUserMember(member,studio);
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
		String openid = request.getParameter("openid");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();

		try {
			dao.updateScheduleName(student_name_new,student_name,studio,campus);
			dao.updateCommentName(student_name_new,student_name,studio,campus);
			dao.updateGiftRecordName(student_name_new,student_name,studio,campus);
			dao.updateLessonName(student_name_new,student_name,studio,campus);
			dao.updateSignUpRecordName(student_name_new,student_name,studio,campus);
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
		String openid = request.getParameter("openid");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();
		String points_get = request.getParameter("points");
		String subject = request.getParameter("subject");
		Integer points = Integer.parseInt(points_get);

		try {
			loginService.deletePoints(student_name,studio,points,subject,campus);
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
		String subject = request.getParameter("subject");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();

		List<User> list = dao.getUser(openid);
		String studio_get = list.get(0).getStudio();

		if ( studio_get.equals(studio)) {
			try {
				loginService.deleteSignUpRecord(Integer.parseInt(id),role,studio,openid);
				loginService.updateMinusLesson(student_name,studio,-Float.parseFloat(count),subject,campus);

				List<Lesson> list1 = dao.getLessonByNameSubject(student_name, studio,subject,campus);
				Float coins = list1.get(0).getCoins();
				loginService.updateAddPoints(student_name,studio,-Math.round(coins),subject,campus);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}else {
			logger.error("it's not your studio, could not delete!");
		}

		return "push massage successfully";
	}

	@RequestMapping("/sendSubscription")
	@ResponseBody
	public String sendSubscription(String subscription,String publickey,String privatekey,String payload){

		try {
			logger.info("开始...");
			String status = webPushService.sendNotification(subscription,publickey,privatekey,payload);
			logger.info("status: " + status);
			return status;
		} catch (Exception e) {
			e.printStackTrace();
			return "something is wrong";
		}
	}
}
	
	


