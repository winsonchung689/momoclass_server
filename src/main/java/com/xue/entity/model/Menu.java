package com.xue.entity.model;


public class Menu {

    private String id;

    private String restaurant;

    private String food_name;

    private String food_image;

    private String category;

    private Float price;

    private Float group_price;

    private Float discount;

    private String create_time;

    private String introduce;

    private String vuuid;

    private String unit;

    private Integer group_buy;

    private Integer group_limit;

    private String open_time;

    private Integer inventory;

    private Integer for_coupon;

    private Integer is_dynamic;

    private String dynamic_type;

    private Float shipping_fee;



    public Float getShipping_fee() {
        return shipping_fee;
    }

    public void setShipping_fee(Float shipping_fee) {
        this.shipping_fee = shipping_fee;
    }

    public String getDynamic_type() {
        return dynamic_type;
    }

    public void setDynamic_type(String dynamic_type) {
        this.dynamic_type = dynamic_type;
    }

    public Integer getIs_dynamic() {
        return is_dynamic;
    }

    public void setIs_dynamic(Integer is_dynamic) {
        this.is_dynamic = is_dynamic;
    }

    public Integer getFor_coupon() {
        return for_coupon;
    }

    public void setFor_coupon(Integer for_coupon) {
        this.for_coupon = for_coupon;
    }

    public Float getDiscount() {
        return discount;
    }

    public void setDiscount(Float discount) {
        this.discount = discount;
    }

    public Integer getInventory() {
        return inventory;
    }

    public void setInventory(Integer inventory) {
        this.inventory = inventory;
    }

    public String getOpen_time() {
        return open_time;
    }

    public void setOpen_time(String open_time) {
        this.open_time = open_time;
    }

    public Integer getGroup_limit() {
        return group_limit;
    }

    public void setGroup_limit(Integer group_limit) {
        this.group_limit = group_limit;
    }

    public Integer getGroup_buy() {
        return group_buy;
    }

    public void setGroup_buy(Integer group_buy) {
        this.group_buy = group_buy;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getVuuid() {
        return vuuid;
    }

    public void setVuuid(String vuuid) {
        this.vuuid = vuuid;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
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

    public Float getGroup_price() {
        return group_price;
    }

    public void setGroup_price(Float group_price) {
        this.group_price = group_price;
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

    public String getFood_image() {
        return food_image;
    }

    public void setFood_image(String food_image) {
        this.food_image = food_image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
}