package com.unic_1.hereim.Model;

import com.unic_1.hereim.Constants.Constant;

/**
 * Created by unic-1 on 15/9/17.
 */

public class UserRequestReference {
    public Constant.Actions action;
    public String request_reference;

    public UserRequestReference(Constant.Actions action, String request_reference) {
        this.action = action;
        this.request_reference = request_reference;
    }

    public Constant.Actions getAction() {
        return action;
    }

    public void setAction(Constant.Actions action) {
        this.action = action;
    }

    public String getRequest_reference() {
        return request_reference;
    }

    public void setRequest_reference(String request_reference) {
        this.request_reference = request_reference;
    }
}
