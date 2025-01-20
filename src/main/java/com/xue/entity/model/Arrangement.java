package com.xue.entity.model;


public class Arrangement {
    private String id;

    private String dayofweek;

    private String class_number;

    private String duration;

    private String limits;

    private String studio;

    private String subject;

    private byte[] photo;

    private String campus;

    private String upcoming;

    private Integer is_reserved;

    private Integer days;

    private Integer is_repeat;

    private String repeat_duration;

    private Integer remind;

    private Integer hours;

    private String repeat_week;

    private Integer class_type;


    public Integer getClass_type() {
        return class_type;
    }

    public void setClass_type(Integer class_type) {
        this.class_type = class_type;
    }

    public String getRepeat_week() {
        return repeat_week;
    }

    public void setRepeat_week(String repeat_week) {
        this.repeat_week = repeat_week;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public Integer getRemind() {
        return remind;
    }

    public void setRemind(Integer remind) {
        this.remind = remind;
    }

    public String getRepeat_duration() {
        return repeat_duration;
    }

    public void setRepeat_duration(String repeat_duration) {
        this.repeat_duration = repeat_duration;
    }

    public Integer getIs_repeat() {
        return is_repeat;
    }

    public void setIs_repeat(Integer is_repeat) {
        this.is_repeat = is_repeat;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public Integer getIs_reserved() {
        return is_reserved;
    }

    public void setIs_reserved(Integer is_reserved) {
        this.is_reserved = is_reserved;
    }

    public String getUpcoming() {
        return upcoming;
    }

    public void setUpcoming(String upcoming) {
        this.upcoming = upcoming;
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


    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }


    public String getDayofweek() {
        return dayofweek;
    }

    public void setDayofweek(String dayofweek) {
        this.dayofweek = dayofweek;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getClass_number() {
        return class_number;
    }

    public void setClass_number(String class_number) {
        this.class_number = class_number;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLimits() {
        return limits;
    }

    public void setLimits(String limits) {
        this.limits = limits;
    }


	@Override
	public String toString() {
		return "SignUp [dayofweek="+ dayofweek + " limits="+ limits + "]";
	}

    
}