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

    @GetMapping("/sendNotification")
    public void sendNotification(String openid,String message){
        WebSocketService.sendMessage(openid,message);
    }

}
