package com.mavericks.massfortwitter.Utility;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * Created by micheal on 17/12/2017.
 */

public class AppPreference {
    Context context;
    Gson gson = new Gson();
    SharedPreferences spref;
    SharedPreferences.Editor editor;
    public AppPreference(Context context){
        this.context = context;
        spref = context.getSharedPreferences(context.getPackageName(),context.MODE_PRIVATE);
        editor = spref.edit();
    }

    public void setSignIn(boolean val){
        editor.putBoolean("SignIn", val).commit();
    }

    public boolean getSignIn(){
        return spref.getBoolean("SignIn", false);
    }
}
