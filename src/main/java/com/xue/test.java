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
import java.util.Calendar;
import java.util.Date;

public class test {
    public static void main(String[] args) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Calendar cal_today = Calendar.getInstance();
        cal_today.add(Calendar.DATE,+1);
        int weekDay_today = cal_today.get(Calendar.DAY_OF_WEEK);
        System.out.println(weekDay_today);

        String dateString = "2025-04-24";
        LocalDate date = LocalDate.parse(dateString);
        int dayofweek = date.getDayOfWeek().getValue();
        System.out.printf( "aa:" + dayofweek);

    }

}
