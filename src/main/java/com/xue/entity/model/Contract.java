package com.xue.entity.model;


import com.alibaba.fastjson.JSONObject;

import javax.xml.soap.Text;

public class Contract {

    private String id;

    private String studio;

    private String campus;

    private Text contract;

    private String create_time;


    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Text getContract() {
        return contract;
    }

    public void setContract(Text contract) {
        this.contract = contract;
    }
    
}