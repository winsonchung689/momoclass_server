package com.xue.repository.dao;

import com.xue.entity.model.*;

import java.security.acl.Group;
import java.util.List;

public interface UserMapper {
    //获取
    List<Message> getMessage(String studio,Integer page_start,Integer page_length,String class_target,String campus);


    List<Message> getCommentModel();

    List<Message> getAlbum(String studio,String campus,String student_name);

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
    List<Schedule> getScheduleAll(Integer date_time,String studio,String campus);

    List<Schedule> getScheduleAllDistinct(Integer date_time,String studio,String campus);

    List<Schedule> getScheduleByUser(Integer date_time,String studio,String student_name,String campus);

    List<Schedule> getSchedule(Integer date_time,String studio,String subject,String campus);

    List<Schedule> getScheduleCheck(String add_date,String duration,String class_number,String subject,String studio,String campus,String student_name);

    List<Schedule> getScheduleDistinct(Integer date_time,String studio,String subject,String campus);

    //获取
    List<Schedule> getScheduleDetail(Integer weekDay,String duration,String studio,String class_number,String subject,String campus);

    List<Schedule> getScheduleDetailAll_O(Integer weekDay,String duration,String studio,String class_number,String subject,String campus,String date_time);

    List<Schedule> getScheduleDetailAll_T(Integer weekDay,String duration,String studio,String class_number,String subject,String campus,String date_time);

    List<Schedule> getScheduleByClassOrdinary(Integer weekDay,String duration,String studio,String class_number,String subject,String campus);

    List<Schedule> getScheduleByClassTransferred(String add_date,String duration,String studio,String class_number,String subject,String campus);

    //获取
    List<Schedule> getTransferAll(String date_time,String studio,String campus);

    List<Book> getBookDetail(String studio,String create_time,String type,String start_date);

    List<Book> getBookByStudio(String studio);
    List<Book> getBookDetailAll(String studio,String create_time,String start_date);

    List<Book> searchBookDetail(String studio,String value,String type);

    List<BookCount> getBookByMonth(String studio,String campus);

    List<BookCount> getBookByDate(String studio,String campus);

    List<BookCount> getAnalyzeSignUp(String studio,String campus,Integer weekDay);

    List<SignUp> getAnalyzeSignUpDetail(String studio,String campus,Integer weekDay);

    List<Schedule> getAnalyzeTryDetail(String studio,String campus,Integer weekDay);

    List<Leave> getAnalyzeLeaveDetail(String studio,String campus,Integer weekDay);

    List<Leave> getAnalyzeAbsentDetail(String studio,String campus,Integer weekDay);

    List<BookCount> getAnalyzeTry(String studio,String campus,Integer weekDay);

    List<BookCount> getAnalyzeLeave(String studio,String campus,Integer weekDay);

    List<BookCount> getAnalyzeAbsent(String studio,String campus,Integer weekDay);

    List<BookCount> getBookByYear(String studio,String campus);

    List<BookCount> getBookByAll(String studio,String campus);

    List<Schedule> getTransfer(String date_time,String studio,String subject,String campus);

    List<Message> getModel(String studio,Integer page_start,Integer page_end,String campus);

    List<CommunicateRecord> getCommunicateRecord(String studio,Integer page_start,Integer page_end,String campus);

    List<CommunicateRecord> getCommunicateLike(String studio,String item,String campus);

    List<CommunicateRecord> getCommunicateById(String id);

    List<Message> getMamaShare(Integer page_start,Integer page_end);

    //获取
    List<Message> getSearch(String student_name,String studio,Integer page_start,Integer page_end,String class_target,String campus);

    //获取详情
    List<Message> getDetails(Integer id);

    List<Post> getPostPublic(Integer page_start,Integer page_length);

    List<Post> getPostPrivate(Integer page_start,Integer page_length,String studio);

    List<PostComment> getPostComment(String post_id);

    List<PostLike> getPostLike(String post_id);

    List<PostLike> getPostLikeByOpenid(String post_id,String openid);

    List<Message> getDetailsUrlByDate(String studio,String duration,String student_name,String date_time,String campus);

    //插入
    int insertBook(Book book);

    int insertCommunicateRecord(CommunicateRecord communicateRecord);

    int insertAnnouncement(Announcement announcement);

    int push(Message message);

    int insertPost(Post post);

    int insertPostComment(PostComment postComment);

    int insertPostLike(PostLike postLike);

    //插入
    int insertSchedule(Schedule schedule);

    int insertOrder(Order order);

    void deleteComment(Integer id,String studio);

    void deletePost(Integer id);

    void deleteBookDetail(Integer id,String studio);

    void deleteCommunicateRecord(Integer id,String studio);

    void updateUuids(Integer id,String studio,String uuids);

