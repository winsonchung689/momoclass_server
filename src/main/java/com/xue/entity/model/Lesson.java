package com.xue.entity.model;


import org.omg.PortableInterceptor.INACTIVE;

public class Lesson {
    private Integer total_amount;

    private Integer left_amount;

    private Integer points;

    private String student_name;

    private String create_time;

    private String id;

    private String studio;

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

    public Integer getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(Integer total_amount) {
        this.total_amount = total_amount;
    }

    public Integer getLeft_amount() {
        return left_amount;
    }

    public void setLeft_amount(Integer left_amount) {
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