package com.xue.entity.model;


import java.sql.Timestamp;

public class SpaceGoodsList {

    private String id;

    private String openid;

    private String goods_name;

    private String goods_intro;

    private String create_time;

    private String price_list;

    private Integer delete_status;

    private String uuids;

    private String vuids;



    public String getUuids() {
        return uuids;
    }

    public void setUuids(String uuids) {
        this.uuids = uuids;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getPrice_list() {
        return price_list;
    }

    public void setPrice_list(String price_list) {
        this.price_list = price_list;
    }

    public Integer getDelete_status() {
        return delete_status;
    }

    public void setDelete_status(Integer delete_status) {
        this.delete_status = delete_status;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getGoods_name() {
        return goods_name;
    }

    public void setGoods_name(String goods_name) {
        this.goods_name = goods_name;
    }

    public String getGoods_intro() {
        return goods_intro;
    }

    public void setGoods_intro(String goods_intro) {
        this.goods_intro = goods_intro;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVuids() {
        return vuids;
    }

    public void setVuids(String vuids) {
        this.vuids = vuids;
    }

    
}