package com.xue.JsonUtils;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Map;

public class JsonUtils {

    public static String doPost(String url, Map<String, String> headers, JSONObject params) {
        String result = null;
        try {
            Connection connection = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .timeout(600000)
                    .method(Connection.Method.POST)
                    .requestBody(params == null ? null : params.toJSONString());

            if (headers != null && headers.size() != 0) {
                connection.headers(headers);
            }
            Connection.Response response = connection.execute();
            result = response.body();
        } catch (IOException e) {
            System.out.println(" * doPsot Error ===> " + e.getMessage() + " \nerror url: " + url);
            e.printStackTrace();
        }
        return result;
    }
}
