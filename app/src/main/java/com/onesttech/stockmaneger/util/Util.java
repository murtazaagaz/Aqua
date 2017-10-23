package com.onesttech.stockmaneger.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.onesttech.stockmaneger.infrastructure.MyApplication;

/**
 * Created by Murtaza on 8/20/2017.
 */

public class Util {
    public static boolean isNetworkAvailable(){
        ConnectivityManager manager = (ConnectivityManager) MyApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable()
                && networkInfo.isConnected();
    }
}

