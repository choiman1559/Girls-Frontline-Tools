package com.fqxd.gftools.features.alarm.vpn;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

import com.fqxd.gftools.features.alarm.ui.AlarmListActivity;
import com.github.megatronking.netbare.NetBareService;

import static com.fqxd.gftools.Global.floatingView;

public class netBareService extends NetBareService {

    @Override
    protected int notificationId() {
        return 12345;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        floatingView.CreateView();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        floatingView.destroy();
    }

    @NonNull
    @Override
    protected Notification createNotification() {
        Intent intent = new Intent(this, AlarmListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .setContentTitle("VPN running")
                .setContentText("click here to open")
                .setContentIntent(pendingIntent);
        if(Build.VERSION.SDK_INT > 25) builder.setChannelId("GFPacketService");
        return builder.build();
    }
}
