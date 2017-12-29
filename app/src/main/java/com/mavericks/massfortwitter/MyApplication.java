package com.mavericks.massfortwitter;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;
import com.twitter.sdk.android.core.Twitter;

/**
 * Created by micheal on 17/12/2017.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Twitter.initialize(this);
        AndroidNetworking.initialize(this);
    }
}
