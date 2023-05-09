package com.xue.repository.dao;

import com.xue.entity.model.*;

import java.util.List;

public interface UserMapper {
    //获取
    List<Message> getMessage(String studio,Integer page_start,Integer page_length,String class_target,String campus);

    List<Message> getMessageByName(String studio,String student_name,Integer page_start,Integer page_length);

    //获取
    List<Message> getCertificate(String studio);

    //获取
    List<Message> getPaycode(String student_name);

    //获取
    List<Message> getCertificateByName(String studio,String student_name);

    //获取
    List<Message> getAdvertise(String class_target,String studio,Integer page_start, Integer page_length);

    //获取
    List<Message> getPpt(Integer page_start, Integer page_length);

    //获取
    List<Message> getStudentPhoto(String student_name,String studio,String campus);

    //获取
    List<Message> getClassSys(String class_target,String studio,Integer page_start, Integer page_length,String campus);

    List<Message> getUuidById(String studio,Integer id);

    //获取
    List<Message> getCourseList(String studio,Integer page_start, Integer page_length);

    //获取
    List<Message> getCourseDetail(String studio,String class_name,Integer page_start, Integer page_length);

    //获取
    List<Message> getHome(String studio,String campus);

    //获取
    List<Message> getMessageClient(String student_name);

    //获取
    List<Message> getMessageInName(String student_names,String studio,Integer page_start,Integer page_length,String class_target,String campus);

    List<Message> getMessageGrowth(String student_names,String studio,Integer page_start,Integer page_length);

    List<Lesson> getLessonInName(String studio,String student_names,Integer page_start,Integer page_length,String campus);

    List<Lesson> getLessonInNameBySubject(String studio,String student_names,Integer page_start,Integer page_length,String subject,String campus);

    //获取
    List<Schedule> getScheduleAll(Integer date_time,String studio);

    List<Schedule> getScheduleByUser(Integer date_time,String studio,String student_name);

    List<Schedule> getSchedule(Integer date_time,String studio,String subject);

    //获取
    List<Schedule> getScheduleDetail(Integer weekDay,String duration,String studio,String class_number,String subject,String campus);

    List<Schedule> getScheduleByClassOrdinary(Integer weekDay,String duration,String studio,String class_number,String subject);

    List<Schedule> getScheduleByClassTransferred(String add_date,String duration,String studio,String class_number,String subject);

    //获取
    List<Schedule> getTransferAll(String date_time,String studio);

    List<Book> getBookDetail(String studio,String create_time,String type,String start_date);

    List<Book> getBookDetailAll(String studio,String create_time,String start_date);

    List<Book> searchBookDetail(String studio,String value,String type);

    List<BookCount> getBookByMonth(String studio,String campus);

    List<BookCount> getBookByDate(String studio,String campus);

    List<BookCount> getBookByYear(String studio,String campus);

    List<BookCount> getBookByAll(String studio,String campus);

    List<Schedule> getTransfer(String date_time,String studio,String subject);

    List<Message> getModel(String studio,Integer page_start,Integer page_end,String campus);

    List<Message> getMamaShare(Integer page_start,Integer page_end);

    //获取
    List<Message> getSearch(String student_name,String studio,Integer page_start,Integer page_end,String class_target,String campus);

    //获取详情
    List<Message> getDetails(Integer id);
    //获取详情
    List<Message> getDetailsUrlByDate(String studio,String duration,String student_name,String date_time,String campus);

    //插入
    int insertBook(Book book);

    int insertAnnouncement(Announcement announcement);

    int push(Message message);

    //插入
    int insertSchedule(Schedule schedule);

    int insertOrder(Order order);

    void deleteComment(Integer id,String studio);

    void deleteBookDetail(Integer id,String studio);

    void updateUuids(Integer id,String studio,String uuids);

    void updateUuids_c(Integer id,String studio,String uuids_c);

    void deleteGoodsList(Integer id,String studio);

    void deleteScheduleByDate(Integer weekDay,String duration,String studio,String class_number,String subject);

    void deleteArrangement(Integer id,String studio);

    void changeClassName(Integer id,String studio,String class_number);

    void modifyMark(String id,String studio,String mark);

    void updateLocation(String studio,String openid,String phone_number,String location);

    void updateNewName(String openid,String new_name);

