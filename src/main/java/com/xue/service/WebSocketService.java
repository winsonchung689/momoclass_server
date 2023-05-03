package com.xue.service;

import com.xue.entity.model.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/websocket/{userName}")
@Component
public class WebSocketService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);

    private static int onlineCount = 0;
    private static ConcurrentHashMap<String, WebSocketClient> webSocketMap = new ConcurrentHashMap<>();

    private Session session;

    private String userName = "";

    @OnOpen
    public void onOpen(Session session, @PathParam("userName") String userName){
        if(!webSocketMap.containsKey(userName)){
            addOnlineCount();
        }
        this.session = session;
        this.userName = userName;
        WebSocketClient client = new WebSocketClient();
        client.setSession(session);
        client.setUri(session.getRequestURI().toString());
        webSocketMap.put(userName,client);

        try {
            sendMessage("connected_" + getOnlineCount());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @OnClose
    public void onClose(){
        if(webSocketMap.containsKey(userName)){
            webSocketMap.remove(userName);
            if(webSocketMap.size()>0){
                subOnlineCount();
            }
        }
//        log.info("当前人数为：" + getOnlineCount());
    }

    @OnMessage
    public void onMessage(String message,Session session){
        log.info("收到信息" + userName + message);
    }

    @OnError
    public void onError(Session session,Throwable error){
        error.printStackTrace();
    }

    public void sendMessage(String message) throws IOException{
        synchronized (session){
            this.session.getBasicRemote().sendText(message);
        }
    }

    public static void sendMessage(String userName,String message){
//        WebSocketClient webSocketClient = webSocketMap.get(userName);
        for (WebSocketClient webSocketClient : webSocketMap.values()) {
            try {
                if(webSocketClient != null){
                    webSocketClient.getSession().getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }

        }


    }

    public static synchronized int getOnlineCount(){
        return onlineCount;
    }

    public static synchronized void addOnlineCount(){
        WebSocketService.onlineCount++;
    }

    public static synchronized void subOnlineCount(){
        WebSocketService.onlineCount--;
    }

    public static void setOnlineCount(int onlineCount){
        WebSocketService.onlineCount = onlineCount;
    }

    public static ConcurrentHashMap<String ,WebSocketClient> getWebSocketMap(){
        return webSocketMap;
    }

    public static void setWebSocketMap(ConcurrentHashMap<String,WebSocketClient> webSocketMap){
        WebSocketService.webSocketMap = webSocketMap;
    }

    public Session getSession(){
        return session;
    }

    public void setSession(Session session){
        this.session = session;
    }

    public void  setUserName(String userName){
        this.userName = userName;
    }

}
