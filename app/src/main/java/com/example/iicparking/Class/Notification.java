package com.example.iicparking.Class;

import java.util.Date;

public class Notification {

    private String matricNo;
    private String blockCarPlate;
    private String phone;
    private String date;
    private String time;
    private String notifType;

    public Notification(String matricNo, String blockCarPlate, String phone, String date, String time, String notifType) {
        this.matricNo = matricNo;
        this.blockCarPlate = blockCarPlate;
        this.phone = phone;
        this.date = date;
        this.time = time;
        this.notifType = notifType;
    }

    public String getNotifType() {
        return notifType;
    }

    public void setNotifType(String notifType) {
        this.notifType = notifType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMatricNo() {
        return matricNo;
    }

    public void setMatricNo(String matricNo) {
        this.matricNo = matricNo;
    }

    public String getBlockCarPlate() {
        return blockCarPlate;
    }

    public void setBlockCarPlate(String blockCarPlate) {
        this.blockCarPlate = blockCarPlate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
