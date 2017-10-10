package com.unic_1.hereim.Model;

/**
 * Created by unic-1 on 15/9/17.
 */

public class UserRequestReference {
    public int action;
    public String request_reference;

    public UserRequestReference(int action, String request_reference) {
        this.action = action;
        this.request_reference = request_reference;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getRequest_reference() {
        return request_reference;
    }

    public void setRequest_reference(String request_reference) {
        this.request_reference = request_reference;
    }
}
