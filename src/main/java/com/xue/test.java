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

        System.out.println(start_time);
        System.out.println(end_time);

        Integer start_week = 0;
        Integer end_week = 0;
        try {
            long timestamp_start = df.parse(start_time).getTime();
            long timestamp_end = df.parse(end_time).getTime();
            while(timestamp_start <= timestamp_end){
                System.out.println(timestamp_start);
                Date date = new Date(timestamp_start);
                String dateString = df.format(date);
                System.out.println(dateString);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timestamp_start);

                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                System.out.println(dayOfWeek);

                timestamp_start = timestamp_start + 60*60*24*1000;
            }

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

}
