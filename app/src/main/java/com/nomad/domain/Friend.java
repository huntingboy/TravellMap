package com.nomad.domain;

/**
 * Created by nomad on 18-4-8.
 * 好友bean
 */

public class Friend {
    private int id;
    private String username;
    private String friend;

    public Friend(){}
    public Friend(String username, String friend) {
        this.username = username;
        this.friend = friend;
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
}
