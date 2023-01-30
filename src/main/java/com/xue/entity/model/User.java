package com.xue.entity.model;


public class User {
    private String nick_name;

    private String student_name;

    private String role;

    private String openid;

    private String create_time;

    private String expired_time;

    private String avatarurl;

    private String studio;

    private String user_type;

    private String comment_style;

    private Integer coins;

    private String theme;

    private Integer display;

    private Integer cover;

    private String phone_number;

    private String location;

    private String lessons;

    private String subjects;

    public String getSubjects() {
        return subjects;
    }

    public void setSubjects(String subjects) {
        this.subjects = subjects;
    }

    public String getLessons() {
        return lessons;
    }

    public void setLessons(String lessons) {
        this.lessons = lessons;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public Integer getCover() {
        return cover;
    }

    public void setCover(Integer cover) {
        this.cover = cover;
    }

    public Integer getDisplay() {
        return display;
    }

    public void setDisplay(Integer display) {
        this.display = display;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getComment_style() {
        return comment_style;
    }

    public void setComment_style(String comment_style) {
        this.comment_style = comment_style;
    }

    public Integer getCoins() {
        return coins;
    }

    public void setCoins(Integer coins) {
        this.coins = coins;
    }

    public String getExpired_time() {
        return expired_time;
    }

    public void setExpired_time(String expired_time) {
        this.expired_time = expired_time;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

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