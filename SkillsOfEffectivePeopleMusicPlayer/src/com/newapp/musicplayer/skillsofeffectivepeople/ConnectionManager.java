package com.newapp.musicplayer.skillsofeffectivepeople;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created with IntelliJ IDEA.
 * User: VOIN
 * Date: 28.10.13
 * Time: 7:59
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionManager {

    public static boolean isOnline(Context ctx) {
        ConnectivityManager cm =
                (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

}
