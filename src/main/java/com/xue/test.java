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
            String today_time = formatter.format(new Date());
            Date today_dt = formatter.parse(today_time.substring(0, 10));
            Date start_date_dt = formatter.parse("2026-10-01");
            Date end_date_dt = formatter.parse("2027-10-01");
            long total_time = end_date_dt.getTime() - start_date_dt.getTime();
            System.out.println(total_time/(60 * 60 * 24 * 1000));
            long used_time = today_dt.getTime() - start_date_dt.getTime();


        } catch (ParseException e) {
            throw new RuntimeException(e);
        }



    }

}
