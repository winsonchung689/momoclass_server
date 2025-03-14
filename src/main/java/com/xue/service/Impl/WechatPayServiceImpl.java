package com.xue.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.service.partnerpayments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.partnerpayments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
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
    public JSONObject weChatPayDirect(String openid,String mchid,String appid,String description,Integer total) {

        String notify_url = Constants.notify_url;
        String mchSerialNo = Constants.MC_SERIAL_NO;
        String apiV3Key = Constants.API_V3_KEY;
        String privateKeyFromPath = Constants.PRIVATE_KEY_FROM_PATH;
        // 微信公钥
        String publicKeyFromPath = Constants.PUBLIC_KEY_FROM_PATH;
        String publicKeyId = Constants.PUBLIC_KEY_ID;
        // 平台证书
        String platform_certificate = Constants.PLATFORM_CERTIFICATE_PATH;
        String certificate_serial = Constants.CERTIFICATE_SERIAL;

        // 使用微信支付公钥的RSA配置
        Config config = new RSAPublicKeyConfig.Builder()
                .merchantId(mchid)
                .privateKeyFromPath(privateKeyFromPath)
                .publicKeyFromPath(publicKeyFromPath)
                .publicKeyId(publicKeyId)
                .merchantSerialNumber(mchSerialNo)
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
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appId",response.getAppId());
        jsonObject.put("timestamp",response.getTimeStamp());
        jsonObject.put("nonceStr",response.getNonceStr());
        jsonObject.put("packageVal",response.getPackageVal());
        jsonObject.put("signType",response.getSignType());
        jsonObject.put("paySign",response.getPaySign());

        return jsonObject;
    }

    @Override
    public JSONObject weChatPayPartner(String openid, String mchid, String sub_mchid, String appid, String description, Integer total) {

        String notify_url = Constants.notify_url ;
        String mchSerialNo = Constants.MC_SERIAL_NO;
        String apiV3Key = Constants.API_V3_KEY;

        // 使用微信支付公钥的RSA配置
        Config config = new RSAPublicKeyConfig.Builder()
                .merchantId(mchid)
                .privateKeyFromPath("/data/certificate/apiclient_key.pem")
                .publicKeyFromPath("/Users/yourname/yourpath/pub_key.pem")
                .publicKeyId("PUB_KEY_ID_00000000000000000000000000000000")
                .merchantSerialNumber(mchSerialNo)
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
        PrepayWithRequestPaymentResponse response = jsapiServiceExtension.prepayWithRequestPayment(prepayRequest,appid);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appId",response.getAppId());
        jsonObject.put("timestamp",response.getTimeStamp());
        jsonObject.put("nonceStr",response.getNonceStr());
        jsonObject.put("packageVal",response.getPackageVal());
        jsonObject.put("signType",response.getSignType());
        jsonObject.put("paySign",response.getPaySign());

        return jsonObject;
    }


}