    void changeSubjectName(Integer id,String studio,String class_number);

    void changeLimit(Integer id,String studio,String class_number);

    void changeScheduleClassName(String old_class_number,String studio,String duration,String new_class_number,String subject,String campus);

    void changeSignUpClassName(String old_class_number,String studio,String duration,String new_class_number,String subject,String campus);

    void changeScheduleSubject(String old_subject,String studio,String duration,String new_subject,String class_number,String campus);

    void changeSignUpSubject(String old_subject,String studio,String duration,String new_subject,String class_number,String campus);

    void deleteStudentPhoto(String student_name,String studio);

    void deleteHome(String studio);

    void deleteMyOrder(Integer id);

    void deleteNote(Integer id);

    void deleteUser(Integer id);

    void deliverMyOrder(Integer id);

    void deleteSignUpRecord(Integer id,String studio);

    void deleteSignUpAllRecord(String student_name,String studio);

    void deleteLeaveAllRecord(String student_name,String studio,String leave_type,String subject);

    void deleteGiftRecord(Integer id,String studio);

    void deleteLeaveRecord(Integer id,String studio);

    void deleteSchedule(Integer id,String studio);

    void deleteScheduleByLesson(String student_name,String studio);

    void confirmSchedule(Integer id,String studio);

    void deleteLesson(Integer id,String studio);

    //插入
    int insertUser(User user);

    int updateUser(User user);

    int updateOpenid(User user);

    int updateAvatar(User user);

    int updateUserDelete(User user);

    int updateBossLessons(User user);

    int updateSchedule(Schedule schedule);

    int updateComment(Message message);

    int updateDetailPhoto(Message message);

    int updateUsertype(User user);

    int updateUserCampus(User user);

    int updateSubscription(User user);

    int updateSubject(User user);

    int updateTheme(String theme,String param2);

    int updatVideoDisplay(String studio,Integer display);

    int updatCoverDisplay(String studio,Integer cover);

    int updateUserPay(User user);

    int updateComentStyle(User user);

    int updateSendTime(User user);

    List<User> getUser(String openid);

    List<User> getUserSendTime(String studio);

    List<User> getUserByOpenid(String openid);

    List<User> getUserByStudent(String student_name,String studio);

    List<User> getUserByNickStudio(String nick_name,String studio);

    List<User> getUserByNickStudioEq(String nick_name,String studio);

    List<User> getComentStyle(String studio);

    List<User> getUserByNickName(String nickName);

    List<User> getUserByStudio(String studio,String campus);

    List<User> getAllUserByStudio(String studio);

    List<User> getBossByStudio(String studio);
    List<User> getAllUser();

    List<User> getStudio();

    List<User> getStudioBoss(String role);

    List<Schedule> getClassNumbers(String studio);

    List<Arrangement> getArrangements(String studio);

    List<Order> getMyOrder(String studio,String openid);

    List<Order> getAllOrder(String studio);

    List<Arrangement> getArrangementsByDate(String studio);

    List<Arrangement> getClassNumber(String studio,Integer dayofweek,String duration);

    List<Message> getCertificateModel(String class_name);

    List<Message> getFrameModel(String studio,Integer page_start,Integer page_length,String class_target,String campus);

    List<Message> getCertificateModelName();

    List<User> getOpenidByNick(String student_name,String studio);

    List<Lesson> getLesson(String studio,String campus);

    List<Lesson> getClassNote(String subject,String studio,String student_name);

    List<Announcement> getAnnouncement(String studio);

    List<Lesson> getLessonBySubject(String studio,String subject,String campus);

    List<Lesson> getTipsDataUrl(String studio,Integer left_amount,String subject);

    List<Lesson> getTipsDataUrlAll(String studio,Integer left_amount);

    AllCount getLessonAllCount(String studio,String campus);

    AllCount getLessonAllCountBySubject(String studio,String subject,String campus);

    Integer getLessonNeedPayCount(String studio,String campus);

    Integer getLessonNeedPayCountBySubject(String studio,String subject,String campus);

    Integer getLessonOweCount(String studio);

    Integer getLessonOweCountBySubject(String studio,String subject,String campus);

    Integer getLessonAllCountByDay(String studio,Integer day,String duration,String class_number,String subject,String campus);

