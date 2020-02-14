package com.fqxd.gftools.features.noti;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.application.isradeleon.notify.Notify;
import com.fqxd.gftools.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessageService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        SharedPreferences prefs = getSharedPreferences("NotiPrefs", MODE_PRIVATE);

        if (prefs.getBoolean("Enabled", false) && !prefs.getString("uid","").equals(""))
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("message"),remoteMessage.getData().get("package"));
    }

    private void sendNotification(String title, String content,String Package) {
        Notify.create(FirebaseMessageService.this)
                .setTitle(title)
                .setContent(content)
                .setLargeIcon(R.drawable.gf_icon)
                .circleLargeIcon()
                .setAction(new Intent(FirebaseMessageService.this,MessageSendClass.class).putExtra("package",Package))
                .setImportance(Notify.NotificationImportance.MAX)
                .setSmallIcon(R.drawable.start_xd)
                .enableVibration(true)
                .setAutoCancel(true)
                .show();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        if(!getSharedPreferences("NotiPrefs", MODE_PRIVATE).getString("uid", "").equals(""))
            FirebaseMessaging.getInstance().subscribeToTopic(getSharedPreferences("NotiPrefs", MODE_PRIVATE).getString("uid", ""));
    }
}
