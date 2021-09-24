package com.xue.entity.model;

public class User {
    private String student_name;

    private String photo;

    private String comment;

    public String getStudent_name() {
        return student_name;
    }

    public void setStudent_name(String student_name) {
        this.student_name = student_name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
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
		return "User [student_name=" + student_name + ", photo=" + photo + ", comment=" + comment + "]";
	}
    
    
}