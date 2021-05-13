package com.fqxd.gftools.features.noti;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.application.isradeleon.notify.Notify;
import com.fqxd.gftools.global.Global;
import com.fqxd.gftools.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FirebaseMessageService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        SharedPreferences prefs = getSharedPreferences(Global.Prefs, MODE_PRIVATE);
        Map<String, String> data = remoteMessage.getData();

        if (prefs.getBoolean("Enabled", false) && !prefs.getString("uid","").equals("") && data.get("type").equals("send"))
            sendNotification(data.get("title"), data.get("message"),data.get("package"),data.get("device_id"),data.get("device_name"));
    }

    private void sendNotification(String title, String content,String Package,String DEVICE_ID,String DEVICE_NAME) {
        Intent intent = new Intent(FirebaseMessageService.this,MessageSendClass.class).putExtra("package",Package).putExtra("device_id",DEVICE_ID).putExtra("device_name",DEVICE_NAME);
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
        if(!getSharedPreferences(Global.Prefs, MODE_PRIVATE).getString("uid", "").equals(""))
            FirebaseMessaging.getInstance().subscribeToTopic(getSharedPreferences(Global.Prefs, MODE_PRIVATE).getString("uid", ""));
    }
}
