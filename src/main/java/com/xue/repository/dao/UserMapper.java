package com.xue.repository.dao;

import com.xue.entity.model.*;

import java.util.List;

public interface UserMapper {
    //获取
    List<Message> getMessage(String studio,Integer page_start,Integer page_length);

    List<Message> getMessageByName(String studio,String student_name,Integer page_start,Integer page_length);

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
    List<Message> getStudentPhoto(String student_name,String studio);

    //获取
    List<Message> getClassSys(String class_target,String studio,Integer page_start, Integer page_length);

    //获取
    List<Message> getCourseList(String studio,Integer page_start, Integer page_length);

    //获取
    List<Message> getCourseDetail(String studio,String class_name,Integer page_start, Integer page_length);

    //获取
    List<Message> getHome(String studio);

    //获取
    List<Message> getMessageClient(String student_name);

    //获取
    List<Message> getMessageInName(String student_names,String studio,Integer page_start,Integer page_length);

    List<Message> getMessageGrowth(String student_names,String studio,Integer page_start,Integer page_length);

    List<Lesson> getLessonInName(String studio,String student_names,Integer page_start,Integer page_length);

    //获取
    List<Schedule> getSchedule(Integer date_time,String studio);

    //获取
    List<Schedule> getScheduleDetail(Integer weekDay,String duration,String studio);

    //获取
    List<Schedule> getTransfer(String date_time,String studio);

    List<Message> getModel(String studio,Integer page_start,Integer page_end);

    List<Message> getMamaShare(Integer page_start,Integer page_end);

    //获取
    List<Message> getSearch(String student_name,String studio,Integer page_start,Integer page_end);

    //获取详情
    List<Message> getDetails(Integer id);
    //获取详情
    List<Message> getDetailsUrlByDate(String studio,String duration,String student_name,String date_time);

    //插入
    int push(Message message);

    //插入
    int insertSchedule(Schedule schedule);

    void deleteComment(Integer id,String studio);

    void deleteArrangement(Integer id,String studio);

    void deleteStudentPhoto(String student_name,String studio);

    void deleteHome(String studio);

    void deleteSignUpRecord(Integer id,String studio);

    void deleteSignUpAllRecord(String student_name,String studio);

    void deleteLeaveAllRecord(String student_name,String studio);

    void deleteGiftRecord(Integer id,String studio);

    void deleteLeaveRecord(Integer id,String studio);

    void deleteSchedule(Integer id,String studio);

    void deleteLesson(Integer id,String studio);

    //插入
    int insertUser(User user);

    int updateUser(User user);

    int updateSchedule(Schedule schedule);

    int updateComment(Message message);

    int updateDetailPhoto(Message message);

    int updateUsertype(User user);

    int updateTheme(String theme,String param2);

    int updateUserPay(User user);

    int updateComentStyle(User user);

    List<User> getUser(String openid);

    List<User> getUserByOpenid(String openid);

    List<User> getUserByStudent(String student_name,String studio);

    List<User> getComentStyle(String studio);

    List<User> getUserByNickName(String nickName);

    List<User> getAllUser();

    List<User> getStudio();

    List<Arrangement> getArrangements(String studio);

    List<Arrangement> getClassNumber(String studio,Integer dayofweek,String duration);

    List<Message> getCertificateModel(String class_name);

    List<Message> getCertificateModelName();

    List<User> getOpenidByNick(String student_name,String studio);

    List<Lesson> getLesson(String studio);

    List<Lesson> getTipsDataUrl(String studio,Integer left_amount);

    Integer getLessonAllCount(String studio);

    Integer getLessonNeedPayCount(String studio);

    Integer getLessonOweCount(String studio);

    Integer getLessonAllCountByDay(String studio,Integer day,String duration);

    Integer getSignUpCountByDay(String studio,String date,String duration);

    List<Lesson> getRating(String studio,Integer page_start,Integer page_end);

    List<Lesson> getRatingByName(String studio,String student_name,Integer page_start,Integer page_end);

    List<Lesson> getLessonLikeName(String studio,String student_name);

    int insertLesson(Lesson lesson);

    int insertSignUp(SignUp signUp);

    int insertLeave(Leave leave);

    int insertArrangement(Arrangement arrangement);

    int insertGift(Gift gift);

    List<Arrangement> getArrangement(String studio,String dayofweek);

    List<SignUp> getSignUp(String student_name,String studio);

    List<Message> getCommentByDate(String student_name,String studio,String date_time);

    List<Message> getCommentByDateDuration(String student_name,String studio,String date_time,String duration);

    List<SignUp> getSignUpByDate(String student_name,String studio,String date_time);

    List<SignUp> getSignUpByDateDuration(String student_name,String studio,String date_time,String duration);

    List<Gift> getGift(String student_name,String studio);

    List<Leave> getLeaveRecord(String student_name,String studio);

    List<Leave> getLeaveByDateDuration(String student_name,String studio,String date_time,String duration);

    int updateLesson(Lesson lesson);

    int updateLessonAll(Float coins,String studio);

    int updateGift(String id);

    int updateCoins(User user);

    int updateScheduleName(String student_name_new,String student_name,String studio);

    int updateCommentName(String student_name_new,String student_name,String studio);

    int updateGiftRecordName(String student_name_new,String student_name,String studio);

    int updateLessonName(String student_name_new,String student_name,String studio);

    int updateSignUpRecordName(String student_name_new,String student_name,String studio);

    List<Lesson> getLessonByName(String student_name,String studio);

    int updateLessonPoint(Lesson lesson);



 
}