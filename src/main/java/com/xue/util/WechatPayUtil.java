package com.xue.util;

import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import com.xue.config.Constants;
import org.apache.logging.log4j.util.Base64Util;
import org.springframework.util.Base64Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
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
            PrivateKey privateKey = PemUtil.loadPrivateKey(new FileInputStream(privateKeyPath));
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(signatureStr.getBytes(StandardCharsets.UTF_8));
            return Base64Utils.encodeToString(signature.sign());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String rsaEncryptOAEP(String message, PrivateKey privateKey)
            throws IllegalBlockSizeException, IOException {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] data = message.getBytes("utf-8");
            byte[] cipherdata = cipher.doFinal(data);
            return Base64.getEncoder().encodeToString(cipherdata);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("当前Java环境不支持RSA v1.5/OAEP", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("无效的公钥", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalBlockSizeException("加密原串的长度不能超过214字节");
        }
    }

}
