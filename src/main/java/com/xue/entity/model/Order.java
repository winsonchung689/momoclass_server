package com.xue.entity.model;


public class Order {

    private String id;
    private String nick_name;
    private String openid;
    private String phone_number;
    private String location;
    private String goods_name;
    private String goods_intro;
    private String studio;
    private String create_time;
    private String campus;
    private String group_role;
    private String goods_id;
    private String sub_goods_id;
    private String leader_id;
    private Float cut_price;
    private String type;
    private Integer counts;
    private Float amount;
    private Float goods_price;
    private Integer status;


    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public Integer getCounts() {
        return counts;
    }

    public void setCounts(Integer counts) {
        this.counts = counts;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Float getCut_price() {
        return cut_price;
    }

    public void setCut_price(Float cut_price) {
        this.cut_price = cut_price;
    }

    public String getLeader_id() {
        return leader_id;
    }

    public void setLeader_id(String leader_id) {
        this.leader_id = leader_id;
    }

    public String getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(String goods_id) {
        this.goods_id = goods_id;
    }

    public String getSub_goods_id() {
        return sub_goods_id;
    }

    public void setSub_goods_id(String sub_goods_id) {
        this.sub_goods_id = sub_goods_id;
    }

    public String getGroup_role() {
        return group_role;
    }

    public void setGroup_role(String group_role) {
        this.group_role = group_role;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Float getGoods_price() {
        return goods_price;
    }

    public void setGoods_price(Float goods_price) {
        this.goods_price = goods_price;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
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


	@Override
	public String toString() {
		return "Order [goods_intro="+ goods_intro + " goods_name="+ goods_name + "]";
	}

    
}