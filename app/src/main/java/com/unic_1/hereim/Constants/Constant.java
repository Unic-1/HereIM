package com.unic_1.hereim.Constants;

import com.unic_1.hereim.Model.NotificationModel;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by unic-1 on 25/8/17.
 */

public class Constant {

    // Types of actions in a request
    public enum Actions {
        REQUEST_SENT(0),
        REQUEST_RECEIVED(1),
        LOCATION_RECEIVED(3),
        REQEUST_DECLINED(4),
        LOCATION_SENT(2);

        public int value;
        private Actions(int value) {
            this.value = value;
        }
    }


    public static ArrayList<NotificationModel> getList() {
        ArrayList<NotificationModel> notificationList = new ArrayList<>();

        notificationList.add(new NotificationModel(
                "Hey Manoj! where are you?",
                "Satyam"
        ));
        notificationList.add(new NotificationModel(
                "Hey Satyadeep! where are you?",
                "Amit"
        ));
        notificationList.add(new NotificationModel(
                "Hey Satyadeep! where are you?",
                "Satyam"
        ));

        return notificationList;
    }

    private HashMap<String, String> countryCodes = new HashMap<>();


}
