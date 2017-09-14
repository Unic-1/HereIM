package com.unic_1.hereim.Model;

/**
 * Created by unic-1 on 7/9/17.
 *
 * Action values:
 * 0 - Request sent, No response
 * 1 - Request received
 * 2 - Location received
 * 3 - Request declined
 * 4 - Location sent
 */



public class Request {
    public int action;
    public long timestamp;
    public String number;

    public Request(int action, long timestamp, String number) {
        this.action = action;
        this.timestamp = timestamp;
        this.number = number;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
