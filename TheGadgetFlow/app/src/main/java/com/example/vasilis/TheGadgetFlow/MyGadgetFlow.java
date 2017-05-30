package com.example.vasilis.TheGadgetFlow;

import android.app.Application;
import android.util.Log;

import com.algolia.search.saas.Client;
import com.crashlytics.android.Crashlytics;

import Helper.Constants;
import io.fabric.sdk.android.Fabric;
import io.intercom.android.sdk.Intercom;

/**
 * Created by Vasilis Fouroulis on 28/12/2016.
 */

public class MyGadgetFlow extends Application {
    private Client client;
    private final static String TAG = "MyGadgetFlow";
    private boolean isMainActivityActivebool = false;

    @Override
    public void onCreate() {
        super.onCreate();

        client = new Client(Constants.ALGOLIA_APPLICATION_ID, Constants.ALGOLIA_APY_KEY);
        Fabric.with(this, new Crashlytics());
        Intercom.initialize(this, "android_sdk-55986579492ddf52f8ba2ed37efbcfa671ff0e84", "o8hv5ejx");

    }

    public Client getClient(){
        return client;
    }

    public boolean isMainActivityActive() {
        return isMainActivityActivebool;
    }

    public void activityMainCreated() {
        isMainActivityActivebool = true;
    }

    public void activityMainPaused() {
        isMainActivityActivebool = false;
    }
}
