package com.android.sun2meg.safetyapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.List;

public class MyTelephonyManager {

    private static final String TAG = "MyTelephonyManager";
    private Context mContext;

    public MyTelephonyManager(Context context) {
        mContext = context;
    }

    private int getActiveSubscriptionSlotIndex() {
        SubscriptionManager subscriptionManager =
                (SubscriptionManager) mContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        if (subscriptionManager != null) {
            // Check if the app has the READ_PHONE_STATE permission
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                // Get a list of all active SIM subscriptions
                List<SubscriptionInfo> activeSubscriptionInfoList =
                        subscriptionManager.getActiveSubscriptionInfoList();
                if (activeSubscriptionInfoList != null) {
                    // Iterate through the list of active SIM subscriptions
                    for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
                        // Check if the subscription is for a SIM card
                        if (subscriptionInfo.getSimSlotIndex() >= 0) {
                            // This subscription is for a SIM card, so return the slot index
                            return subscriptionInfo.getSimSlotIndex();
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to get active subscription info list");
                }
            } else {
                Log.e(TAG, "Missing READ_PHONE_STATE permission");
            }
        } else {
            Log.e(TAG, "Failed to get SubscriptionManager");
        }
        // No active SIM card found, so return -1
        return -1;
    }


    public String getImei() {
        int slotIndex = getActiveSubscriptionSlotIndex();
        String imei = null;
        TelephonyManager telephonyManager =
                (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                imei = telephonyManager.getDeviceId(slotIndex);
            } else {
                imei = telephonyManager.getDeviceId();
            }
        } else {
            Log.e(TAG, "Failed to get TelephonyManager");
        }
        return imei;
    }

//
//    public String getImei() {
//        int slotIndex = getActiveSubscriptionSlotIndex();
//        String imei = null;
//        TelephonyManager telephonyManager =
//                (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
//        if (telephonyManager != null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                if (mContext.checkSelfPermission(android.Manifest.permission.READ_PRIVILEGED_PHONE_STATE) ==
//                        android.content.pm.PackageManager.PERMISSION_GRANTED) {
//                    imei = telephonyManager.getImei(slotIndex);
//                } else {
//                    Log.e(TAG, "No permission to get IMEI");
//                }
//            } else {
//                imei = telephonyManager.getDeviceId(slotIndex);
//            }
//        } else {
//            Log.e(TAG, "Failed to get TelephonyManager");
//        }
//        return imei;
//    }
}
