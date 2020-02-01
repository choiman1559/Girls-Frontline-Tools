package com.fqxd.gftools.features.txt;

import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.fqxd.gftools.R;

import java.util.List;

public class TxtKRActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_txt_kr);

        Button origin = findViewById(R.id.origin);
        Switch onoff = findViewById(R.id.onoff);

        Switch bill = findViewById(R.id.bill);
        Switch dgts = findViewById(R.id.dgts);
        Switch chna = findViewById(R.id.chna);
        Switch jpan = findViewById(R.id.jpan);

        SharedPreferences prefs = getSharedPreferences("TxtKRPrefs", MODE_PRIVATE);

        bill.setChecked(prefs.getBoolean("bill",false));
        dgts.setChecked(prefs.getBoolean("dgts",false));
        chna.setChecked(prefs.getBoolean("chna",false));
        jpan.setChecked(prefs.getBoolean("jpan",false));


        onoff.setChecked(prefs.getBoolean("isChecked", false));
        onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (onoff.isChecked() && !isContainedInAccessbility(TxtKRActivity.this)) {
                    Toast.makeText(TxtKRActivity.this, "해당 기능은 접근성 권한이 필요합니다!", Toast.LENGTH_SHORT).show();
                    startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
                    onoff.setChecked(false);
                } else prefs.edit().putBoolean("isChecked", onoff.isChecked()).apply();

                bill.setEnabled(onoff.isChecked());
                dgts.setEnabled(onoff.isChecked());
                //chna.setEnabled(onoff.isChecked());
                jpan.setEnabled(onoff.isChecked());
            }
        });

        bill.setEnabled(onoff.isChecked());
        dgts.setEnabled(onoff.isChecked());
        //chna.setEnabled(onoff.isChecked());
        chna.setEnabled(false);
        jpan.setEnabled(onoff.isChecked());

        origin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TxtKRActivity.this,TXTActivity.class));
            }
        });


        Switch.OnCheckedChangeListener OnCheckedChangeListener = (foo,bar) -> {
            switch (foo.getId()) {
                case R.id.bill:
                    prefs.edit().putBoolean("bill",foo.isChecked()).apply();
                     break;

                case R.id.dgts:
                    prefs.edit().putBoolean("dgts",foo.isChecked()).apply();
                    break;

                case R.id.chna:
                    prefs.edit().putBoolean("chna",foo.isChecked()).apply();
                    break;

                case R.id.jpan:
                    prefs.edit().putBoolean("jpan",foo.isChecked()).apply();
                    break;

                    default:
                        break;
            }
        };

        bill.setOnCheckedChangeListener(OnCheckedChangeListener);
        dgts.setOnCheckedChangeListener(OnCheckedChangeListener);
        chna.setOnCheckedChangeListener(OnCheckedChangeListener);
        jpan.setOnCheckedChangeListener(OnCheckedChangeListener);
    }

    public static boolean isContainedInAccessbility(Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceList = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        return serviceList.toString().contains(context.getPackageName());
    }
}