    Integer getScheduleRemind(String studio,Integer day,String duration,String class_number,String subject);

    Integer getLessonAllCountByDayUnconfirmed(String studio,Integer day,String duration,String class_number,String subject,String campus);

    Integer getLessonAllCountByDayByName(String studio,Integer day,String duration,String class_number,String subject,String student_name,String campus);

    Integer getClassesCountAll(String studio,String campus);

    Integer getClassesCountAllLesson(String studio,String campus);

    Integer getClassesCountBySubject(String studio,String subject,String campus);

    Integer getSignUpByMonthAll(String studio,String create_time);

    Integer getSignUpByMonth(String studio,String subject,String create_time);

    Integer getClassesCountBySubjectLesson(String studio,String subject,String campus);

    Integer getSignUpCountByDay(String studio,String date,String duration,String class_number,String campus);

    List<Lesson> getRating(String studio,Integer page_start,Integer page_end,String campus);

    List<Lesson> getRatingBySubject(String studio,Integer page_start,Integer page_end,String subject,String campus);

    List<Lesson> getRatingByName(String studio,String student_name,Integer page_start,Integer page_end);

    List<Lesson> getRatingByNameBySubject(String studio,String student_name,Integer page_start,Integer page_end,String subject);

    List<Lesson> getLessonLikeName(String studio,String student_name,String campus);

    List<Lesson> getLessonLikeNameBySubject(String studio,String student_name,String subject,String campus);

    int insertNote(Note note);

    int insertLesson(Lesson lesson);

    int insertSignUp(SignUp signUp);

    int insertLeave(Leave leave);

    int insertArrangement(Arrangement arrangement);

    int insertGoodsList(GoodsList goodsList);

    int insertGift(Gift gift);

    List<Arrangement> getArrangement(String studio,String dayofweek,String subject,String campus);

    List<GoodsList> getGoodsList(String studio,Integer page_start,Integer page_end,String campus);

    List<Arrangement> getArrangementAll(String studio,String dayofweek,String campus);

    List<Arrangement> getArrangementById(String studio,Integer id);

    List<Arrangement> getArrangementByDate(String studio,String dayofweek,String class_number,String duration,String subject,String campus);

    List<SignUp> getSignUp(String student_name,String studio,String subject);

    List<SignUp> getStudentByTeacher(String studio,String teacher);

    List<SignUp> getStudentByTeacherByDuration(String studio,String teacher,String date_start,String date_end);

    List<Message> getCommentByDate(String student_name,String studio,String date_time,String campus);

    List<Message> getCommentByDateDuration(String student_name,String studio,String date_time,String duration,String campus);

    List<SignUp> getSignUpByDate(String student_name,String studio,String date_time,String campus);

    List<SignUp> getSignUpByDateDuration(String student_name,String studio,String date_time,String duration,String campus);

    List<Gift> getGift(String student_name,String studio);

    List<Leave> getLeaveRecord(String student_name,String studio,String leave_type,String subject);

    List<Leave> getLeaveRecordAll(String student_name,String studio,String leave_type,String subject);

    List<Leave> getLeaveByDateDuration(String student_name,String studio,String date_time,String duration);

    int updateLesson(Lesson lesson);

    int updateLessonSubject(String subject_new,String student_name,String studio, String subject);

    int updateLessonAll(Float coins,String studio,String campus);

    int updateGift(String id);

    int updateCoins(User user);

    int updateScheduleName(String student_name_new,String student_name,String studio,String campus);

    int updateRemind(Integer remind,String subject,String studio,String duration,String class_number,String dayofweek,String campus);

    int updateCommentName(String student_name_new,String student_name,String studio,String campus);

    int updateGiftRecordName(String student_name_new,String student_name,String studio,String campus);

    int updateLessonName(String student_name_new,String student_name,String studio,String campus);

    int updateSignUpRecordName(String student_name_new,String student_name,String studio,String campus);

    int updateUserStudent(String student_name_new,String student_name,String studio,String campus);

    int updateUserExpired(String role,String studio,String role_old,String campus);

    int updateUserMember(String member,String studio);

    List<Lesson> getLessonByName(String student_name,String studio,String campus);

    List<Lesson> getLessonByNameSubject(String student_name,String studio,String subject,String campus);

    int updateLessonPoint(Lesson lesson);



 
}