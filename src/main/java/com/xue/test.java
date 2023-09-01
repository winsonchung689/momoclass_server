package com.xue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class test {
    public static void main(String[] args) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        Integer weekDay_now = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.HOUR_OF_DAY, 1);
        Integer weekDay = cal.get(Calendar.DAY_OF_WEEK);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        String date_time = df.format(cal.getTime());
        String now_time = df_now.format(new Date()).split(" ")[1];
        String now_date = df_now.format(new Date()).split(" ")[0];

        System.out.println(weekDay);
        System.out.println(hour + ":"+ minute);
        System.out.println(date_time + " " +now_date + " " + now_time);
    }
}
