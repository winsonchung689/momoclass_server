package com.xue.entity.model;


public class Lesson {
    private Float total_amount;

    private Float left_amount;

    private Integer points;

    private String student_name;

    private String create_time;

    private String id;

    private String studio;

    private String subject;

    private Float minus;

    private Float coins;

    private Float price;

    private String campus;

    private Integer is_combine;

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Integer getIs_combine() {
        return is_combine;
    }

    public void setIs_combine(Integer is_combine) {
        this.is_combine = is_combine;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Float getCoins() {
        return coins;
    }

    public void setCoins(Float coins) {
        this.coins = coins;
    }

    public Float getMinus() {
        return minus;
    }

    public void setMinus(Float minus) {
        this.minus = minus;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Float getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(Float total_amount) {
        this.total_amount = total_amount;
    }

    public Float getLeft_amount() {
        return left_amount;
    }

    public void setLeft_amount(Float left_amount) {
        this.left_amount = left_amount;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
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
		return "Schedule [total_amount=" + total_amount+ ", student_name="+ student_name + ", left_amount="+ left_amount + ", point="+ points + ", create_time="+ create_time + "]";
	}

    
}