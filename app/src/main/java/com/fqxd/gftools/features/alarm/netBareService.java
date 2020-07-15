package com.fqxd.gftools.features.alarm;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

import com.fqxd.gftools.MainActivity;
import com.fqxd.gftools.R;
import com.github.megatronking.netbare.NetBareService;

public class netBareService extends NetBareService {
    @Override
    protected int notificationId() {
        return 12345;
    }

    @NonNull
    @Override
    protected Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .setContentTitle("VPN running")
                .setContentText("click here to open")
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_MIN);
        if(Build.VERSION.SDK_INT > 25) builder.setChannelId(getString(R.string.notify_channel_id));
        return builder.build();
    }
}
