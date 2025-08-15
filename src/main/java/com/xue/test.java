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

        System.out.println(td_time);
        System.out.println(td_date);

        Calendar cal_tomorrow = Calendar.getInstance();
        cal_tomorrow.add(Calendar.HOUR,-23);
        Integer weekDay_tomorrow = cal_tomorrow.get(Calendar.DAY_OF_WEEK);
        long tm_time = cal_tomorrow.getTimeInMillis();
        String tm_date = df.format(tm_time);

        System.out.println(tm_time);
        System.out.println(tm_date);


    }

}
