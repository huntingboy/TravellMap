package com.nomad.domain;

import java.util.Date;

/**
 * Created by nomad on 18-4-20.
 * 评论表：id 经纬度 city address username description time approve disapprove
 */

public class CommentAround {
    private int id;
    private double latitude;
    private double longitude;
    private String city;
    private String address;
    private String username;
    private Date dateTime;  //数据库使用的名字是  dateTime
    private String description;
    private int approve = 0;
    private int disapprove = 0;

    public CommentAround() {
    }

    public CommentAround(double latitude, double longitude, String city, String address, String username, String description, Date time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.address = address;
        this.username = username;
        this.description = description;
        this.dateTime = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getApprove() {
        return approve;
    }

    public void setApprove(int approve) {
        this.approve = approve;
    }

    public int getDisapprove() {
        return disapprove;
    }

    public void setDisapprove(int disapprove) {
        this.disapprove = disapprove;
    }
}
