package com.fqxd.gftools.features.proxy;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import android.provider.Settings;
import android.util.Patterns;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.fqxd.gftools.R;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class ProxyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proxy);
        String Package = getIntent().getStringExtra("pkg");
        TextView target = findViewById(R.id.target);
        try {
            PackageManager pm = this.getPackageManager();
            target.setText("target : " + pm.getApplicationLabel(pm.getApplicationInfo(Package, PackageManager.GET_META_DATA)) + " (" + Package + ")");
        } catch (PackageManager.NameNotFoundException ignored) {
            target.setText("Unknown" + " (" + Package + ")");
        }

        Switch Enabled = findViewById(R.id.proxy_toggle);
        EditText Address = findViewById(R.id.proxy_address);
        EditText Port = findViewById(R.id.proxy_port);

        try {
            JSONObject json = ProxyUtils.getJsonFromPrefs(Package, this);
            if (json != null) {
                ProxyConfig cfg = ProxyConfig.getProxyConfigFromJson(json);
                Enabled.setChecked(cfg.getEnabled());
                Address.setText(cfg.getAddress());
                Port.setText(cfg.getPort());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(Enabled.isChecked()) {
            Address.setEnabled(false);
            Port.setEnabled(false);
        }

        Enabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
                    Enabled.setChecked(false);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("특수 권한이 필요합니다").setMessage("이 기능을 사용하려면 WRITE_SECURE_SETTINGS 권한이 필요합니다");
                    builder.setPositiveButton("슈퍼유저 사용", (dialog, id) -> {
                        try {
                            Runtime.getRuntime().exec("su -c pm grant com.fqxd.gftools android.permission.WRITE_SECURE_SETTINGS");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).setNegativeButton("취소", (dialog, which) -> {
                    }).show();
                } else {
                    if (checkAccessibilityPermissions()) {
                        if(Address.getText().toString().equals("") || Port.getText().toString().equals("")) {
                            if(Address.getText().toString().equals("")) Address.setError("Input Address");
                            if(Port.getText().toString().equals("")) Port.setError("Input Port");
                            Enabled.setChecked(false);
                        } else {
                            if (Patterns.IP_ADDRESS.matcher(Address.getText()).matches()) {
                                try {
                                    ProxyConfig config = new ProxyConfig();
                                    config.setEnabled(true);
                                    config.setPackage(Package);
                                    config.setAddress(Address.getText().toString());
                                    config.setPort(Port.getText().toString());
                                    ProxyUtils.saveJsonInPrefs(ProxyConfig.getJsonFromProxyConfig(config), this);

                                    Address.setEnabled(false);
                                    Port.setEnabled(false);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Address.setError("Invalid IP Address");
                                Enabled.setChecked(false);
                            }
                        }
                    } else {
                        Enabled.setChecked(false);
                        setAccessibilityPermissions();
                    }

                }
            } else {
                try {
                    ProxyConfig config = new ProxyConfig();
                    config.setEnabled(false);
                    config.setPackage(Package);
                    config.setAddress(Address.getText().toString());
                    config.setPort(Port.getText().toString());
                    ProxyUtils.saveJsonInPrefs(ProxyConfig.getJsonFromProxyConfig(config), this);

                    Address.setEnabled(true);
                    Port.setEnabled(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Button InstallCA = findViewById(R.id.install_ca);
        InstallCA.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(this, "android.permission.PACKAGE_USAGE_STATS") != PackageManager.PERMISSION_GRANTED) {
                Enabled.setChecked(false);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("특수 권한이 필요합니다").setMessage("이 기능을 사용하려면 ROOT 권한이 필요합니다");
                builder.setPositiveButton("슈퍼유저 사용", (dialog, id) -> {
                    try {
                        Runtime.getRuntime().exec("su -c pm grant com.fqxd.gftools android.permission.PACKAGE_USAGE_STATS");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).setNegativeButton("취소", (dialog, which) -> {
                }).show();
            } else {
                Intent i = new Intent(this, CAFilePicker.class);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE,false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR,false);
                startActivityForResult(i, 5217);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 5217 && resultCode == RESULT_OK) {
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            File file = null;
            for (Uri uri: files) {
                file = Utils.getFileForUri(uri);
            }

            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Notice");
            b.setMessage("Do you want to Install?");
            File finalFile = file;
            b.setPositiveButton("Yes", (dialogInterface, i) -> mvCA(finalFile));
            b.setNegativeButton("No", (dialogInterface, i) -> finish());
            AlertDialog d = b.create();
            d.show();
        }
    }

    private void mvCA(File file) {
        try {
            Runtime.getRuntime().exec("su -c mount -o remount,rw /").waitFor();
            Runtime.getRuntime().exec("su -c cp " + file.getAbsolutePath() + " /system/etc/security/cacerts").waitFor();
            Runtime.getRuntime().exec("su -c chmod 644 /system/etc/security/cacerts/" + file.getName()).waitFor();
            Runtime.getRuntime().exec("su -c mount -o remount,ro /").waitFor();
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Waring");
            b.setMessage("You Need Reboot to apply the CA.\nAre you want to reboot?");
            b.setPositiveButton("Reboot", (dialogInterface, i) -> {
                try {
                    Runtime.getRuntime().exec("su -c reboot");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            b.setNegativeButton("Later", (dialogInterface, i) -> finish());
            AlertDialog d = b.create();
            d.show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
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