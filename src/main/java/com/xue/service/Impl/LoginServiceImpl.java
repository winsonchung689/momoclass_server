package com.xue.service.Impl;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.xue.entity.model.*;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class LoginServiceImpl implements LoginService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserMapper dao;

    @Override
    public int push(Message message) {
        int result = 0;
        FileInputStream in = null;
        System.out.println(message);
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
        System.out.println(lesson);
        try {
            result = dao.insertLesson(lesson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateLesson(Lesson lesson,Float lessons_amount) {
        int result = 0;
        try {
            String student_name = lesson.getStudent_name();
            Float total = lesson.getTotal_amount();
            Float left = lesson.getLeft_amount();
            Float minus = lesson.getMinus();
            Float coins = lesson.getCoins();
            String studio = lesson.getStudio();
            Float total_amount = 0.0f;
            Float minus_amount = 0.0f;
            Float left_amount = 0.0f;
            Float coins_amount = 0.0f;
            if (student_name != null) {
                List<Lesson> lessons = dao.getLessonByName(student_name, studio);
                    if(lessons.size()>0){
                        Lesson lesson_get = lessons.get(0);
                        total_amount = lesson_get.getTotal_amount();
                        if (total > 0) {
                            total_amount = total;
                        }
                        left_amount = lesson_get.getLeft_amount();
                        if (left >= 0) {
                            left_amount = left;
                        }
                        minus_amount = lesson_get.getMinus();
                        if (minus >= 0) {
                            minus_amount = minus;
                        }
                        coins_amount = lesson_get.getCoins();
                        if (coins >= 0) {
                            coins_amount = coins;
                        }
                    }
            }
            lesson.setStudent_name(student_name);
            lesson.setTotal_amount(total_amount + lessons_amount);
            lesson.setLeft_amount(left_amount + lessons_amount);
            lesson.setMinus(minus_amount);
            lesson.setCoins(coins_amount);
            result = dao.updateLesson(lesson);
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
                photo = line.getPhoto();
                class_target = line.getClass_target();
				studio = line.getStudio();
                duration = line.getDuration();
                positive = line.getPositive();
                discipline = line.getDiscipline();
                happiness = line.getHappiness();

                jsonObject.put("isHide",true);
                List<User> user = dao.getUserByStudent(student_name,studio);
                if (user.size()>0){
                    jsonObject.put("isHide",false);
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
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getDetailsUrlByDate(String studio, String duration, String student_name, String date_time) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String class_name = null;
        String class_target = null;
        String id = null;
        Integer positive = 0 ;
        Integer discipline = 0;
        Integer happiness = 0;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getDetailsUrlByDate(studio,duration,student_name,date_time);
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

                jsonObject.put("isHide",true);
                List<User> user = dao.getUserByStudent(student_name,studio);
                if (user.size()>0){
                    jsonObject.put("isHide",false);
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
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getSearch(String student_name, String studio,Integer page) {
        String comment = null;
        String class_name = null;
        String class_target = null;
        String id = null;
        String create_time = null;
        Integer page_start = (page - 1) * 7;
        Integer page_length = 7;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getSearch(student_name, studio,page_start,page_length);
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
                    List<Lesson> lessons = dao.getLessonByName(student_name, studio);
                    Lesson lesson = lessons.get(0);
                    left = lesson.getLeft_amount();
                    total = lesson.getTotal_amount();
                    if (left > 0 || total > 0) {
                        percent = left * 100 / total;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
    public List getSignUp(String student_name, String studio) {
        String create_time = null;
        String sign_time = null;
        String id = null;
        String mark = null;
        String duration = null;
        Float count = 0.0f;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<SignUp> list = dao.getSignUp(student_name, studio);
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

                SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
                Date create_time_dt = df1.parse(create_time.substring(0,10));
                Date sign_time_dt = df1.parse(sign_time.substring(0,10));
                int compare = sign_time_dt.compareTo(create_time_dt);
                if (compare == 0) {
                    jsonObject.put("status", "正常签到");
                } else if(compare > 0){
                    jsonObject.put("status", "补签到");
                } else if(compare < 0){
                    jsonObject.put("status", "提前签到");
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
    public List getLeaveRecord(String student_name, String studio) {
        String create_time = null;
        String date_time = null;
        String duration=null;
        String id = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Leave> list = dao.getLeaveRecord(student_name, studio);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Leave line = list.get(i);
                //获取字段
                create_time = line.getCreate_time();
                date_time = line.getDate_time();
                duration = line.getDuration();
                id = line.getId();

                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("create_time", create_time);
                jsonObject.put("date_time", date_time);
                jsonObject.put("duration", duration);
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
    public List getArrangement(String studio,Integer dayofweek,String date) {
        String class_number = null;
        String duration = null;
        String limits = null;
        byte[] photo = null;
        String id = null;
        Integer dayofweek_by= 0;
        List<JSONObject> resul_list = new ArrayList<>();
        Integer classes_count =0;
        Integer sign_count =0;
        if(dayofweek==7){
            dayofweek_by=1;
        }else {
            dayofweek_by = dayofweek + 1;
        }

        try {
            List<Arrangement> list = dao.getArrangement(studio,dayofweek.toString());
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Arrangement line = list.get(i);
                //获取字段
                class_number = line.getClass_number();
                duration = line.getDuration();
                limits = line.getLimits();
                photo = line.getPhoto();
                id = line.getId();
                classes_count = dao.getLessonAllCountByDay(studio,dayofweek_by,duration,class_number);
                try {
                    sign_count = dao.getSignUpCountByDay(studio,date+" 00:00:00",duration,class_number);
                } catch (Exception e) {
//                    e.printStackTrace();
                }

                //json
                jsonObject.put("class_number", class_number);
                jsonObject.put("duration", duration);
                jsonObject.put("limits", limits);
                jsonObject.put("photo", photo);
                jsonObject.put("classes_count", classes_count);
                jsonObject.put("dayofweek",dayofweek);
                jsonObject.put("id",id);
                jsonObject.put("sign_count",sign_count);
                resul_list.add(jsonObject);
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
        System.out.println(schedule);
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
        System.out.println(signUp);
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
        System.out.println(arrangement);
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
    public List getSchedule(String date_time, String studio) {
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
        Date d = null;
        String class_number = null;
        Integer weekDay=0;
        Integer weekofday=0;
        String mark = null;


        // 获取常规学生
        try {
            d = fmt.parse(date_time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            weekDay = cal.get(Calendar.DAY_OF_WEEK);
            if(weekDay==1){
                weekofday=7;
            }else {
                weekofday = weekDay - 1;
            }

            List<Schedule> list = dao.getSchedule(weekDay, studio);
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

                List<Arrangement> arrangements = dao.getClassNumber(studio,weekofday,duration);
                if(arrangements.size()>0){
                    Arrangement arrangement = arrangements.get(0);
                    class_number = arrangement.getClass_number();
                    jsonObject.put("class_number", class_number);
                }else{
                    jsonObject.put("class_number","临时加课");
                }

                jsonObject.put("comment_status", "课评");
                jsonObject.put("comment_color", "rgb(157, 162, 165)");
                List<Message> messages = dao.getCommentByDate(student_name,studio,date_time);
                if (messages.size()>=1){
                    if(messages.get(0).getDuration().equals("00:00-00:00")){
                        jsonObject.put("comment_status", "已课评");
                        jsonObject.put("comment_color", "rgba(162, 106, 214, 0.849)");
                    }else {
                        List<Message> messagesDuration = dao.getCommentByDateDuration(student_name, studio, date_time, duration);
                        if (messagesDuration.size() == 1) {
                            jsonObject.put("comment_status", "已课评");
                            jsonObject.put("comment_color", "rgba(162, 106, 214, 0.849)");
                        }
                    }
                }

                //json
                List<Lesson> lessons = dao.getLessonByName(student_name, studio);
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
                    List<SignUp> signUps = dao.getSignUpByDate(student_name,studio,date_time + " 00:00:00");
                    if(signUps.size()>=1){
                        if(signUps.get(0).getDuration().equals("00:00-00:00")){
                            jsonObject.put("sign_up", "已签到");
                            jsonObject.put("sign_color", "rgba(55, 188, 221, 0.849)");
                            mark = signUps.get(0).getMark();
                            jsonObject.put("mark", mark);

                        }else {
                            List<SignUp> signUpsDuration = dao.getSignUpByDateDuration(student_name,studio,date_time+" 00:00:00",duration);
                            if(signUpsDuration.size()==1){
                                jsonObject.put("sign_up", "已签到");
                                jsonObject.put("sign_color", "rgba(55, 188, 221, 0.849)");
                                mark = signUpsDuration.get(0).getMark();
                                jsonObject.put("mark", mark);
                            }
                        }
                    }

                    jsonObject.put("leave", "请假");
                    List<Leave> leaves = dao.getLeaveByDateDuration(student_name,studio,date_time,duration);
                    if(leaves.size()==1){
                        jsonObject.put("leave", "已请假");
                        jsonObject.put("leave_color", "rgb(218, 144, 84)");
                    }

                    resul_list.add(jsonObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 获取插班生
        try {
            List<Schedule> list = dao.getTransfer(date_time, studio);
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

                List<Arrangement> arrangements = dao.getClassNumber(studio,weekofday,duration);
                if(arrangements.size()>0){
                    Arrangement arrangement = arrangements.get(0);
                    class_number = arrangement.getClass_number();
                    jsonObject.put("class_number", class_number+"(插班生)");
                }else{
                    jsonObject.put("class_number","临时加课(插班生)");
                }

                jsonObject.put("comment_status", "课评");
                jsonObject.put("comment_color", "rgb(157, 162, 165)");
                List<Message> messages = dao.getCommentByDate(student_name,studio,date_time);
                if (messages.size()>=1){
                    if(messages.get(0).getDuration().equals("00:00-00:00")){
                        jsonObject.put("comment_status", "已课评");
                        jsonObject.put("comment_color", "rgba(162, 106, 214, 0.849)");
                    }else {
                        List<Message> messagesDuration = dao.getCommentByDateDuration(student_name, studio, date_time, duration);
                        if (messagesDuration.size() == 1) {
                            jsonObject.put("comment_status", "已课评");
                            jsonObject.put("comment_color", "rgba(162, 106, 214, 0.849)");
                        }
                    }
                }

                //json
                List<Lesson> lessons = dao.getLessonByName(student_name, studio);
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
                    List<SignUp> signUps = dao.getSignUpByDate(student_name,studio,date_time + " 00:00:00");
                    if(signUps.size()>=1){
                        if(signUps.get(0).getDuration().equals("00:00-00:00")){
                            jsonObject.put("sign_up", "已签到");
                            jsonObject.put("sign_color", "rgba(55, 188, 221, 0.849)");
                            mark = signUps.get(0).getMark();
                            jsonObject.put("mark", mark);

                        }else {
                            List<SignUp> signUpsDuration = dao.getSignUpByDateDuration(student_name,studio,date_time+" 00:00:00",duration);
                            if(signUpsDuration.size()==1){
                                jsonObject.put("sign_up", "已签到");
                                jsonObject.put("sign_color", "rgba(55, 188, 221, 0.849)");
                                mark = signUpsDuration.get(0).getMark();
                                jsonObject.put("mark", mark);
                            }
                        }
                    }

                    jsonObject.put("leave", "请假");
                    List<Leave> leaves = dao.getLeaveByDateDuration(student_name,studio,date_time,duration);
                    if(leaves.size()==1){
                        jsonObject.put("leave", "已请假");
                        jsonObject.put("leave_color", "rgb(218, 144, 84)");
                    }
                    resul_list.add(jsonObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getScheduleDetail(Integer weekDay, String duration, String studio,String class_number) {
        String age = null;
        String student_name = null;
        String id = null;
        List<JSONObject> resul_list = new ArrayList<>();
        Integer status = 0;
        String status_str = "待确认";

        // 获取常规学生
        try {
            List<Schedule> list = dao.getScheduleDetail(weekDay,duration,studio,class_number);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Schedule line = list.get(i);

                //获取字段
                age = line.getAge();
                student_name = line.getStudent_name();
                duration = line.getDuration();
                id = line.getId();
                status = line.getStatus();
                if(status == 1){
                    status_str = "已确认";
                }else {
                    status_str = "待确认";
                }

                jsonObject.put("age", age);
                jsonObject.put("student_name", student_name);
                jsonObject.put("duration", duration);
                jsonObject.put("id", id);
                jsonObject.put("status", status);
                jsonObject.put("status_str", status_str);

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

            if ("boss".equals(role) && studio_get.equals(studio)) {
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
    public int deleteArrangement(Integer id,String role,String studio,String openid) {
        try {
            List<User> list = dao.getUser(openid);
            String studio_get = list.get(0).getStudio();
            if ("boss".equals(role) && studio_get.equals(studio)) {
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
    public int changeClassName(Integer id, String role, String studio, String openid,String class_number) {
        try {
            List<User> list = dao.getUser(openid);
            String studio_get = list.get(0).getStudio();
            if ("boss".equals(role) && studio_get.equals(studio)) {
                dao.changeClassName(id,studio,class_number);
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

            if ("boss".equals(role) && studio_get.equals(studio)) {
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

            if ("boss".equals(role) && studio_get.equals(studio)) {
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

            if ("boss".equals(role) && studio_get.equals(studio)) {
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

            if ("boss".equals(role) && studio_get.equals(studio)) {
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
    public int confirmSchedule(Integer id, String role, String studio, String openid) {
        try {
            List<User> list = dao.getUser(openid);
            String studio_get = list.get(0).getStudio();

            if ("boss".equals(role) && studio_get.equals(studio)) {
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
    public int deleteLesson(Integer id, String role,String studio,String openid) {
        try {
            List<User> list = dao.getUser(openid);
            String studio_get = list.get(0).getStudio();

            if ("boss".equals(role) && studio_get.equals(studio)) {
                dao.deleteLesson(id,studio);
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
        FileInputStream in = null;
        System.out.println(user);
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
        String theme = null;
        List<User> list= null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getUser(openid);
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
        List<User> list= null;
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
                jsonObject.put("comment_style",comment_style);
                jsonObject.put("openid",openid);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getStudio() {
        String studio = null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {

            List<User> list = dao.getStudio();
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
    public List getArrangements(String studio) {
        String dayofweek = null;
        String class_number = null;
        String duration = null;
        List<String> resul_list = new ArrayList<>();
        try {

            List<Arrangement> list = dao.getArrangements(studio);
            for (int i = 0; i < list.size(); i++) {
                Arrangement line = list.get(i);
                //获取字段
                dayofweek = line.getDayofweek();
                class_number = line.getClass_number();
                duration = line.getDuration();


                String item = "星期"+dayofweek+ "," + class_number + "," + duration;

                //json
                resul_list.add(item);
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
    public List getFrameModel(String studio,Integer page) {
        byte[] photo = null;
        String class_name = null;
        String id = null;
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
            List<Message> list = dao.getFrameModel(studio,page_start,page_length);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                id = line.getId();
                photo = line.getPhoto();
                class_name =line.getClass_name();

                //json
                jsonObject.put("id", id);
                jsonObject.put("photo", photo);
                jsonObject.put("class_name", class_name);
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
    public List getAdvertise(String studio) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String student_name = null;
        String class_name = null;
        String class_target = null;
        String id = null;
        String create_time = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getAdvertise(studio);
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
    public List getClassSys(String class_target, String studio,Integer page) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String student_name = null;
        String class_name = null;
        String id = null;
        String create_time = null;
        Integer page_start = (page - 1) * 1;
        Integer page_length = 1;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getClassSys(class_target, studio,page_start,page_length);
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
    public List getHome(String studio) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String student_name = null;
        String class_name = null;
        String class_target = null;
        String id = null;
        String create_time = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getHome(studio);
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
    public int updateMinusLesson(String student_name, String studio,Float class_count) {
        int result = 0;
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        Float new_left = 0.0f;
        Float minus = 0.0f;
        Float coins = 0.0f;
        System.out.println(student_name);

        List<Lesson> list = dao.getLessonByName(student_name, studio);
        try {
            for (int i = 0; i < list.size(); i++) {
                Lesson line = list.get(i);
                total_amount = line.getTotal_amount();
                left_amount = line.getLeft_amount();
                new_left = left_amount - class_count;
                minus = line.getMinus();
                coins = line.getCoins();
                Lesson lesson = new Lesson();
                lesson.setStudent_name(student_name);
                lesson.setLeft_amount(new_left);
                lesson.setTotal_amount(total_amount);
                lesson.setStudio(studio);
                lesson.setMinus(minus);
                lesson.setCoins(coins);
                result = dao.updateLesson(lesson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateAddPoints(String student_name, String studio,Integer points_int) {
        int result = 0;
        Integer points = 0;
        Integer new_points = 0;

        List<Lesson> list = dao.getLessonByName(student_name, studio);
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
                result = dao.updateLessonPoint(lesson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    @Override
    public int deletePoints(String student_name, String studio,Integer points) {
        int result = 0;
        System.out.println(student_name);
        try {
            List<Lesson> lessons = dao.getLessonByName(student_name, studio);
            Lesson lesson_get = lessons.get(0);
            Integer total_points = lesson_get.getPoints();
            Integer new_points = total_points-points;

            Lesson lesson = new Lesson();
            lesson.setStudent_name(student_name);
            lesson.setPoints(new_points);
            lesson.setStudio(studio);
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
    public List getLessonByName(String student_name, String studio) {
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        List<JSONObject> resul_list = new ArrayList<>();
        try {

            List<Lesson> list = dao.getLessonByName(student_name, studio);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Lesson line = list.get(i);
                //获取字段
                total_amount = line.getTotal_amount();
                left_amount = line.getLeft_amount();
                //json
                jsonObject.put("total_amount", total_amount);
                jsonObject.put("left_amount", left_amount);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }


    @Override
    public List getMessage(String studio, Integer page,String comment_style,String openid,String role) {
        String comment = null;
        String class_name = null;
        String class_target = null;
        String id = null;
        String create_time = null;
        byte[] photo = null;
        String student_name_get = null;
        String student_name = null;
        Integer page_start = (page - 1) * 7;
        Integer page_length = 7;
        List<JSONObject> resul_list = new ArrayList<>();
        List<Message> list=null;
        List<User> users=null;
        String duration = null;
        Integer positive = 0;
        Integer discipline = 0;
        Integer happiness = 0;
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
                list = dao.getMessageInName(student_names.toString(),studio,page_start,page_length);
            }else {
                list = dao.getMessage(studio, page_start, page_length);
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

                    try {
                        List<Lesson> lessons = dao.getLessonByName(student_name, studio);
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
                    resul_list.add(jsonObject);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getGrowthRecord(String studio, Integer page, String student_name) {
        String comment = null;
        String id = null;
        String create_time = null;
        byte[] photo = null;
        Integer page_start = (page - 1) * 3;
        Integer page_length = 3;
        List<JSONObject> resul_list = new ArrayList<>();
        List<Message> list=null;
        String duration = null;
        String class_name = null;

        try {
            list = dao.getMessageGrowth(student_name,studio,page_start,page_length);
            if(list.size()>0){
                for (int i = 0; i < list.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    Message line = list.get(i);
                    //获取字段
                    comment = line.getComment();
                    id = line.getId();
                    create_time = line.getCreate_time();
                    duration = line.getDuration();
                    photo = line.getPhoto();
                    class_name = line.getClass_name();

                    //json
                    jsonObject.put("comment", comment);
                    jsonObject.put("id", id);
                    jsonObject.put("create_time", create_time.substring(0,10));
                    jsonObject.put("duration", duration);
                    jsonObject.put("photo", photo);
                    jsonObject.put("class_name", class_name);
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
    public List getModel(String studio, Integer page) {
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
            List<Message> list = dao.getModel(studio, page_start, page_length);
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
    public List getLesson(String studio,String student_name) {
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        String create_time = null;
        String id = null;
        Integer points = 0;
        Float percent = 0.0f;
        Float minus = 0.0f;
        Float coins = 0.0f;
        List<Lesson> list = null;
//        List<Message> list_student = null;
        List<JSONObject> resul_list = new ArrayList<>();
        Integer length = student_name.split(",").length;
        Integer total_student =0;
        Integer need_pay = 0;
        Integer owe = 0;
        try {
            total_student =dao.getLessonAllCount(studio);
            need_pay = dao.getLessonNeedPayCount(studio);
            owe = dao.getLessonOweCount(studio);
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            if(student_name.equals("all")) {
                    list = dao.getLesson(studio);
            }else if (length>1) {
                    list = dao.getLessonInName(studio,student_name,0,10000);
            }else {
                    list = dao.getLessonLikeName(studio,student_name);
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Lesson line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();

                total_amount = line.getTotal_amount();
                left_amount = line.getLeft_amount();
                percent = (float) Math.round(left_amount * 100 / total_amount);
                id = line.getId();
                create_time = line.getCreate_time();
                points = line.getPoints();
                minus = line.getMinus();
                coins = line.getCoins();
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
                jsonObject.put("minus", minus);
                jsonObject.put("coins", coins);
                jsonObject.put("need_pay", need_pay);
                jsonObject.put("owe", owe);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getTipsDataUrl(String studio,Integer left_amount_get) {
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
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getTipsDataUrl(studio,left_amount_get);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Lesson line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                total_amount = line.getTotal_amount();
                left_amount = line.getLeft_amount();
                percent = (float) Math.round(left_amount * 100 / total_amount);
                id = line.getId();
                create_time = line.getCreate_time();
                points = line.getPoints();
                minus = line.getMinus();
                coins = line.getCoins();
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
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getLessonInName(String studio, String student_name,Integer page) {
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        String create_time = null;
        String id = null;
        Integer points = 0;
        Float percent = 0.0f;
        Integer page_start = (page - 1) * 4;
        Integer page_length = 4;
        List<Lesson> list = null;
        List<Message> list_student = null;
        List<JSONObject> resul_list = new ArrayList<>();
        Integer length = student_name.split(",").length;

        try {
            if (length>1) {
                list = dao.getLessonInName(studio,student_name,page_start,page_length);
            }else {
                list = dao.getLessonLikeName(studio,student_name);
            }
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Lesson line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();

                byte[] photo = null;
                try {
                    list_student =dao.getStudentPhoto(student_name,studio);
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
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getRating(String studio,String student_name,Integer page) {
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        String create_time = null;
        String id = null;
        Integer points = 0;
        Float percent = 0.0f;
        Integer page_start = (page - 1) * 3;
        Integer page_length = 3;
        List<Message> list_student = null;
        List<JSONObject> resul_list = new ArrayList<>();
        if(student_name.equals("all")){
            try {
                List<Lesson> list = dao.getRating(studio,page_start,page_length);
                for (int i = 0; i < list.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    Lesson line = list.get(i);
                    //获取字段
                    student_name = line.getStudent_name();

                    byte[] photo = null;
//                    try {
//                        list_student =dao.getStudentPhoto(student_name,studio);
//                        //获取图片
//                        if(list_student.size()>0){
//                            photo = list_student.get(0).getPhoto();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }

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
                    jsonObject.put("rank", i + page_start + 1);
                    jsonObject.put("photo", photo);
                    resul_list.add(jsonObject);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                List<Lesson> list = dao.getRatingByName(studio,student_name,page_start,page_length);
                for (int i = 0; i < list.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    Lesson line = list.get(i);
                    //获取字段
                    student_name = line.getStudent_name();

                    byte[] photo = null;
//                    try {
//                        list_student =dao.getStudentPhoto(student_name,studio);
//                        //获取图片
//                        if(list_student.size()>0){
//                            photo = list_student.get(0).getPhoto();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }

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
