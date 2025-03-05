package com.xue.entity.model;


public class Announcement {
    private String id;

    private String studio;

    private String create_time;

    private String content;

    private String campus;

    private String title;

    private String uuid;



    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }


    
}