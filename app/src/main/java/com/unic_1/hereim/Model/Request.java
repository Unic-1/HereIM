package com.unic_1.hereim.Model;

import com.unic_1.hereim.Constants.Constant;

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
    private Constant.Actions action;
    public long timestamp;
    public String to;
    public String from;
    public LocationCoordinates location;



    public Request(Constant.Actions action, long timestamp, String to, String from) {
        this.action = action;
        this.timestamp = timestamp;
        this.to = to;
        this.from = from;
    }

    // For request sent and request received
    public Request(long timestamp, String to, String from) {
        this.timestamp = timestamp;
        this.to = to;
        this.from = from;
    }

    // For location sent and location received
    public Request(long timestamp, String to, String from, LocationCoordinates location) {
        this.timestamp = timestamp;
        this.to = to;
        this.from = from;
        this.location = location;
    }

    public Request(Constant.Actions action, long timestamp, String to, String from, LocationCoordinates location) {
        this.action = action;
        this.timestamp = timestamp;
        this.to = to;
        this.from = from;
        this.location = location;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public LocationCoordinates getLocation() {
        return location;
    }

    public void setLocation(LocationCoordinates location) {
        this.location = location;
    }

    public Constant.Actions getAction() {
        return action;
    }

    public void setAction(Constant.Actions action) {
        this.action = action;
    }
}
