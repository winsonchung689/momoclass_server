package com.xue.entity.model;


public class SignUp {

    private String student_name;

    private String create_time;

    private String studio;

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
		return "SignUp [student_name="+ student_name + " create_time="+ create_time + "]";
	}

    
}