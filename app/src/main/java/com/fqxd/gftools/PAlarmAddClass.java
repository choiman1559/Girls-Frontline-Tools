package com.fqxd.gftools;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class PAlarmAddClass extends Application {
    public static Context context;
    public static Boolean isNO = false;
    public static Boolean isasking = false;
    public void add(File file) {

        if(new PacketClass().isInclude(file,"Operation")) {

            //Base64 String
            String outcodedata = parseData(file);

            if(new PacketClass().isInclude(file,"start")) {
                //군수 시작처리
            }

            if(new PacketClass().isInclude(file,"abort")){
                //군수 취소처리
            }
        }

        if (new PacketClass().isInclude(file, "uid")) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("ListAlarm", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String uid = parseUID(file);
            Log.d("user_uid", "uid : " + uid);
            if (sharedPreferences.getString("uid", "") != uid && uid != null && !isNO && !isasking) {
                isasking = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("군수알리미 알림");
                builder.setMessage("새 UID 가 포착되었습니다!\nUID : " + uid + "\n위 UID 가 일치합니까?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putString("uid", uid).apply();
                        Toast.makeText(context, "UID saved!", Toast.LENGTH_SHORT).show();
                        isasking = false;
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isNO = true;
                        isasking = false;
                    }
                });
                AlertDialog alertDialog = builder.create();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                else alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                alertDialog.show();
            }
        }
    }

    private String parseUID(File file) {
        String toParse = "";
        String line;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("outdatacode")) toParse = line;
            }
            bufferedReader.close();
        } catch (Exception e) {
        }
        return substringBetween(toParse,"uid=","&out");
    }

    private String parseData(File file) {
        String toParse = "";
        String line;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("outdatacode")) toParse = line;
            }
            bufferedReader.close();
        } catch (Exception e) {
        }
        return substringBetween(toParse,"code=","&req");
    }

    private String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }
}
