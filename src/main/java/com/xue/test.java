package com.xue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class test {
    public static void main(String[] args) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
        cal_today.add(Calendar.HOUR_OF_DAY,hours);
        weekDay_today = cal_today.get(Calendar.DAY_OF_WEEK);

        cal_today.add(Calendar.DATE,+1);
        int weekDay_ttt = cal_today.get(Calendar.DAY_OF_WEEK);

        hour = cal_today.get(Calendar.HOUR_OF_DAY);
        minute = cal_today.get(Calendar.MINUTE);
        duration_st = hour + ":" + minute;
        if(minute<10){
            duration_st = hour + ":0" + minute;
        }


        System.out.println(weekDay_today);
        System.out.println(weekDay_ttt);
    }
}
