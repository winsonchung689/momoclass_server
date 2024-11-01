package com.xue.repository.dao;

import com.alibaba.fastjson.JSONObject;
import com.xue.entity.model.*;

import javax.xml.soap.Text;
import java.security.acl.Group;
import java.util.List;

public interface UserMapper {
    //获取
    List<Message> getMessage(String studio,Integer page_start,Integer page_length,String class_target,String campus);


    List<Message> getCommentModel();

    List<Message> getAlbum(String studio,String campus,String student_name);

    List<Contract> getContract(String studio,String campus);

    List<Message> getExhibitionByType(String studio,String type,Integer page_start,Integer page_length);

    List<Message> getUpdateNews();

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

    List<Message> getUuidById(Integer id);

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

    List<Schedule> getScheduleById(Integer id);

    List<Schedule> getScheduleByStudent(String studio,String campus,String subject,String student_name);

    List<Schedule> getScheduleByStudentDay(String studio,Integer day,String duration,String class_number,String subject,String campus,String student_name);

    List<Schedule> getScheduleAllDistinct(Integer date_time,String studio,String campus);

    List<Schedule> getScheduleByUser(Integer date_time,String studio,String student_name,String campus);

    List<Schedule> getScheduleByUserDurationSt(Integer date_time,String studio,String student_name,String campus,String duration_st);

    List<Schedule> getSchedule(Integer date_time,String studio,String subject,String campus);

    List<Schedule> getScheduleCheck(String add_date,String duration,String class_number,String subject,String studio,String campus,String student_name);

    List<Schedule> getScheduleByDuration(String add_date,String duration,String class_number,String subject,String studio,String campus);

    List<Schedule> getScheduleCheckArrangement(Integer add_date,String duration,String class_number,String subject,String studio,String campus,String student_name);

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

    List<Book> getBookDetailByMark(String studio,String create_time,String start_date);

    List<Book> searchBookDetail(String studio,String value,String type);

    List<BookCount> getBookByMonth(String studio,String campus);

    List<BookCount> getBookByDate(String studio,String campus);

    List<AnalyzeCount> getAnalyzeSignUp(String studio,String campus,String start_date,String end_date);

    List<AnalyzeCount> getAnalyzeSignUpByStudent(String studio,String campus,String start_date,String end_date);

    List<AnalyzeCount> getAnalyzeSignUpBySubject(String studio,String campus,String subject,String start_date,String end_date);

    List<AnalyzeCount> getAnalyzeSignUpByMonth(String studio,String campus,String start_date,String end_date);

    List<AnalyzeCount> getAnalyzeSignUpByMonthByStudent(String studio,String campus,String start_date,String end_date);

    List<AnalyzeCount> getAnalyzeSignUpByMonthBySubject(String studio,String campus,String subject,String start_date,String end_date);

    List<SignUp> getAnalyzeSignUpDetail(String studio,String campus,String create_time);

    List<SignUp> getAnalyzeSignUpDetailByStudent(String studio,String campus,String create_time,String student_name);

    List<SignUp> getAnalyzeSignUpDetailBySubject(String studio,String campus,String subject,String create_time,String student_name);

    List<SignUp> getAnalyzeSignUpDetailByMonth(String studio,String campus,String create_time);

    List<SignUp> getAnalyzeSignUpDetailByMonthByStudent(String studio,String campus,String create_time,String student_name);

    List<SignUp> getAnalyzeSignUpDetailByMonthBySubject(String studio,String campus,String subject,String create_time,String student_name);

    List<Schedule> getAnalyzeTryDetail(String studio,String campus,Integer weekDay);

    List<Leave> getAnalyzeLeaveDetail(String studio,String campus,Integer weekDay);

    List<Leave> getAnalyzeAbsentDetail(String studio,String campus,Integer weekDay);

    List<AnalyzeCount> getAnalyzeTry(String studio,String campus,String create_time);

    List<AnalyzeCount> getAnalyzeTryByMonth(String studio,String campus,String create_time);

