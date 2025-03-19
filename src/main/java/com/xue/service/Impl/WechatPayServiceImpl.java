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
import com.xue.entity.model.PostComment;
import com.xue.entity.model.User;
import com.xue.entity.model.Wallet;
import com.xue.repository.dao.UserMapper;
import com.xue.service.WechatPayService;
import com.xue.util.WechatPayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class WechatPayServiceImpl implements WechatPayService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserMapper dao;

    @Override
    public JSONObject weChatPayDirect(String openid,String mchid,String appid,String description,Integer total,String is_client) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String create_time = df.format(new Date());

        List<User> users = dao.getUserByOpenid(openid);
        User user = users.get(0);
        String studio = user.getStudio();
        String campus = user.getCampus();

        String notify_url = Constants.notify_url;
        String mchSerialNo = Constants.MC_SERIAL_NO;
        String apiV3Key = Constants.API_V3_KEY;
        String privateKeyFromPath = Constants.PRIVATE_KEY_FROM_PATH;
        // 微信公钥
        String publicKeyFromPath = Constants.PUBLIC_KEY_FROM_PATH;
        String publicKeyId = Constants.PUBLIC_KEY_ID;

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
        jsonObject.put("order_no",request.getOutTradeNo());

        //插入wallet
        Wallet wallet =new Wallet();
        wallet.setOrder_no(request.getOutTradeNo());
        wallet.setStudio(studio);
        wallet.setCampus(campus);
        wallet.setAmount(total);
        wallet.setCreate_time(create_time);
        wallet.setType("收入");
        wallet.setAppid(appid);
        wallet.setRate(5.6f);
        wallet.setStatus(0);
        wallet.setDescription(description);
        wallet.setIs_client(Integer.parseInt(is_client));
        if(!"请录入工作室".equals(studio)){
            dao.insertWallet(wallet);
        }

        return jsonObject;
    }

    @Override
    public JSONObject weChatPayPartner(String openid, String mchid, String sub_mchid, String appid, String description, Integer total) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String create_time = df.format(new Date());

        List<User> users = dao.getUserByOpenid(openid);
        User user = users.get(0);
        String studio = user.getStudio();
        String campus = user.getCampus();

        String notify_url = Constants.notify_url ;
        String mchSerialNo = Constants.SER_MC_SERIAL_NO;
        String apiV3Key = Constants.API_V3_KEY;
        String privateKeyFromPath = Constants.SER_PRIVATE_KEY_FROM_PATH;
        // 微信公钥
        String publicKeyFromPath = Constants.SER_PUBLIC_KEY_FROM_PATH;
        String publicKeyId = Constants.SER_PUBLIC_KEY_ID;

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
        jsonObject.put("order_no",prepayRequest.getOutTradeNo());

        //插入wallet
        Wallet wallet =new Wallet();
        wallet.setOrder_no(prepayRequest.getOutTradeNo());
        wallet.setStudio(studio);
        wallet.setCampus(campus);
        wallet.setAmount(total);
        wallet.setCreate_time(create_time);
        wallet.setType("收入");
        wallet.setAppid(appid);
        wallet.setRate(0.6f);
        wallet.setStatus(0);
        wallet.setDescription(description);
        wallet.setIs_client(1);
        if(!"请录入工作室".equals(studio)){
            dao.insertWallet(wallet);
        }

        return jsonObject;
    }

    @Override
    public List getWalletByStudio(String studio) {
        List<JSONObject> resul_list = new ArrayList<>();
        try {
            List<Wallet> wallets = dao.getWalletByStudio(studio);
            for (int i = 0; i < wallets.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                Wallet line = wallets.get(i);
                //获取字段
                String order_no = line.getOrder_no();
                String description = line.getDescription();
                Integer amount = line.getAmount();
                String type = line.getType();
                Float rate = line.getRate();
                Integer status = line.getStatus();
                String create_time = line.getCreate_time();

                jsonObject.put("order_no", order_no);
                jsonObject.put("description", description);
                jsonObject.put("amount", amount);
                jsonObject.put("type", type);
                jsonObject.put("rate",rate);
                jsonObject.put("status", status);
                jsonObject.put("create_time", create_time);
                resul_list.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resul_list;
    }


}
