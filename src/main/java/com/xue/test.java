package com.xue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class test {
    public static void main(String[] args) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");

        Calendar cal = Calendar.getInstance();
        String end_time = df.format(cal.getTime());
        cal.add(Calendar.DATE,-7);
        String start_time = df.format(cal.getTime());

        Calendar cal_today = Calendar.getInstance();
        cal_today.add(Calendar.HOUR_OF_DAY,2);
        int weekDay_today = cal_today.get(Calendar.DAY_OF_WEEK);

        System.out.println(weekDay_today);

        Calendar cal_tomorrow = Calendar.getInstance();
        cal_tomorrow.add(Calendar.DATE,+1);
        Integer weekDay_tomorrow = cal_tomorrow.get(Calendar.DAY_OF_WEEK);

        System.out.println(weekDay_tomorrow);

    }

}
