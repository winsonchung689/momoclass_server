package com.xue.service;

import java.util.List;

import com.xue.entity.model.*;

public interface LoginService {

    public List  getMessage(String studio,Integer page);

    public List  getMessageClient(String nickName);

    public List  getModel(String studio,Integer page);

    public List  getPpt(Integer page);

    public List  getLesson(String studio,String student_name);

    public List  getRating(String studio,String student_name);

    public int push(Message message);

    public int insertLesson(Lesson lesson);

	public int updateLesson(Lesson lesson);

    public List  getDetails(Integer id);

    public List  getSearch(String student_name,String studio);

    public List  getSignUp(String student_name,String studio);

    public List  getGift(String student_name,String studio);

    public int insertSchedule(Schedule schedule);

    public int insertSignUp(SignUp signUp);

    public int insertGift(Gift gift);

    public List  getSchedule(String date_time,String studio);

    public int  deleteComment(Integer id,String role);

    public int  deleteSignUpRecord(Integer id,String role);

    public int  deleteGiftRecord(Integer id,String role);

    public int  deleteSchedule(Integer id,String role);

    public int  deleteLesson(Integer id,String role);

    public int insertUser(User user);

    public int updateUser(User user);

    public int updateSchedule(Schedule schedule);

    public int updateUsertype(User user);

    public List getUser(String openid);

    public List getStudio();

    public List getCertificateModel(String class_name);

    public List getCertificateModelName();

    public List getOpenidByNick(String student_name,String studio);

    public List getAdvertise(String studio);

    public List getCertificate(String studio,String student_name);

    public List getPaycode(String student_name);

    public List getClassSys(String class_target,String studio,Integer limit);

    public List getCourseList(String studio,Integer page);

    public List getCourseDetail(String studio,String class_name);

    public List getHome(String studio);

    public int updateMinusLesson(String student_name,String studio,Float class_count);

    public List  getLessonByName(String student_name,String studio);

    public int updateAddPoints(String student_name,String studio);

    public int deletePoints(String student_name,String studio,Integer points);

    public int updateCoins(String openid,String type);

    public int updateGift(String id);



}
