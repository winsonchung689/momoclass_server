package com.xue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class test {
    public static void main(String[] args) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");

//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.DATE,+1);
//        Integer weekDay_tomorrow = cal.get(Calendar.DAY_OF_WEEK);
//        String date_time = df.format(cal.getTime());
//        String now_time = df_now.format(new Date()).split(" ")[1];
//        String now_date = df_now.format(new Date()).split(" ")[0];
//
//        System.out.println(weekDay_tomorrow);

        Integer weekDay_today = 0;
        int hour = 0;
        int hours = 0;
        int minute = 0;
        String duration_st =null;
        Calendar cal_today = Calendar.getInstance();
        cal_today.add(Calendar.HOUR_OF_DAY,23);
        weekDay_today = cal_today.get(Calendar.DAY_OF_WEEK);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,+1);
        int weekDay_ttt = cal.get(Calendar.DAY_OF_WEEK);

        hour = cal_today.get(Calendar.HOUR_OF_DAY);
        minute = cal_today.get(Calendar.MINUTE);
        duration_st = hour + ":" + minute;
        if(minute<10){
            duration_st = hour + ":0" + minute;
        }

        String now_time_end = df_now.format(new Date(new Date().getTime() + 5 * 60000)).split(" ")[1];
        String now_time = df_now.format(new Date()).split(" ")[1];
        String send_time = "17:00:00";
        try {
            Date timestamp1 = df_now.parse( "2023-01-01 " + now_time);
            Date timestamp2 = df_now.parse( "2023-01-01 " + now_time_end);
            Date timestamp3 = df_now.parse( "2023-01-01 " + send_time);
            long now_time_timestamp = timestamp1.getTime();
            long now_time_end_timestamp = timestamp2.getTime();
            long send_time_timestamp = timestamp3.getTime();

            System.out.println(now_time_timestamp);
            System.out.println(now_time_end_timestamp);
            System.out.println(send_time_timestamp);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
//        if(time < now_time_end){
//            System.out.printf("tt");
//        }
        System.out.println(now_time_end);
//        System.out.println(weekDay_ttt);
    }
}
