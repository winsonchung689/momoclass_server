package com.xue.entity.model;


public class ShippingFee {

    private String id;

    private String region;

    private Integer first_weight_1;

    private Integer first_weight_2;

    private Integer first_weight_5;

    private Integer additional_weight;

    private Integer preservation_fee;

    private String create_time;

    private String restaurant;

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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getFirst_weight_1() {
        return first_weight_1;
    }

    public void setFirst_weight_1(Integer first_weight_1) {
        this.first_weight_1 = first_weight_1;
    }

    public Integer getFirst_weight_2() {
        return first_weight_2;
    }

    public void setFirst_weight_2(Integer first_weight_2) {
        this.first_weight_2 = first_weight_2;
    }

    public Integer getFirst_weight_5() {
        return first_weight_5;
    }

    public void setFirst_weight_5(Integer first_weight_5) {
        this.first_weight_5 = first_weight_5;
    }

    public Integer getAdditional_weight() {
        return additional_weight;
    }

    public void setAdditional_weight(Integer additional_weight) {
        this.additional_weight = additional_weight;
    }

    public Integer getPreservation_fee() {
        return preservation_fee;
    }

    public void setPreservation_fee(Integer preservation_fee) {
        this.preservation_fee = preservation_fee;
    }

    
}