    List<AnalyzeCount> getAnalyzeLeave(String studio,String campus,String create_time);

    List<AnalyzeCount> getAnalyzeLeaveByMonth(String studio,String campus,String create_time);

    List<BookCount> getAnalyzeAbsent(String studio,String campus,Integer weekDay);

    List<BookCount> getBookByYear(String studio,String campus);

    List<BookCount> getBookByAll(String studio,String campus);

    List<Schedule> getTransfer(String date_time,String studio,String subject,String campus);

    List<Message> getModel(String studio,Integer page_start,Integer page_end,String campus);

    List<Message> getOnlineTeacher(String type,Integer page_start,Integer page_end);

    List<Message> getAllOnlineTeacher();

    List<Message> getOnlineTeacherByOpenid(String openid);

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

    List<PostComment> getLeaveMessage(String studio,String type);

    List<PostComment> getPostComment(String post_id);

    List<PostLike> getPostLike(String post_id);

    List<PostLike> getPostLikeByOpenid(String post_id,String openid);

    List<Points> getPointsRecordByStudent(String student_name,String studio,String campus,String subject,String create_time);

    List<Message> getDetailsUrlByDate(String studio,String duration,String student_name,String date_time,String campus,String class_target_bak);

    //插入
    int insertBook(Book book);

    int insertWebsite(Website website);

    int insertContract(String studio, String campus, String contract, String create_time);

    int insertBookDetail(BookDetail bookDetail);

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

    void updateVideoTop(Integer id,String create_time);

    void deletePost(Integer id);

    void deletePostComment(Integer id);

    void deleteBookDetail(Integer id,String studio);

    void deleteCommunicateRecord(Integer id,String studio);

    void updateUuids(Integer id,String studio,String uuids);

    void updateBookMark(Integer id,String mark);

    void updateBookAmount(Integer id,String amount);

    void updateBookCreateTime(Integer id,String create_time);

    void updateUuids_c(Integer id,String studio,String uuids_c);

    void deleteGoodsList(Integer id,String studio);

    void deleteScheduleByDate(Integer weekDay,String duration,String studio,String class_number,String subject);

    void cancelBook(String add_date,String duration,String studio,String class_number,String subject,String campus,String student_name);

    void deleteArrangement(Integer id,String studio);

    void deleteBBookeDetail(Integer id);

    void deleteGroupBuy(String goods_id,String leader_id);

    void changeClassName(Integer id,String studio,String class_number);

    void modifyGoodsIntro(String id,String studio,String campus,String goods_intro);

    void modifyGoodsName(String id,String studio,String campus,String goods_intro);

    void modifyGoodsGroupNum(String id,String studio,String campus,String goods_intro);
    void modifyGoodsGroupPrice(String id,String studio,String campus,String goods_intro);

    void modifyGoodsCutStep(String id,String studio,String campus,String goods_intro);

    void modifyGoodsSeckillPrice(String id,String studio,String campus,String goods_intro);

    void modifyGoodsExpiredTime(String id,String studio,String campus,String goods_intro);

    void modifyGoodsPrice(String id,String studio,String campus,String goods_intro);

    void modifySignUpMark(String id,String studio,String mark);

    void modifySignUpCount(String id,String studio,Float count);

    void modifyOrderCutPrice(String goods_id,String leader_id,Float cut_price);

    void updateLocation(String openid,String location);

    void updatePhoneNumber(String openid,String phone_number);

    void updateUserStudentName(String openid,String student_name);

    void updateNewName(String openid,String new_name);

    void updateContract(String studio, String campus, String contract);

    void changeSubjectName(Integer id,String studio,String class_number);

    void changeLimit(Integer id,String studio,String limits);

    void changeUpcoming(Integer id,String studio,String upcoming);

    void changeDuration(Integer id,String studio,String duration);

    void changeScheduleClassName(String old_class_number,String studio,String duration,String new_class_number,String subject,String campus,Integer dayofweek);

