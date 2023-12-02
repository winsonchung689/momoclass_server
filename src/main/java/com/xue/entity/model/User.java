package com.xue.entity.model;


public class User {
    private String nick_name;

    private String student_name;

    private String role;

    private String openid;

    private String create_time;

    private String expired_time;

    private String expired_time_ad;

    private String avatarurl;

    private String studio;

    private String user_type;

    private String comment_style;

    private Float coins;

    private Float read_times;

    private String theme;

    private Integer display;

    private Integer days;

    private Integer cover;

    private Integer is_open;

    private String phone_number;

    private String location;

    private String lessons;

    private String subjects;

    private String send_time;

    private String id;

    private String subscription;

    private String member;

    private String campus;

    private String back_uuid;

    private String unionid;

    private String official_openid;

    private String remind_type;

    private Integer hours;

    private String send_status;



    public String getSend_status() {
        return send_status;
    }

    public void setSend_status(String send_status) {
        this.send_status = send_status;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public String getRemind_type() {
        return remind_type;
    }

    public void setRemind_type(String remind_type) {
        this.remind_type = remind_type;
    }

    public String getOfficial_openid() {
        return official_openid;
    }

    public void setOfficial_openid(String official_openid) {
        this.official_openid = official_openid;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public String getBack_uuid() {
        return back_uuid;
    }

    public void setBack_uuid(String back_uuid) {
        this.back_uuid = back_uuid;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }


    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSend_time() {
        return send_time;
    }

    public void setSend_time(String send_time) {
        this.send_time = send_time;
    }

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

    public Integer getIs_open() {
        return is_open;
    }

    public void setIs_open(Integer is_open) {
        this.is_open = is_open;
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

    public Float getCoins() {
        return coins;
    }

    public void setCoins(Float coins) {
        this.coins = coins;
    }

    public Float getRead_times() {
        return read_times;
    }

    public void setRead_times(Float read_times) {
        this.read_times = read_times;
    }

    public String getExpired_time() {
        return expired_time;
    }

    public void setExpired_time(String expired_time) {
        this.expired_time = expired_time;
    }

    public String getExpired_time_ad() {
        return expired_time_ad;
    }

    public void setExpired_time_ad(String expired_time_ad) {
        this.expired_time_ad = expired_time_ad;
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