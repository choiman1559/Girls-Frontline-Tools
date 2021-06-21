package com.fqxd.gftools.features.noti;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.application.isradeleon.notify.Notify;
import com.fqxd.gftools.global.Global;
import com.fqxd.gftools.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Objects;

import static com.fqxd.gftools.features.noti.NotiListenerClass.getMACAddress;

public class FirebaseMessageService extends FirebaseMessagingService {

    SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences(Global.Prefs, MODE_PRIVATE);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();

        if (prefs.getBoolean("Enabled", false) && !prefs.getString("uid", "").equals("")) {
            if (data.get("type").equals("send") && prefs.getString("notiMode","").equals("receive")) {
                sendNotification(data.get("title"), data.get("message"), data.get("package"), data.get("device_id"), data.get("device_name"));
            } else {
                String DEVICE_NAME = Build.MANUFACTURER + " " + Build.MODEL;
                String DEVICE_ID = getMACAddress();
                if (data.get("type").equals("reception") && prefs.getString("notiMode","").equals("send") && data.get("device_id").equals(DEVICE_ID) && data.get("device_name").equals(DEVICE_NAME)) {
                    String Package = data.get("Package");
                    new Handler(Looper.getMainLooper()).postDelayed(() -> Toast.makeText(this, "소전툴즈에서 원격 실행됨\n보낸 기기 : " + data.get("from_name"), Toast.LENGTH_SHORT).show(), 0);
                    try {
                        getPackageManager().getPackageInfo(Package, PackageManager.GET_ACTIVITIES);
                        Intent intent = getPackageManager().getLaunchIntentForPackage(Package);
                        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    } catch (Exception e) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Package));
                        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                }
            }
        }
    }

    private void sendNotification(String title, String content, String Package, String DEVICE_ID, String DEVICE_NAME) {
        Intent intent = new Intent(FirebaseMessageService.this, MessageSendClass.class).putExtra("package", Package).putExtra("device_id", DEVICE_ID).putExtra("device_name", DEVICE_NAME);
        Notify.create(FirebaseMessageService.this)
                .setTitle(title)
                .setContent(content)
                .setLargeIcon(R.drawable.gf_icon)
                .circleLargeIcon()
                .setAction(intent)
                .setImportance(Notify.NotificationImportance.MAX)
                .setSmallIcon(R.drawable.start_xd)
                .enableVibration(true)
                .setAutoCancel(true)
                .show();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        if (!getSharedPreferences(Global.Prefs, MODE_PRIVATE).getString("uid", "").equals(""))
            FirebaseMessaging.getInstance().subscribeToTopic(Objects.requireNonNull(prefs.getString("uid", "")));
    }
}
