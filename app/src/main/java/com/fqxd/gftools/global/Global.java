package com.fqxd.gftools.global;

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
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Global extends Application {
    public static String Storage;
    public static String Prefs;
    public static boolean isAmazonBuild;

    @Override
    public void onCreate() {
        super.onCreate();
        Storage = Environment.getExternalStorageDirectory().getPath();
        Prefs = getPackageName() + "_preferences";
        registerNotificationChannels();
        isAmazonBuild = getSHA1Hash(this).equals("23:B5:4C:B4:01:78:A9:01:AB:A6:F3:EA:8B:29:5E:2E:C1:E1:69:3D");
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
        gsDialog.setTitle(context.getString(R.string.setAccessibilityPermissionTitle));
        gsDialog.setMessage(context.getString(R.string.setAccessibilityPermissionMessage));
        gsDialog.setPositiveButton(R.string.Global_OK, (dialog, which) -> context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).create();
        gsDialog.setNegativeButton(R.string.Global_Cancel, ((dialog, which) -> { }));
        gsDialog.show();
    }

    public static boolean checkRootPermission() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("su -c id");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if(line.contains("uid=0") && line.contains("gid=0") && line.contains("root")) return true;
        }
        process.waitFor();
        return false;
    }

    private void registerNotificationChannels() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT > 25) {
            NotificationChannel mChannel = new NotificationChannel("GFRotationService", "GFRotationService", NotificationManager.IMPORTANCE_NONE);
            mChannel.setDescription("GF Screen Rotation service notification");
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
