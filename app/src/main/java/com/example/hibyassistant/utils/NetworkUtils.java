package com.example.hibyassistant.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

public class NetworkUtils {
    private static boolean isNetworkAvailable = true;
    private static NetworkCallback networkCallback;

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;
        
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }

    public static void registerNetworkCallback(Context context) {
        if (context == null) return;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return;

        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        NetworkRequest networkRequest = builder.build();

        networkCallback = new NetworkCallback();
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    public static void unregisterNetworkCallback(Context context) {
        if (context == null || networkCallback == null) return;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return;

        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    public static boolean getNetworkState() {
        return isNetworkAvailable;
    }

    private static class NetworkCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(Network network) {
            isNetworkAvailable = true;
        }

        @Override
        public void onLost(Network network) {
            isNetworkAvailable = false;
        }
    }
} 