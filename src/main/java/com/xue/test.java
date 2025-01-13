package com.xue;

import com.alibaba.fastjson.JSONObject;

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


        try {
            Date date_start = fmt.parse("2025-01-12");
            long start_timestamp = date_start.getTime();
            Date date_end = fmt.parse("2023-01-17");
            long end_timestamp = date_end.getTime();
            String today_time = fmt.format(new Date());
            Date today_dt = fmt.parse(today_time.substring(0,10));
            long today_timestamp = today_dt.getTime();

            System.out.println(start_timestamp);
            System.out.println(end_timestamp);
            System.out.println(today_timestamp);
            if(today_timestamp >= start_timestamp ){
                System.out.println("aaaa");
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


    }

}
