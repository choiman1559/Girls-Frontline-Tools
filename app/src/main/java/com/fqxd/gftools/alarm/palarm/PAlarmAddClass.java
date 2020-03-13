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
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.fqxd.gftools.R;
import com.fqxd.gftools.alarm.alarm.AlarmUtills;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class PAlarmAddClass extends Application {
    public static Context context;
    public static Boolean isNO = false;
    public static Boolean isasking = false;
    private static int m_RSeqNo = 0;

    @Deprecated
    @SuppressLint({"TrulyRandom"})
    public String parseJsonPacket(String encoded) throws Exception {
        Cipher m_RCipher = Cipher.getInstance("AES");
        byte[] nullKey = new byte[16];
        Arrays.fill(nullKey, (byte) 0);
        SecretKeySpec secretKeySpec = new SecretKeySpec(nullKey, "AES");
        m_RCipher.init(2, secretKeySpec);

        byte[] plain;
        byte[] data = Base64.decode(encoded, 2);
        Log.d("Base64 decode", new String(data));

        if (data[0] != (byte) 0) {
            throw new Exception("SDK support 'null key' only");
        }
        byte[] buffer = m_RCipher.doFinal(data, 1, data.length - 1);
        if (((((buffer[3] & 255) << 24) + ((buffer[2] & 255) << 16)) + ((buffer[1] & 255) << 8)) + ((buffer[0] & 255)) != m_RSeqNo) {
            throw new Exception("Packet is damaged because of 'invalid seqeunce'");
        }
        plain = new byte[(buffer.length - 4)];
        System.arraycopy(buffer, 4, plain, 0, plain.length);
        m_RSeqNo++;
        return new String(plain);
    }

    public void add(@NonNull File file) {
        if (new PacketClass().isInclude(file, "Operation")) {

            //Base64 String
            /*
            String outcodedata = parseData(file);
            String decoded;

            try {
                decoded = parseJsonPacket(outcodedata.replace("%2d","-").replace("%2f","/").replace("%3d","=").replace("%2b","+").replace("%5f","_"));
                Log.d("Base64 decode", "JSON : " + decoded);
            } catch (Exception e) { e.printStackTrace(); }
            */

            if (new PacketClass().isInclude(file, "start")) {
                //군수 시작처리
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.dialog_selnum, null);

                final NumberPicker sel1 = view.findViewById(R.id.sel1);
                final NumberPicker sel2 = view.findViewById(R.id.sel2);

                builder.setView(view).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addSharedprefs(sel1.getValue(), sel2.getValue());
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                sel1.setMaxValue(12);
                sel1.setMinValue(0);

                sel2.setMaxValue(4);
                sel2.setMinValue(1);

                builder.setTitle("새 군수 확인됨!");
                builder.setMessage("군수 지역을 선택해 주세요.");

                AlertDialog alertDialog = builder.create();
                Objects.requireNonNull(alertDialog.getWindow()).
                        setType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                alertDialog.show();
            }

            if (new PacketClass().isInclude(file, "abort")) {
                //군수 취소처리
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.dialog_delete, null);
                final Spinner List = view.findViewById(R.id.List);

                int count = context.getSharedPreferences("ListAlarm", MODE_PRIVATE).getInt("PAlarmCount", 0);
                ArrayList<String> Mlist = new ArrayList<>();
                Mlist.add("...");
                for (int i = 1; i <= count; i++) {
                    SharedPreferences a = context.getSharedPreferences("p" + i, MODE_PRIVATE);
                    Mlist.add(a.getString("name", ""));
                }
                ArrayAdapter<String> Madpt = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, Mlist);
                List.setAdapter(Madpt);

                builder.setView(view).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int count = context.getSharedPreferences("ListAlarm", MODE_PRIVATE).getInt("PAlarmCount", 0);
                        if (!List.getSelectedItem().toString().equals("...")) {
                            new PacketClass().cancel(context,List.getSelectedItem().toString());

                        } else {
                            Toast.makeText(context, "값을 선택하여 주십시오.", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });

                builder.setTitle("군수 취소 확인됨!");
                builder.setMessage("취소할 군수 지역을 선택해 주세요.");

                AlertDialog alertDialog = builder.create();
                Objects.requireNonNull(alertDialog.getWindow()).
                        setType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                alertDialog.show();
            }
        }

        if (new PacketClass().isInclude(file, "uid")) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("ListAlarm", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String uid = parseUID(file);
            String temp = sharedPreferences.getString("uid", "000000");
            Log.d("user_uid", "uid : " + uid);
            if(uid != null) {
                if (!temp.equals(uid) && !isNO && !isasking) {
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
                    Objects.requireNonNull(alertDialog.getWindow()).
                            setType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alertDialog.show();
                }
            }
        }
    }

    public void addSharedprefs(int local1, int local2) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("ListAlarm", MODE_PRIVATE);
        int count = sharedPreferences.getInt("PAlarmCount", 0);
        SharedPreferences.Editor editor = context.getSharedPreferences("p" + (count += 1), MODE_PRIVATE).edit();

        editor.putString("package", "kr.txwy.and.snqx");
        String value = Integer.toString(local1) + "-" + Integer.toString(local2);
        editor.putString("name", value);
        editor.putInt("H", local1);
        editor.putInt("M", local2);
        editor.putLong("nextAlarm", new AlarmUtills().calculate(local1, local2, context).getTimeInMillis());
        editor.apply();

        sharedPreferences.edit().putInt("PAlarmCount", count).apply();

        new PacketClass().repeat(context, value);
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
        } catch (Exception ignored) { }
        return substringBetween(toParse, "uid=", "&out");
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
        } catch (Exception ignored) { }
        return substringBetween(toParse, "code=", "&req");
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