    void changeScheduleDuration(String class_number,String studio,String duration,String new_duration,String subject,String campus,Integer dayofweek);

    void changeSignUpClassName(String old_class_number,String studio,String duration,String new_class_number,String subject,String campus);

    void changeScheduleSubject(String old_subject,String studio,String duration,String new_subject,String class_number,String campus,Integer dayofweek);

    void changeSignUpSubject(String old_subject,String studio,String duration,String new_subject,String class_number,String campus);

    void deleteStudentPhoto(String student_name,String studio);

    void deleteHome(String studio);

    void deleteGoodsLike(String goods_id,String openid,String delete_status);

    void deleteMyOrder(String id);

    void deleteNote(Integer id);

    void deleteNotice(Integer id);

    void deleteUser(Integer id);

    void deleteGiftList(Integer id);

    void deleteCard(Integer id);

    void deletePointsRecordById(Integer id);

    void deletePointsRecordByStudent(String student_name,String studio,String campus,String subject);

    void deliverMyOrder(String id,Integer status);

    void deleteSignUpRecord(Integer id,String studio);

    void deleteSignUpAllRecord(String student_name,String studio);

    void updateLeaveAllRecord(String student_name,String studio,String campus);

    void deleteLeaveAllRecord(String student_name,String studio,String leave_type,String subject);

    void deleteGiftRecord(Integer id,String studio);

    void deleteLeaveRecord(Integer id,String studio);

    void cancelLeave(String student_name,String studio,String subject,String campus,String date_time,String duration);

    void deleteSchedule(Integer id,String studio);

    void deleteScheduleBySubject(String student_name,String studio,String subject,String campus);

    void confirmSchedule(Integer id,String studio);

    void deleteLesson(Integer id,String studio,String today_time);

    void deleteLessonForever(Integer id,String studio);

    void recoverLesson(Integer id,String studio,String create_time);

    void deleteLessonPackage(Integer id);

    void deleteLessonPackageByName(String student_name,String studio,String subject,String campus);

    void recoverLessonPackageByName(String student_name,String studio,String subject,String campus);

    //插入
    int insertUser(User user);

    int insertRestaurantUser(RestaurantUser restaurantUser);

    int insertBookUser(BookUser book);

    int insertPointsRecord(Points points);
    int updateUser(User user);

    int updateWebsiteUuids(String id,String uuids);

    int updateWebsiteTeacher(String id,String teacher);

    int updateWebsiteCompany(String id,String company);

    int updateOpenid(User user);

    int updateOpenidById(String openid_old,String openid_new);

    int updateAvatar(User user);

    int updateLessonAvatar(String id,String uuid);

    int updateLessonAgeById(String id,String age);

    int updateLessonSchoolById(String id,String school);

    int updateLessonPhoneNumberById(String id,String phone_number);

    int updateLessonUrgePaymentById(String id,Integer urge_payment);

    int updateLessonLocationById(String id,String location);

    int updateLessonBirthdateById(String id,String birthdate);

    int updateLessonSubjectById(String id,String subject);

    int updateLessonStudentNameById(String id,String student_name);

    int updateLessonCampusById(String id,String subject);

    int updateLessonRelatedById(Integer id,String related_id);

    int updateRestaurantAvatar(RestaurantUser restaurantUser);

    int updateBookAvatar(BookUser bookUser);

    int updateBookDetailBookName(String book_name_old,String book_name_new);

    int updateRestaurantNickName(RestaurantUser restaurantUser);

    int updateBookNickName(BookUser bookUser);

    int updateRestaurantLogo(RestaurantUser restaurantUser);

    int updateBookLogo(BookUser bookUser);

    int updateRestaurantOrderStatus(String id,int status);

    int updateRestaurantName(RestaurantUser restaurantUser);

    int updateRestaurantRole(RestaurantUser restaurantUser);

    int updateBookRole(BookUser bookUser);

    int updateBookName(BookUser bookUser);

    int updateBudget(BookUser bookUser);

    int updateRestaurantMenuImage(Menu menu);

