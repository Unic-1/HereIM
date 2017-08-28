package com.unic_1.hereim.Constants;

import com.unic_1.hereim.Model.NotificationModel;

import java.util.ArrayList;

/**
 * Created by unic-1 on 25/8/17.
 */

public class Constant {

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
