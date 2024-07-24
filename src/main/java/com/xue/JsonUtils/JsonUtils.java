package com.xue.JsonUtils;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

    public static String doGet(String url){
        String result = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("Response Body: " + response.toString());
            result = response.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
