package com.xue.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import com.xue.config.Constants;
import com.xue.config.TokenCache;
import com.xue.entity.model.*;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;
import com.xue.service.WebPushService;
import com.xue.service.WechatPayService;
import com.xue.util.HttpUtil;
import com.xue.util.WechatPayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WechatPayServiceImpl implements WechatPayService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserMapper dao;

    @Autowired
    private WebPushService webPushService;

    @Override
    public List weChatPayDirect(String openid,String mchid,String appid,String description,Integer total) {

        String notify_url = Constants.notify_url;

        // 使用微信支付公钥的RSA配置
        Config config = new RSAAutoCertificateConfig.Builder()
                .merchantId(mchid)
                .privateKeyFromPath("")
                .merchantSerialNumber("")
                .apiV3Key("")
                .build();

        // 构建service
        JsapiService service = new JsapiService.Builder().config(config).build();
        // request.setXxx(val)设置所需参数，具体参数可见Request定义
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(total);
        amount.setCurrency("CNY");
        request.setAmount(amount);
        request.setAppid(appid);
        request.setMchid(mchid);
        request.setDescription(description);
        request.setNotifyUrl(notify_url);
        request.setOutTradeNo(WechatPayUtil.generateOrderNo());
        Payer payer = new Payer();
        payer.setOpenid(openid);
        request.setPayer(payer);

        List<JSONObject> resul_list = null;
        try {
            // 获取prepay_id
            PrepayResponse response = service.prepay(request);
            String prepay_id = response.getPrepayId();

            // 返回数据给前端
            JSONObject result_json = new JSONObject();
            Long timestamp = System.currentTimeMillis()/1000;
            String nonceStr = WechatPayUtil.generateNonceStr();
            String pack = "prepay_id=" + prepay_id;
            String signatureStr = appid + "\n" + timestamp + "\n" + nonceStr + "\n" + pack + "\n";

            result_json.put("timeStamp",String.valueOf(timestamp));
            result_json.put("nonceStr",nonceStr);
            result_json.put("package",pack);
            result_json.put("signType","RSA");
            result_json.put("paySign",WechatPayUtil.generateSignature(signatureStr,""));
            resul_list.add(result_json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return resul_list;
    }

    @Override
    public List weChatPayPartner(String openid, String mchid, String sub_mchid, String appid, String description, Integer total) {

        String notify_url = Constants.notify_url ;

        String v3_url = Constants.host_name + "/v3/pay/partner/transactions/jsapi";

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sp_appid",appid);
        jsonObject.put("sp_mchid",mchid);
        jsonObject.put("sub_appid",appid);
        jsonObject.put("sub_mchid",sub_mchid);
        jsonObject.put("description",description);
        jsonObject.put("out_trade_no",WechatPayUtil.generateOrderNo());
        jsonObject.put("notify_url",notify_url);

        JSONObject amount = new JSONObject();
        amount.put("total",total);
        amount.put("currency","CNY");
        jsonObject.put("amount",amount);

        JSONObject payer = new JSONObject();
        payer.put("sp_openid",openid);
        jsonObject.put("payer",payer);

        List<JSONObject> resul_list = new ArrayList<>();
        try {
            String prepay_id = HttpUtil.sendWeChatPayPost(v3_url,jsonObject.toString());

            // 返回数据给前端
            JSONObject result_json = new JSONObject();
            Long timestamp = System.currentTimeMillis()/1000;
            String nonceStr = WechatPayUtil.generateNonceStr();
            String pack = "prepay_id=" + prepay_id;
            String signatureStr = appid + "\n" + timestamp + "\n" + nonceStr + "\n" + pack + "\n";

            result_json.put("timeStamp",String.valueOf(timestamp));
            result_json.put("nonceStr",nonceStr);
            result_json.put("package",pack);
            result_json.put("signType","RSA");
            result_json.put("paySign",WechatPayUtil.generateSignature(signatureStr,""));
            resul_list.add(result_json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return resul_list;
    }


}
