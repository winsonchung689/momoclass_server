package com.xue.entity.model;


public class PptMenu {

    private String id;

    private String studio;

    private String campus;

    private String ppt_name;

    private String uuids;

    private String uuid;

    private String category;

    private Float price;

    private String create_time;

    private String introduce;

    private String type;

    private Integer size;



    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
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

    public String getPpt_name() {
        return ppt_name;
    }

    public void setPpt_name(String ppt_name) {
        this.ppt_name = ppt_name;
    }

    public String getUuids() {
        return uuids;
    }

    public void setUuids(String uuids) {
        this.uuids = uuids;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
}