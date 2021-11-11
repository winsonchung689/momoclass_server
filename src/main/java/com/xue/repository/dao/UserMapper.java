package com.xue.repository.dao;

import com.xue.entity.model.Message;
import com.xue.entity.model.Schedule;
import com.xue.entity.model.User;

import java.util.List;

public interface UserMapper {
    //获取
    List<Message> getMessage();

    //获取
    List<Message> getAdvertise();

    //获取
    List<Message> getMessageClient(String student_name);

    //获取
    List<Schedule> getSchedule(String date_time);

    List<Message> getModel();

    //获取
    List<Message> getSearch(String student_name);

    //获取详情
    List<Message> getDetails(Integer id);

    //插入
    int push(Message message);

    //插入
    int insertSchedule(Schedule schedule);

    void deleteComment(Integer id);

    //插入
    int insertUser(User user);

    List<User> getUser(String nick_name);

 
}