package com.fqxd.gftools.features.alarm.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;
import com.fqxd.gftools.features.alarm.logcat.LogCatReaderService;
import com.github.megatronking.netbare.NetBare;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class AlarmListActivity extends AppCompatActivity {
    public static AppCompatActivity appCompatActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarmlist);
        SharedPreferences prefs = getSharedPreferences("com.fqxd.gftools_preferences", MODE_PRIVATE);
        Switch alarmOnOff = findViewById(R.id.OnOff);
        RadioButton adbRadio = findViewById(R.id.ADBRadio);
        RadioButton vpnRadio = findViewById(R.id.VPNRadio);

        if(prefs.getString("AlarmMethod","Logcat").equals("Logcat")) {
            adbRadio.setChecked(true);
            vpnRadio.setChecked(false);
        } else {
            adbRadio.setChecked(false);
            vpnRadio.setChecked(true);
        }

        if(prefs.getBoolean("AlarmOnOff",false)) {
            adbRadio.setEnabled(false);
            vpnRadio.setEnabled(false);
        }

        if(Build.VERSION.SDK_INT < 26) vpnRadio.setEnabled(false);

        RadioGroup group = findViewById(R.id.RadioGroup);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            alarmOnOff.setChecked(false);
            prefs.edit().putBoolean("AlarmOnOff", false).apply();
            NetBare.get().stop();
            stopService();
            if(adbRadio.isChecked()) {
                prefs.edit().putString("AlarmMethod","Logcat").apply();
            } else prefs.edit().putString("AlarmMethod","Vpn").apply();
        });

        if(!Global.checkAccessibilityPermissions(this)) prefs.edit().putBoolean("AlarmOnOff",false).apply();
        alarmOnOff.setChecked(prefs.getBoolean("AlarmOnOff", false));
        alarmOnOff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d("access",Global.checkAccessibilityPermissions(this) ? "true" : "false");
            if(!Global.checkAccessibilityPermissions(this)) {
                if(!alarmOnOff.isChecked()) {
                    prefs.edit().putBoolean("AlarmOnOff", alarmOnOff.isChecked()).apply();
                    stopService();
                }
                else {
                    setAccessibilityPermissions();
                    alarmOnOff.setChecked(false);
                }
            }
            else {
                prefs.edit().putBoolean("AlarmOnOff", alarmOnOff.isChecked()).apply();
                if(alarmOnOff.isChecked()) {
                    if (ContextCompat.checkSelfPermission(AlarmListActivity.this, Manifest.permission.READ_LOGS) != PackageManager.PERMISSION_GRANTED && adbRadio.isChecked()) {
                        alarmOnOff.setChecked(false);
                        prefs.edit().putBoolean("AlarmOnOff", alarmOnOff.isChecked()).apply();

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("특수 권한이 필요합니다").setMessage("이 기능을 사용하려면 READ_LOGS 권한이 필요합니다");
                        builder.setPositiveButton("슈퍼유저 사용", (dialog, id) -> {
                            try {
                                Runtime.getRuntime().exec("su -c pm grant com.fqxd.gftools android.permission.READ_LOGS && am force-stop com.fqxd.gftools");
                            } catch (IOException e) {
                                AlertDialog.Builder b = new AlertDialog.Builder(this);
                                b.setTitle("Error!").setMessage("루트 권한을 인식할수 없습니다! 기기가 루팅이 되어있는지 확인 후 다시 시도하십시오!");
                                b.setPositiveButton("OK", (d, i) -> { });
                                b.create().show();
                                e.printStackTrace();
                            }
                        });

                        builder.setNegativeButton("adb 사용", (dialog, id) -> {
                            AlertDialog.Builder adb = new AlertDialog.Builder(this);
                            adb.setTitle("adb 사용").setMessage("1. adb와 컴퓨터를 연결합니다.\n2. 터미널(이나 cmd)에 다음과 같이 입력합니다 : \nadb shell \"pm grant com.fqxd.gftools android.permission.READ_LOGS && am force-stop com.fqxd.gftools\"\n");
                            adb.setPositiveButton("복사", (d, i) -> {
                                ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("명령어","adb shell \"pm grant com.fqxd.gftools android.permission.READ_LOGS && am force-stop com.fqxd.gftools\"");
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(AlarmListActivity.this,"클립보드에 복사됨",Toast.LENGTH_SHORT).show();
                            });

                            adb.setNeutralButton("취소", (d, i) -> { });
                            AlertDialog alertDialog = adb.create();
                            alertDialog.show();
                        });

                        builder.setNeutralButton("취소", (dialog, id) -> { });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                    else {
                        if(Build.VERSION.SDK_INT > 22 && !Settings.canDrawOverlays(AlarmListActivity.this)) {
                            alarmOnOff.setChecked(false);
                            prefs.edit().putBoolean("AlarmOnOff", alarmOnOff.isChecked()).apply();
                            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                        else {
                            if(adbRadio.isChecked()) startService();
                            else {
                                Intent netBareIntent = NetBare.get().prepare();
                                if(netBareIntent == null) NetBare.get().start(((Global)getApplication()).getConfig());
                                else startActivityForResult(netBareIntent,0);
                            }
                            adbRadio.setEnabled(false);
                            vpnRadio.setEnabled(false);
                        }
                    }
                } else {
                    if(adbRadio.isChecked()) stopService();
                    else NetBare.get().stop();
                    adbRadio.setEnabled(true);
                    vpnRadio.setEnabled(true);
                }
            }
        });

        Button add = findViewById(R.id.add);
        add.setVisibility(prefs.getBoolean("debug", false) ? View.VISIBLE : View.GONE);
        add.setOnClickListener(v -> startActivity(new Intent(this, AddAlarmManuallyClass.class)));

        RecyclerView recyclerView = findViewById(R.id.ListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        try {
            JSONArray array = new JSONArray(prefs.getString("AlarmData", "[ ]"));
            if (array.length() > 0) {
                ListViewAdapter adapter = new ListViewAdapter(array);
                adapter.setOnDataChangedListener(AlarmListActivity.this::recreate);
                recyclerView.setAdapter(adapter);
                findViewById(R.id.dataEmpty).setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.GONE);
                findViewById(R.id.dataEmpty).setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 || requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (!NetBare.get().isActive()) {
                    NetBare.get().start(((Global) getApplication()).getConfig());
                }
            } else {
                Intent intent = NetBare.get().prepare();
                startActivityForResult(intent, requestCode);
            }
        }
    }

    void startService() {
        Set<String> set = new HashSet<>();
        for(int i = 0;i < 5;i++) set.add(Integer.toString(i));
        getSharedPreferences("com.fqxd.gftools_preferences",MODE_PRIVATE).edit().putStringSet("pref_key_logcat_buffers",set).apply();
        Intent intent = new Intent(getApplicationContext(),LogCatReaderService.class);
        if(Build.VERSION.SDK_INT > 25) startForegroundService(intent);
        else startService(intent);
        appCompatActivity = AlarmListActivity.this;
    }

    void stopService() {
        stopService(new Intent(this,LogCatReaderService.class));
    }

    public void setAccessibilityPermissions() {
        AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
        gsDialog.setTitle("접근성 권한 설정");
        gsDialog.setMessage("이 기능을 사용하려면 접근성 권한이 필요합니다");
        gsDialog.setPositiveButton("확인", (dialog, which) -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).create();
        gsDialog.setNegativeButton("취소",((dialog, which) -> {}));
        gsDialog.show();
    }
}