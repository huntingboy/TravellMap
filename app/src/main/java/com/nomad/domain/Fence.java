package com.nomad.domain;

/**
 * Created by nomad on 18-4-8.
 * 围栏bean
 */

public class Fence {
    private int id;
    private String username;
    private double latitude;
    private double longitude;
    private int radius;
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Fence(){}
    public Fence(double latitude, double longitude, int radius, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.address = address;
    }
    public Fence(String username, double latitude, double longitude, int radius, String address) {
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.address = address;
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

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
