package com.unic_1.hereim.Constants;

import com.unic_1.hereim.Model.NotificationModel;

import java.util.ArrayList;

/**
 * Created by unic-1 on 25/8/17.
 */

public class Constant {

    // Types of actions in a request
    public enum Actions {
        REQUEST_SENT(0),
        REQUEST_RECEIVED(1),
        LOCATION_RECEIVED(2),
        REQEUST_DECLINED(3),
        LOCATION_SENT(4);

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
}
