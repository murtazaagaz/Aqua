package com.onesttech.stockmaneger.storage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Murtaza on 8/20/2017.
 */

public class MySharedPrefrences{

    public static SharedPreferences mSharedPrefrences;
    public static MySharedPrefrences mInstance;
    public static Context mContext;

    private String SHARED_PREF_NAME = "tac_seller";
    private String DEALER_NAME = "dealer_name";
    private String DEALER_STORE = "store_name";
    private String DEALER_UID = "dealer_uid";
    private String DEALER_EMAIL = "dealer_email";
    private String DEFAULT = null;

    public MySharedPrefrences(){
        mSharedPrefrences = mContext.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
    }
    public static MySharedPrefrences getInstance(Context context){
        mContext = context;
        if (mInstance == null){
            mInstance = new MySharedPrefrences();
        }
        return mInstance;
    }
    public void setKey (String key,String value) {
        mSharedPrefrences.edit().putString(key,value).apply();
    }
    public String getKey(String key){
        return mSharedPrefrences.getString(key,DEFAULT);
    }
    public void setStep (String key,String value) {
        mSharedPrefrences.edit().putString(key,value).apply();
    }
    public String getStep(String key){
        return mSharedPrefrences.getString(key,"step1");
    }
    public void setBoolean(String key, boolean value){
        mSharedPrefrences.edit().putBoolean(key,value).apply();
    }
    public boolean getBoolean(String key){
        return mSharedPrefrences.getBoolean(key,false);
    }
}
