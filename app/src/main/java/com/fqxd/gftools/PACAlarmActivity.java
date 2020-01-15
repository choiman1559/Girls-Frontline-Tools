package com.fqxd.gftools;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.fqxd.gftools.vpn.utils.VpnServiceHelper;

public class PACAlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pacalarm);
        PAlarmAddClass.isasking = false;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(PACAlarmActivity.this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
            if(!Settings.canDrawOverlays(PACAlarmActivity.this)) {
                Toast.makeText(getApplicationContext(), "다른 앱 위에 그리기 권한 없음", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
        SharedPreferences setEnable = getSharedPreferences("ListAlarm", MODE_PRIVATE);
        SharedPreferences.Editor editEnable = setEnable.edit();

        Switch isEnabled = findViewById(R.id.serviceonoff);
        isEnabled.setChecked(setEnable.getBoolean("isChecked", false));
        if(setEnable.getBoolean("isChecked",false) && !VpnServiceHelper.vpnRunningStatus()) isEnabled.setChecked(false);

        isEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PacketClass pacls = new PacketClass();

                if (!isEnabled.isChecked()) {
                    pacls.endVpn(PACAlarmActivity.this);
                    editEnable.putBoolean("isChecked", false).apply();
                } else {
                    pacls.setVpn(PACAlarmActivity.this);
                    pacls.runVpn(PACAlarmActivity.this);
                    editEnable.putBoolean("isChecked", true).apply();
                    PAlarmAddClass.context = PACAlarmActivity.this;
                    PAlarmAddClass.isNO = false;
                }
            }

        });
    }
}