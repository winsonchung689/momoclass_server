package com.xue.entity.model;


public class SpaceOrder {

    private String id;

    private String openid;

    private String openid_qr;

    private String lesson_id;

    private String create_time;

    private Integer status;



    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getOpenid_qr() {
        return openid_qr;
    }

    public void setOpenid_qr(String openid_qr) {
        this.openid_qr = openid_qr;
    }

    public String getLesson_id() {
        return lesson_id;
    }

    public void setLesson_id(String lesson_id) {
        this.lesson_id = lesson_id;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    
}