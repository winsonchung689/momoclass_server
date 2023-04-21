package com.xue.controller;

import com.xue.service.WebSocketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/websocket")
public class WebSocketController {

    @GetMapping("/pushone")
    public void pushone(){
        WebSocketService.sendMessage("baoba","test");
    }
}
