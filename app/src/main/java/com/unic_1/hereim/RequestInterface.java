package com.unic_1.hereim;

import android.content.Context;

import com.unic_1.hereim.Model.LocationCoordinates;
import com.unic_1.hereim.Model.Request;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by unic-1 on 15/9/17.
 */

public interface RequestInterface {
    void addData(Request req, String to, String from, int requestId);
    ArrayList<Request> getData(String number);
    void updateRequest(String to, String from, String requestid, int action, LocationCoordinates locationCoordinates);
    void isPresent(HashMap<String, ArrayList<String>> contactMap, Context context);
}
