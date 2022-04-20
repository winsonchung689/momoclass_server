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
    public int updateLesson(Lesson lesson) {
        int result = 0;
        try {
            String student_name = lesson.getStudent_name();
            Float total = lesson.getTotal_amount();
            Float left = lesson.getLeft_amount();
            String studio = lesson.getStudio();
            Float total_amount = 0.0f;
            Float left_amount = 0.0f;
            if (student_name != null) {
                List<Lesson> lessons = dao.getLessonByName(student_name, studio);
                Lesson lesson_get = lessons.get(0);
                total_amount = lesson_get.getTotal_amount();
                if (total > 0) {
                    total_amount = total;
                }
                left_amount = lesson_get.getLeft_amount();
                if (left >= 0) {
                    left_amount = left;
                }
            }
            lesson.setStudent_name(student_name);
            lesson.setTotal_amount(total_amount);
            lesson.setLeft_amount(left_amount);
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
//		String id = null;
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
//				id = line.getId();
                //json
                jsonObject.put("student_name", student_name);
                jsonObject.put("class_name", class_name);
                jsonObject.put("comment", comment);
                jsonObject.put("photo", photo);
                jsonObject.put("class_target", class_target);
                jsonObject.put("id", id);
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public List getSearch(String student_name, String studio) {
        String comment = null;
        String class_name = null;
        String class_target = null;
        String id = null;
        String create_time = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getSearch(student_name, studio);
            for (int i = 0; i < list.size(); i++) {
                Float percent = 0.0f;
                Float left = 0.0f;
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
                    Float total = lesson.getTotal_amount();
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
        String id =null;
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

                SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                Date create_time_dt = df1.parse(create_time);
                Date sign_time_dt = df1.parse(sign_time);
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
        List<JSONObject> resul_list = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date d = null;

        try {
            d = fmt.parse(date_time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            Integer weekDay = cal.get(Calendar.DAY_OF_WEEK);

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
                //json
                jsonObject.put("add_date", add_date);
                jsonObject.put("age", age);
                jsonObject.put("student_name", student_name);

                List<Lesson> lessons = dao.getLessonByName(student_name, studio);
                Lesson lesson = lessons.get(0);
                left = lesson.getLeft_amount();
                jsonObject.put("left", left);

                jsonObject.put("duration", duration);
                jsonObject.put("create_time", create_time.substring(0,10));
                jsonObject.put("id", id);
                jsonObject.put("update_time", update_time.substring(0,10));

                SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                Date update = df1.parse(update_time);
                Date today = df1.parse(date_time + " 00:00:00");
                int compare = update.compareTo(today);
                if (compare > 0) {
                    jsonObject.put("sign_up", "已签到");

                } else {
                    jsonObject.put("sign_up", "未签到");
                }
                resul_list.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resul_list;
    }

    @Override
    public int deleteComment(Integer id, String role) {
        try {
            if ("boss".equals(role)) {
                dao.deleteComment(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int deleteSignUpRecord(Integer id, String role) {
        try {
            if ("boss".equals(role)) {
                dao.deleteSignUpRecord(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int deleteSchedule(Integer id, String role) {
        try {
            if ("boss".equals(role)) {
                dao.deleteSchedule(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int deleteLesson(Integer id, String role) {
        try {
            if ("boss".equals(role)) {
                dao.deleteLesson(id);
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
        List<JSONObject> resul_list = new ArrayList<>();
        try {

            List<User> list = dao.getUser(openid);
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
        List<String> resul_list = new ArrayList<>();
        try {

            List<User> list = dao.getStudio();
            for (int i = 0; i < list.size(); i++) {
                User line = list.get(i);
                //获取字段
                studio = line.getStudio();

                //json
                resul_list.add(studio);
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
    public List getClassSys(String class_target, String studio, Integer limit) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String student_name = null;
        String class_name = null;
        String id = null;
        String create_time = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getClassSys(class_target, studio, limit);
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
    public List getCourseDetail(String studio, String class_name) {
        byte[] photo = null;
        InputStream inputStream_photo = null;
        String comment = null;
        String student_name = null;
        String id = null;
        String create_time = null;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getCourseDetail(studio, class_name);
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
    public int updateMinusLesson(String student_name, String studio) {
        int result = 0;
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        Float new_left = 0.0f;
        System.out.println(student_name);

        List<Lesson> list = dao.getLessonByName(student_name, studio);
        try {
            for (int i = 0; i < list.size(); i++) {
                Lesson line = list.get(i);
                total_amount = line.getTotal_amount();
                left_amount = line.getLeft_amount();
                new_left = left_amount - 1;
                Lesson lesson = new Lesson();
                lesson.setStudent_name(student_name);
                lesson.setLeft_amount(new_left);
                lesson.setTotal_amount(total_amount);
                lesson.setStudio(studio);
                result = dao.updateLesson(lesson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int updateAddPoints(String student_name, String studio) {
        int result = 0;
        Integer points = 0;
        Integer new_points = 0;
        System.out.println(student_name);
        System.out.println(studio);

        List<Lesson> list = dao.getLessonByName(student_name, studio);
        try {
            for (int i = 0; i < list.size(); i++) {
                Lesson line = list.get(i);
                points = line.getPoints();
                if (points == null) {
                    points = 0;
                }
                new_points = points + 1;
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
    public List getMessage(String studio, Integer page) {
        String comment = null;
        String student_name = null;
        String class_name = null;
        String class_target = null;
        String id = null;
        String create_time = null;
        Integer page_start = (page - 1) * 7;
        Integer page_length = 7;
        List<JSONObject> resul_list = new ArrayList<>();

        try {
            List<Message> list = dao.getMessage(studio, page_start, page_length);
            for (int i = 0; i < list.size(); i++) {
                Float percent = 0.0f;
                Float left = 0.0f;
                JSONObject jsonObject = new JSONObject();
                Message line = list.get(i);
                //获取字段
                student_name = line.getStudent_name();
                class_name = line.getClass_name();
                comment = line.getComment();
                class_target = line.getClass_target();
                id = line.getId();
                create_time = line.getCreate_time();
                studio = line.getStudio();

                try {
                    List<Lesson> lessons = dao.getLessonByName(student_name, studio);
                    Lesson lesson = lessons.get(0);
                    left = lesson.getLeft_amount();
                    Float total = lesson.getTotal_amount();
                    if (left > 0 || total > 0) {
                        percent = (float) Math.round(left * 100 / total);
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
        List<JSONObject> resul_list = new ArrayList<>();
        if(student_name.equals("all")) {
            try {
                List<Lesson> list = dao.getLesson(studio);
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
                    resul_list.add(jsonObject);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                List<Lesson> list = dao.getLessonLikeName(studio,student_name);
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
                    resul_list.add(jsonObject);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resul_list;
    }

    @Override
    public List getRating(String studio,String student_name) {
        Float total_amount = 0.0f;
        Float left_amount = 0.0f;
        String create_time = null;
        String id = null;
        Integer points = 0;
        Float percent = 0.0f;
        List<JSONObject> resul_list = new ArrayList<>();
        if(student_name.equals("all")){
            try {
                List<Lesson> list = dao.getRating(studio);
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
                    //json
                    jsonObject.put("student_name", student_name);
                    jsonObject.put("total_amount", total_amount);
                    jsonObject.put("left_amount", left_amount);
                    jsonObject.put("id", id);
                    jsonObject.put("create_time", create_time);
                    jsonObject.put("percent", percent);
                    jsonObject.put("points", points);
                    jsonObject.put("rank", i + 1);
                    resul_list.add(jsonObject);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                List<Lesson> list = dao.getRatingByName(studio,student_name);
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
                    //json
                    jsonObject.put("student_name", student_name);
                    jsonObject.put("total_amount", total_amount);
                    jsonObject.put("left_amount", left_amount);
                    jsonObject.put("id", id);
                    jsonObject.put("create_time", create_time);
                    jsonObject.put("percent", percent);
                    jsonObject.put("points", points);
                    jsonObject.put("rank", i + 1);
                    resul_list.add(jsonObject);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return resul_list;
    }


}
