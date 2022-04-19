package com.xue.repository.dao;

import com.xue.entity.model.*;

import java.util.List;

public interface UserMapper {
    //获取
    List<Message> getMessage(String studio,Integer page_start,Integer page_length);

    //获取
    List<Message> getCertificate(String studio);

    //获取
    List<Message> getPaycode(String student_name);

    //获取
    List<Message> getCertificateByName(String studio,String student_name);

    //获取
    List<Message> getAdvertise(String studio);

    //获取
    List<Message> getPpt(Integer page_start, Integer page_length);

    //获取
    List<Message> getClassSys(String class_target,String studio,Integer limit);

    //获取
    List<Message> getCourseList(String studio,Integer page_start, Integer page_length);

    //获取
    List<Message> getCourseDetail(String studio,String class_name);

    //获取
    List<Message> getHome(String studio);

    //获取
    List<Message> getMessageClient(String student_name);

    //获取
    List<Schedule> getSchedule(Integer date_time,String studio);

    List<Message> getModel(String studio,Integer page_start,Integer page_end);

    //获取
    List<Message> getSearch(String student_name,String studio);

    //获取详情
    List<Message> getDetails(Integer id);

    //插入
    int push(Message message);

    //插入
    int insertSchedule(Schedule schedule);

    void deleteComment(Integer id);

    void deleteSchedule(Integer id);

    void deleteLesson(Integer id);

    //插入
    int insertUser(User user);

    int updateUser(User user);

    int updateSchedule(Schedule schedule);

    int updateUsertype(User user);

    List<User> getUser(String openid);

    List<User> getStudio();

    List<Message> getCertificateModel(String class_name);

    List<Message> getCertificateModelName();

    List<User> getOpenidByNick(String student_name,String studio);

    List<Lesson> getLesson(String studio);

    List<Lesson> getRating(String studio);

    List<Lesson> getRatingByName(String studio,String student_name);

    List<Lesson> getLessonLikeName(String studio,String student_name);

    int insertLesson(Lesson lesson);

    int insertSignUp(SignUp signUp);

    int insertGift(Gift gift);

    List<SignUp> getSignUp(String student_name,String studio);

    List<Gift> getGift(String student_name,String studio);

    int updateLesson(Lesson lesson);

    int updateGift(String id);

    int updateCoins(User user);

    List<Lesson> getLessonByName(String student_name,String studio);

    int updateLessonPoint(Lesson lesson);



 
}