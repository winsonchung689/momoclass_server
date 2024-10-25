package com.xue.entity.model;


public class Gift {

    private String id;

    private String student_name;

    private String create_time;

    private String expired_time;

    private String gift_name;

    private Integer gift_amount;

    private String studio;

    private Integer status;

    private String campus;

    private String gift_id;


    public String getGift_id() {
        return gift_id;
    }

    public void setGift_id(String gift_id) {
        this.gift_id = gift_id;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }


    public String getExpired_time() {
        return expired_time;
    }

    public void setExpired_time(String expired_time) {
        this.expired_time = expired_time;
    }

    public String getGift_name() {
        return gift_name;
    }

    public void setGift_name(String gift_name) {
        this.gift_name = gift_name;
    }

    public Integer getGift_amount() {
        return gift_amount;
    }

    public void setGift_amount(Integer gift_amount) {
        this.gift_amount = gift_amount;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public String getStudent_name() {
        return student_name;
    }

    public void setStudent_name(String student_name) {
        this.student_name = student_name;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }


	@Override
	public String toString() {
		return "SignUp [student_name="+ student_name + " create_time="+ create_time + "]";
	}

    
}