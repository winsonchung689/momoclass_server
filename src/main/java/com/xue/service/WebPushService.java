package com.xue.service;

import com.alibaba.fastjson.JSONObject;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import nl.martijndwars.webpush.Utils;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.concurrent.ExecutionException;

@Service
public class WebPushService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

//    @Value("${vapid.public.key}")
//    private String publicKey;
//    @Value("${vapid.private.key}")
//    private String privateKey;

    private static final String publicKey = "BP75YB6apr3U36uUoAGd_oEF4pK3QLu4RQl5jKA7SBvjPs5ssoQzVZKccSqKH-PXBgB5AAp_F4knCx3QRR9Pavg";
    private static final String privateKey = "6ZAkoZHBvfPRq-0KLIK2ePLZ6HBmpOWVWae2DEuz0Lg";

    private PushService pushService;

    @PostConstruct
    private void init() throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        pushService = new PushService();
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String sendNotification(Subscription subscription, String payload) {
        try {
            //endpoint
            String endpoint = subscription.endpoint;
            logger.info("endpoint: " + endpoint);

            //user key/auth
            String userPlickKey =  subscription.keys.p256dh;
            String userAuth = subscription.keys.auth;
            logger.info("userPlickKey: " + userPlickKey);
            logger.info("userAuth: " + userAuth);

            // server public key/private key
            String vapidPublicKey = publicKey;
            String vapidPrivateKey = privateKey;
            logger.info("vapidPublicKey: " + vapidPublicKey);
            logger.info("vapidPrivateKey: " + vapidPrivateKey);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("title","Hello");
            jsonObject.put("message","World -- java web");

            Notification notification = new Notification(endpoint,userPlickKey,userAuth,jsonObject.toString().getBytes());
            pushService.setSubject("mailto:exmaple@yourdomai.org");
            pushService.setPublicKey(Utils.loadPublicKey(vapidPublicKey));
            pushService.setPrivateKey(Utils.loadPrivateKey(vapidPrivateKey));

            logger.info("sending..");
            HttpResponse httpResponse = pushService.send(notification);
            logger.info("Content : " + httpResponse);

            int statusCode = httpResponse.getStatusLine().getStatusCode();

            return String.valueOf(statusCode);
        } catch (GeneralSecurityException | IOException | JoseException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

}
