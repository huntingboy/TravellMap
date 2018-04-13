package com.nomad.domain;

import java.util.Date;

/**
 * Created by nomad on 18-4-10.
 * 签到表 用户名，经纬度，地址，时间，用户输入描述
 */

public class Checkin {
    private int id;
    private String username;
    private double latitude;
    private double longitude;
    private String address;
    private Date dateTime;
    private String description;

    public Checkin(String username, double latitude, double longitude,
                   String address, Date dateTime, String description) {
        super();
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.dateTime = dateTime;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }
}
