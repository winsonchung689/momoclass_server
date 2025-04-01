package com.xue.entity.model;


public class Menu {

    private String id;

    private String restaurant;

    private String food_name;

    private String food_image;

    private String category;

    private Float price;

    private Float group_price;

    private String create_time;

    private String introduce;

    private String vuuid;

    private String unit;

    private Integer group_buy;



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