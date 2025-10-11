package com.xue.entity.model;


public class SpaceGoodsOrder {

    private String id;

    private String openid;

    private String leader_openid;

    private String goods_id;

    private String group_price;

    private Integer delete_status;

    private String create_time;



    public String getLeader_openid() {
        return leader_openid;
    }

    public void setLeader_openid(String leader_openid) {
        this.leader_openid = leader_openid;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(String goods_id) {
        this.goods_id = goods_id;
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

    public String getGroup_price() {
        return group_price;
    }

    public void setGroup_price(String group_price) {
        this.group_price = group_price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    
}