    int deleteRestaurantFood(int id);

    int updateUserDelete(User user);

    int updateUserBackUrl(User user);

    int updateClassSendStatus(String id,String send_status);

    int updateClassSendStatusByOpenid(String openid,String send_status);

    int updateClassSendStatusByStudio(String studio,String send_status);

    int updateCommunicateContent(String id,String content);

    int updateCommunicatePhoneNumber(String id,String phone_number);

    int updateCommunicateUuids(String id,String uuids);

    int updateCommunicateStatus(String id,Integer status);

    int updateBossLessons(User user);

    int updateSchedule(Schedule schedule);

    int updateScheduleById(Schedule schedule);

    int updateComment(Message message);

    int updateDetailPhoto(Message message);

    int updateUsertype(User user);

    int updateUserCampus(User user);

    int updateUserContract(String openid,Integer contract);

    int updateUserStudioByOpenid(User user);

    int updateUserStudio(String studio,String new_studio);

    int updateUserCampusByStudio(String studio,String campus);

    int updateLessonPackageStudio(String studio,String new_studio);

    int updateLessonPackageCampusByStudio(String studio,String campus);

    int updateUserUnionid(String openid,String unionid,String app);

    int updateRestaurantUserUnionid(String openid,String unionid);

    int updateBookUserUnionid(String openid,String unionid);

    int updateUserOfficialOpenid(String unionid,String official_openid);

    int updateRestaurantUserOfficialOpenid(String unionid,String official_openid);

    int updateLessonStudio(String studio,String new_studio);

    int updateLessonCampusByStudio(String studio,String campus);

    int updateClassScheduleStudio(String studio,String new_studio);

    int updateClassScheduleCampusByStudio(String studio,String campus);

    int updateCommentStudio(String studio,String new_studio);

    int updateCommentCampusByStudio(String studio,String campus);

    int updateScheduleArrangementStudio(String studio,String new_studio);

    int updateScheduleArrangementCampusByStudio(String studio,String campus);

    int updateSignUpRecordStudio(String studio,String new_studio);

    int updateSignUpRecordCampusByStudio(String studio,String campus);

    int updateLeaveRecordStudio(String studio,String new_studio);

    int updateLeaveRecordCampusByStudio(String studio,String campus);

    int updateLeaveStatus(String id,Integer status);

    int updateCombine(Lesson lesson);

    int updateLessonCampus(String studio,String student_name,String campus);

    int updateLessonLeft(Float left_amount,String studio,String student_name,String campus,String subject);

    int updateLessonTotal(Float left_amount,String studio,String student_name,String campus,String subject);

    int updateLessonMinus(Float minus,String studio,String student_name,String campus,String subject);

    int updateLessonCoins(Float coins,String studio,String student_name,String campus,String subject);

    int updateLessonTotalMoney(Float total_money,String studio,String student_name,String campus,String subject);

    int updateLessonDiscountMoney(Float discount_money,String studio,String student_name,String campus,String subject);

    int updateSignUpCampus(String studio,String student_name,String campus);

    int updateSignUpByBacth(Float count,String studio,String student_name,String subject,String campus);

    int updateSignUpSubject(String studio,String student_name,String campus,String subject,String new_subject);

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

    int updateIsOpen(User user);

    int updateSendTime(User user);


    int updateHours(User user);

    List<User> getUser(String openid);

    List<Card> getCard(String studio,String campus,String student_name,String subject);

    List<CardRecord> getCardRecord(String student_name,String card_id,String studio,String campus,String subject);

    List<RestaurantUser> getRestaurantUser(String openid);

    List<BookUser> getBookUser(String openid);

    List<BookUser> getBookUserByBookName(String book_name);

    List<BookUser> getBookUserById(String id);

    List<BookDetail> getBBookDetail(String openid,String date_time,String book_name);

    List<RestaurantUser> getRestaurantUserAll();

    List<RestaurantUser> getRestaurantUserByShop(String restaurant);

    List<RestaurantOrder> getRestaurantOrderByOpenid(String openid);

