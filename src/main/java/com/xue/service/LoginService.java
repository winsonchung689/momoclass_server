package com.xue.service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import com.xue.entity.model.User;
import org.hibernate.engine.jdbc.BinaryStream;

public interface LoginService {

    public List  getMessage();

	public int push(User user);

    public List  getDetails(Integer id);

    public List  getSearch(String student_name);



}
