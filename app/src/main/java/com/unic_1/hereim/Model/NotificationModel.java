package com.unic_1.hereim.Model;

/**
 * Created by unic-1 on 25/8/17.
 */

public class NotificationModel {
    String message;
    String sender;

    public NotificationModel(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
