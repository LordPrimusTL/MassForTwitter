package com.mavericks.massfortwitter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Method;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mavericks.massfortwitter.Models.Followers;
import com.mavericks.massfortwitter.Utility.AppPreference;
import com.mavericks.massfortwitter.Utility.HMAC;
import com.mavericks.massfortwitter.Utility.HmacShaEncrypt;
import com.mavericks.massfortwitter.http.Api;
import com.mavericks.massfortwitter.http.ApiClient;
import com.mavericks.massfortwitter.http.RetrofitClient;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.StatusesService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;


    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    String nonce;
    long timestamp;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    @BindView(R.id.email_sign_in_button)Button mEmailSignInButton;
    @BindView(R.id.login_button)TwitterLoginButton loginButton;
    String TAG = "LoginActivity";
    AppPreference appPreference;
    ApiClient apiClient;
    TwitterSession session;
    RetrofitClient retrofit;
    String consumerKey = "KarGWMrkBvCfpWFGCS1AiwhLA";
    String consumerSecret = "DFcne3mjw1p0ZB0BRy5ZD57m5W7LOGSF0rXmtZBb4YXMXXYYlB";
    String signature;
    String access_token = "1687782750-OpajtQ94dojTlRjTt5Zgf3AjkpK4ERptkr3lhjM";
    String access_token_secret = "oi06pgv55nThBRnkNgFj0vka1lQWiv2LesfAliGHUwC6h";
    String BASE_URL = "https://api.twitter.com/1.1/";
    String HTTP_METHOD = "GET";
    String oauth_signature_method = "HMAC-SHA1";
    String auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        appPreference = new AppPreference(this);
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig("CONSUMER_KEY", "CONSUMER_SECRET"))
                .debug(true)
                .build();
        Twitter.initialize(config);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        // mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getTwitterFriend();
            }
        });

        if(appPreference.getSignIn()){
            //loginButton.setVisibility(View.INVISIBLE);
            getData();
        }else{
        }

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.e(TAG, "success: " + result.data);
                appPreference.setSignIn(true);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.e(TAG, "failure: " + exception);
            }
        });

    }

    private void getData(){
        session = TwitterCore.getInstance().getSessionManager().getActiveSession();
        TwitterAuthToken authToken = session.getAuthToken();
        String token = authToken.token;
        String secret = authToken.secret;
        Log.e(TAG, "getData: " + token + " " + secret);
        getTwitterFriend();
    }



    private void getFriends(){
        apiClient = new ApiClient(session);
//        apiClient.getAddedService().getFriends().subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
//        .subscribe(new Subscriber<List<User>>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Log.e(TAG, "onError: " + e);
//            }
//
//            @Override
//            public void onNext(List<User> users) {
//                Log.e(TAG, "onNext: User" + users);
//            }
//        });
    }

    private void getRetroFriends(){
//        retrofit = new RetrofitClient(this, session);
//        retrofit.getApiService().getFriends().subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<List<User>>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.e(TAG, "onError: " + e);
//
//                    }
//
//                    @Override
//                    public void onNext(List<User> users) {
//                        Log.e(TAG, "onNext: " + users);
//                    }
//                });
    }
    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }


    public void calculateAuthorization(){
        //Timespamp
        timestamp = System.currentTimeMillis() / 1000L;
        nonce = UUID.randomUUID().toString();
        nonce = nonce.replace("-","");
        try {
            nonce = URLEncoder.encode(Base64.encodeToString(nonce.getBytes(), Base64.NO_WRAP), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String parameterString = "oauth_consumer_key=\""+consumerKey.trim() +"\"&oauth_nonce=\"" + nonce.trim()+ "\"&oauth_signature_method=HMAC-SHA1&oauth_timestamp=\"" + timestamp + "\"&oauth_token=\"" + access_token.trim() + "\"&oauth_version=1.0";
        //String parameterString = "POST&https%3A%2F%2Fapi.twitter.com%2F1.1%2Fstatuses%2Fupdate.json&include_entities%3Dtrue%26oauth_consumer_key%3Dxvz1evFS4wEEPTGEFPHBog%26oauth_nonce%3DkYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1318622958%26oauth_token%3D370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb%26oauth_version%3D1.0%26status%3DHello%2520Ladies%2520%252B%2520Gentlemen%252C%2520a%2520signed%2520OAuth%2520request%2521";
        String encoded = null;
        HTTP_METHOD = HTTP_METHOD.toUpperCase();

        try {
            encoded = URLEncoder.encode(BASE_URL + "followers/ids.json","UTF-8");
            //encoded = URLEncoder.encode(BASE_URL + "followers/ids.json","UTF-8");
            signature = HTTP_METHOD + "&"+ encoded +"&" + URLEncoder.encode(parameterString, "UTF-8");
            Log.e(TAG, "calculateAuthorization: "+signature);
            Log.e(TAG, "calculateAuthorization: SignEnco" + signature);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String signKey = null;
        try {
            signKey= URLEncoder.encode(consumerSecret, "utf-8") + "&" + URLEncoder.encode(access_token_secret,"utf-8");
            Log.e(TAG, "calculateAuthorization: signinkey"+signKey );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String data = null;
        try {
            //signature = URLEncoder.encode(HmacShaEncrypt.calculateRFC2104HMAC(signature,signKey), "utf-8");
            signature = URLEncoder.encode(hmac_sha1(signature,signKey), "utf-8");
            //signature = "1ed4e409117d19ff84de5de98b445dc57432f861";
        } catch (Exception e) {
            e.printStackTrace();
        }

        //generate Auth
        //auth = "OAuth oauth_consumer_key=\""+consumerKey.trim()+"\",oauth_token=\"" + access_token.trim() + "\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\""+timestamp+"\",oauth_nonce=\"" + nonce.trim() + "\",oauth_version=\"1.0\",oauth_signature=\"" + signature.trim() + "\"";
        auth = "OAuth oauth_consumer_key=\"KarGWMrkBvCfpWFGCS1AiwhLA\",oauth_token=\"1687782750-OpajtQ94dojTlRjTt5Zgf3AjkpK4ERptkr3lhjM\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\"1514344277\",oauth_nonce=\"NzAwODI4MDBkMDM0NDRhNzg0NTdjMzc2NGY4MGE3OWE%3D\",oauth_version=\"1.0\",oauth_signature=\"yQOQ6IzAifVDZh0ZfECC0aoNgDA%3D\"";
        Log.e(TAG, "calculateAuthorization: " + timestamp);
        Log.e(TAG, "calculateAuthorization: Cons" + consumerKey);
        Log.e(TAG, "calculateAuthorization: nonce" + nonce);
        Log.e(TAG, "calculateAuthorization: Sign" + signature);
        Log.e(TAG, "calculateAuthorization: Enco" + encoded );
        Log.e(TAG, "calculateAuthorization: sign" + signKey);
        Log.e(TAG, "calculateAuthorization: Encry" + signature);
        Log.e(TAG, "calculateAuthorization: Auth " + auth);
        //OAuth oauth_consumer_key="KarGWMrkBvCfpWFGCS1AiwhLA",oauth_token="1687782750-OpajtQ94dojTlRjTt5Zgf3AjkpK4ERptkr3lhjM",oauth_signature_method="HMAC-SHA1",oauth_timestamp="1514158719",oauth_nonce="NzAwODI4MDBkMDM0NDRhNzg0NTdjMzc2NGY4MGE3OWE%3D",oauth_version="1.0",oauth_signature="4%2FxqBwj7tob3ihwK7kP8lC68P1A%3D"

    }


    private void getTwitterFriend(){

        calculateAuthorization();
//        try{
//            apiClient = new ApiClient(session);
//            apiClient.getAddedService().getFriends(auth).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<String>() {
//                @Override
//                public void onCompleted() {
//
//                }
//
//                @Override
//                public void onError(Throwable e) {
//                    if(e instanceof TwitterException){
//                        Log.e(TAG, "onError: " + e);
//                    }else{
//                        Log.e(TAG, "onErrr: " + e);
//                    }
//                }
//
//                @Override
//                public void onNext(String s) {
//                    Log.e(TAG, "onNext: " + s);
//                }
//            });
//        }catch (Exception ex)
//        {
//            Log.e(TAG, "getTwitterFriend: " + ex);
//        }

//        retrofit = new RetrofitClient(this,RetrofitClient.DEFAULT_HOST);
//        retrofit.getApiService().getFriends(auth).subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Result>() {
//            @Override
//            public void onCompleted() {
//                Log.e(TAG, "onCompleted: ");
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Log.e(TAG, "onError: +"+ e );
//                Log.e(TAG, "onError: +"+ e.getMessage() );
//                Log.e(TAG, "onError: +"+ e.getSuppressed() );
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onNext(Result responseBody) {
//                Log.e(TAG, "onNext: +"+ responseBody.response);
//                Log.e(TAG, "onNext: +"+ new Gson().toJson(responseBody));
//                Log.e(TAG, "onNext: +"+ responseBody.data);
//                Log.e(TAG, "onNext: +"+ responseBody);
//            }
//        });


       try{
           retrofit = new RetrofitClient(this,RetrofitClient.DEFAULT_HOST);
           retrofit.getApiService().getFriends(auth).enqueue(new Callback<Result>() {
               @Override
               public void success(Result<Result> result) {
                   Log.e(TAG, "success: "+new Gson().toJson(result));
                   Log.e(TAG, "success: "+new Gson().toJson(result.response.headers()));
                   Log.e(TAG, "success: "+new Gson().toJson(result.response.raw()));
               }

               @Override
               public void failure(TwitterException exception) {
                   exception.printStackTrace();
                   Log.e(TAG, "failure: " + exception);
               }
           });

       }catch (Exception ex){
           Log.e(TAG, "getTwitterFriend: vretrofit" + ex);
       }

       try{
           AndroidNetworking.get("https://api.twitter.com/1.1/followers/list.json")
                   .addPathParameter("pageNumber", "0")
                   .addQueryParameter("limit", "3")
                   .setPriority(Priority.HIGH)
                   .addHeaders("Authorization", auth)
                   .build()
                   .getAsJSONArray(new JSONArrayRequestListener() {
                       @Override
                       public void onResponse(JSONArray response) {
                           Log.e(TAG, "onResponse: " + response);
                       }
                       @Override
                       public void onError(ANError error) {
                           // handle error
                           Log.e(TAG, "onError: " + error.getErrorBody());
                           Log.e(TAG, "onError: " + error.getErrorDetail());
                           Log.e(TAG, "onError: " + error.getErrorCode());
                       }
                   });


       }catch (Exception ex){
           Log.e(TAG, "getTwitterFriend: FAN" + ex);
       }


       try{
           String url = "https://api.twitter.com/1.1/followers/list.json";
           JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET,
                   url, null,
                   new Response.Listener<JSONObject>() {

                       @Override
                       public void onResponse(JSONObject response) {
                           Log.d(TAG, response.toString());
                       }
                   }, new Response.ErrorListener() {
               @Override
               public void onErrorResponse(VolleyError error) {
                   Log.e(TAG, "onErrorResponse: " + error.getMessage());
               }

           }){
               @Override
               public Map<String, String> getHeaders() throws AuthFailureError {
                   HashMap<String, String> headers = new HashMap<>();
                   headers.put("Content-Type", "application/json");
                   headers.put("Authorization", auth);
                   return headers;
               }
           };

           Volley.newRequestQueue(this).add(jsonObjReq);
       }catch (Exception ex){
           Log.e(TAG, "getTwitterFriend: volley" + ex);
       }


    }

    private String hmac_sha1(String Base, String Key){

        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(Key.getBytes("UTF-8"), "HmacSHA1");
            mac.init(secret);
            byte[] digest = mac.doFinal(Base.getBytes("UTF-8"));

            //Log.i("hex",bytesToHex(digest));

            return Base64.encodeToString(digest, Base64.NO_WRAP);

            //return new String(Base64.encode(digest,Base64.NO_WRAP));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return "";
    }
}

