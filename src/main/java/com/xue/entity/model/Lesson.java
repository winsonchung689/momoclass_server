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

    private Float total_money;

    private Float discount_money;

    private String campus;

    private Integer is_combine;

    private Integer delete_status;

    private String final_time;

    private Float leave_times;

    private String age;

    private String related_id;

    private String uuid;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRelated_id() {
        return related_id;
    }

    public void setRelated_id(String related_id) {
        this.related_id = related_id;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public Integer getDelete_status() {
        return delete_status;
    }

    public void setDelete_status(Integer delete_status) {
        this.delete_status = delete_status;
    }

    public Float getLeave_times() {
        return leave_times;
    }

    public void setLeave_times(Float leave_times) {
        this.leave_times = leave_times;
    }

    public String getFinal_time() {
        return final_time;
    }

    public void setFinal_time(String final_time) {
        this.final_time = final_time;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Float getTotal_money() {
        return total_money;
    }

    public void setTotal_money(Float total_money) {
        this.total_money = total_money;
    }

    public Float getDiscount_money() {
        return discount_money;
    }

    public void setDiscount_money(Float discount_money) {
        this.discount_money = discount_money;
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