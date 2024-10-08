package com.xue.entity.model;


public class Leave {

    private String id;

    private String student_name;

    private String create_time;

    private String date_time;

    private String studio;

    private String duration;

    private String leave_type;

    private String mark_leave;

    private String subject;

    private String campus;

    private String makeup_date;

    private Integer ending_status;

    private Integer status;


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getEnding_status() {
        return ending_status;
    }

    public void setEnding_status(Integer ending_status) {
        this.ending_status = ending_status;
    }

    public String getMakeup_date() {
        return makeup_date;
    }

    public void setMakeup_date(String makeup_date) {
        this.makeup_date = makeup_date;
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

    public String getMark_leave() {
        return mark_leave;
    }

    public void setMark_leave(String mark_leave) {
        this.mark_leave = mark_leave;
    }

    public String getLeave_type() {
        return leave_type;
    }

    public void setLeave_type(String leave_type) {
        this.leave_type = leave_type;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
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
		return "Leave [student_name="+ student_name + " create_time="+ create_time + "]";
	}

    
}