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

//    private static final String publicKey = "BP75YB6apr3U36uUoAGd_oEF4pK3QLu4RQl5jKA7SBvjPs5ssoQzVZKccSqKH-PXBgB5AAp_F4knCx3QRR9Pavg";
//    private static final String privateKey = "6ZAkoZHBvfPRq-0KLIK2ePLZ6HBmpOWVWae2DEuz0Lg";
    private static final String GCMKey = "BBTlFdrD-2wGu50fiPgO2eMw2L9JW7Y6BGrt6nXmkXqxHnyX2SlXSy7EfFXCOzz0rxuubJcJFA86hQaTfdA0jXk";

    private PushService pushService;

    @PostConstruct
    private void init() throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        pushService = new PushService();
    }

    public String sendNotification(Subscription subscription,String publickey,String privatekey, String payload) {
        try {
            //endpoint
            String endpoint = subscription.endpoint;
            //user key/auth
            String userPlickKey =  subscription.keys.p256dh;
            String userAuth = subscription.keys.auth;

            // server public key/private key
            String vapidPublicKey = publickey;
            String vapidPrivateKey = privatekey;

//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("message",payload);

            Notification notification = new Notification(endpoint,userPlickKey,userAuth,payload.getBytes());
            pushService.setSubject("mailto:exmaple@yourdomai.org");
            pushService.setPublicKey(Utils.loadPublicKey(vapidPublicKey));
            pushService.setPrivateKey(Utils.loadPrivateKey(vapidPrivateKey));

//            pushService.setGcmApiKey(GCMKey);

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
