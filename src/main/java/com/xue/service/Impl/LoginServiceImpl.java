package com.xue.service.Impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xue.entity.model.User;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;
@Service
public class LoginServiceImpl implements LoginService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private UserMapper dao;

	@Override
	public int push(User user) {
		System.out.printf("push data");
		int result = 0;
		System.out.println(user);
		try {
			result = dao.insertUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public String getPhoto() {
		System.out.printf("get photo");
		String photo = null;
		try {
			photo = dao.selectUser().getPhoto();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return photo;
	}

	@Override
	public String getMessage() {
		System.out.printf("get photo");
		String comment = null;
		try {
			comment = dao.selectUser().getComment();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return comment;
	}


}
