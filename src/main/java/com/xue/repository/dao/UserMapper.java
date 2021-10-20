package com.xue.repository.dao;

import com.xue.entity.model.Schedule;
import com.xue.entity.model.User;
import com.xue.entity.model.UserExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    //获取
    List<User> selectUser();

    //获取
    List<Schedule> selectSchedule();

    List<User> selectModel();

    //获取
    List<User> selectSearch(String student_name);

    //获取详情
    List<User> selectDetails(Integer id);

    //插入
    int insertUser(User user);

    //插入
    int insertSchedule(Schedule schedule);

    void deleteUser(Integer id);

 
}