    void updateBookMark(Integer id,String mark);

    void updateBookAmount(Integer id,String amount);

    void updateBookCreateTime(Integer id,String create_time);

    void updateUuids_c(Integer id,String studio,String uuids_c);

    void deleteGoodsList(Integer id,String studio);

    void deleteScheduleByDate(Integer weekDay,String duration,String studio,String class_number,String subject);

    void deleteArrangement(Integer id,String studio);

    void deleteGroupBuy(String goods_id,String nick_name,String studio);

    void changeClassName(Integer id,String studio,String class_number);

    void modifyGoodsIntro(String id,String studio,String campus,String goods_intro);

    void modifyGoodsName(String id,String studio,String campus,String goods_intro);

    void modifyGoodsPrice(String id,String studio,String campus,String goods_intro);

    void modifyMark(String id,String studio,String mark);

    void updateLocation(String studio,String openid,String phone_number,String location);

    void updateNewName(String openid,String new_name);

    void changeSubjectName(Integer id,String studio,String class_number);

    void changeLimit(Integer id,String studio,String limits);

    void changeDuration(Integer id,String studio,String duration);

    void changeScheduleClassName(String old_class_number,String studio,String duration,String new_class_number,String subject,String campus);

    void changeScheduleDuration(String class_number,String studio,String duration,String new_duration,String subject,String campus);

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

    void deleteLessonPackage(Integer id);

    //插入
    int insertUser(User user);

    int updateUser(User user);

    int updateOpenid(User user);

    int updateOpenidById(String openid_old,String openid_new);

    int updateAvatar(User user);

    int updateUserDelete(User user);

    int updateUserBackUrl(User user);

    int updateCommunicateContent(String id,String content);

    int updateCommunicateUuids(String id,String uuids);

    int updateBossLessons(User user);

    int updateSchedule(Schedule schedule);

    int updateComment(Message message);

    int updateDetailPhoto(Message message);

    int updateUsertype(User user);

    int updateUserCampus(User user);

    int updateUserStudio(String studio,String new_studio);

    int updateLessonStudio(String studio,String new_studio);

    int updateClassScheduleStudio(String studio,String new_studio);

    int updateCommentStudio(String studio,String new_studio);

    int updateScheduleArrangementStudio(String studio,String new_studio);

    int updateSignUpRecordStudio(String studio,String new_studio);

    int updateLeaveRecordStudio(String studio,String new_studio);

    int updateCombine(Lesson lesson);

    int updateLessonCampus(String studio,String student_name,String campus);

    int updateLessonLeft(Float left_amount,String studio,String student_name,String campus,String subject);

    int updateLessonTotal(Float left_amount,String studio,String student_name,String campus,String subject);

    int updateLessonMinus(Float minus,String studio,String student_name,String campus,String subject);

    int updateLessonCoins(Float coins,String studio,String student_name,String campus,String subject);

    int updateLessonTotalMoney(Float total_money,String studio,String student_name,String campus,String subject);

    int updateLessonDiscountMoney(Float discount_money,String studio,String student_name,String campus,String subject);

    int updateSignUpCampus(String studio,String student_name,String campus);

    int updateGiftCampus(String studio,String student_name,String campus);

    int updateNoteCampus(String studio,String student_name,String campus);

    int updateLeaveCampus(String studio,String student_name,String campus);

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

    List<User> getUserByChooseLesson(String chooseLesson);

    List<User> getUserByOpenidIgnore(String openid);

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

    List<Lesson> getSubjectByStudio(String studio,String campus);

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

    List<LessonPackage> getLessonPackage(String student_name,String studio,String campus,String subject);

    List<LessonPackage> getLessonPackageByStudent(String student_name,String studio,String campus);

    List<LessonPackage> getLessonPackageByStudentSubject(String student_name,String studio,String campus,String subject);

    List<LessonPackage> getLessonPackageByCampus(String studio,String campus);

    List<LessonPackage> getLessonPackageBySubject(String studio,String campus,String subject);

    List<Lesson> getLessonByPage(String studio,String campus,Integer page_start,Integer page_length);

    List<Lesson> getClassNote(String subject,String studio,String student_name);

    List<Announcement> getAnnouncement(String studio);

    List<Lesson> getLessonBySubject(String studio,String subject,String campus);

    List<Lesson> getLessonByStudio(String studio);

    List<Lesson> getLessonBySubjectByPage(String studio,String subject,String campus,Integer page_start,Integer page_length);

    List<Lesson> getTipsDataUrl(String studio,Integer left_amount,String subject,String campus);

    List<Lesson> getTipsDataUrlAll(String studio,Integer left_amount,String campus);

    AllCount getLessonAllCount(String studio,String campus);

    AllCount getLessonAllCountBySubject(String studio,String subject,String campus);

