package com.xue.service.Impl;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.service.partnerpayments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.partnerpayments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.xue.config.Constants;
import com.xue.repository.dao.UserMapper;
import com.xue.service.WechatPayService;
import com.xue.util.WechatPayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WechatPayServiceImpl implements WechatPayService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserMapper dao;

    @Override
    public String weChatPayDirect(String openid,String mchid,String appid,String description,Integer total) {

        String notify_url = Constants.notify_url;
        String mc_serial = Constants.mc_serial;
        String apiV3Key = Constants.apiV3Key;

        // 使用微信支付公钥的RSA配置
        Config config = new RSAAutoCertificateConfig.Builder()
                .merchantId(mchid)
                .privateKeyFromPath("/data/certificate/apiclient_key.pem")
                .merchantSerialNumber(mc_serial)
                .apiV3Key(apiV3Key)
                .build();

        // 构建service
        com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension service = new com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension.Builder().config(config).build();

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

        // 获取 response
        com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse response = service.prepayWithRequestPayment(request);

        return response.toString();
    }

    @Override
    public String weChatPayPartner(String openid, String mchid, String sub_mchid, String appid, String description, Integer total) {

        String notify_url = Constants.notify_url ;
        String mc_serial = Constants.mc_serial;
        String apiV3Key = Constants.apiV3Key;

        // 使用微信支付公钥的RSA配置
        Config config = new RSAAutoCertificateConfig.Builder()
                .merchantId(mchid)
                .privateKeyFromPath("/data/certificate/apiclient_key.pem")
                .merchantSerialNumber(mc_serial)
                .apiV3Key(apiV3Key)
                .build();

        // 构建service
        JsapiServiceExtension jsapiServiceExtension = new JsapiServiceExtension.Builder().config(config).build();

        // request.setXxx(val)设置所需参数，具体参数可见Request定义
        com.wechat.pay.java.service.partnerpayments.jsapi.model.PrepayRequest prepayRequest = new com.wechat.pay.java.service.partnerpayments.jsapi.model.PrepayRequest();
        com.wechat.pay.java.service.partnerpayments.jsapi.model.Amount amount = new com.wechat.pay.java.service.partnerpayments.jsapi.model.Amount();
        amount.setTotal(total);
        amount.setCurrency("CNY");
        prepayRequest.setAmount(amount);
        prepayRequest.setSpAppid(appid);
        prepayRequest.setSpMchid(mchid);
        prepayRequest.setSubAppid(appid);
        prepayRequest.setSubMchid(sub_mchid);
        prepayRequest.setDescription(description);
        prepayRequest.setNotifyUrl(notify_url);
        prepayRequest.setOutTradeNo(WechatPayUtil.generateOrderNo());
        com.wechat.pay.java.service.partnerpayments.jsapi.model.Payer payer = new com.wechat.pay.java.service.partnerpayments.jsapi.model.Payer();
        payer.setSpOpenid(openid);
        prepayRequest.setPayer(payer);

        // 获取response
        PrepayWithRequestPaymentResponse prepayResponse = jsapiServiceExtension.prepayWithRequestPayment(prepayRequest,appid);

        return prepayResponse.toString();
    }


}
