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

	@RequestMapping("/chatImg")
	@ResponseBody
	public static String chatImg(String question,String uuid){
		String res = null;
		try {
			String img_url = "https://www.momoclasss.xyz:443/data/disk/uploadAIAsk/" + uuid;
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
			params.put("model", "o1-mini");
			List<JSONObject> jsonObjects = new ArrayList<>();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("role", "user");
			jsonObject.put("content", question);
			jsonObjects.add(jsonObject);
//			params.put("reasoning_effort", "medium");
			params.put("messages", jsonObjects);
			params.put("max_completion_tokens", 2048);
//			params.put("top_p", 1);
//			params.put("frequency_penalty", 0.0);
//			params.put("presence_penalty", 0.6);

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
			String url = "http://43.156.34.5:80/chat?question=" + question_encode;
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
			String url = "http://43.156.34.5:80/chatImg?question=" + question_encode + "&uuid=" + uuid;
			res = JsonUtils.doGet(url);
			System.out.println(res);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return res;
	}


}
	
	


