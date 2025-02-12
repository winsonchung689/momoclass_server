package com.xue.entity.model;


public class AnalyzeCount {

    private String create_time;

    private String student_name;

    private Float sign_count;

    private Float try_count;

    private Float leave_count;

    private Float lesson_count;

    private Float package_count;

    private Float package_sum_l;

    private Float package_sum_m;



    public Float getPackage_sum_m() {
        return package_sum_m;
    }

    public void setPackage_sum_m(Float package_sum_m) {
        this.package_sum_m = package_sum_m;
    }

    public Float getPackage_sum_l() {
        return package_sum_l;
    }

    public void setPackage_sum_l(Float package_sum_l) {
        this.package_sum_l = package_sum_l;
    }

    public Float getPackage_count() {
        return package_count;
    }

    public void setPackage_count(Float package_count) {
        this.package_count = package_count;
    }

    public String getStudent_name() {
        return student_name;
    }

    public void setStudent_name(String student_name) {
        this.student_name = student_name;
    }

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