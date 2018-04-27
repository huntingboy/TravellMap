package com.nomad.domain;

import java.util.Date;

/**
 * Created by nomad on 18-4-8.
 * 好友bean
 */

public class Friend {
    private int id;
    private String username;
    private String friend;
    private double latitude; //好友最后一次登录的地点
    private double longitude;
    private Date dateTime;

    public Friend(){}
    public Friend(String username, String friend, double latitude, double longitude, Date dateTime) {
        this.username = username;
        this.friend = friend;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateTime = dateTime;
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

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
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

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }
}
