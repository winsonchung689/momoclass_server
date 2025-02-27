package com.xue.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xue.config.Constants;
import com.xue.entity.model.*;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;
import com.xue.service.WebPushService;
import com.xue.service.WechatPayService;
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
public class WechatPayController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String tample10 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"LlmWw436tKz1DwZzpWmgnIBFAtXCUj_HxDN7xub03e4\",\"data\":{\"thing1\":{\"value\": \"AA\"},\"time5\":{\"value\": \"time\"},\"thing3\":{\"value\": \"A1\"}}}";

	@Autowired
	private WechatPayService wechatPayService;

	@Autowired
	private UserMapper dao;

	@RequestMapping("/getWeChatPayNotify")
	@ResponseBody
	public void getWeChatPayNotify(){
		System.out.println("pay sucessfully");
	}

	@RequestMapping("/weChatPayDirect")
	@ResponseBody
	public List weChatPayDirect(HttpServletRequest request, HttpServletResponse response){

		String description = request.getParameter("description");
		String total = request.getParameter("total");
		String openid = request.getParameter("openid");
		List<User> users = dao.getUser(openid);
		User user = users.get(0);
		String studio = user.getStudio();
		String campus = user.getCampus();
		String appid = request.getParameter("appid");
		List<Merchant> merchants =dao.getMerchant(studio,campus,appid);
		Merchant merchant = merchants.get(0);
		String mchid = merchant.getMchid();

		List list = null;
		try {
			list = wechatPayService.weChatPayDirect(openid,mchid,appid,description,Integer.parseInt(total));
		} catch (NumberFormatException e) {
			throw new RuntimeException(e);
		}

		return list;
	}


}
	
	


