package com.fqxd.gftools.alarm.palarm;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class PAlarmAddClass extends Application {
    public static Context context;
    public static Boolean isNO = false;
    public static Boolean isasking = false;
    private static int m_RSeqNo = 0;

    @SuppressLint({"TrulyRandom"})
    public String parseJsonPacket(String encoded) throws Exception {
        Cipher m_RCipher = Cipher.getInstance("AES");
        byte[] nullKey = new byte[16];
        Arrays.fill(nullKey, (byte) 0);
        SecretKeySpec secretKeySpec = new SecretKeySpec(nullKey, "AES");
        m_RCipher.init(2, secretKeySpec);

        byte[] plain;
        byte[] data = Base64.decode(encoded,2);
        Log.d("Base64 decode", new String(data));

            if (data[0] != (byte) 0) {
                Log.d("Base64 decode", "chk5");
                throw new Exception("SDK support 'null key' only");
            }
            byte[] buffer = m_RCipher.doFinal(data, 1, data.length - 1);
            if (((((buffer[3] & 255) << 24) + ((buffer[2] & 255) << 16)) + ((buffer[1] & 255) << 8)) + ((buffer[0] & 255) << 0) != m_RSeqNo) {
                throw new Exception("Packet is damaged because of 'invalid seqeunce'");
            }
            plain = new byte[(buffer.length - 4)];
            System.arraycopy(buffer, 4, plain, 0, plain.length);
            m_RSeqNo++;
        return new String(plain);
    }

    public void add(File file) {

        if(new PacketClass().isInclude(file,"Operation")) {

            Log.d("Base64 decode", "chk1");

            //Base64 String
            String outcodedata = parseData(file);
            String decoded;

            try {
                decoded = parseJsonPacket(outcodedata.replace("%2d","-").replace("%2f","/").replace("%3d","=").replace("%2b","+").replace("%5f","_"));
                Log.d("Base64 decode", "JSON : " + decoded);
            } catch (Exception e) { e.printStackTrace(); }

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
