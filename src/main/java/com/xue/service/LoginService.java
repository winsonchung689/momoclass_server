package com.xue.service;

import java.util.List;
import java.util.Map;

import com.xue.entity.model.*;

public interface LoginService {

    public List  getMessage(String studio,Integer page,String comment_style,String openid,String role,String class_target,String campus);

    public List  getCommentModel(String openid,String date_time,String duration,String class_number);

    public List  getPost(String studio,Integer page,String openid,String type);

    public List  getPostComment(String post_id);

    public List  getLeaveMessage(String studio,String type);

    public String  changeClass(String studio,Integer changeday,String duration,String class_number,Integer weekday,String subject,String campus);

    public List  getGrowthRecord(String studio,Integer page,String student_name);

    public List  getUserByOpenid(String openid);

    public List  getNewUser(String openid);

    public List  getContract(String openid);

    public List  getMessageClient(String nickName);

    public List  getModel(String studio,Integer page,String campus);

    public List  getOnlineTeacher(String type,Integer page,String openid,String id);

    public List  getCommunicateRecord(String studio,Integer page,String campus);

    public List  getCommunicateLike(String studio,String item,String campus);

    public List  getMamaShare(Integer page);

    public List  getLesson(String studio,String student_name,String subject,String campus);

    public List  getLessonHead(String studio,String student_name,String subject,String campus,String month_date);

    public List  getLessonByStudio(String studio);

    public List  getLessonByStudioCampus(String studio,String campus);

    public List  getAllUserByStudio(String studio);

    public List  getUserByRole(String role);

    public List  getUserByOpenidQr(String openid_qr,Integer page);

    public List  getUserByOpenidQrAll();

    public List  getAllUserByStudioByPage(String studio,Integer page);

    public List  getUserByNickStudio(String nick_name,String studio);

    public List  getAnalyzeDetail(String studio,String dimension,String campus,String date_time,String duration_time);

    public List  getAnalyzeDetailWeek(String studio,String type,String weekday,String campus,String subject);

    public List  getLessonByPage(String studio,String student_name,String subject,String openid,Integer page);

    public List  getTipsDataUrl(String studio,Integer left_amount,String subject,String campus,String type,String month_date);

    public List  getBookDetailByMonth(String openid,String book_name,String date_time);

    public List  getGoneStudent(String studio,String campus);

    public List  getAbnormalStudent(String studio,String campus,String type);

    public List  getLessonInName(String studio,String student_name,Integer page,String subject,String openid);

    public List  getGoodsList(String studio,Integer page,String campus,String content,String type,String goods_type,String openid);

    public List  getGoodsListById(String goods_id);

    public List  getSubGoods(String goods_id,String goods_type);

    public List  getGroupBuy(String studio,String goods_id);

    public List  getRating(String studio,String student_name,Integer page,String subject,String openid);

    public int push(Message message);

    public int insertLesson(Lesson lesson);

    public int insertOrder(Order order);

    public int sendOrderNotice(String openid,String goods_name);

	public int updateLesson(Lesson lesson,Float lessons_amount,Float consume_lesson_amount,String subject_new,String compus);

    public int updateLessonRelated(String id,Integer related_id,String openid,String type);

    public List  getDetails(Integer id);

    public List  getDetailsUrlByDate(String studio,String duration,String student_name,String date_time,String openid,String class_target);

    public List  getCommentLikeStudent(String openid,String content,String duration);

    public List  getSignUp(String student_name,String studio,String subject,String openid);

    public List  getSignUpByBetween(String student_name,String subject,String openid,String duration_time);

    public List  getCardRecordByBetween(String student_name,String card_id,String subject,String openid,String duration_time);

    public List  getGiftList(String studio,String campus);

    public List  getGiftListByCouponType(String studio,String campus,Integer coupon_type);

    public List  getGiftByGiftId(String gift_id,String studio,String campus);

    public List  getSignUpByAll(String studio,String openid,String duration,String type,String student_name,String subject);

    public List  getLessonPackageByAll(String studio,String openid);

    public List  getLessonPackageByStudent(String student_name,String openid,String subject);

    public List  getSignUpByDateDuration(String student_name,String studio,String date_time,String duration,String campus,String subject);

    public List  getGift(String student_name,String openid,Integer coupon_type);

    public List  getLeaveRecord(String student_name,String studio,String leave_type,String subject,String campus);

    public List  getArrangement(String studio,Integer dayofweek,String date,String subject,String openid,String student_name);

    public List  getTodaySchedule(String studio,Integer dayofweek,String date,String subject,String openid);

    public List  getClassStudent(String studio,String campus,String type,String subject,String date_time);

    public int insertSchedule(Schedule schedule);

    public int insertSignUp(SignUp signUp);

    public int insertArrangement(Arrangement arrangement);

    public int insertGift(Gift gift);

    public int insertGoodsList(GoodsList goodsList);

    public List  getSchedule(String date_time,String studio,String subject,String openid,String test);

    public List  getTodayClasses(String date_time,String studio,String openid);

    public List  getClassByDate(String date_time,String studio,String subject,String openid);

    public List  getScheduleByClass(String date_time,String duration,String studio,String class_number,String subject,String openid);

