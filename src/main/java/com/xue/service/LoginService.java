package com.xue.service;

import java.util.List;

import com.xue.entity.model.*;

public interface LoginService {

    public List  getMessage(String studio,Integer page,String comment_style,String openid,String role,String class_target);

    public String  changeClass(String studio,Integer changeday,String duration,String class_number,Integer weekday,String subject);

    public List  getGrowthRecord(String studio,Integer page,String student_name);

    public List  getUserByOpenid(String openid);

    public List  getMessageClient(String nickName);

    public List  getModel(String studio,Integer page);

    public List  getMamaShare(Integer page);

    public List  getPpt(Integer page);

    public List  getLesson(String studio,String student_name,String subject);

    public List  getTipsDataUrl(String studio,Integer left_amount,String subject);

    public List  getLessonInName(String studio,String student_name,Integer page,String subject);

    public List  getGoodsList(String studio,Integer page);

    public List  getRating(String studio,String student_name,Integer page,String subject);

    public int push(Message message);

    public int insertLesson(Lesson lesson);

    public int insertOrder(Order order);

	public int updateLesson(Lesson lesson,Float lessons_amount,Float consume_lesson_amount,String subject_new);

    public List  getDetails(Integer id);

    public List  getDetailsUrlByDate(String studio,String duration,String student_name,String date_time);

    public List  getSearch(String student_name,String studio,Integer page,String class_target);

    public List  getSignUp(String student_name,String studio,String subject);

    public List  getGift(String student_name,String studio);

    public List  getLeaveRecord(String student_name,String studio,String leave_type,String subject);

    public List  getArrangement(String studio,Integer dayofweek,String date,String subject,String openid);

    public int insertSchedule(Schedule schedule);

    public int insertSignUp(SignUp signUp);

    public int insertArrangement(Arrangement arrangement);

    public int insertGift(Gift gift);

    public int insertGoodsList(GoodsList goodsList);

    public List  getSchedule(String date_time,String studio,String subject,String openid);

    public List  getScheduleByClass(String date_time,String duration,String studio,String class_number,String subject,String openid);

    public List  getScheduleDetail(Integer weekDay,String duration,String studio,String class_number,String subject);

    public int  deleteComment(Integer id,String role,String studio,String openid);

    public int  deleteUuids(Integer id,String role,String studio,String openid,String uuid);

    public int  deleteGoodsList(Integer id,String role,String studio,String openid);

    public int  deleteArrangement(Integer id,String role,String studio,String openid);

    public int  changeClassName(String id,String role,String studio,String openid,String class_number,String change_title,String limit_number);

    public int  deleteSignUpRecord(Integer id,String role,String studio,String openid);

    public int  deleteSignUpAllRecord(String name,String role,String studio,String openid);

    public int  deleteGiftRecord(Integer id,String role,String studio,String openid);

    public int  deleteSchedule(Integer id,String role,String studio,String openid);

    public int  deleteScheduleByDate(Integer weekDay,String duration,String studio,String class_number,String role,String openid);

    public int  confirmSchedule(Integer id,String role,String studio,String openid);

    public int  deleteLesson(Integer id,String role,String studio,String openid,String student_name);

    public int insertUser(User user);

    public int updateUser(User user);

    public int updateBossLessons(User user);

    public int updateComentStyle(User user);

    public int updateSchedule(Schedule schedule);

    public int updateComment(Message message);

    public int updateDetailPhoto(Message message);

    public int updateUsertype(User user);

    public List getUser(String openid);

    public List getUserByNickName(String nickName);

    public List getUserByStudio(String Studio);

    public List getStudio(String role);

    public List getClassNumbers(String studio);

    public List getArrangements(String studio);

    public List getArrangementsByDate(String studio,String date_time);

    public List getCertificateModel(String class_name);

    public List getMyOrder(String studio,String openid);

    public List getFrameModel(String studio,Integer page,String class_target);

    public List getCertificateModelName();

    public List getOpenidByNick(String student_name,String studio);

    public List getAdvertise(String studio);

    public List getBook(String studio,String dimension);

    public List getCertificate(String studio,String student_name);

    public List getPaycode(String student_name);

    public List getClassSys(String class_target,String studio,Integer page);

    public List getCourseList(String studio,Integer page);

    public List getCourseDetail(String studio,String class_name,Integer page);

    public List getHome(String studio);

    public int updateMinusLesson(String student_name,String studio,Float class_count,String subject);

    public List  getLessonByName(String student_name,String studio);

    public List  getLessonByNameSubject(String student_name,String studio,String subject);

    public int updateAddPoints(String student_name,String studio,Integer points,String subject);

    public int deletePoints(String student_name,String studio,Integer points,String subject);

    public int updateCoins(String openid,String type);

    public int updateGift(String id);

    public void sendClassRemind();



}
