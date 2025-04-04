package com.xue.service;

import com.alibaba.fastjson.JSONObject;
import com.xue.entity.model.*;

import java.util.List;

public interface WechatPayService {

    public JSONObject weChatPayDirect(String openid, String mchid, String appid, String description, Integer total,String is_client);

    public JSONObject weChatPayPartner(String openid, String mchid, String sub_mchid, String appid, String description, Integer total);

    public List  getWalletByStudio(String studio,String appid);

    public JSONObject weChatPayDirectRefund(String openid,String mchid,String appid,String order_no);

    public JSONObject applymentForSub();
}
