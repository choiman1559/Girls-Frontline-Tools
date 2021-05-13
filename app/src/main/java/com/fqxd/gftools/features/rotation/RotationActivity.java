package com.fqxd.gftools.features.rotation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fqxd.gftools.global.Global;
import com.fqxd.gftools.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONObject;

public class RotationActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotation);
        SharedPreferences prefs = getSharedPreferences(Global.Prefs, Context.MODE_PRIVATE);
        String Package = getIntent().getStringExtra("pkg");
        SwitchMaterial Enabled = findViewById(R.id.Enabled);
        TextView Target = findViewById(R.id.target);

        try {
            PackageManager pm = getPackageManager();
            Target.setText(String.format("target : %s\n(%s)", pm.getApplicationLabel(pm.getApplicationInfo(Package, PackageManager.GET_META_DATA)), Package));
            Enabled.setChecked(new JSONObject(prefs.getString("RotationData","")).getBoolean(Package));
        } catch (Exception e) {
            Enabled.setChecked(false);
        }

        Enabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!isChecked) stopService(new Intent(this, RotationService.class));
            JSONObject obj;
            try {
                if(Build.VERSION.SDK_INT > 22 && !Settings.canDrawOverlays(RotationActivity.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    Toast.makeText(getApplicationContext(), "이 기능을 사용하려면 다른 앱 위에 그리기 기능이 필요합니다!", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    Enabled.setChecked(false);
                }else{
                    if(Build.VERSION.SDK_INT > 22 && !Settings.System.canWrite(this.getApplicationContext())) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                        Toast.makeText(getApplicationContext(), "이 기능을 사용하려면 시스템 설정 수정 기능이 필요합니다!", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        Enabled.setChecked(false);
                    }
                    else if(Build.VERSION.SDK_INT < 23 && ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_SETTINGS},1000);
                        Enabled.setChecked(false);
                    }
                    else {
                        if(!Global.checkAccessibilityPermissions(this)) {
                            Global.setAccessibilityPermissions(this);
                            Enabled.setChecked(false);
                        }
                        else {
                            String str = prefs.getString("RotationData","");
                            obj = str.equals("") ? new JSONObject() : new JSONObject(str);
                            obj.put(Package,isChecked);
                            prefs.edit().putString("RotationData",obj.toString()).apply();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Enabled.setChecked(checkPermission() && Enabled.isChecked());
    }

    boolean checkPermission() {
        boolean a = Build.VERSION.SDK_INT > 22 && Settings.canDrawOverlays(RotationActivity.this);
        boolean b = Build.VERSION.SDK_INT > 22 && Settings.System.canWrite(this.getApplicationContext());
        boolean c = Global.checkAccessibilityPermissions(this);
        return a && b && c;
    }
}
