package com.xue.entity.model;


public class AnalyzeCount {

    private String create_time;

    private Float sign_count;

    private Float try_count;

    private Float leave_count;

    private Float lesson_count;



    public Float getSign_count() {
        return sign_count;
    }

    public void setSign_count(Float sign_count) {
        this.sign_count = sign_count;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public Float getTry_count() {
        return try_count;
    }

    public void setTry_count(Float try_count) {
        this.try_count = try_count;
    }

    public Float getLeave_count() {
        return leave_count;
    }

    public void setLeave_count(Float leave_count) {
        this.leave_count = leave_count;
    }

    public Float getLesson_count() {
        return lesson_count;
    }

    public void setLesson_count(Float lesson_count) {
        this.lesson_count = lesson_count;
    }

    
}