package com.xue.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSONArray;
import com.xue.JsonUtils.JsonUtils;
import com.xue.config.Constants;
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
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class AIController {

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


	@RequestMapping("/chatImg")
	@ResponseBody
	public static String chatImg(String question,String uuid){
		String res = null;
		try {
			String img_url = "https://www.momoclasss.xyz:443/data1/uploadAIAsk/" + uuid;
			System.out.println(question);
			System.out.println(img_url);

			String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
			Map<String, String> header = new HashMap<String, String>();
			header.put("Content-Type", "application/json");
			header.put("Authorization", "Bearer " + OPENAI_API_KEY);
			JSONObject params = new JSONObject();
			params.put("model", "gpt-4o-mini");
			List<JSONObject> jsonObjects = new ArrayList<>();
			JSONObject jsonObject = new JSONObject();
			// content
			List<JSONObject> content_list = new ArrayList<>();

			// content 文字
			JSONObject content_js_text = new JSONObject();
			content_js_text.put("type","text");
			content_js_text.put("text",question);

			// content 图片
			JSONObject content_js_img = new JSONObject();
			content_js_img.put("type","image_url");
			JSONObject img_js = new JSONObject();
			img_js.put("url",img_url);
			content_js_img.put("image_url",img_js);
			// 入参
			content_list.add(content_js_text);
			content_list.add(content_js_img);

			jsonObject.put("role", "user");
			jsonObject.put("content", content_list);
			jsonObjects.add(jsonObject);
			params.put("messages", jsonObjects);
			params.put("temperature", 0.9);
			params.put("max_tokens", 2048);
			params.put("top_p", 1);
			params.put("frequency_penalty", 0.0);
			params.put("presence_penalty", 0.6);

			JSONArray stop = new JSONArray();
			stop.add("<br>");
			params.put("stop", stop);
			res = JsonUtils.doPost("https://api.openai.com/v1/chat/completions", header, params);
			System.out.println(res);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return res;
	}

	@RequestMapping("/chat")
	@ResponseBody
	public static String chat(String question){
		System.out.println(question);
		String res = null;
		try {
			String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
			Map<String, String> header = new HashMap<String, String>();
			header.put("Content-Type", "application/json");
			header.put("Authorization", "Bearer " + OPENAI_API_KEY);
			JSONObject params = new JSONObject();
			params.put("model", "gpt-4o-mini");
			List<JSONObject> jsonObjects = new ArrayList<>();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("role", "user");
			jsonObject.put("content", question);
			jsonObjects.add(jsonObject);
			params.put("messages", jsonObjects);
			params.put("temperature", 0.9);
			params.put("max_tokens", 2048);
			params.put("top_p", 1);
			params.put("frequency_penalty", 0.0);
			params.put("presence_penalty", 0.6);

			JSONArray stop = new JSONArray();
			stop.add("<br>");
			params.put("stop", stop);
			res = JsonUtils.doPost("https://api.openai.com/v1/chat/completions", header, params);
			System.out.println(res);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return res;
	}

	@RequestMapping("/momoChat")
	@ResponseBody
	public static String momoChat(String question){
		String res = null;
		System.out.println(question);
		try {
			String question_encode = URLEncoder.encode(question, "UTF-8");
			String url = "http://43.156.34.5:443/chat?question=" + question_encode;
			res = JsonUtils.doGet(url);
			System.out.println(res);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return res;
	}

	@RequestMapping("/momoChatImg")
	@ResponseBody
	public static String momoChatImg(String question,String uuid){
		String res = null;
		System.out.println(question);
		try {
			String question_encode = URLEncoder.encode(question, "UTF-8");
			String url = "http://43.156.34.5:443/chatImg?question=" + question_encode + "&uuid=" + uuid;
			res = JsonUtils.doGet(url);
			System.out.println(res);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return res;
	}



}
	
	


