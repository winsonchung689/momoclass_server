package com.xue.entity.model;

import javax.websocket.Session;

public class WebSocketClient {

    public Session session;

    public String uri;

    public Session getSession(){
        return session;
    }

    public void setSession(Session session){
        this.session = session;
    }

    public String getUri(){
        return uri;
    }

    public void setUri(String uri){
        this.uri = uri;
    }
}
