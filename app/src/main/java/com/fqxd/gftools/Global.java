package com.fqxd.gftools;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import com.fqxd.gftools.features.alarm.vpn.PacketInjector;
import com.github.megatronking.netbare.NetBare;
import com.github.megatronking.netbare.NetBareConfig;
import com.github.megatronking.netbare.http.HttpInjectInterceptor;
import com.github.megatronking.netbare.ssl.JKS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

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
        for (String str : array) configBuilder.addAllowedApplication(str);
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
        NetBare.get().attachApplication(this, BuildConfig.DEBUG);

        registerNotificationChannels("GFPacketService", "GF Packet Notification Channel");
        registerNotificationChannels("GFRotationService", "GF Screen Rotation service notification");
    }

    public static boolean checkAccessibilityPermissions(Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.DEFAULT);
        for (int i = 0; i < list.size(); i++) {
            AccessibilityServiceInfo info = list.get(i);
            if (info.getResolveInfo().serviceInfo.packageName.equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public static void setAccessibilityPermissions(Context context) {
        AlertDialog.Builder gsDialog = new AlertDialog.Builder(context);
        gsDialog.setTitle("접근성 권한 설정");
        gsDialog.setMessage("이 기능을 사용하려면 접근성 권한이 필요합니다");
        gsDialog.setPositiveButton("확인", (dialog, which) -> context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).create();
        gsDialog.setNegativeButton("취소", ((dialog, which) -> { }));
        gsDialog.show();
    }

    public static boolean checkRootPermission() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("su -c id");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        boolean value = false;
        while ((line = reader.readLine()) != null) {
            if(line.contains("uid=0") && line.contains("gid=0") && line.contains("root")) value = true;
        }
        process.waitFor();
        return value;
    }

    private void registerNotificationChannels(String ID, String DES) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT > 25) {
            NotificationChannel mChannel = new NotificationChannel(ID, ID, NotificationManager.IMPORTANCE_NONE);
            mChannel.setDescription(DES);
            mChannel.enableVibration(false);
            mChannel.setImportance(NotificationManager.IMPORTANCE_NONE);
            mChannel.enableLights(false);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @SuppressLint("PackageManagerGetSignatures")
    public static String getSHA1Hash(Context context) {
        try {
            final PackageInfo info = context.getPackageManager()
                    .getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                final MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());

                final byte[] digest = md.digest();
                final StringBuilder toRet = new StringBuilder();
                for (int i = 0; i < digest.length; i++) {
                    if (i != 0) toRet.append(":");
                    int b = digest[i] & 0xff;
                    String hex = Integer.toHexString(b);
                    if (hex.length() == 1) toRet.append("0");
                    toRet.append(hex);
                }
                return toRet.toString();
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
        return "";
    }
}
