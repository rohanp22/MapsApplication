package com.example.mapsapplication.Model;

public class FriendRequest {

    private String sender, friendid, receiver, status;

    public FriendRequest(String friendid, String sender, String receiver, String status) {
        this.sender = sender;
        this.friendid = friendid;
        this.receiver = receiver;
        this.status = status;
    }

    public String getSender() {
        return sender;
    }

    public String getFriendid() {
        return friendid;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getStatus() {
        return status;
    }
}
