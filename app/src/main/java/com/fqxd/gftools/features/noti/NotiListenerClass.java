package com.fqxd.gftools.features.noti;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.toolbox.JsonObjectRequest;
import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.R;
import com.fqxd.gftools.global.Global;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotiListenerClass extends NotificationListenerService {

    SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences(Global.Prefs, Context.MODE_PRIVATE);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        if (prefs.getBoolean("Enabled", false) && prefs.getString("notiMode","").equals("send")) {
            if (isGF(sbn.getPackageName())) {
                Notification notification = sbn.getNotification();
                Bundle extra = notification.extras;

                String TOPIC = "/topics/" + prefs.getString("uid", "");
                String TITLE = extra.getString(Notification.EXTRA_TITLE);
                String TEXT = extra.getCharSequence(Notification.EXTRA_TEXT).toString();
                String DEVICE_NAME = Build.MANUFACTURER + " " + Build.MODEL;
                String DEVICE_ID = getMACAddress();
                String Package = "" + sbn.getPackageName();

                JSONObject notificationHead = new JSONObject();
                JSONObject notifcationBody = new JSONObject();
                try {
                    notifcationBody.put("title", TITLE);
                    notifcationBody.put("message", TEXT);
                    notifcationBody.put("package", Package);
                    notifcationBody.put("type", "send");
                    notifcationBody.put("device_name", DEVICE_NAME);
                    notifcationBody.put("device_id",  DEVICE_ID);

                    notificationHead.put("to", TOPIC);
                    notificationHead.put("data", notifcationBody);
                } catch (JSONException e) {
                    Log.e("Noti", "onCreate: " + e.getMessage());
                }
                sendNotification(notificationHead);
            }
        }
    }

    static String getMACAddress() {
        String interfaceName = "wlan0";
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) return "";
                StringBuilder buf = new StringBuilder();
                for (byte b : mac) buf.append(String.format("%02X:", b));
                if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    public boolean isGF(String Packagename) {
        if (Packagename.equals(getString(R.string.target_cn_bili))) return true;
        if (Packagename.equals(getString(R.string.target_cn_uc))) return true;
        if (Packagename.equals(getString(R.string.target_en))) return true;
        if (Packagename.equals(getString(R.string.target_jp))) return true;
        if (Packagename.equals(getString(R.string.target_kr))) return true;
        if (Packagename.equals(getString(R.string.target_tw))) return true;
        return (BuildConfig.DEBUG && Packagename.equals(getApplicationContext().getPackageName()));
    }

    private void sendNotification(JSONObject notification) {
        final String FCM_API = "https://fcm.googleapis.com/fcm/send";
        final String serverKey = "key=" + getString(R.string.serverKey);
        final String contentType = "application/json";
        final String TAG = "NOTIFICATION TAG";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                response -> Log.i(TAG, "onResponse: " + response.toString()),
                error -> {
                    Toast.makeText(NotiListenerClass.this, "알람 전송 실패! 인터넷 환경을 확인해주세요!", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "onErrorResponse: Didn't work");
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }
}