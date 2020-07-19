package com.fqxd.gftools;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.fqxd.gftools.features.alarm.PacketInjector;
import com.github.megatronking.netbare.NetBare;
import com.github.megatronking.netbare.NetBareConfig;
import com.github.megatronking.netbare.http.HttpInjectInterceptor;
import com.github.megatronking.netbare.ssl.JKS;
import com.gitlab.prototypeg.Session;

import java.util.ArrayList;

import kotlin.collections.CollectionsKt;

public class Global extends Application {
    private JKS jks;
    private final Session session = new Session();

    public final Session getSession() {
        return this.session;
    }

    public final JKS getJks() {
        return this.jks;
    }

    public final NetBareConfig getConfig() {
        JKS jks2 = this.jks;
        NetBareConfig.Builder configBuilder = NetBareConfig.defaultHttpConfig(jks2, CollectionsKt.listOf(HttpInjectInterceptor.createFactory(new PacketInjector(this.session, this)))).newBuilder();
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
        this.jks = new JKS(context, string, charArray, getString(R.string.app_name), getString(R.string.app_name), getString(R.string.app_name), getString(R.string.app_name), getString(R.string.app_name));
        registerNotificationChannels();
        NetBare.get().attachApplication(this, false);
    }

    private void registerNotificationChannels() {
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT > 25) {
            NotificationChannel mChannel = new NotificationChannel("VpnService", "VpnService", NotificationManager.IMPORTANCE_NONE);
            mChannel.setDescription("vpn service Notification Channel");
            mChannel.enableVibration(false);
            mChannel.setImportance(NotificationManager.IMPORTANCE_NONE);
            mChannel.enableLights(false);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }
}
