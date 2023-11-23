package com.example.alertify_main_admin.main_utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;

public class NetworkUtils {

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            } else {
                // For older versions of Android
                return cm.getActiveNetworkInfo() != null &&
                        (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI ||
                                cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE);
            }
        }

        return false;
    }
}

