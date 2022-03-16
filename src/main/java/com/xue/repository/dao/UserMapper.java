package com.xue.repository.dao;

import com.xue.entity.model.Lesson;
import com.xue.entity.model.Message;
import com.xue.entity.model.Schedule;
import com.xue.entity.model.User;

import java.util.List;

public interface UserMapper {
    //获取
    List<Message> getMessage(String studio);

    //获取
    List<Message> getCertificate(String studio);

    //获取
    List<Message> getPaycode(String student_name);

    //获取
    List<Message> getCertificateByName(String studio,String student_name);

    //获取
    List<Message> getAdvertise(String studio);

    //获取
    List<Message> getClassSys(String studio);

    //获取
    List<Message> getHome(String studio);

    //获取
    List<Message> getMessageClient(String student_name);

    //获取
    List<Schedule> getSchedule(Integer date_time,String studio);

    List<Message> getModel(String studio);

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

    int updateUsertype(User user);

    List<User> getUser(String openid);

    List<User> getStudio();

    List<User> getOpenidByNick(String student_name,String studio);

    List<Lesson> getLesson(String studio);

    int insertLesson(Lesson lesson);

    int updateLesson(Lesson lesson);

    List<Lesson> getLessonByName(String student_name,String studio);

    int updateLessonPoint(Lesson lesson);



 
}