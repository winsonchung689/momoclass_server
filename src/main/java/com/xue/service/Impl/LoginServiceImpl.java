package com.xue.service.Impl;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.xue.config.Constants;
import com.xue.config.TokenCache;
import com.xue.entity.model.*;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;
import com.xue.service.WebPushService;
import com.xue.util.HttpUtil;
import nl.martijndwars.webpush.Subscription;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.type.IntegerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class LoginServiceImpl implements LoginService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserMapper dao;

    @Autowired
    private WebPushService webPushService;

    @Override
    public int push(Message message) {
        int result = 0;
        try {
            result = dao.push(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

	@Override
    public int insertLesson(Lesson lesson) {
        int result = 0;
        try {
            result = dao.insertLesson(lesson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int insertOrder(Order order) {
        int result = 0;
        try {
            result = dao.insertOrder(order);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateLesson(Lesson lesson,Float lessons_amount,Float consume_lesson_amount,String subject_new,String campus) {
        int result = 0;
        try {
            String student_name = lesson.getStudent_name();
            Float total = lesson.getTotal_amount();
            Float left = lesson.getLeft_amount();
            Float minus = lesson.getMinus();
            Float coins = lesson.getCoins();
            String studio = lesson.getStudio();
            String subject = lesson.getSubject();
            Float total_amount = 0.0f;
            Float minus_amount = 0.0f;
            Float left_amount = 0.0f;
            Float coins_amount = 0.0f;
            Integer is_combine = 0;
            if (student_name != null) {
                List<Lesson> lessons = dao.getLessonByNameSubject(student_name, studio,subject,campus);
                    if(lessons.size()>0){
                        Lesson lesson_get = lessons.get(0);
                        total_amount = lesson_get.getTotal_amount();
                        if (total > 0) {
                            total_amount = total;
                        }
                        left_amount = lesson_get.getLeft_amount();
                        if (left > 0) {
                            left_amount = left;
                        }
                        left_amount = left_amount - consume_lesson_amount;

                        minus_amount = lesson_get.getMinus();
                        if (minus > 0) {
                            minus_amount = minus;
                        }
                        coins_amount = lesson_get.getCoins();
                        if (coins > 0) {
                            coins_amount = coins;
                        }

                        is_combine = lesson_get.getIs_combine();
                    }
            }
            lesson.setStudent_name(student_name);
            lesson.setTotal_amount(total_amount + lessons_amount);
            lesson.setLeft_amount(left_amount + lessons_amount);
            lesson.setMinus(minus_amount);
            lesson.setCoins(coins_amount);
            lesson.setSubject(subject);
            lesson.setCampus(campus);
            if("全科目".equals(subject_new)){
                if(is_combine == 0){
                    result = dao.updateLesson(lesson);
                }else if (is_combine == 1){
                    result = dao.updateLessonBoth(lesson);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List getDetails(Integer id) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String student_name = null;
        String class_name = null;
        String class_target = null;
		String studio = null;
		String duration = null;
		Integer positive = 0 ;
        Integer discipline = 0;
        Integer happiness = 0;
        String mp3_url=null;
        String uuids=null;
        String uuids_c=null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getDetails(id);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                class_name = line.getClass_name();
                comment = line.getComment();
                class_target = line.getClass_target();
				studio = line.getStudio();
                duration = line.getDuration();
                positive = line.getPositive();
                discipline = line.getDiscipline();
                happiness = line.getHappiness();
                mp3_url=line.getMp3_url();
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                try {
                    uuids_c = line.getUuids_c().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                photo = line.getPhoto();
                if(uuids != null){
                    photo = null;

                }

                jsonObject.put("isHide",true);
                try {
                    List<User> user = dao.getUserByStudent(student_name,studio);
                    if (user.size()>0){
                        jsonObject.put("isHide",false);
                    }
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }

                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("photo", photo);
                jsonObject.put("class_target", class_target);
                jsonObject.put("id", id);
                jsonObject.put("duration", duration);
                jsonObject.put("positive", positive);
                jsonObject.put("discipline", discipline);
                jsonObject.put("happiness", happiness);
                jsonObject.put("mp3_url", mp3_url);
                jsonObject.put("uuids", uuids);
                jsonObject.put("uuids_c", uuids_c);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getDetailsUrlByDate(String studio, String duration, String student_name, String date_time,String openid) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String class_name = null;
        String class_target = null;
        String id = null;
        Integer positive = 0 ;
        Integer discipline = 0;
        Integer happiness = 0;
        String mp3_url = null;
        String uuids = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<User> list_user = dao.getUser(openid);
            String campus = list_user.get(0).getCampus();
            List<Message> list = dao.getDetailsUrlByDate(studio,duration,student_name,date_time,campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                class_name = line.getClass_name();
                comment = line.getComment();
                photo = line.getPhoto();
                class_target = line.getClass_target();
                studio = line.getStudio();
                duration = line.getDuration();
                positive = line.getPositive();
                discipline = line.getDiscipline();
                happiness = line.getHappiness();
                id = line.getId();
                mp3_url=line.getMp3_url();
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                photo = line.getPhoto();
                if(uuids != null){
                    photo = null;

                }

                jsonObject.put("isHide",true);
                try {
                    List<User> user = dao.getUserByStudent(student_name,studio);
                    if (user.size()>0){
                        jsonObject.put("isHide",false);
                    }
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }

                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("photo", photo);
                jsonObject.put("class_target", class_target);
                jsonObject.put("id", id);
                jsonObject.put("duration", duration);
                jsonObject.put("positive", positive);
                jsonObject.put("discipline", discipline);
                jsonObject.put("happiness", happiness);
                jsonObject.put("mp3_url", mp3_url);
                jsonObject.put("uuids", uuids);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getSearch(String student_name, String studio,Integer page,String class_target,String campus) {
        String comment = null;
        String class_name = null;
        String id = null;
        String create_time = null;
        Integer page_start = (page - 1) * 7;
        Integer page_length = 7;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getSearch(student_name, studio,page_start,page_length,class_target,campus);
            for (int i = 0; i < list.size(); i++) {
                Float percent = 0.0f;
                Float left = 0.0f;
                Float total = 0.0f;
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                class_name = line.getClass_name();
                comment = line.getComment();
                class_target = line.getClass_target();
                id = line.getId();
                create_time = line.getCreate_time();

                try {
                    List<Lesson> lessons = dao.getLessonByName(student_name, studio,campus);
                    Lesson lesson = lessons.get(0);
                    left = lesson.getLeft_amount();
                    total = lesson.getTotal_amount();
                    if (left > 0 || total > 0) {
                        percent = left * 100 / total;
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }


                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("class_target", class_target);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("percent", percent);
                jsonObject.put("left", left);
                jsonObject.put("total",total);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getSignUp(String student_name, String studio,String subject) {
        String create_time = null;
        String sign_time = null;
        String id = null;
        String mark = null;
        String duration = null;
        Float count = 0.0f;
        Integer ending_status = 0;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<SignUp> list = dao.getSignUp(student_name, studio,subject);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                SignUp line = list.get(i);
                //获取字段
                create_time = line.getCreate_time();
                sign_time = line.getSign_time();
                id = line.getId();
                mark = line.getMark();
                duration = line.getDuration();
                count = line.getCount();
                ending_status = line.getEnding_status();
                if (ending_status == 0) {
                    jsonObject.put("ending_status", "未结");
                } else if(ending_status == 1){
                    jsonObject.put("ending_status", "已结");
                }

                SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
                Date create_time_dt = df1.parse(create_time.substring(0,10));
                Date sign_time_dt = df1.parse(sign_time.substring(0,10));
                int compare = sign_time_dt.compareTo(create_time_dt);
                if (compare == 0) {
                    jsonObject.put("status", "正常签");
                } else if(compare > 0){
                    jsonObject.put("status", "补签");
                } else if(compare < 0){
                    jsonObject.put("status", "提前签");
                }

                //json
                jsonObject.put("id", id);
                jsonObject.put("student_name", student_name);
                jsonObject.put("create_time", create_time.substring(0,10));
                jsonObject.put("sign_time", sign_time.substring(0,10));
                jsonObject.put("rank", i + 1);
                jsonObject.put("mark", mark);
                jsonObject.put("duration", duration);
                jsonObject.put("count", count);
                jsonObject.put("subject", subject);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getSignUpByDateDuration(String student_name,String studio,String date_time,String duration,String campus) {
        String id = null;
        String openid = null;
        String subscription = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<SignUp> list = dao.getSignUpByDateDuration(student_name,studio,date_time,duration,campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                SignUp line = list.get(i);
                //获取字段
                id = line.getId();
                try {
                    List<User> users = dao.getUserByStudent(student_name,studio);
                    if(users.size()>0){
                        openid = users.get(0).getOpenid();
                        subscription = users.get(0).getSubscription();
                    }
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }

                jsonObject.put("id",id);
                jsonObject.put("openid",openid);
                jsonObject.put("subscription",subscription);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getGift(String student_name, String studio) {
        String create_time = null;
        String expired_time = null;
        String gift_name = null;
        Integer gift_amount = null;
        Integer status=null;
        String id = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Gift> list = dao.getGift(student_name, studio);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Gift line = list.get(i);
                //获取字段
                create_time = line.getCreate_time();
                expired_time = line.getExpired_time();
                gift_name = line.getGift_name();
                gift_amount = line.getGift_amount();
                status = line.getStatus();
                id = line.getId();

                SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//
                String now_time = df1.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

                Date now_time_dt = df1.parse(now_time);
                Date expired_time_dt = df1.parse(expired_time);
                int compare = now_time_dt.compareTo(expired_time_dt);
                if (status==0){
                    if (compare > 0) {
                        jsonObject.put("status", "已过期");
                    } else {
                        jsonObject.put("status", "未过期");
                    }
                } else if (status==1) {
                    jsonObject.put("status", "已领取");
                }


                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("create_time", create_time.substring(0,10));
                jsonObject.put("expired_time", expired_time.substring(0,10));
                jsonObject.put("gift_name", gift_name);
                jsonObject.put("gift_amount", gift_amount);
                jsonObject.put("rank", i + 1);
                jsonObject.put("id",id);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getLeaveRecord(String student_name, String studio,String leave_type,String subject) {
        String create_time = null;
        String date_time = null;
        String duration=null;
        String id = null;
        String mark_leave = null;
        String student_get = null;
        String makeup_date = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Leave> list = dao.getLeaveRecord(student_name, studio,leave_type,subject);
            if("all".equals(student_name)){
                list = dao.getLeaveRecordAll(student_name, studio,leave_type,subject);
            }
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Leave line = list.get(i);
                //获取字段
                create_time = line.getCreate_time();
                date_time = line.getDate_time();
                duration = line.getDuration();
                id = line.getId();
                mark_leave = line.getMark_leave();
                student_get = line.getStudent_name();
                makeup_date = line.getMakeup_date();
                Integer ending_status = line.getEnding_status();

                jsonObject.put("ending_status","未扣");
                if(ending_status == 1){
                    jsonObject.put("ending_status","已扣");
                }
                jsonObject.put("student_name", student_get);
                jsonObject.put("create_time", create_time);
                jsonObject.put("date_time", date_time);
                jsonObject.put("duration", duration);
                jsonObject.put("rank", i + 1);
                jsonObject.put("id",id);
                jsonObject.put("mark_leave",mark_leave);
                jsonObject.put("subject",subject);
                jsonObject.put("makeup_date",makeup_date);

                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getArrangement(String studio,Integer dayofweek,String date,String subject,String openid,String student_name_in) {
        String class_number = null;
        String duration = null;
        String limits = "0";
        byte[] photo = null;
        String id = null;
        Integer dayofweek_by= 0;
        List<JSONObject> resul_list = new ArrayList<>();
        Integer class_res =0;
        Integer sign_count =0;
        Integer classes_count_all =0;
        Integer classes_count_all_lesson =0;
        Integer search_res = 0;

        if(dayofweek==7){
            dayofweek_by=1;
        }else {
            dayofweek_by = dayofweek + 1;
        }

        List<User> user_get= dao.getUser(openid);
        String role = user_get.get(0).getRole();
        Integer user_get_size = user_get.size();

        String campus = user_get.get(0).getCampus();

        try {
            List<Arrangement> list =null;
            if(subject.equals("全科目")){
                list = dao.getArrangementAll(studio,dayofweek.toString(),campus);
                classes_count_all=dao.getClassesCountAll(studio,campus);
                classes_count_all_lesson = dao.getClassesCountAllLesson(studio,campus);
            }else {
                list = dao.getArrangement(studio,dayofweek.toString(),subject,campus);
                classes_count_all=dao.getClassesCountBySubject(studio,subject,campus);
                classes_count_all_lesson = dao.getClassesCountBySubjectLesson(studio,subject,campus);
            }

            for (int i = 0; i < list.size(); i++) {
                String student_string = null;
                Integer classes_count =0;
                Integer uncomfirmed_count = 0;
                Integer remind = 0;
                String remind_name="否";
                JSONObject jsonObject = new JSONObject();
                Arrangement line = list.get(i);
                //获取字段
                class_number = line.getClass_number();
                duration = line.getDuration();
                limits = line.getLimits();
                photo = line.getPhoto();
                id = line.getId();
                subject = line.getSubject();

                if("client".equals(role)){
                    for(int j = 0; j < user_get_size;j++){
                        String student_name = user_get.get(j).getStudent_name();
                        student_string = student_string + "," + student_name;
                        class_res = dao.getLessonAllCountByDayByName(studio,dayofweek_by,duration,class_number,subject,student_name,campus);
                        uncomfirmed_count = dao.getLessonAllCountByDayUnconfirmed(studio,dayofweek_by,duration,class_number,subject,campus);
                        if(class_res > 0){
                             classes_count = dao.getLessonAllCountByDay(studio,dayofweek_by,duration,class_number,subject,campus);
                            if(date != null){
                                sign_count = dao.getSignUpCountByDay(studio,date+" 00:00:00",duration,class_number,campus);
                            }
                        }
                    }

                }else {
                    remind = dao.getScheduleRemind(studio,dayofweek_by,duration,class_number,subject);
                    if(remind == null){
                        remind = 0;
                    }
                    if(remind == 1 ){
                        remind_name = "是";
                    }
                    classes_count = dao.getLessonAllCountByDay(studio,dayofweek_by,duration,class_number,subject,campus);
                    uncomfirmed_count = dao.getLessonAllCountByDayUnconfirmed(studio,dayofweek_by,duration,class_number,subject,campus);
                    if(date != null){
                        sign_count = dao.getSignUpCountByDay(studio,date+" 00:00:00",duration,class_number,campus);
                    }
                }

                jsonObject.put("chooseLesson","未选");
                try {
                    String lessons = user_get.get(0).getLessons();
                    String[] list_1 =lessons.split("\\|");
                    String lesson_string = "星期" + dayofweek + "," + subject + "," + class_number + "," + duration;
                    List<String> list_2 = Arrays.asList(list_1);
                    if(list_2.contains(lesson_string)){
                        jsonObject.put("chooseLesson","已选");
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }

                if(!"all".equals(student_name_in)){
                    search_res = dao.getLessonAllCountByDayByName(studio,dayofweek_by,duration,class_number,subject,student_name_in,campus);
                    if(search_res>0){
                        jsonObject.put("class_number", class_number);
                        jsonObject.put("duration", duration);
                        jsonObject.put("limits", limits);
                        jsonObject.put("photo", photo);
                        jsonObject.put("classes_count", classes_count);
                        jsonObject.put("dayofweek",dayofweek);
                        jsonObject.put("id",id);
                        jsonObject.put("sign_count",sign_count);
                        jsonObject.put("subject",subject);
                        jsonObject.put("classes_count_all",classes_count_all);
                        jsonObject.put("classes_count_all_lesson",classes_count_all_lesson);
                        jsonObject.put("uncomfirmed_count",uncomfirmed_count);
                        jsonObject.put("student_string",student_string);
                        jsonObject.put("remind",remind);
                        jsonObject.put("remind_name",remind_name);
                        resul_list.add(jsonObject);
                    }
                }else {
                    jsonObject.put("class_number", class_number);
                    jsonObject.put("duration", duration);
                    jsonObject.put("limits", limits);
                    jsonObject.put("photo", photo);
                    jsonObject.put("classes_count", classes_count);
                    jsonObject.put("dayofweek",dayofweek);
                    jsonObject.put("id",id);
                    jsonObject.put("sign_count",sign_count);
                    jsonObject.put("subject",subject);
                    jsonObject.put("classes_count_all",classes_count_all);
                    jsonObject.put("classes_count_all_lesson",classes_count_all_lesson);
                    jsonObject.put("uncomfirmed_count",uncomfirmed_count);
                    jsonObject.put("student_string",student_string);
                    jsonObject.put("remind",remind);
                    jsonObject.put("remind_name",remind_name);
                    resul_list.add(jsonObject);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getTodaySchedule(String studio, Integer dayofweek, String date, String subject, String openid) {
        String class_number = null;
        String duration = null;
        String limits = "0";
        String id = null;
        Integer dayofweek_by= 0;
        List<JSONObject> resul_list = new ArrayList<>();
        Integer sign_count =0;
        Integer classes_count_all =0;
        Integer classes_count_all_lesson =0;

        if(dayofweek==7){
            dayofweek_by=1;
        }else {
            dayofweek_by = dayofweek + 1;
        }

        List<User> user_get= dao.getUser(openid);
        String role = user_get.get(0).getRole();
        Integer user_get_size = user_get.size();
        String campus = user_get.get(0).getCampus();

        try {
            List<Arrangement> list =null;
            if(subject.equals("全科目")){
                list = dao.getArrangementAll(studio,dayofweek.toString(),campus);
                classes_count_all=dao.getClassesCountAll(studio,campus);
                classes_count_all_lesson = dao.getClassesCountAllLesson(studio,campus);
            }else {
                list = dao.getArrangement(studio,dayofweek.toString(),subject,campus);
                classes_count_all=dao.getClassesCountBySubject(studio,subject,campus);
                classes_count_all_lesson = dao.getClassesCountBySubjectLesson(studio,subject,campus);
            }

            for (int i = 0; i < list.size(); i++) {
                String student_string = null;
                Integer classes_count =0;
                Integer uncomfirmed_count = 0;
                Integer remind = 0;
                JSONObject jsonObject = new JSONObject();
                Arrangement line = list.get(i);
                //获取字段
                class_number = line.getClass_number();
                duration = line.getDuration();
                limits = line.getLimits();
                id = line.getId();
                subject = line.getSubject();

                if("client".equals(role)){
                    Integer class_res =0;
                    for(int j = 0; j < user_get_size;j++){
                        String student_name = user_get.get(j).getStudent_name();
                        student_string = student_string + "," + student_name;
                        class_res = dao.getLessonAllCountByDayByName(studio,dayofweek_by,duration,class_number,subject,student_name,campus);
                        uncomfirmed_count = dao.getLessonAllCountByDayUnconfirmed(studio,dayofweek_by,duration,class_number,subject,campus);
                        if(class_res > 0){
                            classes_count = dao.getLessonAllCountByDay(studio,dayofweek_by,duration,class_number,subject,campus);
                            if(date != null){
                                sign_count = dao.getSignUpCountByDay(studio,date+" 00:00:00",duration,class_number,campus);
                            }
                        }
                    }

                    if(class_res > 0){
                        jsonObject.put("class_number", class_number);
                        jsonObject.put("duration", duration);
                        jsonObject.put("limits", limits);
                        jsonObject.put("classes_count", classes_count);
                        jsonObject.put("dayofweek",dayofweek);
                        jsonObject.put("id",id);
                        jsonObject.put("sign_count",sign_count);
                        jsonObject.put("subject",subject);
                        jsonObject.put("classes_count_all",classes_count_all);
                        jsonObject.put("classes_count_all_lesson",classes_count_all_lesson);
                        jsonObject.put("uncomfirmed_count",uncomfirmed_count);
                        jsonObject.put("student_string",student_string);
                        jsonObject.put("remind",remind);
                        resul_list.add(jsonObject);
                    }

                }else if("boss".equals(role) || "teacher".equals(role)) {
                    classes_count = dao.getLessonAllCountByDay(studio,dayofweek_by,duration,class_number,subject,campus);
                    uncomfirmed_count = dao.getLessonAllCountByDayUnconfirmed(studio,dayofweek_by,duration,class_number,subject,campus);
                    if(date != null){
                        sign_count = dao.getSignUpCountByDay(studio,date+" 00:00:00",duration,class_number,campus);
                    }
                    jsonObject.put("chooseLesson","未选");
                    try {
                        String lessons = user_get.get(0).getLessons();
                        String[] list_1 =lessons.split("\\|");
                        String lesson_string = "星期" + dayofweek + "," + subject + "," + class_number + "," + duration;
                        List<String> list_2 = Arrays.asList(list_1);
                        if(list_2.contains(lesson_string)){
                            jsonObject.put("chooseLesson","已选");
                            jsonObject.put("class_number", class_number);
                            jsonObject.put("duration", duration);
                            jsonObject.put("limits", limits);
                            jsonObject.put("classes_count", classes_count);
                            jsonObject.put("dayofweek",dayofweek);
                            jsonObject.put("id",id);
                            jsonObject.put("sign_count",sign_count);
                            jsonObject.put("subject",subject);
                            jsonObject.put("classes_count_all",classes_count_all);
                            jsonObject.put("classes_count_all_lesson",classes_count_all_lesson);
                            jsonObject.put("uncomfirmed_count",uncomfirmed_count);
                            jsonObject.put("student_string",student_string);
                            jsonObject.put("remind",remind);
                            resul_list.add(jsonObject);
                        }
                    } catch (Exception e) {
//                    e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public int insertSchedule(Schedule schedule) {
        int result = 0;
        FileInputStream in = null;
        try {
            result = dao.insertSchedule(schedule);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int insertSignUp(SignUp signUp) {
        int result = 0;
        FileInputStream in = null;
        try {
            result = dao.insertSignUp(signUp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int insertArrangement(Arrangement arrangement) {
        int result = 0;
        FileInputStream in = null;
        try {
            result = dao.insertArrangement(arrangement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int insertGift(Gift gift) {
        int result = 0;
        FileInputStream in = null;
        try {
            result = dao.insertGift(gift);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int insertGoodsList(GoodsList goodsList) {
        int result = 0;
        try {
            result = dao.insertGoodsList(goodsList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List getSchedule(String date_time, String studio,String subject,String openid,String test) {
        String add_date = null;
        String age = null;
        String student_name = null;
        String duration = null;
        String create_time = null;
        String id = null;
        String update_time = null;
        Float left = 0.0f;
        Float total = 0.0f;
        List<JSONObject> resul_list = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat fmt_m = new SimpleDateFormat("yyyy-MM");
        Date d = null;
        String class_number = null;
        Integer weekDay=0;
        Integer weekofday=0;
        String mark = null;
        Integer sign_counts=0;
        Integer sign_counts_get=0;
        List<Schedule> list_tra=null;
        Integer remind=0;
        List<User> list_user = dao.getUser(openid);
        String campus = list_user.get(0).getCampus();


        if(subject.equals("全科目")){
            sign_counts_get = dao.getSignUpByMonthAll(studio, date_time.substring(0,7),campus);
        }else {
            sign_counts_get = dao.getSignUpByMonth(studio, subject,date_time.substring(0,7),campus);
        }
        JSONObject jsonObject_1 = new JSONObject();
        if(sign_counts_get!=null){
            sign_counts=sign_counts_get;
        }
        jsonObject_1.put("sign_counts", sign_counts);
        resul_list.add(jsonObject_1);

        // 获取常规学生
        try {
            d = fmt.parse(date_time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            weekDay = cal.get(Calendar.DAY_OF_WEEK);

            List<Schedule> list=null;
            if(subject.equals("全科目")){
                list = dao.getScheduleAll(weekDay, studio,campus);
                list_tra = dao.getTransferAll(date_time, studio,campus);
            }else {
                list = dao.getSchedule(weekDay, studio,subject,campus);
                list_tra = dao.getTransfer(date_time, studio,subject,campus);
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Schedule line = list.get(i);
                //获取字段
                add_date = line.getAdd_date();
                age = line.getAge();
                student_name = line.getStudent_name();
                duration = line.getDuration();
                id = line.getId();
                create_time = line.getCreate_time();
                update_time = line.getUpdate_time();
                class_number = line.getClass_number();
                subject = line.getSubject();
                remind = line.getRemind();
                String role = "visit";
                String lesson_string = null;
                List<String> list_2 = null;
                Integer contains = 0;
                try {
                    if(openid != null){
                        User user_get= dao.getUser(openid).get(0);
                        String lessons_string = user_get.getLessons();
                        role = user_get.getRole();
                        String[] list_1 =lessons_string.split("\\|");
                        if(weekDay == 1){
                            weekofday = 7 ;
                        }else {
                            weekofday = weekDay - 1;
                        }
                        lesson_string = "星期" + weekofday + "," + subject + "," + class_number + "," + duration;
                        list_2 = Arrays.asList(list_1);
                        if(list_2.contains(lesson_string)){
                            contains = 1;
                        }
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
                if( contains == 1 || "1".equals(test) || "client".equals(role)) {
                    jsonObject.put("subject", subject);
                    jsonObject.put("class_number", class_number);
                    jsonObject.put("comment_status", "课评");
                    jsonObject.put("comment_color", "rgb(157, 162, 165)");
                    List<Message> messages = dao.getCommentByDate(student_name, studio, date_time,campus);
                    if (messages.size() >= 1) {
                        if (messages.get(0).getDuration().equals("00:00-00:00")) {
                            jsonObject.put("comment_status", "已课评");
                            jsonObject.put("comment_color", "rgba(162, 106, 214, 0.849)");
                        } else {
                            List<Message> messagesDuration = dao.getCommentByDateDuration(student_name, studio, date_time, duration,campus);
                            if (messagesDuration.size() == 1) {
                                jsonObject.put("comment_status", "已课评");
                                jsonObject.put("comment_color", "rgba(162, 106, 214, 0.849)");
                            }
                        }
                    }

                    //json
                    List<Lesson> lessons = dao.getLessonByName(student_name, studio,campus);
                    if (lessons.size() > 0) {
                        Lesson lesson = lessons.get(0);
                        left = lesson.getLeft_amount();
                        total = lesson.getTotal_amount();
                        jsonObject.put("left", left);
                        jsonObject.put("total", total);
                        jsonObject.put("add_date", add_date);
                        jsonObject.put("age", age);
                        jsonObject.put("student_name", student_name);
                        jsonObject.put("duration", duration);
                        jsonObject.put("create_time", create_time.substring(0, 10));
                        jsonObject.put("id", id);
                        jsonObject.put("update_time", update_time.substring(0, 10));
                        jsonObject.put("leave_color", "rgb(157, 162, 165)");
                        jsonObject.put("sign_color", "rgb(157, 162, 165)");
                        jsonObject.put("remind",remind);

                        jsonObject.put("sign_up", "签到");
                        jsonObject.put("mark", "备注");
                        List<SignUp> signUps = dao.getSignUpByDate(student_name, studio, date_time + " 00:00:00",campus);
                        if (signUps.size() >= 1) {
                            if (signUps.get(0).getDuration().equals("00:00-00:00")) {
                                jsonObject.put("sign_up", "已签到");
                                jsonObject.put("sign_color", "rgba(55, 188, 221, 0.849)");
                                mark = signUps.get(0).getMark();
                                jsonObject.put("mark", mark);

                            } else {
                                List<SignUp> signUpsDuration = dao.getSignUpByDateDuration(student_name, studio, date_time + " 00:00:00", duration,campus);
                                if (signUpsDuration.size() == 1) {
                                    jsonObject.put("sign_up", "已签到");
                                    jsonObject.put("sign_color", "rgba(55, 188, 221, 0.849)");
                                    mark = signUpsDuration.get(0).getMark();
                                    jsonObject.put("mark", mark);
                                }
                            }
                        }

                        jsonObject.put("leave", "缺席");
                        List<Leave> leaves = dao.getLeaveByDateDuration(student_name, studio, date_time, duration);
                        if (leaves.size() == 1) {
                            String leave_type = leaves.get(0).getLeave_type();
                            jsonObject.put("leave", "已请假");
                            if (leave_type.equals("旷课")) {
                                jsonObject.put("leave", "已旷课");
                            }
                            jsonObject.put("leave_color", "rgb(218, 144, 84)");
                        }

                        resul_list.add(jsonObject);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 获取插班生
        try {
            for (int i = 0; i < list_tra.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Schedule line = list_tra.get(i);
                //获取字段
                add_date = line.getAdd_date();
                age = line.getAge();
                student_name = line.getStudent_name();
                duration = line.getDuration();
                id = line.getId();
                create_time = line.getCreate_time();
                update_time = line.getUpdate_time();
                class_number = line.getClass_number();
                subject = line.getSubject();
                jsonObject.put("subject", subject);
                jsonObject.put("class_number","无班号(插班生)");
                if(class_number.length()>0){
                    jsonObject.put("class_number", class_number+"(插班生)");
                }

                jsonObject.put("comment_status", "课评");
                jsonObject.put("comment_color", "rgb(157, 162, 165)");
                List<Message> messages = dao.getCommentByDate(student_name,studio,date_time,campus);
                if (messages.size()>=1){
                    if(messages.get(0).getDuration().equals("00:00-00:00")){
                        jsonObject.put("comment_status", "已课评");
                        jsonObject.put("comment_color", "rgba(162, 106, 214, 0.849)");
                    }else {
                        List<Message> messagesDuration = dao.getCommentByDateDuration(student_name, studio, date_time, duration,campus);
                        if (messagesDuration.size() == 1) {
                            jsonObject.put("comment_status", "已课评");
                            jsonObject.put("comment_color", "rgba(162, 106, 214, 0.849)");
                        }
                    }
                }

                //json
                List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                if(lessons.size()>0){
                    Lesson lesson = lessons.get(0);
                    left = lesson.getLeft_amount();
                    total = lesson.getTotal_amount();
                    jsonObject.put("left", left);
                    jsonObject.put("total", total);
                    jsonObject.put("add_date", add_date);
                    jsonObject.put("age", age);
                    jsonObject.put("student_name", student_name);
                    jsonObject.put("duration", duration);
                    jsonObject.put("create_time", create_time.substring(0,10));
                    jsonObject.put("id", id);
                    jsonObject.put("update_time", update_time.substring(0,10));
                    jsonObject.put("leave_color", "rgb(157, 162, 165)");
                    jsonObject.put("sign_color", "rgb(157, 162, 165)");

                    jsonObject.put("sign_up", "签到");
                    jsonObject.put("mark", "备注");
                    List<SignUp> signUps = dao.getSignUpByDate(student_name,studio,date_time + " 00:00:00",campus);
                    if(signUps.size()>=1){
                        if(signUps.get(0).getDuration().equals("00:00-00:00")){
                            jsonObject.put("sign_up", "已签到");
                            jsonObject.put("sign_color", "rgba(55, 188, 221, 0.849)");
                            mark = signUps.get(0).getMark();
                            jsonObject.put("mark", mark);

                        }else {
                            List<SignUp> signUpsDuration = dao.getSignUpByDateDuration(student_name,studio,date_time+" 00:00:00",duration,campus);
                            if(signUpsDuration.size()==1){
                                jsonObject.put("sign_up", "已签到");
                                jsonObject.put("sign_color", "rgba(55, 188, 221, 0.849)");
                                mark = signUpsDuration.get(0).getMark();
                                jsonObject.put("mark", mark);
                            }
                        }
                    }

                    jsonObject.put("leave", "缺席");
                    List<Leave> leaves = dao.getLeaveByDateDuration(student_name,studio,date_time,duration);
                    if(leaves.size()==1){
                        String leave_type = leaves.get(0).getLeave_type();
                        jsonObject.put("leave", "已请假");
                        if (leave_type.equals("旷课")){
                            jsonObject.put("leave", "已旷课");
                        }
                        jsonObject.put("leave_color", "rgb(218, 144, 84)");
                    }

                }else {
                    jsonObject.put("left", 0);
                    jsonObject.put("total", 0);
                    jsonObject.put("add_date", add_date);
                    jsonObject.put("age", age);
                    jsonObject.put("student_name", student_name);
                    jsonObject.put("duration", duration);
                    jsonObject.put("create_time", create_time.substring(0,10));
                    jsonObject.put("id", id);
                    jsonObject.put("update_time", update_time.substring(0,10));
                    jsonObject.put("leave_color", "rgb(157, 162, 165)");
                    jsonObject.put("sign_color", "rgb(157, 162, 165)");
                    jsonObject.put("sign_up", "试听生");
                    jsonObject.put("mark", "试听生");
                    jsonObject.put("leave", "试听生");
                }
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getClassByDate(String date_time, String studio, String subject, String openid, String test) {
        String duration = null;
        List<JSONObject> resul_list = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date d = null;
        String class_number = null;
        Integer weekDay=0;
        Integer weekofday=0;
        Integer sign_counts=0;
        Integer sign_counts_get=0;
        Integer remind=0;
        String campus = null;
        JSONObject jsonObject_1 = new JSONObject();
        try {
            List<User> list_user = dao.getUser(openid);
            if(list_user.size()>0){
                campus = list_user.get(0).getCampus();
            }

            if(subject.equals("全科目")){
                sign_counts_get = dao.getSignUpByMonthAll(studio, date_time.substring(0,7),campus);
            }else {
                sign_counts_get = dao.getSignUpByMonth(studio, subject,date_time.substring(0,7),campus);
            }

            if(sign_counts_get!=null){
                sign_counts = sign_counts_get;
            }
            jsonObject_1.put("sign_counts", sign_counts);
            resul_list.add(jsonObject_1);

            d = fmt.parse(date_time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            weekDay = cal.get(Calendar.DAY_OF_WEEK);

            List<Schedule> list=null;
            try {
                if(subject.equals("全科目")){
                    list = dao.getScheduleAllDistinct(weekDay, studio,campus);
                }else {
                    list = dao.getScheduleDistinct(weekDay, studio,subject,campus);
                }
            } catch (Exception e) {
//                throw new RuntimeException(e);
            }

            if(list.size()>0){
                for (int i = 0; i < list.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    Schedule line = list.get(i);
                    //获取字段
                    studio = line.getStudio();
                    duration = line.getDuration();
                    class_number = line.getClass_number();
                    subject = line.getSubject();
                    remind = line.getRemind();
                    String lesson_string = null;
                    List<String> list_2 = null;
                    Integer contains = 0;

                    try {
                        if(openid != null){
                            User user_get= dao.getUser(openid).get(0);
                            String lessons_string = user_get.getLessons();
                            String[] list_1 =lessons_string.split("\\|");
                            if(weekDay == 1){
                                weekofday = 7 ;
                            }else {
                                weekofday = weekDay - 1;
                            }
                            lesson_string = "星期" + weekofday + "," + subject + "," + class_number + "," + duration;
                            list_2 = Arrays.asList(list_1);
                            if(list_2.contains(lesson_string)){
                                contains = 1;
                            }
                        }
                    } catch (Exception e) {
//                        e.printStackTrace();
                    }
                    if(contains == 1 || "1".equals(test)){
                        jsonObject.put("studio", studio);
                        jsonObject.put("duration", duration);
                        jsonObject.put("class_number", class_number);
                        jsonObject.put("subject", subject);
                        jsonObject.put("remind",remind);
                        resul_list.add(jsonObject);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getScheduleByClass(String date_time,String duration,String studio,String class_number,String subject,String openid) {
        String add_date = null;
        String age = null;
        String student_name = null;
        String create_time = null;
        String id = null;
        String update_time = null;
        Float left = 0.0f;
        Float total = 0.0f;
        List<JSONObject> resul_list = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date d = null;
        Integer weekDay=0;
        Integer weekofday=0;
        String mark = null;
        List<Schedule> list_tra=null;
        Integer remind=0;
        String student_type=null;
        List<User> list_user = dao.getUser(openid);
        String campus = list_user.get(0).getCampus();


        // 获取常规学生
        try {
            d = fmt.parse(date_time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            weekDay = cal.get(Calendar.DAY_OF_WEEK);

            List<Schedule> list=null;
            list = dao.getScheduleByClassOrdinary(weekDay,duration,studio,class_number,subject,campus);
            list_tra = dao.getScheduleByClassTransferred(date_time,duration,studio,class_number,subject,campus);
            list.addAll(list_tra);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Schedule line = list.get(i);
                //获取字段
                add_date = line.getAdd_date();
                age = line.getAge();
                student_name = line.getStudent_name();
                duration = line.getDuration();
                id = line.getId();
                create_time = line.getCreate_time();
                update_time = line.getUpdate_time();
                class_number = line.getClass_number();
                student_type = line.getStudent_type();
                subject = line.getSubject();
                remind = line.getRemind();
                String role = "visit";
                String lesson_string = null;
                List<String> list_2 = null;
                Integer contains = 0;

                jsonObject.put("student_type", student_type);
                jsonObject.put("subject", subject);
                if("transferred".equals(student_type)){
                    class_number = class_number+"(插班生)";
                }
                jsonObject.put("class_number", class_number);

                jsonObject.put("comment_status", "课评");
                jsonObject.put("comment_color", "rgb(157, 162, 165)");
                List<Message> messages = dao.getCommentByDate(student_name, studio, date_time,campus);
                if (messages.size() >= 1) {
                    if (messages.get(0).getDuration().equals("00:00-00:00")) {
                        jsonObject.put("comment_status", "已课评");
                        jsonObject.put("comment_color", "rgba(162, 106, 214, 0.849)");
                    } else {
                        List<Message> messagesDuration = dao.getCommentByDateDuration(student_name, studio, date_time, duration,campus);
                        if (messagesDuration.size() == 1) {
                            jsonObject.put("comment_status", "已课评");
                            jsonObject.put("comment_color", "rgba(162, 106, 214, 0.849)");
                        }
                    }
                }

                //json
                List<Lesson> lessons = dao.getLessonByName(student_name, studio,campus);
                if (lessons.size() > 0) {
                    Lesson lesson = lessons.get(0);
                    left = lesson.getLeft_amount();
                    total = lesson.getTotal_amount();
                    String final_time = lesson.getFinal_time();
                    Float leave_times = lesson.getLeave_times();

                    jsonObject.put("left", left);
                    jsonObject.put("total", total);
                    jsonObject.put("add_date", add_date);
                    jsonObject.put("age", age);
                    jsonObject.put("student_name", student_name);
                    jsonObject.put("duration", duration);
                    jsonObject.put("create_time", create_time.substring(0, 10));
                    jsonObject.put("id", id);
                    jsonObject.put("update_time", update_time.substring(0, 10));
                    jsonObject.put("leave_color", "rgb(157, 162, 165)");
                    jsonObject.put("sign_color", "rgb(157, 162, 165)");
                    jsonObject.put("remind",remind);
                    jsonObject.put("final_time",final_time);
                    jsonObject.put("leave_times",leave_times);

                    jsonObject.put("sign_up", "签到");
                    jsonObject.put("mark", "备注");
                    List<SignUp> signUps = dao.getSignUpByDate(student_name, studio, date_time + " 00:00:00",campus);
                    if (signUps.size() >= 1) {
                        if (signUps.get(0).getDuration().equals("00:00-00:00")) {
                            jsonObject.put("sign_up", "已签到");
                            jsonObject.put("sign_color", "rgba(55, 188, 221, 0.849)");
                            mark = signUps.get(0).getMark();
                            jsonObject.put("mark", mark);

                        } else {
                            List<SignUp> signUpsDuration = dao.getSignUpByDateDuration(student_name, studio, date_time + " 00:00:00", duration,campus);
                            if (signUpsDuration.size() == 1) {
                                jsonObject.put("sign_up", "已签到");
                                jsonObject.put("sign_color", "rgba(55, 188, 221, 0.849)");
                                mark = signUpsDuration.get(0).getMark();
                                jsonObject.put("mark", mark);
                            }else if(signUpsDuration.size() > 1){
                                jsonObject.put("sign_up", "重复签到");
                                jsonObject.put("sign_color", "rgba(55, 188, 221, 0.849)");
                                mark = signUpsDuration.get(0).getMark();
                                jsonObject.put("mark", mark);
                            }
                        }
                    }

                    jsonObject.put("leave", "缺席");
                    List<Leave> leaves = dao.getLeaveByDateDuration(student_name, studio, date_time, duration);
                    if (leaves.size() == 1) {
                        String leave_type = leaves.get(0).getLeave_type();
                        jsonObject.put("leave", "已请假");
                        if (leave_type.equals("旷课")) {
                            jsonObject.put("leave", "已旷课");
                        }
                        jsonObject.put("leave_color", "rgb(218, 144, 84)");
                    }

                }else{
                    jsonObject.put("left", 0);
                    jsonObject.put("total", 0);
                    jsonObject.put("add_date", add_date);
                    jsonObject.put("age", age);
                    jsonObject.put("student_name", student_name);
                    jsonObject.put("duration", duration);
                    jsonObject.put("create_time", create_time.substring(0,10));
                    jsonObject.put("id", id);
                    jsonObject.put("update_time", update_time.substring(0,10));
                    jsonObject.put("leave_color", "rgb(157, 162, 165)");
                    jsonObject.put("sign_color", "rgb(157, 162, 165)");
                    jsonObject.put("sign_up", "试听生");
                    jsonObject.put("mark", "试听生");
                    jsonObject.put("leave", "试听生");
                    jsonObject.put("final_time","无");
                    jsonObject.put("leave_times","无");
                }
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getScheduleDetail(Integer weekDay, String duration, String studio,String class_number,String subject,String campus) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");//设置日期格式
        String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
        String age = null;
        String id = null;
        List<JSONObject> resul_list = new ArrayList<>();
        Integer status = 0;
        String status_str = "待确认";

        // 获取常规学生
        try {
            List<Schedule> list = dao.getScheduleDetail(weekDay,duration,studio,class_number,subject,campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Schedule line = list.get(i);

                //获取字段
                age = line.getAge();
                String student_name = line.getStudent_name();
                Integer student_count = dao.getSignUpByMonthStudent(studio,subject,create_time,campus,student_name);
                Integer student_classes = dao.getClassesCountByStudent(studio,subject,campus,student_name);
                duration = line.getDuration();
                id = line.getId();
                status = line.getStatus();
                if(status == 1){
                    status_str = "已确认";
                }else {
                    status_str = "待确认";
                }

                if(student_classes == null){
                    student_classes = 0;
                }

                if(student_count == null){
                    student_count = 0;
                }

                jsonObject.put("age", age);
                jsonObject.put("student_name", student_name);
                jsonObject.put("duration", duration);
                jsonObject.put("id", id);
                jsonObject.put("status", status);
                jsonObject.put("status_str", status_str);
                jsonObject.put("student_count", student_count);
                jsonObject.put("student_classes", student_classes*4);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return resul_list;
    }

    @Override
    public int deleteComment(Integer id, String role,String studio,String openid) {
        try {
            List<User> list = dao.getUser(openid);
            String studio_get = list.get(0).getStudio();

            if ( studio_get.equals(studio)) {
                dao.deleteComment(id,studio);
            }else {
                logger.error("it's not your studio, could not delete!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int deleteUuids(Integer id, String role,String studio,String openid,String uuid) {
        try {
            List<Message> list = dao.getUuidById(studio,id);
            String class_target_bak = list.get(0).getClass_target_bak();
            if("课评".equals(class_target_bak) || "环境".equals(class_target_bak) || "课程体系".equals(class_target_bak) || "广告".equals(class_target_bak) || "兼职".equals(class_target_bak)){
                String uuids = list.get(0).getUuids().replace("\"","").replace("[","").replace("]","");
                String studio_get = list.get(0).getStudio();
                String[] result = uuids.split(",");
                List<String> list_new = new ArrayList<>();
                for(int i =0;i<result.length;i++){
                    if(!result[i].equals(uuid)){
                        list_new.add(result[i]);
                    }
                }

                if (studio_get.equals(studio)) {
                    dao.updateUuids(id,studio,list_new.toString().replace(" ",""));
                }else {
                    logger.error("it's not your studio, could not delete!");
                }
            }else if ("课后作业".equals(class_target_bak)){
                String uuids = list.get(0).getUuids_c().replace("\"","").replace("[","").replace("]","");
                String studio_get = list.get(0).getStudio();
                String[] result = uuids.split(",");
                List<String> list_new = new ArrayList<>();
                for(int i =0;i<result.length;i++){
                    if(!result[i].equals(uuid)){
                        list_new.add(result[i]);
                    }
                }

                if ( studio_get.equals(studio)) {
                    dao.updateUuids_c(id,studio,list_new.toString().replace(" ",""));
                }else {
                    logger.error("it's not your studio, could not delete!");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int deleteGoodsList(Integer id, String role, String studio, String openid) {
        try {
            List<User> list = dao.getUser(openid);
            String studio_get = list.get(0).getStudio();

            if ( studio_get.equals(studio)) {
                dao.deleteGoodsList(id,studio);
            }else {
                logger.error("it's not your studio, could not delete!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int deleteArrangement(Integer id,String role,String studio,String openid) {
        try {
            List<User> list = dao.getUser(openid);
            String studio_get = list.get(0).getStudio();
            if ( studio_get.equals(studio)) {
                dao.deleteArrangement(id,studio);
            }else {
                logger.error("it's not your studio, could not delete!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int changeClassName(String id, String role, String studio, String openid,String class_number,String change_title,String limit_number,String campus) {
        try {
            Integer id1 = Integer.parseInt(id);
            List<User> list = dao.getUser(openid);
            String studio_get = list.get(0).getStudio();
            List<Arrangement> list_1 = dao.getArrangementById(studio,id1);
            Arrangement arrangement = list_1.get(0);
            String duration = arrangement.getDuration();
            String old_class_number = arrangement.getClass_number();
            String old_subject = arrangement.getSubject();


            if ( studio_get.equals(studio)) {
                if(change_title.equals("班号")){
                    dao.changeClassName(id1,studio,class_number);
                    dao.changeScheduleClassName(old_class_number,studio,duration,class_number,old_subject,campus);
                    dao.changeSignUpClassName(old_class_number,studio,duration,class_number,old_subject,campus);
                }else if(change_title.equals("科目")){
                    dao.changeSubjectName(id1,studio,class_number);
                    dao.changeScheduleSubject(old_subject,studio,duration,class_number,old_class_number,campus);
                    dao.changeSignUpSubject(old_subject,studio,duration,class_number,old_class_number,campus);
                }else if(change_title.equals("上限")){
                    dao.changeLimit(id1,studio,limit_number);
                }else if(change_title.equals("时间")){
                    dao.changeDuration(id1,studio,limit_number);
                    dao.changeScheduleDuration(old_class_number,studio,duration,limit_number,old_subject,campus);
                }

            }else {
                logger.error("it's not your studio, could not delete!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int deleteSignUpRecord(Integer id, String role,String studio,String openid) {
        try {
            List<User> list = dao.getUser(openid);
            String studio_get = list.get(0).getStudio();

            if ( studio_get.equals(studio)) {
                dao.deleteSignUpRecord(id,studio);
            }else {
                logger.error("it's not your studio, could not delete!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int deleteSignUpAllRecord(String name, String role, String studio,String openid) {
        try {
            List<User> list = dao.getUser(openid);
            String studio_get = list.get(0).getStudio();

            if ( studio_get.equals(studio)) {
                dao.deleteSignUpAllRecord(name,studio);
            }else {
                logger.error("it's not your studio, could not delete!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int deleteGiftRecord(Integer id, String role,String studio,String openid) {
        try {
            List<User> list = dao.getUser(openid);
            String studio_get = list.get(0).getStudio();

            if ( studio_get.equals(studio)) {
                dao.deleteGiftRecord(id,studio);
            }else {
                logger.error("it's not your studio, could not delete!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int deleteSchedule(Integer id, String role,String studio,String openid) {
        try {
            List<User> list = dao.getUser(openid);
            String studio_get = list.get(0).getStudio();

            if ( studio_get.equals(studio)) {
                dao.deleteSchedule(id,studio);
            }else {
                logger.error("it's not your studio, could not delete!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int deleteScheduleByDate(Integer weekDay,String duration,String studio,String class_number,String role,String openid,String subject) {
        try {
            List<User> list = dao.getUser(openid);
            String studio_get = list.get(0).getStudio();
            Integer weekofday=0;
            if(weekDay==7){
                weekofday=1;
            }else {
                weekofday = weekDay + 1;
            }
            if ( studio_get.equals(studio)) {
                dao.deleteScheduleByDate(weekofday,duration,studio,class_number,subject);
            }else {
                logger.error("it's not your studio, could not delete!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int confirmSchedule(Integer id, String role, String studio, String openid) {
        try {
            List<User> list = dao.getUser(openid);
            String studio_get = list.get(0).getStudio();

            if ( studio_get.equals(studio)) {
                dao.confirmSchedule(id,studio);
            }else {
                logger.error("it's not your studio, could not delete!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int deleteLesson(Integer id, String role,String studio,String openid,String student_name) {
        try {
            List<User> list = dao.getUser(openid);
            String studio_get = list.get(0).getStudio();

            if ( studio_get.equals(studio)) {
                dao.deleteLesson(id,studio);
                dao.deleteScheduleByLesson(student_name,studio);
            }else {
                logger.error("it's not your studio, could not delete!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int insertUser(User user) {
        int result = 0;
        try {
            result = dao.insertUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateUser(User user) {
        int result = 0;

        try {
            result = dao.updateUser(user);
            String openid = user.getOpenid();
            String nick_name = user.getNick_name();
            String studio = user.getStudio();
            String md5 = DigestUtils.md5Hex(nick_name + studio);
            if(result == 0 && !openid.equals(md5)){
                result = dao.updateOpenid(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateBossLessons(User user) {
        int result = 0;

        try {
            result = dao.updateBossLessons(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateComentStyle(User user) {
        int result = 0;

        try {
            result = dao.updateComentStyle(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateSchedule(Schedule schedule) {
        int result = 0;

        try {
            result = dao.updateSchedule(schedule);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateComment(Message message) {
        int result = 0;

        try {
            result = dao.updateComment(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateDetailPhoto(Message message) {
        int result = 0;

        try {
            result = dao.updateDetailPhoto(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateUsertype(User user) {
        int result = 0;

        try {
            result = dao.updateUsertype(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List getUser(String openid) {
        String role = null;
        String student_name = null;
        String avatarurl = null;
        String nick_name = null;
        String studio = null;
        String user_type = null;
        String create_time = null;
        String expired_time = null;
        Integer coins = 0;
        String comment_style = null;
        String openid_get = null;
        String theme = null;
        String phone_number = null;
        String location = null;
        Integer display = null;
        Integer cover = null;
        String subjects = null;
        String member = null;
        List<User> list= new ArrayList<>();;
        String send_time = null;
        String subscription = null;
        String campus =null;
        List<Lesson> list_lesson= new ArrayList<>();
        List<JSONObject> resul_list = new ArrayList<>();
        long pay_days = 0;
        try {
            if(openid.equals("all")){
                list = dao.getAllUser();
                for (int i = 0; i < list.size(); i++) {
                    String role_get = list.get(i).getRole();
                    if(role_get.equals("boss")){
                        String expird_time_get = list.get(i).getExpired_time();
                        String studio_get = list.get(i).getStudio();
                        String campus_get = list.get(i).getCampus();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
                        String today_time = df.format(new Date());
                        Date today_dt = df.parse(today_time.substring(0,10));
                        Date expired_dt = df.parse(expird_time_get.substring(0,10));
                        int compare_all = today_dt.compareTo(expired_dt);
                        if(compare_all > 0){
                            dao.updateUserExpired("client",studio_get,role_get,campus_get);
                        }
                    }
                }
            }else {
                list = dao.getUser(openid);
                if(list.size()>0){
                    String role_get = list.get(0).getRole();
                    String expird_time_get = list.get(0).getExpired_time();
                    String studio_get = list.get(0).getStudio();
                    String campus_get = list.get(0).getCampus();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
                    String today_time = df.format(new Date());
                    Date today_dt = df.parse(today_time.substring(0,10));
                    Date expired_dt = df.parse(expird_time_get.substring(0,10));
                    int compare = today_dt.compareTo(expired_dt);
                    if(role_get.equals("boss") && compare > 0){
                        dao.updateUserExpired("client",studio_get,role_get,campus_get);
                    }
                    long diff = expired_dt.getTime() - today_dt.getTime();
                    pay_days = diff / (24*60*60*1000);
                }
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                User line = list.get(i);
                //获取字段
                role = line.getRole();
                student_name = line.getStudent_name();
                avatarurl = line.getAvatarurl();
                nick_name = line.getNick_name();
                studio = line.getStudio();
                user_type = line.getUser_type();
                create_time = line.getCreate_time();
                expired_time = line.getExpired_time();
                coins = line.getCoins();
                openid_get = line.getOpenid();
                comment_style = line.getComment_style();
                theme = line.getTheme();
                display = line.getDisplay();
                cover = line.getCover();
                phone_number=line.getPhone_number();
                location = line.getLocation();
                subjects = line.getSubjects();
                send_time = line.getSend_time();
                subscription = line.getSubscription();
                member = line.getMember();
                campus = line.getCampus();

                if(!openid.equals("all")){
                    list_lesson = dao.getLessonByName(student_name,studio,campus);
                }

                //json
                jsonObject.put("role", role);
                if(!openid.equals("all") && student_name.equals("no_name") && role.equals("client")){
                    jsonObject.put("role", "visit");
                }
                if(!openid.equals("all") && !student_name.equals("no_name") && role.equals("client") && list_lesson.size()==0){
                    jsonObject.put("role", "visit");
                }
                jsonObject.put("student_name", student_name);
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("studio", studio);

                jsonObject.put("pay_type", "试用");
                jsonObject.put("all_days", 0);
                jsonObject.put("amount", 0);
                if(!openid.equals("all")) {
                    List<Book> books = dao.getBookByStudio(studio);
                    if(books.size()>0){
                        String mark = books.get(0).getMark();
                        Float amount = books.get(0).getAmount();
                        jsonObject.put("amount", amount);
                        String[] mark_list = mark.split("_");
                        if(mark_list.length == 3){
                            String pay_type = mark_list[1];
                            jsonObject.put("pay_type", pay_type);

                            String all_days = mark_list[2];
                            jsonObject.put("all_days", all_days);
                        }
                    }
                }

                jsonObject.put("user_type", user_type);
                jsonObject.put("create_time", create_time);
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("coins", coins);
                jsonObject.put("openid",openid_get);
                jsonObject.put("comment_style",comment_style);
                jsonObject.put("theme",theme);
                jsonObject.put("display",display);
                jsonObject.put("cover",cover);
                jsonObject.put("phone_number",phone_number);
                jsonObject.put("location",location);
                jsonObject.put("subjects",subjects);
                jsonObject.put("send_time",send_time);
                jsonObject.put("subscription",subscription);
                jsonObject.put("member",member);
                jsonObject.put("campus",campus);
                jsonObject.put("show", false);
                jsonObject.put("name", nick_name);
                jsonObject.put("search", nick_name);
                jsonObject.put("pay_days", pay_days);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getUserByNickName(String nickName) {
        String role = null;
        String student_name = null;
        String avatarurl = null;
        String nick_name = null;
        String studio = null;
        String user_type = null;
        String create_time = null;
        String expired_time = null;
        Integer coins = 0;
        String comment_style = null;
        String openid = null;
        String subjects = null;
        List<User> list= null;
        String id = null;
        String member = null;
        String campus = null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getUserByNickName(nickName);

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                User line = list.get(i);
                //获取字段
                role = line.getRole();
                student_name = line.getStudent_name();
                avatarurl = line.getAvatarurl();
                nick_name = line.getNick_name();
                studio = line.getStudio();
                user_type = line.getUser_type();
                create_time = line.getCreate_time();
                expired_time = line.getExpired_time();
                coins = line.getCoins();
                comment_style =line.getComment_style();
                openid = line.getOpenid();
                subjects = line.getSubjects();
                id = line.getId();
                member = line.getMember();
                campus = line.getCampus();

                //json
                jsonObject.put("id", id);
                jsonObject.put("role", role);
                jsonObject.put("student_name", student_name);
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("studio", studio);
                jsonObject.put("user_type", user_type);
                jsonObject.put("create_time", create_time);
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("coins", coins);
                jsonObject.put("comment_style",comment_style);
                jsonObject.put("openid",openid);
                jsonObject.put("subjects",subjects);
                jsonObject.put("member",member);
                jsonObject.put("campus",campus);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getUserByStudio(String studio,String campus) {
        String role = null;
        String student_name = null;
        String avatarurl = null;
        String nick_name = null;
        String user_type = null;
        String create_time = null;
        String expired_time = null;
        Integer coins = 0;
        String comment_style = null;
        String openid = null;
        String subjects = null;
        List<User> list= null;
        String id = null;
        Integer days=0;
        String subscription = null;
        String member = null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getUserByStudio(studio,campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                User line = list.get(i);
                //获取字段
                role = line.getRole();
                student_name = line.getStudent_name();
                avatarurl = line.getAvatarurl();
                nick_name = line.getNick_name();
                studio = line.getStudio();
                user_type = line.getUser_type();
                create_time = line.getCreate_time();
                expired_time = line.getExpired_time();
                coins = line.getCoins();
                comment_style =line.getComment_style();
                openid = line.getOpenid();
                subjects = line.getSubjects();
                id = line.getId();
                days = line.getDays();
                subscription = line.getSubscription();
                member = line.getMember();


                //json
                jsonObject.put("id", id);
                jsonObject.put("days", days);
                jsonObject.put("role", role);
                jsonObject.put("student_name", student_name);
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("studio", studio);
                jsonObject.put("user_type", user_type);
                jsonObject.put("create_time", create_time);
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("coins", coins);
                jsonObject.put("comment_style",comment_style);
                jsonObject.put("openid",openid);
                jsonObject.put("subjects",subjects);
                jsonObject.put("subscription",subscription);
                jsonObject.put("member",member);
                jsonObject.put("show", false);
                jsonObject.put("name", nick_name);
                jsonObject.put("search", nick_name);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getStudio(String role) {
        String studio = null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {

            List<User> list = null;
            if(role.equals("boss") || role.endsWith("会员")){
                list = dao.getStudioBoss(role);
            }else{
                list = dao.getStudio();
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                User line = list.get(i);
                //获取字段
                studio = line.getStudio();

                //json
                jsonObject.put("studio", studio);
                jsonObject.put("show", false);
                jsonObject.put("name", studio);
                jsonObject.put("search", studio);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getClassNumbers(String studio) {
        List<JSONObject> resul_list = new ArrayList<>();
        String class_number = null;
        try {

            List<Schedule> list = dao.getClassNumbers(studio);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Schedule line = list.get(i);
                //获取字段
                class_number = line.getClass_number();

                //json
                jsonObject.put("class_number", class_number);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getArrangements(String studio) {
        String dayofweek = null;
        String class_number = null;
        String duration = null;
        String subject = null;
        List<String> resul_list = new ArrayList<>();
        try {
            List<Arrangement> list = dao.getArrangements(studio);
            for (int i = 0; i < list.size(); i++) {
                Arrangement line = list.get(i);
                //获取字段
                dayofweek = line.getDayofweek();
                class_number = line.getClass_number();
                duration = line.getDuration();
                subject = line.getSubject();


                String item = "星期"+dayofweek+ "," + class_number + "," + duration + "," + subject;

                //json
                resul_list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getArrangementsByDate(String studio, String date_time) {
        String dayofweek = null;
        String class_number = null;
        String duration = null;
        String subject = null;
        List<String> resul_list = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Integer weekDay=0;
        Integer weekofday=0;

        try {
            Date d = null;
            d = fmt.parse(date_time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            weekDay = cal.get(Calendar.DAY_OF_WEEK);
            if(weekDay==1){
                weekofday=7;
            }else {
                weekofday = weekDay - 1;
            }

            List<Arrangement> list = dao.getArrangements(studio);
            for (int i = 0; i < list.size(); i++) {
                Arrangement line = list.get(i);
                //获取字段
                dayofweek = line.getDayofweek();
                class_number = line.getClass_number();
                duration = line.getDuration();
                subject = line.getSubject();
                String item = "星期"+dayofweek+ "," + class_number + "," + duration + "," + subject;
                if(weekofday.equals(Integer.parseInt(dayofweek))){
                    resul_list.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getCertificateModel(String class_name) {
        byte[] photo = null;
        List<byte[]> resul_list = new ArrayList<>();
        try {

            List<Message> list = dao.getCertificateModel(class_name);
            for (int i = 0; i < list.size(); i++) {
                Message line = list.get(i);
                //获取字段
                photo = line.getPhoto();

                //json
                resul_list.add(photo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getMyOrder(String studio, String openid) {
        String goods_name = null;
        String goods_intro = null;
        Float goods_price = 0.0f;
        Integer status = 0;
        String status_get = null;
        String create_time = null;
        String id = null;
        String phone_number = null;
        String location = null;
        String nick_name = null;
        String open_id = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Order> list =null;
            if("All".equals(openid)){
                list = dao.getAllOrder(studio);
            }else {
                list = dao.getMyOrder(studio,openid);
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Order line = list.get(i);
                //获取字段
                id = line.getId();
                goods_name = line.getGoods_name();
                goods_intro = line.getGoods_intro();
                goods_price = line.getGoods_price();
                status = line.getStatus();
                phone_number = line.getPhone_number();
                location = line.getLocation();
                nick_name = line.getNick_name();
                open_id = line.getOpenid();

                if(0==status){
                    status_get="未发货";
                }
                if(1==status){
                    status_get="已发货";
                }
                create_time = line.getCreate_time();

                jsonObject.put("id", id);
                jsonObject.put("goods_name", goods_name);
                jsonObject.put("goods_intro", goods_intro);
                jsonObject.put("goods_price", goods_price);
                jsonObject.put("status", status_get);
                jsonObject.put("create_time", create_time);
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("location", location);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("openid", open_id);

                //json
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getFrameModel(String studio,Integer page,String class_target,String campus) {
        byte[] photo = null;
        String class_name = null;
        String id = null;
        String uuids = null;
        Integer page_start = 0;
        Integer page_length = 0;
        if (page == 1){
            page_start = (page - 1) * 6;
            page_length = 6;
        }
        if (page > 1){
            page_start = (page - 2) * 4 + 6;
            page_length = 4;
        }
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<Message> list = dao.getFrameModel(studio,page_start,page_length,class_target,campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                id = line.getId();
                photo = line.getPhoto();
                class_name =line.getClass_name();
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }

                //json
                jsonObject.put("id", id);
                jsonObject.put("photo", photo);
                jsonObject.put("class_name", class_name);
                jsonObject.put("uuids", uuids);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getCertificateModelName() {
        String class_name = null;
        List<String> resul_list = new ArrayList<>();
        try {

            List<Message> list = dao.getCertificateModelName();
            for (int i = 0; i < list.size(); i++) {
                Message line = list.get(i);
                //获取字段
                class_name = line.getClass_name();

                //json
                resul_list.add(class_name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getOpenidByNick(String student_name, String studio) {
        String openid = null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {

            List<User> list = dao.getOpenidByNick(student_name, studio);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                User line = list.get(i);
                //获取字段
                openid = line.getOpenid();
                //json
                jsonObject.put("openid", openid);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getAdvertise(String class_target,String studio,Integer page) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String student_name = null;
        String class_name = null;
        String id = null;
        String create_time = null;
        Integer page_start = (page - 1) * 1;
        Integer page_length = 1;
        String uuids = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getAdvertise(class_target, studio,page_start,page_length);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                class_name = line.getClass_name();
                comment = line.getComment();
                photo = line.getPhoto();
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                if(uuids != null){
                    photo = null;
                }
                class_target = line.getClass_target();
                id = line.getId();
                create_time = line.getCreate_time();
                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("photo", photo);
                jsonObject.put("class_target", class_target);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("uuids", uuids);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getBook(String studio,String dimension,String campus) {
        Float income = 0.0f;
        Float expenditure = 0.0f;
        String create_time = null;
        List<BookCount> list =null;
        Float sum = 0.0f;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            if("月".equals(dimension)){
                list = dao.getBookByMonth(studio,campus);
            }else if("日".equals(dimension)){
                list = dao.getBookByDate(studio,campus);
            }else if("年".equals(dimension)){
                list = dao.getBookByYear(studio,campus);
            }else if("总".equals(dimension)){
                list = dao.getBookByAll(studio,campus);
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                BookCount line = list.get(i);
                //获取字段
                income = line.getIncome();
                expenditure = line.getExpenditure();
                create_time = line.getCreate_time();
                sum = income  - expenditure;

                jsonObject.put("income", income);
                jsonObject.put("expenditure", expenditure);
                jsonObject.put("sum", sum);
                jsonObject.put("create_time", create_time);

                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getBookDetail(String studio, String create_time, String type,String start_date) {
        List<Book> list =null;
        String mark = null;
        Float amount = 0.0f;
        String id = null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            if("all".equals(type)){
                list = dao.getBookDetailAll(studio,create_time,start_date);
            }else {
                list = dao.getBookDetail(studio,create_time,type,start_date);
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Book line = list.get(i);
                //获取字段
                mark = line.getMark();
                amount = line.getAmount();
                create_time = line.getCreate_time();
                type = line.getType();
                id = line.getId();

                jsonObject.put("type", type);
                jsonObject.put("amount", amount);
                jsonObject.put("mark", mark);
                jsonObject.put("create_time", create_time);
                jsonObject.put("id", id);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List searchBookDetail(String studio, String value, String type) {
        List<Book> list =null;
        String mark = null;
        Float amount = 0.0f;
        String id = null;
        String create_time = null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.searchBookDetail(studio,value,type);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Book line = list.get(i);
                //获取字段
                mark = line.getMark();
                amount = line.getAmount();
                create_time = line.getCreate_time();
                type = line.getType();
                id = line.getId();

                jsonObject.put("type", type);
                jsonObject.put("amount", amount);
                jsonObject.put("mark", mark);
                jsonObject.put("create_time", create_time);
                jsonObject.put("id", id);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getCertificate(String studio, String student_name) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String class_name = null;
        String class_target = null;
        String id = null;
        String create_time = null;
        List<JSONObject> resul_list = new ArrayList<>();
        List<Message> list = null;
        try {
            if ("all".equals(student_name)) {
                list = dao.getCertificate(studio);
            } else {
                list = dao.getCertificateByName(studio, student_name);
            }
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                class_name = line.getClass_name();
                comment = line.getComment();
                photo = line.getPhoto();
                class_target = line.getClass_target();
                id = line.getId();
                create_time = line.getCreate_time();
                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("photo", photo);
                jsonObject.put("class_target", class_target);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getPaycode(String student_name) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String class_name = null;
        String class_target = null;
        String id = null;
        String create_time = null;
        String uuids = null;
        List<JSONObject> resul_list = new ArrayList<>();
        List<Message> list = null;
        try {
            list = dao.getPaycode(student_name);

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                class_name = line.getClass_name();
                comment = line.getComment();
                photo = line.getPhoto();
                class_target = line.getClass_target();
                id = line.getId();
                create_time = line.getCreate_time();
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                if(uuids != null){
                    photo = null;
                }
                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("photo", photo);
                jsonObject.put("class_target", class_target);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("uuids", uuids);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getClassSys(String class_target, String studio,Integer page,String campus) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String student_name = null;
        String class_name = null;
        String id = null;
        String create_time = null;
        Integer page_start = (page - 1) * 1;
        Integer page_length = 1;
        String uuids = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getClassSys(class_target, studio,page_start,page_length,campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                class_name = line.getClass_name();
                comment = line.getComment();
                photo = line.getPhoto();
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                if(uuids != null){
                    photo = null;
                }
                class_target = line.getClass_target();
                id = line.getId();
                create_time = line.getCreate_time();
                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("photo", photo);
                jsonObject.put("class_target", class_target);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("uuids", uuids);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
//            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getAlbum(String studio,String openid) {
        String uuids = null;
        String back_uuids = null;
        StringBuilder uuidString = new StringBuilder();
        List<JSONObject> resul_list = new ArrayList<>();
        List<User> list_user = dao.getUserByOpenid(openid);
        try {
            JSONObject jsonObject = new JSONObject();
            try {
                back_uuids = list_user.get(0).getBack_uuid().replace("\"","").replace("[","").replace("]","");
            } catch (Exception e) {
                //                    throw new RuntimeException(e);
            }

            for(int i = 0;i< list_user.size();i++){
                String campus = list_user.get(i).getCampus();
                String student_name = list_user.get(i).getStudent_name();
                List<Message> list = dao.getAlbum(studio,campus,student_name);

                for (int j = 0; j < list.size(); j++) {
                    Message line = list.get(j);
                    try {
                        uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                    } catch (Exception e) {
    //                    throw new RuntimeException(e);
                    }
                    uuidString.append(uuids).append(",");
                }
            }
            jsonObject.put("back_uuid",back_uuids);
            jsonObject.put("uuidString", uuidString.deleteCharAt(uuidString.length()-1));
            resul_list.add(jsonObject);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getCourseList(String studio, Integer page) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String student_name = null;
        String class_name = null;
        String id = null;
        String create_time = null;
        Integer page_start = (page - 1) * 3;
        Integer page_length = 3;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getCourseList(studio, page_start, page_length);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                class_name = line.getClass_name();
                comment = line.getComment();
                photo = line.getPhoto();
                id = line.getId();
                create_time = line.getCreate_time();
                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("photo", photo);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getCourseDetail(String studio, String class_name,Integer page) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String student_name = null;
        String id = null;
        String create_time = null;
        Integer page_start = (page - 1) * 1;
        Integer page_length = 1;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getCourseDetail(studio, class_name,page_start,page_length);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                class_name = line.getClass_name();
                comment = line.getComment();
                photo = line.getPhoto();
                id = line.getId();
                create_time = line.getCreate_time();
                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("photo", photo);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getHome(String studio,String campus) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String student_name = null;
        String class_name = null;
        String class_target = null;
        String id = null;
        String create_time = null;
        String uuids = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getHome(studio,campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                class_name = line.getClass_name();
                comment = line.getComment();
                photo = line.getPhoto();
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                if(uuids != null){
                   photo = null;
                }
                class_target = line.getClass_target();
                id = line.getId();
                create_time = line.getCreate_time();
                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("photo", photo);
                jsonObject.put("class_target", class_target);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("uuids", uuids);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public int updateMinusLesson(String student_name, String studio,Float class_count,String subject,String campus) {
        int result = 0;
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        Float new_left = 0.0f;
        Float minus = 0.0f;
        Float coins = 0.0f;
        Integer is_combine = 0;

        List<Lesson> list = dao.getLessonByNameSubject(student_name, studio,subject,campus);
        try {
            for (int i = 0; i < list.size(); i++) {
                Lesson line = list.get(i);
                total_amount = line.getTotal_amount();
                left_amount = line.getLeft_amount();
                new_left = left_amount - class_count;
                minus = line.getMinus();
                coins = line.getCoins();
                subject = line.getSubject();
                is_combine = line.getIs_combine();

                Lesson lesson = new Lesson();
                lesson.setStudent_name(student_name);
                lesson.setLeft_amount(new_left);
                lesson.setTotal_amount(total_amount);
                lesson.setStudio(studio);
                lesson.setMinus(minus);
                lesson.setCoins(coins);
                lesson.setSubject(subject);
                lesson.setCampus(campus);
                if(is_combine == 0){
                    result = dao.updateLesson(lesson);
                }else if (is_combine == 1){
                    result = dao.updateLessonBoth(lesson);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateAddPoints(String student_name, String studio,Integer points_int,String subject,String campus) {
        int result = 0;
        Integer points = 0;
        Integer new_points = 0;

        List<Lesson> list = dao.getLessonLikeNameBySubject(studio, student_name,subject,campus);
        try {
            for (int i = 0; i < list.size(); i++) {
                Lesson line = list.get(i);
                points = line.getPoints();
                if (points == null) {
                    points = 0;
                }
                new_points = points + points_int;
                Lesson lesson = new Lesson();
                lesson.setStudent_name(student_name);
                lesson.setPoints(new_points);
                lesson.setStudio(studio);
                lesson.setSubject(subject);
                lesson.setCampus(campus);
                result = dao.updateLessonPoint(lesson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    @Override
    public int deletePoints(String student_name, String studio,Integer points,String subject,String campus) {
        int result = 0;
        try {
            List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
            Lesson lesson_get = lessons.get(0);
            Integer total_points = lesson_get.getPoints();
            Integer new_points = total_points-points;

            Lesson lesson = new Lesson();
            lesson.setStudent_name(student_name);
            lesson.setPoints(new_points);
            lesson.setStudio(studio);
            lesson.setSubject(subject);
            lesson.setCampus(campus);
            result = dao.updateLessonPoint(lesson);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateCoins(String openid, String type) {
        int result = 0;
        Integer coins = 0;
        Integer new_coins = 0;

        List<User> list = dao.getUser(openid);
        try {
            for (int i = 0; i < list.size(); i++) {
                User line = list.get(i);
                coins = line.getCoins();
                if (coins == null) {
                    coins = 0;
                }
                if ("add".equals(type)) {
                    new_coins = coins + 1;
                } else if ("minus".equals(type)) {
                    new_coins = coins - 1;
                }

                User user = new User();
                user.setCoins(new_coins);
                user.setOpenid(openid);
                result = dao.updateCoins(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateGift(String id) {
        int result = 0;
        try {
            result = dao.updateGift(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void sendClassRemind() {
        // 获取 token
        String url = "https://api.weixin.qq.com/cgi-bin/token";
        String MOMO2B_param = "appid=wxc61d8f694d20f083&secret=ed083522ff79ac7dad24e115aecfbc08&grant_type=client_credential";
        String MOMO_param = "appid=wxa3dc1d41d6fa8284&secret=f2c191273540906cbc74e67d0b8fdd2a&grant_type=client_credential";

        String token_result = HttpUtil.sendPost(url,MOMO2B_param);
        JSONObject jsonObject = JSON.parseObject(token_result);
        String token = jsonObject.getString("access_token");

        String token_resul1 = HttpUtil.sendPost(url,MOMO_param);
        JSONObject jsonObject1 = JSON.parseObject(token_resul1);
        String token1 = jsonObject1.getString("access_token");


        // 获取用户信息
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal   =   Calendar.getInstance();
        cal.add(Calendar.DATE,+1);
        Integer weekDay = cal.get(Calendar.DAY_OF_WEEK);
        String date_time = df.format(cal.getTime());
        String now_time = df_now.format(new Date()).split(" ")[1];
        String now_date = df_now.format(new Date()).split(" ")[0];

        String result = null;
        String openid = null;
        String studio =null;
        String student_name = null;
        String role = null;
        String duration = null;
        String class_number = null;
        String send_time = null;
        String expried_time = null;
        String subject = null;
        List<User> list= null;
        Integer remind = 0;
        String subscription =null;
        List<Schedule> list_schedule = null;
        String tample3 = "{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"3BPMQuajTekT04oI8rCTKMB2iNO4XWdlDiMqR987TQk\",\"data\":{\"date1\":{\"value\": \"2022-11-01 10:30-11:30\"},\"thing2\":{\"value\": \"A1\"},\"name3\":{\"value\": \"小明\"},\"thing5\":{\"value\": \"记得来上课哦\"}}}";
        String tample4 = "{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"eJHpjkk4NqP6Y4qCMqGY1V5w4eeMVvRAkubflv25oh0\",\"data\":{\"name1\":{\"value\": \"name1\"},\"thing2\":{\"value\": \"thing2\"},\"date3\":{\"value\": \"date3\"},\"thing4\":{\"value\": \"thing4\"}}}";
        String tample5 ="{\"touser\":\"openid\",\"mp_template_msg\":{\"appid\":\"wxc79a69144e4fd233\",\"template_id\":\"AvUav55Zplm-MihGDlW0TxWMr_QMhWgP_6PFfJs27wc\",\"url\":\"http://weixin.qq.com/download\", \"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"},\"data\":{\"first\":{\"value\": \"AA\"},\"keyword1\":{\"value\": \"time\"},\"keyword2\":{\"value\": \"A1\"},\"keyword3\":{\"value\": \"A1\"},\"remark\":{\"value\": \"A1\"}}}}";
        String tample13 ="{\"touser\":\"openid\",\"mp_template_msg\":{\"appid\":\"wxc79a69144e4fd233\",\"template_id\":\"O9vQEneXUbkhdCuWW_-hQEGqUztTXQ8g0Mrgy97VAuI\",\"url\":\"http://weixin.qq.com/download\", \"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"},\"data\":{\"first\":{\"value\": \"通知广播\"},\"keyword1\":{\"value\": \"time\"},\"keyword2\":{\"value\": \"A1\"},\"remark\":{\"value\": \"请点击查看详情。\"}}}}";
        String url_send = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
        String url_union = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/uniform_send?access_token=" + token1;
        String publickey = "BGVksyYnr7LQ2tjLt8Y6IELBlBS7W8IrOvVszRVuE0F97qvcV6qB_41BJ-pXPaDf6Ktqdg6AogGK_UUc3zf8Snw";
        String privatekey = "oc5e7TovuZB8WVXqQoma-I14sYjoeBp0VJTjqOWL7mE";
        String campus = null;

        list = dao.getAllUser();
        for (int i = 0; i < list.size(); i++) {
            User user = list.get(i);
            role = user.getRole();
            openid = user.getOpenid();
            studio = user.getStudio();
            student_name = user.getStudent_name();
            send_time = user.getSend_time();
            expried_time = user.getExpired_time();
            subscription = user.getSubscription();
            Long compare = 10L;
            campus = user.getCampus();
            try {
                Date today_dt = df.parse(now_date.substring(0,10));
                Date expired_dt = df.parse(expried_time.substring(0,10));
                Long day2 = expired_dt.getTime();
                Long day1 = today_dt.getTime();
                compare = (day2 - day1)/(24*3600*1000);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            if(role.equals("boss") && compare <= 5L && send_time.equals(now_time)){
                JSONObject queryJson = JSONObject.parseObject(tample4);
                queryJson.put("touser",openid);
                queryJson.getJSONObject("data").getJSONObject("name1").put("value","小桃子助手");
                queryJson.getJSONObject("data").getJSONObject("thing2").put("value",studio);
                queryJson.getJSONObject("data").getJSONObject("date3").put("value",expried_time);
                queryJson.getJSONObject("data").getJSONObject("thing4").put("value","BOSS还有"+ compare +"天就期啦，记得续费哦");

                //公众号通知
                JSONObject queryJson1 = JSONObject.parseObject(tample13);
                queryJson1.put("touser",openid);
                queryJson1.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("keyword1").put("value","有效期至:" + expried_time);
                queryJson1.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("keyword2").put("value",studio+"还有"+ compare +"天就期啦，记得续费哦");

                try {
                    result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
                    System.out.printf("res:" + result);

                    result = HttpUtil.sendPostJson(url_union,queryJson1.toJSONString());
                    System.out.printf("res:" + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if("boss".equals(role) || "teacher".equals(role) ){
                if(send_time.equals(now_time)){
                    list_schedule = dao.getScheduleAll(weekDay,studio,campus);
                    if(list_schedule.size()>0){
                        //小程序通知
                        JSONObject queryJson = JSONObject.parseObject(tample3);
                        queryJson.put("touser",openid);
                        queryJson.getJSONObject("data").getJSONObject("date1").put("value",date_time);
                        queryJson.getJSONObject("data").getJSONObject("thing2").put("value","老师好");
                        queryJson.getJSONObject("data").getJSONObject("name3").put("value","上课提醒已发送");

                        result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
                        System.out.printf("res:" + result);

                        //公众号通知
                        JSONObject queryJson1 = JSONObject.parseObject(tample5);
                        queryJson1.put("touser",openid);
                        queryJson1.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("keyword1").put("value","上课提醒已发送");
                        queryJson1.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("keyword2").put("value",date_time);
                        queryJson1.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("keyword3").put("value",studio);

                        System.out.println("json:" + queryJson1.toJSONString());
                        result = HttpUtil.sendPostJson(url_union,queryJson1.toJSONString());
                        System.out.printf("res:" + result);

                    }
                }
            }

            if(!"no_name".equals(student_name) && send_time.equals(now_time)){
                list_schedule = dao.getScheduleByUser(weekDay,studio,student_name,campus);
                for (int j = 0; j < list_schedule.size(); j++) {
                    Schedule schedule = list_schedule.get(j);
                    duration = schedule.getDuration();
                    class_number = schedule.getClass_number();
                    subject = schedule.getSubject();
                    remind = schedule.getRemind();
                    Integer choose = 0;
                    Integer weekDayChoose = 0;
                    if(weekDay == 1){
                        weekDayChoose = 7;
                    }else {
                        weekDayChoose = weekDay -1;
                    }

                    String chooseLesson = "星期"+  weekDayChoose + "," + subject + "," + class_number + "," + duration ;
                    List<User> users = null;
                    try {
                        users = dao.getUserByChooseLesson(chooseLesson);
                        if(users.size()>0){
                            choose = 1;
                        }
                    } catch (Exception e) {
//                        throw new RuntimeException(e);
                    }

                    JSONObject queryJson = JSONObject.parseObject(tample3);
                    JSONObject queryJson1 = JSONObject.parseObject(tample5);
                    if(remind == 1 && choose == 1){
                        queryJson.put("touser",openid);
                        queryJson.getJSONObject("data").getJSONObject("date1").put("value",date_time +" " + duration.split("-")[0]);
                        queryJson.getJSONObject("data").getJSONObject("thing2").put("value",class_number);
                        queryJson.getJSONObject("data").getJSONObject("name3").put("value",student_name);

                        //公众号通知
                        queryJson1.put("touser",openid);
                        queryJson1.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("keyword1").put("value",class_number + "("+ student_name + ")");
                        queryJson1.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("keyword2").put("value",date_time + " " + duration.split("-")[0]);
                        queryJson1.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("keyword3").put("value",studio);

                        try {
                            result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
                            System.out.printf("res:" + result);

                            result = HttpUtil.sendPostJson(url_union,queryJson1.toJSONString());
                            System.out.printf("res:" + result);

                            JSONObject payload = new JSONObject();
                            payload.put("title",studio);
                            payload.put("message","上课日期:"+ date_time +"\n上课时间:"+ duration + "\n班号:" + class_number + "\n学生名:" + student_name );

                            String status = webPushService.sendNotification(subscription,publickey,privatekey,payload.toString());
                            System.out.printf("status:" + status);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    @Override
    public String updateLessonRemind(String student_name, String studio, String campus, String subject, String modify_amount,String openid,String modify_type) {
        String result = null;
        String modify_name = null;
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        Float old_number = 0.0f;
        Float new_number = 0.0f;
        Float value = 0.0f;
        String token = getToken("MOMO");
        String url = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/uniform_send?access_token=" + token;
        String tample1 ="{\"touser\":\"openid\",\"mp_template_msg\":{\"appid\":\"wxc79a69144e4fd233\",\"template_id\":\"KrFUcqBQqmMP3sJVOJIeku7zQUh0JNXqUpHCbK_CpJI\",\"url\":\"http://weixin.qq.com/download\", \"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"},\"data\":{\"phrase3\":{\"value\": \"通知广播\"},\"thing11\":{\"value\": \"time\"},\"number6\":{\"value\": \"1\"},\"number9\":{\"value\": \"1\"}}}}";
        String tample2 ="{\"touser\":\"openid\",\"mp_template_msg\":{\"appid\":\"wxc79a69144e4fd233\",\"template_id\":\"KNSzzGomOzv8xiO0NryfMueMm2rfNfUkG53gAqk8yPY\",\"url\":\"http://weixin.qq.com/download\", \"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"},\"data\":{\"phrase3\":{\"value\": \"通知广播\"},\"thing13\":{\"value\": \"time\"},\"number4\":{\"value\": \"1\"},\"number5\":{\"value\": \"1\"}}}}";

        try {
            List<User> list = dao.getUser(openid);
            String nick_name = list.get(0).getNick_name();

            List<Lesson> lessons_get = dao.getLessonByNameSubject(student_name,studio,subject,campus);
            total_amount = lessons_get.get(0).getTotal_amount();
            left_amount = lessons_get.get(0).getLeft_amount();

            if (!modify_amount.isEmpty() && !"0".equals(modify_amount)){
                new_number = Float.valueOf(modify_amount);
            }else if ("0".equals(modify_amount) && "total_modify".equals(modify_type)){
                new_number = 0.0f;
            }

            if("total_modify".equals(modify_type)){
                modify_name = "总课时";
                old_number = total_amount;
            }else if("left_modify".equals(modify_type)){
                modify_name = "余课时";
                old_number = left_amount;
            }

            value = new_number - old_number;

            DecimalFormat df = new DecimalFormat("0.00");

            List<User> users =dao.getBossByStudio(studio);
            if(users.size()>0){
                for(int i=0;i < users.size(); i++){
                    openid = users.get(i).getOpenid();
                    String role = users.get(i).getRole();
                    if("boss".equals(role)){
                        JSONObject queryJson = new JSONObject();;
                        if(value>=0){
                            queryJson = JSONObject.parseObject(tample1);
                            queryJson.put("touser",openid);
                            queryJson.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("phrase3").put("value","修改" + modify_name);
                            queryJson.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("thing11").put("value",student_name + "(" + nick_name + ")");
                            queryJson.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("number6").put("value",df.format(Math.abs(value)));
                            queryJson.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("number9").put("value",df.format(new_number));
                        }else if(value<0){
                            queryJson = JSONObject.parseObject(tample2);
                            queryJson.put("touser",openid);
                            queryJson.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("phrase3").put("value","修改" + modify_name);
                            queryJson.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("thing13").put("value", student_name + "(" + nick_name + ")" );
                            queryJson.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("number4").put("value",df.format(Math.abs(value)));
                            queryJson.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("number5").put("value",df.format(new_number));
                        }

                        String param1="access_token="+ token +"&data=" + queryJson.toJSONString();
                        System.out.printf("param:"+param1);
                        result = HttpUtil.sendPostJson(url,queryJson.toJSONString());
                        System.out.printf("res:" + result);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String getToken(String app) {
        String result = null;
        String token = null;
        String param = null;
        String appid = Constants.appid;
        String secret = Constants.secret;
        String appid_2b = Constants.appid_2b;
        String secret_2b = Constants.secret_2b;
        String url = "https://api.weixin.qq.com/cgi-bin/token";

        if ("MOMO2B".equals(app)){
            param = "appid=" + appid_2b + "&secret=" + secret_2b + "&grant_type=client_credential";
        }else if ("MOMO".equals(app)){
            param = "appid=" + appid + "&secret=" + secret + "&grant_type=client_credential";;
        }
        try {
            token = TokenCache.get(app);
            if(token == null){
                result = HttpUtil.sendPost(url,param);
                JSONObject jsonObject = JSON.parseObject(result);
                token = jsonObject.getString("access_token");
                TokenCache.put(app,token);
            }
        } catch (Exception e) {
//			e.printStackTrace();
        }
        return token;
    }

    @Override
    public List getStudentByTeacher(String studio, String openid, String date_start, String date_end) {
        List<SignUp> list = null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<User> list_user = dao.getUser(openid);
            String nick_name = list_user.get(0).getNick_name();
            String campus = list_user.get(0).getCampus();
            if(date_end.equals("undefined")){
                list = dao.getStudentByTeacher(studio,nick_name);
            }else {
                list = dao.getStudentByTeacherByDuration(studio,nick_name,date_start,date_end);
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                SignUp line = list.get(i);
                String subject = line.getSubject();
                String student_name = line.getStudent_name();
                String sign_time =line.getSign_time();
                String create_time = line.getCreate_time();
                String mark = line.getMark();
                Float count = line.getCount();
                Float total_amount = 0.0f;
                Float total_money = 0.0f;
                Float discount_money = 0.0f;
                Float price = 0.0f;
                Float sign_price = 0.0f;

                try {
                    List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                    if(lessons.size()>0){
                        Lesson lesson = lessons.get(0);
                        price = lesson.getPrice();
                        total_amount = lesson.getTotal_amount();
                    }


                    List<LessonPackage> lessonPackages = dao.getLessonPackage(student_name,studio,campus,subject);
                    if(lessonPackages.size()>0){
                        for(int j = 0; j < lessonPackages.size(); j++){
                            LessonPackage lessonPackage = lessonPackages.get(j);
                            total_money = total_money + lessonPackage.getTotal_money();
                            discount_money = discount_money + lessonPackage.getDiscount_money();
                        }
                    }

                    Float receipts = total_money - discount_money;
                    Float re_price = receipts/total_amount;
                    if(re_price>0){
                        price = re_price;
                    }

                    sign_price = count * price;

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                DecimalFormat df = new DecimalFormat("0.00");

                jsonObject.put("studio", studio);
                jsonObject.put("subject", subject);
                jsonObject.put("campus", campus);
                jsonObject.put("student_name", student_name);
                jsonObject.put("sign_time", sign_time);
                jsonObject.put("create_time", create_time);
                jsonObject.put("mark", mark);
                jsonObject.put("count", count);
                jsonObject.put("price", df.format(price));
                jsonObject.put("sign_price", df.format(sign_price));
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public String getOpenid(String code, String app) {
        String result = null;
        String openid = null;
        String param = null;
        String appid = Constants.appid;
        String secret = Constants.secret;
        String appid_2b = Constants.appid_2b;
        String secret_2b = Constants.secret_2b;
        String url = "https://api.weixin.qq.com/sns/jscode2session";

        if ("MOMO2B".equals(app)){
            param = "appid="+ appid_2b + "&secret=" + secret_2b + "&js_code="+ code +"&grant_type=authorization_code";;
        }else if("MOMO".equals(app)){
            param = "appid="+ appid + "&secret=" + secret + "&js_code="+ code +"&grant_type=authorization_code";
        }
        try {
            result = HttpUtil.sendPost(url	,param);
            JSONObject jsonObject = JSON.parseObject(result);
            openid = jsonObject.getString("openid");
        } catch (Exception e) {
//			e.printStackTrace();
        }
        return openid;
    }

    @Override
    public String renewLessonRemind(String student_name, String studio, String campus, String subject, Float lesson_amount) {
        String result = null;
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        String token = getToken("MOMO");
        String url = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/uniform_send?access_token=" + token;
        String tample1 ="{\"touser\":\"openid\",\"mp_template_msg\":{\"appid\":\"wxc79a69144e4fd233\",\"template_id\":\"LbJ2VBZ7f3qz_i3nBRzynL79DVOmRqIN_61reo5m4p4\",\"url\":\"http://weixin.qq.com/download\", \"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"},\"data\":{\"thing2\":{\"value\": \"AA\"},\"thing3\":{\"value\": \"AA\"},\"thing1\":{\"value\": \"1\"}}}}";

        try {
            List<User> list = dao.getUserByStudent(student_name,studio);
            String openid = list.get(0).getOpenid();

            List<Lesson> lessons_get = dao.getLessonByNameSubject(student_name,studio,subject,campus);
            total_amount = lessons_get.get(0).getTotal_amount();
            left_amount = lessons_get.get(0).getLeft_amount();

            Float total_new = total_amount + lesson_amount;
            Float left_new = left_amount + lesson_amount;


            DecimalFormat df = new DecimalFormat("0.00");
            JSONObject queryJson = JSONObject.parseObject(tample1);
            queryJson.put("touser",openid);
            queryJson.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("thing2").put("value",student_name+"(" + subject + ")");
            queryJson.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("thing3").put("value","成功续课" + lesson_amount + "课时");
            queryJson.getJSONObject("mp_template_msg").getJSONObject("data").getJSONObject("thing1").put("value",studio + "(总" + total_new + "余"+ left_new + ")");
            String param1="access_token="+ token +"&data=" + queryJson.toJSONString();
            System.out.printf("param:"+param1);
            result = HttpUtil.sendPostJson(url,queryJson.toJSONString());
            System.out.printf("res:" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List getLessonByName(String student_name, String studio,String campus){
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        String subject =null;
        Integer points=0;
        List<JSONObject> resul_list = new ArrayList<>();
        try {

            List<Lesson> list = dao.getLessonByName(student_name,studio,campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Lesson line = list.get(i);
                //获取字段
                total_amount = line.getTotal_amount();
                left_amount = line.getLeft_amount();
                subject = line.getSubject();
                points = line.getPoints();

                //json
                jsonObject.put("total_amount", total_amount);
                jsonObject.put("left_amount", left_amount);
                jsonObject.put("subject", subject);
                jsonObject.put("student_name", student_name);
                jsonObject.put("studio", studio);
                jsonObject.put("points", points);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getLessonPackage(String student_name, String studio,String campus,String subject) {
        Float total_money = 0.0f;
        Float discount_money = 0.0f;
        Float all_lesson = 0.0f;
        Float give_lesson = 0.0f;
        List<JSONObject> resul_list = new ArrayList<>();
        try {

            List<LessonPackage> list = dao.getLessonPackage(student_name,studio,campus,subject);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                LessonPackage line = list.get(i);
                //获取字段
                total_money = line.getTotal_money();
                discount_money = line.getDiscount_money();
                String mark = line.getMark();
                String start_date = line.getStart_date();
                String end_date = line.getEnd_date();
                String id = line.getId();
                all_lesson = line.getAll_lesson();
                give_lesson = line.getGive_lesson();

                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("total_money", total_money);
                jsonObject.put("discount_money", discount_money);
                jsonObject.put("mark", mark);
                jsonObject.put("start_date", start_date);
                jsonObject.put("end_date", end_date);
                jsonObject.put("id", id);
                jsonObject.put("all_lesson", all_lesson);
                jsonObject.put("give_lesson", give_lesson);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getLessonByNameSubject(String student_name, String studio,String subject,String campus) {
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        Float minus = 0.0f;
        List<JSONObject> resul_list = new ArrayList<>();
        try {

            List<Lesson> list = dao.getLessonByNameSubject(student_name, studio,subject,campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Lesson line = list.get(i);

                //获取字段
                total_amount = line.getTotal_amount();
                left_amount = line.getLeft_amount();
                minus = line.getMinus();

                //json
                jsonObject.put("total_amount", total_amount);
                jsonObject.put("left_amount", left_amount);
                jsonObject.put("minus", minus);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getMessage(String studio, Integer page,String comment_style,String openid,String role,String class_target,String campus) {
        String comment = null;
        String class_name = null;
        String id = null;
        String create_time = null;
        byte[] photo = null;
        String student_name_get = null;
        String student_name = null;
        Integer page_start = (page - 1) * 7;
        Integer page_length = 7;
        List<JSONObject> resul_list = new ArrayList<>();
        List<Message> list=new ArrayList<>();;
        List<User> users=null;
        String duration = null;
        Integer positive = 0;
        Integer discipline = 0;
        Integer happiness = 0;
        String mp3_url = null;
        StringBuilder student_names = new StringBuilder();

        try {
            users  =dao.getUserByOpenid(openid);
            if(users.size()>0){
                for (int i = 0; i < users.size(); i++) {
                    User line = users.get(i);
                    student_name_get = line.getStudent_name();
                    student_names = student_names.append(student_name_get).append(",");
                }
                student_names = student_names.deleteCharAt(student_names.lastIndexOf(","));
            }

            if(comment_style.equals("self")&&role.equals("client")){
                list = dao.getMessageInName(student_names.toString(),studio,page_start,page_length,class_target,campus);
            }else if(role.equals("boss") || comment_style.equals("public") || role.equals("teacher")) {
                list = dao.getMessage(studio, page_start, page_length,class_target,campus);
            }

            if(list.size()>0){
                for (int i = 0; i < list.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    Float percent = 0.0f;
                    Float left = 0.0f;
                    Float total = 0.0f;
                    Message line = list.get(i);
                    //获取字段
                    student_name = line.getStudent_name();
                    class_name = line.getClass_name();
                    comment = line.getComment();
                    class_target = line.getClass_target();
                    id = line.getId();
                    create_time = line.getCreate_time();
                    studio = line.getStudio();
                    duration = line.getDuration();
                    positive = line.getPositive();
                    discipline = line.getDiscipline();
                    happiness = line.getHappiness();
                    mp3_url = line.getMp3_url();
                    String uuids = null;
                    try {
                        uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                    } catch (Exception e) {
//                    throw new RuntimeException(e);
                    }
                    if(uuids != null){
                        photo = null;
                    }

                    try {
                        List<Lesson> lessons = dao.getLessonByName(student_name, studio,campus);
                        if(lessons.size()>0){
                            Lesson lesson = lessons.get(0);
                            left = lesson.getLeft_amount();
                            total = lesson.getTotal_amount();
                            if (left > 0 || total > 0) {
                                percent = (float) Math.round(left * 100 / total);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //json
                    jsonObject.put("student_name", student_name);
                    jsonObject.put("class_name", class_name);
                    jsonObject.put("comment", comment);
                    jsonObject.put("percent", percent);
                    jsonObject.put("left", left);
                    jsonObject.put("total",total);
                    jsonObject.put("class_target", class_target);
                    jsonObject.put("id", id);
                    jsonObject.put("create_time", create_time.substring(0,10));
                    jsonObject.put("duration", duration);
                    jsonObject.put("positive", positive);
                    jsonObject.put("discipline", discipline);
                    jsonObject.put("happiness", happiness);
                    jsonObject.put("uuids", uuids);
                    jsonObject.put("mp3_url", mp3_url);
                    resul_list.add(jsonObject);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getCommentModel() {
        List<JSONObject> resul_list = new ArrayList<>();
        List<Message> list = dao.getCommentModel();
        for (int i = 0; i < list.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            Message line = list.get(i);
            String class_target = line.getClass_target();
            String comment = line.getComment();
            String id = line.getId();

            jsonObject.put("class_target", class_target);
            jsonObject.put("comment", comment);
            jsonObject.put("id", id);
            resul_list.add(jsonObject);
        }
        return resul_list;
    }

    @Override
    public List getPost(String studio, Integer page, String openid,String type) {
        List<JSONObject> resul_list = new ArrayList<>();
        Integer page_start = (page - 1) * 4;
        Integer page_length = 4;

        try {
            List<Post> posts =null;
            if("public".equals(type)){
                 posts = dao.getPostPublic(page_start,page_length);
            }else if("private".equals(type)){
                 posts = dao.getPostPrivate(page_start,page_length,studio);
            }
            for (int i = 0; i < posts.size(); i++) {
                String openid_get = null;
                String studio_get = null;
                String content =null;
                String create_time = null;
                String id = null;
                String avatar = "https://www.momoclasss.xyz:443/file/uploadimages/fa8a634a-40c2-412a-9a95-2bd8d5ba5675.png";
                String nick_name = "游客";
                Integer comment_amount = 0;
                Integer like_amount = 0;
                JSONObject jsonObject = new JSONObject();
                Post line = posts.get(i);
                //获取字段
                openid_get = line.getOpenid();
                try {
                    List<User> list_user = dao.getUser(openid_get);
                    avatar = list_user.get(0).getAvatarurl();
                    nick_name = list_user.get(0).getNick_name();
                } catch (Exception e) {
                    // throw new RuntimeException(e);
                }
                studio_get = line.getStudio();
                String uuids = null;
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                content = line.getContent();
                create_time = line.getCreate_time();
                id = line.getId();
                try {
                    List<PostComment> postComments = dao.getPostComment(id);
                    comment_amount = postComments.size();
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }

                try {
                    List<PostLike> postLikes = dao.getPostLike(id);
                    like_amount = postLikes.size();
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }


                jsonObject.put("openid_get", openid_get);
                jsonObject.put("studio_get", studio_get);
                jsonObject.put("uuids", uuids);
                jsonObject.put("content", content);
                jsonObject.put("avatar", avatar);
                jsonObject.put("nick_name",nick_name);
                jsonObject.put("create_time", create_time);
                jsonObject.put("comment_amount", comment_amount);
                jsonObject.put("like_amount", like_amount);
                jsonObject.put("id", id);
                resul_list.add(jsonObject);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getPostComment(String post_id) {
        List<JSONObject> resul_list = new ArrayList<>();
        String openid = null;
        String studio = null;
        String content =null;
        String create_time = null;
        String id = null;

        try {
            List<PostComment> postComments = dao.getPostComment(post_id);
            for (int i = 0; i < postComments.size(); i++) {
                String nick_name = "游客";
                String avatar = null;
                JSONObject jsonObject = new JSONObject();
                PostComment line = postComments.get(i);
                //获取字段
                openid = line.getOpenid();
                try {
                    List<User> list_user = dao.getUser(openid);
                    avatar = list_user.get(0).getAvatarurl();
                    nick_name = list_user.get(0).getNick_name();
                } catch (Exception e) {
                    // throw new RuntimeException(e);
                }

                studio = line.getStudio();
                content = line.getContent();
                create_time = line.getCreate_time();
                id = line.getId();

                jsonObject.put("openid", openid);
                jsonObject.put("studio_get", studio);
                jsonObject.put("content", content);
                jsonObject.put("avatar", avatar);
                jsonObject.put("nick_name",nick_name);
                jsonObject.put("create_time", create_time);
                jsonObject.put("id", id);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public String changeClass(String studio, Integer changeday, String duration, String class_number, Integer weekday,String subject,String campus) {
        Integer dayofweek_by= 0;
        List<JSONObject> resul_list = new ArrayList<>();
        String limits = null;
        String add_date = null;
        String student_name = null;
        String student_type = null;
        if(weekday==7){
            dayofweek_by=1;
        }else {
            dayofweek_by = weekday + 1;
        }

        try {
            List<Arrangement> arrangement_list = dao.getArrangementByDate(studio,weekday.toString(),class_number,duration,subject,campus);
            for (int i = 0; i < arrangement_list.size(); i++) {
                Arrangement line = arrangement_list.get(i);
                //获取字段
                class_number = line.getClass_number();
                duration = line.getDuration();
                limits = line.getLimits();
                subject = line.getSubject();

                List<Arrangement> check_list = dao.getArrangementByDate(studio,changeday.toString(),class_number,duration,subject,campus);
                if(check_list.size() == 0){
                    Arrangement arrangement =new Arrangement();
                    arrangement.setDayofweek(changeday.toString());
                    arrangement.setClass_number(class_number);
                    arrangement.setLimits(limits);
                    arrangement.setStudio(studio);
                    arrangement.setDuration(duration);
                    arrangement.setSubject(subject);
                    arrangement.setCampus(campus);
                    if(changeday != weekday){
                        dao.insertArrangement(arrangement);

                        try {
                            List<Schedule> schedule_list = dao.getScheduleDetail(dayofweek_by,duration,studio,class_number,subject,campus);
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");//设置日期格式
                            String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
                            String age =null;
                            Integer status = 0;
                            if(changeday==1){
                                add_date = "2022-05-02";
                            }else if(changeday==2){
                                add_date = "2022-05-03";
                            }else if(changeday==3){
                                add_date = "2022-05-04";
                            }else if(changeday==4){
                                add_date = "2022-05-05";
                            }else if(changeday==5){
                                add_date = "2022-05-06";
                            }else if(changeday==6){
                                add_date = "2022-05-07";
                            }else if(changeday==7){
                                add_date = "2022-05-08";
                            }
                            for (int j = 0; j < schedule_list.size(); j++) {
                                Schedule line_class = schedule_list.get(j);
                                //获取字段
                                class_number = line_class.getClass_number();
                                duration = line_class.getDuration();
                                student_name = line_class.getStudent_name();
                                student_type = line_class.getStudent_type();
                                age = line_class.getAge();
                                status = line_class.getStatus();
                                subject = line_class.getSubject();

                                Schedule schedule =new Schedule();
                                schedule.setAdd_date(add_date);
                                schedule.setAge(age);
                                schedule.setStudent_name(student_name);
                                schedule.setDuration(duration);
                                schedule.setCreate_time(create_time);
                                schedule.setUpdate_time(create_time);
                                schedule.setStudio(studio);
                                schedule.setClass_number(class_number);
                                schedule.setStudent_type(student_type);
                                schedule.setStatus(status);
                                schedule.setSubject(subject);
                                schedule.setCampus(campus);
                                schedule.setIs_try(0);
                                List<Schedule> check_schedule = dao.getScheduleCheck(add_date,duration,class_number,subject,studio,campus,student_name);
                                if(check_schedule.size()==0){
                                    dao.insertSchedule(schedule);
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }



        return "changeClass successfully";
    }

    @Override
    public List getGrowthRecord(String studio, Integer page, String student_name) {
        String comment = null;
        String id = null;
        String create_time = null;
        byte[] photo = null;
        Integer page_start = (page - 1) * 4;
        Integer page_length = 4;
        List<JSONObject> resul_list = new ArrayList<>();
        List<Message> list=null;
        String duration = null;
        String class_name = null;
        String mp3_url = null;

        try {
            list = dao.getMessageGrowth(student_name,studio,page_start,page_length);
            if(list.size()>0){
                for (int i = 0; i < list.size(); i++) {
                    String uuids = null;
                    JSONObject jsonObject = new JSONObject();
                    Message line = list.get(i);
                    //获取字段
                    comment = line.getComment();
                    id = line.getId();
                    create_time = line.getCreate_time();
                    duration = line.getDuration();
                    photo = line.getPhoto();
                    class_name = line.getClass_name();
                    mp3_url = line.getMp3_url();
                    try {
                        uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                    } catch (Exception e) {
//                    throw new RuntimeException(e);
                    }
                    if(uuids != null){
                        photo = null;

                    }

                    //json
                    jsonObject.put("comment", comment);
                    jsonObject.put("id", id);
                    jsonObject.put("create_time", create_time.substring(0,10));
                    jsonObject.put("duration", duration);
                    jsonObject.put("photo", photo);
                    jsonObject.put("class_name", class_name);
                    jsonObject.put("uuids", uuids);
                    jsonObject.put("mp3_url", mp3_url);
                    resul_list.add(jsonObject);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getUserByOpenid(String openid) {
        String role = null;
        String student_name = null;
        String avatarurl = null;
        String nick_name = null;
        String studio = null;
        String user_type = null;
        String create_time = null;
        String expired_time = null;
        Integer coins = 0;
        String comment_style = null;
        String theme = null;
        String subjects = null;
        List<User> list= null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getUserByOpenid(openid);
            if(openid.equals("all")){
                list = dao.getAllUser();
            }
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                User line = list.get(i);
                //获取字段
                role = line.getRole();
                student_name = line.getStudent_name();
                avatarurl = line.getAvatarurl();
                nick_name = line.getNick_name();
                studio = line.getStudio();
                user_type = line.getUser_type();
                create_time = line.getCreate_time();
                expired_time = line.getExpired_time();
                coins = line.getCoins();
                openid = line.getOpenid();
                comment_style = line.getComment_style();
                theme = line.getTheme();
                subjects = line.getSubjects();

                //json
                jsonObject.put("role", role);
                jsonObject.put("student_name", student_name);
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("studio", studio);
                jsonObject.put("user_type", user_type);
                jsonObject.put("create_time", create_time);
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("coins", coins);
                jsonObject.put("openid",openid);
                jsonObject.put("comment_style",comment_style);
                jsonObject.put("theme",theme);
                jsonObject.put("subjects",subjects);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getMessageClient(String student_name) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String class_name = null;
        String class_target = null;
        String id = null;
        String create_time = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getMessageClient(student_name);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                class_name = line.getClass_name();
                comment = line.getComment();
                photo = line.getPhoto();
                class_target = line.getClass_target();
                id = line.getId();
                create_time = line.getCreate_time();
                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("photo", photo);
                jsonObject.put("class_target", class_target);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getModel(String studio, Integer page,String campus) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String student_name = null;
        String class_name = null;
        String class_target = null;
        String id = null;
        String create_time = null;
        Integer page_start = (page - 1) * 3;
        Integer page_length = 3;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getModel(studio, page_start, page_length,campus);
            for (int i = 0; i < list.size(); i++) {
                String uuids = null;
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                class_name = line.getClass_name();
                comment = line.getComment();
                photo = line.getPhoto();
                class_target = line.getClass_target();
                id = line.getId();
                create_time = line.getCreate_time();
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                if(uuids != null){
                    photo = null;

                }
                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("photo", photo);
                jsonObject.put("class_target", class_target);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("uuids",uuids);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getCommunicateRecord(String studio, Integer page,String campus) {
        Integer page_start = (page - 1) * 10;
        Integer page_length = 10;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<CommunicateRecord> list = dao.getCommunicateRecord(studio, page_start, page_length,campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                CommunicateRecord line = list.get(i);
                //获取字段
                String student_name = line.getStudent_name();
                String content = line.getContent();
                String create_time = line.getCreate_time();
                String id = line.getId();
                String nick_name = line.getOpenid();
                String uuids = line.getUuids();

                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("studio", studio);
                jsonObject.put("campus", campus);
                jsonObject.put("content", content);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("nick_name",nick_name);
                jsonObject.put("uuids",uuids);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getCommunicateLike(String studio,String item,String campus) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<CommunicateRecord> list = dao.getCommunicateLike(studio, item, campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                CommunicateRecord line = list.get(i);
                //获取字段
                String student_name = line.getStudent_name();
                String content = line.getContent();
                String create_time = line.getCreate_time();
                String id = line.getId();
                String nick_name = line.getOpenid();
                String uuids = line.getUuids();

                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("studio", studio);
                jsonObject.put("campus", campus);
                jsonObject.put("content", content);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("nick_name",nick_name);
                jsonObject.put("uuids",uuids);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getMamaShare(Integer page) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String student_name = null;
        String class_name = null;
        String class_target = null;
        String id = null;
        String create_time = null;
        String studio = null;
        Integer page_start = (page - 1) * 4;
        Integer page_length = 4;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getMamaShare(page_start, page_length);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                class_name = line.getClass_name();
                comment = line.getComment();
                photo = line.getPhoto();
                class_target = line.getClass_target();
                id = line.getId();
                create_time = line.getCreate_time();
                studio = line.getStudio();
                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("photo", photo);
                jsonObject.put("class_target", class_target);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("studio", studio);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getPpt(Integer page) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String student_name = null;
        String class_name = null;
        String class_target = null;
        String id = null;
        String create_time = null;
        Integer page_start = (page - 1) * 3;
        Integer page_length = 3;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getPpt(page_start, page_length);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                class_name = line.getClass_name();
                comment = line.getComment();
                photo = line.getPhoto();
                class_target = line.getClass_target();
                id = line.getId();
                create_time = line.getCreate_time();
                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("photo", photo);
                jsonObject.put("class_target", class_target);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }


    @Override
    public List getLesson(String studio,String student_name,String subject,String campus) {
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        String create_time = null;
        String id = null;
        String subject_get = null;
        Integer points = 0;
        Float percent = 0.0f;
        Float minus = 0.0f;
        Float coins = 0.0f;
        List<Lesson> list = null;
        List<JSONObject> resul_list = new ArrayList<>();
        Integer length = student_name.split(",").length;
        Integer total_student =0;
        Float total_amount_all = 0.0f ;
        Float left_amount_all = 0.0f ;
        Integer need_pay = 0;
        Integer owe = 0;

        try {
            if(subject.equals("全科目")){
                AllCount allCount =dao.getLessonAllCount(studio,campus);
                total_student = allCount.getStudent_count();
                total_amount_all = allCount.getTotal_amount();
                left_amount_all = allCount.getLeft_amount();
                need_pay = dao.getLessonNeedPayCount(studio,campus);
                owe = dao.getLessonOweCount(studio,campus);
            }else{
                AllCount allCount =dao.getLessonAllCountBySubject(studio,subject,campus);
                if(allCount.getStudent_count()>0){
                    total_student = allCount.getStudent_count();
                    total_amount_all = allCount.getTotal_amount();
                    left_amount_all = allCount.getLeft_amount();
                    need_pay = dao.getLessonNeedPayCountBySubject(studio,subject,campus);
                    owe = dao.getLessonOweCountBySubject(studio,subject,campus);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if(student_name.equals("all")) {
                if(subject.equals("全科目")){
                    list = dao.getLesson(studio,campus);
                }else {
                    list = dao.getLessonBySubject(studio,subject,campus);
                }
            }else if (length>1) {
                if(subject.equals("全科目")){
                    list = dao.getLessonInName(studio,student_name,0,10000,campus);
                }else {
                    list = dao.getLessonInNameBySubject(studio,student_name,0,10000,subject,campus);
                }
            }else {
                if(subject.equals("全科目")){
                    list = dao.getLessonLikeName(studio,student_name,campus);
                }else {
                    list = dao.getLessonLikeNameBySubject(studio,student_name,subject,campus);
                }
            }


            for (int i = 0; i < list.size(); i++) {
                String parent = "未绑定";
                String avatarurl = "未绑定";
                JSONObject jsonObject = new JSONObject();
                Lesson line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                try {
                    List<User> user = dao.getUserByStudent(student_name,studio);
                    if(user.size()>0){
                        parent = user.get(0).getNick_name();
                        avatarurl = user.get(0).getAvatarurl();
                    }
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }

                total_amount = line.getTotal_amount();
                left_amount = line.getLeft_amount();
                percent = (float) Math.round(left_amount * 100 / total_amount);
                id = line.getId();
                create_time = line.getCreate_time();
                points = line.getPoints();
                minus = line.getMinus();
                coins = line.getCoins();
                subject_get = line.getSubject();
                studio = line.getStudio();
                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("total_amount", total_amount);
                jsonObject.put("left_amount", left_amount);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("percent", percent);
                jsonObject.put("points", points);
                jsonObject.put("rank", i + 1);
                jsonObject.put("show", false);
                jsonObject.put("name", student_name);
                jsonObject.put("search", student_name);
                jsonObject.put("total_student", total_student);
                jsonObject.put("total_amount_all", total_amount_all);
                jsonObject.put("left_amount_all", left_amount_all);
                jsonObject.put("minus", minus);
                jsonObject.put("coins", coins);
                jsonObject.put("need_pay", need_pay);
                jsonObject.put("owe", owe);
                jsonObject.put("subject", subject_get);
                jsonObject.put("parent", parent);
                jsonObject.put("studio", studio);
                jsonObject.put("avatarurl", avatarurl);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getLessonHead(String studio, String student_name, String subject, String campus) {
        List<JSONObject> resul_list = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        Integer total_student =0;
        Float total_amount_all = 0.0f ;
        Float left_amount_all = 0.0f ;
        Float total_price_all = 0.0f ;
        Float left_price_all = 0.0f ;
        Float total_money = 0.0f ;
        Float left_money = 0.0f ;
        Integer need_pay = 0;
        Integer owe = 0;
        DecimalFormat df = new DecimalFormat("0.00");
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM");//
        String month_date = df1.format(new Date());

        try {
            if(subject.equals("全科目")){
                AllCount allCount =dao.getLessonAllCount(studio,campus);
                total_student = allCount.getStudent_count();
                total_amount_all = allCount.getTotal_amount();
                left_amount_all = allCount.getLeft_amount();
                total_price_all = allCount.getTotal_price();
                left_price_all = allCount.getLeft_price();
                need_pay = dao.getLessonNeedPayCount(studio,campus);
                owe = dao.getLessonOweCount(studio,campus);

                List<Lesson> lessons = dao.getLesson(studio,campus);
                for(int i = 0;i < lessons.size();i++){
                    Lesson lesson = lessons.get(i);
                    String student_name_all = lesson.getStudent_name();
                    Float total_amount = lesson.getTotal_amount();
                    Float left_amount = lesson.getLeft_amount();
                    Float total = 0.0f;
                    Float disc = 0.0f;
                    List<LessonPackage> lessonPackages1 = dao.getLessonPackageByStudent(student_name_all,studio,campus);
                    if(lessonPackages1.size()>0){
                        for(int j = 0; j < lessonPackages1.size(); j++){
                            LessonPackage lessonPackage = lessonPackages1.get(j);
                            total = total + lessonPackage.getTotal_money();
                            disc = disc + lessonPackage.getDiscount_money();
                        }
                    }

                    Float price = (total-disc)/total_amount;
                    Float left_single = price * left_amount;
                    total_money = total_money + (total-disc);
                    left_money = left_money + left_single;
                }

            }else{
                AllCount allCount =dao.getLessonAllCountBySubject(studio,subject,campus);
                if(allCount.getStudent_count()>0){
                    total_student = allCount.getStudent_count();
                    total_amount_all = allCount.getTotal_amount();
                    left_amount_all = allCount.getLeft_amount();
                    total_price_all = allCount.getTotal_price();
                    left_price_all = allCount.getLeft_price();
                    need_pay = dao.getLessonNeedPayCountBySubject(studio,subject,campus);
                    owe = dao.getLessonOweCountBySubject(studio,subject,campus);

                    List<Lesson> lessons = dao.getLessonBySubject(studio,subject,campus);
                    for(int i = 0;i < lessons.size();i++){
                        Lesson lesson = lessons.get(i);
                        String student_name_all = lesson.getStudent_name();
                        Float total_amount = lesson.getTotal_amount();
                        Float left_amount = lesson.getLeft_amount();
                        Float total = 0.0f;
                        Float disc = 0.0f;
                        List<LessonPackage> lessonPackages1 = dao.getLessonPackageByStudentSubject(student_name_all,studio,campus,subject);
                        if(lessonPackages1.size()>0){
                            for(int j = 0; j < lessonPackages1.size(); j++){
                                LessonPackage lessonPackage = lessonPackages1.get(j);
                                total = total + lessonPackage.getTotal_money();
                                disc = disc + lessonPackage.getDiscount_money();
                            }
                        }

                        Float price = (total-disc)/total_amount;
                        Float left_single = price * left_amount;
                        total_money = total_money + (total-disc);
                        left_money = left_money + left_single;
                    }
                }
            }

            List<String> list_subject = new ArrayList<>();
            List<Lesson> subject_list = dao.getSubjectByStudio(studio,campus);
            try {
                for (int i = 0; i < subject_list.size(); i++) {
                    String subject_get = subject_list.get(i).getSubject();
                    if(!list_subject.contains(subject_get)){
                        list_subject.add(subject_get);
                    }
                }
            } catch (Exception e) {
//                throw new RuntimeException(e);
            }

            if(total_money > 0){
                total_price_all = total_money;
            }

            if(left_money > 0){
                left_price_all = left_money;
            }

            Integer new_student = 0;
            Float new_money = 0.0f;
            Float new_lesson = 0.0f;
            Integer continue_student = 0;
            Integer loss_student = 0;
            try {
                AllCount allcount = dao.getLessonPackageAllCount(studio,campus,month_date);
                continue_student = allcount.getStudent_count();
                new_money = allcount.getTotal_price() - allcount.getLeft_price();
                new_lesson = allcount.getTotal_amount()  + allcount.getLeft_amount();

                AllCount allCount1 = dao.getLessonAllCountNewStudent(studio,campus,month_date);
                new_student = allCount1.getStudent_count();

                AllCount allCount2 = dao.getLessonAllCountLossStudent(studio,campus,month_date);
                loss_student = allCount2.getStudent_count();
            } catch (Exception e) {
//                throw new RuntimeException(e);
            }

            jsonObject.put("total_student", total_student);
            jsonObject.put("total_amount_all", df.format(total_amount_all));
            jsonObject.put("left_amount_all", df.format(left_amount_all));
            jsonObject.put("total_price_all", df.format(total_price_all));
            jsonObject.put("left_price_all", df.format(left_price_all));
            jsonObject.put("need_pay", need_pay);
            jsonObject.put("owe", owe);
            jsonObject.put("subject_list", subject_list);
            jsonObject.put("continue_student", continue_student);
            jsonObject.put("new_money", new_money);
            jsonObject.put("new_lesson", new_lesson);
            jsonObject.put("new_student", new_student);
            jsonObject.put("loss_student", loss_student);

            resul_list.add(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    public List getLessonByStudio(String studio) {
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        String create_time = null;
        String id = null;
        String subject_get = null;
        Integer points = 0;
        Float percent = 0.0f;
        Float minus = 0.0f;
        Float coins = 0.0f;
        List<Lesson> list = null;
        List<JSONObject> resul_list = new ArrayList<>();
        Integer total_student =0;
        Float total_amount_all = 0.0f ;
        Float left_amount_all = 0.0f ;
        Integer need_pay = 0;
        Integer owe = 0;
        String student_name = null;
        String campus = null;
        list = dao.getLessonByStudio(studio);

        try {
            for (int i = 0; i < list.size(); i++) {
                String nick_name = "未绑定";
                JSONObject jsonObject = new JSONObject();
                Lesson line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                try {
                    List<User> users = dao.getUserByStudent(student_name,studio);
                    nick_name = users.get(0).getNick_name();
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                total_amount = line.getTotal_amount();
                left_amount = line.getLeft_amount();
                percent = (float) Math.round(left_amount * 100 / total_amount);
                id = line.getId();
                create_time = line.getCreate_time();
                points = line.getPoints();
                minus = line.getMinus();
                coins = line.getCoins();
                subject_get = line.getSubject();
                studio = line.getStudio();
                campus = line.getCampus();
                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("total_amount", total_amount);
                jsonObject.put("left_amount", left_amount);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("percent", percent);
                jsonObject.put("points", points);
                jsonObject.put("rank", i + 1);
                jsonObject.put("show", false);
                jsonObject.put("name", student_name);
                jsonObject.put("search", student_name);
                jsonObject.put("total_student", total_student);
                jsonObject.put("total_amount_all", total_amount_all);
                jsonObject.put("left_amount_all", left_amount_all);
                jsonObject.put("minus", minus);
                jsonObject.put("coins", coins);
                jsonObject.put("need_pay", need_pay);
                jsonObject.put("owe", owe);
                jsonObject.put("subject", subject_get);
                jsonObject.put("studio", studio);
                jsonObject.put("campus", campus);
                jsonObject.put("nick_name", nick_name);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getAllUserByStudio(String studio) {
        List<User> list = null;
        List<JSONObject> resul_list = new ArrayList<>();
        list = dao.getAllUserByStudio(studio);
        try {
            for (int i = 0; i < list.size(); i++) {
                String role_cn = null;
                String commentStyle_cn = null;
                JSONObject jsonObject = new JSONObject();
                User line = list.get(i);
                //获取字段
                studio = line.getStudio();
                String student_name = line.getStudent_name();
                String nick_name = line.getNick_name();
                String role = line.getRole();
                if("boss".equals(role)){
                    role_cn = "校长";
                }else if("teacher".equals(role)){
                    role_cn = "老师";
                }else if("client".equals(role)){
                    role_cn = "家长";
                }
                String comment_style = line.getComment_style();
                if("public".equals(comment_style)){
                    commentStyle_cn = "公开";
                }else if("self".equals(comment_style)){
                    commentStyle_cn = "私人";
                }

                String campus = line.getCampus();
                String expired_time = line.getExpired_time();
                String openid = line.getOpenid();
                String id = line.getId();
                String subjects = line.getSubjects();
                String member = line.getMember();

                //json
                jsonObject.put("studio", studio);
                jsonObject.put("student_name", student_name);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("role", role);
                jsonObject.put("role_cn", role_cn);
                jsonObject.put("comment_style", comment_style);
                jsonObject.put("commentStyle_cn", commentStyle_cn);
                jsonObject.put("campus", campus);
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("openid", openid);
                jsonObject.put("id", id);
                jsonObject.put("subjects", subjects);
                jsonObject.put("member", member);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getAllUserByStudioByPage(String studio, Integer page) {
        Integer page_start = (page - 1) * 20;
        Integer page_length = 20;
        List<User> list = null;
        List<JSONObject> resul_list = new ArrayList<>();
        list = dao.getAllUserByStudioByPage(studio,page_start,page_length);
        try {
            for (int i = 0; i < list.size(); i++) {
                String role_cn = null;
                String commentStyle_cn = null;
                JSONObject jsonObject = new JSONObject();
                User line = list.get(i);
                //获取字段
                studio = line.getStudio();
                String student_name = line.getStudent_name();
                String nick_name = line.getNick_name();
                String role = line.getRole();
                if("boss".equals(role)){
                    role_cn = "校长";
                }else if("teacher".equals(role)){
                    role_cn = "老师";
                }else if("client".equals(role)){
                    role_cn = "家长";
                }
                String comment_style = line.getComment_style();
                if("public".equals(comment_style)){
                    commentStyle_cn = "公开";
                }else if("self".equals(comment_style)){
                    commentStyle_cn = "私人";
                }

                String campus = line.getCampus();
                String expired_time = line.getExpired_time();
                String openid = line.getOpenid();
                String id = line.getId();
                String subjects = line.getSubjects();
                String member = line.getMember();

                //json
                jsonObject.put("studio", studio);
                jsonObject.put("student_name", student_name);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("role", role);
                jsonObject.put("role_cn", role_cn);
                jsonObject.put("comment_style", comment_style);
                jsonObject.put("commentStyle_cn", commentStyle_cn);
                jsonObject.put("campus", campus);
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("openid", openid);
                jsonObject.put("id", id);
                jsonObject.put("subjects", subjects);
                jsonObject.put("member", member);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getUserByNickStudio(String nick_name,String studio) {
        List<User> list = null;
        List<JSONObject> resul_list = new ArrayList<>();
        list = dao.getUserByNickStudio(nick_name,studio);
        try {
            for (int i = 0; i < list.size(); i++) {
                String role_cn = null;
                String commentStyle_cn = null;
                JSONObject jsonObject = new JSONObject();
                User line = list.get(i);
                //获取字段
                studio = line.getStudio();
                String student_name = line.getStudent_name();
                String role = line.getRole();
                if("boss".equals(role)){
                    role_cn = "校长";
                }else if("teacher".equals(role)){
                    role_cn = "老师";
                }else if("client".equals(role)){
                    role_cn = "家长";
                }
                String comment_style = line.getComment_style();
                if("public".equals(comment_style)){
                    commentStyle_cn = "公开";
                }else if("self".equals(comment_style)){
                    commentStyle_cn = "私人";
                }

                String campus = line.getCampus();
                String expired_time = line.getExpired_time();
                String openid = line.getOpenid();
                String id = line.getId();
                String subjects = line.getSubjects();
                String member = line.getMember();
                nick_name = line.getNick_name();
                studio = line.getStudio();

                //json
                jsonObject.put("studio", studio);
                jsonObject.put("student_name", student_name);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("role", role);
                jsonObject.put("role_cn", role_cn);
                jsonObject.put("comment_style", comment_style);
                jsonObject.put("commentStyle_cn", commentStyle_cn);
                jsonObject.put("campus", campus);
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("openid", openid);
                jsonObject.put("id", id);
                jsonObject.put("subjects", subjects);
                jsonObject.put("member", member);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }


    @Override
    public List getAnalyzeDetail(String studio, String dimension, String campus,String date_time) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            Date d = fmt.parse(date_time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            Integer weekDay_INIT = cal.get(Calendar.WEEK_OF_YEAR);
            for(int weekDay = weekDay_INIT; weekDay >= 1; weekDay--){
                JSONObject jsonObject = new JSONObject();
                Float signCount = 0.0f;
                Float tryCount = 0.0f;
                Float leaveCount = 0.0f;
                Float absentCount = 0.0f;
                Float lessonCount = 0.0f;
                Float weekPrice = 0.0f;
                jsonObject.put("weekDay", weekDay);
                try {
                    List<BookCount> list = dao.getAnalyzeSignUp(studio,campus,weekDay);
                    if(list.size()>0){
                        signCount = list.get(0).getIncome();
                        lessonCount = list.get(0).getExpenditure();
                        List<SignUp> signUps = dao.getAnalyzeSignUpDetail(studio,campus,weekDay);
                        for (int i = 0; i < signUps.size(); i++) {
                            SignUp signUp = signUps.get(i);
                            String student_name = signUp.getStudent_name();
                            String subject = signUp.getSubject();
                            Float count = signUp.getCount();
                            try {
                                List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                                if(lessons.size()>0){
                                    Float total_amount = lessons.get(0).getTotal_amount();
                                    Float price = lessons.get(0).getPrice();

                                    Float total_money = 0.0f;
                                    Float dis_money = 0.0f;
                                    List<LessonPackage> lessonPackages = dao.getLessonPackageByStudentSubject(student_name,studio,campus,subject);
                                    if(lessonPackages.size()>0){
                                        for (int j = 0; j < lessonPackages.size(); j++) {
                                            Float total_money_get = lessonPackages.get(j).getTotal_money();
                                            Float dis_money_get = lessonPackages.get(j).getDiscount_money();
                                            total_money = total_money + total_money_get;
                                            dis_money = dis_money + dis_money_get;
                                        }
                                    }

                                    if(total_money>0){
                                        price = (total_money - dis_money)/total_amount;
                                    }

                                    weekPrice = weekPrice + price*count;
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                } catch (Exception e) {
                throw new RuntimeException(e);
                }
                try {
                    List<BookCount> list1 = dao.getAnalyzeTry(studio,campus,weekDay);
                    tryCount = list1.get(0).getIncome();
                } catch (Exception e) {
//                throw new RuntimeException(e);
                }
                try {
                    List<BookCount> list2 = dao.getAnalyzeLeave(studio,campus,weekDay);
                    leaveCount = list2.get(0).getIncome();
                } catch (Exception e) {
//                throw new RuntimeException(e);
                }
                try {
                    List<BookCount> list3 = dao.getAnalyzeAbsent(studio,campus,weekDay);
                    absentCount = list3.get(0).getIncome();
                } catch (Exception e) {
//                throw new RuntimeException(e);
                }
                DecimalFormat df = new DecimalFormat("0.00");

                jsonObject.put("signCount", signCount);
                jsonObject.put("tryCount", tryCount);
                jsonObject.put("leaveCount", leaveCount);
                jsonObject.put("absentCount", absentCount);
                jsonObject.put("lessonCount", lessonCount);
                jsonObject.put("weekPrice", df.format(weekPrice));
                resul_list.add(jsonObject);
            }
        } catch (ParseException e) {
//            throw new RuntimeException(e);
        }


        return resul_list;
    }

    @Override
    public List getAnalyzeDetailWeek(String studio, String type, Integer weekday,String campus) {

        List<JSONObject> resul_list = new ArrayList<>();
        if("签到".equals(type)){
            List<SignUp> list = dao.getAnalyzeSignUpDetail(studio,campus,weekday);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                SignUp line = list.get(i);
                //获取字段
                String create_time = line.getCreate_time();
                String student_name = line.getStudent_name();
                String subject = line.getSubject();
                Float count = line.getCount();
                //json
                jsonObject.put("create_time", create_time.split(" ")[0]);
                jsonObject.put("student_name", student_name+"(课时:"+count+")");
                jsonObject.put("subject", subject);
                resul_list.add(jsonObject);
            }
        }else if("试听".equals(type)){
            List<Schedule> list = dao.getAnalyzeTryDetail(studio,campus,weekday);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Schedule line = list.get(i);
                //获取字段
                String create_time = line.getAdd_date();
                String student_name = line.getStudent_name();
                String subject = line.getSubject();
                //json
                jsonObject.put("create_time", create_time);
                jsonObject.put("student_name", student_name);
                jsonObject.put("subject", subject);
                resul_list.add(jsonObject);
            }
        }else if("请假".equals(type)){
            List<Leave> list = dao.getAnalyzeLeaveDetail(studio,campus,weekday);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Leave line = list.get(i);
                //获取字段
                String create_time = line.getDate_time();
                String student_name = line.getStudent_name();
                String subject = line.getSubject();
                //json
                jsonObject.put("create_time", create_time);
                jsonObject.put("student_name", student_name);
                jsonObject.put("subject", subject);
                resul_list.add(jsonObject);
            }
        }else if("旷课".equals(type)){
            List<Leave> list = dao.getAnalyzeAbsentDetail(studio,campus,weekday);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Leave line = list.get(i);
                //获取字段
                String create_time = line.getDate_time();
                String student_name = line.getStudent_name();
                String subject = line.getSubject();
                //json
                jsonObject.put("create_time", create_time);
                jsonObject.put("student_name", student_name);
                jsonObject.put("subject", subject);
                resul_list.add(jsonObject);
            }
        }

        return resul_list;
    }

    @Override
    public List getLessonByPage(String studio,String student_name,String subject,String campus,Integer page) {
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        String create_time = null;
        String id = null;
        String subject_get = null;
        Integer points = 0;
        Float percent = 0.0f;
        Float minus = 0.0f;
        Float coins = 0.0f;
        Float price = 0.0f;
        List<Lesson> list = null;
        Integer page_start = (page - 1) * 10;
        Integer page_length = 10;
        List<JSONObject> resul_list = new ArrayList<>();
        Integer length = student_name.split(",").length;
        Integer total_student =0;
        Float total_amount_all = 0.0f ;
        Float left_amount_all = 0.0f ;
        Integer need_pay = 0;
        Integer owe = 0;
        String campus_get = null;
        Integer is_combine = 0;

        try {
            if(subject.equals("全科目")){
                AllCount allCount =dao.getLessonAllCount(studio,campus);
                total_student = allCount.getStudent_count();
                total_amount_all = allCount.getTotal_amount();
                left_amount_all = allCount.getLeft_amount();
                need_pay = dao.getLessonNeedPayCount(studio,campus);
                owe = dao.getLessonOweCount(studio,campus);
            }else{
                AllCount allCount =dao.getLessonAllCountBySubject(studio,subject,campus);
                if(allCount.getStudent_count()>0){
                    total_student = allCount.getStudent_count();
                    total_amount_all = allCount.getTotal_amount();
                    left_amount_all = allCount.getLeft_amount();
                    need_pay = dao.getLessonNeedPayCountBySubject(studio,subject,campus);
                    owe = dao.getLessonOweCountBySubject(studio,subject,campus);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if(student_name.equals("all")) {
                if(subject.equals("全科目")){
                    list = dao.getLessonByPage(studio,campus,page_start,page_length);
                }else {
                    list = dao.getLessonBySubjectByPage(studio,subject,campus,page_start,page_length);
                }
            }else if (length>1) {
                if(subject.equals("全科目")){
                    list = dao.getLessonInName(studio,student_name,0,10000,campus);
                }else {
                    list = dao.getLessonInNameBySubject(studio,student_name,0,10000,subject,campus);
                }
            }else {
                if(subject.equals("全科目")){
                    list = dao.getLessonLikeName(studio,student_name,campus);
                }else {
                    list = dao.getLessonLikeNameBySubject(studio,student_name,subject,campus);
                }
            }

            for (int i = 0; i < list.size(); i++) {
                Float total_money = 0.0f ;
                Float discount_money = 0.0f ;
                String parent = "未绑定";
                String avatarurl = "未绑定";
                String phone_number = "未录入";
                JSONObject jsonObject = new JSONObject();
                Lesson line = list.get(i);

                //获取字段
                student_name = line.getStudent_name();

                try {
                    List<User> user = dao.getUserByStudent(student_name,studio);
                    if(user.size()>0){
                        parent = user.get(0).getNick_name();
                        avatarurl = user.get(0).getAvatarurl();
                        phone_number = user.get(0).getPhone_number();
                    }
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }

                total_amount = line.getTotal_amount();
                left_amount = line.getLeft_amount();
                percent = (float) Math.round(left_amount * 100 / total_amount);
                id = line.getId();
                create_time = line.getCreate_time();
                points = line.getPoints();
                minus = line.getMinus();
                coins = line.getCoins();
                subject_get = line.getSubject();
                studio = line.getStudio();
                campus_get = line.getCampus();
                is_combine = line.getIs_combine();
                price = line.getPrice();
                String final_time = line.getFinal_time();
                Float leave_times = line.getLeave_times();

                String combine = "分";
                if(is_combine == 1){
                    combine = "合";
                }

                try {
                    List<LessonPackage> lessonPackages = dao.getLessonPackage(student_name,studio,campus,subject_get);
                    if(lessonPackages.size()>0){
                        for(int j = 0; j < lessonPackages.size(); j++){
                            LessonPackage lessonPackage = lessonPackages.get(j);
                            total_money = total_money + lessonPackage.getTotal_money();
                            discount_money = discount_money + lessonPackage.getDiscount_money();
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                Float receipts = total_money - discount_money;
                Float re_price = receipts/total_amount;
                if(re_price>0){
                    price = re_price;
                }
                Float left_money = price * left_amount;
                if(total_money == 0.0f){
                    left_money = 0.0f;
                }

                DecimalFormat df = new DecimalFormat("0.00");

                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("total_amount", total_amount);
                jsonObject.put("left_amount", left_amount);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("percent", percent);
                jsonObject.put("points", points);
                jsonObject.put("rank", i + page_start + 1);
                jsonObject.put("show", false);
                jsonObject.put("name", student_name);
                jsonObject.put("search", student_name);
                jsonObject.put("total_student", total_student);
                jsonObject.put("total_amount_all", total_amount_all);
                jsonObject.put("left_amount_all", left_amount_all);
                jsonObject.put("minus", minus);
                jsonObject.put("coins", coins);
                jsonObject.put("need_pay", need_pay);
                jsonObject.put("owe", owe);
                jsonObject.put("subject", subject_get);
                jsonObject.put("parent", parent);
                jsonObject.put("studio", studio);
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("campus", campus_get);
                jsonObject.put("is_combine", combine);
                jsonObject.put("price",df.format(price));
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("total_money", df.format(total_money));
                jsonObject.put("discount_money", df.format(discount_money));
                jsonObject.put("receipts", df.format(receipts));
                jsonObject.put("left_money", df.format(left_money));
                jsonObject.put("final_time", final_time);
                jsonObject.put("leave_times", leave_times);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getTipsDataUrl(String studio,Integer left_amount_get,String subject,String campus_in) {
        Float total_amount = 0.0f;
        String create_time = null;
        String id = null;
        Integer points = 0;
        Float percent = 0.0f;
        Float minus = 0.0f;
        Float coins = 0.0f;
        Float left_amount = 0.0f;
        List<Lesson> list = null;
        String student_name =null;
        String campus =null;
        Integer is_combine = 0;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            if("全科目".equals(subject)){
                list = dao.getTipsDataUrlAll(studio,left_amount_get,campus_in);
            }else{
                list = dao.getTipsDataUrl(studio,left_amount_get,subject,campus_in);
            }
            for (int i = 0; i < list.size(); i++) {
                String parent = "未绑定";
                JSONObject jsonObject = new JSONObject();
                Lesson line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                try {
                    List<User> user = dao.getUserByStudent(student_name,studio);
                    if(user.size()>0){
                        parent = user.get(0).getNick_name();
                    }
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                total_amount = line.getTotal_amount();
                left_amount = line.getLeft_amount();
                percent = (float) Math.round(left_amount * 100 / total_amount);
                id = line.getId();
                create_time = line.getCreate_time();
                points = line.getPoints();
                minus = line.getMinus();
                coins = line.getCoins();
                subject = line.getSubject();
                campus =line.getCampus();
                is_combine = line.getIs_combine();
                String combine = "分";
                if(is_combine == 1){
                    combine = "合";
                }

                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("total_amount", total_amount);
                jsonObject.put("left_amount", left_amount);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("percent", percent);
                jsonObject.put("points", points);
                jsonObject.put("rank", i + 1);
                jsonObject.put("show", false);
                jsonObject.put("name", student_name);
                jsonObject.put("search", student_name);
                jsonObject.put("minus", minus);
                jsonObject.put("coins", coins);
                jsonObject.put("subject", subject);
                jsonObject.put("parent", parent);
                jsonObject.put("campus", campus);
                jsonObject.put("is_combine", combine);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getLessonInName(String studio, String student_name,Integer page,String subject,String campus) {
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        String create_time = null;
        String id = null;
        Integer points = 0;
        Float percent = 0.0f;
        Integer page_start = (page - 1) * 10;
        Integer page_length = 10;
        List<Lesson> list = null;
        List<Message> list_student = null;
        List<JSONObject> resul_list = new ArrayList<>();
        Integer length = student_name.split(",").length;
        String subject_get = null;

        try {
            if (length>1) {
                if("全科目".equals(subject)){
                    list = dao.getLessonInName(studio,student_name,page_start,page_length,campus);
                }else {
                    list = dao.getLessonInNameBySubject(studio,student_name,page_start,page_length,subject,campus);
                }

            }else {
                if("全科目".equals(subject)){
                    list = dao.getLessonLikeName(studio,student_name,campus);
                }else {
                    list = dao.getLessonLikeNameBySubject(studio,student_name,subject,campus);
                }

            }
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Lesson line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();

                byte[] photo = null;
                try {
                    list_student =dao.getStudentPhoto(student_name,studio,campus);
                    //获取图片
                    if(list_student.size()>0){
                        photo = list_student.get(0).getPhoto();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                total_amount = line.getTotal_amount();
                left_amount = line.getLeft_amount();
                percent = (float) Math.round(left_amount * 100 / total_amount);
                id = line.getId();
                create_time = line.getCreate_time();
                points = line.getPoints();
                subject_get = line.getSubject();
                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("total_amount", total_amount);
                jsonObject.put("left_amount", left_amount);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("percent", percent);
                jsonObject.put("points", points);
                jsonObject.put("rank", i + 1);
                jsonObject.put("show", false);
                jsonObject.put("name", student_name);
                jsonObject.put("search", student_name);
                jsonObject.put("photo", photo);
                jsonObject.put("subject_get", subject_get);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getGoodsList(String studio, Integer page,String campus) {
        String goods_name = null;
        String goods_intro = null;
        String create_time = null;
        String id = null;
        Float goods_price = 0.0f;
        Integer page_start = (page - 1) * 10;
        Integer page_length = 10;
        List<GoodsList> list = null;
        List<GroupBuy> list_buy = null;
        String uuids = null;
        byte[] photo = null;
        Integer is_group = 0;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            list = dao.getGoodsList(studio,page_start,page_length,campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                GoodsList line = list.get(i);
                //获取字段
                goods_name = line.getGoods_name();
                goods_intro = line.getGoods_intro();
                goods_price = line.getGoods_price();
                photo = line.getPhoto();
                id = line.getId();
                is_group = line.getIs_group();
                StringBuilder group = new StringBuilder(10);
                list_buy = dao.getGroupBuy(studio,id);
                for (int j = 0; j < list_buy.size(); j++) {
                    GroupBuy line_group = list_buy.get(j);
                    String nick_name = line_group.getNick_name();
                    group.append(nick_name+",");
                }
                create_time = line.getCreate_time();
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                photo = line.getPhoto();
                if(uuids != null){
                    photo = null;

                }

                //json
                jsonObject.put("goods_name", goods_name);
                jsonObject.put("goods_intro", goods_intro);
                jsonObject.put("goods_price", goods_price);
                jsonObject.put("create_time", create_time);
                jsonObject.put("photo", photo);
                jsonObject.put("id", id);
                jsonObject.put("group", group);
                jsonObject.put("uuids", uuids);
                jsonObject.put("is_group", is_group);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getGroupBuy(String studio,String goods_id) {
        String goods_name = null;
        String create_time = null;
        String id = null;
        String nick_name = null;
        String openid = null;
        List<GroupBuy> list = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            list = dao.getGroupBuy(studio,goods_id);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                GroupBuy line = list.get(i);
                //获取字段
                goods_name = line.getGoods_name();
                nick_name = line.getNick_name();
                openid = line.getOpenid();
                id = line.getId();
                create_time = line.getCreate_time();
                //json
                jsonObject.put("goods_name", goods_name);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("openid", openid);
                jsonObject.put("create_time", create_time);
                jsonObject.put("id", id);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getRating(String studio,String student_name,Integer page,String subject,String campus) {
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        String create_time = null;
        String id = null;
        Integer points = 0;
        Float percent = 0.0f;
        Integer page_start = (page - 1) * 10;
        Integer page_length = 10;
        List<Lesson> list = null;
        List<Message> list_student = null;
        List<JSONObject> resul_list = new ArrayList<>();
        String subject_get = null;
        if(student_name.equals("all")){
            try {
                if(subject.equals("全科目")){
                    list = dao.getRating(studio,page_start,page_length,campus);
                }else {
                    list = dao.getRatingBySubject(studio,page_start,page_length,subject,campus);
                }

                for (int i = 0; i < list.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    Lesson line = list.get(i);
                    //获取字段
                    student_name = line.getStudent_name();
                    byte[] photo = null;
                    total_amount = line.getTotal_amount();
                    left_amount = line.getLeft_amount();
                    percent = (float) Math.round(left_amount * 100 / total_amount);
                    id = line.getId();
                    create_time = line.getCreate_time();
                    points = line.getPoints();
                    subject_get = line.getSubject();
                    //json
                    jsonObject.put("student_name", student_name);
                    jsonObject.put("total_amount", total_amount);
                    jsonObject.put("left_amount", left_amount);
                    jsonObject.put("id", id);
                    jsonObject.put("create_time", create_time);
                    jsonObject.put("percent", percent);
                    jsonObject.put("points", points);
                    jsonObject.put("rank", i + page_start + 1);
                    jsonObject.put("photo", photo);
                    jsonObject.put("subject_get", subject_get);
                    resul_list.add(jsonObject);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                if(subject.equals("全科目")){
                    list = dao.getRatingByName(studio,student_name,page_start,page_length);
                }else {
                    list = dao.getRatingByNameBySubject(studio,student_name,page_start,page_length,subject);
                }

                for (int i = 0; i < list.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    Lesson line = list.get(i);
                    //获取字段
                    student_name = line.getStudent_name();
                    byte[] photo = null;
                    total_amount = line.getTotal_amount();
                    left_amount = line.getLeft_amount();
                    percent = (float) Math.round(left_amount * 100 / total_amount);
                    id = line.getId();
                    create_time = line.getCreate_time();
                    points = line.getPoints();
                    //json
                    jsonObject.put("student_name", student_name);
                    jsonObject.put("total_amount", total_amount);
                    jsonObject.put("left_amount", left_amount);
                    jsonObject.put("id", id);
                    jsonObject.put("create_time", create_time);
                    jsonObject.put("percent", percent);
                    jsonObject.put("points", points);
                    jsonObject.put("rank", i + 1);
                    jsonObject.put("photo", photo);
                    resul_list.add(jsonObject);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return resul_list;
    }


}
