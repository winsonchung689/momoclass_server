package com.xue.controller;

import com.google.gson.Gson;
import com.xue.service.WebSocketService;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Security;

@RestController
@RequestMapping("/websocket")
public class WebSocketController {

    private static final String PUBLIC_KEY = "BLCgkVlBgC37Mk-8n0G0GMXyXiLVJDudK6A1DCGqLvaeu87B-GZw9jzzybRJ4vZE5BxYGhNGePeiDRWj06bit2o";
    private static final String PRIVATE_KEY = "NulDpKbxecsYor6p1DVhWOm1j3e2VGHRxxmP__B3f-w";
    private static final String SUBJECT = "Foobarbaz";
    private static final String PAYLOAD = "My fancy message";

    @GetMapping("/sendNotification")
    public void sendNotification(String openid,String message){
        WebSocketService.sendMessage(openid,message);
    }

    @RequestMapping("/send")
    public String send(@RequestParam("subscriptionJson") String subscriptionJson) {

        try {
            PushService pushService = new PushService(PUBLIC_KEY, PRIVATE_KEY, SUBJECT);
            Subscription subscription = new Gson().fromJson(subscriptionJson, Subscription.class);
            Notification notification = new Notification(subscription, PAYLOAD);
            HttpResponse httpResponse = pushService.send(notification);
            int statusCode = httpResponse.getStatusLine().getStatusCode();

            return String.valueOf(statusCode);
        } catch (Exception e) {
            e.printStackTrace();
            return "something is wrong";
        }

    }
}
