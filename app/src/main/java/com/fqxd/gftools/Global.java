package com.fqxd.gftools;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.fqxd.gftools.features.alarm.vpn.PacketInjector;
import com.github.megatronking.netbare.NetBare;
import com.github.megatronking.netbare.NetBareConfig;
import com.github.megatronking.netbare.http.HttpInjectInterceptor;
import com.github.megatronking.netbare.ssl.JKS;

import java.util.ArrayList;

import kotlin.collections.CollectionsKt;

public class Global extends Application {
    private static JKS jks;

    public NetBareConfig getConfig() {
        NetBareConfig.Builder configBuilder = NetBareConfig.defaultHttpConfig(jks, CollectionsKt.listOf(HttpInjectInterceptor.createFactory(new PacketInjector(new com.gitlab.prototypeg.Session(), this)))).newBuilder();
        ArrayList<String> array = new ArrayList<>();
        array.add("com.digitalsky.girlsfrontline.cn.uc");
        array.add("com.digitalsky.girlsfrontline.cn.bili");
        array.add("com.sunborn.girlsfrontline.en");
        array.add("com.sunborn.girlsfrontline.jp");
        array.add("tw.txwy.and.snqx");
        array.add("kr.txwy.and.snqx");
        for(String str : array) configBuilder.addAllowedApplication(str);
        return configBuilder.build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = this;
        String string = getString(R.string.app_name);
        String string2 = getString(R.string.app_name);
        char[] charArray = string2.toCharArray();
        jks = new JKS(context, string, charArray, getString(R.string.app_name), getString(R.string.app_name), getString(R.string.app_name), getString(R.string.app_name), getString(R.string.app_name));
        registerNotificationChannels();
        NetBare.get().attachApplication(this, false);
    }

    private void registerNotificationChannels() {
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT > 25) {
            NotificationChannel mChannel = new NotificationChannel("GFPacketService", "GFPacketService", NotificationManager.IMPORTANCE_NONE);
            mChannel.setDescription("GF Packet Notification Channel");
            mChannel.enableVibration(false);
            mChannel.setImportance(NotificationManager.IMPORTANCE_NONE);
            mChannel.enableLights(false);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }
}
