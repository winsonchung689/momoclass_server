package com.xue.entity.model;


import java.sql.Timestamp;

public class GoodsList {

    private String id;

    private String goods_name;

    private String goods_intro;

    private String create_time;

    private Float goods_price;

    private Float group_price;

    private Float seckill_price;

    private Float cut_step;

    private Integer delete_status;

    private Integer is_group;

    private Integer group_num;

    private String studio;

    private String photo;

    private String campus;

    private String uuids;

    private String goods_type;

    private Timestamp expired_time;

    private String goods_id;



    public String getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(String goods_id) {
        this.goods_id = goods_id;
    }

    public String getGoods_type() {
        return goods_type;
    }

    public void setGoods_type(String goods_type) {
        this.goods_type = goods_type;
    }

    public Timestamp getExpired_time() {
        return expired_time;
    }

    public void setExpired_time(Timestamp expired_time) {
        this.expired_time = expired_time;
    }

    public Integer getGroup_num() {
        return group_num;
    }

    public void setGroup_num(Integer group_num) {
        this.group_num = group_num;
    }

    public Float getGroup_price() {
        return group_price;
    }

    public void setGroup_price(Float group_price) {
        this.group_price = group_price;
    }

    public Float getSeckill_price() {
        return seckill_price;
    }

    public void setSeckill_price(Float seckill_price) {
        this.seckill_price = seckill_price;
    }

    public Float getCut_step() {
        return cut_step;
    }

    public void setCut_step(Float cut_step) {
        this.cut_step = cut_step;
    }

    public Integer getIs_group() {
        return is_group;
    }

    public void setIs_group(Integer is_group) {
        this.is_group = is_group;
    }

    public String getUuids() {
        return uuids;
    }

    public void setUuids(String uuids) {
        this.uuids = uuids;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
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

    public Float getGoods_price() {
        return goods_price;
    }

    public void setGoods_price(Float goods_price) {
        this.goods_price = goods_price;
    }

	@Override
	public String toString() {
		return "goodsList [goods_name="+ goods_name + " goods_intro="+ goods_intro + "]";
	}

    
}