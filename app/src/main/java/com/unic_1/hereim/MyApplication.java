package com.unic_1.hereim;

import android.app.Application;

/**
 * Created by unic-1 on 29/11/17.
 */

public class MyApplication extends Application {

    public static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.sConnectivityReceiverListener = listener;
    }
}
