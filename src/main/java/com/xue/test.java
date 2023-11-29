package com.xue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class test {
    public static void main(String[] args) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");

        try {
            Date date =new Date();
            long timestamp = date.getTime();
            int hour = date.getHours();
            Date date1 = df.parse("2023-10-01");
            long timestamp1 = date1.getTime();
            System.out.println(hour);

            Date date_now = df_now.parse("2023-10-01 16:00:00");
            long timestamp2 = date_now.getTime();
            int hour1 = date_now.getHours();
            System.out.println(hour1);
            long timestamp_end = timestamp2 + 10*60*1000;
            System.out.println(timestamp_end);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

}
