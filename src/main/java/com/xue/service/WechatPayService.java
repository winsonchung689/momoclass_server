package com.xue.service;

import com.xue.entity.model.*;

import java.util.List;

public interface WechatPayService {

    public List weChatPayDirect(String openid, String mchid, String appid, String description, Integer total);

    public List weChatPayPartner(String openid, String mchid, String sub_mchid, String appid, String description, Integer total);

}