    List<RestaurantOrder> getRestaurantOrderByShop(String restaurant);

    List<Menu> getRestaurantCategory(String restaurant);

    List<Menu> getRestaurantMenu(String restaurant);

    List<User> getUserSendTime(String studio);

    List<User> getUserByOpenid(String openid);

    List<User> getUserByUnionid(String unionid);

    List<User> getUserByNoOfficialOpenid();

    List<RestaurantUser> getRestaurantUserByUnionid(String unionid);

    List<User> getUserByChooseLesson(String chooseLesson,String studio);

    List<User> getUserByOpenidIgnore(String openid);

    List<User> getUserByStudent(String student_name,String studio);

    List<User> getUserByStudentOpenid(String student_name,String studio,String openid);

    List<User> getUserByNickStudio(String nick_name,String studio);

    List<User> getUserByNickStudioEq(String nick_name,String studio);

    List<User> getComentStyle(String studio);

    List<User> getUserByNickName(String nickName);

    List<User> getUserByStudio(String studio,String campus);

    List<User> getAllUserByStudio(String studio);

    List<User> getAllUserByStudioByPage(String studio,Integer page_start,Integer page_length);

    List<User> getBossByStudio(String studio);

    List<User> getBossByStudioOnly(String studio);
    List<User> getAllUser();

    List<User> getAllBoss();

    List<User> getUserByRole(String role);

    List<User> getUserByOpenidQr(String openid_qr,Integer page_start,Integer page_length);

    List<User> getUserByOpenidQrAll(Integer page_start,Integer page_length);

    List<User> getUserByOpenidQrLike(String studio);

    List<Website> getWebsite(String studio,String campus);

    List<User> getStudio();

    List<User> getStudioBoss(String role);

    List<User> getUserIsSquare(String role);

    List<Lesson> getSubjectByStudio(String studio,String campus);

    List<Schedule> getClassNumbers(String studio);

    List<Arrangement> getArrangements(String studio,String campus);

    List<Order> getMyOrder(String studio,String openid);

    List<Order> getOrderByGoodsId(String goods_id);

    List<Order> getOrderByGoodsLeader(String goods_id,String leader_id);

    List<Order> getOrderById(String id);

    List<Order> getAllOrder(String studio);

    List<Arrangement> getArrangementsByDate(String studio);

    List<Arrangement> getClassNumber(String studio,Integer dayofweek,String duration);

    List<Message> getCertificateModel(String class_name);

    List<Message> getFrameModel(String studio,Integer page_start,Integer page_length,String class_target,String campus);

    List<Message> getCertificateModelName();

    List<User> getOpenidByNick(String student_name,String studio);

    List<Lesson> getLesson(String studio,String campus);

    List<LessonPackage> getLessonPackageById(Integer id);

    List<LessonPackage> getLessonPackage(String student_name,String studio,String campus,String subject);

    List<LessonPackage> getLessonPackageAll(String studio,String campus);

    List<LessonPackage> getLessonPackageByDuration(String student_name,String studio,String campus,String subject,String start_time,String end_time);

    List<LessonPackage> getLessonPackageByDurationAll(String studio,String campus,String start_time,String end_time);

    List<LessonPackage> getLessonPackageRenew(String studio,String campus,String create_time);

    List<LessonPackage> getLessonPackageByStudent(String student_name,String studio,String campus,String subject);

    List<LessonPackage> getLessonPackageByStudentSubject(String student_name,String studio,String campus,String subject);

    List<LessonPackage> getLessonPackageByStudentCombine(String student_name,String studio,String campus);

    List<LessonPackage> getLessonPackageByStudentSubjectBatch(String student_name,String studio,String campus,String subject);

    List<LessonPackage> getLessonPackageByCampus(String studio,String campus);

    List<LessonPackage> getLessonPackageBySubject(String studio,String campus,String subject);

    List<Lesson> getLessonByPage(String studio,String campus,Integer page_start,Integer page_length);

