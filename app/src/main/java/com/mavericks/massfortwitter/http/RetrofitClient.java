package com.mavericks.massfortwitter.http;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mavericks.massfortwitter.BuildConfig;
import com.mavericks.massfortwitter.LoginActivity;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by micheal on 17/12/2017.
 */

public class RetrofitClient  extends TwitterApiClient{
    private Context context;
    private Api apiService = null;
    public static String DEFAULT_HOST = "https://api.twitter.com/1.1/";
    private static RetrofitClient retrofitClient = null;
    public static RetrofitClient getInstance(Context ctx, String url) {
        if (retrofitClient == null) {
            retrofitClient = new RetrofitClient(ctx, url);

        }
        return retrofitClient;
    }

    public RetrofitClient(final Context ctx, String url) {
        context = ctx;
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();




        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG)
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        else
            interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        OkHttpClient client = new OkHttpClient.Builder().addNetworkInterceptor(interceptor).build();

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();

                // Request customization: add request headers
                Request.Builder requestBuilder = original.newBuilder()
                        .addHeader("Cache-Control", "no-cache")
                        .addHeader("Cache-Control", "no-store");

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        httpClient.addNetworkInterceptor(interceptor);
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .baseUrl(url)
                .build();

        apiService = retrofit.create(Api.class);
    }

    public Api getApiService() {
        return apiService;
    }
}