    Integer getLessonNeedPayCount(String studio,String campus);

    Integer getLessonNeedPayCountBySubject(String studio,String subject,String campus);

    Integer getLessonOweCount(String studio,String campus);

    Integer getLessonOweCountBySubject(String studio,String subject,String campus);

    Integer getLessonAllCountByDay(String studio,Integer day,String duration,String class_number,String subject,String campus);

    Integer getScheduleRemind(String studio,Integer day,String duration,String class_number,String subject);

    List<Lesson> getCampusByStudio(String studio);

    Integer getLessonAllCountByDayUnconfirmed(String studio,Integer day,String duration,String class_number,String subject,String campus);

    Integer getLessonAllCountByDayByName(String studio,Integer day,String duration,String class_number,String subject,String student_name,String campus);

    Integer getClassesCountAll(String studio,String campus);

    Integer getClassesCountAllLesson(String studio,String campus);

    Integer getClassesCountBySubject(String studio,String subject,String campus);

    Integer getSignUpByMonthAll(String studio,String create_time,String campus);

    Integer getSignUpByMonth(String studio,String subject,String create_time,String campus);

    Integer getSignUpByMonthStudent(String studio,String subject,String create_time,String campus,String student_name);

    Integer getClassesCountBySubjectLesson(String studio,String subject,String campus);

    Integer getClassesCountByStudent(String studio,String subject,String campus,String student_name);

    Integer getSignUpCountByDay(String studio,String date,String duration,String class_number,String campus);

    List<Lesson> getRating(String studio,Integer page_start,Integer page_end,String campus);

    List<Lesson> getRatingBySubject(String studio,Integer page_start,Integer page_end,String subject,String campus);

    List<Lesson> getRatingByName(String studio,String student_name,Integer page_start,Integer page_end);

    List<Lesson> getRatingByNameBySubject(String studio,String student_name,Integer page_start,Integer page_end,String subject);

    List<Lesson> getLessonLikeName(String studio,String student_name,String campus);

    List<Lesson> getLessonLikeNameBySubject(String studio,String student_name,String subject,String campus);

    int insertNote(Note note);

    int insertLessonPackage(LessonPackage lessonPackage);

    int insertLesson(Lesson lesson);

    int insertSignUp(SignUp signUp);

    int insertLeave(Leave leave);

    int insertArrangement(Arrangement arrangement);

    int insertGoodsList(GoodsList goodsList);

    int insertGroupBuy(GroupBuy groupBuy);

    int insertGift(Gift gift);

    List<Arrangement> getArrangement(String studio,String dayofweek,String subject,String campus);

    List<GoodsList> getGoodsList(String studio,Integer page_start,Integer page_end,String campus);

    List<GroupBuy> getGroupBuy(String studio, String goods_id);

    List<Arrangement> getArrangementAll(String studio,String dayofweek,String campus);

    List<Arrangement> getArrangementById(String studio,Integer id);

    List<Arrangement> getArrangementByDate(String studio,String dayofweek,String class_number,String duration,String subject,String campus);

    List<SignUp> getSignUp(String student_name,String studio,String subject);

    List<SignUp> getStudentByTeacher(String studio,String teacher);


    List<SignUp> getStudentByTeacherAll();

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

    int updateSignUpTeacher(String teacher,String id);

    int updateLessonBoth(Lesson lesson);

    int updateLessonSubject(String subject_new,String student_name,String studio, String subject,String campus);

    int updateCoinsAll(Float coins,String studio,String campus);

    int updateCoinsByStudent(Float coins,String studio,String campus,String student_name,String subject);

    int updatePriceAll(Float coins,String studio,String campus);

    int updatePriceByStudent(Float coins,String studio,String campus,String student_name,String subject);

    int updateGift(String id);

    int updateCoins(User user);

    int updateScheduleName(String student_name_new,String student_name,String studio,String campus);

    int updateRemind(Integer remind,String subject,String studio,String duration,String class_number,String dayofweek,String campus);

    int updateCommentName(String student_name_new,String student_name,String studio,String campus);

    int updateGiftRecordName(String student_name_new,String student_name,String studio,String campus);

    int updateLessonName(String student_name_new,String student_name,String studio,String campus);

    int updateSignUpRecordName(String student_name_new,String student_name,String studio,String campus);

    int updateUserStudent(String student_name_new,String student_name,String studio,String campus);

    int updateUserStudentByOpenid(String student_name,String openid,String id);

    int updateUserExpired(String role,String studio,String role_old,String campus);

    int updateUserMember(String member,String studio);

    List<Lesson> getLessonByName(String student_name,String studio,String campus);

    List<Lesson> getLessonByNameSubject(String student_name,String studio,String subject,String campus);

    int updateLessonPoint(Lesson lesson);



 
}