    List<Lesson> getLessonNew(String studio,String campus,String create_time);

    List<Lesson> getLessonLoss(String studio,String campus,String create_time);

    List<Lesson> getClassNote(String subject,String studio,String student_name);

    List<Announcement> getAnnouncement(String studio,String campus);

    List<Lesson> getLessonBySubject(String studio,String subject,String campus);

    List<Lesson> getLessonByStudio(String studio);

    List<Lesson> getLessonByStudioCampus(String studio,String campus);

    List<Lesson> getLessonBySubjectByPage(String studio,String subject,String campus,Integer page_start,Integer page_length);

    List<Lesson> getTipsDataUrl(String studio,Integer left_amount,String subject,String campus);

    List<Lesson> getTipsDataUrlAll(String studio,Integer left_amount,String campus);

    List<Lesson> getGoneStudent(String studio,String campus);

    AllCount getLessonAllCount(String studio,String campus);

    AllCount getLessonPackageAllCount(String studio,String campus,String month_date);

    AllCount getLessonAllCountNewStudent(String studio,String campus,String month_date);

    AllCount getLessonAllCountLossStudent(String studio,String campus,String month_date);

    AllCount getLessonAllCountBySubject(String studio,String subject,String campus);

    Integer getLessonNeedPayCount(String studio,String campus);

    Integer getLessonNeedPayCountBySubject(String studio,String subject,String campus);

    Integer getLessonOweCount(String studio,String campus);

    Integer getLessonOweCountBySubject(String studio,String subject,String campus);

    Integer getLessonAllCountByDay(String studio,Integer day,String duration,String class_number,String subject,String campus);

    Integer getScheduleRemind(String studio,Integer day,String duration,String class_number,String subject);

    List<Lesson> getCampusByStudio(String studio);

    List<Lesson> getLessonById(Integer id);

    List<AnalyzeCount> getLessonAllCountBySumUp(String studio,String campus,String create_time);

    List<AnalyzeCount> getLessonAllCountBySumUpByStudent(String studio,String campus,String create_time,String student_name);

    List<AnalyzeCount> getLessonAllCountBySumUpBySubject(String studio,String campus,String subject,String create_time,String student_name);

    List<AnalyzeCount> getLessonAllCountBySumUpMonth(String studio,String campus,String create_time);

    List<AnalyzeCount> getLessonAllCountBySumUpMonthByStudent(String studio,String campus,String student_name);

    List<AnalyzeCount> getLessonAllCountBySumUpMonthBySubject(String studio,String campus,String subject,String student_name);

    Integer getLessonAllCountByDayByName(String studio,Integer day,String duration,String class_number,String subject,String student_name,String campus);

    Integer getClassesCountAll(String studio,String campus);

    Integer getClassesCountAllLesson(String studio,String campus);

    Integer getClassesCountBySubject(String studio,String subject,String campus);

    Float getSignUpByMonthAll(String studio,String create_time,String campus);

    Integer getTryByMonthAll(String studio,String create_time,String campus);

    Integer getLeaveByMonthAll(String studio,String create_time,String campus);

    Float getSignUpByMonth(String studio,String subject,String create_time,String campus);

    Integer getTryByMonth(String studio,String subject,String create_time,String campus);

    Integer getLeaveByMonth(String studio,String subject,String create_time,String campus);

    Integer getSignUpByMonthStudent(String studio,String subject,String create_time,String campus,String student_name);

    Float getAllSignUpByStudent(String studio,String subject,String campus,String student_name);

    Float getAllSignUpByStudentCombine(String studio,String campus,String student_name);

    Integer getBookSumByMonth(String openid,String book_name,String create_time);

    List<BookDetail> getBookDetailByMonth(String openid,String book_name,String create_time);

    Integer getClassesCountBySubjectLesson(String studio,String subject,String campus);

    Integer getClassesCountByStudent(String studio,String subject,String campus,String student_name);

    Integer getSignUpCountByDay(String studio,String date,String duration,String class_number,String campus,String subject);

