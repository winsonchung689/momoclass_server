package com.xue.entity.model;


public class Schedule {
    private String add_date;

    private String student_name;

    private String duration;

    private String create_time;


    public String getAdd_date() {
        return add_date;
    }

    public void setAdd_date(String add_date) {
        this.add_date = add_date;
    }


    public String getStudent_name() {
        return student_name;
    }

    public void setStudent_name(String student_name) {
        this.student_name = student_name;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

	@Override
	public String toString() {
		return "Schedule [add_date=" + add_date+ ", student_name="+ student_name + ", duration="+ duration + ", create_time="+ create_time + "]";
	}

    
}