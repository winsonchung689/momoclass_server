package com.xue.service;

import java.util.List;

import com.xue.entity.model.*;

public interface LoginService {

    public List  getMessage(String studio,Integer page,String comment_style,String openid,String role);

    public List  getGrowthRecord(String studio,Integer page,String student_name);

    public List  getUserByOpenid(String openid);

    public List  getMessageClient(String nickName);

    public List  getModel(String studio,Integer page);

    public List  getMamaShare(Integer page);

    public List  getPpt(Integer page);

    public List  getLesson(String studio,String student_name);

    public List  getTipsDataUrl(String studio,Integer left_amount);

    public List  getLessonInName(String studio,String student_name,Integer page);

    public List  getRating(String studio,String student_name,Integer page);

    public int push(Message message);

    public int insertLesson(Lesson lesson);

	public int updateLesson(Lesson lesson,Float lessons_amount);

    public List  getDetails(Integer id);

    public List  getDetailsUrlByDate(String studio,String duration,String student_name,String date_time);

    public List  getSearch(String student_name,String studio,Integer page);

    public List  getSignUp(String student_name,String studio);

    public List  getGift(String student_name,String studio);

    public List  getLeaveRecord(String student_name,String studio);

    public List  getArrangement(String studio,Integer dayofweek,String date);

    public int insertSchedule(Schedule schedule);

    public int insertSignUp(SignUp signUp);

    public int insertArrangement(Arrangement arrangement);

    public int insertGift(Gift gift);

    public List  getSchedule(String date_time,String studio);

    public List  getScheduleDetail(Integer weekDay,String duration,String studio);

    public int  deleteComment(Integer id,String role,String studio,String openid);

    public int  deleteArrangement(Integer id,String role,String studio,String openid);

    public int  deleteSignUpRecord(Integer id,String role,String studio,String openid);

    public int  deleteSignUpAllRecord(String name,String role,String studio,String openid);

    public int  deleteGiftRecord(Integer id,String role,String studio,String openid);

    public int  deleteSchedule(Integer id,String role,String studio,String openid);

    public int  deleteLesson(Integer id,String role,String studio,String openid);

    public int insertUser(User user);

    public int updateUser(User user);

    public int updateComentStyle(User user);

    public int updateSchedule(Schedule schedule);

    public int updateComment(Message message);

    public int updateDetailPhoto(Message message);

    public int updateUsertype(User user);

    public List getUser(String openid);

    public List getUserByNickName(String openid);

    public List getStudio();

    public List getArrangements(String studio);

    public List getCertificateModel(String class_name);

    public List getCertificateModelName();

    public List getOpenidByNick(String student_name,String studio);

    public List getAdvertise(String studio);

    public List getCertificate(String studio,String student_name);

    public List getPaycode(String student_name);

    public List getClassSys(String class_target,String studio,Integer page);

    public List getCourseList(String studio,Integer page);

    public List getCourseDetail(String studio,String class_name,Integer page);

    public List getHome(String studio);

    public int updateMinusLesson(String student_name,String studio,Float class_count);

    public List  getLessonByName(String student_name,String studio);

    public int updateAddPoints(String student_name,String studio,Integer points);

    public int deletePoints(String student_name,String studio,Integer points);

    public int updateCoins(String openid,String type);

    public int updateGift(String id);



}
