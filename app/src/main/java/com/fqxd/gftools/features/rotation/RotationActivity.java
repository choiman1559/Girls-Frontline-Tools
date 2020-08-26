package com.fqxd.gftools.features.rotation;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fqxd.gftools.R;

import org.json.JSONObject;

import java.util.List;

public class RotationActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotation);
        SharedPreferences preferences = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
        String Package = getIntent().getStringExtra("pkg");
        Switch Enabled = findViewById(R.id.Enabled);
        TextView Target = findViewById(R.id.target);

        try {
            PackageManager pm = getPackageManager();
            Target.setText("target : " + pm.getApplicationLabel(pm.getApplicationInfo(Package, PackageManager.GET_META_DATA)) + "\n(" + Package + ")");
            Enabled.setChecked(new JSONObject(preferences.getString("RotationData","")).getBoolean(Package));
        } catch (Exception e) {
            Enabled.setChecked(false);
        }
        Enabled.setChecked(checkAccessibilityPermissions());

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
                        if(!checkAccessibilityPermissions()) {
                            setAccessibilityPermissions();
                            Enabled.setChecked(false);
                        }
                        else {
                            String str = preferences.getString("RotationData","");
                            obj = str.equals("") ? new JSONObject() : new JSONObject(str);
                            obj.put(Package,isChecked);
                            preferences.edit().putString("RotationData",obj.toString()).apply();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    boolean checkAccessibilityPermissions() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceList = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        return serviceList.toString().contains("com.fqxd.gftools");
    }

    public void setAccessibilityPermissions() {
        AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
        gsDialog.setTitle("접근성 권한 설정");
        gsDialog.setMessage("이 기능을 사용하려면 접근성 권한이 필요합니다");
        gsDialog.setPositiveButton("확인", (dialog, which) -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).create();
        gsDialog.setNegativeButton("취소", ((dialog, which) -> {
        }));
        gsDialog.show();
    }
}
