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
//            Date date = df.parse("2024-08-16");
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(date);
//            int weekday = calendar.get(Calendar.DAY_OF_WEEK);
//            System.out.println("weekday:" + weekday);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = formatter.parse("2024-08-16");
            long end_timestamp = date.getTime();
            long timestamp = new Date().getTime();
            System.out.println(end_timestamp);
            System.out.println(timestamp);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

}
