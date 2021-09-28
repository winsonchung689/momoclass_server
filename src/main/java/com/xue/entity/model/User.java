package com.xue.entity.model;


public class User {
    private String student_name;

    private String class_name;

    private byte[] photo;

    private String comment;

    private String create_time;

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
		return "User [student_name=" + student_name+ ", class_name=" + class_name + ", photo=" + photo + ", comment=" + comment + ", create_time=" + create_time + "]";
	}

    
}