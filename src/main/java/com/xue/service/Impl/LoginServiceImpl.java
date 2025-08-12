package com.xue.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import redis.clients.jedis.Jedis;

import java.io.*;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
            String openid = order.getOpenid();
            String goods_name = order.getGoods_name();
            result = dao.insertOrder(order);
            if(result > 0){
                sendOrderNotice(openid,goods_name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int sendOrderNotice(String openid, String goods_name) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd HH:mm:ss");//设置日期格式
        String create_time = df.format(new Date());
        String result = null;
        String url_send = null;
        String model ="{\"touser\":\"openid\",\"template_id\":\"4Pkp58wCQy0cR5N-cTQuATysehBBvxwuyczrdsjHD2A\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing3\":{\"value\": \"AA\"},\"thing5\":{\"value\": \"A1\"},\"time2\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";
        String token = getToken("MOMO_OFFICIAL");

        List<User> users = dao.getUser(openid);
        User user = users.get(0);
        String studio = user.getStudio();
        String nick_name = user.getNick_name();

        try {
            List<User> list = dao.getBossByStudio(studio);
            list.addAll(users);
            for (int i = 0; i < list.size(); i++) {
                User user_get = list.get(i);
                String official_openid = user_get.getOfficial_openid();
                String openid_get = user_get.getOpenid();
                String role_get = user_get.getRole();
                url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
                if(official_openid != null){
                    String[] official_list = official_openid.split(",");
                    for(int j=0;j<official_list.length;j++){
                        String official_openid_get = official_list[j];
                        JSONObject queryJson = JSONObject.parseObject(model);
                        queryJson.put("touser",official_openid_get);
                        queryJson.getJSONObject("data").getJSONObject("thing3").put("value",nick_name);
                        queryJson.getJSONObject("data").getJSONObject("thing5").put("value",goods_name);
                        queryJson.getJSONObject("data").getJSONObject("time2").put("value",create_time);
                        queryJson.getJSONObject("miniprogram").put("pagepath","/pages/my_order/my_order?studio=" + studio + "&openid=" + openid_get + "&role=" + role_get);

                        System.out.println("MOMO_OFFICIAL_PARAM:" + queryJson.toJSONString());
                        result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
                        System.out.printf("MOMO_OFFICIAL_RES:" + result);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
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
            String related_id = null;
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
                    related_id = lesson_get.getRelated_id();
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

                try {
                    if(!"no_id".equals(related_id)){
                        String[] related_id_list = related_id.split(",");
                        for(int j=0;j < related_id_list.length; j++){
                            String id_get = related_id_list[j];
                            List<Lesson> lessons = dao.getLessonById(id_get);
                            if(lessons.size()>0){
                                Lesson lesson_get = lessons.get(0);
                                String student_name_get = lesson_get.getStudent_name();
                                if(!student_name.equals(student_name_get)){
                                    lesson.setStudent_name(student_name_get);
                                    dao.updateLesson(lesson);
                                }
                            }
                        }

                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateLessonRelated(String id, Integer related_id, String openid,String type) {
        int result = 0;
        try {
            List<Lesson> lessons = dao.getLessonById(id);
            Lesson lesson = lessons.get(0);
            String related_id_get = lesson.getRelated_id();
            Float total_amount = lesson.getTotal_amount();
            Float left_amount = lesson.getLeft_amount();

            if("关联".equals(type)){
                StringBuffer related_id_new = new StringBuffer();
                if(!"no_id".equals(related_id_get)){
                    String[] array = related_id_get.split(",");
                    List<String> list = new ArrayList<>();
                    for(int index = 0; index < array.length; index++) {
                        list.add(array[index]);
                    }

                    boolean flag_id = list.contains(id.toString());
                    if(flag_id == false){
                        list.add(id.toString());
                    }
                    boolean flag_related_id = list.contains(related_id.toString());
                    if(flag_related_id == false){
                        list.add(related_id.toString());
                    }

                    for(int i = 0; i < list.size(); i++){
                        String id_get = list.get(i);
                        if(id_get != "null" && id_get != null){
                            related_id_new.append(id_get);
                            related_id_new.append(",");
                        }
                    }
                }else{
                    related_id_new.append(id);
                    related_id_new.append(",");
                    related_id_new.append(related_id);
                    related_id_new.append(",");
                }

                // 更新所有关联ID
                String[] related_new_list = related_id_new.toString().split(",");
                for(int j = 0;j < related_new_list.length;j++){
                    String value  = related_new_list[j];
                    result = dao.updateLessonRelatedById(Integer.valueOf(value),related_id_new.toString(),total_amount,left_amount);
                }
            }else if ("取关".equals(type)){
                StringBuffer related_id_new = new StringBuffer();
                String[] array = related_id_get.split(",");
                List<String> list_get = Arrays.asList(array);
                List<String> list = new ArrayList<>();

                // 剔除元素
                for(int k = 0; k < list_get.size(); k++){
                    String value = list_get.get(k);
                    String id_str = id.toString();
                    if(!value.equals(id_str) ){
                        list.add(value);
                    }
                }

                if(list.size() == 1){
                    String id_get = list.get(0);
                    dao.updateLessonRelatedById(Integer.valueOf(id_get),"no_id",total_amount,left_amount);
                }else if(list.size() > 1){
                    for(int i = 0; i < list.size(); i++){
                        String id_get = list.get(i);
                        if(id_get != "null" && id_get != null){
                            related_id_new.append(id_get);
                            related_id_new.append(",");
                        }
                    }
                    // 更新所有关联ID
                    String[] related_new_list = related_id_new.toString().split(",");
                    for(int j = 0;j < related_new_list.length;j++){
                        String value  = related_new_list[j];
                        result = dao.updateLessonRelatedById(Integer.valueOf(value),related_id_new.toString(),total_amount,left_amount);
                    }
                }
                // 取消本人关联状态
                dao.updateLessonRelatedById(Integer.valueOf(id),"no_id",total_amount,left_amount);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
                String campus = line.getCampus();
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

                String create_time = line.getCreate_time();
                Integer views = line.getViews();
                String openid = line.getOpenid();
                String teacher = "默认";
                List<User> users_get = dao.getUserByOpenid(openid);
                if(users_get.size()>0){
                    User user_get = users_get.get(0);
                    teacher = user_get.getNick_name();
                }

                //json
                jsonObject.put("teacher", teacher);
                jsonObject.put("openid", openid);
                jsonObject.put("views", views);
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
                jsonObject.put("create_time", create_time);
                jsonObject.put("studio", studio);
                jsonObject.put("campus", campus);
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

                String create_time = line.getCreate_time();
                Integer views = line.getViews();
                String openid_get = line.getOpenid();
                String teacher = "默认";
                List<User> users_get = dao.getUserByOpenid(openid_get);
                if(users_get.size()>0){
                    User user_get = users_get.get(0);
                    teacher = user_get.getNick_name();
                }

                //json
                jsonObject.put("teacher", teacher);
                jsonObject.put("openid", openid_get);
                jsonObject.put("views", views);
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
                jsonObject.put("create_time", create_time);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getCommentLikeStudent(String openid,String content,String duration) {
        List<JSONObject> resul_list = new ArrayList<>();
        List<User> users = dao.getUserByOpenid(openid);
        User user = users.get(0);
        String studio = user.getStudio();
        String campus = user.getCampus();
        String role = user.getRole();
        String start_date = duration.split("_")[0];
        String end_date = duration.split("_")[1];

        try {
            List<Message> list = dao.getCommentLikeStudent(content,studio,campus,start_date,end_date);
            for (int i = 0; i < list.size(); i++) {
                Float percent = 0.0f;
                Float left = 0.0f;
                Float total = 0.0f;
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                String student_name = line.getStudent_name();
                String class_name = line.getClass_name();
                String comment = line.getComment();
                String class_target = line.getClass_target();
                String id = line.getId();
                String create_time = line.getCreate_time();

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
                String openid_get = line.getOpenid();
                String teacher = "默认";
                List<User> users_get = dao.getUserByOpenid(openid_get);
                if(users_get.size() > 0){
                    User user_get = users_get.get(0);
                    teacher = user_get.getNick_name();
                }
                String uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");;

                List<User> users1 = dao.getUserByStudentOpenid(student_name,studio,openid);

                //json
                jsonObject.put("openid", openid_get);
                jsonObject.put("teacher", teacher);
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("class_target", class_target);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("percent", percent);
                jsonObject.put("left", left);
                jsonObject.put("total",total);
                jsonObject.put("uuids",uuids);
                if("boss".equals(role) || "teacher".equals(role) || users1.size()>0){
                    resul_list.add(jsonObject);
                }
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
                String teacher = line.getTeacher();

                String package_mark = "无备注";
                Float all_lesson = 0.0f;
                Float given_lesson = 0.0f;
                String package_id = line.getPackage_id();

                try {
                    List<LessonPackage> lessonPackages = dao.getLessonPackageById(Integer.parseInt(package_id));
                    if(lessonPackages.size()>0){
                        LessonPackage lessonPackage = lessonPackages.get(0);
                        package_mark = lessonPackage.getMark();
                        all_lesson = lessonPackage.getAll_lesson();
                        given_lesson = lessonPackage.getGive_lesson();
                    }
                } catch (NumberFormatException e) {
//                    throw new RuntimeException(e);
                }

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
                jsonObject.put("package_mark", package_mark);
                jsonObject.put("all_lesson", all_lesson);
                jsonObject.put("given_lesson", given_lesson);
                jsonObject.put("package_id", package_id);
                jsonObject.put("teacher", teacher);
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
    public List getSignUpByBetween(String student_name, String subject, String openid,String duration_time) {
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<User> user_get= dao.getUser(openid);
            String campus = user_get.get(0).getCampus();
            String studio = user_get.get(0).getStudio();
            String[] duration_list = duration_time.split("_");
            String start_time = duration_list[0];
            String end_time = duration_list[1];

            List<SignUp> list = dao.getSignUpByBetween(student_name,studio,campus,subject,start_time,end_time);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                SignUp line = list.get(i);
                //获取字段
                String create_time = line.getCreate_time();
                String sign_time = line.getSign_time();
                String id = line.getId();
                String mark = line.getMark();
                String duration = line.getDuration();
                Float count = line.getCount();
                Integer ending_status_get = line.getEnding_status();
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getCardRecordByBetween(String student_name, String card_id, String subject, String openid, String duration_time) {
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<User> user_get= dao.getUser(openid);
            String campus = user_get.get(0).getCampus();
            String studio = user_get.get(0).getStudio();
            String[] duration_list = duration_time.split("_");
            String start_time = duration_list[0];
            String end_time = duration_list[1];

            List<CardRecord> list = dao.getCardRecordByBetween(student_name,card_id,studio,campus,subject,start_time,end_time);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                CardRecord line = list.get(i);

                //获取字段
                String mark = line.getMark();
                String duration = line.getDuration();
                String create_time = line.getCreate_time();
                String id = line.getId();
                String teacher = line.getTeacher();

                jsonObject.put("teacher",teacher);
                jsonObject.put("mark",mark);
                jsonObject.put("duration",duration);
                jsonObject.put("id",id);
                jsonObject.put("create_time",create_time);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getGiftList(String studio, String campus) {
        List<JSONObject> resul_list = new ArrayList<>();

        List<GiftList> giftLists = dao.getGiftList(studio,campus);
        for (int i = 0; i < giftLists.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            GiftList line = giftLists.get(i);
            //获取字段
            String create_time = line.getCreate_time();
            String gift_name = line.getGift_name();
            String uuids = line.getUuids();
            Integer coins = line.getCoins();
            String id = line.getId();
            String type = line.getType();
            Float price = line.getPrice();
            Integer coupon_type = line.getCoupon_type();
            String mark = line.getMark();
            Integer amount = line.getAmount();
            Integer send_type = line.getSend_type();

            //json
            jsonObject.put("id", id);
            jsonObject.put("create_time", create_time);
            jsonObject.put("gift_name", gift_name);
            jsonObject.put("uuids", uuids);
            jsonObject.put("coins", coins);
            jsonObject.put("type", type);
            jsonObject.put("price", price);
            jsonObject.put("coupon_type", coupon_type);
            jsonObject.put("mark", mark);
            jsonObject.put("amount", amount);
            jsonObject.put("send_type", send_type);
            resul_list.add(jsonObject);
        }
        return resul_list;
    }

    @Override
    public List getGiftListByCouponType(String studio, String campus, Integer coupon_type) {
        List<JSONObject> resul_list = new ArrayList<>();

        List<GiftList> giftLists = dao.getGiftListByCouponType(studio,campus,coupon_type);
        for (int i = 0; i < giftLists.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            GiftList line = giftLists.get(i);
            //获取字段
            String create_time = line.getCreate_time();
            String gift_name = line.getGift_name();
            String uuids = line.getUuids();
            Integer coins = line.getCoins();
            String id = line.getId();
            String type = line.getType();
            Float price = line.getPrice();
            coupon_type = line.getCoupon_type();
            String mark = line.getMark();
            Integer amount = line.getAmount();
            Integer send_type = line.getSend_type();

            //json
            jsonObject.put("id", id);
            jsonObject.put("create_time", create_time);
            jsonObject.put("gift_name", gift_name);
            jsonObject.put("uuids", uuids);
            jsonObject.put("coins", coins);
            jsonObject.put("type", type);
            jsonObject.put("price", price);
            jsonObject.put("coupon_type", coupon_type);
            jsonObject.put("mark", mark);
            jsonObject.put("amount", amount);
            jsonObject.put("send_type", send_type);
            resul_list.add(jsonObject);
        }
        return resul_list;
    }

    @Override
    public List getGiftByGiftId(String gift_id,String studio, String campus) {
        List<JSONObject> resul_list = new ArrayList<>();

        List<Gift> gifts = dao.getGiftByGiftId(gift_id,studio,campus);
        for (int i = 0; i < gifts.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            Gift line = gifts.get(i);
            //获取字段
            String create_time = line.getCreate_time();
            String expired_time = line.getExpired_time();
            String id = line.getId();
            Integer gift_amount = line.getGift_amount();
            Integer status = line.getStatus();

            String openid = line.getOpenid();
            List<User> users = dao.getUserByOpenid(openid);
            if(users.size()>0){
                User user = users.get(0);
                String nick_name = user.getNick_name();
                String student_name = user.getStudent_name();
                String openid_id = user.getId();

                //json
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("gift_amount", gift_amount);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("student_name", student_name);
                jsonObject.put("status", status);
                jsonObject.put("openid_id", openid_id);
                jsonObject.put("openid", openid);
                resul_list.add(jsonObject);
            }
        }
        return resul_list;
    }

    @Override
    public List getSignUpByAll(String studio,String openid,String duration,String type,String student_name,String subject) {
        String create_time = null;
        String sign_time = null;
        String id = null;
        String mark = null;
        Float count = 0.0f;
        Integer ending_status_get = 0;
        List<JSONObject> resul_list = new ArrayList<>();
        String title = "序号,学生名,科目,上课日,时间段,签到日,备注,课时,状态,结课";
        List<String> data_list = new ArrayList<>();
        List<SignUp> signUps = new ArrayList<>();

        try {
            List<User> user_get= dao.getUser(openid);
            String campus = user_get.get(0).getCampus();
            String date_start = duration.split("_")[0];
            String date_end = duration.split("_")[1];

            if("all_sign".equals(type)){
                signUps = dao.getSignUpByAllByDuration(studio,campus,date_start,date_end);
            }else if("single_sign".equals(type)){
                signUps = dao.getSignUpByBetween(student_name,studio,campus,subject,date_start,date_end);
            }

            for (int i = 0; i < signUps.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                SignUp line = signUps.get(i);
                //获取字段
                create_time = line.getCreate_time();
                sign_time = line.getSign_time();
                id = line.getId();
                mark = line.getMark();
                String duration_get = line.getDuration();
                count = line.getCount();
                String student_name_get = line.getStudent_name();
                String subject_get = line.getSubject();
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
                jsonObject.put("student_name", student_name_get);
                jsonObject.put("create_time", create_time.substring(0,10));
                jsonObject.put("sign_time", sign_time.substring(0,10));
                jsonObject.put("rank", rank);
                jsonObject.put("mark", mark);
                jsonObject.put("duration", duration_get);
                jsonObject.put("count", count);
                jsonObject.put("subject", subject_get);
                jsonObject.put("status", status);
                jsonObject.put("ending_status", ending_status);
                resul_list.add(jsonObject);

                String data_line = rank + "," + student_name_get + "," + subject_get + "," + create_time.substring(0,10) + "," + duration_get + "," + sign_time.substring(0,10) + "," +mark + "," +count + "," + status + "," + ending_status;
                data_list.add(data_line);
            }
            downloadByOpenid(studio,openid,data_list,title,type);
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
    public List getGift(String student_name,String openid,Integer coupon_type) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now_time = df.format(new Date());
        List<JSONObject> resul_list = new ArrayList<>();
        String studio = null;
        String campus = null;

        try {
            List<Gift> list = null;
            if(coupon_type == 1){
                List<User> Users = dao.getUser(openid);
                User user = Users.get(0);
                studio = user.getStudio();
                campus = user.getCampus();
                list = dao.getGift(student_name, studio);
            }else if(coupon_type == 2){
                List<User> Users = dao.getUser(openid);
                User user = Users.get(0);
                studio = user.getStudio();
                campus = user.getCampus();
                list = dao.getGiftByOpenid(openid,studio,campus);
            }else if(coupon_type == 3){
                List<User> Users = dao.getUser(openid);
                User user = Users.get(0);
                studio = user.getStudio();
                campus = user.getCampus();
                list = dao.getGift(student_name,studio);
                List<User> users_get =dao.getUserByStudent(student_name,studio);
                if(users_get.size()>0){
                    User user_get = users_get.get(0);
                    String openid_get = user.getOpenid();
                    List<Gift> list1 = dao.getGiftByOpenid(openid_get,studio,campus);
                    list.addAll(list1);
                }
            }else if(coupon_type == 4){
                List<RestaurantUser> restaurantUsers = dao.getRestaurantUserByOpenid(openid);
                RestaurantUser restaurantUser = restaurantUsers.get(0);
                String restaurant = restaurantUser.getRestaurant();
                list = dao.getGiftByOpenid(openid, restaurant,restaurant);
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Gift line = list.get(i);
                //获取字段
                String create_time = line.getCreate_time();
                String expired_time = line.getExpired_time();
                String gift_name = line.getGift_name();
                Integer gift_amount = line.getGift_amount();
                Integer status = line.getStatus();
                String id = line.getId();

                Date now_time_dt = df.parse(now_time);
                Date expired_time_dt = df.parse(expired_time);
                int compare = now_time_dt.compareTo(expired_time_dt);

                String type = line.getType();
                Float price = line.getPrice();
                String uuids = line.getUuids();
                Integer coupon_type_get = 1;
                String mark = "无备注";
                String gift_id = line.getGift_id();
                List<GiftList> giftLists = dao.getGiftListById(gift_id);
                if(giftLists.size()>0){
                    GiftList giftList = giftLists.get(0);
//                    type = giftList.getType();
                    price = giftList.getPrice();
                    uuids = giftList.getUuids();
                    coupon_type_get = giftList.getCoupon_type();
                    mark = giftList.getMark();
                }

                String status_cn = "待领取";
                if (status==0){
                    if (compare > 0) {
                        status_cn = "已过期";
                    } else {
                        if(coupon_type_get == 2){
                            status_cn = "未生效";
                        }
                    }
                } else if (status==1) {
                    status_cn = "已领取";
                    if(coupon_type_get == 2){
                        status_cn = "已生效";
                    }
                }else if (status==2) {
                    status_cn = "已消费";
                }

                //json
                jsonObject.put("mark", mark);
                jsonObject.put("status_cn", status_cn);
                jsonObject.put("status", status);
                jsonObject.put("student_name", student_name);
                jsonObject.put("create_time", create_time.substring(0,10));
                jsonObject.put("expired_time", expired_time.substring(0,10));
                jsonObject.put("gift_name", gift_name);
                jsonObject.put("gift_amount", gift_amount);
                jsonObject.put("rank", i + 1);
                jsonObject.put("id",id);
                jsonObject.put("type",type);
                jsonObject.put("price",price);
                jsonObject.put("uuids",uuids);
                jsonObject.put("coupon_type",coupon_type_get);
                jsonObject.put("gift_id",gift_id);
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
                Integer status = line.getStatus();
                jsonObject.put("status","未审核");
                if(status == 1){
                    jsonObject.put("status","已通过");
                }else if(status == 2){
                    jsonObject.put("status","不通过");
                }


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
                String id = null;
                Integer sign_count =0;
                StringBuffer teachers = new StringBuffer();
                StringBuffer all_teachers = new StringBuffer();
                String student_string = null;
                Integer classes_count =0;
                Integer remind = 0;
                Integer hours = 0;
                String remind_name="否";
                String chooseLesson="未选";

                JSONObject jsonObject = new JSONObject();
                Arrangement line = list.get(i);

                //获取字段
                remind = line.getRemind();
                hours = line.getHours();
                class_number = line.getClass_number();
                duration = line.getDuration();
                limits = line.getLimits();
                id = line.getId();
                subject = line.getSubject();
                String upcoming = line.getUpcoming();
                Integer is_repeat = line.getIs_repeat();
                String repeat_duration = line.getRepeat_duration();
                String[] repeat_duration_list = repeat_duration.split(",");
                String start_date = "2025-01-01";
                String end_date = "2025-01-01";
                if(repeat_duration_list.length ==2){
                    start_date = repeat_duration_list[0];
                    end_date = repeat_duration_list[1];
                }

                Integer is_reserved = line.getIs_reserved();
                String is_reserved_cn = "是";
                if(is_reserved == 0){
                    is_reserved_cn = "否";
                }

                if(remind == 1 ){
                    remind_name = "是";
                }
                classes_count = dao.getLessonAllCountByDay(studio,dayofweek_by,duration,class_number,subject,campus);

                // 选课
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
                int class_type = line.getClass_type();
                String repeat_week = line.getRepeat_week();

                if(!"all".equals(student_name_in)){
                    search_res = dao.getLessonAllCountByDayByName(studio,dayofweek_by,duration,class_number,subject,student_name_in,campus);
                    if(search_res>0){
                        jsonObject.put("start_date", start_date);
                        jsonObject.put("end_date", end_date);
                        jsonObject.put("is_reserved", is_reserved);
                        jsonObject.put("is_reserved_cn", is_reserved_cn);
                        jsonObject.put("upcoming", upcoming);
                        jsonObject.put("class_number", class_number);
                        jsonObject.put("duration", duration);
                        jsonObject.put("limits", limits);
                        jsonObject.put("is_repeat", is_repeat);
                        jsonObject.put("repeat_duration", repeat_duration);
                        jsonObject.put("classes_count", classes_count);
                        jsonObject.put("dayofweek",dayofweek);
                        jsonObject.put("id",id);
                        jsonObject.put("sign_count",sign_count);
                        jsonObject.put("subject",subject);
                        jsonObject.put("classes_count_all",classes_count_all);
                        jsonObject.put("classes_count_all_not",classes_count_all_lesson - classes_count_all);
                        jsonObject.put("student_string",student_string);
                        jsonObject.put("remind",remind);
                        jsonObject.put("class_type",class_type);
                        jsonObject.put("remind_name",remind_name);
                        jsonObject.put("hours",hours);
                        jsonObject.put("teachers",teachers);
                        jsonObject.put("all_teachers",all_teachers);
                        jsonObject.put("chooseLesson",chooseLesson);
                        jsonObject.put("repeat_week",repeat_week);
                        resul_list.add(jsonObject);
                    }
                }else {
                    jsonObject.put("start_date", start_date);
                    jsonObject.put("end_date", end_date);
                    jsonObject.put("is_reserved", is_reserved);
                    jsonObject.put("is_reserved_cn", is_reserved_cn);
                    jsonObject.put("upcoming", upcoming);
                    jsonObject.put("class_number", class_number);
                    jsonObject.put("duration", duration);
                    jsonObject.put("limits", limits);
                    jsonObject.put("is_repeat", is_repeat);
                    jsonObject.put("repeat_duration", repeat_duration);
                    jsonObject.put("classes_count", classes_count);
                    jsonObject.put("dayofweek",dayofweek);
                    jsonObject.put("id",id);
                    jsonObject.put("sign_count",sign_count);
                    jsonObject.put("subject",subject);
                    jsonObject.put("classes_count_all",classes_count_all);
                    jsonObject.put("classes_count_all_not",classes_count_all_lesson - classes_count_all);
                    jsonObject.put("student_string",student_string);
                    jsonObject.put("remind",remind);
                    jsonObject.put("class_type",class_type);
                    jsonObject.put("remind_name",remind_name);
                    jsonObject.put("hours",hours);
                    jsonObject.put("teachers",teachers);
                    jsonObject.put("all_teachers",all_teachers);
                    jsonObject.put("chooseLesson",chooseLesson);
                    jsonObject.put("repeat_week",repeat_week);
                    resul_list.add(jsonObject);
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
        String role = user_get.get(0).getRole();

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
                    String add_date = schedule.getAdd_date();
                    String student_type = schedule.getStudent_type();

                    int weekDayChoose = 0;
                    if(dayOfWeek == 1){
                        weekDayChoose = 7;
                    }else {
                        weekDayChoose = dayOfWeek -1;
                    }

                    if(("transferred".equals(student_type) && add_date.equals(dateString)) || "ordinary".equals(student_type)){
                        if("boss".equals(role) || "teacher".equals(role)){
                            String chooseLesson = "星期"+  weekDayChoose + "," + subject + "," + class_number + "," + duration ;
                            List<User> users = dao.getUserByChooseLesson(chooseLesson,studio);
                            for(int j=0;j<users.size();j++){
                                User user = users.get(j);
                                String openid_get = user.getOpenid();
                                if(openid_get.equals(openid)){
                                    int classes_count = dao.getLessonAllCountByDay(studio,dayOfWeek,duration,class_number,subject,campus);
                                    int sign_count = dao.getSignUpCountByDay(studio,dateString+" 00:00:00",duration,class_number,campus,subject);
                                    List<Leave> leaves = dao.getLeaveRecordByDuration(studio,campus,dateString,duration);
                                    int loss = classes_count - sign_count - leaves.size();
                                    String result = class_number + ":" + loss + "人未签" ;

                                    if(loss > 0){
                                        schedule_status.append(result);
                                        schedule_status.append(",");
                                    }
                                }
                            }
                        } else if ("client".equals(role)) {
                            List<User> users = dao.getUserByOpenid(openid);
                            for(int j=0;j<users.size();j++){
                                User user = users.get(j);
                                String student_name = user.getStudent_name();
                                List<Schedule> schedules1 = dao.getScheduleByStudentDay(studio,dayOfWeek,duration,class_number,subject,campus,student_name);
                                if(schedules1.size()>0){
                                    String result = class_number + ":" + student_name + "有课" ;
                                    schedule_status.append(result);
                                    schedule_status.append(",");
                                }
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
                        jsonObject.put("counts", 1);
                        resul_list.add(jsonObject);
                    }
                }
            }
        }else {
            if("全科目".equals(subject)){
                lessons = dao.getLesson(studio,campus);
                List<Lesson> go_lesson = dao.getGoneStudent(studio,campus);
                lessons.addAll(go_lesson);
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
                Integer delete_status = lesson.getDelete_status();
                if("已排课".equals(type) || "未排课".equals(type)){
                    List<Schedule> schedules = dao.getScheduleByStudent(studio_get,campus_get,subject_get,student_name);
                    if("已排课".equals(type) && schedules.size()>0 && delete_status == 0){
                        jsonObject.put("student_name",student_name);
                        jsonObject.put("subject", subject_get);
                        jsonObject.put("campus", campus_get);
                        jsonObject.put("total_amount", total_amount);
                        jsonObject.put("left_amount", left_amount);
                        jsonObject.put("delete_status", delete_status);
                        resul_list.add(jsonObject);

                    }else if("未排课".equals(type) && schedules.size() == 0 && delete_status == 0){
                        jsonObject.put("student_name",student_name);
                        jsonObject.put("subject", subject_get);
                        jsonObject.put("campus", campus_get);
                        jsonObject.put("total_amount", total_amount);
                        jsonObject.put("left_amount", left_amount);
                        jsonObject.put("delete_status", delete_status);
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
                        jsonObject.put("subject", subject_get);
                        jsonObject.put("campus", campus_get);
                        jsonObject.put("total_amount", total_amount);
                        jsonObject.put("left_amount", left_amount);
                        jsonObject.put("counts", counts);
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
                        jsonObject.put("subject", subject_get);
                        jsonObject.put("campus", campus_get);
                        jsonObject.put("total_amount", total_amount);
                        jsonObject.put("left_amount", left_amount);
                        jsonObject.put("counts", counts);
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
        try {
            result = dao.insertGift(gift);
            // 更新减少库存
            String gift_id = gift.getGift_id();
            List<GiftList> giftLists = dao.getGiftListById(gift_id);
            if(giftLists.size()>0){
                GiftList giftList = giftLists.get(0);
                Integer amount = giftList.getAmount();
                Integer coupon_type = giftList.getCoupon_type();
                if(coupon_type == 2){
                    if(amount >= 1){
                        amount = amount -1;
                        giftList.setAmount(amount);
                        dao.updateGiftDetail(giftList);
                    }
                }
            }
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
    public List getSchedule(String date_time, String studio,String subject,String openid) {
        List<JSONObject> resul_list = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        List<Schedule> list_tra=null;

        List<User> users = dao.getUser(openid);
        User user = users.get(0);
        String campus = user.getCampus();

        // 获取学生
        try {
            Date d = fmt.parse(date_time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            Integer weekDay = cal.get(Calendar.DAY_OF_WEEK);

            List<Schedule> list=null;
            if(subject.equals("全科目")){
                list = dao.getScheduleAll(weekDay, studio,campus);
                list_tra = dao.getTransferAll(date_time, studio,campus);
                list.addAll(list_tra);
            }else {
                list = dao.getSchedule(weekDay, studio,subject,campus);
                list_tra = dao.getTransfer(date_time, studio,subject,campus);
                list.addAll(list_tra);
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Schedule line = list.get(i);

                //获取字段
                String id = line.getId();
                String add_date = line.getAdd_date();
                String student_name = line.getStudent_name();
                String duration = line.getDuration();
                String class_number = line.getClass_number();
                String subject_get = line.getSubject();
                String create_time = line.getCreate_time();

                String button = "通知";
                List<Departure> departures = dao.getDepartureRecordByStudent(studio,campus,student_name);
                if(departures.size()>0){
                    Departure departure = departures.get(0);
                    String create_time_get = departure.getCreate_time();
                    if(date_time.equals(create_time_get.substring(0,10))){
                        button = "已通知";
                    }

                }

                jsonObject.put("id", id);
                jsonObject.put("button", button);
                jsonObject.put("add_date", add_date);
                jsonObject.put("student_name", student_name);
                jsonObject.put("duration", duration);
                jsonObject.put("class_number", class_number);
                jsonObject.put("subject", subject_get);
                jsonObject.put("create_time", create_time);

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
            List<Arrangement> repeat_list = dao.getArrangementByRepeat(studio,campus);
            list.addAll(repeat_list);

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Arrangement line = list.get(i);
                //获取字段
                String dayofweek = line.getDayofweek();
                String class_number = line.getClass_number();
                String duration = line.getDuration();
                String subject = line.getSubject();
                String limits = line.getLimits();
                String id = line.getId();
                String upcoming = line.getUpcoming();
                Integer is_reserved = line.getIs_reserved();
                Integer days = line.getDays();
                Integer is_repeat = line.getIs_repeat();
                String repeat_week = line.getRepeat_week();
                List<String> repeat_week_list = Arrays.asList(repeat_week.split(","));

                StringBuilder student_names = new StringBuilder();
                StringBuilder student_arranges = new StringBuilder();
                StringBuilder student_leaves = new StringBuilder();
                String book_stauts = "未预约";
                for (int j = 0; j < list_user.size(); j++) {
                    User user = list_user.get(j);
                    String student_name = user.getStudent_name();
                    String nick_name = user.getNick_name();
                    if(subject.contains("成人") || class_number.contains("成人")){
                        student_name = nick_name;
                    }
                    List<Schedule> check_schedule = dao.getScheduleCheck(date_time,duration,class_number,subject,studio,campus,student_name);
                    if(check_schedule.size() >= 1){
                        student_names = student_names.append(student_name).append(",");
                        book_stauts = "已预约";
                    }

                    List<Schedule> check_arrange = dao.getScheduleCheckArrangement(weekDay,duration,class_number,subject,studio,campus,student_name);
                    if(check_arrange.size() >= 1){
                        student_arranges = student_arranges.append(student_name).append(",");
                        book_stauts = "已排课";
                    }

                    List<Leave> leaves = dao.getLeaveByDateDuration(student_name,studio,date_time,duration);
                    if(leaves.size() >= 1){
                        student_leaves = student_leaves.append(student_name).append(",");
                    }

                }
                if(student_names.length()>0){
                    student_names = student_names.deleteCharAt(student_names.lastIndexOf(","));
                }
                if(student_arranges.length()>0){
                    student_arranges = student_arranges.deleteCharAt(student_arranges.lastIndexOf(","));
                }
                if(student_leaves.length()>0){
                    student_leaves = student_leaves.deleteCharAt(student_leaves.lastIndexOf(","));
                }

                int classes_count = dao.getLessonAllCountByDay(studio,weekDay,duration,class_number,subject,campus);

                int classes_count_t = dao.getLessonAllCountByDayT(studio,date_time,duration,class_number,subject,campus);

                String lesson_string = "星期" + dayofweek + "," + subject + "," + class_number + "," + duration;

                String avatarurl = null;
                StringBuffer teachers = new StringBuffer();
                List<User> teacher_user = dao.getUserByChooseLesson(lesson_string,studio);
                if(teacher_user != null){
                    for(int t = 0;t < teacher_user.size(); t++){
                        avatarurl = teacher_user.get(0).getAvatarurl();
                        String nick_name_get = teacher_user.get(t).getNick_name();
                        teachers.append(nick_name_get);
                        teachers.append(",");
                    }
                    if(teachers.length()>0) {
                        teachers = teachers.deleteCharAt(teachers.lastIndexOf(","));
                    }

                }

                String repeat_duration = line.getRepeat_duration();
                String[] repeat_duration_list = repeat_duration.split(",");
                String start_date = "2025-01-01";
                String end_date = "2025-01-01";
                if(repeat_duration_list.length ==2){
                    start_date = repeat_duration_list[0];
                    end_date = repeat_duration_list[1];
                }
                Date date_start = fmt.parse(start_date);
                long start_timestamp = date_start.getTime();
                Date date_end = fmt.parse(end_date);
                long end_timestamp = date_end.getTime();
                Date today_dt = fmt.parse(date_time);
                long today_timestamp = today_dt.getTime();

                jsonObject.put("days", days);
                jsonObject.put("is_reserved", is_reserved);
                jsonObject.put("dayofweek", dayofweek);
                jsonObject.put("duration", duration);
                jsonObject.put("class_number", class_number);
                jsonObject.put("subject", subject);
                jsonObject.put("student_names", student_names);
                jsonObject.put("student_arranges", student_arranges);
                jsonObject.put("student_leaves", student_leaves);
                jsonObject.put("limits", limits);
                jsonObject.put("classes_count", classes_count+classes_count_t);
                jsonObject.put("teachers", teachers);
                jsonObject.put("book_stauts",book_stauts);
                jsonObject.put("id",id);
                jsonObject.put("upcoming",upcoming);
                jsonObject.put("is_repeat",is_repeat);
                jsonObject.put("repeat_week",repeat_week);
                jsonObject.put("avatarurl",avatarurl);

                if(is_repeat == 1 && repeat_week_list.contains(weekofday.toString())){
                    if(today_timestamp >= start_timestamp && today_timestamp <= end_timestamp){
                        resul_list.add(jsonObject);
                    }
                }else if(is_repeat == 0){
                    resul_list.add(jsonObject);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getClassByDate(String date_time, String studio, String subject, String openid) {
        List<JSONObject> resul_list = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Integer weekDay=0;
        Integer weekofday=0;
        Float sign_counts=0.0f;
        Integer try_counts=0;
        Integer leave_counts=0;
        Float sign_counts_get=0.0f;
        Integer try_counts_get=0;
        Integer leave_counts_get=0;
        String campus = null;
        String lessons_string = null;
        JSONObject jsonObject_1 = new JSONObject();
        try {
            List<User> list_user = dao.getUser(openid);
            if(list_user.size()>0){
                campus = list_user.get(0).getCampus();
                lessons_string = list_user.get(0).getLessons();
            }

            // 统计
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

            Date d = fmt.parse(date_time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            weekDay = cal.get(Calendar.DAY_OF_WEEK);
            if(weekDay==1){
                weekofday=7;
            }else {
                weekofday = weekDay - 1;
            }

            List<Arrangement> list=null;
            List<Arrangement> repeat_list=null;
            try {
                if(subject.equals("全科目")){
                    list = dao.getArrangementByDay(studio,weekofday,campus);
                }else {
                    list = dao.getArrangement(studio,weekofday.toString(),subject,campus);
                }

                repeat_list = dao.getArrangementByRepeat(studio,campus);
                list.addAll(repeat_list);
            } catch (Exception e) {
//                throw new RuntimeException(e);
            }

            if(list.size()>0){
                for (int i = 0; i < list.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    Arrangement line = list.get(i);
                    //获取字段
                    studio = line.getStudio();
                    String duration = line.getDuration();
                    String class_number = line.getClass_number();
                    subject = line.getSubject();
                    int is_repeat = line.getIs_repeat();
                    String repeat_duration = line.getRepeat_duration();
                    String dayofweek_get = line.getDayofweek();
                    String start_date = repeat_duration.split(",")[0];
                    String end_date = repeat_duration.split(",")[1];
                    String repeat_week = line.getRepeat_week();
                    int class_type = line.getClass_type();

                    String lesson_string = null;
                    List<String> list_2 = null;
                    Integer contains = 0;
                    try {
                        // 判断老师选课
                        if(lessons_string != null){
                            String[] list_1 =lessons_string.split("\\|");
                            lesson_string = "星期" + dayofweek_get + "," + subject + "," + class_number + "," + duration;

                            list_2 = Arrays.asList(list_1);
                            if(list_2.contains(lesson_string)){
                                if(is_repeat == 0){
                                    contains = 1;
                                }
                                // 判断重复区间
                                if(is_repeat == 1){
                                    Date date_start = fmt.parse(start_date);
                                    long start_timestamp = date_start.getTime();
                                    Date date_end = fmt.parse(end_date);
                                    long end_timestamp = date_end.getTime();
                                    Date today_dt = fmt.parse(date_time);
                                    long today_timestamp = today_dt.getTime();
                                    if(today_timestamp >= start_timestamp && today_timestamp <= end_timestamp){
                                        contains = 1;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
//                        e.printStackTrace();
                    }

                    if(contains == 1){
                        jsonObject.put("studio", studio);
                        jsonObject.put("duration", duration);
                        jsonObject.put("class_number", class_number);
                        jsonObject.put("subject", subject);
                        jsonObject.put("is_repeat", is_repeat);
                        jsonObject.put("dayofweek", dayofweek_get);
                        jsonObject.put("repeat_week", repeat_week);
                        jsonObject.put("class_type", class_type);
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
        String mark = null;
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

            List<Schedule> list = dao.getScheduleByClassOrdinary(weekDay,duration,studio,class_number,subject,campus);
            List<Schedule> list_tra = dao.getScheduleByClassTransferred(date_time,duration,studio,class_number,subject,campus);
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

                int has_card = 0;
                List<Card> cards = dao.getCard(studio,campus,student_name,subject);
                if(cards.size()>0){
                    has_card = 1;
                }

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
                        if (messagesDuration.size() >= 1) {
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
                    if (homeworksDuration.size() >= 1) {
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
                    String related_id = lesson.getRelated_id();

                    jsonObject.put("has_card", has_card);
                    jsonObject.put("related_id", related_id);
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
                    List<SignUp> signUpsDuration = dao.getSignUpByDateDuration(student_name, studio, date_time, duration,campus,subject);
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
    public List getScheduleByClassRepeat(String date_time, Integer dayofweek, String duration, String class_number, String subject, String openid) {
        List<JSONObject> resul_list = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        List<User> list_user = dao.getUser(openid);
        String studio = list_user.get(0).getStudio();
        String campus = list_user.get(0).getCampus();

        // 获取常规学生
        try {
            List<Schedule> list = dao.getScheduleByClassOrdinary(dayofweek,duration,studio,class_number,subject,campus);
            List<Schedule> list_tra = dao.getScheduleByClassTransferred(date_time,duration,studio,class_number,subject,campus);
            list.addAll(list_tra);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Schedule line = list.get(i);
                //获取字段
                String add_date = line.getAdd_date();
                String student_name = line.getStudent_name();
                duration = line.getDuration();
                String id = line.getId();
                String create_time = line.getCreate_time();
                String update_time = line.getUpdate_time();
                class_number = line.getClass_number();
                String student_type = line.getStudent_type();
                subject = line.getSubject();
                int remind = line.getRemind();

                int has_card = 0;
                List<Card> cards = dao.getCard(studio,campus,student_name,subject);
                if(cards.size()>0){
                    has_card = 1;
                }

                jsonObject.put("student_type", student_type);
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
                    String related_id = lesson.getRelated_id();

                    jsonObject.put("has_card", has_card);
                    jsonObject.put("related_id", related_id);
                    jsonObject.put("minus", minus);
                    jsonObject.put("left", left);
                    jsonObject.put("total", total);
                    jsonObject.put("add_date", add_date);
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
                    String mark = null;
                    List<SignUp> signUpsDuration = dao.getSignUpByDateDuration(student_name, studio, date_time, duration,campus,subject);
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
        SimpleDateFormat df_date = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        String create_time = df.format(new Date());
        String create_date = df_date.format(new Date());
        List<JSONObject> resul_list = new ArrayList<>();
        Integer status = 0;
        String status_str = "待确认";

        // 获取常规学生
        try {
            List<Schedule> list = dao.getScheduleDetail(weekDay,duration,studio,class_number,subject,campus);
            List<Schedule> transfer_list = dao.getScheduleByClassTransferred(create_date,duration,studio,class_number,subject,campus);
            list.addAll(transfer_list);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Schedule line = list.get(i);

                //获取字段
                String age = line.getAge();
                String student_name = line.getStudent_name();
                Integer student_count = dao.getSignUpByMonthStudent(studio,subject,create_time,campus,student_name);
                Integer student_classes = dao.getClassesCountByStudent(studio,subject,campus,student_name);
                duration = line.getDuration();
                String id = line.getId();
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
                Integer hours = line.getHours();


                jsonObject.put("hours", hours);
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
            Message message = list.get(0);
            String class_target_bak = message.getClass_target_bak();
            String studio_get = message.getStudio();
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
                String uuids = message.getUuids().replace("\"","").replace("[","").replace("]","");
                String[] result = uuids.split(",");
                List<String> list_new = new ArrayList<>();
                for(int i =0;i<result.length;i++){
                    if(!result[i].equals(uuid)){
                        list_new.add(result[i]);
                    }
                }
                String vuuid = message.getVuuid();
                dao.updateUuids(id,studio,list_new.toString().replace(" ",""),vuuid);

                // 删除图片
                try {
                    String d_path = "/data/uploadimages/" ;
                    File temp = new File(d_path, uuid);
                    temp.delete();
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }

            }else if ("课后作业".equals(class_target_bak)){
                String uuids = message.getUuids_c().replace("\"","").replace("[","").replace("]","");
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
            List<User> users = dao.getUser(openid);
            User user = users.get(0);
            String campus = user.getCampus();
            String lessons = user.getLessons();

            dao.deleteArrangement(id,studio);

            //重置个人选课
            if(lessons != null){
                String[] list =lessons.split("\\|");
                List<String> list_lessons = Arrays.asList(list);
                List<Arrangement> arrangements = dao.getArrangementsByStudio(studio);
                if(arrangements.size()>0){
                    StringBuffer new_lessons = new StringBuffer();
                    for(int i=0;i<arrangements.size();i++){
                        Arrangement arrangement = arrangements.get(i);
                        String dayofweek = arrangement.getDayofweek();
                        String subject = arrangement.getSubject();
                        String class_number = arrangement.getClass_number();
                        String duration = arrangement.getDuration();
                        String lesson_string = "星期" + dayofweek + "," + subject + "," + class_number + "," + duration;
                        if(list_lessons.contains(lesson_string)){
                            new_lessons.append(lesson_string);
                            new_lessons.append("|");
                        }
                    }
                    user.setLessons(new_lessons.toString());
                    dao.updateBossLessons(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int changeClassName(String id, String openid,String content,String type) {
        try {
            List<User> list = dao.getUser(openid);
            String studio = list.get(0).getStudio();
            String campus = list.get(0).getCampus();

            String duration = null;
            String old_class_number = null;
            String old_subject = null;
            Integer is_reserved = null;
            Integer dayofweek = null;
            Integer is_repeat = null;
            Integer class_type = null;
            Arrangement arrangement = null;
            try {
                List<Arrangement> list_1 = dao.getArrangementById(studio,Integer.parseInt(id));
                if(list_1.size() > 0){
                    arrangement = list_1.get(0);
                    duration = arrangement.getDuration();
                    old_class_number = arrangement.getClass_number();
                    old_subject = arrangement.getSubject();
                    is_reserved = arrangement.getIs_reserved();
                    is_repeat = arrangement.getIs_repeat();
                    class_type = arrangement.getClass_type();

                    dayofweek = Integer.parseInt(arrangement.getDayofweek());
                    if(dayofweek==7){
                        dayofweek=1;
                    }else {
                        dayofweek = dayofweek + 1;
                    }
                }
            } catch (NumberFormatException e) {
//                throw new RuntimeException(e);
            }


            if(type.equals("班号")){
                arrangement.setClass_number(content);
                dao.changeArrangementById(arrangement);

                dao.changeScheduleClassName(old_class_number,studio,duration,content,old_subject,campus,dayofweek);
                dao.changeSignUpClassName(old_class_number,studio,duration,content,old_subject,campus);
            }else if(type.equals("科目")){
                arrangement.setSubject(content);
                dao.changeArrangementById(arrangement);

                dao.changeScheduleSubject(old_subject,studio,duration,content,old_class_number,campus,dayofweek);
            }else if(type.equals("上限")){
                arrangement.setLimits(content);
                dao.changeArrangementById(arrangement);
            }else if(type.equals("时间")){
                arrangement.setDuration(content);
                dao.changeArrangementById(arrangement);

                dao.changeScheduleDuration(old_class_number,studio,duration,content,old_subject,campus,dayofweek);
            }else if(type.equals("预告")){
                arrangement.setUpcoming(content);
                dao.changeArrangementById(arrangement);
            }else if(type.equals("预约")){
                Integer new_reserved = 1;
                if(is_reserved == 1){
                    new_reserved = 0;
                }
                arrangement.setIs_reserved(new_reserved);
                dao.changeArrangementById(arrangement);
            }else if(type.equals("可提前")){
                dao.changeArrangementDays(studio,campus,content);
            }else if(type.equals("重复")){
                Integer new_is_repeat = 1;
                if(is_repeat == 1){
                    new_is_repeat = 0;
                }
                arrangement.setIs_repeat(new_is_repeat);
                dao.changeArrangementById(arrangement);
            }else if(type.equals("重复时间")){
                arrangement.setRepeat_duration(content);
                dao.changeArrangementById(arrangement);
            }else if(type.equals("重复星期")){
                arrangement.setRepeat_week(content);
                dao.changeArrangementById(arrangement);
            }else if(type.equals("课状态")){
                dao.updateArrangementClassType(studio,Integer.parseInt(content));
            }
        } catch (Exception e) {
//            e.printStackTrace();
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
    public int updateSignUpEnding(String student_name,String openid,String id,String ending_status,String create_time) {
        try {
            List<User> list = dao.getUser(openid);
            String studio = list.get(0).getStudio();
            if("未结".equals(ending_status)){
                dao.updateSignUpEndingByAll(student_name,studio,create_time);
            }else if("已结".equals(ending_status)){
                dao.updateSignUpEndingById(id,studio);
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
    public int deleteLesson(String id, String role,String studio,String openid,String student_name) {
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
            String studio = user.getStudio();
            String role = user.getRole();
//            String md5 = DigestUtils.md5Hex(nick_name + studio);
            if(!"请录入工作室".equals(studio)){
                if(!"boss".equals(role) && !"teacher".equals(role)){
                    result = dao.updateUser(user);
                }
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
        List<User> list= new ArrayList<>();
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
                    User user_init = list.get(i);
                    String role_get = user_init.getRole();
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
                Float coins_single = line.getCoins_single();
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
                // is_exchange 表示是否在群内
                Integer is_exchange = line.getIs_exchange();
                Integer is_teacher = line.getIs_teacher();
                String openid_qr = line.getOpenid_qr();
                Integer is_square = line.getIs_square();
                jsonObject.put("ai_type", "团体");
                if(is_square==0){
                    jsonObject.put("ai_type", "个人");
                }
                String subject = line.getSubject();

                String today_time = df.format(new Date());
                Date today_dt = df.parse(today_time.substring(0,10));
                Date expired_time_ad_dt = df.parse(expired_time_ad.substring(0,10));
                int compare = today_dt.compareTo(expired_time_ad_dt);
                jsonObject.put("is_show_ad", "false");
                if(compare>0){
                    jsonObject.put("is_show_ad", "true");
                }

                jsonObject.put("official_openid", official_openid);
                jsonObject.put("official_status", "未关注");
                if(!"no_id".equals(official_openid)){
                    jsonObject.put("official_status", "已关注");
                }

                if(!openid.equals("all")){
                    list_lesson = dao.getLessonByName(student_name,studio,campus);
                }

                Integer contract = line.getContract();
                String unionid = line.getUnionid();
                Integer is_open = line.getIs_open();
                Integer is_studentmg = line.getIs_studentmg();

                //json
                jsonObject.put("is_studentmg", is_studentmg);
                jsonObject.put("unionid", unionid);
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
                    List<Book> books = dao.getAccountBookByStudio(studio);
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

                jsonObject.put("contract", contract);
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
                jsonObject.put("openid_qr", openid_qr);
                jsonObject.put("is_exchange", is_exchange);
                jsonObject.put("is_square", is_square);
                jsonObject.put("is_open", is_open);
                jsonObject.put("coins_single", coins_single);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getCardByStudent(String studio, String campus, String student_name) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        DecimalFormat df = new DecimalFormat("0.00");
        List<JSONObject> resul_list = new ArrayList<>();

        List<Card> cards = dao.getCardByStudent(studio,campus,student_name);
        for (int i = 0; i < cards.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            Card line = cards.get(i);

            String type = line.getType();
            String mark = line.getMark();
            String start_date = line.getStart_date();
            String end_date = line.getEnd_date();
            Float price = line.getPrice();
            Float used_price = 0.0f;
            Float left_price = 0.0f;

            String status = "生效中";
            try {
                Date date = formatter.parse(end_date);
                long end_timestamp = date.getTime();
                long timestamp = new Date().getTime() - 60*60*24*1000;
                if(end_timestamp < timestamp){
                    status = "已过期";
                }

                String today_time = formatter.format(new Date());
                Date today_dt = formatter.parse(today_time.substring(0,10));
                Date start_date_dt = formatter.parse(start_date.substring(0,10));
                Date end_date_dt = formatter.parse(end_date.substring(0,10));
                long total_time = end_date_dt.getTime() - start_date_dt.getTime() + 60*60*24*1000;
                long used_time = today_dt.getTime() - start_date_dt.getTime() + 60*60*24*1000;
                used_price = price * used_time/total_time;
                left_price = price - used_price;
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            String uuid = line.getUuid();
            String id = line.getId();
            String subject = line.getSubject();
            Integer gift_id = line.getGift_id();
            String gift_name = "无";
            List<GiftList> giftLists = dao.getGiftListById(gift_id.toString());
            if(giftLists.size()>0){
                GiftList giftList = giftLists.get(0);
                gift_name = giftList.getGift_name();
                type = giftList.getType();
            }

            jsonObject.put("type",type);
            jsonObject.put("gift_name",gift_name);
            jsonObject.put("subject",subject);
            jsonObject.put("mark",mark);
            jsonObject.put("start_date",start_date);
            jsonObject.put("end_date",end_date);
            jsonObject.put("uuid",uuid);
            jsonObject.put("id",id);
            jsonObject.put("status",status);
            jsonObject.put("price",df.format(price));
            jsonObject.put("used_price",df.format(used_price));
            jsonObject.put("left_price",df.format(left_price));
            resul_list.add(jsonObject);
        }
        return resul_list;
    }

    @Override
    public List getCard(String studio, String campus, String student_name,String subject) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        DecimalFormat df = new DecimalFormat("0.00");
        List<JSONObject> resul_list = new ArrayList<>();

        List<Card> cards = dao.getCard(studio,campus,student_name,subject);
        for (int i = 0; i < cards.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            Card line = cards.get(i);

            String type = line.getType();
            String mark = line.getMark();
            String start_date = line.getStart_date();
            String end_date = line.getEnd_date();
            Float price = line.getPrice();
            Float used_price = 0.0f;
            Float left_price = 0.0f;

            String status = "生效中";
            try {
                Date date = formatter.parse(end_date);
                long end_timestamp = date.getTime();
                long timestamp = new Date().getTime() - 60*60*24*1000;
                if(end_timestamp < timestamp){
                    status = "已过期";
                    used_price = price;
                    left_price = 0.0f;
                }else {
                    String today_time = formatter.format(new Date());
                    Date today_dt = formatter.parse(today_time.substring(0, 10));
                    Date start_date_dt = formatter.parse(start_date.substring(0, 10));
                    Date end_date_dt = formatter.parse(end_date.substring(0, 10));
                    long total_time = end_date_dt.getTime() - start_date_dt.getTime() + 60 * 60 * 24 * 1000;
                    long used_time = today_dt.getTime() - start_date_dt.getTime() + 60 * 60 * 24 * 1000;
                    used_price = price * used_time / total_time;
                    left_price = price - used_price;
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            String uuid = line.getUuid();
            String id = line.getId();

            jsonObject.put("type",type);
            jsonObject.put("mark",mark);
            jsonObject.put("start_date",start_date);
            jsonObject.put("end_date",end_date);
            jsonObject.put("uuid",uuid);
            jsonObject.put("id",id);
            jsonObject.put("status",status);
            jsonObject.put("price",df.format(price));
            jsonObject.put("used_price",df.format(used_price));
            jsonObject.put("left_price",df.format(left_price));
            resul_list.add(jsonObject);
        }
        return resul_list;
    }

    @Override
    public List getCardRecord(String openid,String student_name, String card_id,String subject) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        DecimalFormat df = new DecimalFormat("0.00");

        List<User> list = dao.getUser(openid);
        String studio = list.get(0).getStudio();
        String campus = list.get(0).getCampus();

        List<Card> cards = dao.getCardById(card_id);
        Card card = cards.get(0);
        String start_date = card.getStart_date();
        String end_date = card.getEnd_date();
        Float price = card.getPrice();
        Float used_price = 0.0f;
        Float left_price = 0.0f;

        try {
            String today_time = formatter.format(new Date());
            Date today_dt = formatter.parse(today_time.substring(0,10));
            Date start_date_dt = formatter.parse(start_date.substring(0,10));
            Date end_date_dt = formatter.parse(end_date.substring(0,10));
            long total_time = end_date_dt.getTime() - start_date_dt.getTime() + 60*60*24*1000;
            long used_time = today_dt.getTime() - start_date_dt.getTime() + 60*60*24*1000;
            used_price = price * used_time/total_time;
            left_price = price - used_price;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        List<JSONObject> resul_list = new ArrayList<>();
        List<CardRecord> cardRecords = dao.getCardRecord(student_name,card_id,studio,campus,subject);
        for (int i = 0; i < cardRecords.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            CardRecord line = cardRecords.get(i);

            String mark = line.getMark();
            String duration = line.getDuration();
            String create_time = line.getCreate_time();
            String id = line.getId();
            String teacher = line.getTeacher();

            jsonObject.put("teacher",teacher);
            jsonObject.put("mark",mark);
            jsonObject.put("duration",duration);
            jsonObject.put("id",id);
            jsonObject.put("create_time",create_time);
            jsonObject.put("price",df.format(price));
            jsonObject.put("used_price",df.format(used_price));
            jsonObject.put("left_price",df.format(left_price));
            resul_list.add(jsonObject);
        }
        return resul_list;
    }

    @Override
    public List getPptMenu(String openid,Integer page,String category,String type) {
        List<User> users = dao.getUser(openid);
        User user = users.get(0);
        String studio = user.getStudio();
        String campus = user.getCampus();

        Integer page_start = 0;
        Integer page_length = 0;
        page_start = (page - 1) * 4;
        page_length = 6;

        List<JSONObject> resul_list = new ArrayList<>();
        try {
            // 获取类目
            List<PptMenu> pptMenus = dao.getPptMenuCategory(studio,campus,type);
            if(pptMenus.size()>0){
                Float used_size = 0.0f;
                StringBuffer category_all = new StringBuffer();
                // 获取类目
                if(page == 1){
                    for(int i=0;i < pptMenus.size();i++){
                        PptMenu pptMenu = pptMenus.get(i);
                        // 获取类目
                        String id = pptMenu.getId();
                        String category_get = pptMenu.getCategory();
                        if(category == null || category.isEmpty() || "undefined".equals(category)){
                            if(i == 0){
                                category = category_get;
                            }
                        }
                        if(category_all.indexOf(category_get)<0){
                            category_all.append(category_get);
                            category_all.append(",");
                        }

                        // 统计空间
                        if("library".equals(type)){
                            List<Library> libraries = dao.getLibraryByMenuId(id);
                            for(int j=0;j < libraries.size();j++){
                                Library library = libraries.get(j);
                                Float size = library.getSize();
                                used_size = used_size + size/1024/1024/1024;
                            }
                        }

                    }
                    if(category_all.length()>0) {
                        category_all = category_all.deleteCharAt(category_all.lastIndexOf(","));
                    }
                }

                //明细
                List<PptMenu> list = dao.getPptMenu(studio,campus,category,type,page_start,page_length);
                for (int i = 0; i < list.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    PptMenu line = list.get(i);
                    //获取字段
                    String ppt_name = line.getPpt_name();
                    category = line.getCategory();
                    String uuids = line.getUuids();
                    String uuid = line.getUuid();
                    String create_time = line.getCreate_time();
                    String introduce = line.getIntroduce();
                    String id = line.getId();
                    Float single_size = 0.0f;
                    if("library".equals(type)){
                        List<Library> libraries = dao.getLibraryByMenuId(id);
                        for(int j=0;j < libraries.size();j++){
                            Library library = libraries.get(j);
                            Float size = library.getSize();
                            single_size = single_size + size/1024/1024/1024;
                        }
                    }
                    Float size_limit = line.getSize_limit();

                    //json
                    jsonObject.put("single_size", single_size);
                    jsonObject.put("size_limit", size_limit);
                    jsonObject.put("used_size", used_size);
                    jsonObject.put("id", id);
                    jsonObject.put("ppt_name", ppt_name);
                    jsonObject.put("category", category);
                    jsonObject.put("uuids", uuids);
                    jsonObject.put("uuid", uuid);
                    jsonObject.put("introduce", introduce);
                    jsonObject.put("create_time", create_time);
                    jsonObject.put("category_all", category_all);
                    resul_list.add(jsonObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getDepartureByOpenid(String openid) {
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<User> users = dao.getUserByOpenid(openid);
            User user = users.get(0);
            String studio = user.getStudio();
            String campus = user.getCampus();
            String role = user.getRole();
            String student_name = user.getStudent_name();


            List<Departure> list = dao.getDepartureRecordByStudio(studio,campus);
            if("client".equals(role)){
                list = dao.getDepartureRecordByStudent(studio,campus,student_name);
            }
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Departure line = list.get(i);

                //获取字段
                String id = line.getId();
                String student_name_get = line.getStudent_name();
                String subject = line.getSubject();
                String class_number = line.getClass_number();
                String mark = line.getMark();
                String create_time = line.getCreate_time();
                Integer status = line.getStatus();
                String status_cn = "已离校";
                if(status == 1){
                    status_cn = "已到家";
                }


                //json
                jsonObject.put("status", status);
                jsonObject.put("status_cn", status_cn);
                jsonObject.put("id", id);
                jsonObject.put("studio", studio);
                jsonObject.put("campus", campus);
                jsonObject.put("student_name", student_name_get);
                jsonObject.put("subject", subject);
                jsonObject.put("class_number", class_number);
                jsonObject.put("mark", mark);
                jsonObject.put("create_time", create_time);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getPptMenuById(String id) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<PptMenu> list = dao.getPptMenuById(id);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                PptMenu line = list.get(i);
                //获取字段
                String ppt_name = line.getPpt_name();
                String category = line.getCategory();
                String uuids = line.getUuids();
                String uuid = line.getUuid();
                String create_time = line.getCreate_time();
                String introduce = line.getIntroduce();
                String studio = line.getStudio();
                String campus = line.getCampus();

                //json
                jsonObject.put("id", id);
                jsonObject.put("studio", studio);
                jsonObject.put("campus", campus);
                jsonObject.put("ppt_name", ppt_name);
                jsonObject.put("category", category);
                jsonObject.put("uuids", uuids);
                jsonObject.put("uuid", uuid);
                jsonObject.put("introduce", introduce);
                jsonObject.put("create_time", create_time);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getExaminationByStudentName(String studio, String campus, String student_name,String type) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<Examination> list = dao.getExaminationByStudentName(studio,campus,student_name,type);
            if("all".equals(student_name)){
                list = dao.getExaminationByStudio(studio,campus,type);
            }
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Examination line = list.get(i);
                //获取字段
                String id = line.getId();
                String student_name_get = line.getStudent_name();
                String subject = line.getSubject();
                String title = line.getTitle();
                String create_time = line.getCreate_time();
                studio = line.getStudio();
                campus = line.getCampus();
                Float score = line.getScore();
                String uuid = line.getUuid();

                //json
                jsonObject.put("uuid", uuid);
                jsonObject.put("id", id);
                jsonObject.put("studio", studio);
                jsonObject.put("campus", campus);
                jsonObject.put("student_name", student_name_get);
                jsonObject.put("title", title);
                jsonObject.put("subject", subject);
                jsonObject.put("score", score);
                jsonObject.put("create_time", create_time);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getUserByNickName(String nickName) {
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
                String theme = line.getTheme();
                String wechat_id = line.getWechat_id();

                Integer is_square = line.getIs_square();
                jsonObject.put("ai_type", "团体");
                if(is_square==0){
                    jsonObject.put("ai_type", "个人");
                }

                int is_teacher = line.getIs_teacher();
                jsonObject.put("is_teacher", "普通");
                if(is_teacher==1){
                    jsonObject.put("is_teacher", "代理");
                }

                String today_time = df.format(new Date());
                Date today_dt = df.parse(today_time.substring(0,10));
                Date create_time_dt = df.parse(create_time.substring(0,10));
                Date expired_time_dt = df.parse(expired_time.substring(0,10));

                long user_diff = today_dt.getTime() - create_time_dt.getTime();
                long use_days = user_diff / (24*60*60*1000);

                long pay_diff = expired_time_dt.getTime()-today_dt.getTime();
                long pay_days = pay_diff / (24*60*60*1000);

                //json
                jsonObject.put("wechat_id", wechat_id);
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
                jsonObject.put("theme",theme);
                jsonObject.put("use_days",use_days);
                jsonObject.put("pay_days",pay_days);
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
    public List getBBookDetail(String openid,String duration,String book_name) {
        List<BookDetail> list= null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            String start_date = duration.split("_")[0];
            String end_date = duration.split("_")[1];

            list = dao.getBBookDetail(start_date,end_date,book_name);
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
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            Integer classes_count_all=dao.getClassesCountAll(studio,campus);
            Integer classes_count_all_lesson = dao.getClassesCountAllLesson(studio,campus);
            List<Arrangement> list = dao.getArrangements(studio,campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                StringBuffer teachers = new StringBuffer();
                StringBuffer all_teachers = new StringBuffer();
                Arrangement line = list.get(i);
                //获取字段
                String dayofweek = line.getDayofweek();
                String class_number = line.getClass_number();
                String duration = line.getDuration();
                String subject = line.getSubject();
                //获取字段
                Integer remind = line.getRemind();
                Integer hours = line.getHours();
                String limits = line.getLimits();
                String id = line.getId();
                String upcoming = line.getUpcoming();
                Integer is_repeat = line.getIs_repeat();
                String repeat_duration = line.getRepeat_duration();
                String[] repeat_duration_list = repeat_duration.split(",");
                String start_date = "2025-01-01";
                String end_date = "2025-01-01";
                if(repeat_duration_list.length ==2){
                    start_date = repeat_duration_list[0];
                    end_date = repeat_duration_list[1];
                }

                Integer is_reserved = line.getIs_reserved();
                String is_reserved_cn = "是";
                if(is_reserved == 0){
                    is_reserved_cn = "否";
                }

                String remind_name = "否";
                if(remind == 1 ){
                    remind_name = "是";
                }

                int classes_count = 0;
                int dayofweek_by = 0;
                int dayofweek_int = Integer.parseInt(dayofweek);
                if(dayofweek_int==7){
                    dayofweek_by=1;
                }else {
                    dayofweek_by = dayofweek_int + 1;
                }
                classes_count = dao.getLessonAllCountByDay(studio,dayofweek_by,duration,class_number,subject,campus);

                String item = "星期" + dayofweek + "," + class_number + "," + duration + "," + subject;
                String week_item = "星期"+dayofweek;

                // 选课
                try {
                    String lesson_string = "星期" + dayofweek + "," + subject + "," + class_number + "," + duration;
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
                int class_type = line.getClass_type();
                String repeat_week = line.getRepeat_week();
                List<String> repeat_week_list = Arrays.asList(repeat_week.split(","));

                int repeat_show = 0;
                if(repeat_week_list.contains(dayofweek)){
                    repeat_show = 1;
                }

                jsonObject.put("week_item", week_item);
                jsonObject.put("classes_count_all",classes_count_all);
                jsonObject.put("classes_count_all_not",classes_count_all_lesson - classes_count_all);
                jsonObject.put("start_date", start_date);
                jsonObject.put("end_date", end_date);
                jsonObject.put("is_reserved", is_reserved);
                jsonObject.put("is_reserved_cn", is_reserved_cn);
                jsonObject.put("upcoming", upcoming);
                jsonObject.put("class_number", class_number);
                jsonObject.put("duration", duration);
                jsonObject.put("limits", limits);
                jsonObject.put("is_repeat", is_repeat);
                jsonObject.put("repeat_show", repeat_show);
                jsonObject.put("repeat_duration", repeat_duration);
                jsonObject.put("classes_count", classes_count);
                jsonObject.put("dayofweek",dayofweek);
                jsonObject.put("id",id);
                jsonObject.put("subject",subject);
                jsonObject.put("remind",remind);
                jsonObject.put("remind_name",remind_name);
                jsonObject.put("hours",hours);
                jsonObject.put("teachers",teachers);
                jsonObject.put("all_teachers",all_teachers);
                jsonObject.put("item",item);
                jsonObject.put("repeat_week",repeat_week);
                jsonObject.put("class_type",class_type);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getArrangementsByRepeat(String studio, String campus) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<Arrangement> list = dao.getArrangementsByRepeat(studio,campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                StringBuffer teachers = new StringBuffer();
                StringBuffer all_teachers = new StringBuffer();
                Arrangement line = list.get(i);
                //获取字段
                String dayofweek = line.getDayofweek();
                String class_number = line.getClass_number();
                String duration = line.getDuration();
                String subject = line.getSubject();
                //获取字段
                Integer remind = line.getRemind();
                Integer hours = line.getHours();
                String limits = line.getLimits();
                String id = line.getId();
                String upcoming = line.getUpcoming();
                Integer is_repeat = line.getIs_repeat();
                String repeat_duration = line.getRepeat_duration();
                String[] repeat_duration_list = repeat_duration.split(",");
                String start_date = "2025-01-01";
                String end_date = "2025-01-01";
                if(repeat_duration_list.length ==2){
                    start_date = repeat_duration_list[0];
                    end_date = repeat_duration_list[1];
                }

                Integer is_reserved = line.getIs_reserved();
                String is_reserved_cn = "是";
                if(is_reserved == 0){
                    is_reserved_cn = "否";
                }

                String remind_name = "否";
                if(remind == 1 ){
                    remind_name = "是";
                }

                int classes_count = 0;
                int dayofweek_by = 0;
                int dayofweek_int = Integer.parseInt(dayofweek);
                if(dayofweek_int==7){
                    dayofweek_by=1;
                }else {
                    dayofweek_by = dayofweek_int + 1;
                }
                classes_count = dao.getLessonAllCountByDay(studio,dayofweek_by,duration,class_number,subject,campus);

                String item = "星期"+dayofweek+ "," + class_number + "," + duration + "," + subject;
                // 选课
                try {
                    String lesson_string = "星期" + dayofweek + "," + subject + "," + class_number + "," + duration;
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
                String repeat_week = line.getRepeat_week();
                int class_type = line.getClass_type();

                jsonObject.put("start_date", start_date);
                jsonObject.put("end_date", end_date);
                jsonObject.put("is_reserved", is_reserved);
                jsonObject.put("is_reserved_cn", is_reserved_cn);
                jsonObject.put("upcoming", upcoming);
                jsonObject.put("class_number", class_number);
                jsonObject.put("duration", duration);
                jsonObject.put("limits", limits);
                jsonObject.put("is_repeat", is_repeat);
                jsonObject.put("repeat_duration", repeat_duration);
                jsonObject.put("classes_count", classes_count);
                jsonObject.put("dayofweek",dayofweek);
                jsonObject.put("id",id);
                jsonObject.put("subject",subject);
                jsonObject.put("remind",remind);
                jsonObject.put("remind_name",remind_name);
                jsonObject.put("hours",hours);
                jsonObject.put("teachers",teachers);
                jsonObject.put("all_teachers",all_teachers);
                jsonObject.put("item",item);
                jsonObject.put("repeat_week",repeat_week);
                jsonObject.put("class_type",class_type);
                resul_list.add(jsonObject);
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
                int is_repeat = line.getIs_repeat();
                String repeat_week = line.getRepeat_week();
                List<String> repeat_week_list = Arrays.asList(repeat_week.split(","));

                String repeat_duration = line.getRepeat_duration();
                String[] repeat_duration_list = repeat_duration.split(",");
                String start_date = "2025-01-01";
                String end_date = "2025-01-01";
                if(repeat_duration_list.length ==2){
                    start_date = repeat_duration_list[0];
                    end_date = repeat_duration_list[1];
                }
                Date date_start = fmt.parse(start_date);
                long start_timestamp = date_start.getTime();
                Date date_end = fmt.parse(end_date);
                long end_timestamp = date_end.getTime();
                Date today_dt = fmt.parse(date_time);
                long today_timestamp = today_dt.getTime();

                String item = "星期"+ dayofweek + "," + class_number + "," + duration + "," + subject;
                if(is_repeat == 0){
                    if(weekofday.equals(Integer.parseInt(dayofweek))){
                        resul_list.add(item);
                    }
                }else if(is_repeat == 1){
                    if(today_timestamp >= start_timestamp && today_timestamp <= end_timestamp){
                        if(repeat_week_list.contains(weekofday.toString())){
                            resul_list.add(item);
                        }
                    }
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
    public List getAllOrderByType(String openid,String type,Integer page) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<User> users = dao.getUser(openid);
            User user = users.get(0);
            String studio = user.getStudio();
            String role = user.getRole();

            Integer page_start = (page - 1) * 10;
            Integer page_length = 10;

            List<Order> list = null;
            Integer status = 0;
            if("全部订单".equals(type)){
                if("boss".equals(role) || "teacher".equals(role)){
                    list = dao.getAllOrderByStudio(studio,page_start,page_length);
                }else{
                    list = dao.getAllOrderByOpenid(openid,page_start,page_length);
                }
            }else{
                if ("待付款".equals(type)) {
                    status = 0;
                }else if ("待使用".equals(type)) {
                    status = 1;
                }else if ("已完成".equals(type)) {
                    status = 2;
                }else if ("已退款".equals(type)) {
                    status = 3;
                }
                list = dao.getAllOrderByType(studio,status,page_start,page_length);
            }

            for (int i = 0; i < list.size(); i++) {
                Float group_price = 0.0f;
                Integer group_num = 0;
                String uuids = null;
                String leader_name = null;
                JSONObject jsonObject = new JSONObject();
                Order line = list.get(i);

                //获取字段
                String id = line.getId();
                String goods_name = line.getGoods_name();
                String goods_intro = line.getGoods_intro();
                Float goods_price = line.getGoods_price();
                status = line.getStatus();
                String phone_number = line.getPhone_number();
                String location = line.getLocation();
                String nick_name = line.getNick_name();
                String goods_id = line.getGoods_id();
                String group_role = line.getGroup_role();
                String leader_id = line.getLeader_id();
                openid = line.getOpenid();
                String client_name = null;
                String client_student = null;
                List<User> users_get = dao.getUser(openid);
                if(users_get.size()>0){
                    User user_get = users_get.get(0);
                    client_name = user_get.getNick_name();
                    client_student = user_get.getStudent_name();
                }

                List<User> leaders = dao.getUser(leader_id);
                if(leaders.size() > 0){
                    User leader_user = leaders.get(0);
                    leader_name = leader_user.getNick_name();
                }

                type = line.getType();
                if("跳蚤市场".equals(type)){
                    List<Album> albums = dao.getAlbumById(goods_id);
                    if(albums.size()>0){
                        Album album = albums.get(0);
                        goods_name = album.getName();
                        goods_price = album.getPrice();
                        uuids = album.getUuid();
                    }
                }else {
                    List<GoodsList> goodsLists = dao.getGoodsListById(goods_id);
                    if(goodsLists.size()>0){
                        GoodsList goodsList = goodsLists.get(0);
                        goods_name = goodsList.getGoods_name();
                        goods_price = goodsList.getGoods_price();
                        group_price = goodsList.getGroup_price();
                        group_num = goodsList.getGroup_num();
                        try {
                            uuids = goodsList.getUuids().replace("\"","").replace("[","").replace("]","");
                        } catch (Exception e) {
//                    throw new RuntimeException(e);
                        }
                    }
                }



                String create_time = line.getCreate_time();

                Integer counts = line.getCounts();
                Float amount = line.getAmount();
                String sub_goods_id = line.getSub_goods_id();
                List<GoodsList> goodsLists1 = dao.getGoodsListById(sub_goods_id);
                String sub_goods_name = "无";
                if(goodsLists1.size()>0){
                    GoodsList goodsList1 = goodsLists1.get(0);
                    sub_goods_name  = goodsList1.getGoods_name();
                }

                List<Order> success_orders = dao.getOrderByGoodsLeader(goods_id,leader_id,type);
                int group_sum = success_orders.size();
                String order_no = line.getOrder_no();

                jsonObject.put("id", id);
                jsonObject.put("order_no", order_no);
                jsonObject.put("sub_goods_name", sub_goods_name);
                jsonObject.put("client_name", client_name);
                jsonObject.put("client_student", client_student);
                jsonObject.put("goods_id", goods_id);
                jsonObject.put("goods_name", goods_name);
                jsonObject.put("goods_intro", goods_intro);
                jsonObject.put("goods_price", goods_price);
                jsonObject.put("create_time", create_time);
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("location", location);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("openid", openid);
                jsonObject.put("group_price", group_price);
                jsonObject.put("leader_id", leader_id);
                jsonObject.put("leader", leader_name);
                jsonObject.put("group_role", group_role);
                jsonObject.put("uuids", uuids);
                jsonObject.put("group_num", group_num);
                jsonObject.put("group_sum", group_sum);
                jsonObject.put("type", type);
                jsonObject.put("counts", counts);
                jsonObject.put("amount", amount);
                jsonObject.put("status", status);

                //json
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getOrderByStudioLike(String openid, String content) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {

            List<User> users = dao.getUser(openid);
            User user = users.get(0);
            String studio = user.getStudio();
            List<Order> list = dao.getOrderByStudioLike(studio,content);

            for (int i = 0; i < list.size(); i++) {
                Float group_price = 0.0f;
                Integer group_num = 0;
                String uuids = null;
                String leader_name = null;
                JSONObject jsonObject = new JSONObject();
                Order line = list.get(i);

                //获取字段
                String id = line.getId();
                String goods_name = line.getGoods_name();
                String goods_intro = line.getGoods_intro();
                Float goods_price = line.getGoods_price();
                Integer status = line.getStatus();
                String phone_number = line.getPhone_number();
                String location = line.getLocation();
                String nick_name = line.getNick_name();
                String goods_id = line.getGoods_id();
                String group_role = line.getGroup_role();
                String leader_id = line.getLeader_id();
                openid = line.getOpenid();
                String client_name = null;
                String client_student = null;
                List<User> users_get = dao.getUser(openid);
                if(users_get.size()>0){
                    User user_get = users_get.get(0);
                    client_name = user_get.getNick_name();
                    client_student = user_get.getStudent_name();
                }

                List<User> leaders = dao.getUser(leader_id);
                if(leaders.size() > 0){
                    User leader_user = leaders.get(0);
                    leader_name = leader_user.getNick_name();
                }

                String type = line.getType();
                if("跳蚤市场".equals(type)){
                    List<Album> albums = dao.getAlbumById(goods_id);
                    if(albums.size()>0){
                        Album album = albums.get(0);
                        goods_name = album.getName();
                        goods_price = album.getPrice();
                        uuids = album.getUuid();
                    }
                }else {
                    List<GoodsList> goodsLists = dao.getGoodsListById(goods_id);
                    if(goodsLists.size()>0){
                        GoodsList goodsList = goodsLists.get(0);
                        goods_name = goodsList.getGoods_name();
                        goods_price = goodsList.getGoods_price();
                        group_price = goodsList.getGroup_price();
                        group_num = goodsList.getGroup_num();
                        try {
                            uuids = goodsList.getUuids().replace("\"","").replace("[","").replace("]","");
                        } catch (Exception e) {
//                    throw new RuntimeException(e);
                        }
                    }
                }

                String create_time = line.getCreate_time();
                Integer counts = line.getCounts();
                Float amount = line.getAmount();
                String sub_goods_id = line.getSub_goods_id();
                List<GoodsList> goodsLists1 = dao.getGoodsListById(sub_goods_id);
                String sub_goods_name = "无";
                if(goodsLists1.size()>0){
                    GoodsList goodsList1 = goodsLists1.get(0);
                    sub_goods_name  = goodsList1.getGoods_name();
                }
                List<Order> success_orders = dao.getOrderByGoodsLeader(goods_id,leader_id,type);
                int group_sum = success_orders.size();
                String order_no = line.getOrder_no();

                jsonObject.put("id", id);
                jsonObject.put("order_no", order_no);
                jsonObject.put("sub_goods_name", sub_goods_name);
                jsonObject.put("client_name", client_name);
                jsonObject.put("client_student", client_student);
                jsonObject.put("goods_id", goods_id);
                jsonObject.put("goods_name", goods_name);
                jsonObject.put("goods_intro", goods_intro);
                jsonObject.put("goods_price", goods_price);
                jsonObject.put("create_time", create_time);
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("location", location);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("openid", openid);
                jsonObject.put("group_price", group_price);
                jsonObject.put("leader_id", leader_id);
                jsonObject.put("leader", leader_name);
                jsonObject.put("group_role", group_role);
                jsonObject.put("uuids", uuids);
                jsonObject.put("group_num", group_num);
                jsonObject.put("group_sum", group_sum);
                jsonObject.put("type", type);
                jsonObject.put("counts", counts);
                jsonObject.put("amount", amount);
                jsonObject.put("status", status);

                //json
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getOrderByGoodsId(String goods_id,String type) {
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Order> list =dao.getOrderByGoodsId(goods_id,type);;

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Order line = list.get(i);
                //获取字段
                String id = line.getId();
                String goods_name = line.getGoods_name();
                String goods_intro = line.getGoods_intro();
                Float goods_price = line.getGoods_price();
                Integer status = line.getStatus();
                String phone_number = line.getPhone_number();
                String location = line.getLocation();
                String nick_name = line.getNick_name();
                String openid = line.getOpenid();
                String group_role = line.getGroup_role();
                String leader_id = line.getLeader_id();
                Float cut_price = line.getCut_price();

                String group_status = "未成团";
                List<GoodsList> goodsLists = dao.getGoodsListById(goods_id);
                int group_num = goodsLists.get(0).getGroup_num();

                List<Order> orders = dao.getOrderByGoodsLeader(goods_id,leader_id,type);
                int group_sum = orders.size();

                if(group_sum >= group_num){
                    group_status = "已成团";
                }

                String avartar_leader = null;
                String studio = null;
                String campus = null;
                List<User> users = dao.getUserByOpenid(leader_id);
                if(users.size()>0){
                    User user = users.get(0);
                    studio = user.getStudio();
                    campus =  user.getCampus();
                    avartar_leader = user.getAvatarurl();
                }

                String avartar_follow = null;
                String student_name = null;
                List<User> users1 = dao.getUserByOpenid(openid);
                int is_new = 1;
                if(users1.size()>0){
                    User user1 = users1.get(0);
                    student_name = user1.getStudent_name();
                    avartar_follow = user1.getAvatarurl();
                    List<Lesson> lessons = dao.getLessonLikeName(studio,student_name,campus);
                    if(lessons.size()>0){
                        is_new = 0;
                    }
                }

                String create_time = line.getCreate_time();

                jsonObject.put("id", id);
                jsonObject.put("is_new", is_new);
                jsonObject.put("goods_name", goods_name);
                jsonObject.put("goods_intro", goods_intro);
                jsonObject.put("goods_price", goods_price);
                jsonObject.put("status", status);
                jsonObject.put("create_time", create_time);
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("location", location);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("openid", openid);
                jsonObject.put("group_role", group_role);
                jsonObject.put("leader_id", leader_id);
                jsonObject.put("studio", studio);
                jsonObject.put("campus", campus);
                jsonObject.put("group_status", group_status);
                jsonObject.put("group_num", group_num);
                jsonObject.put("group_sum", group_sum);
                jsonObject.put("cut_price", cut_price);
                jsonObject.put("student_name", student_name);
                jsonObject.put("avartar_leader", avartar_leader);
                jsonObject.put("avartar_follow", avartar_follow);

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
    public List getUuidByTarget(String class_target,String openid) {
        List<User> list_user = dao.getUser(openid);
        String studio = list_user.get(0).getStudio();
        String campus = list_user.get(0).getCampus();

        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<Message> list = dao.getUuidByTarget(class_target,studio,campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);

                //获取字段
                String uuids = null;
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                String id = line.getId();
                String create_time = line.getCreate_time();
                String comment = line.getComment();

                //json
                jsonObject.put("class_target", class_target);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("uuids", uuids);
                jsonObject.put("comment", comment);
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
    public List getAccountBookDetail(String studio, String create_time, String type,String start_date) {
        List<Book> list =null;
        String mark = null;
        Float amount = 0.0f;
        String id = null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            if("all".equals(type)){
                list = dao.getAccountBookDetailAll(studio,create_time,start_date);
            }else {
                list = dao.getAccountBookDetail(studio,create_time,type,start_date);
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
    public List getAlbum(String student_name,String openid,Integer page,String type) {
        Integer page_start = (page - 1) * 10;
        Integer page_length = 10;
        List<JSONObject> resul_list = new ArrayList<>();
        String studio = null;
        String campus = null;

        try {
            List<User> users = dao.getUserByOpenid(openid);
            StringBuilder student_names = new StringBuilder();
            for(int i = 0;i< users.size();i++){
                User user = users.get(i);
                String student_get = user.getStudent_name();
                studio = user.getStudio();
                campus = user.getCampus();
                student_names = student_names.append(student_get).append(",");
            }
            if(student_names.length()>0){
                student_names = student_names.deleteCharAt(student_names.lastIndexOf(","));
            }

            List<Album> albums = null;
            if("全部".equals(student_name)){
                albums = dao.getAlbumByType(studio,campus,type,page_start,page_length);
            }else{
                albums = dao.getAlbum(student_name,studio,campus,type,page_start,page_length);
            }

            for(int i = 0;i< albums.size();i++){
                JSONObject jsonObject = new JSONObject();
                Album album = albums.get(i);
                String uuid = album.getUuid();
                String id = album.getId();
                String name = album.getName();
                Float price = album.getPrice();
                String intro = album.getIntro();
                int has_sale = 0;
                String nick_name = null;
                String phone_number = null;
                String location = null;
                if("趣卖".equals(type)){
                    List<Order> orders = dao.getOrderByGoodsId(id,"跳蚤市场");
                    if(orders.size()>0){
                        has_sale = 1;
                        Order order = orders.get(0);
                        nick_name = order.getNick_name();
                        phone_number = order.getPhone_number();
                        location = order.getLocation();
                    }
                }
                student_name = album.getStudent_name();

                jsonObject.put("id", id);
                jsonObject.put("uuid", uuid);
                jsonObject.put("student_name", student_name);
                jsonObject.put("student_names", student_names);
                jsonObject.put("name", name);
                jsonObject.put("price", price);
                jsonObject.put("intro", intro);
                jsonObject.put("has_sale", has_sale);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("location", location);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getWebsite(String studio,String campus) {
        List<JSONObject> resul_list = new ArrayList<>();
        List<Website> websites = dao.getWebsite(studio,campus);
        try {
            for(int i = 0;i< websites.size();i++){
                JSONObject jsonObject = new JSONObject();
                Website website = websites.get(i);
                String company = website.getCompany();
                String teacher = website.getTeacher();
                String uuids = website.getUuids();

                jsonObject.put("uuids", uuids);
                jsonObject.put("teacher", teacher);
                jsonObject.put("company", company);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getExhibition(String openid, String type,Integer page) {
        List<JSONObject> resul_list = new ArrayList<>();
        List<User> list_user = dao.getUser(openid);
        String studio = list_user.get(0).getStudio();
        Integer page_start = (page - 1) * 20;
        Integer page_length = 20;

        // 作品转到展览处
        if("回收".equals(type)){
            dao.updateCommentTargetBak(studio);
            return resul_list;
        }

        List<Message> messages = dao.getExhibitionByType(studio,type,page_start, page_length);

        for (int i = 0; i < messages.size(); i++) {
            Message line = messages.get(i);
            String uuids = line.getUuids();
            String vuuid = line.getVuuid();
            String comment = line.getComment();
            Integer views = line.getViews();
            String id = line.getId();

            int liked = 0;
            int like_count =0;
            List<PostLike> postLikes = dao.getPostLike(id);
            if(postLikes.size()>0){
                for (int j = 0; j < postLikes.size(); j++) {
                    like_count = like_count + 1;
                    PostLike postLike = postLikes.get(j);
                    String openid_get = postLike.getOpenid();
                    if(openid.equals(openid_get)){
                        liked = 1;
                    }
                }
            }


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

            String class_name = line.getClass_name();
            String student_name = line.getStudent_name();

            String[] uuids_list = uuids.split(",");
            if(uuids.length()>2){
                for(int j=0;j<uuids_list.length;j++){
                    JSONObject jsonObject = new JSONObject();
                    String uuids_get = uuids_list[j];
                    jsonObject.put("rank", i + page_start + 1);
                    jsonObject.put("uuids",uuids_get);
                    jsonObject.put("vuuid",vuuid);
                    jsonObject.put("id",id);
                    jsonObject.put("comment",comment);
                    jsonObject.put("views",views);
                    jsonObject.put("class_name",class_name);
                    jsonObject.put("student_name",student_name);
                    jsonObject.put("liked",liked);
                    jsonObject.put("like_count",like_count);
                    resul_list.add(jsonObject);
                }
            }else{
                dao.deleteComment(Integer.parseInt(id),studio);
            }
        }

        return resul_list;
    }

    @Override
    public List getExhibitionRank(String openid, String type) {
        List<User> list_user = dao.getUser(openid);
        String studio = list_user.get(0).getStudio();
        Integer page_start = 0;
        Integer page_length = 10000;
        List<JSONObject> resul_list = new ArrayList<>();
        List<Message> messages = dao.getExhibitionByType(studio,type,page_start, page_length);

        for (int i = 0; i < messages.size(); i++) {
            Message line = messages.get(i);
            String uuids = line.getUuids();
            String vuuid = line.getVuuid();
            String comment = line.getComment();
            Integer views = line.getViews();
            String id = line.getId();

            int liked = 0;
            int like_count =0;
            List<PostLike> postLikes = dao.getPostLike(id);
            if(postLikes.size()>0){
                for (int j = 0; j < postLikes.size(); j++) {
                    like_count = like_count + 1;
                    PostLike postLike = postLikes.get(j);
                    String openid_get = postLike.getOpenid();
                    if(openid.equals(openid_get)){
                        liked = 1;
                    }
                }
            }


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

            String class_name = line.getClass_name();
            String student_name = line.getStudent_name();

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
                    jsonObject.put("class_name",class_name);
                    jsonObject.put("student_name",student_name);
                    jsonObject.put("liked",liked);
                    jsonObject.put("like_count",like_count);
                    resul_list.add(jsonObject);
                }
            }else{
                dao.deleteComment(Integer.parseInt(id),studio);
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

                // 判断是否合并分科更新本人的课时
                Lesson lesson = new Lesson();
                lesson.setStudent_name(student_name);
                lesson.setLeft_amount(new_left);
                lesson.setTotal_amount(total_amount);
                lesson.setStudio(studio);
                lesson.setSubject(subject);
                lesson.setCampus(campus);
                if(is_combine == 0){
                    result = dao.updateLesson(lesson);
                }else if (is_combine == 1){
                    result = dao.updateLessonBoth(lesson);
                }

                try {
                    // 判断是否关联并更新其他人的课时
                    String related_id = line.getRelated_id();
                    // 判定有关联
                    if(!"no_id".equals(related_id)){
                        String[] related_id_list = related_id.split(",");
                        for(int j=0;j < related_id_list.length; j++){
                            String id_get = related_id_list[j];
                            if(id_get != null && id_get != ""){
                                List<Lesson> lessons = dao.getLessonById(id_get);
                                if(lessons.size()>0){
                                    Lesson lesson_get = lessons.get(0);
                                    String student_name_get = lesson_get.getStudent_name();
                                    // 判定其他人
                                    if(!student_name.equals(student_name_get)){
                                        String subject_get = lesson_get.getSubject();
                                        Float minus_get = lesson_get.getMinus();
                                        Float coins_get = lesson_get.getCoins();

                                        Lesson lesson_re = new Lesson();
                                        lesson_re.setStudent_name(student_name_get);
                                        lesson_re.setLeft_amount(new_left);
                                        lesson_re.setTotal_amount(total_amount);
                                        lesson_re.setStudio(studio);
                                        lesson_re.setCampus(campus);
                                        lesson_re.setSubject(subject_get);

                                        dao.updateLesson(lesson_re);
                                    }
                                }
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public synchronized int syncUpdateMinusLesson(String student_name, String studio,Float class_count,String subject,String campus) {
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

                // 判断是否合并分科更新本人的课时
                Lesson lesson = new Lesson();
                lesson.setStudent_name(student_name);
                lesson.setLeft_amount(new_left);
                lesson.setTotal_amount(total_amount);
                lesson.setStudio(studio);
                lesson.setSubject(subject);
                lesson.setCampus(campus);
                if(is_combine == 0){
                    result = dao.updateLesson(lesson);
                }else if (is_combine == 1){
                    result = dao.updateLessonBoth(lesson);
                }

                try {
                    // 判断是否关联并更新其他人的课时
                    String related_id = line.getRelated_id();
                    // 判定有关联
                    if(!"no_id".equals(related_id)){
                        String[] related_id_list = related_id.split(",");
                        for(int j=0;j < related_id_list.length; j++){
                            String id_get = related_id_list[j];
                            if(id_get != null && id_get != ""){
                                List<Lesson> lessons = dao.getLessonById(id_get);
                                if(lessons.size()>0){
                                    Lesson lesson_get = lessons.get(0);
                                    String student_name_get = lesson_get.getStudent_name();
                                    // 判定其他人
                                    if(!student_name.equals(student_name_get)){
                                    String subject_get = lesson_get.getSubject();
                                    Float minus_get = lesson_get.getMinus();
                                    Float coins_get = lesson_get.getCoins();

                                    Lesson lesson_re = new Lesson();
                                    lesson_re.setStudent_name(student_name_get);
                                    lesson_re.setLeft_amount(new_left);
                                    lesson_re.setTotal_amount(total_amount);
                                    lesson_re.setStudio(studio);
                                    lesson_re.setCampus(campus);
                                    lesson_re.setSubject(subject_get);

                                    dao.updateLesson(lesson_re);
                                }
                                }
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateAddPoints(String student_name, String studio,Integer points_int,String subject,String campus,String mark,String type) {
        int result = 0;
        Integer points = 0;
        Integer new_points = 0;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String create_time = df.format(new Date());

        List<Lesson> list = dao.getLessonByNameSubject(student_name,studio,subject,campus);
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

                if("上课积分".equals(type) || "add".equals(type) || "minus".equals(type) || "兑换积分".equals(type) || "取消签到".equals(type)){
                    Points points_rd = new Points();
                    points_rd.setStudent_name(student_name);
                    points_rd.setCreate_time(create_time);
                    points_rd.setStudio(studio);
                    points_rd.setMark(mark);
                    points_rd.setPoints((float)points_int);
                    points_rd.setSubject(subject);
                    points_rd.setCampus(campus);
                    if(points_int != 0){
                        dao.insertPointsRecord(points_rd);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateLessonPackage(String id, String content,String type) {
        int result = 0;
        List<LessonPackage> lessonPackages = dao.getLessonPackageById(Integer.parseInt(id));
        LessonPackage lessonPackage = lessonPackages.get(0);
        try {
            if("课包原价".equals(type)){
                lessonPackage.setTotal_money(Float.parseFloat(content));
                dao.updateLessonPackageDetail(lessonPackage);
            }else if("优惠金额".equals(type)){
                lessonPackage.setDiscount_money(Float.parseFloat(content));
                dao.updateLessonPackageDetail(lessonPackage);
            }else if("结课状态".equals(type)){
                int end_status = lessonPackage.getEnd_status();
                int status = 1;
                if(end_status == 1 ){
                    status = 0;
                }
                lessonPackage.setEnd_status(status);
                dao.updateLessonPackageDetail(lessonPackage);
            }else if("备注".equals(type)){
                lessonPackage.setMark(content);
                dao.updateLessonPackageDetail(lessonPackage);
            }else if("原课时".equals(type)){
                lessonPackage.setAll_lesson(Float.parseFloat(content));
                dao.updateLessonPackageDetail(lessonPackage);
            }else if("赠课时".equals(type)){
                lessonPackage.setGive_lesson(Float.parseFloat(content));
                dao.updateLessonPackageDetail(lessonPackage);
            }else if("报课时间".equals(type)){
                lessonPackage.setStart_date(content);
                dao.updateLessonPackageDetail(lessonPackage);
            }else if("有效期至".equals(type)){
                lessonPackage.setEnd_date(content);
                dao.updateLessonPackageDetail(lessonPackage);
            }else if("分拆".equals(type)){
                dao.updateLessonPackageGiveLesson(id,0.0f);
                lessonPackage.setTotal_money(0.0f);
                lessonPackage.setDiscount_money(0.0f);
                lessonPackage.setAll_lesson(0.0f);
                dao.insertLessonPackage(lessonPackage);
            }else if("优先指定".equals(type)){
                int is_first_get = lessonPackage.getIs_first();
                int is_first = 1;
                if(is_first_get == 1 ){
                    is_first = 0;
                }
                lessonPackage.setIs_first(is_first);
                dao.updateLessonPackageDetail(lessonPackage);
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public int updateCoinsByStudio(String studio,String openid,Float number,String type) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        String now_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
        int result = 0;
        try {
            List<User> users = dao.getUserByOpenid(openid);
            User user = users.get(0);
            Float coins = user.getCoins();
            Float coins_single = user.getCoins_single();
            Integer is_square = user.getIs_square();
            if (coins == null) {
                coins = 0.0f;
            }
            if("消耗".equals(type)){
                if(is_square == 1){
                    coins = coins - number;
                    user.setCoins(coins);
                }else if(is_square == 0){
                    coins_single = coins_single - number;
                    user.setCoins_single(coins_single);
                }
            }

            if("充值".equals(type)){
                if(is_square == 0){
                    coins_single = coins_single + number;
                    user.setCoins_single(coins_single);
                }
            }

            dao.updateCoinsByUser(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public void sendClassRemind() {
        List<String> apps = new ArrayList<>();
        apps.add("MOMO_OFFICIAL");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");

        String publickey = Constants.publickey;
        String privatekey = Constants.privatekey;

        List<User> list = dao.getAllUser();
        for (int i = 0; i < list.size(); i++) {
            try {
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

                //获取提前时间
                Calendar cal_today = Calendar.getInstance();
                cal_today.add(Calendar.HOUR_OF_DAY,hours);
                int weekDay_today = cal_today.get(Calendar.DAY_OF_WEEK);
                long td_time = cal_today.getTimeInMillis();
                String td_date = df.format(td_time);

                //获取统一时间
                Calendar cal_tomorrow = Calendar.getInstance();
                cal_tomorrow.add(Calendar.DATE,+1);
                Integer weekDay_tomorrow = cal_tomorrow.get(Calendar.DAY_OF_WEEK);
                long tm_time = cal_tomorrow.getTimeInMillis();
                String tm_date = df.format(tm_time);

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
    //                throw new RuntimeException(e);
                }

                Integer weekDay = 0;
                String date_time = null;
                List<Schedule> list_schedule = new ArrayList<>();
                List<Schedule> list_schedule_re = new ArrayList<>();
                //上课通知
                if(!"no_name".equals(student_name)){
                    // 查找每天重复的课程
                    List<Arrangement> arrangements_re = dao.getArrangementsByRepeat(studio,campus);
                    Integer weekofday = 0;
                    if(arrangements_re.size()>0){
                        for(int index = 0;index < arrangements_re.size();index++){
                            Arrangement arrangement = arrangements_re.get(index);
                            Integer dayofweek =Integer.parseInt(arrangement.getDayofweek());
                            int dayofweek_in = 0;
                            if(dayofweek == 7){
                                dayofweek_in = 1;
                            }else {
                                dayofweek_in = dayofweek + 1;
                            }
                            String repeat_week = arrangement.getRepeat_week();
                            List<String> repeat_week_list = Arrays.asList(repeat_week.split(","));
                            String repeat_duration = arrangement.getRepeat_duration();
                            String repeat_end = repeat_duration.split(",")[1];
                            String duration = arrangement.getDuration();
                            String duration_start = duration.split("-")[0];

                            // 判断是否在期内
                            Long compare = 10L;
                            try {
                                Date today_dt = df.parse(now_date.substring(0,10));
                                Date expired_dt = df.parse(repeat_end);
                                Long day2 = expired_dt.getTime();
                                Long day1 = today_dt.getTime();
                                compare = (day2 - day1)/(24*3600*1000);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                            if(compare > 0){
                                if("统一提醒次日".equals(remindType)){
                                    if(weekDay_tomorrow==1){
                                        weekofday = 7;
                                    }else {
                                        weekofday = weekDay_tomorrow - 1;
                                    }
                                }else if("提前N小时提醒".equals(remindType)){
                                    if(weekDay_today==1){
                                        weekofday = 7;
                                    }else {
                                        weekofday = weekDay_today - 1;
                                    }
                                }
                                if(repeat_week_list.contains(weekofday.toString())){
                                    List<Schedule> list_schedule_get = dao.getScheduleByUserDurationSt(dayofweek_in,studio,student_name,campus,duration_start,duration_start);
                                    list_schedule_re.addAll(list_schedule_get);
                                }
                            }
                        }
                    }

                    // 通知分类
                    if("统一提醒次日".equals(remindType) && timestamp >= timestamp_start && timestamp <=timestamp_end){
                        weekDay = weekDay_tomorrow;
                        date_time = df.format(cal_tomorrow.getTime());
                        list_schedule = dao.getScheduleByUser(weekDay_tomorrow,studio,student_name,campus);
                        list_schedule.addAll(list_schedule_re);
                    }else if("提前N小时提醒".equals(remindType) && hours > 0){
                        weekDay = weekDay_today;
                        date_time = df.format(cal_today.getTime());
                        List<Schedule> schedules = dao.getScheduleByUser(weekDay_today,studio,student_name,campus);
                        list_schedule.addAll(list_schedule_re);
                        for(int j = 0;j < schedules.size();j++){
                            Schedule schedule = schedules.get(j);
                            Integer hours_get = schedule.getHours();
                            String class_number = schedule.getClass_number();
                            String duration = schedule.getDuration();
                            String subject = schedule.getSubject();
                            String add_date = schedule.getAdd_date();
                            LocalDate localDate = LocalDate.parse(add_date);
                            Integer weekDayChoose = localDate.getDayOfWeek().getValue();
                            List<Arrangement> arrangements = dao.getArrangementByDate(studio,weekDayChoose.toString(),class_number,duration,subject,campus);
                            if(arrangements.size()>0){
                                Arrangement arrangement = arrangements.get(0);
                                hours_get = arrangement.getHours();
                            }

                            //获取提前时间
                            Calendar cal_today_get = Calendar.getInstance();
                            cal_today_get.add(Calendar.HOUR_OF_DAY,hours_get);
                            int hour = cal_today_get.get(Calendar.HOUR_OF_DAY);
                            int minute = cal_today_get.get(Calendar.MINUTE);
                            String hour_st = Integer.toString(hour);
                            String minute_st = Integer.toString(minute);
                            if(hour < 10 ){
                                hour_st = "0" + hour_st;
                            }
                            if(minute < 10 ){
                                minute_st = "0" + minute_st;
                            }
                            String duration_ed = hour_st + ":" + minute_st;
                            LocalTime time = LocalTime.of(hour, minute);
                            Duration fiveMinutes = Duration.ofMinutes(3);
                            LocalTime duration_st = time.minus(fiveMinutes);

                            // 获取课程表时间
                            Integer week_day = 0;
                            if(weekDayChoose == 7){
                                week_day = 1;
                            }else {
                                week_day = weekDayChoose + 1;
                            }
                            List<Schedule> schedules_tmp = dao.getScheduleByUserDurationSt(week_day,studio,student_name,campus,duration_st.toString(),duration_ed);
                            if(schedules_tmp.size()>0){
                                list_schedule = schedules_tmp;
                            }
                        }
                    }

                    // 向家长发送通知
                    if(list_schedule.size() > 0 && weekDay > 0 && !"no_id".equals(official_openid)){
                        for (int j = 0; j < list_schedule.size(); j++) {
                            Integer weekDay_ta = 0;
                            if(weekDay==1){
                                weekDay_ta = 7;
                            }else {
                                weekDay_ta = weekDay - 1;
                            }
                            Schedule schedule = list_schedule.get(j);
                            String duration = schedule.getDuration();
                            String class_number = schedule.getClass_number();
                            String subject = schedule.getSubject();
                            Integer remind = schedule.getRemind();
                            String id = schedule.getId();
                            String send_status = schedule.getSend_status();
                            String student_type = schedule.getStudent_type();
                            String add_date = schedule.getAdd_date();
                            LocalDate localDate = LocalDate.parse(add_date);
                            Integer weekDayChoose = localDate.getDayOfWeek().getValue();

                            if("统一提醒次日".equals(remindType)){
                                // 跳过插班生
                                if("transferred".equals(student_type)){
                                    if(!tm_date.equals(add_date)){
                                        send_status = now_date;
                                    }
                                }
                                // 跳过请假生
                                List<Leave> leaves = dao.getLeaveRecordByDate(student_name,studio,subject,campus,tm_date);
                                if(leaves.size()>0){
                                    send_status = now_date;
                                }
                            }else if("提前N小时提醒".equals(remindType)){
                                // 跳过插班生
                                if("transferred".equals(student_type)){
                                    if(!td_date.equals(add_date)){
                                        send_status = now_date;
                                    }
                                }
                                // 跳过请假生
                                List<Leave> leaves = dao.getLeaveRecordByDate(student_name,studio,subject,campus,td_date);
                                if(leaves.size()>0){
                                    send_status = now_date;
                                }
                            }

                            // 课程设计
                            Integer choose = 0;
                            String upcoming = "未设";
                            Integer is_repeat = 0;
                            List<Arrangement> arrangement_list = dao.getArrangementByDate(studio,weekDayChoose.toString(),class_number,duration,subject,campus);
                            if(arrangement_list.size()>0){
                                Arrangement arrangement = arrangement_list.get(0);
                                upcoming = arrangement.getUpcoming();
                                remind = arrangement.getRemind();
                                is_repeat = arrangement.getIs_repeat();
                                String repeat_week = arrangement.getRepeat_week();
                                List<String> repeat_week_list = Arrays.asList(repeat_week.split(","));
                                String repeat_duration = arrangement.getRepeat_duration();
                                String repeat_end = repeat_duration.split(",")[1];

                                // 判断是否在期内
                                Long compare = 10L;
                                try {
                                    Date today_dt = df.parse(now_date.substring(0,10));
                                    Date expired_dt = df.parse(repeat_end);
                                    Long day2 = expired_dt.getTime();
                                    Long day1 = today_dt.getTime();
                                    compare = (day2 - day1)/(24*3600*1000);
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }

                                if(compare < 0 && is_repeat == 1){
                                    send_status = now_date;
                                }

                                if(is_repeat == 1 && !repeat_week_list.contains(weekDay_ta.toString())){
                                    send_status = now_date;
                                }
                            }

                            // 判断是否已发
                            if(!send_status.equals(now_date)){
                                //选课老师上课通知
                                String chooseLesson = "星期"+  weekDayChoose + "," + subject + "," + class_number + "," + duration ;
                                List<User> users = dao.getUserByChooseLesson(chooseLesson,studio);
                                if(users.size()>0 && remind == 1){
                                    choose = 1;
                                    for(int ui=0;ui<users.size();ui++){
                                        User user_teacher = users.get(ui);
                                        String openid_get = user_teacher.getOpenid();
                                        classRemind(openid_get,student_name,studio,subject,class_number,duration,date_time,upcoming,id,now_date);
                                    }
                                }

                                //学生家长上课通知
                                if(remind == 1 && choose == 1){
                                    //小程序公众号通知
                                    classRemind(openid,student_name,studio,subject,class_number,duration,date_time,upcoming,id,now_date);

                                    //pwa版上课通知
                                    try {
                                        if(subscription != null){
                                            JSONObject payload = new JSONObject();
                                            payload.put("title",studio);
                                            payload.put("message","上课日期:"+ date_time +"\n上课时间:"+ duration + "\n班号:" + class_number + "\n学生名:" + student_name );
                                            String status = webPushService.sendNotification(subscription,publickey,privatekey,payload.toString());
                                            System.out.printf("status:" + status);
                                        }
                                    } catch (Exception e) {
    //                                    throw new RuntimeException(e);
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
                        for (int j = 0; j < lessons.size(); j++) {
                            Lesson lesson = lessons.get(j);
                            Float left_amount = lesson.getLeft_amount();
                            String subject = lesson.getSubject();
                            String student_lesson = lesson.getStudent_name();
                            String student_split = student_lesson.split("_")[0];
                            Integer urge_payment = lesson.getUrge_payment();
                            if (student_split.equals(student_name) && left_amount <= 2 && send_time.equals(now_time) && urge_payment == 0) {
                                String token = getToken("MOMO_OFFICIAL");
                                String model ="{\"touser\":\"openid\",\"template_id\":\"Bl9ZwhH2pWqL2pgo-WF1T5LPI4QUxmN9y7OWmwvvd58\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing16\":{\"value\": \"time\"},\"thing17\":{\"value\": \"A1\"},\"short_thing5\":{\"value\": \"AA\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";
                                String url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
                                if (!"no_id".equals(official_openid)) {
                                    String[] official_list = official_openid.split(",");
                                    for (int k = 0; k < official_list.length; k++) {
                                        String official_openid_get = official_list[k];
                                        JSONObject queryJson2 = JSONObject.parseObject(model);
                                        queryJson2.put("touser", official_openid_get);
                                        queryJson2.getJSONObject("data").getJSONObject("thing16").put("value", studio + "_" + subject);
                                        queryJson2.getJSONObject("data").getJSONObject("thing17").put("value", student_lesson + "剩下" + left_amount + "课时");
                                        queryJson2.getJSONObject("data").getJSONObject("short_thing5").put("value", "请及时续课");
                                        HttpUtil.sendPostJson(url_send, queryJson2.toJSONString());
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void produceClassRemindRedis() {
        Jedis jedis = new Jedis("localhost", 6379);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");

        List<User> list = dao.getAllUser();
        for (int i = 0; i < list.size(); i++) {
            try {
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

                //获取提前时间
                Calendar cal_today = Calendar.getInstance();
                cal_today.add(Calendar.HOUR_OF_DAY,hours);
                int weekDay_today = cal_today.get(Calendar.DAY_OF_WEEK);
                long td_time = cal_today.getTimeInMillis();
                String td_date = df.format(td_time);

                //获取统一时间
                Calendar cal_tomorrow = Calendar.getInstance();
                cal_tomorrow.add(Calendar.DATE,+1);
                Integer weekDay_tomorrow = cal_tomorrow.get(Calendar.DAY_OF_WEEK);
                long tm_time = cal_tomorrow.getTimeInMillis();
                String tm_date = df.format(tm_time);

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
                    //                throw new RuntimeException(e);
                }

                Integer weekDay = 0;
                String date_time = null;
                List<Schedule> list_schedule = new ArrayList<>();
                List<Schedule> list_schedule_re = new ArrayList<>();
                //上课通知
                if(!"no_name".equals(student_name)){
                    // 通知分类
                    if("统一提醒次日".equals(remindType)){
                        weekDay = weekDay_tomorrow;
                        date_time = df.format(cal_tomorrow.getTime());
                        list_schedule = dao.getScheduleByUser(weekDay_tomorrow,studio,student_name,campus);
                        list_schedule.addAll(list_schedule_re);
                    }else if("提前N小时提醒".equals(remindType) && hours > 0){
                        weekDay = weekDay_today;
                        date_time = df.format(cal_today.getTime());
                        List<Schedule> schedules = dao.getScheduleByUser(weekDay_today,studio,student_name,campus);
                        list_schedule.addAll(list_schedule_re);
                    }

                    // 向redis写入队列
                    if(list_schedule.size() > 0 && weekDay > 0){
                        for (int j = 0; j < list_schedule.size(); j++) {
                            Schedule schedule = list_schedule.get(j);
                            String duration = schedule.getDuration().replace("：",":");
                            String subject = schedule.getSubject();
                            String id = schedule.getId();
                            String send_status = schedule.getSend_status();
                            String student_type = schedule.getStudent_type();
                            String add_date = schedule.getAdd_date();
                            LocalDate localDate = LocalDate.parse(add_date);
                            Integer weekDayChoose = localDate.getDayOfWeek().getValue();
                            Integer hours_prev = schedule.getHours();

                            if("统一提醒次日".equals(remindType)){
                                // 跳过插班生
                                if("transferred".equals(student_type)){
                                    if(!tm_date.equals(add_date)){
                                        send_status = now_date;
                                    }
                                }
                                // 跳过请假生
                                List<Leave> leaves = dao.getLeaveRecordByDate(student_name,studio,subject,campus,tm_date);
                                if(leaves.size()>0){
                                    send_status = now_date;
                                }
                            }else if("提前N小时提醒".equals(remindType)){
                                // 跳过插班生
                                if("transferred".equals(student_type)){
                                    if(!td_date.equals(add_date)){
                                        send_status = now_date;
                                    }
                                }
                                // 跳过请假生
                                List<Leave> leaves = dao.getLeaveRecordByDate(student_name,studio,subject,campus,td_date);
                                if(leaves.size()>0){
                                    send_status = now_date;
                                }
                            }

                            // 判断是否已发
                            if(!send_status.equals(now_date)){
                                String taskData = null;
                                if("统一提醒次日".equals(remindType)){
                                    taskData = remindType+","+openid+","+id+","+timestamp_start/1000;
                                }else if("提前N小时提醒".equals(remindType)){
                                    String today_str = now_date + " " + duration.split("-")[0]+":00";
                                    Date today_str_date = df_now.parse(today_str);
                                    Calendar today_str_cl = Calendar.getInstance();
                                    today_str_cl.setTime(today_str_date);
                                    today_str_cl.add(Calendar.HOUR,-hours_prev);
                                    timestamp_start = today_str_cl.getTimeInMillis();
                                    taskData = remindType+","+openid+","+id+","+timestamp_start/1000;
                                }
                                jedis.zadd("delay_queue",timestamp_start/1000,taskData);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }finally {
                jedis.close();
            }
        }

    }


    @Override
    public void sendDepartureNotice(String student_name, String studio) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd HH:mm:ss");//设置日期格式
        String create_time = df.format(new Date());
        String result = null;
        String model ="{\"touser\":\"openid\",\"template_id\":\"izSojc8jwoAlxi5Cia0z5oE6sMspB8PpNkI--zIsaOs\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing3\":{\"value\": \"AA\"},\"thing2\":{\"value\": \"A1\"},\"time1\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";
        String token = getToken("MOMO_OFFICIAL");


        try {
            List<User> list = dao.getUserByStudent(student_name,studio);
            for (int i = 0; i < list.size(); i++) {
                User user_get = list.get(i);
                String official_openid = user_get.getOfficial_openid();
                String openid_get = user_get.getOpenid();
                String url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
                if(official_openid != null){
                    String[] official_list = official_openid.split(",");
                    for(int j=0;j<official_list.length;j++){
                        String official_openid_get = official_list[j];
                        JSONObject queryJson = JSONObject.parseObject(model);
                        queryJson.put("touser",official_openid_get);
                        queryJson.getJSONObject("data").getJSONObject("thing3").put("value",student_name);
                        queryJson.getJSONObject("data").getJSONObject("thing2").put("value",studio);
                        queryJson.getJSONObject("data").getJSONObject("time1").put("value",create_time);
                        queryJson.getJSONObject("miniprogram").put("pagepath","/pages/departure/departure?studio=" + studio + "&openid=" + openid_get);

                        System.out.println("MOMO_OFFICIAL_PARAM:" + queryJson.toJSONString());
                        result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
                        System.out.printf("MOMO_OFFICIAL_RES:" + result);
                    }
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
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
                        if(!"no_id".equals(official_openid)){
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
    public String sendFeedback(String openid, String target_studio, String expired_time, String days, String mark) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd HH:mm:ss");//设置日期格式
        String create_time = df.format(new Date());
        String result = null;
        String model ="{\"touser\":\"openid\",\"template_id\":\"icj6FVVB2sdpUGbwLvZ3kYnLYMPTYTlXbwxCsXkQ7Hk\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing2\":{\"value\": \"AA\"},\"thing4\":{\"value\": \"A1\"},\"character_string3\":{\"value\": \"A1\"},\"time6\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";

        String official_openid=null;
        if("系统续费".equals(mark)){
            List<User> users = dao.getUser(openid);
            User user = users.get(0);
            official_openid = user.getOfficial_openid();
        } else if ("蓝桃续费".equals(mark)) {
            List<RestaurantUser> restaurantUsers = dao.getRestaurantUserByOpenid(openid);
            RestaurantUser restaurantUser = restaurantUsers.get(0);
            official_openid = restaurantUser.getOfficial_openid();
        }

        try {
            String token = getToken("MOMO_OFFICIAL");
            String url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
            if(official_openid != null){
                String[] official_list = official_openid.split(",");
                for(int j=0;j<official_list.length;j++){
                    String official_openid_get = official_list[j];
                    JSONObject queryJson = JSONObject.parseObject(model);
                    queryJson.put("touser",official_openid_get);
                    queryJson.getJSONObject("data").getJSONObject("thing2").put("value",target_studio);
                    queryJson.getJSONObject("data").getJSONObject("thing4").put("value","续期" + days + "天" );
                    queryJson.getJSONObject("data").getJSONObject("character_string3").put("value","TO"+expired_time );
                    queryJson.getJSONObject("data").getJSONObject("time6").put("value",create_time);

                    System.out.println("MOMO_OFFICIAL_PARAM:" + queryJson.toJSONString());
                    result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
                    System.out.printf("MOMO_OFFICIAL_RES:" + result);
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
        String order_appid = Constants.order_appid;
        String order_secret = Constants.order_secret;
        String official_appid = Constants.official_appid;
        String official_secret = Constants.official_secret;
        String url = "https://api.weixin.qq.com/cgi-bin/token";

        if ("ORDER".equals(app)){
            param = "appid=" + order_appid + "&secret=" + order_secret + "&grant_type=client_credential";
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
    public List getStudentByTeacher(String type,String openid,String duration_time,Integer page,String class_number) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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
            String studio = list_user.get(0).getStudio();
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
            String title = "科目,名字,班号,上课日,签到日,备注,课时,课均单价";
            List<String> data_list = new ArrayList<>();
            if(page == 1){
                if("普通".equals(type)){
                    // 签到记录相关计算
                    List<SignUp> list = dao.getStudentByTeacherByDuration(studio,nick_name,date_start,date_end);
                    for (int i = 0; i < list.size(); i++) {
                        SignUp line = list.get(i);
                        String subject = line.getSubject();
                        String student_name = line.getStudent_name();
                        Float count = line.getCount();
                        String create_time = line.getCreate_time();
                        String sign_time = line.getSign_time();
                        String mark = line.getMark();
                        String class_number_get = line.getClass_number();

                        Float total_money = 0.0f;
                        Float discount_money = 0.0f;
                        Float price = 0.0f;
                        Float sign_price = 0.0f;
                        Float all_lesson = 0.0f;
                        Float give_lesson = 0.0f;

                        // 计算金额
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

                        if("全部".equals(class_number)){
                            count_sum = count_sum + count;
                            price_sum = price_sum + sign_price;
                            sign_sum = sign_sum + 1;
                        }else if(class_number.equals(class_number_get)){
                            count_sum = count_sum + count;
                            price_sum = price_sum + sign_price;
                            sign_sum = sign_sum + 1;
                        }

                        String data_line = subject + "," + student_name + "," + class_number_get + "," + create_time + "," + sign_time + "," +mark + "," + count + "," + price;
                        data_list.add(data_line);
                    }
                } else if ("卡签".equals(type)) {
                    // 统计卡签记录
                    List<CardRecord> list = dao.getCardRecordByTeacherByDuration(studio,nick_name,date_start,date_end);
                    for(int i = 0;i < list.size();i++){
                        count_sum = count_sum + 1;
                    }
                }

                jsonObject_all.put("price_sum", df1.format(price_sum));
                jsonObject_all.put("sign_sum", sign_sum);
                jsonObject_all.put("count_sum", count_sum);
                resul_list.add(jsonObject_all);
                downloadByOpenid(studio,openid,data_list,title,"form");
            }
            //普通签到
            if("普通".equals(type)){
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
                    String class_number_get = line.getClass_number();
                    Float price = 0.0f;
                    Float total_money = 0.0f;
                    Float discount_money = 0.0f;
                    Float all_lesson = 0.0f;
                    Float given_lesson = 0.0f;

                    // 计算单价
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

                    //计算应出勤
                    List<Schedule> schedules = dao.getScheduleByStudent(studio,campus,subject,student_name);
                    int week_count = schedules.size();
                    // 单天平均
                    double day_count  =  (double) week_count/7;

                    // 日期内的应出勤数
                    Date date1 = sdf.parse(date_start);
                    Date date2 = sdf.parse(date_end);
                    long diff = date2.getTime() - date1.getTime();
                    long days = diff / (1000 * 60 * 60 * 24);
                    double all_count = day_count * Math.round(days);

                    // 时间内的实际出勤数
                    List<SignUp> signUps = dao.getSignUpByBetween(student_name,studio,campus,subject,date_start,date_end);
                    Integer all_up = signUps.size();

                    jsonObject.put("studio", studio);
                    jsonObject.put("subject", subject);
                    jsonObject.put("class_number", class_number_get);
                    jsonObject.put("campus", campus);
                    jsonObject.put("student_name", student_name);
                    jsonObject.put("sign_time", sign_time);
                    jsonObject.put("create_time", create_time);
                    jsonObject.put("mark", mark);
                    jsonObject.put("count", count);
                    jsonObject.put("all_count", Math.round(all_count));
                    jsonObject.put("price", df1.format(price));
                    jsonObject.put("all_up", all_up);
                    if(student_name.length() >0){
                        if("全部".equals(class_number)){
                            resul_list.add(jsonObject);
                        }else if(class_number.equals(class_number_get)){
                            resul_list.add(jsonObject);
                        }
                    }
                }
            }
            //卡签
            if("卡签".equals(type)){
                List<CardRecord> list_detail = dao.getCardRecordByTeacherByDurationByPage(studio,nick_name,date_start,date_end,page_start,page_length);
                for(int j = 0;j<list_detail.size();j++){
                    JSONObject jsonObject = new JSONObject();
                    CardRecord line = list_detail.get(j);
                    String subject = line.getSubject();
                    String student_name = line.getStudent_name();
                    String mark = line.getMark();
                    String create_time = line.getCreate_time();
                    String card_id = line.getCard_id();
                    //获取卡
                    List<Card> cards = dao.getCardById(card_id);
                    Card card = cards.get(0);
                    String card_type = card.getType();

                    jsonObject.put("studio", studio);
                    jsonObject.put("subject", subject);
                    jsonObject.put("student_name", student_name);
                    jsonObject.put("mark", mark);
                    jsonObject.put("create_time", create_time);
                    jsonObject.put("card_type", card_type);
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
        String order_appid = Constants.order_appid;
        String order_secret = Constants.order_secret;
        String book_appid = Constants.book_appid;
        String book_secret = Constants.book_secret;
        String url = "https://api.weixin.qq.com/sns/jscode2session";


        if("MOMO".equals(app)){
            param = "appid="+ appid + "&secret=" + secret + "&js_code="+ code +"&grant_type=authorization_code";
        }else if("ORDER".equals(app)){
            param = "appid="+ order_appid + "&secret=" + order_secret + "&js_code="+ code +"&grant_type=authorization_code";
        }else if("BOOK".equals(app)){
            param = "appid="+ book_appid + "&secret=" + book_secret + "&js_code="+ code +"&grant_type=authorization_code";
        }

        try {
            result = HttpUtil.sendPost(url,param);
            JSONObject jsonObject = JSON.parseObject(result);
            openid = jsonObject.getString("openid");
            unionid = jsonObject.getString("unionid");
            if(unionid != null) {
                if ("MOMO".equals(app)) {
                    dao.updateUserUnionid(openid, unionid, app);
                } else if ("ORDER".equals(app)) {
                    dao.updateRestaurantUserUnionid(openid, unionid);
                } else if ("BOOK".equals(app)) {
                    dao.updateBookUserUnionid(openid, unionid);
                }
            }
        } catch (Exception e) {
//			e.printStackTrace();
        }
        return openid;
    }

    @Override
    public String getOpenidOfficial() {
        // 每天更新公众号绑定状态
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

                    dao.updateUserOfficialOpenid(unionid,official_openid);

                    //更新小桃子助手公众号
//                    try {
//                        List<User> users = dao.getUserByUnionid(unionid);
//                        if(users.size()>0){
//                            for(int j =0;j<users.size();j++){
//                                String official_openid_get = users.get(j).getOfficial_openid();
//                                if(official_openid_get == null){
//                                    official_openid_get = "no_id";
//                                }
//                                try {
//                                    if("no_id".equals(official_openid_get) || official_openid_get.isEmpty()){
//                                        dao.updateUserOfficialOpenid(unionid,official_openid);
//                                    }
//                                } catch (Exception e) {
////                                    throw new RuntimeException(e);
//                                }
//
//                            }
//                        }
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }

                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public String updateOpenidOfficialByOpenid(String openid,String unionid) {
        // 更新公众号ID
        try {
            List<User> users = dao.getUserByOpenid(openid);
            for(int i=0;i<users.size();i++){
                User user = users.get(i);
                String official_openid = user.getOfficial_openid();
                if("no_id".equals(official_openid)){
                    String token = getToken("MOMO_OFFICIAL");
                    String url1 = "https://api.weixin.qq.com/cgi-bin/user/get";
                    String param1 = "access_token="+ token;
                    String result1 = HttpUtil.sendPost(url1  ,param1);
                    JSONObject jsonObject1 = JSON.parseObject(result1);
                    String data = jsonObject1.getString("data");
                    JSONObject jsonObject2 = JSON.parseObject(data);
                    String list = jsonObject2.getString("openid").replace("[","").replace("]","").replace("\"","");
                    String[] openid_list = list.split(",");
                    for(int j=0;j<openid_list.length;j++){
                        String official_openid_get = openid_list[j];
                        String url2 = "https://api.weixin.qq.com/cgi-bin/user/info";
                        String param2 = "access_token="+ token + "&openid=" + official_openid_get  + "&lang=zh_CN";
                        String result2 = HttpUtil.sendPost(url2 ,param2);
                        JSONObject jsonObject_info = JSON.parseObject(result2);
                        String unionid_get = jsonObject_info.getString("unionid");
                        if(unionid.equals(unionid_get)){
                            dao.updateUserOfficialOpenid(unionid_get,official_openid_get);
                        }
                    }
                }
            }

        } catch (Exception e) {
//                throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public String updateCoinsLevel() {
        // 每天重置AI课评可用次数 A级:30次,B级:60次,C级:90次,D级:120次
        List<User> user = dao.getUserIsSquare("boss");
        for(int i=0;i<user.size();i++){
            String studio = user.get(i).getStudio();
            String theme = user.get(i).getTheme();
            if("A".equals(theme)){
                dao.updateCoinsByStudio(studio,30.0f);
            }else if("B".equals(theme)){
                dao.updateCoinsByStudio(studio,60.0f);
            }else if("C".equals(theme)){
                dao.updateCoinsByStudio(studio,90.0f);
            }else if("D".equals(theme)){
                dao.updateCoinsByStudio(studio,120.0f);
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
                for(int i=0;i<users.size();i++){
                    User user = users.get(i);
                    String official_openid = user.getOfficial_openid();

                    List<Lesson> lessons_get = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                    total_amount = lessons_get.get(0).getTotal_amount();
                    left_amount = lessons_get.get(0).getLeft_amount();
                    Float total_new = total_amount + lesson_amount;
                    Float left_new = left_amount + lesson_amount;

                    try {
                        String token = getToken("MOMO_OFFICIAL");
                        url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
                        if(!"no_id".equals(official_openid)){
                            String[] official_list = official_openid.split(",");
                            for(int j=0;j<official_list.length;j++){
                                String official_openid_get = official_list[j];
                                JSONObject queryJson = JSONObject.parseObject(model);
                                queryJson.put("touser",official_openid_get);
                                queryJson.getJSONObject("data").getJSONObject("thing2").put("value",student_name+"(" + subject + ")");
                                queryJson.getJSONObject("data").getJSONObject("thing3").put("value","成功续课" + lesson_amount + "课时");
                                queryJson.getJSONObject("data").getJSONObject("thing1").put("value","总" + total_new + "余"+ left_new + "");

                                System.out.println("MOMO_OFFICIAL_PARAM:" + queryJson.toJSONString());
                                result = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
                                System.out.printf("MOMO_OFFICIAL_RES:" + result);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
                    if(!"no_id".equals(official_openid)){
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
                    list = dao.getLessonPackageAll(studio,campus);
                }else {
                    list = dao.getLessonPackageByDurationAll(studio,campus,start_time,end_time + " 23:59:59");
                }
            }

            String title = "学生名,原价,优惠,原课时,赠课时,报课时间,有效期至,备注,操作人";
            List<String> data_list = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                LessonPackage line = list.get(i);

                Float total_money = line.getTotal_money();
                Float discount_money = line.getDiscount_money();
                String mark = line.getMark();
                String end_date = line.getEnd_date();
                String id = line.getId();
                Float all_lesson = line.getAll_lesson();
                Float give_lesson = line.getGive_lesson();
                String nick_name = line.getNick_name();
                student_name  = line.getStudent_name();
                String start_date = line.getStart_date();

                String start_date_next = "9999-01-01";
                if(i > 0){
                    LessonPackage line1 = list.get(i-1);
                    start_date_next = line1.getStart_date();
                }

                List<SignUp> signUps = dao.getSignUpByPackageId(student_name,studio,subject,campus,id);
                Float package_sum = 0.0f;
                if(signUps.size()>0){
                    for (int j = 0; j < signUps.size(); j++) {
                        Float count = signUps.get(j).getCount();
                        package_sum = package_sum + count;
                    }
                }
                Float lesson_left = all_lesson + give_lesson - package_sum;

                Float package_sum1 = 0.0f;
                Float lesson_left1 = 0.0f;
                try {
                    if(start_date.split("-").length >= 3){
                        List<SignUp> signUps1 = dao.getSignUpByBetween(student_name,studio,campus,subject,start_date,start_date_next);
                        if(signUps1.size()>0){
                            for (int j = 0; j < signUps1.size(); j++) {
                                Float count = signUps1.get(j).getCount();
                                package_sum1 = package_sum1 + count;
                            }
                        }
                        lesson_left1 = all_lesson + give_lesson - package_sum1;
                    }
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }

                int end_status = line.getEnd_status();
                jsonObject.put("end_status", "未结课");
                if(end_status == 1){
                    jsonObject.put("end_status", "已结课");
                }
                int is_first = line.getIs_first();

                //json
                jsonObject.put("is_first", is_first);
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
                jsonObject.put("package_sum", package_sum);
                jsonObject.put("lesson_left", lesson_left);
                jsonObject.put("package_sum1", package_sum1);
                jsonObject.put("lesson_left1", lesson_left1);
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
    public List getLessonPackageByStudent(String student_name, String openid,String subject) {
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<User> list_user = dao.getUser(openid);
            String campus = list_user.get(0).getCampus();
            String studio = list_user.get(0).getStudio();

            List<LessonPackage> lessonPackages = dao.getLessonPackageByStudent(student_name,studio,campus,subject);
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
                int end_status = line.getEnd_status();

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
                jsonObject.put("end_status", end_status);
                if(end_status == 0){
                    resul_list.add(jsonObject);
                }
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

                    Integer views = line.getViews();
                    String openid_get = line.getOpenid();
                    String teacher = "默认";
                    List<User> users_get = dao.getUserByOpenid(openid_get);
                    if(users_get.size() > 0){
                        User user_get = users_get.get(0);
                        teacher = user_get.getNick_name();
                    }

                    //json
                    jsonObject.put("teacher", teacher);
                    jsonObject.put("openid", openid_get);
                    jsonObject.put("views", views);
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
    public List getCommentModel(String openid,String date_time,String duration,String class_number) {
        List<JSONObject> resul_list = new ArrayList<>();

        List<User> list_user = dao.getUser(openid);
        String campus = list_user.get(0).getCampus();
        String studio = list_user.get(0).getStudio();
        String class_target_int = null;
        String class_name_int = null;
        List<Message> messages = dao.getMessageByDurationDate(studio,campus,date_time,duration);
        if(messages.size()>0){
            class_target_int = messages.get(0).getClass_target();
            class_name_int = messages.get(0).getClass_name();
        }

        List<Message> list = dao.getCommentModel();
        for (int i = 0; i < list.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            Message line = list.get(i);
            String class_target = line.getClass_target();
            String comment = line.getComment();
            String id = line.getId();

            jsonObject.put("class_target", class_target);
            jsonObject.put("class_target_int", class_target_int);
            jsonObject.put("class_name_int", class_name_int);
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
                String avatar = "fa8a634a-40c2-412a-9a95-2bd8d5ba5675.png";
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
                Arrangement arrangement = arrangement_list.get(i);
                //获取字段
                List<Arrangement> check_list = dao.getArrangementByDate(studio,changeday.toString(),class_number,duration,subject,campus);
                if(check_list.size() == 0){
                    arrangement.setDayofweek(changeday.toString());
                    if(changeday != weekday){
                        dao.insertArrangement(arrangement);

                        // 插入选课老师
                        String chooseLesson = "星期"+  weekday + "," + subject + "," + class_number + "," + duration ;
                        String chooseLesso_new = "星期"+  changeday + "," + subject + "," + class_number + "," + duration ;
                        List<User> users = dao.getUserByChooseLesson(chooseLesson,studio);
                        for(int j =0;j < users.size();j++){
                            User user = users.get(j);
                            String lesson = user.getLessons();
                            String new_lesson = lesson + "|" + chooseLesso_new;
                            user.setLessons(new_lesson);
                            updateBossLessons(user);
                        }

                        // 插入排课学生
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
                                int remind  = line_class.getRemind();
                                int hours = line_class.getHours();

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
                                schedule.setRemind(remind);
                                schedule.setHours(hours);
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
        Integer page_start = (page - 1) * 4;
        Integer page_length = 4;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getMessageGrowth(student_name,studio,page_start,page_length);
            for (int i = 0; i < list.size(); i++) {
                String uuids = null;
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                String comment = line.getComment();
                String id = line.getId();
                String create_time = line.getCreate_time();
                String duration = line.getDuration();
                String class_name = line.getClass_name();
                String mp3_url = line.getMp3_url();
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                String v_uuid = line.getVuuid();
                String class_target = line.getClass_target();

                //json
                jsonObject.put("class_target", class_target);
                jsonObject.put("comment", comment);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time.substring(0,10));
                jsonObject.put("duration", duration);
                jsonObject.put("v_uuid", v_uuid);
                jsonObject.put("class_name", class_name);
                jsonObject.put("uuids", uuids);
                jsonObject.put("mp3_url", mp3_url);
                resul_list.add(jsonObject);
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
                String campus= line.getCampus();
                int is_open = line.getIs_open();

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
                String today_time = df.format(new Date());
                Date today_dt = df.parse(today_time.substring(0,10));
                Date expired_time_ad_dt = df.parse(expired_time_ad.substring(0,10));
                int compare = today_dt.compareTo(expired_time_ad_dt);
                jsonObject.put("is_show_ad", "false");
                if(compare>0){
                    jsonObject.put("is_show_ad", "true");
                }

                String id = line.getId();
                int is_online = 0;
                List<Merchant> merchants = dao.getMerchant(studio,campus,Constants.appid);
                if(merchants.size()>0){
                    is_online = 1;
                }

                //json
                jsonObject.put("id", id);
                jsonObject.put("is_open", is_open);
                jsonObject.put("is_online", is_online);
                jsonObject.put("campus", campus);
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
    public List getNewUser(String openid) {
        List<JSONObject> resul_list = new ArrayList<>();
        List<String> student_list = new ArrayList<>();
        try {
            List<User> users = dao.getUser(openid);
            User user = users.get(0);
            String campus = user.getCampus();
            String studio = user.getStudio();

            List<CommunicateRecord> communicateRecords = dao.getCommunicateRecord(studio, 0, 1000,campus);
            for (int i = 0; i < communicateRecords.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                CommunicateRecord communicateRecord = communicateRecords.get(i);

                //获取字段
                String student_name = communicateRecord.getStudent_name();
                String create_time = communicateRecord.getCreate_time();
                String openid_get = communicateRecord.getOpenid();
                String nick_name = communicateRecord.getNick_name();
                String phone_number = communicateRecord.getPhone_number();
                String teacher = communicateRecord.getTeacher();
                String id = communicateRecord.getId();
                String status = "未报课";
                List<Lesson> lessons = dao.getLessonByName(student_name,studio,campus);
                if(lessons.size() > 0){
                    status = "已报课";
                }
                String type = communicateRecord.getType();

                //json
                jsonObject.put("type", type);
                jsonObject.put("status", status);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("student_name", student_name);
                jsonObject.put("phone_number",phone_number);
                jsonObject.put("teacher",teacher);
                jsonObject.put("create_time", create_time);
                jsonObject.put("openid",openid_get);
                jsonObject.put("id",id);

                if(!student_list.contains(student_name)){
                    resul_list.add(jsonObject);
                    student_list.add(student_name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getContract(String openid) {
        List<User> users = dao.getUser(openid);
        User user = users.get(0);
        String campus = user.getCampus();
        String studio = user.getStudio();
        Integer contract_status = user.getContract();

        String student_name = user.getStudent_name();
        StringBuilder sb = new StringBuilder();
        sb.append(student_name);
        List<User> users_all = dao.getUserByOpenid(openid);
        for(int j=0;j< users_all.size();j++){
            String student_get = users_all.get(j).getStudent_name();
            if(!"no_name".equals(student_get) && !student_name.equals(student_get)){
                sb.append(".");
                sb.append(student_get);
            }
        }

        List<Contract> list= null;
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            list = dao.getContract(studio,campus);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Contract line = list.get(i);
                //获取字段
                String contract = line.getContract();
                String create_time = line.getCreate_time();
                String uuid = line.getUuid();
                String type = line.getType();

                //json
                jsonObject.put("uuid", uuid);
                jsonObject.put("type", type);
                jsonObject.put("contract", contract);
                jsonObject.put("student_name", sb);
                jsonObject.put("create_time", create_time);
                jsonObject.put("contract_status", contract_status);
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
                String nick_name = line.getNick_name();
                String uuids = line.getUuids();
                String phone_number = line.getPhone_number();
                String status_cn = "沟通中";
                Integer status = line.getStatus();
                if(status == 1){
                    status_cn = "已完成";
                }
                String teacher = line.getTeacher();

                //json
                jsonObject.put("teacher", teacher);
                jsonObject.put("student_name", student_name);
                jsonObject.put("studio", studio);
                jsonObject.put("campus", campus);
                jsonObject.put("content", content);
                jsonObject.put("id", id);
                jsonObject.put("create_time", create_time);
                jsonObject.put("nick_name",nick_name);
                jsonObject.put("uuids",uuids);
                jsonObject.put("phone_number",phone_number);
                jsonObject.put("status_cn",status_cn);
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
                String nick_name = line.getNick_name();
                String uuids = line.getUuids();
                String status_cn = "沟通中";
                Integer status = line.getStatus();
                if(status == 1){
                    status_cn = "已完成";
                }
                String teacher = line.getTeacher();
                String phone_number = line.getPhone_number();

                //json
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("status_cn", status_cn);
                jsonObject.put("teacher", teacher);
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
                int delete_status = line.getDelete_status();

                //json
                jsonObject.put("delete_status", delete_status);
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
                    String related_id = lesson.getRelated_id();

                    Float total = 0.0f;
                    Float disc = 0.0f;
                    Float all_lesson = 0.0f;
                    Float give_lesson = 0.0f;
                    Float package_lesson = 0.0f;
                    List<LessonPackage> lessonPackages1 = null;

                    // 判断课包是否合并
                    if(is_combine == 0){
                        lessonPackages1 = dao.getLessonPackageByStudentSubject(student_name_all,studio,campus,subject_get);
                    }else if (is_combine == 1){
                        lessonPackages1 = dao.getLessonPackageByStudentCombine(student_name_all,studio,campus);
                    }
                    // 寻找其他关联课包
                    if(!"no_id".equals(related_id)){
                        String[] related_id_list = related_id.split(",");
                        for(int j=0;j < related_id_list.length; j++){
                            String id_get = related_id_list[j];
                            if(id_get != null && id_get != "") {
                                List<Lesson> lessons_re = dao.getLessonById(id_get);
                                if(lessons_re.size()>0){
                                    Lesson lesson_re = lessons_re.get(0);
                                    String student_name_get = lesson_re.getStudent_name();
                                    String subject_re = lesson_re.getSubject();
                                    if (!student_name_all.equals(student_name_get)) {
                                        List<LessonPackage> lessonPackages_re = dao.getLessonPackageByStudentSubject(student_name_get, studio, campus, subject_re);
                                        lessonPackages1.addAll(lessonPackages_re);
                                    }
                                }
                            }
                        }
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

                    // 判断课时是否合并
                    Float consume_lesson = 0.0f;
                    Float consume_lesson_get = 0.0f;
                    Float lesson_gap = total_amount - left_amount;

                    // 计算消耗课时
                    if (is_combine == 0) {
                        List<SignUp> signUps = dao.getSignUp(student_name_all,studio,subject_get,campus);
                        if(signUps.size() > 0) {
                            consume_lesson_get = dao.getAllSignUpByStudent(studio, subject_get, campus, student_name_all);
                        }
                    } else if (is_combine == 1) {
                        consume_lesson_get = dao.getAllSignUpByStudentCombine(studio, campus, student_name_all);
                    }
                    if(consume_lesson_get == null){
                        consume_lesson_get = 0.0f;
                    }


                    // 计算找其他关联消耗课时
                    if(!"no_id".equals(related_id)){
                        String[] related_id_list = related_id.split(",");
                        for(int j=0;j < related_id_list.length; j++){
                            String id_get = related_id_list[j];
                            if(id_get != null && id_get != "") {
                                List<Lesson> lessons_re = dao.getLessonById(id_get);
                                if(lessons_re.size()>0) {
                                    Lesson lesson_re = lessons_re.get(0);
                                    String student_name_get = lesson_re.getStudent_name();
                                    String subject_re = lesson_re.getSubject();
                                    Integer is_combine_re = lesson_re.getIs_combine();

                                    if (!student_name_all.equals(student_name_get)) {
                                        Float consume_lesson_re = 0.0f;
                                        if(is_combine_re == 0){
                                            List<SignUp> signUps1 = dao.getSignUp(student_name_get, studio, subject_re, campus);
                                            if (signUps1.size() > 0) {
                                                consume_lesson_re = dao.getAllSignUpByStudent(studio, subject_re, campus, student_name_get);
                                            }
                                        }else if(is_combine_re == 1){
                                            consume_lesson_re = dao.getAllSignUpByStudentCombine(studio, campus, student_name_get);
                                            if(consume_lesson_re == null){
                                                consume_lesson_re = 0.0f;
                                            }
                                        }
                                        consume_lesson_get = consume_lesson_get + consume_lesson_re;
                                    }
                                }
                            }
                        }
                    }

                    if(consume_lesson_get > 0){
                        consume_lesson = consume_lesson_get;
                    }


                    int compareToResult1 = consume_lesson.compareTo(lesson_gap);
                    int compareToResult2 = package_lesson.compareTo(total_amount);

                    if(compareToResult1 != 0){
                        abnormal_lesson = abnormal_lesson + 1;
                    }

                    if(compareToResult2 != 0){
                        abnormal_package = abnormal_package + 1;
                    }

                    Float price = (total-disc)/(all_lesson+give_lesson);
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
                        Float all_lesson = 0.0f;
                        Float give_lesson = 0.0f;
                        List<LessonPackage> lessonPackages1 = dao.getLessonPackageByStudentSubject(student_name_all,studio,campus,subject);
                        if(lessonPackages1.size()>0){
                            for(int j = 0; j < lessonPackages1.size(); j++){
                                LessonPackage lessonPackage = lessonPackages1.get(j);
                                total = total + lessonPackage.getTotal_money();
                                disc = disc + lessonPackage.getDiscount_money();
                                all_lesson = all_lesson + lessonPackage.getAll_lesson();
                                give_lesson = give_lesson + lessonPackage.getGive_lesson();
                            }
                        }

                        Float price = (total-disc)/(all_lesson+give_lesson);
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

                String uuid = line.getUuid();
                if("no_id".equals(uuid)){
                    uuid = "fa8a634a-40c2-412a-9a95-2bd8d5ba5675.png";
                }

                DecimalFormat df = new DecimalFormat("0.00");
                //json
                jsonObject.put("uuid", uuid);
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
                if(!"no_id".equals(official_openid)){
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

                String theme = line.getTheme();
                String campus = line.getCampus();
                String expired_time = line.getExpired_time();
                String create_time = line.getCreate_time();
                String avatarurl = line.getAvatarurl();

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
                Float coins = line.getCoins();
                int is_square = line.getIs_square();
                jsonObject.put("ai_type", "团体");
                if(is_square==0){
                    jsonObject.put("ai_type", "个人");
                }

                int is_teacher = line.getIs_teacher();
                jsonObject.put("is_teacher", "普通");
                if(is_teacher==1){
                    jsonObject.put("is_teacher", "代理");
                }
                String wechat_id = line.getWechat_id();

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
                jsonObject.put("theme", theme);
                jsonObject.put("coins", coins);
                jsonObject.put("avatarurl", avatarurl);
                jsonObject.put("wechat_id", wechat_id);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getUserByRole(String role) {
        List<JSONObject> resul_list = new ArrayList<>();
        List<User> list = dao.getUserByRole(role);
        try {
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                User line = list.get(i);
                //获取字段
                String studio = line.getStudio();
                String student_name = line.getStudent_name();
                String nick_name = line.getNick_name();
                String openid = line.getOpenid();

                //json
                jsonObject.put("studio", studio);
                jsonObject.put("student_name", student_name);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("openid", openid);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getUserByOpenidQr(String openid_qr,Integer page) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        String now_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
        List<JSONObject> resul_list = new ArrayList<>();
        Integer page_start = (page - 1) * 50;
        Integer page_length = 50;
        Integer all_sum = 0;
        Integer no_try = 0;
        Integer no_paid = 0;
        Integer has_paid = 0;
        Integer has_expired = 0;

        try {
            if(page == 1){
                List<User> list_init = dao.getUserByOpenidQr(openid_qr,0,10000);
                if(Constants.admin_openid.equals(openid_qr)){
                    list_init = dao.getUserByOpenidQrAll(0,10000);
                }
                for (int i = 0; i < list_init.size(); i++) {
                    User line = list_init.get(i);
                    int is_paid = line.getIs_paid();
                    String user_type = line.getUser_type();
                    String expired_time = line.getExpired_time();
                    String today_time = df.format(new Date());
                    Date today_dt = df.parse(today_time.substring(0,10));
                    Date expired_time_dt = df.parse(expired_time.substring(0,10));
                    int compare = today_dt.compareTo(expired_time_dt);

                    all_sum = all_sum + 1;
                    if(is_paid == 1){
                        has_paid = has_paid + 1;
                    }else if(is_paid == 0 && "新用户".equals(user_type)){
                        no_try = no_try + 1;
                    }else if(compare > 0 && "老用户".equals(user_type)){
                        has_expired = has_expired + 1;
                    }else{
                        no_paid = no_paid + 1;
                    }
                }
            }

            List<User> list = dao.getUserByOpenidQr(openid_qr,page_start,page_length);
            if(Constants.admin_openid.equals(openid_qr)){
                list = dao.getUserByOpenidQrAll(page_start,page_length);
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                User line = list.get(i);
                //获取字段
                String studio = line.getStudio();
                int number = 0;
                String pay_type ="未续费";
                List<Book> books = dao.getAccountBookDetailByMark(studio,now_time,"2024-01-01");
                for(int j=0; j < books.size();j++){
                    String mark = books.get(j).getMark();
                    String type = books.get(j).getType();
                    String studio_get = mark.split("_")[0];
                    pay_type = books.get(0).getMark().split("_")[1];
                    if(studio.equals(studio_get) && "收入".equals(type)){
                        number += 1;
                    }
                }

                String student_name = line.getStudent_name();
                String nick_name = line.getNick_name();
                String user_type = line.getUser_type();

                //推荐人
                String openid_qr_get = line.getOpenid_qr();
                List<User> users_qr = dao.getUser(openid_qr_get);
                User user_qr = users_qr.get(0);
                String nick_name_rc = user_qr.getNick_name();

                //本尊
                String openid = line.getOpenid();
                List<User> users = dao.getUser(openid);
                User user = users.get(0);
                String phone_number = user.getPhone_number();
                String wechat_id = user.getWechat_id();

                int is_paid = line.getIs_paid();
                String is_paid_cn = "未返现";
                if("新用户".equals(user_type)){
                    is_paid_cn = "未试用";
                }

                String expired_time = line.getExpired_time();
                String today_time = df.format(new Date());
                Date today_dt = df.parse(today_time.substring(0,10));
                Date expired_time_dt = df.parse(expired_time.substring(0,10));
                int compare = today_dt.compareTo(expired_time_dt);
                if(compare > 0 && "老用户".equals(user_type)){
                    is_paid_cn = "已过期";
                }
                if(is_paid == 1){
                    is_paid_cn = "已返现";
                }

                String cash_uuid = line.getCash_uuid();
                String create_time = line.getCreate_time();

                //json
                jsonObject.put("rank", i + page_start + 1);
                jsonObject.put("studio", studio);
                jsonObject.put("student_name", student_name);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("wechat_id", wechat_id);
                jsonObject.put("openid", openid);
                jsonObject.put("is_paid", is_paid);
                jsonObject.put("is_paid_cn", is_paid_cn);
                jsonObject.put("nick_name_rc", nick_name_rc);
                jsonObject.put("cash_uuid", cash_uuid);
                jsonObject.put("create_time", create_time);
                jsonObject.put("all_sum", all_sum);
                jsonObject.put("no_try", no_try);
                jsonObject.put("no_paid", no_paid);
                jsonObject.put("has_paid", has_paid);
                jsonObject.put("has_expired", has_expired);
                jsonObject.put("pay_type", pay_type);
                jsonObject.put("number", number);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getUserByOpenidQrAll() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        String today_time = df.format(new Date());

        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<User> list = dao.getUserByOpenidQrAll(0,10000);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                User line = list.get(i);
                //获取字段
                String student_name = line.getStudent_name();
                String nick_name = line.getNick_name();
                String openid = line.getOpenid();
                String openid_qr = line.getOpenid_qr();
                List<User> users = dao.getUser(openid_qr);
                String nick_name_rc = users.get(0).getNick_name();
                String user_type = line.getUser_type();
                int is_paid = line.getIs_paid();
                String cash_uuid = line.getCash_uuid();
                String create_time = line.getCreate_time();
                String studio = line.getStudio();
                String phone_number = line.getPhone_number();
                String wechat_id = line.getWechat_id();

                String is_paid_cn = "未返现";
                if("新用户".equals(user_type)){
                    is_paid_cn = "未试用";
                }

                String expired_time = line.getExpired_time();
                Date today_dt = df.parse(today_time.substring(0,10));
                Date expired_time_dt = df.parse(expired_time.substring(0,10));
                int compare = today_dt.compareTo(expired_time_dt);
                if(compare > 0 && "老用户".equals(user_type)){
                    is_paid_cn = "已过期";
                }

                if(is_paid == 1){
                    is_paid_cn = "已返现";
                }

                int number = 0;
                String pay_type ="未续费";
                List<Book> books = dao.getAccountBookDetailByMark(studio,today_time,"2024-01-01");
                for(int j=0; j < books.size();j++){
                    String mark = books.get(j).getMark();
                    String type = books.get(j).getType();
                    String studio_get = mark.split("_")[0];
                    pay_type = books.get(0).getMark().split("_")[1];
                    if(studio.equals(studio_get) && "收入".equals(type)){
                        number += 1;
                    }
                }

                //json
                jsonObject.put("phone_number", phone_number);
                jsonObject.put("wechat_id", wechat_id);
                jsonObject.put("studio", studio);
                jsonObject.put("student_name", student_name);
                jsonObject.put("nick_name", nick_name);
                jsonObject.put("openid", openid);
                jsonObject.put("is_paid", is_paid);
                jsonObject.put("is_paid_cn", is_paid_cn);
                jsonObject.put("nick_name_rc", nick_name_rc);
                jsonObject.put("cash_uuid", cash_uuid);
                jsonObject.put("create_time", create_time);
                jsonObject.put("pay_type", pay_type);
                jsonObject.put("number", number);
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
                String location = line.getLocation();
                String wechat_id = line.getWechat_id();

                //json
                jsonObject.put("location", location);
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
                jsonObject.put("wechat_id", wechat_id);
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
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

                LocalDate dateTime1 = LocalDate.parse(start_date, formatter);
                LocalDate dateTime2 = LocalDate.parse(end_date, formatter);

                while (!dateTime2.isBefore(dateTime1)){
                    JSONObject jsonObject = new JSONObject();
                    Float signCount = 0.0f;
                    Float tryCount = 0.0f;
                    Float leaveCount = 0.0f;
                    Float lessonCount = 0.0f;
                    Float weekPrice = 0.0f;
                    Float all_lesson_count = 0.0f;
                    Float package_count = 0.0f;
                    Float package_sum_l = 0.0f;
                    Float package_sum_m = 0.0f;

                    // 签到
                    List<SignUp> signUps = dao.getAnalyzeSignUpDetail(studio,campus,dateTime2.toString());
                    if(signUps.size() > 0){
                        for (int j = 0; j < signUps.size(); j++) {
                            SignUp signUp = signUps.get(j);
                            String student_name = signUp.getStudent_name();
                            String subject = signUp.getSubject();
                            Float count = signUp.getCount();
                            signCount = signCount + 1.0f;
                            lessonCount = lessonCount + count;
                            String package_id = signUp.getPackage_id();
                            try {
                                List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                                if(lessons.size()>0){
                                    Float price = lessons.get(0).getPrice();
                                    Float total_money = 0.0f;
                                    Float dis_money = 0.0f;
                                    Float all_lesson = 0.0f;
                                    Float give_lesson = 0.0f;
                                    List<LessonPackage> lessonPackages = dao.getLessonPackageById(Integer.parseInt(package_id));
                                    if(lessonPackages.size() == 0){
                                        lessonPackages = dao.getLessonPackageByStudentSubject(student_name,studio,campus,subject);
                                    }
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
                                            price = (total_money - dis_money)/(all_lesson + give_lesson);
                                        }
                                        weekPrice = weekPrice + price*count;
                                    }
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    // 试听
                    List<AnalyzeCount> list1 = dao.getAnalyzeTry(studio,campus,dateTime2.toString());
                    if(list1.size() > 0){
                        tryCount = list1.get(0).getTry_count();
                    }

                    // 请假
                    List<AnalyzeCount> list2 = dao.getAnalyzeLeave(studio,campus,dateTime2.toString());
                    if(list2.size() > 0){
                        leaveCount = list2.get(0).getLeave_count();
                    }

                    // 排课
                    List<AnalyzeCount> list3 = dao.getLessonAllCountBySumUp(studio,campus,dateTime2.toString());
                    if(list3.size() > 0){
                        all_lesson_count = list3.get(0).getLesson_count();
                    }

                    // 续课
                    List<AnalyzeCount> list4 = dao.getAnalyzePackage(studio,campus,dateTime2.toString());
                    if(list4.size() > 0){
                        package_count = list4.get(0).getPackage_count();
                        package_sum_l = list4.get(0).getPackage_sum_l();
                        package_sum_m = list4.get(0).getPackage_sum_m();
                    }

                    DecimalFormat df = new DecimalFormat("0.00");
                    jsonObject.put("create_time", dateTime2.toString());
                    jsonObject.put("tryCount", tryCount);
                    jsonObject.put("leaveCount", leaveCount);
                    jsonObject.put("signCount", signCount);
                    jsonObject.put("lessonCount", df.format(lessonCount));
                    jsonObject.put("all_lesson_count", all_lesson_count);
                    jsonObject.put("weekPrice", df.format(weekPrice));
                    jsonObject.put("rate", df.format(signCount/all_lesson_count*100));
                    jsonObject.put("package_count", package_count);
                    jsonObject.put("package_sum_l", package_sum_l);
                    jsonObject.put("package_sum_m", package_sum_m);
                    if(tryCount>0 || leaveCount >0 || signCount > 0 || package_count >0){
                        resul_list.add(jsonObject);
                    }

                    dateTime2 = dateTime2.minusDays(1);
                }
            }else if("月".equals(dimension)){
                cal.add(Calendar.DATE,-31);
                start_date = fmt.format(cal.getTime()).substring(0,7) + "-01";
                end_date = date_time.substring(0,7) + "-01";

                if(!"无_无".equals(duration_time)){
                    start_date = duration_time.split("_")[0].substring(0,7) + "-01";
                    end_date = duration_time.split("_")[1].substring(0,7) + "-01";
                }

                LocalDate dateTime1 = LocalDate.parse(start_date, formatter);
                LocalDate dateTime2 = LocalDate.parse(end_date, formatter);

                while (!dateTime2.isBefore(dateTime1)){
                    JSONObject jsonObject = new JSONObject();
                    Float signCount = 0.0f;
                    Integer tryCount = 0;
                    Integer leaveCount = 0;
                    Float lessonCount = 0.0f;
                    Float weekPrice = 0.0f;
                    Float all_lesson_count = 0.0f;
                    Float package_count = 0.0f;
                    Float package_sum_l = 0.0f;
                    Float package_sum_m = 0.0f;

                    // 签到
                    List<SignUp> signUps = dao.getAnalyzeSignUpDetailByMonth(studio,campus,dateTime2.toString().substring(0,7));
                    if(signUps.size() > 0){
                        for (int j = 0; j < signUps.size(); j++) {
                            SignUp signUp = signUps.get(j);
                            String student_name = signUp.getStudent_name();
                            String subject = signUp.getSubject();
                            Float count = signUp.getCount();
                            signCount = signCount + 1.0f;
                            lessonCount = lessonCount + count;
                            String package_id = signUp.getPackage_id();
                            try {
                                List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                                if(lessons.size()>0){
                                    Float price = lessons.get(0).getPrice();
                                    Float total_money = 0.0f;
                                    Float dis_money = 0.0f;
                                    Float all_lesson = 0.0f;
                                    Float give_lesson = 0.0f;
                                    List<LessonPackage> lessonPackages = dao.getLessonPackageById(Integer.parseInt(package_id));
                                    if(lessonPackages.size() == 0){
                                        lessonPackages = dao.getLessonPackageByStudentSubject(student_name,studio,campus,subject);
                                    }
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
                                            price = (total_money - dis_money)/(all_lesson + give_lesson);
                                        }
                                        weekPrice = weekPrice + price*count;
                                    }

                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    // 试听
                    List<Schedule> list1 = dao.getTryDetailByMonthStudent(studio,dateTime2.toString().substring(0,7),campus);
                    if(list1.size() > 0){
                        tryCount = list1.size();
                    }

                    // 请假
                    List<Leave> list2 = dao.getAnalyzeLeaveByMonth(studio,campus,dateTime2.toString().substring(0,7));
                    if(list2.size() > 0){
                        leaveCount = list2.size();
                    }

                    // 排课
                    List<AnalyzeCount> list3 = dao.getLessonAllCountBySumUpMonth(studio,campus,dateTime2.toString().substring(0,7));
                    if(list3.size() > 0){
                        all_lesson_count = list3.get(0).getLesson_count()*4;
                    }

                    // 续课
                    List<AnalyzeCount> list4 = dao.getAnalyzePackageByMonth(studio,campus,dateTime2.toString().substring(0,7));
                    if(list4.size() > 0){
                        package_count = list4.get(0).getPackage_count();
                        package_sum_l = list4.get(0).getPackage_sum_l();
                        package_sum_m = list4.get(0).getPackage_sum_m();
                    }


                    DecimalFormat df = new DecimalFormat("0.00");
                    jsonObject.put("create_time", dateTime2.toString().substring(0,7));
                    jsonObject.put("tryCount", tryCount);
                    jsonObject.put("leaveCount", leaveCount);
                    jsonObject.put("signCount", signCount);
                    jsonObject.put("lessonCount", df.format(lessonCount));
                    jsonObject.put("all_lesson_count", all_lesson_count);
                    jsonObject.put("weekPrice", df.format(weekPrice));
                    jsonObject.put("rate", df.format(signCount/all_lesson_count*100));
                    jsonObject.put("package_count", package_count);
                    jsonObject.put("package_sum_l", package_sum_l);
                    jsonObject.put("package_sum_m", package_sum_m);

                    if(tryCount>0 || leaveCount >0 || signCount > 0 || package_count >0){
                        resul_list.add(jsonObject);
                    }

                    dateTime2 = dateTime2.minusMonths(1);
                }
            }else if ("年".equals(dimension)){
                cal.add(Calendar.DATE,-31);
                start_date = fmt.format(cal.getTime()).substring(0,7) + "-01";
                end_date = date_time.substring(0,7) + "-01";

                if(!"无_无".equals(duration_time)){
                    start_date = duration_time.split("_")[0].substring(0,7) + "-01";
                    end_date = duration_time.split("_")[1].substring(0,7) + "-01";
                }

                LocalDate dateTime1 = LocalDate.parse(start_date, formatter);
                LocalDate dateTime2 = LocalDate.parse(end_date, formatter);

                while (!dateTime2.isBefore(dateTime1)){
                    JSONObject jsonObject = new JSONObject();
                    Float signCount = 0.0f;
                    Integer tryCount = 0;
                    Integer leaveCount = 0;
                    Float lessonCount = 0.0f;
                    Float weekPrice = 0.0f;
                    Float all_lesson_count = 0.0f;
                    Float package_count = 0.0f;
                    Float package_sum_l = 0.0f;
                    Float package_sum_m = 0.0f;

                    // 签到
                    List<SignUp> signUps = dao.getAnalyzeSignUpDetailByYear(studio,campus,dateTime2.toString().substring(0,4));
                    if(signUps.size() > 0){
                        for (int j = 0; j < signUps.size(); j++) {
                            SignUp signUp = signUps.get(j);
                            String student_name = signUp.getStudent_name();
                            String subject = signUp.getSubject();
                            Float count = signUp.getCount();
                            signCount = signCount + 1.0f;
                            lessonCount = lessonCount + count;
                            String package_id = signUp.getPackage_id();
                            try {
                                List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                                if(lessons.size()>0){
                                    Float price = lessons.get(0).getPrice();
                                    Float total_money = 0.0f;
                                    Float dis_money = 0.0f;
                                    Float all_lesson = 0.0f;
                                    Float give_lesson = 0.0f;
                                    List<LessonPackage> lessonPackages = dao.getLessonPackageById(Integer.parseInt(package_id));
                                    if(lessonPackages.size() == 0){
                                        lessonPackages = dao.getLessonPackageByStudentSubject(student_name,studio,campus,subject);
                                    }
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
                                            price = (total_money - dis_money)/(all_lesson + give_lesson);
                                        }
                                        weekPrice = weekPrice + price*count;
                                    }

                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    // 试听
                    List<Schedule> list1 = dao.getTryDetailByYearStudent(studio,dateTime2.toString().substring(0,4),campus);
                    if(list1.size() > 0){
                        tryCount = list1.size();
                    }

                    // 请假
                    List<Leave> list2 = dao.getAnalyzeLeaveByYear(studio,campus,dateTime2.toString().substring(0,4));
                    if(list2.size() > 0){
                        leaveCount = list2.size();
                    }

                    // 排课
                    List<AnalyzeCount> list3 = dao.getLessonAllCountBySumUpMonth(studio,campus,dateTime2.toString().substring(0,7));
                    if(list3.size() > 0){
                        all_lesson_count = list3.get(0).getLesson_count()*4*12;
                    }

                    // 续课
                    List<AnalyzeCount> list4 = dao.getAnalyzePackageByYear(studio,campus,dateTime2.toString().substring(0,4));
                    if(list4.size() > 0){
                        package_count = list4.get(0).getPackage_count();
                        package_sum_l = list4.get(0).getPackage_sum_l();
                        package_sum_m = list4.get(0).getPackage_sum_m();
                    }


                    DecimalFormat df = new DecimalFormat("0.00");
                    jsonObject.put("create_time", dateTime2.toString().substring(0,4));
                    jsonObject.put("tryCount", tryCount);
                    jsonObject.put("leaveCount", leaveCount);
                    jsonObject.put("signCount", signCount);
                    jsonObject.put("lessonCount", df.format(lessonCount));
                    jsonObject.put("all_lesson_count", all_lesson_count);
                    jsonObject.put("weekPrice", df.format(weekPrice));
                    jsonObject.put("rate", df.format(signCount/all_lesson_count*100));
                    jsonObject.put("package_count", package_count);
                    jsonObject.put("package_sum_l", package_sum_l);
                    jsonObject.put("package_sum_m", package_sum_m);

                    if(tryCount>0 || leaveCount >0 || signCount > 0 || package_count >0){
                        resul_list.add(jsonObject);
                    }

                    dateTime2 = dateTime2.minusYears(1);
                }
            }

        } catch (ParseException e) {
//            throw new RuntimeException(e);
        }


        return resul_list;
    }

    @Override
    public List getAnalyzeDetailWeek(String studio, String type, String weekday,String campus,String subject_in) {
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<JSONObject> resul_list = new ArrayList<>();
        if(subject_in == null || subject_in.isEmpty() || "undefined".equals(subject_in)){
            subject_in = "全科目";
        }

        //按月算
        if(weekday.length() == 7){
            // 出勤数
            if("出勤数".equals(type)){
                //获取签到次数与课时
                List<AnalyzeCount> list = null;
                if("全科目".equals(subject_in)){
                    list = dao.getAnalyzeSignUpByMonthByStudent(studio,campus,weekday,weekday);
                }else{
                    list = dao.getAnalyzeSignUpByMonthBySubject(studio,campus,subject_in,weekday,weekday);
                }

                for(int i=0;i< list.size();i++){
                    JSONObject jsonObject = new JSONObject();
                    Float weekPrice = 0.0f;
                    Float all_lesson_count = 0.0f;
                    String student_name = list.get(i).getStudent_name();
                    Float signCount = list.get(i).getSign_count();
                    Float lessonCount = list.get(i).getLesson_count();

                    //获取签到记录
                    List<SignUp> signUps = null;
                    if("全科目".equals(subject_in)){
                        signUps = dao.getAnalyzeSignUpDetailByMonthByStudent(studio,campus,weekday,student_name);
                    }else{
                        signUps = dao.getAnalyzeSignUpDetailByMonthBySubject(studio,campus,subject_in,weekday,student_name);
                    }
                    if(signUps.size() > 0){
                        for (int j = 0; j < signUps.size(); j++) {
                            SignUp signUp = signUps.get(j);
                            String subject = signUp.getSubject();
                            Float count = signUp.getCount();
                            String package_id = signUp.getPackage_id();
                            try {
                                //获取课时数
                                List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                                if(lessons.size()>0){
                                    Float price = lessons.get(0).getPrice();
                                    Float total_money = 0.0f;
                                    Float dis_money = 0.0f;
                                    Float all_lesson = 0.0f;
                                    Float give_lesson = 0.0f;

                                    //获取课包记录
                                    List<LessonPackage> lessonPackages = dao.getLessonPackageById(Integer.parseInt(package_id));
                                    if(lessonPackages.size() == 0){
                                        lessonPackages = dao.getLessonPackageByStudentSubject(student_name,studio,campus,subject);
                                    }
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
                                            price = (total_money - dis_money)/(all_lesson + give_lesson);
                                        }
                                        weekPrice = weekPrice + price*count;
                                    }

                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    //获取排课记录
                    List<AnalyzeCount> list3 = null;
                    if("全科目".equals(subject_in)){
                        list3 = dao.getLessonAllCountBySumUpMonthByStudent(studio,campus,student_name);
                    }else{
                        list3 = dao.getLessonAllCountBySumUpMonthBySubject(studio,campus,subject_in,student_name);
                    }
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

            if("请假数".equals(type)){
                List<Leave> leaves = dao.getAnalyzeLeaveByMonth(studio,campus,weekday);
                for(int i=0;i< leaves.size();i++){
                    JSONObject jsonObject = new JSONObject();
                    Leave leave = leaves.get(i);
                    String student_name = leave.getStudent_name();
                    String mark = leave.getMark_leave();
                    String date_time = leave.getDate_time();

                    jsonObject.put("student_name", student_name);
                    jsonObject.put("mark", mark);
                    jsonObject.put("create_time", date_time);
                    resul_list.add(jsonObject);
                }

            }

            if("续费数".equals(type)){
                List<LessonPackage> lessonPackages = dao.getLessonPackageByMonth(studio,campus,weekday);
                for(int i=0;i< lessonPackages.size();i++){
                    JSONObject jsonObject = new JSONObject();
                    LessonPackage lessonPackage = lessonPackages.get(i);
                    String student_name = lessonPackage.getStudent_name();
                    Float total_money = lessonPackage.getTotal_money();
                    Float discount_money = lessonPackage.getDiscount_money();
                    Float all_lesson = lessonPackage.getAll_lesson();
                    Float give_lesson = lessonPackage.getGive_lesson();
                    String mark = lessonPackage.getMark();
                    String create_time = lessonPackage.getCreate_time();

                    jsonObject.put("student_name", student_name);
                    jsonObject.put("total_money", total_money);
                    jsonObject.put("discount_money", discount_money);
                    jsonObject.put("all_lesson", all_lesson);
                    jsonObject.put("give_lesson", give_lesson);
                    jsonObject.put("mark", mark);
                    jsonObject.put("create_time", create_time);
                    resul_list.add(jsonObject);
                }

            }

        //按日算
        }else if(weekday.length() == 10){

            if("出勤数".equals(type)){
                //获取签到次数与课时
                List<AnalyzeCount> list = null;
                if("全科目".equals(subject_in)){
                    list = dao.getAnalyzeSignUpByStudent(studio,campus,weekday,weekday);
                }else{
                    list = dao.getAnalyzeSignUpBySubject(studio,campus,subject_in,weekday,weekday);
                }
                for(int i=0;i< list.size();i++){
                    JSONObject jsonObject = new JSONObject();
                    Float weekPrice = 0.0f;
                    Float all_lesson_count = 0.0f;
                    String student_name = list.get(i).getStudent_name();
                    Float signCount = list.get(i).getSign_count();
                    Float lessonCount = list.get(i).getLesson_count();

                    //获取签到记录
                    List<SignUp> signUps = null;
                    if("全科目".equals(subject_in)){
                        signUps = dao.getAnalyzeSignUpDetailByStudent(studio,campus,weekday,student_name);
                    }else{
                        signUps = dao.getAnalyzeSignUpDetailBySubject(studio,campus,subject_in,weekday,student_name);
                    }
                    if(signUps.size() > 0){
                        for (int j = 0; j < signUps.size(); j++) {
                            SignUp signUp = signUps.get(j);
                            String subject = signUp.getSubject();
                            Float count = signUp.getCount();
                            String package_id = signUp.getPackage_id();
                            try {
                                //获取课时数
                                List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                                if(lessons.size()>0){
                                    Float total_amount = lessons.get(0).getTotal_amount();
                                    Float price = lessons.get(0).getPrice();
                                    Float total_money = 0.0f;
                                    Float dis_money = 0.0f;
                                    Float all_lesson = 0.0f;
                                    Float give_lesson = 0.0f;
                                    //获取课包记录
                                    List<LessonPackage> lessonPackages = dao.getLessonPackageById(Integer.parseInt(package_id));
                                    if(lessonPackages.size() == 0){
                                        lessonPackages = dao.getLessonPackageByStudentSubject(student_name,studio,campus,subject);
                                    }
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
                                            price = (total_money - dis_money)/(all_lesson + give_lesson);
                                        }
                                        weekPrice = weekPrice + price*count;
                                    }
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    //获取排课记录
                    List<AnalyzeCount> list3 = null;
                    if("全科目".equals(subject_in)){
                        list3 = dao.getLessonAllCountBySumUpByStudent(studio,campus,weekday,student_name);
                    }else{
                        list3 = dao.getLessonAllCountBySumUpBySubject(studio,campus,subject_in,weekday,student_name);
                    }
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

            if("请假数".equals(type)){
                List<Leave> leaves = dao.getLeaveRecordByDateAll(studio,campus,weekday);
                for(int i=0;i< leaves.size();i++){
                    JSONObject jsonObject = new JSONObject();
                    Leave leave = leaves.get(i);
                    String student_name = leave.getStudent_name();
                    String mark = leave.getMark_leave();
                    String date_time = leave.getDate_time();

                    jsonObject.put("student_name", student_name);
                    jsonObject.put("mark", mark);
                    jsonObject.put("create_time", date_time);
                    resul_list.add(jsonObject);
                }

            }

            if("续费数".equals(type)) {
                // 解析日期字符串
                LocalDate date = LocalDate.parse(weekday, formatter);
                // 增加一天
                LocalDate newDate = date.plusDays(1);
                // 格式化日期为字符串
                String formattedDate = newDate.format(formatter);

                List<LessonPackage> lessonPackages = dao.getLessonPackageByDurationAll(studio, campus, weekday, formattedDate);
                for (int i = 0; i < lessonPackages.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    LessonPackage lessonPackage = lessonPackages.get(i);
                    String student_name = lessonPackage.getStudent_name();
                    Float total_money = lessonPackage.getTotal_money();
                    Float discount_money = lessonPackage.getDiscount_money();
                    Float all_lesson = lessonPackage.getAll_lesson();
                    Float give_lesson = lessonPackage.getGive_lesson();
                    String mark = lessonPackage.getMark();
                    String create_time = lessonPackage.getCreate_time();

                    jsonObject.put("student_name", student_name);
                    jsonObject.put("total_money", total_money);
                    jsonObject.put("discount_money", discount_money);
                    jsonObject.put("all_lesson", all_lesson);
                    jsonObject.put("give_lesson", give_lesson);
                    jsonObject.put("mark", mark);
                    jsonObject.put("create_time", create_time);
                    resul_list.add(jsonObject);
                }
            }

        // 按年算
        }else if(weekday.length() == 4){
            // 出勤数
            if("出勤数".equals(type)){
                //获取签到次数与课时
                List<AnalyzeCount> list = null;
                if("全科目".equals(subject_in)){
                    list = dao.getAnalyzeSignUpByYearByStudent(studio,campus,weekday,weekday);
                }else{
                    list = dao.getAnalyzeSignUpByYearBySubject(studio,campus,subject_in,weekday,weekday);
                }

                for(int i=0;i< list.size();i++){
                    JSONObject jsonObject = new JSONObject();
                    Float weekPrice = 0.0f;
                    Float all_lesson_count = 0.0f;
                    String student_name = list.get(i).getStudent_name();
                    Float signCount = list.get(i).getSign_count();
                    Float lessonCount = list.get(i).getLesson_count();

                    //获取签到记录
                    List<SignUp> signUps = null;
                    if("全科目".equals(subject_in)){
                        signUps = dao.getAnalyzeSignUpDetailByYearByStudent(studio,campus,weekday,student_name);
                    }else{
                        signUps = dao.getAnalyzeSignUpDetailByMonthBySubject(studio,campus,subject_in,weekday,student_name);
                    }
                    if(signUps.size() > 0){
                        for (int j = 0; j < signUps.size(); j++) {
                            SignUp signUp = signUps.get(j);
                            String subject = signUp.getSubject();
                            Float count = signUp.getCount();
                            String package_id = signUp.getPackage_id();
                            try {
                                //获取课时数
                                List<Lesson> lessons = dao.getLessonByNameSubject(student_name,studio,subject,campus);
                                if(lessons.size()>0){
                                    Float price = lessons.get(0).getPrice();
                                    Float total_money = 0.0f;
                                    Float dis_money = 0.0f;
                                    Float all_lesson = 0.0f;
                                    Float give_lesson = 0.0f;

                                    //获取课包记录
                                    List<LessonPackage> lessonPackages = dao.getLessonPackageById(Integer.parseInt(package_id));
                                    if(lessonPackages.size() == 0){
                                        lessonPackages = dao.getLessonPackageByStudentSubject(student_name,studio,campus,subject);
                                    }
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
                                            price = (total_money - dis_money)/(all_lesson + give_lesson);
                                        }
                                        weekPrice = weekPrice + price*count;
                                    }

                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    //获取排课记录
                    List<AnalyzeCount> list3 = null;
                    if("全科目".equals(subject_in)){
                        list3 = dao.getLessonAllCountBySumUpMonthByStudent(studio,campus,student_name);
                    }else{
                        list3 = dao.getLessonAllCountBySumUpMonthBySubject(studio,campus,subject_in,student_name);
                    }
                    if(list3.size() > 0){
                        all_lesson_count = list3.get(0).getLesson_count()*4*12;
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

            if("请假数".equals(type)){
                List<Leave> leaves = dao.getAnalyzeLeaveByYear(studio,campus,weekday);
                for(int i=0;i< leaves.size();i++){
                    JSONObject jsonObject = new JSONObject();
                    Leave leave = leaves.get(i);
                    String student_name = leave.getStudent_name();
                    String mark = leave.getMark_leave();
                    String date_time = leave.getDate_time();

                    jsonObject.put("student_name", student_name);
                    jsonObject.put("mark", mark);
                    jsonObject.put("create_time", date_time);
                    resul_list.add(jsonObject);
                }

            }

            if("续费数".equals(type)) {
                List<LessonPackage> lessonPackages = dao.getLessonPackageByDurationAll(studio, campus, weekday+"-01-01", weekday+"-12-31");
                for (int i = 0; i < lessonPackages.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    LessonPackage lessonPackage = lessonPackages.get(i);
                    String student_name = lessonPackage.getStudent_name();
                    Float total_money = lessonPackage.getTotal_money();
                    Float discount_money = lessonPackage.getDiscount_money();
                    Float all_lesson = lessonPackage.getAll_lesson();
                    Float give_lesson = lessonPackage.getGive_lesson();
                    String mark = lessonPackage.getMark();
                    String create_time = lessonPackage.getCreate_time();

                    jsonObject.put("student_name", student_name);
                    jsonObject.put("total_money", total_money);
                    jsonObject.put("discount_money", discount_money);
                    jsonObject.put("all_lesson", all_lesson);
                    jsonObject.put("give_lesson", give_lesson);
                    jsonObject.put("mark", mark);
                    jsonObject.put("create_time", create_time);
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
        Integer page_start = (page - 1) * 20;
        Integer page_length = 20;
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
                String official_openid = "no_id";
                String phone_number = "未录入";
                Float price = 0.0f;
                JSONObject jsonObject = new JSONObject();
                Lesson line = list.get(i);

                //获取字段
                student_name = line.getStudent_name();
                String related_id = line.getRelated_id();

                try {
                    List<User> user = dao.getUserByStudent(student_name,studio);
                    if(user.size()>0){
                        parent = user.get(0).getNick_name();
                        avatarurl = user.get(0).getAvatarurl();
//                        phone_number = user.get(0).getPhone_number();
                        official_openid = user.get(0).getOfficial_openid();
                    }
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }

                // 获取关联名单
                StringBuffer related_names = new StringBuffer();
                try {
                    if(!"no_id".equals(related_id)){
                        String[] related_id_list = related_id.split(",");
                        for(int index = 0;index < related_id_list.length; index++){
                            String id_get = related_id_list[index];
                            if(id_get != null && id_get != ""){
                                List<Lesson> lessons_re = dao.getLessonById(id_get);
                                if(lessons_re.size()>0) {
                                    String student_name_re = lessons_re.get(0).getStudent_name();
                                    String subject_re = lessons_re.get(0).getSubject();
                                    String value = student_name_re + "(" + subject_re + ")";
                                    related_names.append(value);
                                    related_names.append(",");
                                }
                            }
                        }
                        if(related_names.length()>0) {
                            related_names = related_names.deleteCharAt(related_names.lastIndexOf(","));
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
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
                String uuid = line.getUuid();
                if("no_id".equals(uuid)){
                    uuid = "fa8a634a-40c2-412a-9a95-2bd8d5ba5675.png";
                }
                String school = line.getSchool();
                String location = line.getLocation();
                String birthdate = line.getBirthdate();
                phone_number = line.getPhone_number();
                Integer urge_payment = line.getUrge_payment();

                String combine = "分";
                if(is_combine == 1){
                    combine = "合";
                }

                String urge_payment_status = "开";
                if(urge_payment == 1){
                    urge_payment_status = "关";
                }

                Float appoint_left = 0.0f;

                try {
                    List<LessonPackage> lessonPackages = dao.getLessonPackage(student_name,studio,campus,subject_get);
                    if(lessonPackages.size()>0){
                        for(int j = 0; j < lessonPackages.size(); j++){
                            LessonPackage lessonPackage = lessonPackages.get(j);
                            total_money = total_money + lessonPackage.getTotal_money();
                            discount_money = discount_money + lessonPackage.getDiscount_money();
                            all_lesson = all_lesson + lessonPackage.getAll_lesson();
                            give_lesson = give_lesson + lessonPackage.getGive_lesson();

                            // 优先课包余课
                            if(j == 0){
                                String package_id_get = lessonPackage.getId();
                                List<SignUp> signUps = dao.getSignUpByPackageId(student_name,studio,subject_get,campus,package_id_get);
                                Float package_sum = 0.0f;
                                if(signUps.size()>0){
                                    for (int k = 0; k < signUps.size(); k++) {
                                        Float count = signUps.get(k).getCount();
                                        package_sum = package_sum + count;
                                    }
                                }
                                appoint_left = all_lesson + give_lesson - package_sum;
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                Float consume_amount = 0.0f;
                Float consume_lesson_get = 0.0f;
                if (is_combine == 0) {
                    List<SignUp> signUps = dao.getSignUp(student_name,studio,subject_get,campus);
                    if(signUps.size() > 0) {
                        consume_lesson_get = dao.getAllSignUpByStudent(studio, subject_get, campus, student_name);
                    }
                } else if (is_combine == 1) {
                    consume_lesson_get = dao.getAllSignUpByStudentCombine(studio, campus, student_name);
                    if(consume_lesson_get == null){
                        consume_lesson_get = 0.0f;
                    }
                }


                // 判断寻找其他关联课时
                if(!"no_id".equals(related_id)){
                    String[] related_id_list = related_id.split(",");
                    for(int j=0;j < related_id_list.length; j++){
                        String id_get = related_id_list[j];
                        if(id_get != null && id_get != "") {
                            List<Lesson> lessons_re = dao.getLessonById(id_get);
                            if(lessons_re.size()>0) {
                                Lesson lesson_re = lessons_re.get(0);
                                String student_name_get = lesson_re.getStudent_name();
                                String subject_re = lesson_re.getSubject();
                                Integer is_combine_re = lesson_re.getIs_combine();

                                if (!student_name.equals(student_name_get)) {
                                    Float consume_lesson_re = 0.0f;
                                    if(is_combine_re == 0){
                                        List<SignUp> signUps1 = dao.getSignUp(student_name_get, studio, subject_re, campus);
                                        if (signUps1.size() > 0) {
                                            consume_lesson_re = dao.getAllSignUpByStudent(studio, subject_re, campus, student_name_get);
                                        }
                                    }else if(is_combine_re == 1){
                                        consume_lesson_re = dao.getAllSignUpByStudentCombine(studio, campus, student_name_get);
                                        if(consume_lesson_re == null){
                                            consume_lesson_re = 0.0f;
                                        }
                                    }
                                    consume_lesson_get = consume_lesson_get + consume_lesson_re;
                                }
                            }
                        }
                    }
                }

                if(consume_lesson_get > 0){
                    consume_amount = consume_lesson_get;
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

                jsonObject.put("appoint_left", appoint_left);
                jsonObject.put("school", school);
                jsonObject.put("urge_payment_status",urge_payment_status);
                jsonObject.put("location", location);
                jsonObject.put("birthdate", birthdate);
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
                jsonObject.put("uuid", uuid);
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
                if(!"no_id".equals(official_openid)){
                    jsonObject.put("official_status", "已关注");
                }
                jsonObject.put("related_names", related_names.toString());

                resul_list.add(jsonObject);

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

                String uuid = line.getUuid();
                if("no_id".equals(uuid)){
                    uuid = "fa8a634a-40c2-412a-9a95-2bd8d5ba5675.png";
                }

                DecimalFormat df = new DecimalFormat("0.00");
                //json
                jsonObject.put("uuid", uuid);
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
                if(!"no_id".equals(official_openid)){
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
                if(!"no_id".equals(official_openid)){
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
                String related_id = line.getRelated_id();
                String uuid = line.getUuid();
                if("no_id".equals(uuid)){
                    uuid = "fa8a634a-40c2-412a-9a95-2bd8d5ba5675.png";
                }

                String combine = "分";
                if(is_combine == 1){
                    combine = "合";
                }


                // 判断课包
                Float all_lesson = 0.0f;
                Float give_lesson = 0.0f;
                Float package_lesson = 0.0f;
                List<LessonPackage> lessonPackages = null;
                try {
                    if(is_combine == 0){
                        lessonPackages = dao.getLessonPackage(student_name,studio,campus,subject);
                    }else if (is_combine == 1){
                        lessonPackages = dao.getLessonPackageByStudentCombine(student_name,studio,campus);
                    }

                    // 寻找其他课包
                    if(!"no_id".equals(related_id)){
                        String[] related_id_list = related_id.split(",");
                        for(int j=0;j < related_id_list.length; j++){
                            String id_get = related_id_list[j];
                            if(id_get != null && id_get != "") {
                                List<Lesson> lessons_re = dao.getLessonById(id_get);
                                if(lessons_re.size()>0) {
                                    Lesson lesson_re = lessons_re.get(0);
                                    String student_name_get = lesson_re.getStudent_name();
                                    String subject_re = lesson_re.getSubject();
                                    if (!student_name.equals(student_name_get)) {
                                        List<LessonPackage> lessonPackages_re = dao.getLessonPackage(student_name_get, studio, campus, subject_re);
                                        lessonPackages.addAll(lessonPackages_re);
                                    }
                                }
                            }
                        }
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

                // 判断课时
                Float consume_lesson = 0.0f;
                Float consume_lesson_get = 0.0f;
                Float consume_amount = 0.0f;
                Float lesson_gap = total_amount - left_amount;
                if (is_combine == 0) {
                    List<SignUp> signUps = dao.getSignUp(student_name,studio,subject,campus);
                    if(signUps.size() > 0) {
                        consume_lesson_get = dao.getAllSignUpByStudent(studio, subject, campus, student_name);
                    }
                } else if (is_combine == 1) {
                    consume_lesson_get = dao.getAllSignUpByStudentCombine(studio, campus, student_name);
                    if(consume_lesson_get == null){
                        consume_lesson_get = 0.0f;
                    }
                }


                // 判断寻找其他关联课时
                if(!"no_id".equals(related_id)){
                    String[] related_id_list = related_id.split(",");
                    for(int j=0;j < related_id_list.length; j++){
                        String id_get = related_id_list[j];
                        if(id_get != null && id_get != "") {
                            List<Lesson> lessons_re = dao.getLessonById(id_get);
                            if(lessons_re.size()>0) {
                                Lesson lesson_re = lessons_re.get(0);
                                String student_name_get = lesson_re.getStudent_name();
                                String subject_re = lesson_re.getSubject();
                                if (!student_name.equals(student_name_get)) {
                                    List<SignUp> signUps1 = dao.getSignUp(student_name_get, studio, subject_re, campus);
                                    if (signUps1.size() > 0) {
                                        Float consume_lesson_re = dao.getAllSignUpByStudent(studio, subject_re, campus, student_name_get);
                                        consume_lesson_get = consume_lesson_get + consume_lesson_re;
                                    }
                                }
                            }
                        }
                    }
                }

                if(consume_lesson_get > 0){
                    consume_lesson = consume_lesson_get;
                    consume_amount = consume_lesson_get;
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

                // 获取关联名单
                StringBuffer related_names = new StringBuffer();
                try {
                    if(!"no_id".equals(related_id)){
                        String[] related_id_list = related_id.split(",");
                        for(int index = 0;index < related_id_list.length; index++){
                            String id_get = related_id_list[index];
                            if(id_get != null && id_get != ""){
                                List<Lesson> lessons_re = dao.getLessonById(id_get);
                                if(lessons_re.size()>0) {
                                    String student_name_re = lessons_re.get(0).getStudent_name();
                                    String subject_re = lessons_re.get(0).getSubject();
                                    String value = student_name_re + "(" + subject_re + ")";
                                    related_names.append(value);
                                    related_names.append(",");
                                }
                            }
                        }
                        if(related_names.length()>0) {
                            related_names = related_names.deleteCharAt(related_names.lastIndexOf(","));
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
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
                    if(!"no_id".equals(official_openid)){
                        jsonObject.put("official_status", "已关注");
                    }

                    jsonObject.put("consume_lesson", consume_lesson);
                    jsonObject.put("lesson_gap", lesson_gap);
                    jsonObject.put("package_lesson", package_lesson);
                    jsonObject.put("uuid", uuid);
                    jsonObject.put("related_names", related_names.toString());
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
                subject_get = line.getSubject();
                Float points = 0.0f;
                List<Points> points1 = dao.getPointsRecordByStudent(student_name,studio,campus,subject_get,"2000-01-01","2100-01-01");
                if(points1.size()>0){
                    for(int j = 0; j < points1.size(); j++){
                        Float point = points1.get(j).getPoints();
                        points = points + point;
                    }
                }

                String uuid = line.getUuid();
                if("no_id".equals(uuid)){
                    uuid = "fa8a634a-40c2-412a-9a95-2bd8d5ba5675.png";
                }
                Integer point_status = line.getPoint_status();

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
                jsonObject.put("uuid", uuid);
                jsonObject.put("point_status", point_status);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getGoodsList(String studio, Integer page,String campus,String content,String type,String goods_type,String openid) {
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
        String photo = null;
        Integer is_group = 0;
        Integer group_num = 0;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            if(type.equals("normal")){
                list = dao.getGoodsList(studio,page_start,page_length,goods_type);
            }else if(type.equals("search")){
                list = dao.getGoodsListSearch(studio,page_start,page_length,content,goods_type);
            }else if(type.equals("all")){
                list = dao.getGoodsList(studio,0,1000,goods_type);
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                GoodsList line = list.get(i);
                //获取字段
                goods_name = line.getGoods_name();
                goods_intro = line.getGoods_intro();
                goods_price = line.getGoods_price();
                group_price = line.getGroup_price();
                Timestamp expired_time = line.getExpired_time();
                Float seckill_price = line.getSeckill_price();
                id = line.getId();
                int like_count = 0;
                List<GoodsLike> goodsLikes1 = dao.getGoodsLikeByGoodsId(id);
                if(goodsLikes1.size()>0){
                    like_count = goodsLikes1.size();
                }

                String liked = "0";
                List<GoodsLike> goodsLikes2 = dao.getGoodsLike(id,openid);
                if(goodsLikes2.size()>0){
                    liked="1";
                }

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

                Float cut_step = line.getCut_step();
                Integer pay_type = line.getPay_type();

                int is_merchant = 0;
                List<Merchant> merchants =dao. getMerchant(studio,campus,Constants.appid);
                if(merchants.size() > 0){
                    is_merchant  = 1;
                }

                //json
                jsonObject.put("is_merchant", is_merchant);
                jsonObject.put("pay_type", pay_type);
                jsonObject.put("cut_step", cut_step);
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
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("seckill_price", seckill_price);
                jsonObject.put("like_count", like_count);
                jsonObject.put("liked", liked);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getGoodsListById(String goods_id) {
        String uuids = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<GoodsList> list = dao.getGoodsListById(goods_id);
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                GoodsList line = list.get(i);
                //获取字段
                String goods_name = line.getGoods_name();
                String goods_intro = line.getGoods_intro();
                Float goods_price = line.getGoods_price();
                Float group_price = line.getGroup_price();
                Timestamp expired_time = line.getExpired_time();
                Float seckill_price = line.getSeckill_price();
                String id = line.getId();
                String create_time = line.getCreate_time();
                try {
                    uuids = line.getUuids().replace("\"","").replace("[","").replace("]","");
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
                Float cut_step = line.getCut_step();
                Integer group_num = line.getGroup_num();
                String photo = line.getPhoto();
                String goods_type = line.getGoods_type();
                String studio = line.getStudio();
                Integer pay_type = line.getPay_type();

                //json
                jsonObject.put("studio", studio);
                jsonObject.put("cut_step", cut_step);
                jsonObject.put("goods_name", goods_name);
                jsonObject.put("goods_intro", goods_intro);
                jsonObject.put("goods_price", goods_price);
                jsonObject.put("create_time", create_time);
                jsonObject.put("group_num", group_num);
                jsonObject.put("id", id);
                jsonObject.put("uuids", uuids);
                jsonObject.put("group_price", group_price);
                jsonObject.put("expired_time", expired_time);
                jsonObject.put("seckill_price", seckill_price);
                jsonObject.put("photo",photo);
                jsonObject.put("goods_type",goods_type);
                jsonObject.put("pay_type",pay_type);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }

    @Override
    public List getSubGoods(String goods_id, String goods_type) {
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<GoodsList> goodsLists = dao.getSubGoods(goods_id,goods_type);

            for (int i = 0; i < goodsLists.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                GoodsList line = goodsLists.get(i);
                //获取字段
                String goods_name = line.getGoods_name();
                String goods_intro = line.getGoods_intro();
                Float goods_price = line.getGoods_price();
                String create_time = line.getCreate_time();
                String uuids = line.getUuids();
                String id = line.getId();

                //json
                jsonObject.put("goods_name", goods_name);
                jsonObject.put("goods_intro", goods_intro);
                jsonObject.put("goods_price", goods_price);
                jsonObject.put("create_time", create_time);
                jsonObject.put("uuids", uuids);
                jsonObject.put("id", id);
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
                Integer point_status = line.getPoint_status();
                student_name = line.getStudent_name();
                byte[] photo = null;
                total_amount = line.getTotal_amount();
                left_amount = line.getLeft_amount();
                percent = (float) Math.round(left_amount * 100 / total_amount);
                id = line.getId();
                create_time = line.getCreate_time();
                subject_get = line.getSubject();
                Float points = 0.0f;
                List<Points> points1 = dao.getPointsRecordByStudent(student_name,studio,campus,subject_get,"2000-01-01","2100-01-01");
                if(points1.size()>0){
                    for(int j = 0; j < points1.size(); j++){
                        Float point = points1.get(j).getPoints();
                        points = points + point;
                    }

                }

                String uuid = line.getUuid();
                if("no_id".equals(uuid)){
                    uuid = "fa8a634a-40c2-412a-9a95-2bd8d5ba5675.png";
                }

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
                    jsonObject.put("point_status", point_status);
                    jsonObject.put("uuid", uuid);
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
                    jsonObject.put("uuid", uuid);
                    jsonObject.put("point_status", point_status);
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
    public List getStandings(String openid, String student_name, String subject,Integer page) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");//设置日期格式
        String create_time = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
        List<User> list_user = dao.getUser(openid);
        String campus = list_user.get(0).getCampus();
        String studio = list_user.get(0).getStudio();
        Integer page_start = (page - 1) * 50;
        Integer page_length = 50;

        List<JSONObject> resul_list = new ArrayList<>();
        List<Lesson> list = null;

        list = dao.getLessonByNameSubject(student_name,studio,subject,campus);
        Integer point_status_init =  list.get(0).getPoint_status();
        if(point_status_init == 0){
            list = dao.getRating(studio,page_start,page_length,campus);
        }

        for (int i = 0; i < list.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            Lesson lesson = list.get(i);
            String student_name_get = lesson.getStudent_name();
            String subject_get = lesson.getSubject();
            Integer point_status = lesson.getPoint_status();
            String point_status_cn = "全部";
            if(point_status == 1){
                point_status_cn = "个人";
            }

            Float points = 0.0f;
            List<Points> points1 = dao.getPointsRecordByStudent(student_name_get,studio,campus,subject_get,"2000-01-01","2100-01-01");
            if(points1.size()>0){
                for(int j = 0; j < points1.size(); j++){
                    Float point = points1.get(j).getPoints();
                    points = points + point;
                }
            }


            Float month_points = 0.0f;
            List<Points> points_list = dao.getPointsRecordByStudent(student_name_get,studio,campus,subject_get,create_time+"-01",create_time + "-31");
            if(points_list.size()>0){
                for(int j = 0; j < points_list.size(); j++){
                    Points points_m = points_list.get(j);
                    Float points_get = points_m.getPoints();
                    month_points = month_points + points_get;
                }
            }

            jsonObject.put("student_name", student_name_get);
            jsonObject.put("subject", subject_get);
            jsonObject.put("points", points);
            jsonObject.put("point_status_cn", point_status_cn);
            jsonObject.put("point_status", point_status);
            jsonObject.put("month_points", month_points);
            jsonObject.put("rank", page_start + i + 1);
            resul_list.add(jsonObject);
        }
        return resul_list;
    }

    @Override
    public List getPointsRecordByMonth(String type, String openid, String student_name, String subject, String month) {

        List<User> list_user = dao.getUser(openid);
        String campus = list_user.get(0).getCampus();
        String studio = list_user.get(0).getStudio();
        List<JSONObject> resul_list = new ArrayList<>();

        List<Points> list = null;
        if("月".equals(type)){
            list = dao.getPointsRecordByStudent(student_name,studio,campus,subject,month+"-01",month+"-31");
        } else if ("年".equals(type)) {
            String year = month.split("-")[0];
            list = dao.getPointsRecordByStudent(student_name,studio,campus,subject,"2022-01-01",year+"-12-31");
        }


        for (int i = 0; i < list.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            Points points = list.get(i);
            String student_name_get = points.getStudent_name();
            String subject_get = points.getSubject();
            Float points_get = points.getPoints();
            String create_time = points.getCreate_time();
            String mark = points.getMark();
            String id = points.getId();

            jsonObject.put("mark", mark);
            jsonObject.put("id", id);
            jsonObject.put("student_name", student_name_get);
            jsonObject.put("subject", subject_get);
            jsonObject.put("points", points_get);
            jsonObject.put("create_time", create_time);
            jsonObject.put("rank", i+1);
            resul_list.add(jsonObject);
        }

        return resul_list;
    }

    @Override
    public String classRemind(String openid, String student_name, String studio, String subject,String class_number, String duration, String date_time, String upcoming,String id,String now_date) {

        String tample ="{\"touser\":\"openid\",\"template_id\":\"MFu-qjMY5twe6Q00f6NaR-cBEn3QYajFquvtysdxk8o\",\"appid\":\"wxa3dc1d41d6fa8284\",\"data\":{\"thing1\":{\"value\": \"time\"},\"time3\":{\"value\": \"A1\"},\"thing2\":{\"value\": \"A1\"}},\"miniprogram\":{\"appid\":\"wxa3dc1d41d6fa8284\",\"pagepath\":\"/pages/index/index\"}}";
        String token = getToken("MOMO_OFFICIAL");
        String url_send = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
        // 绑定公众号通知
        List<User> users = dao.getUser(openid);
        User user = users.get(0);
        String official_openid = user.getOfficial_openid();
        String role = user.getRole();

        String[] official_list = official_openid.split(",");
        for(int i=0;i<official_list.length;i++){
            try {
                String official_openid_get = official_list[i];
                if(!"no_id".equals(official_openid_get)){
                    JSONObject queryJson2 = JSONObject.parseObject(tample);
                    queryJson2.put("touser",official_openid_get);

                    // 分情况
                    String thing1 = student_name;
                    if(!"client".equals(role)){
                        thing1 = "上课提醒已发送" +"(" + student_name + ")";
                    }

                    queryJson2.getJSONObject("data").getJSONObject("thing1").put("value",thing1);
                    queryJson2.getJSONObject("data").getJSONObject("time3").put("value",date_time + " " + duration.split("-")[0]);
                    // 课程预告
                    if("未设".equals(upcoming)){
                        upcoming = class_number;
                    }
                    queryJson2.getJSONObject("data").getJSONObject("thing2").put("value",class_number);

                    System.out.printf("param:" + queryJson2.toJSONString());
                    String result = HttpUtil.sendPostJson(url_send,queryJson2.toJSONString());
                    System.out.printf("res:" + result);
                    // 更新通知状态
                    if("client".equals(role)){
                        dao.updateClassSendStatus(id,now_date);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }


}
