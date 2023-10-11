package com.example.iicparking.Class;

import java.util.Date;

public class Notification {
    private String title;
    private String date;
    private String description;

    //Constructor
    public Notification(String title, String date, String desc){
        this.title = title;
        this.date = date;
        this.description = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
