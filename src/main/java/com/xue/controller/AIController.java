package com.xue.controller;

import com.alibaba.fastjson.JSONObject;
import com.xue.entity.model.Message;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;
import com.xue.util.HttpUtil;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

@Controller
public class AIController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LoginService loginService;

	@Autowired
	private UserMapper dao;

	////////////////////////////// AI服务器接口 //////////////////////////////////

	// 官方接口
	@RequestMapping("/chatImgDirect")
	@ResponseBody
	public static String chatImgDirect(String question,String uuid){
		String res = null;
		try {
			String img_url = "https://www.momoclasss.xyz:443/data/disk/uploadAIAsk/" + uuid;
			if("课后点评".equals(question.split("_")[0])){
				img_url = "https://www.momoclasss.xyz:443/data/disk/uploadimages/" + uuid;
			}

			// ========== ① 下载远程图片 ==========
			URL url = new URL(img_url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(60000);   // 下载图片的超时时间
			conn.setReadTimeout(60000);

			InputStream in = conn.getInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
			in.close();

			byte[] imgBytes = out.toByteArray();

			// ========== ② 图片转 Base64 ==========
			String base64Img = Base64.getEncoder().encodeToString(imgBytes);
			String base64Url = "data:image/png;base64," + base64Img;


			String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
			Map<String, String> header = new HashMap<String, String>();
			header.put("Content-Type", "application/json");
			header.put("Authorization", "Bearer " + OPENAI_API_KEY);
			JSONObject params = new JSONObject();
			params.put("model", "gpt-5.4");
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
			img_js.put("url",base64Url);
			content_js_img.put("image_url",img_js);
			// 入参
			content_list.add(content_js_text);
			content_list.add(content_js_img);

			jsonObject.put("role", "user");
			jsonObject.put("content", content_list);
			jsonObjects.add(jsonObject);
			params.put("messages", jsonObjects);
			params.put("temperature", 0.9);
			params.put("max_completion_tokens", 2048);
			params.put("top_p", 1);
			params.put("frequency_penalty", 0.0);
			params.put("presence_penalty", 0.6);

			res = HttpUtil.doPost("https://api.openai.com/v1/chat/completions", header, params);
			System.out.println(res);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return res;
	}

	// 官方接口
	@RequestMapping("/chatDirect")
	@ResponseBody
	public static String chatDirect(String question){
		System.out.println(question);
		String res = null;
		try {
			String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
			Map<String, String> header = new HashMap<String, String>();
			header.put("Content-Type", "application/json");
			header.put("Authorization", "Bearer " + OPENAI_API_KEY);
			JSONObject params = new JSONObject();
			params.put("model", "gpt-5.4-nano");
			List<JSONObject> jsonObjects = new ArrayList<>();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("role", "user");
			jsonObject.put("content", question);
			jsonObjects.add(jsonObject);
			params.put("messages", jsonObjects);
			params.put("max_completion_tokens", 2048);

			res = HttpUtil.doPost("https://api.openai.com/v1/chat/completions", header, params);
			System.out.println(res);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return res;
	}

	// 非官方接口
	@RequestMapping("/imgGenerateAgent")
	@ResponseBody
	public String imgGenerateAgent(String question,String uuid,String image_type,String ratio,String studio){
		String baseUrl = "https://www.momoclasss.xyz:443/data/disk/uploadAIAsk/";
		if("课评".equals(image_type)){
			baseUrl = "https://www.momoclasss.xyz:443/data/disk/uploadimages/";
		}

		String logo_url = "none";
		List<Message> messages = dao.getFrameModel(studio,0,6,"Logo图片");
		if(messages.size()>0){
			Message message = messages.get(0);
			String logo_uuid = message.getUuids();
			try {
				logo_uuid = logo_uuid.replace("\"","").replace("[","").replace("]","");
			} catch (Exception e) {
//                    throw new RuntimeException(e);
			}
			logo_url = "https://www.momoclasss.xyz:443/data/disk/uploadimages/" + logo_uuid;
		}

		String res = null;
		try {
			String OPENAI_API_KEY = System.getenv("ONLINE_OPENAI_API_KEY");
			Map<String, String> header = new HashMap<String, String>();
			header.put("Content-Type", "application/json");
			header.put("Authorization", "Bearer " + OPENAI_API_KEY);
			JSONObject params = new JSONObject();
			params.put("model", "gpt-image-2");

			// 图片列表
			List<String> images_list = new ArrayList<>();

			// 学生图片
			String[] uuid_list = uuid.split(",");
			for(int i =0;i<uuid_list.length;i++){
				String uuid_get = uuid_list[i];
				JSONObject image_json = new JSONObject();
				String base64Url = urlToBase64(baseUrl + uuid_get);
//				image_json.put("image_url",base64Url);
				images_list.add(base64Url);
			}

			//logo图片
			if(!"none".equals(logo_url)){
				//主体图片
				JSONObject image_json_logo = new JSONObject();
				String base64LogoUrl = urlToBase64(logo_url);
//				image_json_logo.put("image_url",base64LogoUrl);
				images_list.add(base64LogoUrl);
				question  = "图组中有一张是品牌logo图其他是学生作品图，先基于学生作品"+ question + ",最后再把Logo放在海报的左上角的位置,大小约120*120，不要改动logo图案";
			}
			System.out.println(question);

			params.put("image_urls", images_list);
			params.put("n", 1);

//			横版:1536x1024   竖版:1024x1536  方形:1024x1024
			String size = "2:3";
			if("横版".equals(ratio)){
				size = "3:2";
			} else if ("方形".equals(ratio)) {
				size = "1:1";
			}
			params.put("size", size);
			params.put("quality", "low");
			String question_encode = URLDecoder.decode(question, "UTF-8");
			params.put("prompt", question_encode);

			res = HttpUtil.doPost("https://api.apimart.ai/v1/images/generations", header, params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return res;
	}

	@RequestMapping("/getTasksAgent")
	@ResponseBody
	public static String getTasksAgent(String task_id){
		System.out.println(task_id);
		String res = null;
		try {
			String OPENAI_API_KEY = System.getenv("ONLINE_OPENAI_API_KEY");

			res = HttpUtil.doGetHeader("https://api.apimart.ai/v1/tasks/"+task_id, OPENAI_API_KEY);
//			System.out.println(res);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return res;
	}


	////////////////////////////// 本地接口 //////////////////////////////////

	@RequestMapping("/momoChat")
	@ResponseBody
	public static String momoChat(String question){
		String res = null;
		System.out.println(question);
		try {
			String question_encode = URLEncoder.encode(question, "UTF-8");
			String url = "http://43.156.34.5:80/chatDirect?question=" + question_encode;
			res = HttpUtil.doGet(url);
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
			String url = "http://43.156.34.5:80/chatImgDirect?question=" + question_encode + "&uuid=" + uuid;
			res = HttpUtil.doGet(url);
			System.out.println(res);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return res;
	}

	@RequestMapping("/imgEdit")
	@ResponseBody
	public static String imgEdit(String question,String uuid,String image_type,String ratio,String studio){
		String res = null;
		System.out.println(question);
		try {
			String question_encode = URLEncoder.encode(question, "UTF-8");
			String url = "http://43.156.34.5:80/imgGenerateAgent?question=" + question_encode + "&uuid=" + uuid  + "&image_type=" + image_type  + "&ratio=" + ratio  + "&studio=" + studio;
			res = HttpUtil.doGet(url);
			System.out.println(res);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return res;
	}

	@RequestMapping("/getTasks")
	@ResponseBody
	public static String getTasks(String task_id){
		String res = null;
		System.out.println(task_id);

		try {
			String url = "http://43.156.34.5:80/getTasksAgent?task_id=" + task_id;
			res = HttpUtil.doGet(url);
			System.out.println(res);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return res;
	}



	////////////////////////////// 第三方接口 //////////////////////////////////

	@RequestMapping("/imgEdit1")
	@ResponseBody
	public String imgEdit1(String question,String uuid,String image_type,String ratio,String studio){
		String baseUrl = "https://www.momoclasss.xyz:443/data/disk/uploadAIAsk/";
		if("课评".equals(image_type)){
			baseUrl = "https://www.momoclasss.xyz:443/data/disk/uploadimages/";
		}

		String logo_url = "none";
		List<Message> messages = dao.getFrameModel(studio,0,6,"Logo图片");
		if(messages.size()>0){
			Message message = messages.get(0);
			String logo_uuid = message.getUuids();
			try {
				logo_uuid = logo_uuid.replace("\"","").replace("[","").replace("]","");
			} catch (Exception e) {
//                    throw new RuntimeException(e);
			}
			logo_url = "https://www.momoclasss.xyz:443/data/disk/uploadimages/" + logo_uuid;
		}

		String res = null;
		try {
			String OPENAI_API_KEY = System.getenv("ONLINE_OPENAI_API_KEY");
			Map<String, String> header = new HashMap<String, String>();
			header.put("Content-Type", "application/json");
			header.put("Authorization", "Bearer " + OPENAI_API_KEY);
			JSONObject params = new JSONObject();
			params.put("model", "gpt-image-2");

			// 图片列表
			List<String> images_list = new ArrayList<>();

			// 学生图片
			String[] uuid_list = uuid.split(",");
			for(int i =0;i<uuid_list.length;i++){
				String uuid_get = uuid_list[i];
				JSONObject image_json = new JSONObject();
				String base64Url = urlToBase64(baseUrl + uuid_get);
//				image_json.put("image_url",base64Url);
				images_list.add(base64Url);
			}

			//logo图片
			if(!"none".equals(logo_url)){
				//主体图片
				JSONObject image_json_logo = new JSONObject();
				String base64LogoUrl = urlToBase64(logo_url);
//				image_json_logo.put("image_url",base64LogoUrl);
				images_list.add(base64LogoUrl);
				question  = "图组中有一张是品牌logo图其他是学生作品图，先基于学生作品"+ question + ",最后再把Logo放在海报的左上角的位置,大小约120*120，不要改动logo图案";
			}
			System.out.println(question);

			params.put("image_urls", images_list);
			params.put("n", 1);

//			横版:1536x1024   竖版:1024x1536  方形:1024x1024
			String size = "2:3";
			if("横版".equals(ratio)){
				size = "3:2";
			} else if ("方形".equals(ratio)) {
				size = "1:1";
			}
			params.put("size", size);
			params.put("quality", "low");
			params.put("prompt", question);

			res = HttpUtil.doPost("https://api.apimart.ai/v1/images/generations", header, params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return res;
	}

	@RequestMapping("/imgGenerate")
	@ResponseBody
	public static String imgGenerate(String question){
		System.out.println(question);
		String res = null;
		try {
			String OPENAI_API_KEY = System.getenv("ONLINE_OPENAI_API_KEY");
			Map<String, String> header = new HashMap<String, String>();
			header.put("Content-Type", "application/json");
			header.put("Authorization", "Bearer " + OPENAI_API_KEY);
			JSONObject params = new JSONObject();
			params.put("model", "gpt-image-2");
			params.put("prompt", question);
			params.put("n", 1);
//			536x1024：横向   1024x1536：纵向
			params.put("size", "1024x1536");
			params.put("quality", "low");

			res = HttpUtil.doPost("https://api.apimart.ai/v1/images/generations", header, params);
//			System.out.println(res);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return res;
	}

	@RequestMapping("/getTasks1")
	@ResponseBody
	public static String getTasks1(String task_id){
		System.out.println(task_id);
		String res = null;
		try {
			String OPENAI_API_KEY = System.getenv("ONLINE_OPENAI_API_KEY");

			res = HttpUtil.doGetHeader("https://api.apimart.ai/v1/tasks/"+task_id, OPENAI_API_KEY);
//			System.out.println(res);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return res;
	}

	public static String urlToBase64(String image_url){
		// ========== ① 下载远程图片 ==========
		byte[] imgBytes = new byte[0];
		try {
			URL url = new URL(image_url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(60000);   // 下载图片的超时时间
			conn.setReadTimeout(60000);

			InputStream in = conn.getInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
			in.close();

			imgBytes = out.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// ========== ② 图片转 Base64 ==========
		String base64Img = Base64.getEncoder().encodeToString(imgBytes);
		String base64Url = "data:image/png;base64," + base64Img;

		return base64Url;
	}

}
	
	


