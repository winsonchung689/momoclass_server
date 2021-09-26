package com.xue.service.Impl;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.xue.util.DBUtil;
import com.xue.util.Imageutil;
import org.hibernate.engine.jdbc.BinaryStream;
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
		FileInputStream in = null;
		System.out.println(user);
		try {
			result = dao.insertUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public void getPhoto(String targePath) {
		System.out.printf("get photo");
		byte[] photo = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs  = null;
		try {
			conn = DBUtil.getConn();
			String sql = "select student_name,photo,comment,create_time from class_comment order by create_time limit 1";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				InputStream in = rs.getBinaryStream("photo");
				Imageutil.readBin2Image(in, targePath);
			}
			photo = dao.selectUser().getPhoto();
			System.out.printf("pppppptype :" + photo);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
