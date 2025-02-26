package com.xue.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class HttpUtil {

    public static String sendPost(String url,String param){
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
//            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
//                ex.printStackTrace();
            }
        }
        return result;
    }

    public static String sendPostJson(String url, String data) {

        String response = null;

        try {
            CloseableHttpClient httpclient = null;
            CloseableHttpResponse httpresponse = null;
            try {
                httpclient = HttpClients.createDefault();
                HttpPost httppost = new HttpPost(url);
                StringEntity stringentity = new StringEntity(data, ContentType.create("text/json", "UTF-8"));
                httppost.setEntity(stringentity);
                httpresponse = httpclient.execute(httppost);
                response = EntityUtils.toString(httpresponse.getEntity());
//                System.out.printf("response: " + response);
            } finally {
                if (httpclient != null) {
                    httpclient.close();
                }
                if (httpresponse != null) {
                    httpresponse.close();
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return response;
    }

    public static ByteArrayInputStream sendBytePost(String URL, String json) {
        InputStream inputStream = null;
        ByteArrayInputStream byteArrayInputStream = null;
        // 创建默认的httpClient实例。
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建httppost
        HttpPost httppost = new HttpPost (URL) ;
        httppost.addHeader ("Content-type", "application/json; charset=utf-8");
        httppost.setHeader("Accept", "application/json");
        try {
            StringEntity s = new StringEntity(json, Charset.forName ("UTF-8" )) ;
            s.setContentEncoding ("UTF-8" );
            httppost.setEntity(s) ;
            HttpResponse response = httpclient.execute(httppost) ;
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                // 获取相应实体
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();
                ByteArrayOutputStream outStream = new ByteArrayOutputStream ();
                // 创建一个Buffer字符串
                byte[] buffer = new byte[1024];
                // 每次读取的字符串长度，如果为-1，代表全部读取完毕
                int len = 0;
                // 使用一个输入流Mbuffer里把数据读取出来
                while ((len = inputStream.read (buffer)) != -1) {
                    // 用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，Len代表读取的长度
                    outStream. write(buffer, 0, len);
                }

                // 关闭输入流
                inputStream. close();
                // 把outStream里的数据写入内存
                byteArrayInputStream = new ByteArrayInputStream(outStream.toByteArray());
            }

        } catch (Exception e) {
            e. printStackTrace();
        } finally {
            // 关闭连接，释放资源
            try {
                httpclient.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
            return byteArrayInputStream;
        }
    }

    public static String sendWeChatPayPost(String url,String body) throws IOException{

        // 创建默认的httpClient实例。
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 创建httppost
        HttpPost httpPost = new HttpPost (url);

        StringEntity entity = new StringEntity(body,"UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept","application/json");
        httpPost.setHeader("Content-Type","application/json");

        String result = null;
        CloseableHttpResponse response = httpClient.execute(httpPost);
        System.out.println(response);

        try {
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println(statusCode);
            if(statusCode == 200){
                result = EntityUtils.toString(response.getEntity());
                System.out.println("suceess,resp code =" + statusCode + ",return body =" + result);
                return result;
            }else if(statusCode == 204){
                System.out.println("suceess,return body =" + statusCode);
            }else {
                System.out.printf("failed,resp code =" + statusCode + ", return body = " + result);
                throw new IOException("request failed");
            }
        } finally {
            response.close();
        }
        return null;
    }




}



