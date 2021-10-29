package com.xue.entity.model;


public class User {
    private String nick_name;

    private String role;

    private String create_time;


    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }



	@Override
	public String toString() {
		return "User [nick_name=" + nick_name+ ", role="+ role + ", create_time=" + create_time + "]";
	}

    
}