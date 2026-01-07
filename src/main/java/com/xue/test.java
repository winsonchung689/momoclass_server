package com.xue;

import com.alibaba.fastjson.JSONObject;

import javax.swing.plaf.IconUIResource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        int weekDay_today = cal_today.get(Calendar.DAY_OF_WEEK);
        long td_time = cal_today.getTimeInMillis();
        String td_date = df.format(td_time);
        LocalDate localDate = LocalDate.parse("2022-05-08");
        Integer weekDayChoose = localDate.getDayOfWeek().getValue();

//        System.out.println(weekDay_today);
//        System.out.println(td_date);

        Long compare = 10L;
        try {
            Date date =new Date();
            long timestamp = date.getTime()/1000;
            String now_date = df.format(date);

            Date today_dt = df.parse(now_date);

//            Date today_dt = df.parse("2026-01-07");
            Date expired_dt = df.parse("2026-01-10");
            Long day2 = expired_dt.getTime();
            Long day1 = today_dt.getTime();
            compare = (day2 - day1)/(24*3600*1000);
            System.out.println(day2);
            System.out.println(day1);
            System.out.println(compare);
            if(compare > 0){
                System.out.printf("aa");
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }



    }

}
