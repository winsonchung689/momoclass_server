package com.xue.entity.model;


public class RestaurantOrder {

    private String id;

    private String restaurant;

    private String food_name;

    private String category;

    private int num;

    private Float price;

    private String create_time;

    private String openid;

    private int delete_status;

    private int status;

    private String goods_id;

    private String order_no;

    private String order_img;

    private Integer group_buy;

    private int shop_status;


    public int getShop_status() {
        return shop_status;
    }

    public void setShop_status(int shop_status) {
        this.shop_status = shop_status;
    }

    public Integer getGroup_buy() {
        return group_buy;
    }

    public void setGroup_buy(Integer group_buy) {
        this.group_buy = group_buy;
    }

    public String getOrder_img() {
        return order_img;
    }

    public void setOrder_img(String order_img) {
        this.order_img = order_img;
    }

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public String getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(String goods_id) {
        this.goods_id = goods_id;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(String restaurant) {
        this.restaurant = restaurant;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFood_name() {
        return food_name;
    }

    public void setFood_name(String food_name) {
        this.food_name = food_name;
    }

    public int getDelete_status() {
        return delete_status;
    }

    public void setDelete_status(int delete_status) {
        this.delete_status = delete_status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}