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
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

//        System.out.println(weekDay_today);
//        System.out.println(td_date);

        Long compare = 10L;
        try {

            Date d = formatter.parse("2026-10-10");
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            cal.add(Calendar.DATE, -1);  // 减1天，得到昨天
            Date yesterday = cal.getTime();
            String yes_date = formatter.format(yesterday);
            System.out.printf(yes_date);

            String today_time = formatter.format(new Date());
            Date today_dt = formatter.parse(today_time.substring(0, 10));
            Date start_date_dt = formatter.parse("2026-10-01");
            Date end_date_dt = formatter.parse("2027-10-01");
            long total_time = end_date_dt.getTime() - start_date_dt.getTime();
//            System.out.println(total_time/(60 * 60 * 24 * 1000));
            long used_time = today_dt.getTime() - start_date_dt.getTime();

            Date date =new Date();
            String now_date = df.format(date);
            System.out.printf(now_date);

            int hour = LocalTime.now().getHour();
            System.out.println(hour);

            LocalDate target = LocalDate.parse("2026-11-29"); // 默认解析 yyyy-MM-dd
            LocalDate today = LocalDate.now();
            if (target.isBefore(today)) {
                System.out.println("1111");
            }


        } catch (ParseException e) {
            throw new RuntimeException(e);
        }



    }

}