    List<Lesson> getRating(String studio,Integer page_start,Integer page_length,String campus);

    List<Lesson> getRatingBySubject(String studio,Integer page_start,Integer page_end,String subject,String campus);

    List<Lesson> getRatingByName(String studio,String student_name,Integer page_start,Integer page_end);

    List<Lesson> getRatingByNameBySubject(String studio,String student_name,Integer page_start,Integer page_end,String subject);

    List<Lesson> getLessonLikeName(String studio,String student_name,String campus);

    List<Lesson> getLessonLikeNameBySubject(String studio,String student_name,String subject,String campus);

    int insertNote(Note note);

    int insertCard(Card card);

    int insertCardRecord(CardRecord cardRecord);

    int insertRestaurantMenu(Menu menu);

    int insertRestaurantOrder(RestaurantOrder restaurantOrder);

    int insertLessonPackage(LessonPackage lessonPackage);

    int insertLesson(Lesson lesson);

    int insertSignUp(SignUp signUp);

    int insertLeave(Leave leave);

    int insertArrangement(Arrangement arrangement);

    int insertGoodsList(GoodsList goodsList);

    int insertGoodsLike(GoodsLike goodslike);

    int insertGroupBuy(GroupBuy groupBuy);

    int insertGift(Gift gift);

    int insertGiftList(GiftList giftList);

    List<Arrangement> getArrangement(String studio,String dayofweek,String subject,String campus);

    List<Arrangement> getArrangementByDay(String studio,Integer dayofweek,String campus);

    List<GoodsList> getGoodsList(String studio,Integer page_start,Integer page_end,String goods_type);

    List<GoodsLike> getGoodsLike(String goods_id,String openid);

    List<GoodsLike> getGoodsLikeByGoodsId(String goods_id);

    List<GoodsList> getGoodsListById(String id);

    List<GoodsList> getGoodsListSearch(String studio,Integer page_start,Integer page_end,String content,String goods_type);

    List<GroupBuy> getGroupBuy(String studio, String goods_id);

    List<Arrangement> getArrangementAll(String studio,String dayofweek,String campus);

    List<Arrangement> getArrangementById(String studio,Integer id);

    List<Arrangement> getArrangementByDate(String studio,String dayofweek,String class_number,String duration,String subject,String campus);

    List<SignUp> getSignUp(String student_name,String studio,String subject,String campus);

    List<GiftList> getGiftList(String studio,String campus);

    List<GiftList> getGiftListById(String id);

    List<SignUp> getSignUpByPackageId(String student_name,String studio,String subject,String campus,String package_id);

    List<SignUp> getSignUpByAll(String studio,String campus);

    List<SignUp> getSignUpByBacth(String student_name,String studio,String subject,String campus);

    List<Leave> getLeaveRecordByStatus(String student_name,String studio,String subject,String campus);

    List<SignUp> getStudentByTeacher(String studio,String teacher);

    List<SignUp> getStudentByTeacherAll();

    List<SignUp> getStudentByTeacherByDuration(String studio,String teacher,String date_start,String date_end);

    List<SignUp> getStudentByTeacherByDurationByPage(String studio,String teacher,String date_start,String date_end,Integer page_start,Integer page_length);

    List<Message> getCommentByDate(String student_name,String studio,String date_time,String campus,String class_target);

    List<Message> getCommentByDateDuration(String student_name,String studio,String date_time,String duration,String campus,String class_target);

    List<SignUp> getSignUpByDate(String student_name,String studio,String date_time,String campus,String subject);

    List<SignUp> getSignUpByBetween(String student_name,String studio,String campus,String subject,String start_time,String end_time);

    List<SignUp> getSignUpDetailByMonthStudent(String student_name,String studio,String date_time,String campus,String subject);

    List<Schedule> getTryDetailByMonthStudent(String studio,String date_time,String campus);

    List<Leave> getLeaveDetailByMonthStudent(String student_name,String studio,String date_time,String campus,String subject);

