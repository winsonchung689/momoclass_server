package com.xue.service;

import java.util.List;

import com.xue.entity.model.Lesson;
import com.xue.entity.model.Message;
import com.xue.entity.model.Schedule;
import com.xue.entity.model.User;

public interface LoginService {

    public List  getMessage(String studio);

    public List  getMessageClient(String nickName);

    public List  getModel(String studio);

    public List  getLesson(String studio);

    public int push(Message message);

    public int insertLesson(Lesson lesson);

	public int updateLesson(Lesson lesson);

    public List  getDetails(Integer id);

    public List  getSearch(String student_name,String studio);

    public int insertSchedule(Schedule schedule);

    public List  getSchedule(String date_time,String studio);

    public int  deleteComment(Integer id,String role);

    public int  deleteSchedule(Integer id);

    public int  deleteLesson(Integer id);

    public int insertUser(User user);

    public int updateUser(User user);

    public int updateUsertype(User user);

    public List getUser(String openid);

    public List getStudio();

    public List getCertificateModel(String class_name);

    public List getCertificateModelName();

    public List getOpenidByNick(String student_name,String studio);

    public List getAdvertise(String studio);

    public List getCertificate(String studio,String student_name);

    public List getPaycode(String student_name);

    public List getClassSys(String studio);

    public List getHome(String studio);

    public int updateMinusLesson(String student_name,String studio);

    public List  getLessonByName(String student_name,String studio);

    public int updateAddPoints(String student_name,String studio);

    public int deletePoints(String student_name,String studio);



}
