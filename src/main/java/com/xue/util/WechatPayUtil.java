package com.xue.util;

import com.wechat.pay.java.core.util.PemUtil;
import org.apache.logging.log4j.util.Base64Util;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.UUID;

public class WechatPayUtil {

    // 读取本地图片获取输入流
    public static String generateNonceStr() {
        // 生成一个随机的UUID
        String uuid = UUID.randomUUID().toString();
        // 取UUID的一部分作为nonceStr，例如前16位
        return uuid.substring(0, 16);
    }

    // 生成订单
    public static String generateOrderNo(String type) {
        return type +"_" + System.currentTimeMillis();
    }

    // RSA签名
    public static String generateSignature(String signatureStr,String privateKeyPath){

        try {
            PrivateKey merchantPrivatekey = PemUtil.loadPrivateKeyFromPath(privateKeyPath);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(merchantPrivatekey);
            signature.update(signatureStr.getBytes(StandardCharsets.UTF_8));
            return Base64Utils.encodeToString(signature.sign());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
    }

}