    List<SignUp> getSignUpByDateDuration(String student_name,String studio,String date_time,String duration,String campus,String subject);

    List<Gift> getGift(String student_name,String studio);

    List<Gift> getGiftById(String id);

    List<Leave> getLeaveRecord(String student_name,String studio,String leave_type,String subject,String campus);

    List<Leave> getLeaveRecordById(String id);

    List<Leave> getLeaveRecordAll(String student_name,String studio,String leave_type,String subject,String campus);

    List<Leave> getLeaveByDateDuration(String student_name,String studio,String date_time,String duration);

    int updateLesson(Lesson lesson);

    int consumeLesson(Lesson lesson);

    int updateSignUpTeacher(String teacher,String id);

    int updateLessonBoth(Lesson lesson);

    int updateLessonSubject(String subject_new,String student_name,String studio, String subject,String campus);

    int updateCoinsAll(Float coins,String studio,String campus);

    int updateLessonPointStatus(String studio,String campus,Integer point_status);

    int updateFinalTime(String studio,String campus,String final_time);

    int updateLeaveTimes(String studio,String campus,String leave_times);

    int updateCoinsByStudent(Float coins,String studio,String campus,String student_name,String subject);

    int updatePriceAll(Float coins,String studio,String campus);

    int updatePriceByStudent(Float coins,String studio,String campus,String student_name,String subject);

    int updateGift(String id,Integer status);

    int updateCoinsByStudio(String studio,Float coins);

    int updateCoinsByUser(String openid,Float coins);

    int updateReadTimesByOpenid(String openid,Float read_times);

    int updateVideoViewsById(String id,Integer views);

    int updateScheduleName(String student_name_new,String student_name,String studio,String campus,String subject);

    int updateRemind(Integer remind,String subject,String studio,String duration,String class_number,String dayofweek,String campus);

    int updateCommentName(String student_name_new,String student_name,String studio,String campus,String subject);

    int updateGiftRecordName(String student_name_new,String student_name,String studio,String campus,String subject);

    int updateLessonName(String student_name_new,String student_name,String studio,String campus,String subject);

    int updateSignUpRecordName(String student_name_new,String student_name,String studio,String campus,String subject);

    int updateLessonPackageName(String student_name_new,String student_name,String studio,String campus,String subject);

    int updateLessonPackageByStudent(Float total_money,Float discount_money,Float all_lesson,Float give_lesson,String student_name,String studio,String campus,String subject);

    int updateUserStudent(String student_name_new,String student_name,String studio,String campus,String subject);

    int updateUserStudentByOpenid(String student_name,String openid,String id);

    int updateUserExpired(String role,String studio,String role_old,String campus);

    int updateUserExpiredTimeAd(String openid,String expired_time_ad);

    int updateUserMember(String member,String studio);

    int updateUserRegion(String region,String studio);

    int updateExchangeByStudio(String studio,Integer is_exchange);

    int updateUserIsPaid(String openid,Integer is_paid,String cash_uuid);

    int updateTeacherByUser(String openid,Integer is_teacher);

    int updateSquareByUser(String openid,Integer is_square);

    int updateCityByStudio(String studio,String city);

    int updateSubjectByStudio(String studio,String subject);

    List<Lesson> getLessonByName(String student_name,String studio,String campus);

    List<Lesson> getLessonByNameSubject(String student_name,String studio,String subject,String campus);

    List<Lesson> getLessonByNameSubjectAll(String student_name,String studio,String subject,String campus);

    int updateLessonPoint(Lesson lesson);

    int updateLessonPackageTotalMoney(String id,String total_money);

    int updateLessonPackageDiscountMoney(String id,String discount_money);

    int updateLessonPackageEndStatus(String id,int end_status);

    int updateLessonPackageMark(String id,String mark);

    int updateLessonPackageAllLesson(String id,Float all_lesson);

    int updateLessonPackageGiveLesson(String id,Float give_lesson);

    int updateLessonPackageStartDate(String id,String start_date);

    int updateLessonPackageEndDate(String id,String end_date);





}