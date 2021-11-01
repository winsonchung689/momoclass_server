package com.xue.entity.model;


public class User {
    private String nick_name;

    private String student_name;

    private String role;

    private String openid;

    private String create_time;

    private String avatarurl;


    public String getAvatarurl() {
        return avatarurl;
    }

    public void setAvatarurl(String avatarurl) {
        this.avatarurl = avatarurl;
    }


    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public String getStudent_name() {
        return student_name;
    }

    public void setStudent_name(String student_name) {
        this.student_name = student_name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }


    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }



	@Override
	public String toString() {
		return "User [nick_name=" + nick_name+ ", role="+ role + ", create_time=" + create_time + ", avatarurl=" + avatarurl + ", openid=" + openid + ", student_name=" + student_name +  "]";
	}

    
}