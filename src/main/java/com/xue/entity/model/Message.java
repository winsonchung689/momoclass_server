package com.xue.entity.model;


import javax.persistence.criteria.CriteriaBuilder;

public class Message {
    private String student_name;

    private String class_name;

    private byte[] photo;

    private String comment;

    private String create_time;

    private String class_target;

    private String class_target_bak;

    private String id;

    private String studio;

    private String mp3_url;

    private String duration;

    private String positive;

    private String discipline;

    private String happiness;

    private String uuids;

    private String uuids_c;

    private String campus;

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getUuids_c() {
        return uuids_c;
    }

    public void setUuids_c(String uuids_c) {
        this.uuids_c = uuids_c;
    }

    public String getUuids() {
        return uuids;
    }

    public void setUuids(String uuids) {
        this.uuids = uuids;
    }

    public String getMp3_url() {
        return mp3_url;
    }

    public void setMp3_url(String mp3_url) {
        this.mp3_url = mp3_url;
    }

    public String getPositive() {
        return positive;
    }

    public void setPositive(String positive) {
        this.positive = positive;
    }

    public String getDiscipline() {
        return discipline;
    }

    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }

    public String getHappiness() {
        return happiness;
    }

    public void setHappiness(String happiness) {
        this.happiness = happiness;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
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

    public String getClass_target() {
        return class_target;
    }

    public void setClass_target(String class_target) {
        this.class_target = class_target;
    }

    public String getClass_target_bak() {
        return class_target_bak;
    }

    public void setClass_target_bak(String class_target_bak) {
        this.class_target_bak = class_target_bak;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getStudent_name() {
        return student_name;
    }

    public void setStudent_name(String student_name) {
        this.student_name = student_name;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment ;
    }

	@Override
	public String toString() {
		return "User [student_name=" + student_name+ ", id="+ id + ", class_name="+ student_name+ ", class_target=" + class_target + ", photo=" + photo + ", comment=" + comment + ", create_time=" + create_time + "]";
	}

    
}