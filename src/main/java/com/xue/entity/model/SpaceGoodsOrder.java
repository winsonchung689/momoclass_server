package com.xue.entity.model;


public class SpaceGoodsOrder {

    private String id;

    private String openid;

    private String openid_qr;

    private String leader_openid;

    private String goods_id;

    private String group_price;

    private String group_number;

    private String group_lesson;

    private Integer delete_status;

    private String create_time;

    private String order_no;

    private Integer status;



    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public String getOpenid_qr() {
        return openid_qr;
    }

    public void setOpenid_qr(String openid_qr) {
        this.openid_qr = openid_qr;
    }

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

    public String getGroup_number() {
        return group_number;
    }

    public void setGroup_number(String group_number) {
        this.group_number = group_number;
    }

    public String getGroup_lesson() {
        return group_lesson;
    }

    public void setGroup_lesson(String group_lesson) {
        this.group_lesson = group_lesson;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    
}