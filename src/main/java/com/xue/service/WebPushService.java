package com.xue.service;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
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
        pushService = new PushService(publicKey, privateKey);
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String sendNotification(Subscription subscription, String messageJson) {
        try {
            logger.info("sending..");
            HttpResponse httpResponse = pushService.send(new Notification(subscription, messageJson));
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            logger.info("statusCode: " + statusCode);
            return String.valueOf(statusCode);
        } catch (GeneralSecurityException | IOException | JoseException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

}
