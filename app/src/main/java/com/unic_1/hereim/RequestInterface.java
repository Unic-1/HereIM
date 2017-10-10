package com.unic_1.hereim;

import com.unic_1.hereim.Model.LocationCoordinates;
import com.unic_1.hereim.Model.Request;

import java.util.ArrayList;

/**
 * Created by unic-1 on 15/9/17.
 */

public interface RequestInterface {
    public void addData(Request req, String to, String from);
    public ArrayList<Request> getData(String number);
    public void updateRequest(String to, String from, String requestid, int action, LocationCoordinates locationCoordinates);
    public void declineReqest(String requestId, String to, String from);
}
