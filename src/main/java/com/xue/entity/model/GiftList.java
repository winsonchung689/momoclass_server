package com.xue.entity.model;


public class GiftList {

    private String id;

    private String gift_name;

    private String studio;

    private String campus;

    private String create_time;

    private Integer delete_status;

    private String uuids;

    private Integer coins;



    public String getUuids() {
        return uuids;
    }

    public void setUuids(String uuids) {
        this.uuids = uuids;
    }

    public Integer getCoins() {
        return coins;
    }

    public void setCoins(Integer coins) {
        this.coins = coins;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public Integer getDelete_status() {
        return delete_status;
    }

    public void setDelete_status(Integer delete_status) {
        this.delete_status = delete_status;
    }

    public String getGift_name() {
        return gift_name;
    }

    public void setGift_name(String gift_name) {
        this.gift_name = gift_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    
}