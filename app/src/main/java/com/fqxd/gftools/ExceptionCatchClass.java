package com.fqxd.gftools;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import com.application.isradeleon.notify.Notify;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionCatchClass {
    public void CatchException(Context context,Exception Exception) {

        StringWriter sw = new StringWriter();
        Exception.printStackTrace(new PrintWriter(sw));
        String StackTraceString = sw.toString();

        try {
            Intent email = new Intent(Intent.ACTION_SEND);
            email.setType("plain/text");
            email.putExtra(Intent.EXTRA_EMAIL,new String[]{"cuj1559@gmail.com"});
            email.putExtra(Intent.EXTRA_SUBJECT,"GFTools Bug Report");
            email.putExtra(Intent.EXTRA_TEXT,"Version : " + context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName + "\nAndroidOS : " + Build.VERSION.SDK_INT + "\nError : " + Exception.toString() + "\n\n\nDetail : " + StackTraceString);
            email.setType("message/rfc822");

            Notify.create(context)
                    .setTitle("Error occurred!")
                    .setContent("Click here to report GFTool's bug!")
                    .setImportance(Notify.NotificationImportance.MAX)
                    .setAutoCancel(true)
                    .setAction(email)
                    .circleLargeIcon()
                    .show();

        } catch (PackageManager.NameNotFoundException e) {
            CatchException(context,e);
        }
    }
}
