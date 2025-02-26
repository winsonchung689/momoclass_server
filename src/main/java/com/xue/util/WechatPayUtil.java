package com.xue.util;

import java.util.UUID;

public class WechatPayUtil {

    // 读取本地图片获取输入流
    public static String generateNonceStr() {
        // 生成一个随机的UUID
        String uuid = UUID.randomUUID().toString();
        // 取UUID的一部分作为nonceStr，例如前16位
        return uuid.substring(0, 16);
    }

}
