package com.mavericks.massfortwitter.http;

import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by micheal on 17/12/2017.
 */

public class ApiClient extends TwitterApiClient {
    public  ApiClient(TwitterSession session){
        super(session);
    }

    public Api getAddedService(){
        return getService(Api.class);
    }
}

