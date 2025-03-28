package com.xue.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSONArray;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AIController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// 直连图文问答
	@RequestMapping("/chatImg")
	@ResponseBody
	public static String chatImg(String question,String uuid){
		String res = null;
		try {
			String img_url = "https://www.momoclasss.xyz:443/data/disk/uploadAIAsk/" + uuid;
			if("课后点评".equals(question.split("_")[0])){
				img_url = "https://www.momoclasss.xyz:443/data/disk/uploadimages/" + uuid;
			}


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
			res = HttpUtil.doPost("https://api.openai.com/v1/chat/completions", header, params);
			System.out.println(res);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return res;
	}

	// 直连文本问答
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
			res = HttpUtil.doPost("https://api.openai.com/v1/chat/completions", header, params);
			System.out.println(res);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return res;
	}

	// 直连文生图
	@RequestMapping("/imgGenerate")
	@ResponseBody
	public static String imgGenerate(String question){
		System.out.println(question);
		String res = null;
		try {
			String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
			Map<String, String> header = new HashMap<String, String>();
			header.put("Content-Type", "application/json");
			header.put("Authorization", "Bearer " + OPENAI_API_KEY);
			JSONObject params = new JSONObject();
			params.put("model", "dall-e-3");
			params.put("prompt", question);
			params.put("n", 1);
			params.put("size", "1024x1024");

			res = HttpUtil.doPost("https://api.openai.com/v1/images/generations", header, params);
			System.out.println(res);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return res;
	}

	// 直连图生图
	@RequestMapping("/imgVariations")
	@ResponseBody
	public static String imgVariations(String uuid){
		String img_url = "https://www.momoclasss.xyz:443/data/disk/uploadAIAsk/" + uuid;
		String filePath = "/data/imgs/imgfile.png";
		String res = null;
		System.out.printf(img_url);
		try {
			URL url = new URL(img_url);
			// 打开连接
			try (InputStream is = url.openStream();
				 FileOutputStream fos = new FileOutputStream(filePath)) {
				// 读取数据并写入文件
				byte[] buffer = new byte[4096]; // 缓冲区大小，可以根据需要调整
				int bytesRead;
				while ((bytesRead = is.read(buffer)) != -1) {
					fos.write(buffer, 0, bytesRead);
				}
			} catch (IOException e) {
				System.err.println("Error reading from URL or writing to file: " + e.getMessage());
			}

			File file = new File(filePath);
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost("https://api.openai.com/v1/images/variations");
			String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
			httpPost.setHeader("Authorization","Bearer " + OPENAI_API_KEY);

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE); // 设置浏览器兼容模式
			builder.addBinaryBody("image", file, ContentType.DEFAULT_BINARY, file.getName()); // 添加文件部分
			builder.addTextBody("n", "1", ContentType.TEXT_PLAIN); // 添加文本部分，例如表单字段
			builder.addTextBody("size", "1024x1024", ContentType.TEXT_PLAIN); // 添加文本部分，例如表单字段
			builder.addTextBody("model", "dall-e-2", ContentType.TEXT_PLAIN); // 添加文本部分，例如表单字段
			builder.addTextBody("prompt", "画一张吉卜力风格的图片", ContentType.TEXT_PLAIN); // 添加文本部分，例如表单字段

			HttpEntity multipart = builder.build();
			httpPost.setEntity(multipart);
			CloseableHttpResponse response = httpClient.execute(httpPost);

			try {
				HttpEntity responseEntity = response.getEntity();
				if (responseEntity != null) {
					res = EntityUtils.toString(responseEntity); // 获取响应内容
					System.out.println(res); // 打印响应内容或进行其他处理
				}
				response.close(); // 关闭响应对象
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return res;
	}

////////////////////////////// 分界线 //////////////////////////////////

	//小程序文本问答
	@RequestMapping("/momoChat")
	@ResponseBody
	public static String momoChat(String question){
		String res = null;
		System.out.println(question);
		try {
			String question_encode = URLEncoder.encode(question, "UTF-8");
			String url = "http://43.156.34.5:80/chat?question=" + question_encode;
			res = HttpUtil.doGet(url);
			System.out.println(res);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return res;
	}

	//小程序图文问答
	@RequestMapping("/momoChatImg")
	@ResponseBody
	public static String momoChatImg(String question,String uuid){
		String res = null;
		System.out.println(question);
		try {
			String question_encode = URLEncoder.encode(question, "UTF-8");
			String url = "http://43.156.34.5:80/chatImg?question=" + question_encode + "&uuid=" + uuid;
			res = HttpUtil.doGet(url);
			System.out.println(res);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return res;
	}

	//小程序文生图
	@RequestMapping("/momoImgGenerate")
	@ResponseBody
	public static String momoImgGenerate(String question){
		String res = null;
		System.out.println(question);
		try {
			String question_encode = URLEncoder.encode(question, "UTF-8");
			String url = "http://43.156.34.5:80/imgGenerate?question=" + question_encode;
			res = HttpUtil.doGet(url);
			System.out.println(res);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return res;
	}

	@RequestMapping("/momoImgVariations")
	@ResponseBody
	public static String momoImgVariations(String uuid){
		String res = null;
		System.out.println(uuid);
		try {
			String url = "http://43.156.34.5:80/imgVariations?uuid=" + uuid;
			res = HttpUtil.doGet(url);
			System.out.println(res);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return res;
	}


}
	
	


