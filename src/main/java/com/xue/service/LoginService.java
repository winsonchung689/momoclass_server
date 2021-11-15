package com.xue.service;

import java.util.List;

import com.xue.entity.model.Message;
import com.xue.entity.model.Schedule;
import com.xue.entity.model.User;

public interface LoginService {

    public List  getMessage();

    public List  getMessageClient(String nickName);

    public List  getModel();

	public int push(Message message);

    public List  getDetails(Integer id);

    public List  getSearch(String student_name);

    public int insertSchedule(Schedule schedule);

    public List  getSchedule(String date_time);

    public int  deleteComment(Integer id);

    public int  deleteSchedule(Integer id);

    public int insertUser(User user);

    public List getUser(String openid);

    public List getOpenidByNick(String nick_name);

    public List getAdvertise();



}
