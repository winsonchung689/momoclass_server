package com.xue.entity.model;


public class LessonPackage {
    private String id;

    private String student_name;

    private String studio;

    private String subject;

    private String create_time;

    private String mark;

    private String campus;

    private Float total_money;

    private Float discount_money;

    private Float all_lesson;

    private Float give_lesson;

    private String  start_date;

    private String  end_date;


    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Float getAll_lesson() {
        return all_lesson;
    }

    public void setAll_lesson(Float all_lesson) {
        this.all_lesson = all_lesson;
    }

    public Float getGive_lesson() {
        return give_lesson;
    }

    public void setGive_lesson(Float give_lesson) {
        this.give_lesson = give_lesson;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
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

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
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
		return "Note [student_name="+ student_name + ", create_time="+ create_time + "]";
	}

    
}