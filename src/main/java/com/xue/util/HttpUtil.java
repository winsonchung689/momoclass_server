package com.xue.util;

import com.alibaba.fastjson.JSONObject;
import com.wechat.pay.contrib.apache.httpclient.WechatPayUploadHttpPost;
import com.wechat.pay.contrib.apache.httpclient.auth.AutoUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.PrivateKey;

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

        String prepay_id = null;
        CloseableHttpResponse response = httpClient.execute(httpPost);
        System.out.println(response);

        try {
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println(statusCode);
            if(statusCode == 200){
                JSONObject object = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
                prepay_id = object.getString("prepay_id");
                System.out.println("suceess,resp code =" + statusCode + ",return body =" + prepay_id);
                return prepay_id;
            }else if(statusCode == 204){
                System.out.println("suceess,return body =" + statusCode);
            }else {
                System.out.printf("failed,resp code =" + statusCode);
                throw new IOException("request failed");
            }
        } finally {
            response.close();
        }
        return null;
    }


    public static String merchantUploadImage(String filePath) throws URISyntaxException {
        URI uri = new URI("https://api.mch.weixin.qq.com/v3/merchant/media/upload");
        File file = new File(filePath);

        CloseableHttpClient httpClient = null;
        AutoUpdateCertificatesVerifier verifier;
        String media_id = null;
        try {
            PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(
                    new FileInputStream("/path/to/apiclient_key.pem"));

            //使用自动更新的签名验证器，不需要传入证书
            verifier = new AutoUpdateCertificatesVerifier(
                    new WechatPay2Credentials("mchId", new PrivateKeySigner("mchSerialNo", merchantPrivateKey)),
                    "apiV3Key".getBytes("utf-8"));

            httpClient = WechatPayHttpClientBuilder.create()
                    .withMerchant("mchId", "mchSerialNo", merchantPrivateKey)
                    .withValidator(new WechatPay2Validator(verifier))
                    .build();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        try (FileInputStream ins1 = new FileInputStream(file)) {
            String sha256 = DigestUtils.sha256Hex(ins1);
            try (InputStream ins2 = new FileInputStream(file)) {
                HttpPost request = new WechatPayUploadHttpPost.Builder(uri)
                        .withImage(file.getName(), sha256, ins2)
                        .build();
                CloseableHttpResponse response = httpClient.execute(request);
                JSONObject object = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
                media_id = object.getString("media_id");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return media_id;
    }

}



