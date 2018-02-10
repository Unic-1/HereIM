package com.unic_1.hereim.Model;

/**
 * Created by unic-1 on 15/9/17.
 */

public class UserRequestReference {
    public String request_reference;
    public int action;
    public String requestID;
    public long order;

    public UserRequestReference(String request_reference, int action, String requestID) {
        this.request_reference = request_reference;
        this.action = action;
        this.requestID = requestID;
    }

    public UserRequestReference(int action, String requestID, long order) {
        this.action = action;
        this.requestID = requestID;
        this.order = order;
    }

    public String getRequest_reference() {
        return request_reference;
    }

    public int getAction() {
        return action;
    }

    public String getRequestID() {
        return requestID;
    }
}
