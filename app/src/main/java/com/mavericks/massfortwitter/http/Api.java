package com.mavericks.massfortwitter.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mavericks.massfortwitter.Models.Followers;
import com.mopub.common.util.Json;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONObject;

import java.util.List;

import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import rx.Observable;

public interface Api{
    @GET("followers/list.json")
    Call<Result> getFriends(@Header("Authorization") String auth);
    //Observable<List<User>> getFriends();
}
