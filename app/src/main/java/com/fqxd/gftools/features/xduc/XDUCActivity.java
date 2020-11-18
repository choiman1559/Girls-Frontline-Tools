package com.fqxd.gftools.features.xduc;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;
import com.fqxd.gftools.fragment.HomeFragment;
import com.google.android.material.snackbar.Snackbar;
import com.xd.xdsdk.XDCallback;
import com.xd.xdsdk.XDSDK;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

public class XDUCActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xduc);

        SharedPreferences prefs = getSharedPreferences(Global.Prefs,MODE_PRIVATE);
        Button UC_KR = findViewById(R.id.Button_kruc);
        Button UC_CN = findViewById(R.id.Button_cnuc);
        EditText UC_Nickname = findViewById(R.id.EditText_Nickname);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        CheckBox Remember = findViewById(R.id.Checkbox_remember);

        Remember.setChecked(prefs.getBoolean("UC_Remember",false));
        if(Remember.isChecked()) UC_Nickname.setText(prefs.getString("UC_Nickname",""));

        Remember.setOnCheckedChangeListener((v,n) -> prefs.edit().putBoolean("UC_Remember",Remember.isChecked()).apply());
        UC_CN.setOnClickListener(v -> XD());
        UC_KR.setOnClickListener(v -> {
            if(isOffline(this)) Snackbar.make(findViewById(R.id.layout),"Check Internet and Try Again", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            else {
                if(UC_Nickname.getText().toString().equals("")) UC_Nickname.setError("Input Nickname");
                else {
                    try {
                        progressBar.setVisibility(View.VISIBLE);
                        if(!Global.checkRootPermission()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("특수 권한이 필요합니다").setMessage("이 기능을 사용하려면 ROOT 권한이 필요합니다");
                            builder.setPositiveButton("슈퍼유저 사용", (dialog, id) -> {
                                try {
                                    Runtime.getRuntime().exec("su");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }).setNegativeButton("취소", (dialog, which) -> { }).show();
                        } else {
                            if(isDataAvailable()) {
                                Process p = Runtime.getRuntime().exec("su");
                                DataOutputStream dos = new DataOutputStream(p.getOutputStream());
                                dos.writeBytes("mount -o remount,rw /data\n");
                                dos.writeBytes("cp -f /data/data/kr.txwy.and.snqx/shared_prefs/file_name.xml /sdcard/GF_Tool/\n");
                                dos.writeBytes("exit\n");
                                dos.flush();
                                dos.close();
                                p.waitFor();

                                String uId = "";
                                String appId = "";
                                String serverId = "";
                                String sig;
                                String nickName = UC_Nickname.getText().toString();
                                long timestamp = Calendar.getInstance().getTimeInMillis();

                                File xml = new File(Global.Storage + "/GF_Tool/file_name.xml");
                                if (xml.exists()) {
                                    String line;
                                    BufferedReader bufferedReader = new BufferedReader(new FileReader(xml));
                                    while ((line = bufferedReader.readLine()) != null) {
                                        if (line.contains("UID"))
                                            uId = substringBetween(line, "\"UID\" value=\"", "\"");
                                        else if (line.contains("APPID"))
                                            appId = substringBetween(line, "\"APPID\">", "</");
                                        else if (line.contains("SID"))
                                            serverId = substringBetween(line, "\"SID\">", "</");
                                    }

                                    prefs.edit().putString("UC_Nickname",nickName).apply();
                                    if (uId.equals("") || appId.equals("") || serverId.equals("")) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                        builder.setTitle("Error!").setMessage("일부 데이터가 누락되었습니다. 재 로그인 후 다시 시도하십시오!");
                                        builder.setPositiveButton("OK", (dialog, id) -> { });
                                        builder.create().show();
                                    } else {
                                        sig = getBugReportSig(serverId, nickName, timestamp, appId, uId);
                                        String uri = "http://csapp.playcomet.com/index.php?appid=" + appId + "&s=" + serverId + "&uid=" + uId + "&uname=g" + uId + "&nickname=" + nickName + "&sig=" + sig + "&time=" + timestamp +"&l=kr";
                                        this.startActivity(new Intent(this,UCWebViewActivity.class).putExtra("uri",uri));
                                    }
                                }
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("Error!").setMessage("데이터 파일을 찾을수 없습니다! 소녀전선(한섭)이 설치되어 있고, 로그인을 한 적 있는지 확인 후 재시도 바랍니다!");
                                builder.setPositiveButton("OK", (dialog, id) -> { });
                                builder.create().show();
                            }
                        }
                    } catch (IOException | InterruptedException e) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Error!").setMessage("루트 권한을 인식할수 없습니다! 기기가 루팅이 되어있는지 확인 후 다시 시도하십시오!");
                        builder.setPositiveButton("OK", (dialog, id) -> { });
                        builder.create().show();
                        e.printStackTrace();
                    } finally {
                        progressBar.setVisibility(View.GONE);
                        File xml = new File(Global.Storage + "/GF_Tool/file_name.xml");
                        if(xml.exists()) xml.delete();
                    }
                }
            }
        });
    }

    @SuppressLint("SdCardPath")
    protected static boolean isDataAvailable() throws InterruptedException, IOException {
        String data = "/data/data/kr.txwy.and.snqx/shared_prefs/file_name.xml";
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream dos = new DataOutputStream(process.getOutputStream());
        dos.writeBytes("find " + data + "\n");
        dos.writeBytes("exit\n");
        dos.flush();
        dos.close();
        process.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        boolean value = false;
        while ((line = reader.readLine()) != null) {
            if (line.contains(data))
                value = true;
        }
        process.waitFor();
        return value;
    }

    @SuppressLint("MissingPermission")
    public boolean isOffline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        if (cm.getActiveNetworkInfo() != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni == null || !ni.isConnected();
        }
        return true;
    }

    private String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return "";
        }
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return "";
    }

    public static String getBugReportSig(String svrId, String nickname, long timestamp, String appid, String uid) {
        String key = appid + '|' + svrId + '|' + uid + '|' + "g" + uid + '|' + nickname + "|sHtsFhdssDF";
        return md5(md5(key) + timestamp);
    }

    public static String md5(String string) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(string.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 255) < 16) {
                    hex.append("0");
                }
                hex.append(Integer.toHexString(b & 255));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        }
    }

    void XD() {
        String TAG = HomeFragment.class.getSimpleName();
        if (isOffline(this)) {
            Snackbar.make(findViewById(R.id.layout), "Check Internet and Try Again", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            XDSDK.setCallback(new XDCallback() {
                @Override
                public void onInitSucceed() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                }

                @Override
                public void onInitFailed(String msg) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + msg);
                }

                @Override
                public void onLoginSucceed(String token) {
                    XDSDK.openUserCenter();
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + token);
                }

                @Override
                public void onLoginFailed(String msg) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + msg);
                }

                @Override
                public void onLoginCanceled() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                }

                @Override
                public void onGuestBindSucceed(String token) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + token);
                }

                @Override
                public void onLogoutSucceed() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                }

                @Override
                public void onPayCompleted() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                }

                @Override
                public void onPayFailed(String msg) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + msg);
                }

                @Override
                public void onPayCanceled() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                }

                @Override
                public void onRealNameSucceed() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                }

                @Override
                public void onRealNameFailed(String msg) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                }
            });

            XDSDK.initSDK(this, "a4d6xky5gt4c80s", 1, "AndroidChannel", "AndroidVersion", true);
            XDSDK.login();
        }
    }
}