    public List  getScheduleByClassRepeat(String date_time,Integer dayofweek,String duration,String class_number,String subject,String openid);

    public List  getScheduleDetail(Integer weekDay,String duration,String studio,String class_number,String subject,String campus);

    public int  deleteComment(Integer id,String role,String studio,String openid);

    public int  deleteUuids(Integer id,String role,String studio,String openid,String uuid);

    public int  deleteGoodsList(Integer id,String role,String studio,String openid);

    public int  deleteArrangement(Integer id,String role,String studio,String openid);

    public int  changeClassName(String id, String openid,String content,String type);

    public int  deleteSignUpRecord(Integer id,String role,String studio,String openid);

    public int  updateSignUpEnding(String student_name,String openid,String id,String ending_status,String create_time);

    public int  deleteGiftRecord(Integer id,String role,String studio,String openid);

    public int  deleteSchedule(Integer id,String role,String studio,String openid);

    public int  deleteScheduleByDate(Integer weekDay,String duration,String studio,String class_number,String role,String openid,String subject);

    public int  confirmSchedule(Integer id,String role,String studio,String openid);

    public int  deleteLesson(String id,String role,String studio,String openid,String student_name);

    public int insertBookUser(BookUser bookUser);

    public int updateUser(User user);

    public int updateBossLessons(User user);

    public int updateSchedule(Schedule schedule);

    public int updateDetailPhoto(Message message);

    public int updateUsertype(User user);

    public List getUser(String openid);

    public List getCardByStudent(String studio,String campus,String student_name);

    public List getCard(String studio,String campus,String student_name,String subject);

    public List getCardRecord(String openid,String student_name,String card_id,String subject);

    public List getPptMenu(String openid,Integer page,String category,String type);

    public List getPptMenuById(String id);

    public List getBookUser(String openid);

    public List getBBookDetail(String openid,String duration,String book_name);

    public List getUserByNickName(String nickName);

    public List getUserByStudio(String Studio,String campus);

    public List getStudio(String role);

    public List getClassNumbers(String studio);

    public List getArrangements(String studio,String campus);

    public List getArrangementsByRepeat(String studio,String campus);

    public List getArrangementsByDate(String studio,String date_time,String campus);

    public List getCertificateModel(String class_name);

    public List getAllOrderByType(String openid,String type,Integer page);

    public List getOrderByStudioLike(String openid,String content);

    public List getOrderByGoodsId(String goods_id,String type);

    public List getFrameModel(String studio,Integer page,String class_target,String campus);

    public List getCertificateModelName();

    public List getOpenidByNick(String student_name,String studio);

    public List getUuidByTarget(String class_target,String openid);

    public List getBook(String studio,String dimension,String campus);

    public List getAccountBookDetail(String studio,String create_time,String type,String start_date);

    public List searchBookDetail(String studio,String value,String type);

    public List getCertificate(String studio,String student_name);

    public List getPaycode(String student_name);

    public List getClassSys(String class_target,String studio,Integer page,String campus);

    public List getAlbum(String student_name,String openid,Integer page,String type);

    public List getWebsite(String studio, String campus);

    public List getExhibition(String openid, String type,Integer page);

    public List getExhibitionRank(String openid, String type);

    public List getCourseList(String studio,Integer page);

    public List getCourseDetail(String studio,String class_name,Integer page);

    public List getHome(String studio,String campus);

    public int updateMinusLesson(String student_name,String studio,Float class_count,String subject,String campus);

    public int syncUpdateMinusLesson(String student_name,String studio,Float class_count,String subject,String campus);

    public List  getLessonByName(String student_name,String studio,String campus);

    public List  getLessonPackage(String student_name,String studio,String subject,String search_type,String duration_time,String openid);

    public List  getLessonByNameSubject(String student_name,String studio,String subject,String campus);

    public int updateAddPoints(String student_name,String studio,Integer points,String subject,String campus,String mark,String type);

    public int updateLessonPackage(String id,String content,String type);

    public int updateCoinsByStudio(String studio,String openid,Float number,String type);

    public void sendClassRemind();

    public void sendBossPayRemind();

    public String sendFeedback(String openid,String target_studio,String expired_time,String days,String mark);

    public String getToken(String app);

    public List getStudentByTeacher(String type,String openid,String duration_time,Integer page,String class_number);

    public String getOpenid(String code,String app);

    public String getOpenidOfficial();

    public String updateOpenidOfficialByOpenid(String openid,String unionid);

    public String updateCoinsLevel();

    public String renewLessonRemind(String student_name,String studio,String campus,String subject,Float lesson_amount);

    public String leaveRemind(String official_openid_boss,String student_name, String studio, String subject, String duration,String date_time,String mark);

    public int deleteLessonPackage(Integer id,String type);

    public String downloadByOpenid(String studio,String openid,List<String> result_list,String title,String file_name);

    public List getStandings(String openid,String student_name,String subject,Integer page);

    public List getPointsRecordByMonth(String type,String openid,String student_name,String subject,String month);

    public String classRemind(String official_openid_boss, String student_name, String studio, String subject,String class_number, String duration, String date_time, String upcoming,String id,String now_date);


}
