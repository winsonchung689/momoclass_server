package com.xue.service.Impl;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
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
            if("全科目".equals(subject_new) && student_name != null){
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
        String positive = null ;
        String discipline = null;
        String happiness = null;
        String mp3_url=null;
        String uuids=null;
        String uuids_c=null;
        String vuuid=null;
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
                String positve_item = "积极性";
                String[] positive_list = positive.split("_");
                if(positive_list.length>1){
                    positve_item = positive_list[0];
                    positive = positive_list[1];
                }

                discipline = line.getDiscipline();
                String discipline_item = "纪律性";
                String[] discipline_list = discipline.split("_");
                if(discipline_list.length>1){
                    discipline_item = discipline_list[0];
                    discipline = discipline_list[1];
                }

                happiness = line.getHappiness();
                String happiness_item = "开心值";
                String[] happiness_list = happiness.split("_");
                if(happiness_list.length>1){
                    happiness_item = happiness_list[0];
                    happiness = happiness_list[1];
                }

                mp3_url=line.getMp3_url();
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                try {
                    vuuid = line.getVuuid().replace("\"","").replace("[","").replace("]","");
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
                jsonObject.put("positve_item", positve_item);
                jsonObject.put("discipline", discipline);
                jsonObject.put("discipline_item", discipline_item);
                jsonObject.put("happiness", happiness);
                jsonObject.put("happiness_item", happiness_item);
                jsonObject.put("mp3_url", mp3_url);
                jsonObject.put("uuids", uuids);
                jsonObject.put("uuids_c", uuids_c);
                jsonObject.put("vuuid", vuuid);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getDetailsUrlByDate(String studio, String duration, String student_name, String date_time,String openid,String class_target_bak) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String class_name = null;
        String id = null;
        String positive = null ;
        String discipline = null;
        String happiness = null;
        String mp3_url = null;
        String uuids = null;
        String uuids_c=null;
        String vuuid=null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<User> list_user = dao.getUser(openid);
            String campus = list_user.get(0).getCampus();
            List<Message> list = dao.getDetailsUrlByDate(studio,duration,student_name,date_time,campus,class_target_bak);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                class_name = line.getClass_name();
                comment = line.getComment();
                String class_target = line.getClass_target();
                studio = line.getStudio();
                duration = line.getDuration();

                positive = line.getPositive();
                String positve_item = "积极性";
                String[] positive_list = positive.split("_");
                if(positive_list.length>1){
                    positve_item = positive_list[0];
                    positive = positive_list[1];
                }

                discipline = line.getDiscipline();
                String discipline_item = "纪律性";
                String[] discipline_list = discipline.split("_");
                if(discipline_list.length>1){
                    discipline_item = discipline_list[0];
                    discipline = discipline_list[1];
                }

                happiness = line.getHappiness();
                String happiness_item = "开心值";
                String[] happiness_list = happiness.split("_");
                if(happiness_list.length>1){
                    happiness_item = happiness_list[0];
                    happiness = happiness_list[1];
                }

                id = line.getId();
                mp3_url=line.getMp3_url();
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }

                try {
                    vuuid = line.getVuuid().replace("\"","").replace("[","").replace("]","");
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
                jsonObject.put("positve_item", positve_item);
                jsonObject.put("discipline", discipline);
                jsonObject.put("discipline_item", discipline_item);
                jsonObject.put("happiness", happiness);
                jsonObject.put("happiness_item", happiness_item);
                jsonObject.put("mp3_url", mp3_url);
                jsonObject.put("uuids", uuids);
                jsonObject.put("uuids_c", uuids_c);
                jsonObject.put("vuuid", vuuid);
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
    public List getSignUp(String student_name, String studio,String subject,String openid) {
        String create_time = null;
        String sign_time = null;
        String id = null;
        String mark = null;
        String duration = null;
        Float count = 0.0f;
        Integer ending_status_get = 0;
        List<JSONObject> resul_list = new ArrayList<>();
        String title = "序号,学生名,科目,上课日,时间段,签到日,备注,课时,状态,结课";
        List<String> data_list = new ArrayList<>();

        try {
            List<User> user_get= dao.getUser(openid);
            String campus = user_get.get(0).getCampus();
            List<SignUp> list = dao.getSignUp(student_name, studio,subject,campus);
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
                ending_status_get = line.getEnding_status();
                String ending_status = "未结";
                if(ending_status_get == 1){
                    ending_status = "已结";
                }

                SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
                Date create_time_dt = df1.parse(create_time.substring(0,10));
                Date sign_time_dt = df1.parse(sign_time.substring(0,10));
                int compare = sign_time_dt.compareTo(create_time_dt);
                String status = "正常签";
                if(compare > 0){
                    status = "补签";
                } else if(compare < 0){
                    status = "提前签";
                }
                int rank = i+1;
                //json
                jsonObject.put("id", id);
                jsonObject.put("student_name", student_name);
                jsonObject.put("create_time", create_time.substring(0,10));
                jsonObject.put("sign_time", sign_time.substring(0,10));
                jsonObject.put("rank", rank);
                jsonObject.put("mark", mark);
                jsonObject.put("duration", duration);
                jsonObject.put("count", count);
                jsonObject.put("subject", subject);
                jsonObject.put("status", status);
                jsonObject.put("ending_status", ending_status);
                resul_list.add(jsonObject);

                String data_line = rank + "," + student_name + "," + subject + "," + create_time.substring(0,10) + "," + duration + "," + sign_time.substring(0,10) + "," +mark + "," +count + "," + status + "," + ending_status;
                data_list.add(data_line);
            }
            downloadByOpenid(studio,openid,data_list,title,"single_sign");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getSignUpByAll(String studio, String openid) {
        String create_time = null;
        String sign_time = null;
        String id = null;
        String mark = null;
        String duration = null;
        Float count = 0.0f;
        Integer ending_status_get = 0;
        List<JSONObject> resul_list = new ArrayList<>();
        String title = "序号,学生名,科目,上课日,时间段,签到日,备注,课时,状态,结课";
        List<String> data_list = new ArrayList<>();

        try {
            List<User> user_get= dao.getUser(openid);
            String campus = user_get.get(0).getCampus();
            List<SignUp> list = dao.getSignUpByAll(studio,campus);
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
                String student_name = line.getStudent_name();
                String subject = line.getSubject();
                ending_status_get = line.getEnding_status();
                String ending_status = "未结";
                if(ending_status_get == 1){
                    ending_status = "已结";
                }

                SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
                Date create_time_dt = df1.parse(create_time.substring(0,10));
                Date sign_time_dt = df1.parse(sign_time.substring(0,10));
                int compare = sign_time_dt.compareTo(create_time_dt);
                String status = "正常签";
                if(compare > 0){
                    status = "补签";
                } else if(compare < 0){
                    status = "提前签";
                }
                int rank = i+1;
                //json
                jsonObject.put("id", id);
                jsonObject.put("student_name", student_name);
                jsonObject.put("create_time", create_time.substring(0,10));
                jsonObject.put("sign_time", sign_time.substring(0,10));
                jsonObject.put("rank", rank);
                jsonObject.put("mark", mark);
                jsonObject.put("duration", duration);
                jsonObject.put("count", count);
                jsonObject.put("subject", subject);
                jsonObject.put("status", status);
                jsonObject.put("ending_status", ending_status);
                resul_list.add(jsonObject);

                String data_line = rank + "," + student_name + "," + subject + "," + create_time.substring(0,10) + "," + duration + "," + sign_time.substring(0,10) + "," +mark + "," +count + "," + status + "," + ending_status;
                data_list.add(data_line);
            }
            downloadByOpenid(studio,openid,data_list,title,"all_sign");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getSignUpByDateDuration(String student_name,String studio,String date_time,String duration,String campus,String subject) {
        String id = null;
        String openid = null;
        String subscription = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<SignUp> list = dao.getSignUpByDateDuration(student_name,studio,date_time,duration,campus,subject);
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
    public List getLeaveRecord(String student_name, String studio,String leave_type,String subject,String campus) {
        String create_time = null;
        String date_time = null;
        String duration=null;
        String id = null;
        String mark_leave = null;
        String student_get = null;
        String makeup_date = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Leave> list = dao.getLeaveRecord(student_name, studio,leave_type,subject,campus);
            if("all".equals(student_name)){
                list = dao.getLeaveRecordAll(student_name, studio,leave_type,subject,campus);
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
        Integer dayofweek_by= 0;
        List<JSONObject> resul_list = new ArrayList<>();
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
        Integer is_open = user_get.get(0).getIs_open();
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
                String class_number = null;
                String duration = null;
                String limits = "0";
                byte[] photo = null;
                String id = null;
                Integer sign_count =0;
                StringBuffer teachers = new StringBuffer();
                StringBuffer all_teachers = new StringBuffer();
                String student_string = null;
                Integer classes_count =0;
                Integer uncomfirmed_count = 0;
                Integer remind = 0;
                String remind_name="否";
                String chooseLesson="未选";
                JSONObject jsonObject = new JSONObject();
                Arrangement line = list.get(i);
                //获取字段
                class_number = line.getClass_number();
                duration = line.getDuration();
                limits = line.getLimits();
                photo = line.getPhoto();
                id = line.getId();
                subject = line.getSubject();


                remind = dao.getScheduleRemind(studio,dayofweek_by,duration,class_number,subject);
                if(remind == null){
                    remind = 0;
                }
                if(remind == 1 ){
                    remind_name = "是";
                }

                classes_count = dao.getLessonAllCountByDay(studio,dayofweek_by,duration,class_number,subject,campus);

                try {
                    String lessons = user_get.get(0).getLessons();
                    String lesson_string = null;
                    lesson_string = "星期" + dayofweek + "," + subject + "," + class_number + "," + duration;
                    if(lessons != null){
                        String[] list_1 =lessons.split("\\|");
                        List<String> list_2 = Arrays.asList(list_1);
                        if(list_2.contains(lesson_string)){
                            chooseLesson = "已选";
                        }
                    }

                    List<User> teacher_user = dao.getUserByChooseLesson(lesson_string,studio);
                    if(teacher_user != null){
                        for(int t = 0;t < teacher_user.size(); t++){
                            String nick_name_get = teacher_user.get(t).getNick_name();
                            String openid_get =teacher_user.get(t).getOpenid();
                            teachers.append(nick_name_get + "|" + openid_get);
                            teachers.append(",");
                        }
                    }


                    List<User> all_teacher_user = dao.getBossByStudio(studio);
                    if(all_teacher_user != null){
                        for(int tt = 0;tt < all_teacher_user.size(); tt++){
                            String nick_name_all = all_teacher_user.get(tt).getNick_name();
                            String openid_all =all_teacher_user.get(tt).getOpenid();
                            all_teachers.append(nick_name_all + "|" + openid_all);
                            all_teachers.append(",");
                        }
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(is_open == 1 || "boss".equals(role)){
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
                            jsonObject.put("classes_count_all_not",classes_count_all_lesson - classes_count_all);
                            jsonObject.put("uncomfirmed_count",uncomfirmed_count);
                            jsonObject.put("student_string",student_string);
                            jsonObject.put("remind",remind);
                            jsonObject.put("remind_name",remind_name);
                            jsonObject.put("teachers",teachers);
                            jsonObject.put("all_teachers",all_teachers);
                            jsonObject.put("chooseLesson",chooseLesson);
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
                        jsonObject.put("classes_count_all_not",classes_count_all_lesson - classes_count_all);
                        jsonObject.put("uncomfirmed_count",uncomfirmed_count);
                        jsonObject.put("student_string",student_string);
                        jsonObject.put("remind",remind);
                        jsonObject.put("remind_name",remind_name);
                        jsonObject.put("teachers",teachers);
                        jsonObject.put("all_teachers",all_teachers);
                        jsonObject.put("chooseLesson",chooseLesson);
                        resul_list.add(jsonObject);
                    }
                }else if("teacher".equals(role) && is_open == 0 && "已选".equals(chooseLesson)){
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
                            jsonObject.put("classes_count_all_not",classes_count_all_lesson - classes_count_all);
                            jsonObject.put("uncomfirmed_count",uncomfirmed_count);
                            jsonObject.put("student_string",student_string);
                            jsonObject.put("remind",remind);
                            jsonObject.put("remind_name",remind_name);
                            jsonObject.put("teachers",teachers);
                            jsonObject.put("all_teachers",all_teachers);
                            jsonObject.put("chooseLesson",chooseLesson);
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
                        jsonObject.put("classes_count_all_not",classes_count_all_lesson - classes_count_all);
                        jsonObject.put("uncomfirmed_count",uncomfirmed_count);
                        jsonObject.put("student_string",student_string);
                        jsonObject.put("remind",remind);
                        jsonObject.put("remind_name",remind_name);
                        jsonObject.put("teachers",teachers);
                        jsonObject.put("all_teachers",all_teachers);
                        jsonObject.put("chooseLesson",chooseLesson);
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
    public List getTodaySchedule(String studio, Integer dayofweek, String date_time, String subject, String openid) {

        List<JSONObject> resul_list = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        List<User> user_get= dao.getUser(openid);
        String campus = user_get.get(0).getCampus();

        Calendar cal = Calendar.getInstance();
        String end_time = df.format(cal.getTime());
        cal.add(Calendar.DATE,-6);
        String start_time = df.format(cal.getTime());


        try {
            long timestamp_start = df.parse(start_time).getTime();
            long timestamp_end = df.parse(end_time).getTime();
            while(timestamp_end >= timestamp_start){
                JSONObject jsonObject = new JSONObject();
                Date date = new Date(timestamp_end);
                String dateString = df.format(date);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timestamp_end);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                List<Schedule> schedules = dao.getScheduleAllDistinct(dayOfWeek,studio,campus);
                StringBuffer schedule_status = new StringBuffer();
                for(int i=0;i<schedules.size();i++){
                    Schedule schedule = schedules.get(i);
                    String duration = schedule.getDuration();
                    String class_number = schedule.getClass_number();
                    subject = schedule.getSubject();

                    int weekDayChoose = 0;
                    if(dayOfWeek == 1){
                        weekDayChoose = 7;
                    }else {
                        weekDayChoose = dayOfWeek -1;
                    }

                    String chooseLesson = "星期"+  weekDayChoose + "," + subject + "," + class_number + "," + duration ;
                    List<User> users = dao.getUserByChooseLesson(chooseLesson,studio);
                    for(int j=0;j<users.size();j++){
                        User user = users.get(j);
                        String openid_get = user.getOpenid();
                        if(openid_get.equals(openid)){
                            int classes_count = dao.getLessonAllCountByDay(studio,dayOfWeek,duration,class_number,subject,campus);
                            int sign_count = dao.getSignUpCountByDay(studio,dateString+" 00:00:00",duration,class_number,campus,subject);
                            int loss = classes_count - sign_count;
                            String result = class_number + ":" + loss + "人未签" ;

                            if(sign_count< classes_count){
                                schedule_status.append(result);
                                schedule_status.append(",");
                            }
                        }
                    }
                }

                if(schedule_status.length()>0) {
                    schedule_status = schedule_status.deleteCharAt(schedule_status.lastIndexOf(","));
                }

                jsonObject.put("dateString", dateString);
                jsonObject.put("schedule_status", schedule_status);
                resul_list.add(jsonObject);

                timestamp_end = timestamp_end - 60*60*24*1000;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getClassStudent(String studio, String campus, String type, String subject,String date_time) {
        List<JSONObject> resul_list = new ArrayList<>();
        List<Lesson> lessons = null;
        if("月试听".equals(type)){
            List<Schedule> schedules = dao.getTryDetailByMonthStudent(studio,date_time.substring(0,7),campus);
            if(schedules.size()>0){
                for (int i = 0; i < schedules.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    Schedule schedule = schedules.get(i);
                    String student_name = schedule.getStudent_name();
                    String subject_get = schedule.getSubject();
                    String campus_get = schedule.getCampus();

                    if("全科目".equals(subject) || subject_get.equals(subject)){
                        jsonObject.put("student_name",student_name);
                        jsonObject.put("subject", subject_get);
                        jsonObject.put("campus", campus_get);
                        jsonObject.put("total_amount", 0);
                        jsonObject.put("left_amount", 0);
                        resul_list.add(jsonObject);
                    }
                }
            }
        }else {
            if("全科目".equals(subject)){
                lessons = dao.getLesson(studio,campus);
            }else{
                lessons = dao.getLessonBySubject(studio,subject,campus);
            }

            for (int i = 0; i < lessons.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Lesson lesson = lessons.get(i);
                String student_name = lesson.getStudent_name();
                String subject_get = lesson.getSubject();
                String studio_get = lesson.getStudio();
                String campus_get = lesson.getCampus();
                Float total_amount = lesson.getTotal_amount();
                Float left_amount = lesson.getLeft_amount();
                if("已排课".equals(type) || "未排课".equals(type)){
                    List<Schedule> schedules = dao.getScheduleByStudent(studio_get,campus_get,subject_get,student_name);
                    if("已排课".equals(type) && schedules.size()>0){
                        jsonObject.put("student_name",student_name);
                        jsonObject.put("subject", subject_get);
                        jsonObject.put("campus", campus_get);
                        jsonObject.put("total_amount", total_amount);
                        jsonObject.put("left_amount", left_amount);
                        resul_list.add(jsonObject);

                    }else if("未排课".equals(type) && schedules.size() == 0){
                        jsonObject.put("student_name",student_name);
                        jsonObject.put("subject", subject_get);
                        jsonObject.put("campus", campus_get);
                        jsonObject.put("total_amount", total_amount);
                        jsonObject.put("left_amount", left_amount);
                        resul_list.add(jsonObject);
                    }
                }else if("月耗课".equals(type)){
                    List<SignUp> signs = dao.getSignUpDetailByMonthStudent(student_name,studio,date_time.substring(0,7),campus,subject_get);
                    if(signs.size()>0){
                        Float counts = 0.0f;
                        for (int j = 0; j < signs.size(); j++) {
                            Float count = signs.get(j).getCount();
                            counts = counts + count;
                        }

                        jsonObject.put("student_name",student_name);
                        jsonObject.put("subject", subject_get+"("+ counts +")");
                        jsonObject.put("campus", campus_get);
                        jsonObject.put("total_amount", total_amount);
                        jsonObject.put("left_amount", left_amount);
                        resul_list.add(jsonObject);
                    }
                }else if("月请假".equals(type)){
                    List<Leave> leaves = dao.getLeaveDetailByMonthStudent(student_name,studio,date_time.substring(0,7),campus,subject_get);
                    if(leaves.size()>0){
                        Float counts = 0.0f;
                        for (int j = 0; j < leaves.size(); j++) {
                            counts = counts + 1;
                        }
                        jsonObject.put("student_name",student_name);
                        jsonObject.put("subject", subject_get+"("+ counts +")");
                        jsonObject.put("campus", campus_get);
                        jsonObject.put("total_amount", total_amount);
                        jsonObject.put("left_amount", left_amount);
                        resul_list.add(jsonObject);
                    }
                }

            }

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
        List<JSONObject> resul_list = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat fmt_m = new SimpleDateFormat("yyyy-MM");
        Date d = null;
        String class_number = null;
        Integer weekDay=0;
        Integer weekofday=0;
        String mark = null;
        Float sign_counts=0.0f;
        Float sign_counts_get=0.0f;
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
                    List<Message> messages = dao.getCommentByDate(student_name, studio, date_time,campus,"课评");
                    if (messages.size() >= 1) {
                        if (messages.get(0).getDuration().equals("00:00-00:00")) {
                            jsonObject.put("comment_status", "已课评");
                            jsonObject.put("comment_color", "rgba(162, 106, 214, 0.849)");
                        } else {
                            List<Message> messagesDuration = dao.getCommentByDateDuration(student_name, studio, date_time, duration,campus,"课评");
                            if (messagesDuration.size() == 1) {
                                jsonObject.put("comment_status", "已课评");
                                jsonObject.put("comment_color", "rgba(162, 106, 214, 0.849)");
                            }
                        }
                    }

                    //json
                    List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                    Float left = 0.0f;
                    Float total = 0.0f;
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
                        List<SignUp> signUps = dao.getSignUpByDate(student_name, studio, date_time + " 00:00:00",campus,subject);
                        if (signUps.size() >= 1) {
                            if (signUps.get(0).getDuration().equals("00:00-00:00")) {
                                jsonObject.put("sign_up", "已签到");
                                jsonObject.put("sign_color", "rgba(55, 188, 221, 0.849)");
                                mark = signUps.get(0).getMark();
                                jsonObject.put("mark", mark);

                            } else {
                                List<SignUp> signUpsDuration = dao.getSignUpByDateDuration(student_name, studio, date_time + " 00:00:00", duration,campus,subject);
                                if (signUpsDuration.size() == 1) {
                                    jsonObject.put("sign_up", "已签到");
                                    jsonObject.put("sign_color", "rgba(55, 188, 221, 0.849)");
                                    mark = signUpsDuration.get(0).getMark();
                                    jsonObject.put("mark", mark);
                                }
                            }
                        }

                        jsonObject.put("leave", "请假");
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
                List<Message> messages = dao.getCommentByDate(student_name,studio,date_time,campus,"课评");
                if (messages.size()>=1){
                    if(messages.get(0).getDuration().equals("00:00-00:00")){
                        jsonObject.put("comment_status", "已课评");
                        jsonObject.put("comment_color", "rgba(162, 106, 214, 0.849)");
                    }else {
                        List<Message> messagesDuration = dao.getCommentByDateDuration(student_name, studio, date_time, duration,campus,"课评");
                        if (messagesDuration.size() == 1) {
                            jsonObject.put("comment_status", "已课评");
                            jsonObject.put("comment_color", "rgba(162, 106, 214, 0.849)");
                        }
                    }
                }

                //json
                List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                Float left = 0.0f;
                Float total = 0.0f;
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
                    List<SignUp> signUps = dao.getSignUpByDate(student_name,studio,date_time + " 00:00:00",campus,subject);
                    if(signUps.size()>=1){
                        if(signUps.get(0).getDuration().equals("00:00-00:00")){
                            jsonObject.put("sign_up", "已签到");
                            jsonObject.put("sign_color", "rgba(55, 188, 221, 0.849)");
                            mark = signUps.get(0).getMark();
                            jsonObject.put("mark", mark);

                        }else {
                            List<SignUp> signUpsDuration = dao.getSignUpByDateDuration(student_name,studio,date_time+" 00:00:00",duration,campus,subject);
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
    public List getTodayClasses(String date_time, String studio, String openid) {
        Date d = null;
        Integer weekDay=0;

        List<User> list_user = dao.getUserByOpenid(openid);
        String campus = list_user.get(0).getCampus();
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            d = fmt.parse(date_time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            weekDay = cal.get(Calendar.DAY_OF_WEEK);
            Integer weekofday=0;
            if(weekDay==1){
                weekofday=7;
            }else {
                weekofday = weekDay - 1;
            }

            List<Arrangement> list = dao.getArrangementByDay(studio,weekofday,campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Arrangement line = list.get(i);
                //获取字段
                String dayofweek = line.getDayofweek();
                String class_number = line.getClass_number();
                String duration = line.getDuration();
                String subject = line.getSubject();

                StringBuilder student_names = new StringBuilder();
                StringBuilder student_arranges = new StringBuilder();
                for (int j = 0; j < list_user.size(); j++) {
                    User user = list_user.get(j);
                    String student_name = user.getStudent_name();
                    List<Schedule> check_schedule = dao.getScheduleCheck(date_time,duration,class_number,subject,studio,campus,student_name);
                    if(check_schedule.size() >= 1){
                        student_names = student_names.append(student_name).append(",");
                    }

                    List<Schedule> check_arrange = dao.getScheduleCheckArrangement(weekDay,duration,class_number,subject,studio,campus,student_name);
                    if(check_arrange.size() >= 1){
                        student_arranges = student_arranges.append(student_name).append(",");
                    }
                }
                if(student_names.length()>0){
                    student_names = student_names.deleteCharAt(student_names.lastIndexOf(","));
                }
                if(student_arranges.length()>0){
                    student_arranges = student_arranges.deleteCharAt(student_arranges.lastIndexOf(","));
                }

                jsonObject.put("dayofweek", dayofweek);
                jsonObject.put("duration", duration);
                jsonObject.put("class_number", class_number);
                jsonObject.put("subject", subject);
                jsonObject.put("student_names", student_names);
                jsonObject.put("student_arranges", student_arranges);
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
        Float sign_counts=0.0f;
        Integer try_counts=0;
        Integer leave_counts=0;
        Float sign_counts_get=0.0f;
        Integer try_counts_get=0;
        Integer leave_counts_get=0;
        Integer remind=0;
        String campus = null;
        String lessons_string = null;
        JSONObject jsonObject_1 = new JSONObject();
        try {
            List<User> list_user = dao.getUser(openid);
            if(list_user.size()>0){
                campus = list_user.get(0).getCampus();
                lessons_string = list_user.get(0).getLessons();
            }

            if(subject.equals("全科目")){
                sign_counts_get = dao.getSignUpByMonthAll(studio, date_time.substring(0,7),campus);
                try_counts_get = dao.getTryByMonthAll(studio, date_time.substring(0,7),campus);
                leave_counts_get = dao.getLeaveByMonthAll(studio, date_time.substring(0,7),campus);
            }else {
                sign_counts_get = dao.getSignUpByMonth(studio, subject,date_time.substring(0,7),campus);
                try_counts_get = dao.getTryByMonth(studio,subject, date_time.substring(0,7),campus);
                leave_counts_get = dao.getLeaveByMonth(studio,subject, date_time.substring(0,7),campus);
            }

            if(sign_counts_get!=null){
                sign_counts = sign_counts_get;
            }
            if(try_counts_get!=null){
                try_counts = try_counts_get;
            }
            if(sign_counts_get!=null){
                leave_counts = leave_counts_get;
            }
            jsonObject_1.put("sign_counts", sign_counts);
            jsonObject_1.put("try_counts", try_counts);
            jsonObject_1.put("leave_counts", leave_counts);
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
                        if(lessons_string != null){
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
                        e.printStackTrace();
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

                jsonObject.put("student_type", student_type);
                jsonObject.put("subject", subject);
                if("transferred".equals(student_type)){
                    class_number = class_number+"(插班生)";
                }
                jsonObject.put("class_number", class_number);

                jsonObject.put("comment_status", "课评");
                jsonObject.put("comment_color", "rgb(157, 162, 165)");
                List<Message> messages = dao.getCommentByDate(student_name, studio, date_time,campus,"课评");
                if (messages.size() >= 1) {
                    if (messages.get(0).getDuration().equals("00:00-00:00")) {
                        jsonObject.put("comment_status", "已课评");
                        jsonObject.put("comment_color", "rgba(162, 106, 214, 0.849)");
                    } else {
                        List<Message> messagesDuration = dao.getCommentByDateDuration(student_name, studio, date_time, duration,campus,"课评");
                        if (messagesDuration.size() == 1) {
                            jsonObject.put("comment_status", "已课评");
                            jsonObject.put("comment_color", "rgba(162, 106, 214, 0.849)");
                        }
                    }
                }

                jsonObject.put("homework_status", "作业");
                jsonObject.put("homework_color", "rgb(157, 162, 165)");
                List<Message> homeworks = dao.getCommentByDate(student_name, studio, date_time,campus,"课后作业");
                if (homeworks.size() >= 1) {
                    List<Message> homeworksDuration = dao.getCommentByDateDuration(student_name, studio, date_time, duration,campus,"课后作业");
                    if (homeworksDuration.size() == 1) {
                        jsonObject.put("homework_status", "已发");
                        jsonObject.put("homework_color", "rgba(162, 106, 214, 0.849)");
                    }
                }

                //json
                List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                Float left = 0.0f;
                Float total = 0.0f;
                if (lessons.size() > 0) {
                    Lesson lesson = lessons.get(0);
                    left = lesson.getLeft_amount();
                    total = lesson.getTotal_amount();
                    String final_time = lesson.getFinal_time();
                    Float leave_times = lesson.getLeave_times();
                    Float minus = lesson.getMinus();

                    jsonObject.put("minus", minus);
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
                    List<SignUp> signUps = dao.getSignUpByDate(student_name, studio, date_time + " 00:00:00",campus,subject);
                    if (signUps.size() >= 1) {
                        if (signUps.get(0).getDuration().equals("00:00-00:00")) {
                            jsonObject.put("sign_up", "已签到");
                            jsonObject.put("sign_color", "rgba(55, 188, 221, 0.849)");
                            mark = signUps.get(0).getMark();
                            jsonObject.put("mark", mark);

                        } else {
                            List<SignUp> signUpsDuration = dao.getSignUpByDateDuration(student_name, studio, date_time + " 00:00:00", duration,campus,subject);
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

                    jsonObject.put("leave", "请假");
                    List<Leave> leaves = dao.getLeaveByDateDuration(student_name, studio, date_time, duration);
                    if (leaves.size() == 1) {
                        String leave_type = leaves.get(0).getLeave_type();
                        jsonObject.put("leave", "已请假");
                        if (leave_type.equals("旷课")) {
                            jsonObject.put("leave", "已旷课");
                        }
                        jsonObject.put("leave_color", "rgb(218, 144, 84)");
                    }else if(leaves.size() > 1){
                        String leave_type = leaves.get(0).getLeave_type();
                        jsonObject.put("leave", "重复请假");
                        if (leave_type.equals("旷课")) {
                            jsonObject.put("leave", "重复旷课");
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

                Float left_amount = 0.0f;
                Float minus = 0.0f;
                List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                if(lessons.size()>0){
                    Lesson lesson = lessons.get(0);
                    left_amount = lesson.getLeft_amount();
                    minus = lesson.getMinus();
                }


                jsonObject.put("age", age);
                jsonObject.put("student_name", student_name);
                jsonObject.put("duration", duration);
                jsonObject.put("id", id);
                jsonObject.put("status", status);
                jsonObject.put("status_str", status_str);
                jsonObject.put("student_count", student_count);
                jsonObject.put("student_classes", student_classes*4);
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
            List<Message> list = dao.getUuidById(id);
            String class_target_bak = list.get(0).getClass_target_bak();
            String studio_get = list.get(0).getStudio();
            if("冲刺".equals(class_target_bak) || "网课".equals(class_target_bak) || "同城".equals(class_target_bak) ||"数学".equals(class_target_bak) || "英语".equals(class_target_bak) || "语文".equals(class_target_bak) || "视频站".equals(class_target_bak)){
                dao.deleteComment(id,studio);

                // 删除视频
                try {
                    String d_path = "/data/uploadVideo/592796c45de54f5c5ba4/" ;
                    File temp = new File(d_path, uuid);
                    temp.delete();
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
            }

            if("课评".equals(class_target_bak) || "环境".equals(class_target_bak) || "课程体系".equals(class_target_bak) || "广告".equals(class_target_bak)){
                String uuids = list.get(0).getUuids().replace("\"","").replace("[","").replace("]","");
                String[] result = uuids.split(",");
                List<String> list_new = new ArrayList<>();
                for(int i =0;i<result.length;i++){
                    if(!result[i].equals(uuid)){
                        list_new.add(result[i]);
                    }
                }
                dao.updateUuids(id,studio,list_new.toString().replace(" ",""));

                // 删除图片
                try {
                    String d_path = "/data/uploadimages/" ;
                    File temp = new File(d_path, uuid);
                    temp.delete();
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }

            }else if ("课后作业".equals(class_target_bak)){
                String uuids = list.get(0).getUuids_c().replace("\"","").replace("[","").replace("]","");
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
            Integer dayofweek =  Integer.parseInt(arrangement.getDayofweek());
            if(dayofweek==7){
                dayofweek=1;
            }else {
                dayofweek = dayofweek + 1;
            }


            if ( studio_get.equals(studio)) {
                if(change_title.equals("班号")){
                    dao.changeClassName(id1,studio,class_number);
                    dao.changeScheduleClassName(old_class_number,studio,duration,class_number,old_subject,campus,dayofweek);
                    dao.changeSignUpClassName(old_class_number,studio,duration,class_number,old_subject,campus);
                }else if(change_title.equals("科目")){
                    dao.changeSubjectName(id1,studio,class_number);
                    dao.changeScheduleSubject(old_subject,studio,duration,class_number,old_class_number,campus,dayofweek);
//                    dao.changeSignUpSubject(old_subject,studio,duration,class_number,old_class_number,campus);
                }else if(change_title.equals("上限")){
                    dao.changeLimit(id1,studio,limit_number);
                }else if(change_title.equals("时间")){
                    dao.changeDuration(id1,studio,limit_number);
                    dao.changeScheduleDuration(old_class_number,studio,duration,limit_number,old_subject,campus,dayofweek);
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
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        String today_time = df.format(new Date());
        try {
            List<User> list = dao.getUser(openid);
            String studio_get = list.get(0).getStudio();
            List<Lesson> lessons = dao.getLessonById(id);
            Lesson lesson = lessons.get(0);
            String subject = lesson.getSubject();
            String campus = lesson.getCampus();

            if ( studio_get.equals(studio)) {
                dao.deleteLesson(id,studio,today_time);
                dao.deleteScheduleBySubject(student_name,studio,subject,campus);
                dao.deleteLessonPackageByName(student_name,studio,subject,campus);
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
    public int insertRestaurantUser(RestaurantUser restaurantUser) {
        int result = 0;
        try {
            result = dao.insertRestaurantUser(restaurantUser);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }

    @Override
    public int insertBookUser(BookUser bookUser) {
        int result = 0;
        try {
            result = dao.insertBookUser(bookUser);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }

    @Override
    public int updateUser(User user) {
        int result = 0;
        try {
            String openid = user.getOpenid();
            String nick_name = user.getNick_name();
            String studio = user.getStudio();
            String role = user.getRole();
//            String md5 = DigestUtils.md5Hex(nick_name + studio);
            if(!"请录入工作室".equals(studio) && !"boss".equals(role) && !"teacher".equals(role)){
                result = dao.updateUser(user);
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
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        String role = null;
        String student_name = null;
        String avatarurl = null;
        String nick_name = null;
        String studio = null;
        String user_type = null;
        String create_time = null;
        String expired_time = null;
        Float coins = 0.0f;
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
        Integer hours =null;
        String remind_type =null;
        String official_openid = null;
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
                StringBuilder sb = new StringBuilder();
                sb.append(student_name);

                if(!openid.equals("all")){
                    List<User> users = dao.getUserByOpenid(openid);
                    for(int ii=0;ii< users.size();ii++){
                        String student_get = users.get(ii).getStudent_name();
                        if(!"no_name".equals(student_get) && !student_name.equals(student_get)){
                            sb.append(".");
                            sb.append(student_get);
                        }
                    }
                }

                String id = line.getId();
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
                hours = line.getHours();
                remind_type = line.getRemind_type();
                official_openid = line.getOfficial_openid();
                Float read_times = line.getRead_times();
                String expired_time_ad = line.getExpired_time_ad();
                String city = line.getCity();
                Integer is_exchange = line.getIs_exchange();
                Integer is_teacher = line.getIs_teacher();
                Integer is_square = line.getIs_square();
                String subject = line.getSubject();

                String today_time = df.format(new Date());
                Date today_dt = df.parse(today_time.substring(0,10));
                Date expired_time_ad_dt = df.parse(expired_time_ad.substring(0,10));
                int compare = today_dt.compareTo(expired_time_ad_dt);
                jsonObject.put("is_show_ad", "false");
                if(compare>0){
                    jsonObject.put("is_show_ad", "true");
                }

                jsonObject.put("official_status", "未关注");
                if(official_openid != null){
                    jsonObject.put("official_status", "已关注");
                }

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
                jsonObject.put("sb", sb);
                jsonObject.put("hours", hours);
                jsonObject.put("remind_type", remind_type);
                jsonObject.put("read_times", read_times);
                jsonObject.put("id", id);
                jsonObject.put("expired_time_ad", expired_time_ad);
                jsonObject.put("city", city);
                jsonObject.put("subject", subject);
                jsonObject.put("is_teacher", is_teacher);
                jsonObject.put("is_exchange", is_exchange);
                jsonObject.put("is_square", is_square);
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
        Float coins = 0.0f;
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
                String role_cn = null;
                JSONObject jsonObject = new JSONObject();
                User line = list.get(i);
                //获取字段
                role = line.getRole();
                if("boss".equals(role)){
                    role_cn = "校长";
                }else if("teacher".equals(role)){
                    role_cn = "老师";
                }else if("client".equals(role)){
                    role_cn = "家长";
                }
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
                jsonObject.put("role_cn", role_cn);
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
    public List getRestaurantUser(String openid) {
        String role = null;
        String avatarurl = null;
        String nick_name = null;
        String restaurant = null;
        String user_type = null;
        String create_time = null;
        String expired_time = null;
        String subjects = null;
        List<RestaurantUser> list= null;
        int id = 0;
        String logo = null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getRestaurantUser(openid);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                RestaurantUser line = list.get(i);
                //获取字段
                role = line.getRole();
                avatarurl = line.getAvatarurl();
                nick_name = line.getNick_name();
                restaurant = line.getRestaurant();
                create_time = line.getCreate_time();
                expired_time = line.getExpired_time();
                openid = line.getOpenid();
                logo = line.getLogo();
                id = line.getId();
                String role_name = "普通";
                if("boss".equals(role)){
                    role_name = "群主";
                }

                //json
                jsonObject.put("id", id);
                jsonObject.put("role", role);
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("restaurant", restaurant);
                jsonObject.put("user_type", user_type);
                jsonObject.put("create_time", create_time);
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("openid",openid);
                jsonObject.put("logo",logo);
                jsonObject.put("role_name",role_name);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getBookUser(String openid) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");//设置日期格式
        String date_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

        List<BookUser> list= null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getBookUser(openid);
            for (int i = 0; i < list.size(); i++) {
                Float budget = 0.0f;
                JSONObject jsonObject = new JSONObject();
                BookUser line = list.get(i);
                //获取字段
                String role = line.getRole();
                String avatarurl = line.getAvatarurl();
                String nick_name = line.getNick_name();
                String create_time = line.getCreate_time();
                String expired_time = line.getExpired_time();
                String book_name = line.getBook_name();
                budget = line.getBudget();
                openid = line.getOpenid();
                String logo = line.getLogo();
                int id = line.getId();
                String role_name = "普通会员";
                if("boss".equals(role)){
                    role_name = "永久会员";
                }
                Integer consume = 0;

                //json
                jsonObject.put("id", id);
                jsonObject.put("role", role);
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("create_time", create_time);
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("openid",openid);
                jsonObject.put("logo",logo);
                jsonObject.put("role_name",role_name);
                jsonObject.put("book_name",book_name);
                jsonObject.put("budget",budget);
                jsonObject.put("consume",consume);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getBBookDetail(String openid,String date_time,String book_name) {
        List<BookDetail> list= null;
        List<JSONObject> resul_list = new ArrayList<>();
        Integer consume = 0;

        try {
            consume = dao.getBookSumByMonth(openid,book_name,date_time.substring(0,7));
            if(consume == null){
                consume = 0;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("consume", consume);
            resul_list.add(jsonObject);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            list = dao.getBBookDetail(openid,date_time,book_name);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                BookDetail line = list.get(i);
                //获取字段
                String type = line.getType();
                String item = line.getItem();
                String mark = line.getMark();
                Float amount = line.getAmount();
                String create_time = line.getCreate_time();
                String id = line.getId();

                //json
                jsonObject.put("type", type);
                jsonObject.put("item", item);
                jsonObject.put("mark", mark);
                jsonObject.put("amount", amount);
                jsonObject.put("create_time", create_time);
                jsonObject.put("openid", openid);
                jsonObject.put("id", id);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getRestaurantUserAll(String restaurant) {
        String role = null;
        String avatarurl = null;
        String nick_name = null;
        String user_type = null;
        String create_time = null;
        String expired_time = null;
        List<RestaurantUser> list= null;
        int id = 0;
        String logo = null;
        String openid = null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            if("all".equals(restaurant)){
                list= dao.getRestaurantUserAll();
            }else {
                list = dao.getRestaurantUserByShop(restaurant);
            }
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                RestaurantUser line = list.get(i);
                //获取字段
                role = line.getRole();
                avatarurl = line.getAvatarurl();
                nick_name = line.getNick_name();
                restaurant = line.getRestaurant();
                create_time = line.getCreate_time();
                expired_time = line.getExpired_time();
                openid = line.getOpenid();
                logo = line.getLogo();
                id = line.getId();
                String role_name = "普通";
                if("boss".equals(role)){
                    role_name = "群主";
                }

                //json
                jsonObject.put("id", id);
                jsonObject.put("role", role);
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("restaurant", restaurant);
                jsonObject.put("user_type", user_type);
                jsonObject.put("create_time", create_time);
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("openid",openid);
                jsonObject.put("logo",logo);
                jsonObject.put("role_name",role_name);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getRestaurantOrder(String openid, String type) {
        List<RestaurantOrder> list= null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<RestaurantUser> restaurantUser = dao.getRestaurantUser(openid);
            String restaurant = restaurantUser.get(0).getRestaurant();

            if("my".equals(type)){
                list = dao.getRestaurantOrderByOpenid(openid);
            } else if ("all".equals(type)) {
                list = dao.getRestaurantOrderByShop(restaurant);

            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                RestaurantOrder line = list.get(i);
                //获取字段
                String food_name = line.getFood_name();
                restaurant = line.getRestaurant();
                String category = line.getCategory();
                int num = line.getNum();
                Float price = line.getPrice();
                String create_time = line.getCreate_time();
                int status_get = line.getStatus();
                String status = "未完成";
                if(status_get==1){
                    status = "已完成";
                }
                String openid_get = line.getOpenid();
                List<RestaurantUser> restaurantUser_get = dao.getRestaurantUser(openid_get);
                String nick_name = restaurantUser_get.get(0).getNick_name();
                String id = line.getId();
                Float total_price = num * price ;

                //json
                jsonObject.put("food_name", food_name);
                jsonObject.put("restaurant", restaurant);
                jsonObject.put("category", category);
                jsonObject.put("num", num);
                jsonObject.put("price", price);
                jsonObject.put("create_time", create_time);
                jsonObject.put("status", status);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("id", id);
                jsonObject.put("total_price", total_price);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getRestaurantCategory(String restaurant) {
        List<Menu> list= null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getRestaurantCategory(restaurant);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Menu line = list.get(i);
                //获取字段
                String category = line.getCategory();

                //json
                jsonObject.put("category", category);
                jsonObject.put("rank", i);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getRestaurantMenu(String restaurant) {
        List<Menu> list= null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getRestaurantMenu(restaurant);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Menu line = list.get(i);
                //获取字段
                String category = line.getCategory();
                String food_name = line.getFood_name();
                String food_image = line.getFood_image();
                String introduce = line.getIntroduce();
                Float price = line.getPrice();
                String id = line.getId();

                //json
                jsonObject.put("category", category);
                jsonObject.put("food_name", food_name);
                jsonObject.put("food_image", food_image);
                jsonObject.put("introduce", introduce);
                jsonObject.put("price", price);
                jsonObject.put("id", id);
                jsonObject.put("num", 0);
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
        Float coins = 0.0f;
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
                if(studio.length() > 0){
                    //json
                    jsonObject.put("studio", studio);
                    jsonObject.put("show", false);
                    jsonObject.put("name", studio);
                    jsonObject.put("search", studio);
                    resul_list.add(jsonObject);
                }
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
    public List getArrangements(String studio,String campus) {
        String dayofweek = null;
        String class_number = null;
        String duration = null;
        String subject = null;
        List<String> resul_list = new ArrayList<>();
        try {
            List<Arrangement> list = dao.getArrangements(studio,campus);
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
    public List getArrangementsByDate(String studio, String date_time,String campus) {
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

            List<Arrangement> list = dao.getArrangements(studio,campus);
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
        Float group_price = 0.0f;
        Integer group_num = 0;
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
                String goods_id = line.getGoods_id();

                List<GoodsList> goodsLists = dao.getGoodsListById(goods_id);
                if(goodsLists.size()>0){
                    GoodsList goodsList = goodsLists.get(0);
                    goods_name = goodsList.getGoods_name();
                    goods_price = goodsList.getGoods_price();
                    group_price = goodsList.getGroup_price();
                    group_num = goodsList.getGroup_num();
                }


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
                jsonObject.put("group_price", group_price);
                jsonObject.put("group_num", group_num);

                //json
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getOrderById(String goods_id) {
        String goods_name = null;
        String goods_intro = null;
        Float goods_price = 0.0f;
        Integer status = 0;
        String status_get = null;
        String create_time = null;
        String phone_number = null;
        String location = null;
        String nick_name = null;
        String openid = null;
        String studio = null;
        String campus = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Order> list =dao.getOrderById(goods_id);;

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Order line = list.get(i);
                //获取字段
                String id = line.getId();
                goods_name = line.getGoods_name();
                goods_intro = line.getGoods_intro();
                goods_price = line.getGoods_price();
                status = line.getStatus();
                phone_number = line.getPhone_number();
                location = line.getLocation();
                nick_name = line.getNick_name();
                openid = line.getOpenid();
                String group_role = line.getGroup_role();
                String leader_id = line.getLeader_id();


                List<User> users = dao.getUserByOpenid(leader_id);
                if(users.size()>0){
                    studio = users.get(0).getStudio();
                    campus =  users.get(0).getCampus();
                }

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
                jsonObject.put("openid", openid);
                jsonObject.put("group_role", group_role);
                jsonObject.put("leader_id", leader_id);
                jsonObject.put("studio", studio);
                jsonObject.put("campus", campus);

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
//                photo = line.getPhoto();
                class_name =line.getClass_name();
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }

                //json
                jsonObject.put("id", id);
                jsonObject.put("studio", studio);
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
    public List getExhibition(String studio, String type,Integer page) {
        Integer page_start = (page - 1) * 4;
        Integer page_length = 4;
        List<JSONObject> resul_list = new ArrayList<>();
        List<Message> messages = dao.getExhibitionByType(studio,type,page_start, page_length);

        for (int i = 0; i < messages.size(); i++) {
            Message line = messages.get(i);
            String uuids = line.getUuids();
            String vuuid = line.getVuuid();
            String comment = line.getComment();
            Integer views = line.getViews();
            String id = line.getId();
            try {
                uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
            } catch (Exception e) {
//                    throw new RuntimeException(e);
            }

            try {
                vuuid = line.getVuuid().replace("\"","").replace("[","").replace("]","");
            } catch (Exception e) {
//                    throw new RuntimeException(e);
            }
            String[] uuids_list = uuids.split(",");
            if(uuids.length()>2){
                for(int j=0;j<uuids_list.length;j++){
                    JSONObject jsonObject = new JSONObject();
                    String uuids_get = uuids_list[j];
                    jsonObject.put("uuids",uuids_get);
                    jsonObject.put("vuuid",vuuid);
                    jsonObject.put("id",id);
                    jsonObject.put("comment",comment);
                    jsonObject.put("views",views);
                    resul_list.add(jsonObject);
                }
            }else{
                dao.deleteComment(Integer.parseInt(id),studio);
            }
        }

        return resul_list;
    }

    @Override
    public List getUpdateNews() {
        List<JSONObject> resul_list = new ArrayList<>();
        List<Message> messages = dao.getUpdateNews();

        for (int i = 0; i < messages.size(); i++) {
            Message line = messages.get(i);
            String uuids = line.getUuids();
            String vuuid = line.getVuuid();
            String comment = line.getComment();
            Integer views = line.getViews();
            String class_target_bak = line.getClass_target_bak();
            String update_type = messages.get(0).getClass_target_bak();
            String id = line.getId();
            try {
                uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
            } catch (Exception e) {
//                    throw new RuntimeException(e);
            }

            try {
                vuuid = line.getVuuid().replace("\"","").replace("[","").replace("]","");
            } catch (Exception e) {
                //                    throw new RuntimeException(e);
            }
            String[] uuids_list = uuids.split(",");
            for(int j=0;j<uuids_list.length;j++){
                JSONObject jsonObject = new JSONObject();
                String uuids_get = uuids_list[j];
                jsonObject.put("uuids",uuids_get);
                jsonObject.put("vuuid",vuuid);
                jsonObject.put("id",id);
                jsonObject.put("comment",comment);
                jsonObject.put("class_target_bak",class_target_bak);
                jsonObject.put("views",views);
                jsonObject.put("update_type",update_type);
                resul_list.add(jsonObject);
            }
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
    public int updateLessonPackage(String id, String content,String type) {
        int result = 0;
        try {
            if("课包原价".equals(type)){
                result = dao.updateLessonPackageTotalMoney(id,content);
            }else if("优惠金额".equals(type)){
                result = dao.updateLessonPackageDiscountMoney(id,content);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
    public int updateCoinsByStudio(String studio,String openid,Float number) {
        int result = 0;
        try {
            List<User> users = dao.getUserByOpenid(openid);
            Float  read_times = users.get(0).getRead_times();
            if (read_times == null) {
                read_times = 0.0f;
            }

            Float new_read_times = read_times + number;
            dao.updateReadTimesByOpenid(openid,new_read_times);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Float new_coins = 0.0f;
        List<User> list = dao.getBossByStudioOnly(studio);
        if(list.size()>0){
            User line = list.get(0);
            Float coins = line.getCoins();
            String expired_time = line.getExpired_time();
            String member = line.getMember();

            if("永恒会员".equals(member)){
                if (coins == null) {
                    coins = 0.0f;
                }
                new_coins = coins + 0.5f;
                if(new_coins<24){
                    User user = new User();
                    user.setCoins(new_coins);
                    user.setStudio(studio);
                    user.setExpired_time(expired_time);
                    result = dao.updateCoinsByStudio(user);
                }else if(new_coins>=24){
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
                    try {
                        cal.setTime(df.parse(expired_time));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    cal.add(cal.DATE,1);
                    String expired_time_new = df.format(cal.getTime());
                    User user = new User();
                    user.setCoins(0.0f);
                    user.setStudio(studio);
                    user.setExpired_time(expired_time_new);
                    result = dao.updateCoinsByStudio(user);
                }
            }
        }


        return result;
    }

    @Override
    public int updateGift(String id) {
        int result = 0;
        try {
            List<Gift> gifts = dao.getGiftById(id);
            Gift gift = gifts.get(0);
            Integer status = gift.getStatus();
            int new_status = 1;
            if(status == 1){
                new_status = 0;
            }
            result = dao.updateGift(id,new_status);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void sendClassRemind() {
        List<String> apps = new ArrayList<>();
        apps.add("MOMO_OFFICIAL");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");

        String result = null;
        List<Schedule> list_schedule = new ArrayList<>();
        String tample6 ="{\"touser\":\"openid\",\"template_id\":\"MFu-qjMY5twe6Q00f6NaR-cBEn3QYajFquvtysdxk8o\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing1\":{\"value\": \"time\"},\"time3\":{\"value\": \"A1\"},\"thing2\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";
        String tample14 ="{\"touser\":\"openid\",\"template_id\":\"Bl9ZwhH2pWqL2pgo-WF1T5LPI4QUxmN9y7OWmwvvd58\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing16\":{\"value\": \"time\"},\"thing17\":{\"value\": \"A1\"},\"short_thing5\":{\"value\": \"AA\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";
        String publickey = Constants.publickey;
        String privatekey = Constants.privatekey;

        List<User> list = dao.getAllUser();
        for (int i = 0; i < list.size(); i++) {
            User user = list.get(i);
            String official_openid = user.getOfficial_openid();
            String studio = user.getStudio();
            String student_name = user.getStudent_name();
            String send_time = user.getSend_time();
            String subscription = user.getSubscription();
            String remindType = user.getRemind_type();
            Integer hours = user.getHours();
            String campus = user.getCampus();
            String openid = user.getOpenid();
            String role = user.getRole();

            //获取提前时间
            Calendar cal_today = Calendar.getInstance();
            cal_today.add(Calendar.HOUR_OF_DAY,hours);
            int weekDay_today = cal_today.get(Calendar.DAY_OF_WEEK);
            int hour = cal_today.get(Calendar.HOUR_OF_DAY);
            int minute = cal_today.get(Calendar.MINUTE);
            String hour_st = Integer.toString(hour);
            String minute_st = Integer.toString(minute);
            if(hour < 10 ){
                hour_st = "0" + hour_st;
            }
            if(minute < 10 ){
                minute_st = "0" + minute_st;
            }
            String duration_st = hour_st + ":" + minute_st;

            //获取统一时间
            Calendar cal_tomorrow = Calendar.getInstance();
            cal_tomorrow.add(Calendar.DATE,+1);
            Integer weekDay_tomorrow = cal_tomorrow.get(Calendar.DAY_OF_WEEK);

            //获取当前时间
            Date date =new Date();
            long timestamp = date.getTime();
            String now_date = df_now.format(date).split(" ")[0];
            String now_time = df_now.format(date).split(" ")[1];

            //获取发送时间戳
            long timestamp_start = 0l;
            long timestamp_end = 0l;
            try {
                Date date_now = df_now.parse(now_date + " " + send_time);
                timestamp_start = date_now.getTime();
                timestamp_end = timestamp_start + 3*60*1000;
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            Integer weekDay = 0;
            String date_time = null;
            //上课通知
            if(!"no_name".equals(student_name)){
                if("统一提醒次日".equals(remindType) && timestamp >= timestamp_start && timestamp <=timestamp_end){
                    weekDay = weekDay_tomorrow;
                    date_time = df.format(cal_tomorrow.getTime());
                    list_schedule = dao.getScheduleByUser(weekDay_tomorrow,studio,student_name,campus);
                }else if("提前N小时提醒".equals(remindType) && hours > 0){
                    weekDay = weekDay_today;
                    date_time = df.format(cal_today.getTime());
                    list_schedule = dao.getScheduleByUserDurationSt(weekDay_today,studio,student_name,campus,duration_st);
                }

                if(list_schedule.size() > 0 && weekDay > 0){
                    for (int j = 0; j < list_schedule.size(); j++) {
                        String duration = null;
                        String class_number = null;
                        String subject = null;
                        Integer remind = 0;
                        Schedule schedule = list_schedule.get(j);
                        duration = schedule.getDuration();
                        class_number = schedule.getClass_number();
                        subject = schedule.getSubject();
                        remind = schedule.getRemind();
                        String id = schedule.getId();
                        String send_status = schedule.getSend_status();
                        Integer choose = 0;
                        Integer weekDayChoose = 0;
                        if(weekDay == 1){
                            weekDayChoose = 7;
                        }else {
                            weekDayChoose = weekDay -1;
                        }

                        if(!send_status.equals(now_date)){
                            //选课老师上课通知
                            String chooseLesson = "星期"+  weekDayChoose + "," + subject + "," + class_number + "," + duration ;
                            List<User> users = dao.getUserByChooseLesson(chooseLesson,studio);
                            if(users.size()>0 && remind == 1){
                                choose = 1;
                                for(int ui=0;ui<users.size();ui++){
                                    String official_openid_boss = users.get(ui).getOfficial_openid();
                                    for(int a=0;a<apps.size();a++){
                                        String url_send = null;
                                        String app = apps.get(a);
                                        String token = getToken(app);

                                        if("MOMO_OFFICIAL".equals(app)){
                                            url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
                                            //绑定公众号通知
                                            if(official_openid_boss != null){
                                                String[] official_list_boss = official_openid_boss.split(",");
                                                for(int k=0;k<official_list_boss.length;k++){
                                                    try {
                                                        String official_openid_get = official_list_boss[k];
                                                        JSONObject queryJson2 = JSONObject.parseObject(tample6);
                                                        queryJson2.put("touser",official_openid_get);
                                                        queryJson2.getJSONObject("data").getJSONObject("thing1").put("value","上课提醒已发送" +"(" + student_name + ")");
                                                        queryJson2.getJSONObject("data").getJSONObject("time3").put("value",date_time + " " + duration.split("-")[0]);
                                                        queryJson2.getJSONObject("data").getJSONObject("thing2").put("value",class_number +"(" + studio + ")");

                                                        System.out.printf("param2:" + queryJson2.toJSONString());
                                                        result = HttpUtil.sendPostJson(url_send,queryJson2.toJSONString());
                                                        System.out.printf("res2:" + result);
                                                    } catch (Exception e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            //学生家长上课通知
                            if(remind == 1 && choose == 1){
                                dao.updateClassSendStatus(id,now_date);
                                //小程序公众号通知
                                for(int a=0;a<apps.size();a++){
                                    String url_send = null;
                                    String app=apps.get(a);
                                    String token = getToken(app);
                                    if("MOMO_OFFICIAL".equals(app)){
                                        url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
                                        //绑定公众号通知
                                        if(official_openid != null){
                                            String[] official_list = official_openid.split(",");
                                            for(int k=0;k<official_list.length;k++){
                                                try {
                                                    String official_openid_get = official_list[k];
                                                    JSONObject queryJson2 = JSONObject.parseObject(tample6);
                                                    queryJson2.put("touser",official_openid_get);
                                                    queryJson2.getJSONObject("data").getJSONObject("thing1").put("value",student_name);
                                                    queryJson2.getJSONObject("data").getJSONObject("time3").put("value",date_time + " " + duration.split("-")[0]);
                                                    queryJson2.getJSONObject("data").getJSONObject("thing2").put("value", class_number+"("+studio+")");

                                                    System.out.println("json2:" + queryJson2.toJSONString());
                                                    result = HttpUtil.sendPostJson(url_send,queryJson2.toJSONString());
                                                    System.out.printf("res22:" + result);
                                                } catch (Exception e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }
                                    }
                                }

                                //pwa版上课通知
                                if(subscription != null){
                                    JSONObject payload = new JSONObject();
                                    payload.put("title",studio);
                                    payload.put("message","上课日期:"+ date_time +"\n上课时间:"+ duration + "\n班号:" + class_number + "\n学生名:" + student_name );
                                    String status = webPushService.sendNotification(subscription,publickey,privatekey,payload.toString());
                                    System.out.printf("status:" + status);
                                }
                            }
                        }
                    }
                }
            }

            //续课通知
            if(!"no_name".equals(student_name)) {
                List<Lesson> lessons = dao.getLessonLikeName(studio, student_name, campus);
                if (lessons.size() > 0) {
                    for (int ii = 0; ii < lessons.size(); ii++) {
                        Lesson lesson = lessons.get(ii);
                        Float left_amount = lesson.getLeft_amount();
                        String subject = lesson.getSubject();
                        String student_lesson = lesson.getStudent_name();
                        String student_split = student_lesson.split("_")[0];
                        if (student_split.equals(student_name) && left_amount <= 2 && send_time.equals(now_time)) {
                            String token = getToken("MOMO_OFFICIAL");
                            String url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
                            if (official_openid != null) {
                                String[] official_list = official_openid.split(",");
                                for (int j = 0; j < official_list.length; j++) {
                                    String official_openid_get = official_list[j];
                                    JSONObject queryJson2 = JSONObject.parseObject(tample14);
                                    queryJson2.put("touser", official_openid_get);
                                    queryJson2.getJSONObject("data").getJSONObject("thing16").put("value", studio + "_" + subject);
                                    queryJson2.getJSONObject("data").getJSONObject("thing17").put("value", student_lesson + "剩下" + left_amount + "课时");
                                    queryJson2.getJSONObject("data").getJSONObject("short_thing5").put("value", "请及时续课");
                                    result = HttpUtil.sendPostJson(url_send, queryJson2.toJSONString());
                                    System.out.printf("res:" + result);
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    @Override
    public void sendSquareRemind() {
        List<String> apps = new ArrayList<>();
        apps.add("MOMO_OFFICIAL");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");

        String result = null;
        String tample14 ="{\"touser\":\"openid\",\"template_id\":\"Bl9ZwhH2pWqL2pgo-WF1T6Sqan69VVUx8liFiogg9YM\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing25\":{\"value\": \"time\"},\"thing44\":{\"value\": \"A1\"},\"thing20\":{\"value\": \"A1\"},\"short_thing5\":{\"value\": \"AA\"},\"time48\":{\"value\": \"time\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";

        String title = null;
        String type = null;
        String comment = null;
        String id = null;
        try {
            Random random = new Random();
            int randomNumber = random.nextInt(2); // 生成0到10之间的随机数
            List<Message> messages = dao.getUpdateNews();
            id = messages.get(randomNumber).getId();
            title = messages.get(0).getComment().split("简介")[0].replaceAll("\n", "");
            type = messages.get(0).getClass_target_bak();
            comment = messages.get(0).getComment().split("简介")[1].replace("：","");
            if(comment.length() > 14){
                comment = comment.substring(0, 14) + "...";
            }
            if(title.length() > 12){
                title = title.substring(0, 12);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<User> list = dao.getAllUser();
        for (int i = 0; i < list.size(); i++) {
            User user = list.get(i);
            String role = user.getRole();
            String official_openid = user.getOfficial_openid();
            String studio = user.getStudio();
            String send_time = "21:00:00";
            String openid = user.getOpenid();
            Float read_times = user.getRead_times();
            String send_status = user.getSend_status();
            Integer is_square = user.getIs_square();

            //获取当前时间
            Date date =new Date();
            int hour = date.getHours();
            long timestamp = date.getTime();
            String update_time = df_now.format(date);
            String now_date = df_now.format(date).split(" ")[0];
            String now_time = df_now.format(date).split(" ")[1];

            if(send_status == null){
                send_status = now_date + " " + send_time;
            }

            //获取发送时间戳
            long timestamp_start = 0l;
            long timestamp_end = 0l;
            try {
                Date date_now = df_now.parse(now_date + " " + send_time);
                timestamp_start = date_now.getTime();
                timestamp_end = timestamp_start + 10*60*1000;

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            String send_status_new = now_date + " " + send_time;

            //广场通知
            if("client".equals(role) && is_square == 1 && timestamp >= timestamp_start && timestamp <=timestamp_end && !send_status.equals(send_status_new)){
                dao.updateClassSendStatusByOpenid(openid,send_status_new);
                String token = getToken("MOMO_OFFICIAL");
                String url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
                if (official_openid != null) {
                    String[] official_list = official_openid.split(",");
                    for(int k=0;k<official_list.length;k++){
                        try {
                            String official_openid_get = official_list[k];
                            JSONObject queryJson2 = JSONObject.parseObject(tample14);
                            queryJson2.put("touser", official_openid_get);
                            queryJson2.getJSONObject("data").getJSONObject("thing25").put("value","系统管理员");
                            queryJson2.getJSONObject("data").getJSONObject("thing44").put("value", "本次推送："+title);
                            queryJson2.getJSONObject("data").getJSONObject("thing20").put("value", "简介：" + comment);
                            queryJson2.getJSONObject("data").getJSONObject("short_thing5").put("value", "待查看");
                            queryJson2.getJSONObject("data").getJSONObject("time48").put("value", now_date+ " " + now_time);
                            queryJson2.getJSONObject("miniprogram").put("pagepath","/pages/album/album?studio=" + studio + "&role=" + role + "&openid=" + openid + "&type=" + type + "&read_times=" + read_times);

                            result = HttpUtil.sendPostJson(url_send, queryJson2.toJSONString());
                            System.out.printf("res:" + result);
                            if("o25ly6whIE5oBYdDjc2M4afnxQmU".equals(openid)){
                                dao.updateVideoTop(Integer.parseInt(id),update_time);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }
        }
    }

    @Override
    public void sendTeacherRemind() {
        List<String> apps = new ArrayList<>();
        apps.add("MOMO_OFFICIAL");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");

        String result = null;
        String tample14 ="{\"touser\":\"openid\",\"template_id\":\"Bl9ZwhH2pWqL2pgo-WF1T6Sqan69VVUx8liFiogg9YM\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing25\":{\"value\": \"time\"},\"thing44\":{\"value\": \"A1\"},\"thing20\":{\"value\": \"A1\"},\"short_thing5\":{\"value\": \"AA\"},\"time48\":{\"value\": \"time\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";

        String nick_name = null;
        String comment = null;
        String teacher_studio = null;
        String id = null;
        try {
            List<Message> messages =dao.getOnlineTeacher("网课",0,1);
            String openid = messages.get(0).getOpenid();
            List<User> users = dao.getUserByOpenid(openid);
            nick_name = users.get(0).getNick_name();
            comment = messages.get(0).getComment();
            teacher_studio = messages.get(0).getStudio();
            id  =  messages.get(0).getId();
            if(comment.length() > 14){
                comment = comment.substring(0, 14) + "...";
            }
            if(nick_name.length() > 12){
                nick_name = nick_name.substring(0, 12);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<User> list = dao.getAllUser();
        for (int i = 0; i < list.size(); i++) {
            User user = list.get(i);
            String role = user.getRole();
            String official_openid = user.getOfficial_openid();
            String studio = user.getStudio();
            String send_time = "21:00:00";
            String openid = user.getOpenid();
            Float read_times = user.getRead_times();
            String send_status = user.getSend_status();
            Integer is_teacher = user.getIs_teacher();

            //获取当前时间
            Date date =new Date();
            int hour = date.getHours();
            long timestamp = date.getTime();
            String update_time = df_now.format(date);
            String now_date = df_now.format(date).split(" ")[0];
            String now_time = df_now.format(date).split(" ")[1];
//
//            if(hour>12){
//                send_time = "15:30:00";
//            }

            if(send_status == null){
                send_status = now_date + " " + send_time;
            }

            //获取发送时间戳
            long timestamp_start = 0l;
            long timestamp_end = 0l;
            try {
                Date date_now = df_now.parse(now_date + " " + send_time);
                timestamp_start = date_now.getTime();
                timestamp_end = timestamp_start + 10*60*1000;

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            String send_status_new = now_date + " " + send_time;

            //广场通知
            if("client".equals(role) && is_teacher == 1 && timestamp >= timestamp_start && timestamp <=timestamp_end && !send_status.equals(send_status_new) && !teacher_studio.equals(studio)){
                dao.updateClassSendStatusByOpenid(openid,send_status_new);
                String token = getToken("MOMO_OFFICIAL");
                String url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
                if (official_openid != null) {
                    String[] official_list = official_openid.split(",");
                    for(int k=0;k<official_list.length;k++){
                        try {
                            String official_openid_get = official_list[k];
                            JSONObject queryJson2 = JSONObject.parseObject(tample14);
                            queryJson2.put("touser", official_openid_get);
                            queryJson2.getJSONObject("data").getJSONObject("thing25").put("value","系统管理员");
                            queryJson2.getJSONObject("data").getJSONObject("thing44").put("value", "网课推荐："+nick_name+"老师");
                            queryJson2.getJSONObject("data").getJSONObject("thing20").put("value", "简介：" + comment);
                            queryJson2.getJSONObject("data").getJSONObject("short_thing5").put("value", "待查看");
                            queryJson2.getJSONObject("data").getJSONObject("time48").put("value", now_date+ " " + now_time);
                            queryJson2.getJSONObject("miniprogram").put("pagepath","/pages/online_teacher/online_teacher?id=" + id + "&openid=" + openid + "&type=" + "网课");

                            result = HttpUtil.sendPostJson(url_send, queryJson2.toJSONString());
                            System.out.printf("res:" + result);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }
        }
    }

    @Override
    public void sendExchangeRemind() {
        List<String> apps = new ArrayList<>();
        apps.add("MOMO_OFFICIAL");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");

        String result = null;
        String tample14 ="{\"touser\":\"openid\",\"template_id\":\"Bl9ZwhH2pWqL2pgo-WF1T6Sqan69VVUx8liFiogg9YM\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing25\":{\"value\": \"time\"},\"thing44\":{\"value\": \"A1\"},\"thing20\":{\"value\": \"A1\"},\"short_thing5\":{\"value\": \"AA\"},\"time48\":{\"value\": \"time\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";

        String nick_name = null;
        String comment = null;
        String teacher_studio = null;
        String id = null;
        String city = null;
        String subject = null;
        try {
            List<Message> messages =dao.getOnlineTeacher("同城",0,1);
            String openid = messages.get(0).getOpenid();
            List<User> users = dao.getUserByOpenid(openid);
            nick_name = users.get(0).getNick_name();
            comment = messages.get(0).getComment();
            teacher_studio = messages.get(0).getStudio();
            id  =  messages.get(0).getId();
            city = users.get(0).getCity();
            subject = users.get(0).getSubject();
            if(comment.length() > 14){
                comment = comment.substring(0, 14) + "...";
            }
            if(nick_name.length() > 12){
                nick_name = nick_name.substring(0, 12);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<User> list = dao.getAllUser();
        for (int i = 0; i < list.size(); i++) {
            User user = list.get(i);
            String role = user.getRole();
            String official_openid = user.getOfficial_openid();
            String studio = user.getStudio();
            String send_time = "21:00:00";
            String openid = user.getOpenid();
            Float read_times = user.getRead_times();
            String send_status = user.getSend_status();
            String city_get = user.getCity();
            String subject_get = user.getSubject();
            Integer is_exchange = user.getIs_exchange();

            //获取当前时间
            Date date =new Date();
            int hour = date.getHours();
            long timestamp = date.getTime();
            String update_time = df_now.format(date);
            String now_date = df_now.format(date).split(" ")[0];
            String now_time = df_now.format(date).split(" ")[1];

//            if(hour>12){
//                send_time = "16:00:00";
//            }

            if(send_status == null){
                send_status = now_date + " " + send_time;
            }

            //获取发送时间戳
            long timestamp_start = 0l;
            long timestamp_end = 0l;
            try {
                Date date_now = df_now.parse(now_date + " " + send_time);
                timestamp_start = date_now.getTime();
                timestamp_end = timestamp_start + 10*60*1000;

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            String send_status_new = now_date + " " + send_time;

            //广场通知
            if(!subject.equals(subject_get) && city.equals(city_get) && is_exchange == 1 && "client".equals(role) && timestamp >= timestamp_start && timestamp <=timestamp_end && !send_status.equals(send_status_new) && !teacher_studio.equals(studio)){
                dao.updateClassSendStatusByOpenid(openid,send_status_new);
                String token = getToken("MOMO_OFFICIAL");
                String url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
                if (official_openid != null) {
                    String[] official_list = official_openid.split(",");
                    for(int k=0;k<official_list.length;k++){
                        try {
                            String official_openid_get = official_list[k];
                            JSONObject queryJson2 = JSONObject.parseObject(tample14);
                            queryJson2.put("touser", official_openid_get);
                            queryJson2.getJSONObject("data").getJSONObject("thing25").put("value","系统管理员");
                            queryJson2.getJSONObject("data").getJSONObject("thing44").put("value", "同城推荐："+nick_name+"老师");
                            queryJson2.getJSONObject("data").getJSONObject("thing20").put("value", "简介：" + comment);
                            queryJson2.getJSONObject("data").getJSONObject("short_thing5").put("value", "待查看");
                            queryJson2.getJSONObject("data").getJSONObject("time48").put("value", now_date+ " " + now_time);
                            queryJson2.getJSONObject("miniprogram").put("pagepath","/pages/online_teacher/online_teacher?id=" + id + "&openid=" + openid + "&type=" + "同城");

                            result = HttpUtil.sendPostJson(url_send, queryJson2.toJSONString());
                            System.out.printf("res:" + result);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }
        }
    }

    @Override
    public void sendBossPayRemind() {
        List<String> apps = new ArrayList<>();
        apps.add("MOMO_OFFICIAL");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");

        String result = null;
        String tample14 ="{\"touser\":\"openid\",\"template_id\":\"Bl9ZwhH2pWqL2pgo-WF1T5LPI4QUxmN9y7OWmwvvd58\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing16\":{\"value\": \"time\"},\"thing17\":{\"value\": \"A1\"},\"short_thing5\":{\"value\": \"AA\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";

        List<User> list = dao.getAllBoss();
        for (int i = 0; i < list.size(); i++) {
            User user = list.get(i);
            String role = user.getRole();
            String official_openid = user.getOfficial_openid();
            String studio = user.getStudio();
            String send_time = user.getSend_time();
            String expried_time = user.getExpired_time();
            Long compare = 10L;
            String campus = user.getCampus();
            String student_name = user.getStudent_name();

            //获取当前时间
            String now_date = df_now.format(new Date()).split(" ")[0];
            String now_time = df_now.format(new Date()).split(" ")[1];

            try {
                Date today_dt = df.parse(now_date.substring(0,10));
                Date expired_dt = df.parse(expried_time.substring(0,10));
                Long day2 = expired_dt.getTime();
                Long day1 = today_dt.getTime();
                compare = (day2 - day1)/(24*3600*1000);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            //续费通知
            if(role.equals("boss") && compare <= 5L && send_time.equals(now_time)){
                for(int a=0;a<apps.size();a++){
                    String url_send = null;
                    String app = apps.get(a);
                    String token = getToken(app);
                    if("MOMO_OFFICIAL".equals(app)){
                        url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
                        //绑定公众号通知
                        if(official_openid != null){
                            String[] official_list = official_openid.split(",");
                            for(int j=0;j<official_list.length;j++){
                                String official_openid_get = official_list[j];
                                JSONObject queryJson2 = JSONObject.parseObject(tample14);
                                queryJson2.put("touser",official_openid_get);
                                queryJson2.getJSONObject("data").getJSONObject("thing16").put("value",studio);
                                queryJson2.getJSONObject("data").getJSONObject("thing17").put("value",compare +"天后到期,至"+expried_time);
                                queryJson2.getJSONObject("data").getJSONObject("short_thing5").put("value","请及时续费");
                                result = HttpUtil.sendPostJson(url_send,queryJson2.toJSONString());
                                System.out.printf("res:" + result);
                            }
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
        String official_appid = Constants.official_appid;
        String official_secret = Constants.official_secret;
        String url = "https://api.weixin.qq.com/cgi-bin/token";

        if ("MOMO2B".equals(app)){
            param = "appid=" + appid_2b + "&secret=" + secret_2b + "&grant_type=client_credential";
        }else if ("MOMO".equals(app)){
            param = "appid=" + appid + "&secret=" + secret + "&grant_type=client_credential";
        }else if("MOMO_OFFICIAL".equals(app)){
            param = "appid=" + official_appid + "&secret=" + official_secret + "&grant_type=client_credential";
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
    public List getStudentByTeacher(String studio,String openid,String duration_time,Integer page) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
        String date_time = df.format(new Date());
        Date date = new Date();
        cal.setTime(date);

        Integer page_start = (page - 1) * 100;
        Integer page_length = 100;
        DecimalFormat df1 = new DecimalFormat("0.00");
        String date_start = date_time;
        String date_end = date_time;
        List<JSONObject> resul_list = new ArrayList<>();
        JSONObject jsonObject_all = new JSONObject();
        try {
            String[] duration_time_list =duration_time.split("_");
            List<User> list_user = dao.getUser(openid);
            String nick_name = list_user.get(0).getNick_name();
            String campus = list_user.get(0).getCampus();
            if("近1周".equals(duration_time)){
                cal.add(Calendar.DATE,-7);
                date_start = df.format(cal.getTime());
            } else if("近1月".equals(duration_time)) {
                cal.add(Calendar.DATE,-31);
                date_start = df.format(cal.getTime());
            }else if("近1年".equals(duration_time)) {
                cal.add(Calendar.DATE,-365);
                date_start = df.format(cal.getTime());
            }else if("自定义".equals(duration_time_list[0])){
                date_start = duration_time_list[1];
                date_end = duration_time_list[2];
            }

            Float count_sum = 0.0f;
            Float price_sum = 0.0f;
            Integer sign_sum = 0;
            String title = "科目,名字,上课日,签到日,备注,课时,课均单价";
            List<String> data_list = new ArrayList<>();
            if(page == 1){
                List<SignUp> list = dao.getStudentByTeacherByDuration(studio,nick_name,date_start,date_end);
                for (int i = 0; i < list.size(); i++) {
                    SignUp line = list.get(i);
                    String subject = line.getSubject();
                    String student_name = line.getStudent_name();
                    Float count = line.getCount();
                    String create_time = line.getCreate_time();
                    String sign_time = line.getSign_time();
                    String mark = line.getMark();

                    Float total_money = 0.0f;
                    Float discount_money = 0.0f;
                    Float price = 0.0f;
                    Float sign_price = 0.0f;
                    Float all_lesson = 0.0f;
                    Float give_lesson = 0.0f;

                    try {
                        List<Lesson> lessons = dao.getLessonByNameSubjectAll(student_name,studio,subject,campus);
                        if(lessons.size()>0){
                            Lesson lesson = lessons.get(0);
                            price = lesson.getPrice();
                        }


                        List<LessonPackage> lessonPackages = dao.getLessonPackage(student_name,studio,campus,subject);
                        if(lessonPackages.size()>0){
                            for(int j = 0; j < lessonPackages.size(); j++){
                                LessonPackage lessonPackage = lessonPackages.get(j);
                                total_money = total_money + lessonPackage.getTotal_money();
                                discount_money = discount_money + lessonPackage.getDiscount_money();
                                all_lesson = all_lesson + lessonPackage.getAll_lesson();
                                give_lesson = give_lesson + lessonPackage.getGive_lesson();
                            }
                        }

                        Float receipts = total_money - discount_money;
                        Float re_price = receipts/(all_lesson+give_lesson);
                        if(re_price>0){
                            price = re_price;
                        }

                        sign_price = count * price;

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    count_sum = count_sum + count;
                    price_sum = price_sum + sign_price;
                    sign_sum = sign_sum + 1;
                    String data_line = subject + "," + student_name + "," + create_time + "," + sign_time + "," +mark + "," + count + "," + price;
                    data_list.add(data_line);
                }

                jsonObject_all.put("price_sum", df1.format(price_sum));
                jsonObject_all.put("sign_sum", sign_sum);
                jsonObject_all.put("count_sum", count_sum);
                resul_list.add(jsonObject_all);
                downloadByOpenid(studio,openid,data_list,title,"form");
            }


            List<SignUp> list_detail = dao.getStudentByTeacherByDurationByPage(studio,nick_name,date_start,date_end,page_start,page_length);
            for (int j = 0; j < list_detail.size(); j++) {
                JSONObject jsonObject = new JSONObject();
                SignUp line = list_detail.get(j);
                String subject = line.getSubject();
                String student_name = line.getStudent_name();
                String sign_time =line.getSign_time();
                String create_time = line.getCreate_time();
                String mark = line.getMark();
                Float count = line.getCount();
                String campus_get = line.getCampus();
                Float price = 0.0f;
                Float total_money = 0.0f;
                Float discount_money = 0.0f;
                Float all_lesson = 0.0f;
                Float given_lesson = 0.0f;

                List<Lesson> lessons = dao.getLessonByNameSubjectAll(student_name,studio,subject,campus_get);
                if(lessons.size()>0){
                    Lesson lesson = lessons.get(0);
                    price = lesson.getPrice();
                }


                List<LessonPackage> lessonPackages = dao.getLessonPackage(student_name,studio,campus_get,subject);
                if(lessonPackages.size()>0){
                    for(int k = 0; k < lessonPackages.size(); k++){
                        LessonPackage lessonPackage = lessonPackages.get(k);
                        total_money = total_money + lessonPackage.getTotal_money();
                        discount_money = discount_money + lessonPackage.getDiscount_money();
                        all_lesson = all_lesson + lessonPackage.getAll_lesson();
                        given_lesson = given_lesson + lessonPackage.getGive_lesson();
                    }
                }

                Float receipts = total_money - discount_money;
                Float re_price = receipts/(all_lesson+given_lesson);
                if(re_price>0){
                    price = re_price;
                }

                jsonObject.put("studio", studio);
                jsonObject.put("subject", subject);
                jsonObject.put("campus", campus);
                jsonObject.put("student_name", student_name);
                jsonObject.put("sign_time", sign_time);
                jsonObject.put("create_time", create_time);
                jsonObject.put("mark", mark);
                jsonObject.put("count", count);
                jsonObject.put("price", df1.format(price));
                if(student_name.length() >0){
                    resul_list.add(jsonObject);
                }
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
        String unionid = null;
        String appid = Constants.appid;
        String secret = Constants.secret;
        String appid_2b = Constants.appid_2b;
        String secret_2b = Constants.secret_2b;
        String order_appid = Constants.order_appid;
        String order_secret = Constants.order_secret;
        String book_appid = Constants.book_appid;
        String book_secret = Constants.book_secret;
        String url = "https://api.weixin.qq.com/sns/jscode2session";


        if ("MOMO2B".equals(app)){
            param = "appid="+ appid_2b + "&secret=" + secret_2b + "&js_code="+ code +"&grant_type=authorization_code";
        }else if("MOMO".equals(app)){
            param = "appid="+ appid + "&secret=" + secret + "&js_code="+ code +"&grant_type=authorization_code";
        }else if("ORDER".equals(app)){
            param = "appid="+ order_appid + "&secret=" + order_secret + "&js_code="+ code +"&grant_type=authorization_code";
        }else if("BOOK".equals(app)){
            param = "appid="+ book_appid + "&secret=" + book_secret + "&js_code="+ code +"&grant_type=authorization_code";
        }

        try {
            result = HttpUtil.sendPost(url	,param);
            JSONObject jsonObject = JSON.parseObject(result);
            openid = jsonObject.getString("openid");
            unionid = jsonObject.getString("unionid");
            if(unionid != null){
                if("MOMO".equals(app)){
                    dao.updateUserUnionid(openid,unionid,app);
                }else if("ORDER".equals(app)){
                    dao.updateRestaurantUserUnionid(openid,unionid);
                }else if("BOOK".equals(app)){
                    dao.updateBookUserUnionid(openid,unionid);
                }

            }
        } catch (Exception e) {
//			e.printStackTrace();
        }
        return openid;
    }

    @Override
    public String getOpenidOfficial() {
        List<String> apps = new ArrayList<>();
        apps.add("MOMO_OFFICIAL");
        for(int k=0;k<apps.size();k++){
            String app = apps.get(k);
            try {
                String result = null;
                String token = getToken(app);
                String url = "https://api.weixin.qq.com/cgi-bin/user/get";
                String param = "access_token="+ token;

                String url1 = "https://api.weixin.qq.com/cgi-bin/user/info";

                result = HttpUtil.sendPost(url	,param);
                JSONObject jsonObject = JSON.parseObject(result);
                String data = jsonObject.getString("data");
                JSONObject jsonObject1 = JSON.parseObject(data);
                String list = jsonObject1.getString("openid").replace("[","").replace("]","").replace("\"","");
                String[] openid_list = list.split(",");
                for(int i=0;i<openid_list.length;i++){
                    String openid = openid_list[i];
                    String param2 = "access_token="+ token + "&openid=" + openid  + "&lang=zh_CN";
                    String result2 = HttpUtil.sendPost(url1	,param2);
                    JSONObject jsonObject2 = JSON.parseObject(result2);
                    String unionid = jsonObject2.getString("unionid");
                    String official_openid = jsonObject2.getString("openid");

                    //更新小桃子助手公众号
                    try {
                        List<User> users = dao.getUserByUnionid(unionid);
                        if(users.size()>0){
                            for(int j =0;j<users.size();j++){
                                String official_openid_get = users.get(j).getOfficial_openid();
                                if(official_openid_get != null){
                                    if(!official_openid_get.contains(official_openid)){
                                        official_openid = official_openid + "," + official_openid_get;
                                    }
                                }
                                dao.updateUserOfficialOpenid(unionid,official_openid);
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    //更新小桃子点点公众号
                    try {
                        List<RestaurantUser> restaurantUsers = dao.getRestaurantUserByUnionid(unionid);
                        if(restaurantUsers.size()>0){
                            for(int jj =0;jj<restaurantUsers.size();jj++){
                                String official_openid_get = restaurantUsers.get(jj).getOfficial_openid();
                                if(official_openid_get != null){
                                    if(!official_openid_get.contains(official_openid)){
                                        official_openid = official_openid + "," + official_openid_get;
                                    }
                                }
                                dao.updateRestaurantUserOfficialOpenid(unionid,official_openid);
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public String renewLessonRemind(String student_name, String studio, String campus, String subject, Float lesson_amount) {
        String result = null;
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        String url_send = null;
        String model ="{\"touser\":\"openid\",\"template_id\":\"LbJ2VBZ7f3qz_i3nBRzynL79DVOmRqIN_61reo5m4p4\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing2\":{\"value\": \"AA\"},\"thing3\":{\"value\": \"A1\"},\"thing1\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";

        try {
            List<User> users = dao.getUserByStudent(student_name,studio);
            if(users.size()>0){
                User user = users.get(0);
                String openid = user.getOpenid();
                String official_openid = user.getOfficial_openid();

                List<Lesson> lessons_get = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                total_amount = lessons_get.get(0).getTotal_amount();
                left_amount = lessons_get.get(0).getLeft_amount();

                Float total_new = total_amount + lesson_amount;
                Float left_new = left_amount + lesson_amount;

                try {
                    String token = getToken("MOMO_OFFICIAL");
                    url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
                    if(official_openid != null){
                        String[] official_list = official_openid.split(",");
                        for(int j=0;j<official_list.length;j++){
                            String official_openid_get = official_list[j];
                            JSONObject queryJson = JSONObject.parseObject(model);
                            queryJson.put("touser",official_openid_get);
                            queryJson.getJSONObject("data").getJSONObject("thing2").put("value",student_name+"(" + subject + ")");
                            queryJson.getJSONObject("data").getJSONObject("thing3").put("value","成功续课" + lesson_amount + "课时");
                            queryJson.getJSONObject("data").getJSONObject("thing1").put("value",studio + "(总" + total_new + "余"+ left_new + ")");

                            System.out.println("MOMO_OFFICIAL_PARAM:" + queryJson.toJSONString());
                            result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
                            System.out.printf("MOMO_OFFICIAL_RES:" + result);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String leaveRemind(String official_openid_boss,String student_name, String studio, String subject, String duration,String date_time,String mark) {
        String result = null;
        String url_send = null;
        String model ="{\"touser\":\"openid\",\"template_id\":\"Ij01JEH2uo4fQDUiYypBEoByO6iO4w_thleeFsj51eg\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing8\":{\"value\": \"AA\"},\"thing3\":{\"value\": \"A1\"},\"time2\":{\"value\": \"A1\"},\"thing4\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";

        try {
            List<User> users = dao.getUserByStudent(student_name,studio);
            if(users.size()>0){
                User user = users.get(0);
                String openid = user.getOpenid();
                String official_openid_client = user.getOfficial_openid();
                String official_openid = official_openid_boss + "," + official_openid_client;
                try {
                    String token = getToken("MOMO_OFFICIAL");
                    url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
                    if(official_openid != null){
                        String[] official_list = official_openid.split(",");
                        for(int j=0;j<official_list.length;j++){
                            String official_openid_get = official_list[j];
                            JSONObject queryJson = JSONObject.parseObject(model);
                            queryJson.put("touser",official_openid_get);
                            queryJson.getJSONObject("data").getJSONObject("thing8").put("value",student_name);
                            queryJson.getJSONObject("data").getJSONObject("thing3").put("value",studio+"_"+subject);
                            queryJson.getJSONObject("data").getJSONObject("time2").put("value",date_time +" "+duration.split("-")[0]);
                            queryJson.getJSONObject("data").getJSONObject("thing4").put("value",mark);
                            queryJson.getJSONObject("miniprogram").put("pagepath","/pages/leaverecord/leaverecord?student_name=" + student_name + "&studio=" + studio + "&subject=" + subject + "&leave_type=" + "请假" + "&openid=" + openid);

                            System.out.println("MOMO_OFFICIAL_PARAM:" + queryJson.toJSONString());
                            result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
                            System.out.printf("MOMO_OFFICIAL_RES:" + result);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int deleteLessonPackage(Integer id,String type) {
        try {
            if("delete".equals(type)){
                dao.deleteLessonPackage(id);
            }else if("return".equals(type)){
                List<LessonPackage> lessonPackages = dao.getLessonPackageById(id);
                LessonPackage lessonPackage = lessonPackages.get(0);
                String studio = lessonPackage.getStudio();
                String campus = lessonPackage.getCampus();
                String student_name = lessonPackage.getStudent_name();
                String subject = lessonPackage.getSubject();
                Float all_lesson = lessonPackage.getAll_lesson();
                Float given_lesson = lessonPackage.getGive_lesson();

                List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                Lesson lesson_get = lessons.get(0);
                Float total_amount = lesson_get.getTotal_amount();
                Float left_amount = lesson_get.getLeft_amount();
                Float coins = lesson_get.getCoins();
                Float minus = lesson_get.getMinus();

                Float total_new = total_amount -(all_lesson + given_lesson);
                Float left_new = left_amount  - (all_lesson+ given_lesson);

                Lesson lesson = new Lesson();
                lesson.setStudent_name(student_name);
                lesson.setStudio(studio);
                lesson.setSubject(subject);
                lesson.setCampus(campus);
                lesson.setLeft_amount(left_new);
                lesson.setTotal_amount(total_new);
                lesson.setCoins(coins);
                lesson.setMinus(minus);
                int res = dao.updateLesson(lesson);
                if(res == 1){
                    dao.deleteLessonPackage(id);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
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
    public List getLessonPackage(String student_name,String studio,String subject,String search_type,String duration_time,String openid) {
        List<JSONObject> resul_list = new ArrayList<>();
        List<LessonPackage> list = null;
        String start_time = duration_time.split("_")[0];
        String end_time = duration_time.split("_")[1];
        try {
            List<User> list_user = dao.getUser(openid);
            String campus = list_user.get(0).getCampus();

            if("个人".equals(search_type)){
                if("无".equals(start_time)){
                    list = dao.getLessonPackage(student_name,studio,campus,subject);
                }else {
                    list = dao.getLessonPackageByDuration(student_name,studio,campus,subject,start_time,end_time + " 23:59:59");
                }
            } else if ("全部".equals(search_type)) {
                if("无".equals(start_time)){
                    list = dao.getLessonPackageAll(studio,campus,subject);
                }else {
                    list = dao.getLessonPackageByDurationAll(studio,campus,subject,start_time,end_time + " 23:59:59");
                }
            }

            String title = "学生名,原价,优惠,原课时,赠课时,报课时间,有效期至,备注,操作人";
            List<String> data_list = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                LessonPackage line = list.get(i);
                //获取字段
                Float total_money = line.getTotal_money();
                Float discount_money = line.getDiscount_money();
                String mark = line.getMark();
                String start_date = line.getStart_date();
                String end_date = line.getEnd_date();
                String id = line.getId();
                Float all_lesson = line.getAll_lesson();
                Float give_lesson = line.getGive_lesson();
                String nick_name = line.getNick_name();
                student_name  = line.getStudent_name();

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
                jsonObject.put("nick_name", nick_name);
                resul_list.add(jsonObject);

                String data_line = student_name + "," + total_money + "," + discount_money + "," +all_lesson + "," + give_lesson + "," + start_date + "," + end_date + "," + mark + "," + nick_name;
                data_list.add(data_line);
            }
            downloadByOpenid(studio,openid,data_list,title,"single_lesson");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getLessonPackageByAll(String studio,String openid) {
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<User> list_user = dao.getUser(openid);
            String campus = list_user.get(0).getCampus();

            List<LessonPackage> lessonPackages = dao.getLessonPackageByCampus(studio,campus);
            String title = "学生名,原价,优惠,原课时,赠课时,报课时间,有效期至,备注,操作人";
            List<String> data_list = new ArrayList<>();
            for (int i = 0; i < lessonPackages.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                LessonPackage line = lessonPackages.get(i);
                //获取字段
                Float total_money = line.getTotal_money();
                Float discount_money = line.getDiscount_money();
                String mark = line.getMark();
                String start_date = line.getStart_date();
                String end_date = line.getEnd_date();
                String id = line.getId();
                Float all_lesson = line.getAll_lesson();
                Float give_lesson = line.getGive_lesson();
                String nick_name = line.getNick_name();
                String student_name  = line.getStudent_name();

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
                jsonObject.put("nick_name", nick_name);
                resul_list.add(jsonObject);

                String data_line = student_name + "," + total_money + "," + discount_money + "," +all_lesson + "," + give_lesson + "," + start_date + "," + end_date + "," + mark + "," + nick_name;
                data_list.add(data_line);
            }
            downloadByOpenid(studio,openid,data_list,title,"all_lesson");
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
        String positive = null;
        String discipline = null;
        String happiness = null;
        String mp3_url = null;
        StringBuilder student_names = new StringBuilder();

        try {
            users  =dao.getUserByOpenid(openid);
            if(users.size()>0){
                for (int i = 0; i < users.size(); i++) {
                    User line = users.get(i);
                    student_name_get = line.getStudent_name();
                    List<Lesson> lessons = dao.getLessonLikeName(studio,student_name_get,campus);
                    if(lessons.size()>0){
                        for(int ii = 0;ii < lessons.size(); ii ++){
                            Lesson lesson = lessons.get(ii);
                            String student_lesson = lesson.getStudent_name();
                            String student_split = student_lesson.split("_")[0];
                            if(student_split.equals(student_name_get)){
                                student_names = student_names.append(student_lesson).append(",");
                            }

                        }
                    }
                }
            }

            if(student_names.length()>0){
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
                    String vuuid = null;
                    try {
                        uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                    } catch (Exception e) {
//                    throw new RuntimeException(e);
                    }

                    try {
                        vuuid = line.getUuids().replace("\"","").replace("[","").replace("]","");
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
                    jsonObject.put("vuuid", vuuid);
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
                String student_name = null;
                JSONObject jsonObject = new JSONObject();
                PostComment line = postComments.get(i);
                //获取字段
                openid = line.getOpenid();
                try {
                    List<User> list_user = dao.getUser(openid);
                    avatar = list_user.get(0).getAvatarurl();
                    nick_name = list_user.get(0).getNick_name();
                    student_name = list_user.get(0).getStudent_name();
                } catch (Exception e) {
                    // throw new RuntimeException(e);
                }

                studio = line.getStudio();
                content = line.getContent();
                create_time = line.getCreate_time();
                id = line.getId();
                String mp3_url = line.getMp3_url();

                jsonObject.put("openid", openid);
                jsonObject.put("studio_get", studio);
                jsonObject.put("content", content);
                jsonObject.put("avatar", avatar);
                jsonObject.put("nick_name",nick_name);
                jsonObject.put("create_time", create_time);
                jsonObject.put("id", id);
                jsonObject.put("mp3_url", mp3_url);
                jsonObject.put("post_id", post_id);
                jsonObject.put("student_name", student_name);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getLeaveMessage(String studio, String type) {
        List<JSONObject> resul_list = new ArrayList<>();
        String openid = null;
        String content =null;
        String create_time = null;
        String id = null;

        try {
            List<PostComment> postComments = dao.getLeaveMessage(studio,type);
            for (int i = 0; i < postComments.size(); i++) {
                String nick_name = "游客";
                String avatar = null;
                JSONObject jsonObject = new JSONObject();
                PostComment line = postComments.get(i);
                //获取字段
                openid = line.getOpenid();
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
        Float coins = 0.0f;
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
                String expired_time_ad = line.getExpired_time_ad();
                String phone_number = line.getPhone_number();
                String location = line.getLocation();

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
                String today_time = df.format(new Date());
                Date today_dt = df.parse(today_time.substring(0,10));
                Date expired_time_ad_dt = df.parse(expired_time_ad.substring(0,10));
                int compare = today_dt.compareTo(expired_time_ad_dt);
                jsonObject.put("is_show_ad", "false");
                if(compare>0){
                    jsonObject.put("is_show_ad", "true");
                }

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
                jsonObject.put("phone_number",phone_number);
                jsonObject.put("location",location);
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
                String vuuid = null;
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
                try {
                    vuuid = line.getVuuid().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                if(uuids != null){
                    photo = null;
                }
                List postComments = getPostComment(id);


                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("photo", photo);
                jsonObject.put("class_target", class_target);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("uuids",uuids);
                jsonObject.put("vuuid",vuuid);
                jsonObject.put("post_comments",postComments);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getOnlineTeacher(String type, Integer page,String openid,String id) {

        try {
            Random random = new Random();
            SimpleDateFormat df_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date =new Date();
            String update_time = df_now.format(date);
            List<Message> messages_all = dao.getAllOnlineTeacher();
            int max = messages_all.size();
            if(max>1){
                int randomNumber = random.nextInt(max);
                String random_id = messages_all.get(randomNumber).getId();
                String openid_get = messages_all.get(randomNumber).getOpenid();
                List<User> users = dao.getUserByOpenid(openid_get);
                String role = users.get(0).getRole();
                if(!"client".equals(role)){
                    dao.updateVideoTop(Integer.parseInt(random_id),update_time);
                }
            }

        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }


        Integer page_start = (page - 1) * 3;
        Integer page_length = 3;
        List<JSONObject> resul_list = new ArrayList<>();

        int hasSend = 0;
        int boss_count = 0;
        int client_count = 0;
        try {
            List<Message> messages =dao.getOnlineTeacherByOpenid(openid);
            hasSend = 0;
            if(messages.size() > 0){
                hasSend = 1;
            }
            List<User> messages1 = dao.getUserByRole("boss");
            boss_count = messages1.size();
            List<User> messages2 = dao.getUserByRole("client");
            client_count = messages2.size();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            List<Message> list = dao.getOnlineTeacher(type, page_start, page_length);
            if(page == 1 && !"noid".equals(id)){
                List<Message> list_init = dao.getUuidById(Integer.parseInt(id));
                list_init.addAll(list);
                list = list_init;
            }
            for (int i = 0; i < list.size(); i++) {
                String uuids = null;
                String vuuid = null;
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                String student_name = line.getStudent_name();
                String comment = line.getComment();
                String class_target = line.getClass_target();
                String id_get = line.getId();
                String create_time = line.getCreate_time();
                String studio = line.getStudio();
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                try {
                    vuuid = line.getVuuid().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                String nick_name = null;
                String openid_get = null;
                String role = null;
                String subject = null;
                String city = null;
                try {
                    openid_get = line.getOpenid();
                    List<User> users = dao.getUser(openid_get);
                    User user = users.get(0);
                    nick_name = user.getNick_name();
                    role = user.getRole();
                    subject = user.getSubject();
                    city = user.getCity();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                //json
                if("boss".equals(role) || "teacher".equals(role)){
                    jsonObject.put("student_name", student_name);
                    jsonObject.put("comment", comment);
                    jsonObject.put("nick_name", nick_name);
                    jsonObject.put("class_target", class_target);
                    jsonObject.put("id", id_get);
                    jsonObject.put("create_time", create_time);
                    jsonObject.put("uuids",uuids);
                    jsonObject.put("vuuid",vuuid);
                    jsonObject.put("studio",studio);
                    jsonObject.put("hasSend",hasSend);
                    jsonObject.put("boss_count",boss_count);
                    jsonObject.put("client_count",client_count);
                    jsonObject.put("openid_get",openid_get);
                    jsonObject.put("subject",subject);
                    jsonObject.put("city",city);
                    resul_list.add(jsonObject);
                }
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
    public List getLessonHead(String studio, String student_name, String subject, String campus,String month_date) {
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
        Integer abnormal_lesson = 0;
        Integer abnormal_package = 0;
        DecimalFormat df = new DecimalFormat("0.00");
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM");//
//        String month_date = df1.format(new Date());

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
                    String subject_get = lesson.getSubject();
                    Integer is_combine = lesson.getIs_combine();

                    Float total = 0.0f;
                    Float disc = 0.0f;
                    Float all_lesson = 0.0f;
                    Float give_lesson = 0.0f;
                    Float package_lesson = 0.0f;
                    List<LessonPackage> lessonPackages1 = null;
                    if(is_combine == 0){
                        lessonPackages1 = dao.getLessonPackageByStudentSubject(student_name_all,studio,campus,subject_get);
                    }else if (is_combine == 1){
                        lessonPackages1 = dao.getLessonPackageByStudentCombine(student_name_all,studio,campus);
                    }

                    if(lessonPackages1.size()>0){
                        for(int j = 0; j < lessonPackages1.size(); j++){
                            LessonPackage lessonPackage = lessonPackages1.get(j);
                            total = total + lessonPackage.getTotal_money();
                            disc = disc + lessonPackage.getDiscount_money();
                            all_lesson = all_lesson + lessonPackage.getAll_lesson();
                            give_lesson = give_lesson + lessonPackage.getGive_lesson();
                        }
                        package_lesson = all_lesson + give_lesson;
                    }

                    Float consume_lesson = 0.0f;
                    Float consume_lesson_get = 0.0f;
                    Float lesson_gap = total_amount - left_amount;
                    try {
                        if(is_combine == 0){
                            consume_lesson_get = dao.getAllSignUpByStudent(studio,subject_get,campus,student_name_all);
                        }else if (is_combine == 1){
                            consume_lesson_get = dao.getAllSignUpByStudentCombine(studio,campus,student_name_all);
                        }

                        if(consume_lesson_get > 0){
                            consume_lesson = consume_lesson_get;
                        }
                    } catch (Exception e) {
//                            throw new RuntimeException(e);
                    }

                    int compareToResult1 = consume_lesson.compareTo(lesson_gap);
                    int compareToResult2 = package_lesson.compareTo(total_amount);

                    if(compareToResult1 != 0){
                        abnormal_lesson = abnormal_lesson + 1;
                    }

                    if(compareToResult2 != 0){
                        abnormal_package = abnormal_package + 1;
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
            } catch (Exception e) {
//                throw new RuntimeException(e);
            }

            try {
                AllCount allCount1 = dao.getLessonAllCountNewStudent(studio,campus,month_date);
                new_student = allCount1.getStudent_count();
            } catch (Exception e) {
//                throw new RuntimeException(e);
            }

            try {
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
            jsonObject.put("abnormal_lesson", abnormal_lesson);
            jsonObject.put("abnormal_package", abnormal_package);

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
    public List getLessonByStudioCampus(String studio, String campus) {

        List<Lesson> list = null;
        List<JSONObject> resul_list = new ArrayList<>();
        list = dao.getLessonByStudioCampus(studio,campus);
        try {
            for (int i = 0; i < list.size(); i++) {
                Float all_lesson = 0.0f;
                Float give_lesson = 0.0f;
                Integer total_student =0;
                Float total_amount_all = 0.0f ;
                Float left_amount_all = 0.0f ;
                Integer need_pay = 0;
                Integer owe = 0;
                Integer delete_status = 0;
                String student_name = null;
                String official_openid = null;
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
                String parent = "未绑定";
                String phone_number = "未录入";
                Float total_money = 0.0f ;
                Float discount_money = 0.0f ;
                JSONObject jsonObject = new JSONObject();
                Lesson line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                try {
                    List<User> users = dao.getUserByStudent(student_name,studio);
                    parent = users.get(0).getNick_name();
                    phone_number = users.get(0).getPhone_number();
                    official_openid = users.get(0).getOfficial_openid();
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
                delete_status = line.getDelete_status();
                Integer is_combine = line.getIs_combine();
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
                            all_lesson = all_lesson + lessonPackage.getAll_lesson();
                            give_lesson = give_lesson + lessonPackage.getGive_lesson();
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                Float receipts = total_money - discount_money;
                Float re_price = receipts/(all_lesson+give_lesson);;
                if(re_price>0){
                    price = re_price;
                }
                Float left_money = price * left_amount;
                if(total_money == 0.0f){
                    left_money = 0.0f;
                }

                Float consume_amount = 0.0f;
                try {
                    Float consume_lesson_get = dao.getAllSignUpByStudent(studio,subject_get,campus,student_name);
                    if(consume_lesson_get > 0){
                        consume_amount = consume_lesson_get;
                    }
                } catch (Exception e) {
//                            throw new RuntimeException(e);
                }

                DecimalFormat df = new DecimalFormat("0.00");

                //json
                jsonObject.put("consume_amount", consume_amount);
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
                jsonObject.put("parent", parent);
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("is_combine", combine);
                jsonObject.put("price",df.format(price));
                jsonObject.put("total_money", df.format(total_money));
                jsonObject.put("discount_money", df.format(discount_money));
                jsonObject.put("receipts", df.format(receipts));
                jsonObject.put("left_money", df.format(left_money));
                jsonObject.put("delete_status", delete_status);
                jsonObject.put("official_status", "未关注");
                if(official_openid != null){
                    jsonObject.put("official_status", "已关注");
                }
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getAllUserByStudio(String studio) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
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
                    commentStyle_cn = "全部";
                }else if("self".equals(comment_style)){
                    commentStyle_cn = "个人";
                }

                Integer is_open_get = line.getIs_open();
                String is_open_name = "全开";
                if(is_open_get == 0){
                    is_open_name = "受限";
                }

                String campus = line.getCampus();
                String expired_time = line.getExpired_time();
                String create_time = line.getCreate_time();

                String today_time = df.format(new Date());
                Date today_dt = df.parse(today_time.substring(0,10));
                Date create_time_dt = df.parse(create_time.substring(0,10));
                Date expired_time_dt = df.parse(expired_time.substring(0,10));

                long user_diff = today_dt.getTime() - create_time_dt.getTime();
                long use_days = user_diff / (24*60*60*1000);

                long pay_diff = expired_time_dt.getTime()-today_dt.getTime();
                long pay_days = pay_diff / (24*60*60*1000);

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
                jsonObject.put("expired_time", expired_time.substring(0,10));
                jsonObject.put("create_time", create_time.substring(0,10));
                jsonObject.put("openid", openid);
                jsonObject.put("id", id);
                jsonObject.put("subjects", subjects);
                jsonObject.put("member", member);
                jsonObject.put("pay_days", pay_days);
                jsonObject.put("use_days", use_days);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getUserByRole(String role) {
        List<User> list = null;
        List<JSONObject> resul_list = new ArrayList<>();
        list = dao.getUserByRole(role);
        try {
            for (int i = 0; i < list.size(); i++) {
                String role_cn = null;
                String commentStyle_cn = null;
                JSONObject jsonObject = new JSONObject();
                User line = list.get(i);
                //获取字段
                String studio = line.getStudio();
                String student_name = line.getStudent_name();
                String nick_name = line.getNick_name();

                //json
                jsonObject.put("studio", studio);
                jsonObject.put("student_name", student_name);
                jsonObject.put("nick_name", nick_name);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getAllUserByStudioByPage(String studio, Integer page) {
        Integer page_start = (page - 1) * 30;
        Integer page_length = 30;
        List<User> list = null;
        List<User> all_user = null;
        Integer all_user_count = 0;
        List<JSONObject> resul_list = new ArrayList<>();
        list = dao.getAllUserByStudioByPage(studio,page_start,page_length);
        if(page ==1 ){
            all_user = dao.getAllUserByStudio(studio);
            all_user_count = all_user.size();
        }
        try {
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                User line = list.get(i);
                //获取字段
                studio = line.getStudio();
                String student_name = line.getStudent_name();
                String nick_name = line.getNick_name();
                String role = line.getRole();
                String role_cn = "家长";
                if("teacher".equals(role)){
                    role_cn = "老师";
                }else if("boss".equals(role)){
                    role_cn = "校长";
                }
                String comment_style = line.getComment_style();
                String commentStyle_cn = "全部";
                 if("self".equals(comment_style)){
                    commentStyle_cn = "个人";
                }

                String campus = line.getCampus();
                String expired_time = line.getExpired_time();
                String openid = line.getOpenid();
                String id = line.getId();
                String subjects = line.getSubjects();
                String member = line.getMember();
                String phone_number =line.getPhone_number();
                Integer is_open_get = line.getIs_open();
                String is_open_name = "全开";
                if(is_open_get == 0){
                    is_open_name = "受限";
                }

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
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("is_open_name", is_open_name);
                jsonObject.put("all_user_count", all_user_count);
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
                String student_name = line.getStudent_name();
                String role = line.getRole();
                if("boss".equals(role)){
                    role_cn = "校长";
                }else if("teacher".equals(role)){
                    role_cn = "老师";
                }else if("client".equals(role)){
                    role_cn = "家长";
                }

                String phone_number =line.getPhone_number();
                String comment_style = line.getComment_style();
                if("public".equals(comment_style)){
                    commentStyle_cn = "全部";
                }else if("self".equals(comment_style)){
                    commentStyle_cn = "个人";
                }

                Integer is_open_get = line.getIs_open();
                String is_open_name = "全开";
                if(is_open_get == 0){
                    is_open_name = "受限";
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
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("is_open_name", is_open_name);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }


    @Override
    public List getAnalyzeDetail(String studio, String dimension, String campus,String date_time,String duration_time) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            Date d = fmt.parse(date_time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            String start_date = null;
            String end_date = null;


            if("日".equals(dimension)){
                cal.add(Calendar.DATE,-7);
                start_date = fmt.format(cal.getTime());
                end_date = date_time;

                if(!"无_无".equals(duration_time)){
                    start_date = duration_time.split("_")[0];
                    end_date = duration_time.split("_")[1];
                }

                List<AnalyzeCount> list = dao.getAnalyzeSignUp(studio,campus,start_date,end_date);
                for(int i=0;i< list.size();i++){
                    JSONObject jsonObject = new JSONObject();
                    Float signCount = 0.0f;
                    Float tryCount = 0.0f;
                    Float leaveCount = 0.0f;
                    Float lessonCount = 0.0f;
                    Float weekPrice = 0.0f;
                    Float all_lesson_count = 0.0f;
                    String create_time = list.get(i).getCreate_time();
                    signCount = list.get(i).getSign_count();
                    lessonCount = list.get(i).getLesson_count();
                    List<SignUp> signUps = dao.getAnalyzeSignUpDetail(studio,campus,create_time);
                    if(signUps.size() > 0){
                        for (int j = 0; j < signUps.size(); j++) {
                            SignUp signUp = signUps.get(j);
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
                                    Float all_lesson = 0.0f;
                                    Float give_lesson = 0.0f;
                                    List<LessonPackage> lessonPackages = dao.getLessonPackageByStudentSubject(student_name,studio,campus,subject);
                                    if(lessonPackages.size()>0){
                                        for (int k = 0; k < lessonPackages.size(); k++) {
                                            Float total_money_get = lessonPackages.get(k).getTotal_money();
                                            Float dis_money_get = lessonPackages.get(k).getDiscount_money();
                                            Float all_lesson_get = lessonPackages.get(k).getAll_lesson();
                                            Float give_lesson_get = lessonPackages.get(k).getGive_lesson();
                                            total_money = total_money + total_money_get;
                                            dis_money = dis_money + dis_money_get;
                                            all_lesson = all_lesson + all_lesson_get;
                                            give_lesson = give_lesson + give_lesson_get;
                                        }
                                        if(total_money>0){
                                            price = (total_money - dis_money)/(all_lesson - give_lesson);
                                        }
                                        weekPrice = weekPrice + price*count;
                                    }
                                }
                            } catch (Exception e) {
                            throw new RuntimeException(e);
                            }
                        }
                    }

                    List<AnalyzeCount> list1 = dao.getAnalyzeTry(studio,campus,create_time);
                    if(list1.size() > 0){
                        tryCount = list1.get(0).getTry_count();
                    }

                    List<AnalyzeCount> list2 = dao.getAnalyzeLeave(studio,campus,create_time);
                    if(list2.size() > 0){
                        leaveCount = list2.get(0).getLeave_count();
                    }

                    List<AnalyzeCount> list3 = dao.getLessonAllCountBySumUp(studio,campus,create_time);
                    if(list3.size() > 0){
                        all_lesson_count = list3.get(0).getLesson_count();
                    }

                    DecimalFormat df = new DecimalFormat("0.00");
                    jsonObject.put("create_time", create_time.substring(0,10));
                    jsonObject.put("tryCount", tryCount);
                    jsonObject.put("leaveCount", leaveCount);
                    jsonObject.put("signCount", signCount);
                    jsonObject.put("lessonCount", lessonCount);
                    jsonObject.put("all_lesson_count", all_lesson_count);
                    jsonObject.put("weekPrice", df.format(weekPrice));
                    jsonObject.put("rate", df.format(signCount/all_lesson_count*100));
                    resul_list.add(jsonObject);
                }
            }else if("月".equals(dimension)){
                cal.add(Calendar.DATE,-31);
                start_date = fmt.format(cal.getTime()).substring(0,7);
                end_date = date_time.substring(0,7);

                if(!"无_无".equals(duration_time)){
                    start_date = duration_time.split("_")[0].substring(0,7);
                    end_date = duration_time.split("_")[1].substring(0,7);
                }

                List<AnalyzeCount> list = dao.getAnalyzeSignUpByMonth(studio,campus,start_date,end_date);
                for(int i=0;i< list.size();i++){
                    JSONObject jsonObject = new JSONObject();
                    Float signCount = 0.0f;
                    Float tryCount = 0.0f;
                    Float leaveCount = 0.0f;
                    Float lessonCount = 0.0f;
                    Float weekPrice = 0.0f;
                    Float all_lesson_count = 0.0f;
                    String create_time = list.get(i).getCreate_time();
                    signCount = list.get(i).getSign_count();
                    lessonCount = list.get(i).getLesson_count();
                    List<SignUp> signUps = dao.getAnalyzeSignUpDetailByMonth(studio,campus,create_time);
                    if(signUps.size() > 0){
                        for (int j = 0; j < signUps.size(); j++) {
                            SignUp signUp = signUps.get(j);
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
                                    Float all_lesson = 0.0f;
                                    Float give_lesson = 0.0f;
                                    List<LessonPackage> lessonPackages = dao.getLessonPackageByStudentSubject(student_name,studio,campus,subject);
                                    if(lessonPackages.size()>0){
                                        for (int k = 0; k < lessonPackages.size(); k++) {
                                            Float total_money_get = lessonPackages.get(k).getTotal_money();
                                            Float dis_money_get = lessonPackages.get(k).getDiscount_money();
                                            Float all_lesson_get = lessonPackages.get(k).getAll_lesson();
                                            Float give_lesson_get = lessonPackages.get(k).getGive_lesson();
                                            total_money = total_money + total_money_get;
                                            dis_money = dis_money + dis_money_get;
                                            all_lesson = all_lesson + all_lesson_get;
                                            give_lesson = give_lesson + give_lesson_get;
                                        }
                                        if(total_money>0){
                                            price = (total_money - dis_money)/total_amount;
                                        }
                                        weekPrice = weekPrice + price*count;
                                    }

                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    List<AnalyzeCount> list1 = dao.getAnalyzeTryByMonth(studio,campus,create_time);
                    if(list1.size() > 0){
                        tryCount = list1.get(0).getTry_count();
                    }

                    List<AnalyzeCount> list2 = dao.getAnalyzeLeaveByMonth(studio,campus,create_time);
                    if(list2.size() > 0){
                        leaveCount = list2.get(0).getLeave_count();
                    }

                    List<AnalyzeCount> list3 = dao.getLessonAllCountBySumUpMonth(studio,campus,create_time);
                    if(list3.size() > 0){
                        all_lesson_count = list3.get(0).getLesson_count()*4;
                    }

                    DecimalFormat df = new DecimalFormat("0.00");
                    jsonObject.put("create_time", create_time);
                    jsonObject.put("tryCount", tryCount);
                    jsonObject.put("leaveCount", leaveCount);
                    jsonObject.put("signCount", signCount);
                    jsonObject.put("lessonCount", lessonCount);
                    jsonObject.put("all_lesson_count", all_lesson_count);
                    jsonObject.put("weekPrice", df.format(weekPrice));
                    jsonObject.put("rate", df.format(signCount/all_lesson_count*100));
                    resul_list.add(jsonObject);
                }
            }


        } catch (ParseException e) {
//            throw new RuntimeException(e);
        }


        return resul_list;
    }

    @Override
    public List getAnalyzeDetailWeek(String studio, String type, String weekday,String campus) {

        List<JSONObject> resul_list = new ArrayList<>();

        if(weekday.length() == 7){
            if("出勤数".equals(type)){
                List<AnalyzeCount> list = dao.getAnalyzeSignUpByMonthByStudent(studio,campus,weekday,weekday);
                for(int i=0;i< list.size();i++){
                    JSONObject jsonObject = new JSONObject();
                    Float weekPrice = 0.0f;
                    Float all_lesson_count = 0.0f;
                    String student_name = list.get(i).getStudent_name();
                    Float signCount = list.get(i).getSign_count();
                    Float lessonCount = list.get(i).getLesson_count();
                    List<SignUp> signUps = dao.getAnalyzeSignUpDetailByMonthByStudent(studio,campus,weekday,student_name);
                    if(signUps.size() > 0){
                        for (int j = 0; j < signUps.size(); j++) {
                            SignUp signUp = signUps.get(j);
                            String subject = signUp.getSubject();
                            Float count = signUp.getCount();
                            try {
                                List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                                if(lessons.size()>0){
                                    Float total_amount = lessons.get(0).getTotal_amount();
                                    Float price = lessons.get(0).getPrice();
                                    Float total_money = 0.0f;
                                    Float dis_money = 0.0f;
                                    Float all_lesson = 0.0f;
                                    Float give_lesson = 0.0f;
                                    List<LessonPackage> lessonPackages = dao.getLessonPackageByStudentSubject(student_name,studio,campus,subject);
                                    if(lessonPackages.size()>0){
                                        for (int k = 0; k < lessonPackages.size(); k++) {
                                            Float total_money_get = lessonPackages.get(k).getTotal_money();
                                            Float dis_money_get = lessonPackages.get(k).getDiscount_money();
                                            Float all_lesson_get = lessonPackages.get(k).getAll_lesson();
                                            Float give_lesson_get = lessonPackages.get(k).getGive_lesson();
                                            total_money = total_money + total_money_get;
                                            dis_money = dis_money + dis_money_get;
                                            all_lesson = all_lesson + all_lesson_get;
                                            give_lesson = give_lesson + give_lesson_get;
                                        }
                                        if(total_money>0){
                                            price = (total_money - dis_money)/total_amount;
                                        }
                                        weekPrice = weekPrice + price*count;
                                    }

                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    List<AnalyzeCount> list3 = dao.getLessonAllCountBySumUpMonthByStudent(studio,campus,student_name);
                    if(list3.size() > 0){
                        all_lesson_count = list3.get(0).getLesson_count()*4;
                    }
                    DecimalFormat df = new DecimalFormat("0.00");
                    jsonObject.put("create_time", weekday);
                    jsonObject.put("student_name", student_name);
                    jsonObject.put("signCount", signCount);
                    jsonObject.put("lessonCount", lessonCount);
                    jsonObject.put("all_lesson_count", all_lesson_count);
                    jsonObject.put("weekPrice", df.format(weekPrice));
                    jsonObject.put("rate", df.format(signCount/all_lesson_count*100));
                    resul_list.add(jsonObject);
                }
            }
        }else if(weekday.length() == 10){
            if("出勤数".equals(type)){
                List<AnalyzeCount> list = dao.getAnalyzeSignUpByStudent(studio,campus,weekday,weekday);
                for(int i=0;i< list.size();i++){
                    JSONObject jsonObject = new JSONObject();
                    Float weekPrice = 0.0f;
                    Float all_lesson_count = 0.0f;
                    String student_name = list.get(i).getStudent_name();
                    Float signCount = list.get(i).getSign_count();
                    Float lessonCount = list.get(i).getLesson_count();
                    List<SignUp> signUps = dao.getAnalyzeSignUpDetailByStudent(studio,campus,weekday,student_name);
                    if(signUps.size() > 0){
                        for (int j = 0; j < signUps.size(); j++) {
                            SignUp signUp = signUps.get(j);
                            String subject = signUp.getSubject();
                            Float count = signUp.getCount();
                            try {
                                List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                                if(lessons.size()>0){
                                    Float total_amount = lessons.get(0).getTotal_amount();
                                    Float price = lessons.get(0).getPrice();
                                    Float total_money = 0.0f;
                                    Float dis_money = 0.0f;
                                    Float all_lesson = 0.0f;
                                    Float give_lesson = 0.0f;
                                    List<LessonPackage> lessonPackages = dao.getLessonPackageByStudentSubject(student_name,studio,campus,subject);
                                    if(lessonPackages.size()>0){
                                        for (int k = 0; k < lessonPackages.size(); k++) {
                                            Float total_money_get = lessonPackages.get(k).getTotal_money();
                                            Float dis_money_get = lessonPackages.get(k).getDiscount_money();
                                            Float all_lesson_get = lessonPackages.get(k).getAll_lesson();
                                            Float give_lesson_get = lessonPackages.get(k).getGive_lesson();
                                            total_money = total_money + total_money_get;
                                            dis_money = dis_money + dis_money_get;
                                            all_lesson = all_lesson + all_lesson_get;
                                            give_lesson = give_lesson + give_lesson_get;
                                        }
                                        if(total_money>0){
                                            price = (total_money - dis_money)/total_amount;
                                        }
                                        weekPrice = weekPrice + price*count;
                                    }

                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    List<AnalyzeCount> list3 = dao.getLessonAllCountBySumUpByStudent(studio,campus,weekday,student_name);
                    if(list3.size() > 0){
                        all_lesson_count = list3.get(0).getLesson_count();
                    }
                    DecimalFormat df = new DecimalFormat("0.00");
                    jsonObject.put("create_time", weekday);
                    jsonObject.put("student_name", student_name);
                    jsonObject.put("signCount", signCount);
                    jsonObject.put("lessonCount", lessonCount);
                    jsonObject.put("all_lesson_count", all_lesson_count);
                    jsonObject.put("weekPrice", df.format(weekPrice));
                    jsonObject.put("rate", df.format(signCount/all_lesson_count*100));
                    resul_list.add(jsonObject);
                }
            }
        }

        return resul_list;
    }

    @Override
    public List getLessonByPage(String studio,String student_name,String subject,String openid,Integer page) {
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
        Integer dayofweek_by = 0;
        List<String> list_choose = new ArrayList<>();

        List<User> list_user = dao.getUser(openid);
        User user_r = list_user.get(0);
        String campus = user_r.getCampus();
        String role = user_r.getRole();
        Integer is_open = user_r.getIs_open();
        if(is_open == 0 && "teacher".equals(role)){
            page_start = (page - 1) * 10000;
            page_length = 10000;
        }
        String lessons = null;

        try {
            lessons = user_r.getLessons();
            if(lessons != null && "teacher".equals(role) && is_open == 0){
                String[] lessons_all =lessons.split("\\|");
                for(int num = 0; num < lessons_all.length; num ++){
                    String lesson_string = lessons_all[num];
                    String[] lesson_tring_list = lesson_string.split(",");
                    String week_string = lesson_tring_list[0].replace("星期","");
                    Integer week = Integer.parseInt(week_string);
                    if(week==7){
                        dayofweek_by=1;
                    }else {
                        dayofweek_by = week + 1;
                    }

                    String subject_t = lesson_tring_list[1];
                    String class_number_t = lesson_tring_list[2];
                    String duration_t = lesson_tring_list[3];
                    List<Schedule> schedules = dao.getScheduleDetail(dayofweek_by,duration_t,studio,class_number_t,subject_t,campus);
                    for(int numm = 0; numm < schedules.size(); numm ++){
                        String student_get = schedules.get(numm).getStudent_name();
                        list_choose.add(student_get);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


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
                Float all_lesson = 0.0f ;
                Float give_lesson = 0.0f ;
                String parent = "未绑定";
                String avatarurl ="https://thirdwx.qlogo.cn/mmopen/vi_32/y667SLJ40Eic5fMnHdibjO4vLG7dmqgjeuwjQbRN5ZJj6uZfl06yA7P9wwl7oYjNRFzBzwcheZtK8zvkibyfamfBA/132";
                String official_openid = null;
                String phone_number = "未录入";
                Float price = 0.0f;
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
                        official_openid = user.get(0).getOfficial_openid();
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
                Integer delete_status = line.getDelete_status();
                String age = line.getAge();

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
                            all_lesson = all_lesson + lessonPackage.getAll_lesson();
                            give_lesson = give_lesson + lessonPackage.getGive_lesson();
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                Float consume_amount = 0.0f;
                Float consume_lesson_get = 0.0f;
                try {
                    if(is_combine == 0){
                        consume_lesson_get = dao.getAllSignUpByStudent(studio,subject_get,campus,student_name);
                    }else if(is_combine ==1 ){
                        consume_lesson_get = dao.getAllSignUpByStudentCombine(studio,campus,student_name);
                    }

                    if(consume_lesson_get > 0){
                        consume_amount = consume_lesson_get;
                    }
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }

                Float receipts = total_money - discount_money;
                Float re_price = receipts/(all_lesson + give_lesson);
                if(re_price>0){
                    price = re_price;
                }
                Float left_money = price * left_amount;
                if(total_money == 0.0f){
                    left_money = 0.0f;
                }

                DecimalFormat df = new DecimalFormat("0.00");

                if("boss".equals(role) || is_open == 1){
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
                    jsonObject.put("delete_status", delete_status);
                    jsonObject.put("age", age);
                    jsonObject.put("consume_amount", consume_amount);
                    jsonObject.put("official_status", "未关注");
                    if(official_openid != null){
                        jsonObject.put("official_status", "已关注");
                    }

                    resul_list.add(jsonObject);
                }else if("teacher".equals(role) && is_open == 0 && list_choose.contains(student_name) ){
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
                    jsonObject.put("delete_status", delete_status);
                    jsonObject.put("age", age);
                    jsonObject.put("consume_amount", consume_amount);
                    jsonObject.put("official_status", "未关注");
                    if(official_openid != null){
                        jsonObject.put("official_status", "已关注");
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
    public List getTipsDataUrl(String studio,Integer left_amount_get,String subject,String campus_in,String type,String month_date) {
        List<Lesson> list = null;
        List<String> renew_students = new ArrayList<>();
        List<JSONObject> resul_list = new ArrayList<>();
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM");//
//        String month_date = df1.format(new Date());

        try {
            if("owePay".equals(type) || "needPay".equals(type) || "renew".equals(type)){
                if("全科目".equals(subject)){
                    list = dao.getTipsDataUrlAll(studio,left_amount_get,campus_in);
                }else{
                    list = dao.getTipsDataUrl(studio,left_amount_get,subject,campus_in);
                }

                if("renew".equals(type)){
                    List<LessonPackage> lessonPackages = dao.getLessonPackageRenew(studio,campus_in,month_date);
                    for(int ii = 0;ii < lessonPackages.size();ii++){
                        String student_name = lessonPackages.get(ii).getStudent_name();
                        renew_students.add(student_name);
                    }
                }

            }else if("new".equals(type)){
                list = dao.getLessonNew(studio,campus_in,month_date);
            }else if("loss".equals(type)){
                list = dao.getLessonLoss(studio,campus_in,month_date);
            }

            for (int i = 0; i < list.size(); i++) {
                Float all_lesson = 0.0f;
                Float give_lesson = 0.0f;
                String student_name =null;
                String campus =null;
                Integer is_combine = 0;
                Float total_amount = 0.0f;
                String create_time = null;
                String id = null;
                Integer points = 0;
                Float percent = 0.0f;
                Float minus = 0.0f;
                Float coins = 0.0f;
                Float left_amount = 0.0f;
                Float total_money = 0.0f ;
                Float discount_money = 0.0f ;
                String parent = "未绑定";
                String avatarurl = "未绑定";
                String phone_number = "未录入";
                String official_openid = null;
                Float price = 0.0f;
                Integer delete_status = 0;
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
                        official_openid = user.get(0).getOfficial_openid();
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
                delete_status = line.getDelete_status();
                String age = line.getAge();
                String combine = "分";
                if(is_combine == 1){
                    combine = "合";
                }

                try {
                    List<LessonPackage> lessonPackages = dao.getLessonPackage(student_name,studio,campus,subject);
                    if(lessonPackages.size()>0){
                        for(int j = 0; j < lessonPackages.size(); j++){
                            LessonPackage lessonPackage = lessonPackages.get(j);
                            total_money = total_money + lessonPackage.getTotal_money();
                            discount_money = discount_money + lessonPackage.getDiscount_money();
                            all_lesson = all_lesson + lessonPackage.getAll_lesson();
                            give_lesson = give_lesson + lessonPackage.getGive_lesson();
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                Float receipts = total_money - discount_money;
                Float re_price = receipts/(all_lesson+give_lesson);
                if(re_price>0){
                    price = re_price;
                }
                Float left_money = price * left_amount;
                if(total_money == 0.0f){
                    left_money = 0.0f;
                }

                Float consume_amount = 0.0f;
                try {
                    Float consume_lesson_get = dao.getAllSignUpByStudent(studio,subject,campus,student_name);
                    if(consume_lesson_get > 0){
                        consume_amount = consume_lesson_get;
                    }
                } catch (Exception e) {
//                            throw new RuntimeException(e);
                }

                DecimalFormat df = new DecimalFormat("0.00");

                //json
                jsonObject.put("consume_amount", consume_amount);
                jsonObject.put("price", price);
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
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("total_money", df.format(total_money));
                jsonObject.put("discount_money", df.format(discount_money));
                jsonObject.put("receipts", df.format(receipts));
                jsonObject.put("left_money", df.format(left_money));
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("delete_status", delete_status);
                jsonObject.put("age", age);
                jsonObject.put("official_status", "未关注");
                if(official_openid != null){
                    jsonObject.put("official_status", "已关注");
                }

                if("renew".equals(type) && renew_students.contains(student_name)){
                    resul_list.add(jsonObject);
                }else if("owePay".equals(type) && left_amount < 0){
                    resul_list.add(jsonObject);
                }else if("needPay".equals(type) || "new".equals(type) || "loss".equals(type)){
                    resul_list.add(jsonObject);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getBookDetailByMonth(String openid, String book_name, String date_time) {
        List<BookDetail> list= null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getBookDetailByMonth(openid,book_name,date_time.substring(0,7));
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                BookDetail line = list.get(i);
                //获取字段
                String type = line.getType();
                String item = line.getItem();
                String mark = line.getMark();
                Float amount = line.getAmount();
                String create_time = line.getCreate_time();
                String id = line.getId();

                //json
                jsonObject.put("type", type);
                jsonObject.put("item", item);
                jsonObject.put("mark", mark);
                jsonObject.put("amount", amount);
                jsonObject.put("create_time", create_time);
                jsonObject.put("openid", openid);
                jsonObject.put("id", id);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getGoneStudent(String studio,String campus) {
        List<Lesson> list = null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getGoneStudent(studio,campus);
            for (int i = 0; i < list.size(); i++) {
                Float all_lesson = 0.0f;
                Float give_lesson = 0.0f;
                String student_name =null;
                Integer is_combine = 0;
                Float total_amount = 0.0f;
                String create_time = null;
                String id = null;
                Integer points = 0;
                Float percent = 0.0f;
                Float minus = 0.0f;
                Float coins = 0.0f;
                Float left_amount = 0.0f;
                Float total_money = 0.0f ;
                Float discount_money = 0.0f ;
                String parent = "未绑定";
                String avatarurl = "未绑定";
                String phone_number = "未录入";
                String official_openid = null;
                Float price = 0.0f;
                String subject = null;
                Integer delete_status = 0;
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
                        official_openid = user.get(0).getOfficial_openid();
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
                delete_status = line.getDelete_status();
                String age = line.getAge();
                String combine = "分";
                if(is_combine == 1){
                    combine = "合";
                }

                try {
                    List<LessonPackage> lessonPackages = dao.getLessonPackage(student_name,studio,campus,subject);
                    if(lessonPackages.size()>0){
                        for(int j = 0; j < lessonPackages.size(); j++){
                            LessonPackage lessonPackage = lessonPackages.get(j);
                            total_money = total_money + lessonPackage.getTotal_money();
                            discount_money = discount_money + lessonPackage.getDiscount_money();
                            all_lesson = all_lesson + lessonPackage.getAll_lesson();
                            give_lesson = give_lesson + lessonPackage.getGive_lesson();
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                Float receipts = total_money - discount_money;
                Float re_price = receipts/(all_lesson+give_lesson);
                if(re_price>0){
                    price = re_price;
                }
                Float left_money = price * left_amount;
                if(total_money == 0.0f){
                    left_money = 0.0f;
                }

                DecimalFormat df = new DecimalFormat("0.00");

                //json
                jsonObject.put("price", price);
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
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("total_money", df.format(total_money));
                jsonObject.put("discount_money", df.format(discount_money));
                jsonObject.put("receipts", df.format(receipts));
                jsonObject.put("left_money", df.format(left_money));
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("delete_status", delete_status);
                jsonObject.put("age", age);
                jsonObject.put("official_status", "未关注");
                if(official_openid != null){
                    jsonObject.put("official_status", "已关注");
                }
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getAbnormalStudent(String studio, String campus,String type) {
        List<Lesson> list = null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getLesson(studio,campus);
            for (int i = 0; i < list.size(); i++) {
                String student_name =null;
                Integer is_combine = 0;
                Float total_amount = 0.0f;
                String create_time = null;
                String id = null;
                Integer points = 0;
                Float percent = 0.0f;
                Float minus = 0.0f;
                Float coins = 0.0f;
                Float left_amount = 0.0f;
                Float total_money = 0.0f ;
                Float discount_money = 0.0f ;
                String parent = "未绑定";
                String avatarurl = "未绑定";
                String phone_number = "未录入";
                String official_openid = null;
                Float price = 0.0f;
                String subject = null;
                Integer delete_status = 0;
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
                        official_openid = user.get(0).getOfficial_openid();
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
                delete_status = line.getDelete_status();
                String age = line.getAge();
                String combine = "分";
                if(is_combine == 1){
                    combine = "合";
                }

                Float all_lesson = 0.0f;
                Float give_lesson = 0.0f;
                Float package_lesson = 0.0f;
                try {
                    List<LessonPackage> lessonPackages = null;
                    if(is_combine == 0){
                        lessonPackages = dao.getLessonPackage(student_name,studio,campus,subject);
                    }else if (is_combine == 1){
                        lessonPackages = dao.getLessonPackageByStudentCombine(student_name,studio,campus);
                    }
                    if(lessonPackages.size()>0){
                        for(int j = 0; j < lessonPackages.size(); j++){
                            LessonPackage lessonPackage = lessonPackages.get(j);
                            total_money = total_money + lessonPackage.getTotal_money();
                            discount_money = discount_money + lessonPackage.getDiscount_money();
                            all_lesson = all_lesson + lessonPackage.getAll_lesson();
                            give_lesson = give_lesson + lessonPackage.getGive_lesson();
                        }
                        package_lesson = all_lesson + give_lesson;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                Float receipts = total_money - discount_money;
                Float re_price = receipts/(all_lesson+give_lesson);
                if(re_price>0){
                    price = re_price;
                }
                Float left_money = price * left_amount;
                if(total_money == 0.0f){
                    left_money = 0.0f;
                }

                Float consume_lesson = 0.0f;
                Float consume_lesson_get = 0.0f;
                Float consume_amount = 0.0f;
                Float lesson_gap = total_amount - left_amount;
                try {
                    if(is_combine == 0){
                        consume_lesson_get = dao.getAllSignUpByStudent(studio,subject,campus,student_name);
                    }else if(is_combine == 1){
                        consume_lesson_get = dao.getAllSignUpByStudentCombine(studio,campus,student_name);
                    }


                    if(consume_lesson_get > 0){
                        consume_lesson = consume_lesson_get;
                        consume_amount = consume_lesson_get;
                    }
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }

                int compareToResult1 = consume_lesson.compareTo(lesson_gap);
                int compareToResult2 = package_lesson.compareTo(total_amount);

                int is_it = 0;
                if(type.equals("lesson")){
                    if(compareToResult1 != 0){
                        is_it = 1;
                    }
                }else if(type.equals("package")){
                    if(compareToResult2 != 0){
                        is_it = 1;
                    }
                }


                if(is_it == 1){
                    DecimalFormat df = new DecimalFormat("0.00");
                    jsonObject.put("consume_amount", consume_amount);
                    jsonObject.put("price", price);
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
                    jsonObject.put("phone_number", phone_number);
                    jsonObject.put("total_money", df.format(total_money));
                    jsonObject.put("discount_money", df.format(discount_money));
                    jsonObject.put("receipts", df.format(receipts));
                    jsonObject.put("left_money", df.format(left_money));
                    jsonObject.put("avatarurl", avatarurl);
                    jsonObject.put("delete_status", delete_status);
                    jsonObject.put("age", age);
                    jsonObject.put("official_status", "未关注");
                    if(official_openid != null){
                        jsonObject.put("official_status", "已关注");
                    }

                    jsonObject.put("consume_lesson", consume_lesson);
                    jsonObject.put("lesson_gap", lesson_gap);
                    jsonObject.put("package_lesson", package_lesson);
                    resul_list.add(jsonObject);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getLessonInName(String studio, String student_name,Integer page,String subject,String openid) {
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
        Integer length = student_name.split(",").length;
        List<JSONObject> resul_list = new ArrayList<>();
        String subject_get = null;
        StringBuilder student_names = new StringBuilder();
        String student_name_get = null;

        List<User> users = dao.getUserByOpenid(openid);
        String campus = users.get(0).getCampus();
        if(users.size()>0){
            for (int i = 0; i < users.size(); i++) {
                User line = users.get(i);
                student_name_get = line.getStudent_name();
                List<Lesson> lessons = dao.getLessonLikeName(studio,student_name_get,campus);
                if(lessons.size()>0){
                    for(int ii = 0;ii < lessons.size(); ii ++){
                        Lesson lesson = lessons.get(ii);
                        String student_lesson = lesson.getStudent_name();
                        String student_split = student_lesson.split("_")[0];
                        if(student_split.equals(student_name_get)){
                            student_names = student_names.append(student_lesson).append(",");
                        }
                    }
                }
            }
        }

        if(student_names.length()>0){
            student_names = student_names.deleteCharAt(student_names.lastIndexOf(","));
        }


        try {
            if (length>1) {
                if("全科目".equals(subject)){
                    list = dao.getLessonInName(studio,student_names.toString(),page_start,page_length,campus);
                }else {
                    list = dao.getLessonInNameBySubject(studio,student_names.toString(),page_start,page_length,subject,campus);
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
    public List getGoodsList(String studio, Integer page,String campus,String content,String type) {
        String goods_name = null;
        String goods_intro = null;
        String create_time = null;
        String id = null;
        Float goods_price = 0.0f;
        Float group_price = 0.0f;
        Integer page_start = (page - 1) * 10;
        Integer page_length = 10;
        List<GoodsList> list = null;
        List<GroupBuy> list_buy = null;
        String uuids = null;
        byte[] photo = null;
        Integer is_group = 0;
        Integer group_num = 0;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            if(type.equals("normal")){
                list = dao.getGoodsList(studio,page_start,page_length);
            }else if(type.equals("search")){
                list = dao.getGoodsListSearch(studio,page_start,page_length,content);
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                GoodsList line = list.get(i);
                //获取字段
                goods_name = line.getGoods_name();
                goods_intro = line.getGoods_intro();
                goods_price = line.getGoods_price();
                group_price = line.getGroup_price();
//                photo = line.getPhoto();
                id = line.getId();
                is_group = line.getIs_group();
                group_num = line.getGroup_num();
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
                jsonObject.put("group_price", group_price);
                jsonObject.put("group_num", group_num);
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
    public List getRating(String studio,String student_name,Integer page,String subject,String openid) {
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
        Integer dayofweek_by = 0;
        List<String> list_choose = new ArrayList<>();

        List<User> list_user = dao.getUser(openid);
        User user_r = list_user.get(0);
        String campus = user_r.getCampus();
        String role = user_r.getRole();
        Integer is_open = user_r.getIs_open();
        if(is_open == 0 && "teacher".equals(role)){
            page_start = (page - 1) * 10000;
            page_length = 10000;
        }


        String lessons = null;
        try {
            lessons = user_r.getLessons();
            if(lessons != null){
                String[] lessons_all =lessons.split("\\|");
                for(int num = 0; num < lessons_all.length; num ++){
                    String lesson_string = lessons_all[num];
                    String[] lesson_tring_list = lesson_string.split(",");
                    String week_string = lesson_tring_list[0].replace("星期","");
                    Integer week = Integer.parseInt(week_string);
                    if(week==7){
                        dayofweek_by=1;
                    }else {
                        dayofweek_by = week + 1;
                    }

                    String subject_t = lesson_tring_list[1];
                    String class_number_t = lesson_tring_list[2];
                    String duration_t = lesson_tring_list[3];
                    List<Schedule> schedules = dao.getScheduleDetail(dayofweek_by,duration_t,studio,class_number_t,subject_t,campus);
                    for(int numm = 0; numm < schedules.size(); numm ++){
                        String student_get = schedules.get(numm).getStudent_name();
                        list_choose.add(student_get);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if(student_name.equals("all")){
                if(subject.equals("全科目")){
                    list = dao.getRating(studio,page_start,page_length,campus);
                }else {
                    list = dao.getRatingBySubject(studio,page_start,page_length,subject,campus);
                }
            }else {
                if(subject.equals("全科目")){
                    list = dao.getRatingByName(studio,student_name,page_start,page_length);
                }else {
                    list = dao.getRatingByNameBySubject(studio,student_name,page_start,page_length,subject);
                }
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
                if("boss".equals(role) || is_open == 1){
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
                }else if("teacher".equals(role) && is_open == 0 && list_choose.contains(student_name)){
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

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return resul_list;
    }

    @Override
    public String downloadByOpenid(String studio,String openid,List<String> result_list,String title,String file_name){
        String path = "/data";
        studio = studio.replace("/","");
        String d_path = path +"/downloadData/"+ studio + "/"+ openid + "/" ;
        File file = new File(d_path);

        if (!file.exists()){ //如果不存在
            file.mkdirs(); //创建目录
        }

        String[] content = file.list();//取得当前目录下所有文件和文件夹
        for(String name : content){
            File temp = new File(d_path, name);
            temp.delete();
        }

        //获取类路径
        String p_path = null;
        p_path = path +"/downloadData/"+ studio + "/" + openid + "/" + file_name + ".xls";
        BufferedWriter bw = null;

        //保存csv
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(p_path),"UTF-8"));
            bw.write(title);
            bw.newLine();
            for(int i=0; i<result_list.size(); i++){
                String line= result_list.get(i);
                bw.write(line);
                bw.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(bw != null){
                    bw.flush();
                    bw.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return p_path;
    }

    @Override
    public List getStandings(String studio, String openid, String student_name, String subject) {
        List<User> list_user = dao.getUser(openid);
        String campus = list_user.get(0).getCampus();
        Integer my_points = 0;
        Integer rank = 0;
        List<JSONObject> resul_list = new ArrayList<>();

        List<Lesson> list = dao.getRating(studio,0,10000,campus);
        for (int i = 0; i < list.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            Lesson lesson = list.get(i);
            String student_name_get = lesson.getStudent_name();
            String subject_get = lesson.getSubject();
            Integer points = lesson.getPoints();
            if(student_name.equals(student_name_get) && subject.equals(subject_get)){
                my_points = my_points + points;
            }

            jsonObject.put("student_name", student_name_get);
            jsonObject.put("subject", subject_get);
            jsonObject.put("points", points);
            jsonObject.put("rank", i+1);
            resul_list.add(jsonObject);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("my_points", my_points);
        resul_list.add(jsonObject);

        return resul_list;
    }


}
