package com.xue.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSONArray;
import com.xue.JsonUtils.JsonUtils;
import com.xue.entity.model.*;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;
import com.xue.service.WebPushService;
import com.xue.util.HttpUtil;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
	private static final String tample10 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"LlmWw436tKz1DwZzpWmgnIBFAtXCUj_HxDN7xub03e4\",\"data\":{\"thing1\":{\"value\": \"AA\"},\"time5\":{\"value\": \"time\"},\"thing3\":{\"value\": \"A1\"}}}";
	private static final String tample11 ="{\"touser\":\"openid\",\"mp_template_msg\":{\"appid\":\"wxc79a69144e4fd233\",\"template_id\":\"Z0mHLtqz1JNHvxTFt2QoiZ2222-FN1TVWEttoWKV12c\",\"url\":\"http://weixin.qq.com/download\", \"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"},\"data\":{\"first\":{\"value\": \"AA\"},\"keyword1\":{\"value\": \"time\"},\"keyword2\":{\"value\": \"A1\"},\"keyword3\":{\"value\": \"A1\"},\"keyword4\":{\"value\": \"A1\"},\"remark\":{\"value\": \"A1\"}}}}";
	private static final String tample12 ="{\"touser\":\"openid\",\"mp_template_msg\":{\"appid\":\"wxc79a69144e4fd233\",\"template_id\":\"kYl_eizTO2EZWIgfSw1ZAUMoS7NF4hTNAhaFBGY-_JA\",\"url\":\"http://weixin.qq.com/download\", \"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"},\"data\":{\"first\":{\"value\": \"AA\"},\"keyword1\":{\"value\": \"AA\"},\"keyword2\":{\"value\": \"A1\"},\"keyword3\":{\"value\": \"A1\"},\"keyword4\":{\"value\": \"time\"},\"remark\":{\"value\": \"A1\"}}}}";
	private static final String tample13 ="{\"touser\":\"openid\",\"mp_template_msg\":{\"appid\":\"wxc79a69144e4fd233\",\"template_id\":\"O9vQEneXUbkhdCuWW_-hQEGqUztTXQ8g0Mrgy97VAuI\",\"url\":\"http://weixin.qq.com/download\", \"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"},\"data\":{\"first\":{\"value\": \"通知广播\"},\"keyword1\":{\"value\": \"time\"},\"keyword2\":{\"value\": \"A1\"},\"remark\":{\"value\": \"请点击查看详情。\"}}}}";
	private static final String tample14 ="{\"touser\":\"openid\",\"mp_template_msg\":{\"appid\":\"wxc79a69144e4fd233\",\"template_id\":\"icj6FVVB2sdpUGbwLvZ3kYnLYMPTYTlXbwxCsXkQ7Hk\",\"url\":\"http://weixin.qq.com/download\", \"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"},\"data\":{\"thing2\":{\"value\": \"AA\"},\"thing4\":{\"value\": \"AA\"},\"character_string3\":{\"value\": \"A1\"},\"time6\":{\"value\": \"AA\"}}}}";
	private static final String tample15 ="{\"touser\":\"openid\",\"mp_template_msg\":{\"appid\":\"wxc79a69144e4fd233\",\"template_id\":\"Bl9ZwhH2pWqL2pgo-WF1T5LPI4QUxmN9y7OWmwvvd58\",\"url\":\"http://weixin.qq.com/download\", \"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"},\"data\":{\"thing16\":{\"value\": \"AA\"},\"thing17\":{\"value\": \"AA\"},\"short_thing5\":{\"value\": \"A1\"}}}}";
	private static final String tample16 ="{\"touser\":\"openid\",\"mp_template_msg\":{\"appid\":\"wxc79a69144e4fd233\",\"template_id\":\"cxL6AZ7ROg7aAlcDDi5M4D6MI0A6Vc7eV33zAdq1Kew\",\"url\":\"http://weixin.qq.com/download\", \"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"},\"data\":{\"thing2\":{\"value\": \"AA\"},\"short_thing3\":{\"value\": \"AA\"},\"short_thing4\":{\"value\": \"A1\"},\"thing1\":{\"value\": \"A1\"}}}}";


	@Autowired
	private LoginService loginService;

	@Autowired
	private WebPushService webPushService;

	@Autowired
	private UserMapper dao;

	@RequestMapping("/sendLeaveRemind")
	@ResponseBody
	public String sendLeaveRemind(String token, String openid, String studio,String subject,String student_name,String date_time,String mark_leave,String makeup_date){
		String result = null;
		String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
		JSONObject queryJson = JSONObject.parseObject(tample10);

		queryJson.put("touser",openid);
		queryJson.getJSONObject("data").getJSONObject("thing1").put("value",subject + "_" + student_name);
		queryJson.getJSONObject("data").getJSONObject("time5").put("value",date_time);
		queryJson.getJSONObject("data").getJSONObject("thing3").put("value",mark_leave+"_补" + makeup_date);

		queryJson.put("page","/pages/leaverecord/leaverecord?share_studio=" + studio + "&share_student_name=" + student_name + "&share_subject=" + subject + "&share_openid=" + openid);


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

	@RequestMapping("/getOpenidOfficial")
	@ResponseBody
	public void getOpenidOfficial(){
		loginService.getOpenidOfficial();
	}

	@RequestMapping("/sendConsumeLesson")
	@ResponseBody
	public String sendConsumeLesson(String openid,String consume_lesson_amount,String student_name,String subject){
		String result = null;
		String url_send = null;
		String model ="{\"touser\":\"openid\",\"template_id\":\"cxL6AZ7ROg7aAlcDDi5M4D6MI0A6Vc7eV33zAdq1Kew\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing2\":{\"value\": \"AA\"},\"short_thing3\":{\"value\": \"A1\"},\"short_thing4\":{\"value\": \"A1\"},\"thing1\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";

		List<User> users = dao.getUser(openid);
		User user = users.get(0);
		String campus = user.getCampus();
		String studio = user.getStudio();
		String official_openid = user.getOfficial_openid();

		List<Lesson> lessons = dao.getLessonByNameSubject(student_name, studio,subject,campus);
		int left_amount = 0;
		if(lessons.size()>0){
			Float left_amount_get = lessons.get(0).getLeft_amount();
			left_amount = Math.round(left_amount_get);
		}

		try {
			String token = loginService.getToken("MOMO_OFFICIAL");
			url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
			if(official_openid != null){
			String[] official_list = official_openid.split(",");
			for(int j=0;j<official_list.length;j++){
				String official_openid_get = official_list[j];
				JSONObject queryJson = JSONObject.parseObject(model);
				queryJson.put("touser",official_openid_get);
				queryJson.getJSONObject("data").getJSONObject("thing2").put("value",subject+"_"+student_name);
				queryJson.getJSONObject("data").getJSONObject("short_thing3").put("value",consume_lesson_amount+"课时");
				queryJson.getJSONObject("data").getJSONObject("short_thing4").put("value",left_amount+"课时");
				queryJson.getJSONObject("data").getJSONObject("thing1").put("value",studio);
				queryJson.getJSONObject("miniprogram").put("pagepath","/pages/signuprecord/signuprecord?student_name=" + student_name + "&studio=" + studio + "&subject=" + subject);

				System.out.println("MOMO_OFFICIAL_PARAM:" + queryJson.toJSONString());
				result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
				System.out.printf("MOMO_OFFICIAL_RES:" + result);
			}
		}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	//	获取token
	@RequestMapping("/sendPostRemind")
	@ResponseBody
	public String sendPostRemind(String openid,String class_name,String student_name,String class_number,String duration){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
		String result = null;
		String url_send = null;
		String model ="{\"touser\":\"openid\",\"template_id\":\"kYl_eizTO2EZWIgfSw1ZAUMoS7NF4hTNAhaFBGY-_JA\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"first\":{\"value\": \"AA\"},\"keyword1\":{\"value\": \"A1\"},\"keyword2\":{\"value\": \"A1\"},\"keyword3\":{\"value\": \"A1\"},\"keyword4\":{\"value\": \"A1\"},\"remark\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";

		List<User> users = dao.getUser(openid);
		User user = users.get(0);
		String studio = user.getStudio();
		String comment_style = user.getComment_style();
		String role = user.getRole();
		String official_openid = user.getOfficial_openid();

		try {
			String token = loginService.getToken("MOMO_OFFICIAL");
			url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
			if(official_openid != null){
				String[] official_list = official_openid.split(",");
				for(int j=0;j<official_list.length;j++){
					String official_openid_get = official_list[j];
					JSONObject queryJson = JSONObject.parseObject(model);
					queryJson.put("touser",official_openid_get);
					queryJson.getJSONObject("data").getJSONObject("keyword1").put("value",class_number);
					queryJson.getJSONObject("data").getJSONObject("keyword2").put("value",class_name + "_" + student_name);
					queryJson.getJSONObject("data").getJSONObject("keyword3").put("value",duration);
					queryJson.getJSONObject("data").getJSONObject("keyword4").put("value",create_time);
					String class_target = class_number.split("_")[1];
					if(class_target.equals("课后点评")){
						class_target = "课评";
					}
					queryJson.getJSONObject("miniprogram").put("pagepath","/pages/comment/comment?openid=" + openid + "&studio=" + studio + "&comment_style=" + comment_style + "&role=" + role + "&class_target=" + class_target);

					System.out.println("MOMO_OFFICIAL_PARAM:" + queryJson.toJSONString());
					result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
					System.out.printf("MOMO_OFFICIAL_RES:" + result);
				}
			}

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
	public String sendSignUpRemind(String openid,String student_name,String date_time,String class_count,String subject,String class_number){
		String result = null;
		String url_send = null;
		String model ="{\"touser\":\"openid\",\"template_id\":\"Z0mHLtqz1JNHvxTFt2QoiZ2222-FN1TVWEttoWKV12c\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"first\":{\"value\": \"AA\"},\"keyword1\":{\"value\": \"A1\"},\"keyword2\":{\"value\": \"A1\"},\"keyword3\":{\"value\": \"A1\"},\"keyword4\":{\"value\": \"A1\"},\"remark\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";
		List<User> users = dao.getUser(openid);
		User user = users.get(0);
		String campus = user.getCampus();
		String official_openid = user.getOfficial_openid();
		String studio = user.getStudio();

		List<Lesson> lessons = dao.getLessonByNameSubject(student_name, studio,subject,campus);
		Float count = 0.0f;
		Float left = 0.0f;
		Float total = 0.0f;
		if(lessons.size()>0){
			count = lessons.get(0).getMinus();
			left = lessons.get(0).getLeft_amount();
			total = lessons.get(0).getTotal_amount();
		}

		if(Float.parseFloat(class_count) != 100){
			count = Float.parseFloat(class_count);
		}

		String thing8 = "本次扣课" + count + "课时";

		try {
			String token = loginService.getToken("MOMO_OFFICIAL");
			url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
			if(official_openid != null){
				String[] official_list = official_openid.split(",");
				for(int j=0;j<official_list.length;j++){
					String official_openid_get = official_list[j];
					JSONObject queryJson = JSONObject.parseObject(model);
					queryJson.put("touser",official_openid_get);
					queryJson.getJSONObject("data").getJSONObject("keyword1").put("value",date_time);
					queryJson.getJSONObject("data").getJSONObject("keyword2").put("value",class_number + "("+subject+student_name+")");
					queryJson.getJSONObject("data").getJSONObject("keyword3").put("value",thing8);
					queryJson.getJSONObject("data").getJSONObject("keyword4").put("value",left + "课时");
					queryJson.getJSONObject("miniprogram").put("pagepath","/pages/signuprecord/signuprecord?student_name=" + student_name + "&studio=" + studio + "&subject=" + subject);

					System.out.println("OFFICIAL_PARAM:" + queryJson.toJSONString());
					result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
					System.out.printf("OFFICIAL_RES:" + result);
				}
			}

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
		String url_send = null;
		String model ="{\"touser\":\"openid\",\"template_id\":\"O9vQEneXUbkhdCuWW_-hQEGqUztTXQ8g0Mrgy97VAuI\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"first\":{\"value\": \"AA\"},\"keyword1\":{\"value\": \"A1\"},\"keyword2\":{\"value\": \"A1\"},\"remark\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";

		List<User> users = dao.getUser(openid);
		User user = users.get(0);
		studio = user.getStudio();
		String official_openid = user.getOfficial_openid();

		String content_head = null;
		if(content.length() > 10){
			content_head = content.substring(0, 10) + "...";
		}

		try {
			token = loginService.getToken("MOMO_OFFICIAL");
			url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
			if(official_openid != null){
				String[] official_list = official_openid.split(",");
				for(int j=0;j<official_list.length;j++){
					String official_openid_get = official_list[j];
					JSONObject queryJson = JSONObject.parseObject(model);
					queryJson.put("touser",official_openid_get);
					queryJson.getJSONObject("data").getJSONObject("keyword1").put("value",title);
					queryJson.getJSONObject("data").getJSONObject("keyword2").put("value",content_head);
					queryJson.getJSONObject("miniprogram").put("pagepath","/pages/noticedetail/noticedetail?studio=" + studio);

					System.out.println("MOMO_OFFICIAL_PARAM:" + queryJson.toJSONString());
					result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
					System.out.printf("MOMO_OFFICIAL_RES:" + result);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	@RequestMapping("/sendReadingCenter")
	@ResponseBody
	public String sendReadingCenter(String openid,String title){
		SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());
		String result = null;
		String url_send = null;
		String model ="{\"touser\":\"openid\",\"template_id\":\"wyrik1xgBBf7m-Oj2UhGfRxuiQXaCbkOIp49oLgQ7Cg\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing7\":{\"value\": \"AA\"},\"thing5\":{\"value\": \"A1\"},\"time6\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";
		String token = loginService.getToken("MOMO_OFFICIAL");

		List<User> users = dao.getUser(openid);
		User user = users.get(0);
		String studio = user.getStudio();
		String campus = user.getCampus();
		String student_name = user.getStudent_name();
		if(title.length() > 10){
			title = title.substring(0, 10);
		}

		try {
			List<User> list = dao.getUserByStudio(studio,campus);
			for (int i = 0; i < list.size(); i++) {
				User user_get = list.get(i);
				String official_openid = user_get.getOfficial_openid();
				String openid_get = user_get.getOpenid();
				url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
				if(official_openid != null){
					String[] official_list = official_openid.split(",");
					for(int j=0;j<official_list.length;j++){
						String official_openid_get = official_list[j];
						JSONObject queryJson = JSONObject.parseObject(model);
						queryJson.put("touser",official_openid_get);
						queryJson.getJSONObject("data").getJSONObject("thing7").put("value",title);
						queryJson.getJSONObject("data").getJSONObject("thing5").put("value",student_name);
						queryJson.getJSONObject("data").getJSONObject("time6").put("value",create_time);
						queryJson.getJSONObject("miniprogram").put("pagepath","/pages/gallery/gallery?studio=" + studio + "&openid=" + openid_get);

						System.out.println("MOMO_OFFICIAL_PARAM:" + queryJson.toJSONString());
						result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
						System.out.printf("MOMO_OFFICIAL_RES:" + result);
					}
				}
			}



		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	@RequestMapping("/sendComment")
	@ResponseBody
	public String sendComment(String openid,String student_name,String class_name,String id,String class_target){
		SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());
		String result = null;
		String url_send = null;
		String model ="{\"touser\":\"openid\",\"template_id\":\"r4UhyPAsqJwh8uUghKcxzSqM5_XNIxZzjn2zFZlhST0\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing5\":{\"value\": \"AA\"},\"thing1\":{\"value\": \"A1\"},\"time2\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";
		String token = loginService.getToken("MOMO_OFFICIAL");

		List<User> users = dao.getUser(openid);
		User user = users.get(0);
		String studio = user.getStudio();
		String nick_name = user.getNick_name();
		if(class_name.length() > 10){
			class_name = class_name.substring(0, 10);
		}

		try {
			List<User> list = dao.getUserByStudent(student_name,studio);
			List<User> list1 = dao.getBossByStudio(studio);
			list.addAll(list1);
			for (int i = 0; i < list.size(); i++) {
				User user_get = list.get(i);
				String official_openid = user_get.getOfficial_openid();
				String openid_get = user_get.getOpenid();
				url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
				if(official_openid != null && openid != openid_get){
					String[] official_list = official_openid.split(",");
					for(int j=0;j<official_list.length;j++){
						String official_openid_get = official_list[j];
						JSONObject queryJson = JSONObject.parseObject(model);
						queryJson.put("touser",official_openid_get);
						queryJson.getJSONObject("data").getJSONObject("thing5").put("value",student_name + "_" + class_name);
						queryJson.getJSONObject("data").getJSONObject("thing1").put("value",nick_name);
						queryJson.getJSONObject("data").getJSONObject("time2").put("value",create_time);
						queryJson.getJSONObject("miniprogram").put("pagepath","/pages/detail/detail?id=" + id + "&class_target=" + class_target + "&openid=" + openid_get);

						System.out.println("MOMO_OFFICIAL_PARAM:" + queryJson.toJSONString());
						result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
						System.out.printf("MOMO_OFFICIAL_RES:" + result);
					}
				}
			}
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
		String url_send = null;
		String model ="{\"touser\":\"openid\",\"template_id\":\"Bl9ZwhH2pWqL2pgo-WF1T5LPI4QUxmN9y7OWmwvvd58\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing16\":{\"value\": \"AA\"},\"thing17\":{\"value\": \"A1\"},\"short_thing5\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";

		List<User> users = dao.getUser(openid);
		User user = users.get(0);
		studio = user.getStudio();
		String nick_name = user.getNick_name();

		try {
			token = loginService.getToken("MOMO_OFFICIAL");
			url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
			JSONObject queryJson = JSONObject.parseObject(model);
			queryJson.put("touser","oFTmu6Z3Wg2hiAXMe13yGsz35opY");
			queryJson.getJSONObject("data").getJSONObject("thing16").put("value",studio+"_"+nick_name);
			queryJson.getJSONObject("data").getJSONObject("thing17").put("value","支付" + amount +"元,续费" + days + "天" );
			queryJson.getJSONObject("data").getJSONObject("short_thing5").put("value","待处理");

			System.out.println("MOMO_OFFICIAL_PARAM:" + queryJson.toJSONString());
			result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
			System.out.printf("MOMO_OFFICIAL_RES:" + result);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	//续费成功通知
	@RequestMapping("/sendFeedback")
	@ResponseBody
	public String sendFeedback(String token, String openid, String studio, String expired_time,String days){
		SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());
		String result = null;
		String url_send = null;
		String model ="{\"touser\":\"openid\",\"template_id\":\"icj6FVVB2sdpUGbwLvZ3kYnLYMPTYTlXbwxCsXkQ7Hk\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing2\":{\"value\": \"AA\"},\"thing4\":{\"value\": \"A1\"},\"character_string3\":{\"value\": \"A1\"},\"time6\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";

		List<User> users = dao.getUser(openid);
		User user = users.get(0);
		String official_openid = user.getOfficial_openid();

		try {
			token = loginService.getToken("MOMO_OFFICIAL");
			url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
			if(official_openid != null){
				String[] official_list = official_openid.split(",");
				for(int j=0;j<official_list.length;j++){
					String official_openid_get = official_list[j];
					JSONObject queryJson = JSONObject.parseObject(model);
					queryJson.put("touser",official_openid_get);
					queryJson.getJSONObject("data").getJSONObject("thing2").put("value",studio);
					queryJson.getJSONObject("data").getJSONObject("thing4").put("value","续费" + days + "天" );
					queryJson.getJSONObject("data").getJSONObject("character_string3").put("value","TO"+expired_time );
					queryJson.getJSONObject("data").getJSONObject("time6").put("value",create_time);

					System.out.println("MOMO_OFFICIAL_PARAM:" + queryJson.toJSONString());
					result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
					System.out.printf("MOMO_OFFICIAL_RES:" + result);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	//客户下单通知
	@RequestMapping("/sendOrderNotice")
	@ResponseBody
	public String sendOrderNotice(String openid,String goods_name){
		SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());
		String result = null;
		String url_send = null;
		String model ="{\"touser\":\"openid\",\"template_id\":\"4Pkp58wCQy0cR5N-cTQuATysehBBvxwuyczrdsjHD2A\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing3\":{\"value\": \"AA\"},\"thing5\":{\"value\": \"A1\"},\"time2\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";
		String token = loginService.getToken("MOMO_OFFICIAL");

		List<User> users = dao.getUser(openid);
		User user = users.get(0);
		String studio = user.getStudio();
		String nick_name = user.getNick_name();

		try {
			List<User> list = dao.getBossByStudio(studio);
			for (int i = 0; i < list.size(); i++) {
				User user_get = list.get(i);
				String official_openid = user_get.getOfficial_openid();
				String openid_get = user_get.getOpenid();
				String role_get = user_get.getRole();
				url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
				if(official_openid != null){
					String[] official_list = official_openid.split(",");
					for(int j=0;j<official_list.length;j++){
						String official_openid_get = official_list[j];
						JSONObject queryJson = JSONObject.parseObject(model);
						queryJson.put("touser",official_openid_get);
						queryJson.getJSONObject("data").getJSONObject("thing3").put("value",nick_name);
						queryJson.getJSONObject("data").getJSONObject("thing5").put("value",goods_name);
						queryJson.getJSONObject("data").getJSONObject("time2").put("value",create_time);
						queryJson.getJSONObject("miniprogram").put("pagepath","/pages/my_order/my_order?studio=" + studio + "&openid=" + openid_get + "&role=" + role_get);

						System.out.println("MOMO_OFFICIAL_PARAM:" + queryJson.toJSONString());
						result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
						System.out.printf("MOMO_OFFICIAL_RES:" + result);
					}
				}
			}



		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}

	//客户下单通知
	@RequestMapping("/sendDeliverNotice")
	@ResponseBody
	public String sendDeliverNotice(String openid,String goods_name){
		SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());
		String result = null;
		String url_send = null;
		String model ="{\"touser\":\"openid\",\"template_id\":\"95nY7GRp4krnen1t5pL67MoQdveejBN0nDPpEaUOTxU\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing17\":{\"value\": \"AA\"},\"thing2\":{\"value\": \"A1\"},,\"thing29\":{\"value\": \"A1\"},\"time4\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";
		String token = loginService.getToken("MOMO_OFFICIAL");


		try {
			List<User> list = dao.getUser(openid);
			User user_get = list.get(0);
			String official_openid = user_get.getOfficial_openid();
			String studio = user_get.getStudio();
			String nick_name = user_get.getNick_name();
			String location = user_get.getLocation();
			String role = user_get.getRole();
			url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
			if(official_openid != null){
				String[] official_list = official_openid.split(",");
				for(int j=0;j<official_list.length;j++){
					String official_openid_get = official_list[j];
					JSONObject queryJson = JSONObject.parseObject(model);
					queryJson.put("touser",official_openid_get);
					queryJson.getJSONObject("data").getJSONObject("thing17").put("value",goods_name);
					queryJson.getJSONObject("data").getJSONObject("thing2").put("value",nick_name);
					queryJson.getJSONObject("data").getJSONObject("thing29").put("value",location);
					queryJson.getJSONObject("data").getJSONObject("time4").put("value",create_time);
					queryJson.getJSONObject("miniprogram").put("pagepath","/pages/my_order/my_order?studio=" + studio + "&openid=" + openid + "&role=" + role);

					System.out.println("MOMO_OFFICIAL_PARAM:" + queryJson.toJSONString());
					result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
					System.out.printf("MOMO_OFFICIAL_RES:" + result);
				}
			}



		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}


	//	获取token
	@RequestMapping("/sendClassRemind")
	@ResponseBody
	public String sendClassRemind(String openid, String duration, String studentname,String remindDay,String class_number){
		String result = null;
		String url_send = null;
		String token = null;
		String tample6 ="{\"touser\":\"openid\",\"template_id\":\"MFu-qjMY5twe6Q00f6NaR-cBEn3QYajFquvtysdxk8o\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing1\":{\"value\": \"time\"},\"time3\":{\"value\": \"A1\"},\"thing2\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";

		try {
			token = loginService.getToken("MOMO_OFFICIAL");
			url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
			List<User> users  = dao.getUser(openid);
			if(users.size()>0){
				User user = users.get(0);
				String official_openid = user.getOfficial_openid();
				String studio = user.getStudio();
				if(official_openid != null){
					String[] official_list = official_openid.split(",");
					for(int j=0;j<official_list.length;j++){
						String official_openid_get = official_list[j];
						JSONObject queryJson2 = JSONObject.parseObject(tample6);
						queryJson2.put("touser",official_openid_get);
						queryJson2.getJSONObject("data").getJSONObject("thing1").put("value",studentname);
						queryJson2.getJSONObject("data").getJSONObject("time3").put("value",remindDay + " " + duration.split("-")[0]);
						queryJson2.getJSONObject("data").getJSONObject("thing2").put("value", class_number+"("+studio+")");

						System.out.println("json2:" + queryJson2.toJSONString());
						result = HttpUtil.sendPostJson(url_send,queryJson2.toJSONString());
						System.out.printf("res22:" + result);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	//	获取Openid
	@RequestMapping("/getOpenid")
	@ResponseBody
	public String getOpenid(String code,String app){
		String openid = loginService.getOpenid(code,app);
		return openid;
	}

	//	获取Openid
	@RequestMapping("/getToken")
	@ResponseBody
	public String getToken(String app){
		String token = loginService.getToken(app);
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

	@RequestMapping("/getCommentModel")
	@ResponseBody
	public List getCommentModel(){
		List list = null;
		try {
			list = loginService.getCommentModel();
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

	@RequestMapping("/getLessonPackage")
	@ResponseBody
	public List getLessonPackage(String student_name,String studio,String openid,String subject,String search_type,String duration_time){
		List list = null;
		try {
			list = loginService.getLessonPackage(student_name,studio,subject,search_type,duration_time,openid);
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

	@RequestMapping("/getLessonHead")
	@ResponseBody
	public List getLessonHead(String studio,String student_name,String subject,String openid,String month_date){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getLessonHead(studio,student_name,subject,campus,month_date);
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
			list = loginService.getLessonByPage(studio,student_name,subject,openid,page);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}


	//	获取全部
	@RequestMapping("/getTipsDataUrl")
	@ResponseBody
	public List getTipsDataUrl(String studio,Integer left_amount,String subject,String openid,String type,String month_date){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getTipsDataUrl(studio,left_amount,subject,campus,type,month_date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getGoneStudent")
	@ResponseBody
	public List getGoneStudent(String studio,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getGoneStudent(studio,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getStandings")
	@ResponseBody
	public List getStandings(String studio, String openid, String student_name, String subject){
		List list = null;
		try {
			list = loginService.getStandings(studio,openid,student_name,subject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getAbnormalStudent")
	@ResponseBody
	public List getAbnormalStudent(String studio,String openid,String type){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getAbnormalStudent(studio,campus,type);
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
			list = loginService.getLessonInName(studio,student_name,page,subject,openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取商品列表
	@RequestMapping("/getGoodsList")
	@ResponseBody
	public List getGoodsList(String studio,Integer page,String openid,String content,String type){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getGoodsList(studio,page,campus,content,type);
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
			list = loginService.getRating(studio,student_name,page,subject,openid);
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

	// 获取相册
	@RequestMapping("/getAlbum")
	@ResponseBody
	public List getAlbum(String studio,String openid){
		List list = null;
		try {
			list = loginService.getAlbum(studio,openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getExhibition")
	@ResponseBody
	public List getExhibition(String studio,String type,Integer page){
		List list = null;
		try {
			list = loginService.getExhibition(studio,type,page);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getUpdateNews")
	@ResponseBody
	public List getUpdateNews(){
		List list = null;
		try {
			list = loginService.getUpdateNews();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/updateExchangeByStudio")
	@ResponseBody
	public int updateExchangeByStudio(String studio,String value_num,String type) {
		int result = 0;
		try {
			if("同城".equals(type)){
				result = dao.updateExchangeByStudio(studio,Integer.parseInt(value_num));
			}else if ("网课".equals(type)){
				result = dao.updateTeacherByStudio(studio,Integer.parseInt(value_num));
			}else if("知识".equals(type)){
				result = dao.updateSquareByStudio(studio,Integer.parseInt(value_num));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping("/updateCityByStudio")
	@ResponseBody
	public int updateCityByStudio(String studio,String city) {
		int result = 0;
		try {
			result = dao.updateCityByStudio(studio,city);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping("/updateSubjectByStudio")
	@ResponseBody
	public int updateSubjectByStudio(String studio,String subject) {
		int result = 0;
		try {
			result = dao.updateSubjectByStudio(studio,subject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
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

	@RequestMapping("/getTodayClasses")
	@ResponseBody
	public List getTodayClasses(String date_time, String studio, String openid){
		List list = null;
		try {
			list = loginService.getTodayClasses(date_time,studio,openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getClassByDate")
	@ResponseBody
	public List getClassByDate(String date_time,String studio,String subject,String openid,String test){
		List list = null;
		try {
			list = loginService.getClassByDate(date_time,studio,subject,openid,test);
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
		if("是".equals(type)){
			remind = 0;
		}else if("否".equals(type)){
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

	@RequestMapping("/getOnlineTeacher")
	@ResponseBody
	public List getOnlineTeacher(String type,Integer page,String openid,String id){
		List list = null;
		try {
			list = loginService.getOnlineTeacher(type,page,openid,id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getCommunicateRecord")
	@ResponseBody
	public List getCommunicateRecord(String studio,Integer page,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getCommunicateRecord(studio,page,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getCommunicateLike")
	@ResponseBody
	public List getCommunicateLike(String studio,String item,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getCommunicateLike(studio,item,campus);
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
	public List getSignUp(String student_name,String studio,String subject,String openid){
		List list = null;
		try {
			list = loginService.getSignUp(student_name,studio,subject,openid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取全部
	@RequestMapping("/getStudentByTeacher")
	@ResponseBody
	public List getStudentByTeacher(String studio,String openid,String duration_time,Integer page){
		List list = null;
		try {
			list = loginService.getStudentByTeacher(studio,openid,duration_time,page);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getAnalyzeDetail")
	@ResponseBody
	public List getAnalyzeDetail(String studio,String dimension,String openid,String date_time,String duration_time){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getAnalyzeDetail(studio,dimension,campus,date_time,duration_time);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getAnalyzeDetailWeek")
	@ResponseBody
	public List getAnalyzeDetailWeek(String studio,String type,String weekday,String campus){
		List list = null;
		try {
			list = loginService.getAnalyzeDetailWeek(studio,type,weekday,campus);
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
	public List getLeaveRecord(String student_name,String studio,String leave_type,String subject,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getLeaveRecord(student_name,studio,leave_type,subject,campus);
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
	public List getSignUpByDateDuration(String student_name,String studio,String date_time,String duration,String openid,String subject){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getSignUpByDateDuration(student_name,studio,date_time,duration,campus,subject);
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
			list = dao.getCommentByDateDuration(student_name,studio,date_time,duration,campus,"课评");
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

	@RequestMapping("/getLeaveMessage")
	@ResponseBody
	public List getLeaveMessage(String studio,String type){
		List list = null;
		try {
			list = loginService.getLeaveMessage(studio,type);
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

	@RequestMapping("/getClassStudent")
	@ResponseBody
	public List getClassStudent(String studio,String campus,String type,String subject,String date_time){
		List list = null;
		try {
			list = loginService.getClassStudent(studio,campus,type,subject,date_time);
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
	public List getDetailsUrlByDate(String studio, String duration, String student_name, String date_time,String openid,String class_target_bak){
		List list = null;
		try {
			list = loginService.getDetailsUrlByDate(studio,duration,student_name,date_time,openid,class_target_bak);
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

	@RequestMapping("/getLessonByStudioCampus")
	@ResponseBody
	public List getLessonByStudioCampus(String studio,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getLessonByStudioCampus(studio,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//	获取校区
	@RequestMapping("/getCampusByStudio")
	@ResponseBody
	public List getCampusByStudio(String studio){
		List list = null;
		try {
			list = dao.getCampusByStudio(studio);
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
			list = loginService.getAllUserByStudio(studio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getUserByRole")
	@ResponseBody
	public List getUserByRole(String role){
		List list = null;
		try {
			list = loginService.getUserByRole(role);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping("/getAllUserByStudioByPage")
	@ResponseBody
	public List getAllUserByStudioByPage(String studio,Integer page){
		List list = null;
		try {
			list = loginService.getAllUserByStudioByPage(studio,page);
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
			list = loginService.getUserByNickStudio(nick_name,studio);
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
	public List getArrangements(String studio,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getArrangements(studio,campus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}


	//	获取课程列表
	@RequestMapping("/getArrangementsByDate")
	@ResponseBody
	public List getArrangementsByDate(String studio,String date_time,String openid){
		List list = null;
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			list = loginService.getArrangementsByDate(studio,date_time,campus);
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

	@RequestMapping("/updateVideoTop")
	@ResponseBody
	public int updateVideoTop(Integer id){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String update_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		try {
			dao.updateVideoTop(id,update_time);
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

	@RequestMapping("/deleteCommunicateRecord")
	@ResponseBody
	public int deleteCommunicateRecord(Integer id,String studio,String openid){
		try {
			List<User> list = dao.getUser(openid);
			String studio_get = list.get(0).getStudio();

			if (studio_get.equals(studio)) {
				dao.deleteCommunicateRecord(id,studio);
			}else {
				logger.error("it's not your studio, could not delete!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@RequestMapping("/updateBookMark")
	@ResponseBody
	public int updateBookMark(Integer id,String data,String type){
		try {
			if("时间".equals(type)){
				dao.updateBookCreateTime(id,data);
			}else if("备注".equals(type)){
				dao.updateBookMark(id,data);
			}else if("金额".equals(type)){
				dao.updateBookAmount(id,data);
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

	@RequestMapping("/deletePostComment")
	@ResponseBody
	public int deletePostComment(Integer id){
		try {
			dao.deletePostComment(id);
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

	@RequestMapping("/updateLeaveAllRecord")
	@ResponseBody
	public int updateLeaveAllRecord(String student_name,String studio,String role,String openid,String leave_type,String subject){
		try {
			List<User> list = dao.getUser(openid);
			String campus = list.get(0).getCampus();

			dao.updateLeaveAllRecord(student_name,studio,campus);
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

	@RequestMapping("/cancelBook")
	@ResponseBody
	public int cancelBook(String studio,String student_name,String duration,String class_number,String subject,String openid,String add_date){
		try {
			List<User> list = dao.getUser(openid);
			String campus = list.get(0).getCampus();
			dao.cancelBook(add_date,duration,studio,class_number,subject,campus,student_name);
			List<User> users = dao.getBossByStudio(studio);
			for(int j=0;j<users.size();j++){
				User user = users.get(j);
				String openid_get = user.getOpenid();
				sendBookCancel(openid_get,duration,student_name,add_date,class_number);
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

		try {
			List<User> users = dao.getUser(openid);
			String nick_name = users.get(0).getNick_name();

			SignUp signUp = new SignUp();
			signUp.setStudent_name(student_name);
			signUp.setStudio(studio);
			signUp.setSign_time(update_time);
			signUp.setMark("划课_"+mark);
			signUp.setCount(Float.parseFloat(consume_lesson_amount));
			signUp.setSubject(subject);
			signUp.setTeacher(nick_name);
			signUp.setCreate_time(update_time);
			signUp.setDuration("00:00:00");
			signUp.setClass_number("无班号");
			List<User> list = dao.getUser(openid);
			String campus = list.get(0).getCampus();
			signUp.setCampus(campus);

			loginService.insertSignUp(signUp);
		} catch (Exception e) {
//			throw new RuntimeException(e);
		}
		return 1;
	}

	@RequestMapping("/insertAnnouncement")
	@ResponseBody
	public int insertAnnouncement(String studio,String content,String title){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		Announcement announcement = new Announcement();
		announcement.setCreate_time(create_time);
		announcement.setStudio(studio);
		announcement.setContent(content);
		announcement.setTitle(title);

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
		String nick_name = list_user.get(0).getNick_name();

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
			signUp.setTeacher(nick_name);
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
		String nick_name =list_user.get(0).getNick_name();

		try {
			d = fmt.parse(date_time);
			Calendar cal = Calendar.getInstance();
			cal.setTime(d);
			Integer weekDay = cal.get(Calendar.DAY_OF_WEEK);

			List<Schedule> schedules = dao.getScheduleDetailAll_O(weekDay,duration,studio,class_number,subject,campus,date_time);
			List<Schedule> schedules_t = dao.getScheduleDetailAll_T(weekDay,duration,studio,class_number,subject,campus,date_time);
			schedules.addAll(schedules_t);

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


				signUp.setStudent_name(student_name);
				signUp.setStudio(studio);
				signUp.setSign_time(update_time);
				signUp.setCreate_time(date_time + " 00:00:00");
				signUp.setMark(mark);
				signUp.setDuration(duration);
				signUp.setCount(count);
				signUp.setSubject(subject);
				signUp.setTeacher(nick_name);
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
	public int leaveRecord(String student_name,String studio,String date_time,String duration,String leave_type,String mark_leave,String subject,String makeup_date,String openid){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			String teacher = list_user.get(0).getNick_name();
			String official_openid = list_user.get(0).getOfficial_openid();
			Leave leave =new Leave();
			leave.setStudent_name(student_name);
			leave.setStudio(studio);
			leave.setDate_time(date_time);
			leave.setDuration(duration);
			leave.setCreate_time(create_time);
			leave.setMark_leave(mark_leave);
			leave.setSubject(subject);
			if(makeup_date == null){
				makeup_date = "无";
			}
			leave.setMakeup_date(makeup_date);
			leave.setCampus(campus);
			if(leave_type == null || leave_type.isEmpty() || "undefined".equals(leave_type)){
				leave_type = "请假";
			}
			leave.setLeave_type(leave_type);
			int result = dao.insertLeave(leave);

			// 判断满几次抠课时
			if(result>0){
				loginService.leaveRemind(official_openid,student_name,studio,subject,duration,date_time,mark_leave);
				List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
				Float leave_times = lessons.get(0).getLeave_times();
				List<Leave> leaves = dao.getLeaveRecordByStatus(student_name,studio,subject,campus);
				Float leave_counts = 0.0f;
				if(leaves.size()>0){
					for(int i =0; i<leaves.size();i++){
						leave_counts = leave_counts + 1.0f;
					}
				}
				if(leave_counts.equals(leave_times) && leave_times != 0.0f){
					SignUp signUp = new SignUp();
					signUp.setStudent_name(student_name);
					signUp.setStudio(studio);
					signUp.setSign_time(create_time);
					signUp.setMark("请假"+leave_times+"次数扣1课时");
					signUp.setCount(1.0f);
					signUp.setSubject(subject);
					signUp.setTeacher(teacher);
					signUp.setCreate_time(create_time);
					signUp.setDuration("00:00:00");
					signUp.setClass_number("无班号");
					signUp.setCampus(campus);
					int result1 = dao.insertSignUp(signUp);
					if(result1>0){
						loginService.updateMinusLesson(student_name,studio,1.0f,subject,campus);
						dao.updateLeaveAllRecord(student_name,studio,campus);
					}
				}
			}

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
		String nick_name= list_user.get(0).getNick_name();

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
			lesson.setPrice(0.00f);
			lesson.setMinus(1.00f);
			lesson.setCampus(campus);
			dao.insertLesson(lesson);

			LessonPackage lessonPackage = new LessonPackage();
			lessonPackage.setStudent_name(student_name);
			lessonPackage.setStart_date(create_time);
			lessonPackage.setEnd_date(create_time);
			lessonPackage.setCampus(campus);
			lessonPackage.setStudio(studio);
			lessonPackage.setSubject(subject);
			lessonPackage.setCreate_time(create_time);
			lessonPackage.setAll_lesson(Float.parseFloat(total_amount));
			lessonPackage.setTotal_money(0.0f);
			lessonPackage.setDiscount_money(0.0f);
			lessonPackage.setMark("初次录入");
			lessonPackage.setGive_lesson(0.0f);
			lessonPackage.setNick_name(nick_name);
			dao.insertLessonPackage(lessonPackage);

			if(lesson.getTotal_amount() - lesson.getLeft_amount() > 0.0f){
				SignUp signUp = new SignUp();
				signUp.setStudio(studio);
				signUp.setSign_time(create_time);
				signUp.setMark("录前消课");
				signUp.setCount(0.0f);
				signUp.setTeacher(nick_name);
				signUp.setCreate_time(create_time);
				signUp.setDuration("00:00:00");
				signUp.setClass_number("无班号");
				signUp.setCampus(campus);
				signUp.setStudent_name(student_name);
				signUp.setSubject(subject);
				List<SignUp> signUps_list = dao.getSignUpByBacth(student_name,studio,subject,campus);
				if(signUps_list.size()==0){
					signUp.setCount(lesson.getTotal_amount() - lesson.getLeft_amount());
					loginService.insertSignUp(signUp);
				}
			}
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

	@RequestMapping("/updateFinal")
	@ResponseBody
	public int updateFinal(HttpServletRequest request, HttpServletResponse response){

		//获取文字
		String studio = request.getParameter("studio");

		String openid = request.getParameter("openid");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();

		String final_time = request.getParameter("final_time");

		String type = request.getParameter("type");

		try {
			if("final_time".equals(type)){
				dao.updateFinalTime(studio,campus,final_time);
			}else if("leave_times".equals(type)){
				dao.updateLeaveTimes(studio,campus,final_time);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/updateLocation")
	@ResponseBody
	public int updateLocation(HttpServletRequest request, HttpServletResponse response){

		String openid = request.getParameter("openid");
		String type = request.getParameter("type");
		String content = request.getParameter("content");
		String id = request.getParameter("id");

		try {
			if("昵称".equals(type)){
				dao.updateNewName(openid,content);
			}else if("电话".equals(type)) {
				dao.updatePhoneNumber(openid,content);
			}else if("地址".equals(type)) {
				dao.updateLocation(openid,content);
			}else if("更新学生".equals(type)){
				dao.updateUserStudentByOpenid(content,openid,id);
			} else if("学生".equals(type)) {
				List<User> users = dao.getUserByOpenid(openid);
				User user = users.get(0);
				String studio =user.getStudio();
				List<User> users1 = dao.getUserByStudentOpenid("no_name",studio,openid);
				if(users1.size()>0){
					dao.updateUserStudentName(openid,content);
				}else {
					List<User> users2 = dao.getUserByStudentOpenid(content,studio,openid);
					if(users2.size()==0){
						user.setStudent_name(content);
						int update_res = dao.updateUserDelete(user);
						if(update_res==0 && openid.length() == 28 && studio.length() > 0){
							dao.insertUser(user);
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 1;
	}

	@RequestMapping("/updateCombine")
	@ResponseBody
	public int updateCombine(HttpServletRequest request, HttpServletResponse response){

		String studio = request.getParameter("studio");
		String student_name = request.getParameter("student_name");
		String openid = request.getParameter("openid");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();
		String is_combine = request.getParameter("is_combine");
		Integer combine = 0;
		if("分".equals(is_combine)){
			combine = 1;
		}

		Lesson lesson = new Lesson();
		lesson.setIs_combine(combine);
		lesson.setStudio(studio);
		lesson.setCampus(campus);
		lesson.setStudent_name(student_name);


		try {
			dao.updateCombine(lesson);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;

	}

	@RequestMapping("/updateMinusByType")
	@ResponseBody
	public int updateMinusByType(HttpServletRequest request, HttpServletResponse response) {
		String studio = request.getParameter("studio");
		String class_number = request.getParameter("class_number");
		String duration = request.getParameter("duration");
		String subject = request.getParameter("subject");
		String minus = request.getParameter("minus");
		String minus_type = request.getParameter("minus_type");
		String minus_student = request.getParameter("minus_student");
		String openid = request.getParameter("openid");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();

		if (minus_type.equals("single")) {
			dao.updateLessonMinus(Float.parseFloat(minus), studio, minus_student, campus, subject);
		} else if (minus_type.equals("class")) {
			String[] student_list = minus_student.split(",");
			for (int i = 0; i < student_list.length; i++) {
				String student_name = student_list[i];
				dao.updateLessonMinus(Float.parseFloat(minus), studio, student_name, campus, subject);
			}
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
			List<Message> list = dao.getUuidById(Integer.parseInt(id));
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

	@RequestMapping("/updateBackUrl")
	@ResponseBody
	public int updateBackUrl(HttpServletRequest request, HttpServletResponse response){

		//获取文字
		String class_target = request.getParameter("class_target");
		String studio = request.getParameter("studio");
		String openid = request.getParameter("openid");
		String uuids = request.getParameter("uuids");

		User user = new User();
		user.setBack_uuid(uuids);
		user.setOpenid(openid);

		try {
			dao.updateUserBackUrl(user);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return 1;

	}

	@RequestMapping("/updateCommunicate")
	@ResponseBody
	public int updateCommunicate(HttpServletRequest request, HttpServletResponse response){

		//获取文字
		String id = request.getParameter("id");
		String content = request.getParameter("content");
		String uuids = request.getParameter("uuids").replace("\"","").replace("[","").replace("]","");

		try {
			if("modify_content".equals(uuids)){
				dao.updateCommunicateContent(id,content);
			}else if("modify_uuids".equals(content)){
				List<CommunicateRecord> communicateRecords = dao.getCommunicateById(id);
				String uuids_get = communicateRecords.get(0).getUuids();
				if(uuids_get != null){
					if(uuids_get.length() > 5){
						uuids = uuids_get + "," + uuids;
						dao.updateCommunicateUuids(id,uuids);
					}else{
						dao.updateCommunicateUuids(id,uuids);
					}
				}else{
					dao.updateCommunicateUuids(id,uuids);
				}
			}else if("update_uuids".equals(content)){
				List<CommunicateRecord> communicateRecords = dao.getCommunicateById(id);
				String uuids_get = communicateRecords.get(0).getUuids();
				if(uuids_get != null){
					String[] result = uuids_get.split(",");
					List<String> list_new = new ArrayList<>();
					for(int i =0;i<result.length;i++){
						if(!result[i].equals(uuids)){
							list_new.add(result[i]);
						}
					}
					dao.updateCommunicateUuids(id,list_new.toString().replace(" ","").replace("[","").replace("]",""));
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
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

	@RequestMapping("/modifyLesson")
	@ResponseBody
	public int modifyLesson(HttpServletRequest request, HttpServletResponse response){

		//获取文字
		String id = request.getParameter("id");

		String content = request.getParameter("content");

		String modifyHead = request.getParameter("modifyHead");

		List<Lesson> lessons = dao.getLessonById(Integer.parseInt(id));
		Lesson lesson = lessons.get(0);
		String studio = lesson.getStudio();
		String student_name = lesson.getStudent_name();
		String campus = lesson.getCampus();
		String subject = lesson.getSubject();

		try {
			if("阶段".equals(modifyHead)){
				dao.updateLessonAgeById(id,content);
			}else if("科目".equals(modifyHead)){
				dao.updateLessonSubjectById(id,content);
				dao.updateSignUpSubject(studio,student_name,campus,subject,content);
			}else if("校区".equals(modifyHead)){
				dao.updateLessonCampusById(id,content);
				dao.updateSignUpCampus(studio,student_name,content);
//				dao.updateGiftCampus(studio,student_name,content);
//				dao.updateNoteCampus(studio,student_name,content);
//				dao.updateLeaveCampus(studio,student_name,content);
			}else if("学生名".equals(modifyHead)){
				dao.updateLessonStudentNameById(id,content);
				dao.updateScheduleName(content,student_name,studio,campus,subject);
				dao.updateSignUpRecordName(content,student_name,studio,campus,subject);
				dao.updateLessonPackageName(content,student_name,studio,campus,subject);
			}
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

			List<Arrangement> arrangement_list = dao.getArrangementByDate(studio,dayofweek,class_number,duration,subject,campus);
			if(arrangement_list.size() == 0){
				Arrangement arrangement =new Arrangement();
				arrangement.setDayofweek(dayofweek);
				arrangement.setClass_number(class_number);
				arrangement.setDuration(duration);
				arrangement.setLimits(limits);
				arrangement.setStudio(studio);
				arrangement.setSubject(subject);
				arrangement.setCampus(campus);
				loginService.insertArrangement(arrangement);
			}

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
					List<Schedule> check_schedule = dao.getScheduleCheck(add_date,duration,class_number,subject,studio,campus,list_student);
					if(check_schedule.size()==0){
						loginService.insertSchedule(schedule);
					}
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

	@RequestMapping("/insertLessonPackage")
	@ResponseBody
	public int insertLessonPackage(HttpServletRequest request, HttpServletResponse response){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String studio =  request.getParameter("studio");
		String student_name =  request.getParameter("student_name");
		String mark =  request.getParameter("mark");
		String openid =  request.getParameter("openid");
		List<User> list = dao.getUser(openid);
		String campus = list.get(0).getCampus();
		String nick_name = list.get(0).getNick_name();
		String total_money =  request.getParameter("total_money");
		String discount_money =  request.getParameter("discount_money");
		String start_date =  request.getParameter("start_date");
		String end_date =  request.getParameter("end_date");
		String all_lesson =  request.getParameter("all_lesson");
		String give_lesson =  request.getParameter("give_lesson");
		String subject =  request.getParameter("subject");

		try {
			LessonPackage lessonPackage = new LessonPackage();
			lessonPackage.setStudent_name(student_name);
			lessonPackage.setTotal_money(Float.parseFloat(total_money));
			lessonPackage.setDiscount_money(Float.parseFloat(discount_money));
			lessonPackage.setMark(mark);
			lessonPackage.setStart_date(start_date);
			lessonPackage.setEnd_date(end_date);
			lessonPackage.setCampus(campus);
			lessonPackage.setStudio(studio);
			lessonPackage.setSubject(subject);
			lessonPackage.setCreate_time(create_time);
			lessonPackage.setAll_lesson(Float.parseFloat(all_lesson));
			lessonPackage.setGive_lesson(Float.parseFloat(give_lesson));
			lessonPackage.setNick_name(nick_name);

			dao.insertLessonPackage(lessonPackage);
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

	@RequestMapping("/recoverLesson")
	@ResponseBody
	public int recoverLesson(Integer id,String studio,String openid,String type){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
		String today_time = df.format(new Date());
		try {
			List<Lesson> lessons = dao.getLessonById(id);
			Lesson lesson = lessons.get(0);
			String subject = lesson.getSubject();
			String campus = lesson.getCampus();
			String student_name = lesson.getStudent_name();

			if("恢复".equals(type)){
				dao.recoverLesson(id,studio,today_time);
				dao.recoverLessonPackageByName(student_name,studio,subject,campus);
			}else if("永久删除".equals(type)){
				dao.deleteLessonForever(id,studio);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@RequestMapping("/deleteLessonPackage")
	@ResponseBody
	public int deleteLessonPackage(Integer id,String type){
		try {
			loginService.deleteLessonPackage(id,type);
		} catch (Exception e) {
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
//		String path = System.getProperty("user.dir");
		String path = "/data";
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
//		String path = System.getProperty("user.dir");
		String path = "/data";
		//获取图片
		MultipartHttpServletRequest req = (MultipartHttpServletRequest)request;
		MultipartFile multipartFile = req.getFile("video");
		String studio =  request.getParameter("studio");
		String d_path = path +"/uploadVideo/"+ studio + "/" ;
		File file = new File(d_path);
		if (!file.exists()){ //如果不存在
			boolean dr = file.mkdirs(); //创建目录
		}

//		String[] content = file.list();//取得当前目录下所有文件和文件夹
//		for(String name : content){
//			File temp = new File(d_path, name);
//			temp.delete();
//		}

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
		String path = "/data";
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
			bw.write("科目,学生名,总课时,余课时,积分");
			bw.newLine();
			for(int i=0; i<lessons.size(); i++){
				String subject = lessons.get(i).getSubject();
				String student_name = lessons.get(i).getStudent_name();
				String total_amount = lessons.get(i).getTotal_amount().toString();
				String left_amount = lessons.get(i).getLeft_amount().toString();
				String points = lessons.get(i).getPoints().toString();
				bw.write(subject+","+student_name+","+total_amount+","+left_amount+","+points);
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
//		String path = System.getProperty("user.dir");
		String path = "/data";
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
//		String path = System.getProperty("user.dir");
		String path = "/data";
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
//		String path = System.getProperty("user.dir");
		String path = "/data";
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
		String path = "/data";
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

	@RequestMapping("/getDownloadSignUpByType")
	@ResponseBody
	public ResponseEntity<byte[]> getDownloadSignUpByType(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String openid =  request.getParameter("openid");
		String studio =  request.getParameter("studio");
		String type =  request.getParameter("type");
		String path = "/data/downloadData/"+ studio + "/"+ openid + "/"+ type + ".xls" ;

		if("all".equals(type)){
			loginService.getSignUpByAll(studio,openid);
		}

		File file = new File(path);
		if(file.exists()){
			org.springframework.http.HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("form", file.getName());
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
//		String path = System.getProperty("user.dir");
		String path = "/data";
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
//		String path = System.getProperty("user.dir");
		String path = "/data";
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
//		String path = System.getProperty("user.dir");
		String path = "/data";
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
		String total_amount= "0";
		String left_amount= "0";
		String gift_name =null;
		String gift_amount = "0";
		String points = "0";
//		String path = System.getProperty("user.dir");
		String path = "/data";
		String path_1 = path +"/uploadexcel/" + studio ;
		java.io.File myFilePath = new java.io.File(path_1);
		String[] tempList = myFilePath.list();
		File temp = new File(path_1 + "/" + tempList[0]);
		try {
			List<User> list_user = dao.getUser(openid);
			String campus = list_user.get(0).getCampus();
			String nick_name = list_user.get(0).getNick_name();
			Workbook book=Workbook.getWorkbook(temp);
			Sheet sheet=book.getSheet(0);
			for(int i=1;i<sheet.getRows();i++){
				LessonPackage lessonPackage = new LessonPackage();
				lessonPackage.setCreate_time(create_time);
				lessonPackage.setStart_date(create_time);
				lessonPackage.setMark("批量录入");
				lessonPackage.setStudio(studio);
				lessonPackage.setCampus(campus);
				lessonPackage.setTotal_money(0.0f);
				lessonPackage.setDiscount_money(0.0f);
				lessonPackage.setAll_lesson(0.0f);
				lessonPackage.setGive_lesson(0.0f);
				lessonPackage.setNick_name(nick_name);

				Gift gift = new Gift();
				gift.setCreate_time(create_time);
				gift.setStudio(studio);
				gift.setStatus(0);
				gift.setCampus(campus);
				gift.setExpired_time(create_time);

				SignUp signUp = new SignUp();
				signUp.setStudio(studio);
				signUp.setSign_time(create_time);
				signUp.setMark("录前消课");
				signUp.setCount(0.0f);
				signUp.setTeacher(nick_name);
				signUp.setCreate_time(create_time);
				signUp.setDuration("00:00:00");
				signUp.setClass_number("无班号");
				signUp.setCampus(campus);

				Lesson lesson =new Lesson();
				lesson.setCreate_time(create_time);
				lesson.setStudio(studio);
				lesson.setMinus(1.0f);
				lesson.setCoins(0.0f);
				lesson.setPrice(0.0f);
				lesson.setTotal_money(0.0f);
				lesson.setDiscount_money(0.0f);
				lesson.setPoints(0);
				lesson.setCampus(campus);
				Cell cell_get=sheet.getCell(0, i);
				try {
					if(!cell_get.getContents().isEmpty()){
						for(int j=0;j<sheet.getColumns();j++){
							Cell cell=sheet.getCell(j, i);
							if(0==j){
								subject = cell.getContents();
								lesson.setSubject(subject);
								lessonPackage.setSubject(subject);
								signUp.setSubject(subject);
							}else if(1==j){
								student_name = cell.getContents();
								lesson.setStudent_name(student_name);
								gift.setStudent_name(student_name);
								lessonPackage.setStudent_name(student_name);
								signUp.setStudent_name(student_name);
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
								}else{
									lesson.setLeft_amount(0.0f);
								}
							}else if (4==j){
								gift_name = cell.getContents();
								gift.setGift_name(gift_name);
							}else if(5==j){
								gift_amount =cell.getContents();
								if(!gift_amount.isEmpty()){
									gift.setGift_amount(Integer.parseInt(gift_amount));
								}else{
									gift.setGift_amount(0);
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
							}else if(7==j){
								points =cell.getContents();
								if(!points.isEmpty()){
									lesson.setPoints(Integer.parseInt(points));
								}
							}else if(8==j){
								String total_money =cell.getContents();
								if(!total_money.isEmpty()){
									lessonPackage.setTotal_money(Float.parseFloat(total_money));
								}
							}else if(9==j){
								String discount_money =cell.getContents();
								if(!discount_money.isEmpty()){
									lessonPackage.setDiscount_money(Float.parseFloat(discount_money));
								}
							}else if(10==j){
								String start_date =cell.getContents();
								if(!start_date.isEmpty()){
									lessonPackage.setStart_date(start_date);
								}
							}else if(11==j){
								String end_date =cell.getContents();
								if(!end_date.isEmpty()){
									lessonPackage.setEnd_date(end_date);
								}
							}else if(12==j){
								String mark =cell.getContents();
								if(!mark.isEmpty()){
									lessonPackage.setMark(mark);
								}
							}
							else if(13==j){
								String all_lesson =cell.getContents();
								if(!all_lesson.isEmpty()){
									lessonPackage.setAll_lesson(Float.parseFloat(all_lesson));
								}
							}else if(14==j){
								String give_lesson =cell.getContents();
								if(!give_lesson.isEmpty()){
									lessonPackage.setGive_lesson(Float.parseFloat(give_lesson));
								}
							}else if(15==j){
								String age =cell.getContents();
								if(!age.isEmpty()){
									lesson.setAge(age);
								}
							}

						}
					}
				} catch (NumberFormatException e) {
//					throw new RuntimeException(e);
				}

				if(student_name != null){
					List<Lesson> lessons_get = dao.getLessonByNameSubject(student_name,studio,subject,campus);
					if(lessons_get.size()==0){
						loginService.insertLesson(lesson);
					}else{
						loginService.updateLesson(lesson,0.0f,0.0f,"全科目",campus);
					}
				}

				if (!gift.getGift_name().isEmpty()){
					loginService.insertGift(gift);
				}

				if (lessonPackage.getAll_lesson() != 0.0f){
					List<LessonPackage> lessonPackages_list = dao.getLessonPackageByStudentSubjectBatch(student_name,studio,campus,subject);
					if(lessonPackages_list.size()==0){
						dao.insertLessonPackage(lessonPackage);
					}else{
						dao.updateLessonPackageByStudent(lessonPackage.getTotal_money(),lessonPackage.getDiscount_money(),lessonPackage.getAll_lesson(),lessonPackage.getGive_lesson(),student_name,studio,campus,subject);
					}
				}

				if(lesson.getTotal_amount() - lesson.getLeft_amount() > 0.0f){
					List<SignUp> signUps_list = dao.getSignUpByBacth(student_name,studio,subject,campus);
					Float count = lesson.getTotal_amount() - lesson.getLeft_amount();
					if(signUps_list.size()==0){
						signUp.setCount(count);
						loginService.insertSignUp(signUp);
					}else {
						dao.updateSignUpByBacth(count,studio,student_name,subject,campus);
					}
				}
			}

		} catch (BiffException e) {
//			e.printStackTrace();
			return "skip null successfully";
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
			positive_get = "积极性_1";
		}
//		Integer positive = Integer.parseInt(positive_get);


		//获取纪律性
		String discipline_get = request.getParameter("discipline");
		if(discipline_get == null || discipline_get.isEmpty() || "undefined".equals(discipline_get)){
			discipline_get = "纪律性_1";
		}
//		Integer discipline = Integer.parseInt(discipline_get);

		//获取开心值
		String happiness_get = request.getParameter("happiness");
		if(happiness_get == null || happiness_get.isEmpty() || "undefined".equals(happiness_get)){
			happiness_get = "开心值_1";
		}
//		Integer happiness = Integer.parseInt(happiness_get);


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

		String vuuid = request.getParameter("vuuid");
		if(vuuid == null || vuuid.isEmpty() || "undefined".equals(vuuid)){
			vuuid = "no_vuuids";
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
		message.setPositive(positive_get);
		message.setDiscipline(discipline_get);
		message.setHappiness(happiness_get);
		message.setMp3_url(mp3_url);
		message.setUuids(uuids);
		message.setCampus(campus);
		message.setVuuid(vuuid);
		message.setOpenid(openid);


		if("课程体系".equals(class_target) || "环境".equals(class_target) || "广告".equals(class_target) ){
			if("noid".equals(id)){
				try {
					loginService.push(message);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}else{
				List<Message> list = dao.getUuidById(Integer.parseInt(id));
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
				if("礼品乐园".equals(class_target)){
					dao.deleteStudentPhoto(student_name,studio);
				}
				if("主页".equals(class_target)){
					dao.deleteHome(studio);
				}
				loginService.push(message);
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
		String mp3_url = request.getParameter("mp3_url");
		String type = request.getParameter("type");

		PostComment postComment = new PostComment();
		postComment.setOpenid(openid);
		postComment.setPost_id(post_id);
		postComment.setContent(content);
		postComment.setStudio(studio);
		postComment.setCreate_time(create_time);
		postComment.setType(type);
		postComment.setMp3_url(mp3_url);

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
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

		String studio = request.getParameter("studio");
		String mark = request.getParameter("mark");
		String type = request.getParameter("type");
		String amount = request.getParameter("amount");
		String openid = request.getParameter("openid");
		String student_name = request.getParameter("student_name");
		String content = request.getParameter("content");
		String class_target = request.getParameter("class_target");
		String uuids = request.getParameter("uuids").replace("\"","").replace("[","").replace("]","");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();
		String nick_name = list_user.get(0).getNick_name();

		if("账本".equals(class_target)){
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
		}else if("沟通记录".equals(class_target)){
			CommunicateRecord communicateRecord = new CommunicateRecord();
			communicateRecord.setStudent_name(student_name);
			communicateRecord.setStudio(studio);
			communicateRecord.setCampus(campus);
			communicateRecord.setContent(content);
			communicateRecord.setOpenid(nick_name);
			communicateRecord.setCreate_time(create_time);
			communicateRecord.setUuids(uuids);
			try {
				dao.insertCommunicateRecord(communicateRecord);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
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

		String openid = request.getParameter("openid");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();

		//获取学生类型
		String student_type = request.getParameter("student_type");

		String studio = request.getParameter("studio");

		String is_try = request.getParameter("is_try");

		String weekofday = request.getParameter("weekofday");

		String class_number = request.getParameter("class_number");
		if(class_number == null || class_number.isEmpty() || "undefined".equals(class_number)){
			class_number = "无班号";
		}

		String subject = request.getParameter("subject");
		if(subject == null || subject.isEmpty() || "undefined".equals(subject)){
			subject = "美术";
		}

		Integer status = 1;

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
				schedule.setIs_try(Integer.parseInt(is_try));
				schedule.setCampus(campus);
				List<Schedule> check_schedule = dao.getScheduleCheck(add_date,duration,class_number,subject,studio,campus,list_student);
				if(check_schedule.size()==0){
					loginService.insertSchedule(schedule);
					if("星期8".equals(weekofday)){
						List<User> users = dao.getBossByStudio(studio);
						for(int j=0;j<users.size();j++){
							User user = users.get(j);
							String openid_get = user.getOpenid();
							sendBookSuccess(openid_get,duration,student_name,add_date,class_number);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	public String sendBookSuccess(String openid, String duration, String student_name,String remindDay,String class_number){
		String result = null;
		String url_send = null;
		String token = null;
		String tample6 ="{\"touser\":\"openid\",\"template_id\":\"MQMjxZmSoXPg2esFC0nJjL2D4wAmiZkF1mrdf76sDe4\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing3\":{\"value\": \"AA\"},\"time5\":{\"value\": \"time\"},\"thing7\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";

		try {
			token = loginService.getToken("MOMO_OFFICIAL");
			url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
			List<User> users  = dao.getUser(openid);
			if(users.size()>0){
				User user = users.get(0);
				String official_openid = user.getOfficial_openid();
				String studio = user.getStudio();
				if(official_openid != null){
					String[] official_list = official_openid.split(",");
					for(int j=0;j<official_list.length;j++){
						String official_openid_get = official_list[j];
						JSONObject queryJson2 = JSONObject.parseObject(tample6);
						queryJson2.put("touser",official_openid_get);
						queryJson2.getJSONObject("data").getJSONObject("thing3").put("value",class_number);
						queryJson2.getJSONObject("data").getJSONObject("time5").put("value",remindDay + " " + duration.split("-")[0]);
						queryJson2.getJSONObject("data").getJSONObject("thing7").put("value", student_name);

						System.out.println("json2:" + queryJson2.toJSONString());
						result = HttpUtil.sendPostJson(url_send,queryJson2.toJSONString());
						System.out.printf("res22:" + result);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public String sendBookCancel(String openid, String duration, String student_name,String remindDay,String class_number){
		String result = null;
		String url_send = null;
		String token = null;
		String tample6 ="{\"touser\":\"openid\",\"template_id\":\"u3Of-LSXGe4IbV9h5MvA03XHunT6pxM26ljfUA6Nduo\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing3\":{\"value\": \"AA\"},\"time9\":{\"value\": \"time\"},\"thing2\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";

		try {
			token = loginService.getToken("MOMO_OFFICIAL");
			url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
			List<User> users  = dao.getUser(openid);
			if(users.size()>0){
				User user = users.get(0);
				String official_openid = user.getOfficial_openid();
				String studio = user.getStudio();
				if(official_openid != null){
					String[] official_list = official_openid.split(",");
					for(int j=0;j<official_list.length;j++){
						String official_openid_get = official_list[j];
						JSONObject queryJson2 = JSONObject.parseObject(tample6);
						queryJson2.put("touser",official_openid_get);
						queryJson2.getJSONObject("data").getJSONObject("thing3").put("value",class_number);
						queryJson2.getJSONObject("data").getJSONObject("time9").put("value",remindDay + " " + duration.split("-")[0]);
						queryJson2.getJSONObject("data").getJSONObject("thing2").put("value", student_name);

						System.out.println("json2:" + queryJson2.toJSONString());
						result = HttpUtil.sendPostJson(url_send,queryJson2.toJSONString());
						System.out.printf("res22:" + result);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
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
		String is_try = "0";

		try {
			student_type = request.getParameter("student_type");
			if(student_type == null || student_type.isEmpty() || "undefined".equals(student_type)){
				student_type = "ordinary";
			}
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
		try {
			is_try = list_get[4];
		} catch (Exception e) {
//			throw new RuntimeException(e);
		}

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
				schedule.setIs_try(Integer.parseInt(is_try));
				List<Schedule> check_schedule = dao.getScheduleCheck(add_date,duration,class_number,subject,studio,campus,list_student);
				if(check_schedule.size()==0){
					loginService.insertSchedule(schedule);
				}
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
		String remind_type = "统一提醒次日";
		Integer hours = 0;
		String comment_style = "public";

		if(list_send.size()>0){
			send_time = list_send.get(0).getSend_time();
			display = list_send.get(0).getDisplay();
			cover = list_send.get(0).getCover();
			remind_type = list_send.get(0).getRemind_type();
			hours = list_send.get(0).getHours();
			comment_style = list_send.get(0).getComment_style();
		}

		User user =new User();
		user.setNick_name(nick_name);
		user.setStudent_name(student_name);
		user.setOpenid(openid);
		user.setCreate_time(create_time);
		user.setAvatarurl(avatarurl);
		user.setStudio(studio);
		user.setExpired_time(expired_time);
		user.setCampus(campus);
		user.setComment_style(comment_style);
		user.setSend_time(send_time);
		user.setDisplay(display);
		user.setCover(cover);
		user.setRemind_type(remind_type);
		user.setHours(hours);
		List<User> list= dao.getUser(openid);
		if(list.size()>0){
			String role_get = list.get(0).getRole();
			user.setRole(role_get);
			int res = loginService.updateUser(user);
			if(res > 0 && !student_name.equals("no_name")){
				String user_type_get = list.get(0).getUser_type();
				String phone_number = list.get(0).getPhone_number();
				String location = list.get(0).getLocation();
				user.setUser_type(user_type_get);
				user.setPhone_number(phone_number);
				user.setLocation(location);
				try {
					if("client".equals(role_get)){
						int update_res = dao.updateUserDelete(user);
						if(update_res==0 && openid.length() == 28 && studio.length() > 0){
							dao.insertUser(user);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}else{
			user.setUser_type("新用户");
			user.setRole("client");
			try {
				int update_res = dao.updateUserDelete(user);
				if(update_res==0 && openid.length() == 28 && studio.length() > 0){
					dao.insertUser(user);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return "push massage successfully";
	}

	@RequestMapping("/webInsertUser")
	@ResponseBody
	public String webInsertUser(HttpServletRequest request, HttpServletResponse response){
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

		String studio = request.getParameter("studio");

		String phone_number = request.getParameter("phone_number");

		String campus = request.getParameter("campus");

		//获取学生名
		String student_name = request.getParameter("student_name");

		//获取 openid
		String openid = request.getParameter("openid");
		if(openid == null || openid.isEmpty() || "undefined".equals(openid)){
			openid = DigestUtils.md5Hex(nick_name + studio + phone_number);
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
		String remind_type = "统一提醒次日";
		Integer hours = 0;
		String comment_style = "public";

		if(list_send.size()>0){
			send_time = list_send.get(0).getSend_time();
			display = list_send.get(0).getDisplay();
			cover = list_send.get(0).getCover();
			remind_type = list_send.get(0).getRemind_type();
			hours = list_send.get(0).getHours();
			comment_style = list_send.get(0).getComment_style();
		}

		User user =new User();
		user.setNick_name(nick_name);
		user.setStudent_name(student_name);
		user.setOpenid(openid);
		user.setCreate_time(create_time);
		user.setAvatarurl(avatarurl);
		user.setStudio(studio);
		user.setExpired_time(expired_time);
		user.setCampus(campus);
		user.setComment_style(comment_style);
		user.setSend_time(send_time);
		user.setDisplay(display);
		user.setCover(cover);
		user.setRemind_type(remind_type);
		user.setHours(hours);
		user.setPhone_number(phone_number);
		user.setUser_type("新用户");
		user.setRole("client");
		String result = "新用户注册成功！";
		try {
			List<User> users = dao.getUserByNickStudioEq(phone_number,studio);
			if(users.size()>0){
				result = "电话号码已被注册！";
			}else {
				int update_res = dao.updateUserDelete(user);
				if(update_res==0 && studio.length() > 0){
					dao.insertUser(user);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
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
				String openid = openid_get.split("@")[0];
				String id = openid_get.split("@")[1];
				String[] student_list = campus.split(" ");
				if(student_list.length == 1){
					dao.updateUserStudentByOpenid(campus,openid,id);
				}else if(student_list.length > 1){
					for(int i=0; i < student_list.length;i++){
						String name = student_list[i];
						if(i == 0){
							dao.updateUserStudentByOpenid(name,openid,id);
						}else{
							List<User> users = dao.getUser(openid);
							User user = users.get(0);
							user.setStudent_name(name);
							String studio = user.getStudio();
							int update_res = dao.updateUserDelete(user);
							if(update_res==0 && openid.length() == 28 && studio.length() > 0){
								dao.insertUser(user);
							}
						}
					}
				}

			}else if("工作室".equals(type)){
				User user =new User();
				user.setOpenid(openid_get);
				user.setStudio(campus);
				dao.updateUserStudioByOpenid(user);
			}else if("电话".equals(type)){
				dao.updatePhoneNumber(openid_get,campus);
			}else if("新孩".equals(type)){
                List<User> users = dao.getUserByOpenid(openid_get);
                User user = users.get(0);
                String studio =user.getStudio();
                List<User> users1 = dao.getUserByStudentOpenid("no_name",studio,openid_get);
                if(users1.size()>0){
                    dao.updateUserStudentName(openid_get,campus);
                }else {
                    List<User> users2 = dao.getUserByStudentOpenid(campus,studio,openid_get);
                    if(users2.size()==0){
                        user.setStudent_name(campus);
                        int update_res = dao.updateUserDelete(user);
                        if(update_res==0 && openid_get.length() == 28 && studio.length() > 0){
                            dao.insertUser(user);
                        }
                    }
                }
            }

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	@RequestMapping("/UpdateStudio")
	@ResponseBody
	public String UpdateStudio(HttpServletRequest request, HttpServletResponse response){
		String studio = request.getParameter("studio");
		String new_studio = request.getParameter("new_studio");

		try {
			dao.updateUserStudio(studio,new_studio);
			dao.updateLessonStudio(studio,new_studio);
			dao.updateClassScheduleStudio(studio,new_studio);
			dao.updateCommentStudio(studio,new_studio);
			dao.updateScheduleArrangementStudio(studio,new_studio);
			dao.updateSignUpRecordStudio(studio,new_studio);
			dao.updateLeaveRecordStudio(studio,new_studio);
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

		String type = request.getParameter("type");

		User user_get= dao.getUser(openid).get(0);
		String comment_style_get = user_get.getComment_style();
		Integer is_open_get = user_get.getIs_open();
		Integer is_open = 0;
		if(is_open_get == 0){
			is_open = 1;
		}

		//定义comment_style
		String comment_style = "public";
		if (comment_style_get.equals("public")){
			comment_style = "self";
		}


		try {
			User user =new User();
			user.setComment_style(comment_style);
			user.setStudio(studio);
			user.setIs_open(is_open);
			if("comment".equals(type)){
				dao.updateComentStyle(user);
			}else if("open".equals(type)){
				dao.updateIsOpen(user);
			}
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

		String remind_type = request.getParameter("remind_type");

		String value = request.getParameter("value");


		try {
			if("统一提醒次日".equals(remind_type)){
				User user =new User();
				user.setSend_time(value);
				user.setStudio(studio);
				user.setRemind_type(remind_type);
				dao.updateSendTime(user);
			}else if("提前N小时提醒".equals(remind_type)){
				User user =new User();
				user.setHours(Integer.parseInt(value));
				user.setStudio(studio);
				user.setRemind_type(remind_type);
				dao.updateHours(user);
			}
			dao.updateClassSendStatusByStudio(studio,"2023-01-01");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	@RequestMapping("/consumeLesson")
	@ResponseBody
	public String consumeLesson(HttpServletRequest request, HttpServletResponse response){
		//获取openid
		String openid = request.getParameter("openid");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();
		String studio = request.getParameter("studio");
		String subject = request.getParameter("subject");
		String student_name = request.getParameter("student_name");
		String consume_lesson_amount = request.getParameter("consume_lesson_amount");

		List<Lesson> lessons = dao.getLessonByNameSubject(student_name, studio,subject,campus);
		if(lessons.size()>0){
			Lesson lesson = lessons.get(0);
			Float total_amount = lesson.getTotal_amount();
			Float left_amount = lesson.getLeft_amount();
			if(consume_lesson_amount != null){
				left_amount = left_amount - Float.parseFloat(consume_lesson_amount);
			}
			Float minus = lesson.getMinus();
			Float coins = lesson.getCoins();
			Integer is_combine = lesson.getIs_combine();


			Lesson lesson_in = new Lesson();
			lesson_in.setStudent_name(student_name);
			lesson_in.setStudio(studio);
			lesson_in.setSubject(subject);
			lesson_in.setCampus(campus);
			lesson_in.setLeft_amount(left_amount);
			lesson_in.setTotal_amount(total_amount);
			lesson_in.setMinus(minus);
			lesson_in.setCoins(coins);

			if(is_combine == 0){
				dao.consumeLesson(lesson_in);
			}else if (is_combine == 1){
				dao.updateLessonBoth(lesson_in);
			}
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
		String nick_name = list_user.get(0).getNick_name();

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
			// 获取type
			String modify_type = request.getParameter("modify_type");

			String all_lesson = request.getParameter("all_lesson");
			if(all_lesson == null || all_lesson.isEmpty() || "undefined".equals(all_lesson)){
				all_lesson = "0";
			}
			String give_lesson = request.getParameter("give_lesson");
			if(give_lesson == null || give_lesson.isEmpty() || "undefined".equals(give_lesson)){
				give_lesson = "0";
			}
			String mark = request.getParameter("mark");
			String start_date = request.getParameter("start_date");
			if(start_date == null || start_date.isEmpty() || "undefined".equals(start_date)){
				start_date = create_time;
			}
			String end_date = request.getParameter("end_date");
			if(end_date == null || end_date.isEmpty() || "undefined".equals(end_date)){
				end_date = create_time;
			}

			//获取原价
			String total_money = request.getParameter("total_money");
			if(total_money == null || total_money.isEmpty() || "undefined".equals(total_money)){
				total_money = "0";
			}
			//获取折扣
			String discount_money = request.getParameter("discount_money");
			if(discount_money == null || discount_money.isEmpty() || "undefined".equals(discount_money)){
				discount_money = "0";
			}

			if("total_money".equals(modify_type)){
				dao.updateLessonTotalMoney(Float.valueOf(total_money),studio,student_name,campus,subject);
				return "push massage successfully";
			}

			if("discount_money".equals(modify_type)){
				dao.updateLessonDiscountMoney(Float.valueOf(discount_money),studio,student_name,campus,subject);
				return "push massage successfully";
			}

			// 获取单次积分
			String coins_amount_get_1 = request.getParameter("coins_amount");
			Float coins_amount_get = 0.0f;
			if (!coins_amount_get_1.isEmpty() && !"0".equals(coins_amount_get_1)){
				coins_amount_get = Float.valueOf(coins_amount_get_1);
			}else if ("0".equals(coins_amount_get_1) && "coins_modify_single".equals(modify_type)){
				Float coins = 0.0f;
				dao.updateLessonCoins(coins,studio,student_name,campus,subject);
			}

			// 获取单次扣课
			String minus_amount_get_1 = request.getParameter("minus_amount");
			Float minus_amount_get = 0.0f;
			if (!minus_amount_get_1.isEmpty() && !"0".equals(minus_amount_get_1)){
				minus_amount_get = Float.valueOf(minus_amount_get_1);
			}else if ("0".equals(minus_amount_get_1) && "minus_modify".equals(modify_type)){
				Float minus = 0.0f;
				dao.updateLessonMinus(minus,studio,student_name,campus,subject);
			}

			//获取总课时
			String total_amount_1 = request.getParameter("total_amount");

			if("total_modify".equals(modify_type)){
				loginService.updateLessonRemind(student_name,studio,campus,subject,total_amount_1,openid,modify_type);
			}

			Float total_amount = 0.0f;
			if (!total_amount_1.isEmpty() && !"0".equals(total_amount_1)){
				total_amount = Float.valueOf(total_amount_1);
			}else if ("0".equals(total_amount_1) && "total_modify".equals(modify_type)){
				total_amount = 0.0f;
				dao.updateLessonTotal(total_amount,studio,student_name,campus,subject);
			}


			//获年剩余课时
			String left_amount_get = request.getParameter("left_amount");

			if("left_modify".equals(modify_type)){
				loginService.updateLessonRemind(student_name,studio,campus,subject,left_amount_get,openid,modify_type);
			}

			Float left_amount = 0.0f;
			if(!left_amount_get.isEmpty() && !"0".equals(left_amount_get)){
				left_amount = Float.parseFloat(left_amount_get);
				if(left_amount < 0.0f){
					dao.updateLessonLeft(left_amount,studio,student_name,campus,subject);
				}
			}else if ("0".equals(left_amount_get) && "left_modify".equals(modify_type)){
				left_amount = 0.0f;
				dao.updateLessonLeft(left_amount,studio,student_name,campus,subject);
			}

			String m_type = request.getParameter("m_type");

			// 获取type
			String lessons_amount_1 = request.getParameter("lessons_amount");
			Float lessons_amount = 0.0f;
			if (!lessons_amount_1.isEmpty()){
				lessons_amount = Float.valueOf(lessons_amount_1);
			}

			//续课记录
			if(lessons_amount>0 && "add_lessons".equals(modify_type)){
				LessonPackage lessonPackage = new LessonPackage();
				lessonPackage.setStudent_name(student_name);
				lessonPackage.setTotal_money(Float.parseFloat(total_money));
				lessonPackage.setDiscount_money(Float.parseFloat(discount_money));
				lessonPackage.setMark(mark);
				lessonPackage.setStart_date(start_date);
				lessonPackage.setEnd_date(end_date);
				lessonPackage.setCampus(campus);
				lessonPackage.setStudio(studio);
				lessonPackage.setSubject(subject);
				lessonPackage.setCreate_time(create_time);
				lessonPackage.setAll_lesson(Float.parseFloat(all_lesson));
				lessonPackage.setGive_lesson(Float.parseFloat(give_lesson));
				lessonPackage.setNick_name(nick_name);

				try {
					dao.insertLessonPackage(lessonPackage);
					loginService.renewLessonRemind(student_name,studio,campus,subject,lessons_amount);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

//			String consume_lesson_amount_1 = request.getParameter("consume_lesson_amount");
			Float consume_lesson_amount = 0.0f;
//			if (!consume_lesson_amount_1.isEmpty()){
//				consume_lesson_amount = Float.valueOf(consume_lesson_amount_1);
//			}

			Float minus_amount=0.0f;
			Float coins_amount=0.0f;
			List<Lesson> lessons_get = dao.getLessonByNameSubject(student_name,studio,subject,campus);
			if(!lessons_get.isEmpty()){
				// 获取单扣课
				minus_amount = lessons_get.get(0).getMinus();
				coins_amount = lessons_get.get(0).getCoins();
			}

			if(minus_amount_get != 0.0f){
				minus_amount = minus_amount_get;
			}
			if(coins_amount_get != 0.0f){
				coins_amount = coins_amount_get;
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
			lesson.setTotal_money(Float.parseFloat(total_money));
			lesson.setDiscount_money(Float.parseFloat(discount_money));

			List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
			if(lessons.size()>0){
				if("coins_modify_all".equals(modify_type)){
					if("积分".equals(m_type)){
						dao.updateCoinsAll(coins_amount,studio,campus);
					}else if("单价".equals(m_type)){
						dao.updatePriceAll(coins_amount,studio,campus);
					}
				}else if("coins_modify_single".equals(modify_type)){
					if("积分".equals(m_type)){
						dao.updateCoinsByStudent(coins_amount,studio,campus,student_name,subject);
					}else if("单价".equals(m_type)){
						dao.updatePriceByStudent(coins_amount,studio,campus,student_name,subject);
					}
				}else {
					loginService.updateLesson(lesson,lessons_amount,consume_lesson_amount,subject_new,campus);
				}

			}else {
				Integer point = 0;
				Float minus_amount_t = 1.0f;
				Float coins_amount_t = 0.0f;
				Float price = 0.0f;
				lesson.setPoints(point);
				lesson.setMinus(minus_amount_t);
				lesson.setCoins(coins_amount_t);
				lesson.setPrice(price);
				loginService.insertLesson(lesson);

				LessonPackage lessonPackage = new LessonPackage();
				lessonPackage.setStudent_name(student_name);
				lessonPackage.setStart_date(start_date);
				lessonPackage.setEnd_date(end_date);
				lessonPackage.setCampus(campus);
				lessonPackage.setStudio(studio);
				lessonPackage.setSubject(subject);
				lessonPackage.setCreate_time(create_time);
				lessonPackage.setAll_lesson(total_amount);
				lessonPackage.setTotal_money(Float.parseFloat(total_money));
				lessonPackage.setDiscount_money(Float.parseFloat(discount_money));
				lessonPackage.setMark("初次录入");
				lessonPackage.setGive_lesson(Float.parseFloat(give_lesson));
				lessonPackage.setNick_name(nick_name);
				dao.insertLessonPackage(lessonPackage);

				if(lesson.getTotal_amount() - lesson.getLeft_amount() > 0.0f){
					SignUp signUp = new SignUp();
					signUp.setStudio(studio);
					signUp.setSign_time(create_time);
					signUp.setMark("录前消课");
					signUp.setCount(0.0f);
					signUp.setTeacher(nick_name);
					signUp.setCreate_time(create_time);
					signUp.setDuration("00:00:00");
					signUp.setClass_number("无班号");
					signUp.setCampus(campus);
					signUp.setStudent_name(student_name);
					signUp.setSubject(subject);
					List<SignUp> signUps_list = dao.getSignUpByBacth(student_name,studio,subject,campus);
					if(signUps_list.size()==0){
						signUp.setCount(lesson.getTotal_amount() - lesson.getLeft_amount());
						loginService.insertSignUp(signUp);
					}
				}
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

	@RequestMapping("/updateLessonPackage")
	@ResponseBody
	public String updateLessonPackage(HttpServletRequest request, HttpServletResponse response){
		//获取用户名
		String id = request.getParameter("id");
		String content = request.getParameter("content");
		String type = request.getParameter("type");

		try {
			loginService.updateLessonPackage(id,content,type);
		} catch (Exception e) {
			throw new RuntimeException(e);
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

	@RequestMapping("/updateOpenid")
	@ResponseBody
	public String updateOpenid(HttpServletRequest request, HttpServletResponse response){
		String openid_old = request.getParameter("openid_old");
		String openid_new = request.getParameter("openid_new");

		try {
			if(openid_old != null && openid_new != null){
				List<User> users = dao.getUserByOpenid(openid_new);
				if(users.size()>0){
					String role = users.get(0).getRole();
					if("client".equals(role)){
                        dao.updateOpenidById(openid_old,openid_new);
					}
				}else {
					dao.updateOpenidById(openid_old,openid_new);
				}

			}
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

	@RequestMapping("/updateUserStudioCampus")
	@ResponseBody
	public String updateUserStudioCampus(HttpServletRequest request, HttpServletResponse response){
		String studio = request.getParameter("studio");
		String openid = request.getParameter("openid");

		try {
			User user = new User();
			user.setOpenid(openid);
			user.setStudio(studio);
			user.setCampus(studio);
			dao.updateUserStudioByOpenid(user);
			dao.updateUserCampus(user);
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


	@RequestMapping("/updateCoinsByStudio")
	@ResponseBody
	public String updateCoinsByStudio(HttpServletRequest request, HttpServletResponse response){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        String now_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
        Calendar cal = Calendar.getInstance();

		//获取用户名
		String studio = request.getParameter("studio");
		String openid = request.getParameter("openid");
		String number = request.getParameter("number");
		String type = request.getParameter("type");
		Float number_in = Float.parseFloat(number);

		try {
			if("获取".equals(type)){
				loginService.updateCoinsByStudio(studio,openid,number_in);
			}else if("禁止".equals(type)){
				loginService.updateCoinsByStudio(studio,openid,-number_in);
                List<User> users = dao.getUserByOpenid(openid);
                String expired_time_ad_get = users.get(0).getExpired_time_ad();
                Date expired_time_ad_get_date = df.parse(expired_time_ad_get);
                long expired_time_timestamp = expired_time_ad_get_date.getTime();
                Date now_time_date = df.parse(now_time);
                long now_time_date_timestamp = now_time_date.getTime();
                if(expired_time_timestamp > now_time_date_timestamp){
                    now_time_date_timestamp = expired_time_timestamp;
                }
				cal.setTimeInMillis(now_time_date_timestamp);
				cal.add(cal.DATE,30);
				String expired_time_ad = df.format(cal.getTime());
				dao.updateUserExpiredTimeAd(openid,expired_time_ad);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "push massage successfully";
	}

	@RequestMapping("/minusReadTimesByOpenid")
	@ResponseBody
	public String minusReadTimesByOpenid(HttpServletRequest request, HttpServletResponse response){
		//获取用户名
		String openid = request.getParameter("openid");
		String video_id = request.getParameter("video_id");

		try {
			List<User> users = dao.getUserByOpenid(openid);
			Float  read_times = users.get(0).getRead_times();
			if (read_times == null) {
				read_times = 0.0f;
			}

			Float new_read_times = read_times - 1.0f;
			dao.updateReadTimesByOpenid(openid,new_read_times);

			List<Message> messages = dao.getDetails(Integer.parseInt(video_id));
			Integer views = messages.get(0).getViews() + 15;
			dao.updateVideoViewsById(video_id,views);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "push massage successfully";
	}

	@RequestMapping("/updateVideoViewsById")
	@ResponseBody
	public String updateVideoViewsById(HttpServletRequest request, HttpServletResponse response){
		//获取用户名
		String video_id = request.getParameter("video_id");

		try {
			List<Message> messages = dao.getDetails(Integer.parseInt(video_id));
			Integer views = messages.get(0).getViews() + 5;
			dao.updateVideoViewsById(video_id,views);

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
		String subject = request.getParameter("subject");
		List<User> list_user = dao.getUser(openid);
		String campus = list_user.get(0).getCampus();

		try {
			dao.updateScheduleName(student_name_new,student_name,studio,campus,subject);
			dao.updateCommentName(student_name_new,student_name,studio,campus,subject);
			dao.updateGiftRecordName(student_name_new,student_name,studio,campus,subject);
			dao.updateLessonName(student_name_new,student_name,studio,campus,subject);
			dao.updateSignUpRecordName(student_name_new,student_name,studio,campus,subject);
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
	
	


