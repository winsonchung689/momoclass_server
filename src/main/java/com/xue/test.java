package com.xue;

import com.alibaba.fastjson.JSONObject;

import javax.swing.plaf.IconUIResource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class test {
    public static void main(String[] args) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Calendar cal_today = Calendar.getInstance();
        cal_today.add(Calendar.DATE,+1);
        int weekDay_today = cal_today.get(Calendar.DAY_OF_WEEK);
        System.out.println(weekDay_today);

        Long compare = 10L;
        try {
            Date date =new Date();
            long timestamp = date.getTime();
            String now_date = df_now.format(date).split(" ")[0];
            Date today_dt = df.parse(now_date.substring(0,10));
            Date expired_dt = df.parse("2025-05-23");
            Long day2 = expired_dt.getTime();
            Long day1 = today_dt.getTime();
            compare = (day2 - day1)/(24*3600*1000);
            System.out.printf("compare:" + compare);
            String repeat_week  = "1,2,3";
            List<String> repeat_week_list = Arrays.asList(repeat_week.split(","));
            Integer i = 3;
            boolean aa = repeat_week_list.contains(i.toString());
            System.out.printf("aa:" + aa);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

}
