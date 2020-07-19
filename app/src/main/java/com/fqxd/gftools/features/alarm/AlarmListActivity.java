package com.fqxd.gftools.features.alarm;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;
import com.github.megatronking.netbare.NetBare;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class AlarmListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarmlist);
        SharedPreferences prefs = getSharedPreferences("MainActivity", MODE_PRIVATE);
        Switch alarmOnOff = findViewById(R.id.OnOff);
        if(!checkAccessibilityPermissions()) prefs.edit().putBoolean("AlarmOnOff",false).apply();
        alarmOnOff.setChecked(prefs.getBoolean("AlarmOnOff", false));
        alarmOnOff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d("access",checkAccessibilityPermissions() ? "true" : "false");
            if(!checkAccessibilityPermissions()) {
                if(!alarmOnOff.isChecked()) {
                    prefs.edit().putBoolean("AlarmOnOff", alarmOnOff.isChecked()).apply();
                    NetBare.get().stop();
                }
                else {
                    setAccessibilityPermissions();
                    alarmOnOff.setChecked(false);
                }
            }
            else {
                prefs.edit().putBoolean("AlarmOnOff", alarmOnOff.isChecked()).apply();
                if(alarmOnOff.isChecked()) {
                    NetBare.get().prepare();
                    NetBare.get().start(((Global) getApplication()).getConfig());
                } else NetBare.get().stop();
            }
        });

        Button add = findViewById(R.id.add);
        add.setVisibility(prefs.getBoolean("debug", false) ? View.VISIBLE : View.GONE);
        add.setOnClickListener(v -> startActivity(new Intent(this, AddAlarmManuallyClass.class)));

        RecyclerView recyclerView = findViewById(R.id.ListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        try {
            JSONArray array = new JSONArray(prefs.getString("AlarmData", "[]"));
            if (array.length() > 0) {
                ListViewAdapter adapter = new ListViewAdapter(array);
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

    boolean checkAccessibilityPermissions() {
        AccessibilityManager accessibilityManager = (AccessibilityManager)getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceList = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        return serviceList.toString().contains("com.fqxd.gftools");
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