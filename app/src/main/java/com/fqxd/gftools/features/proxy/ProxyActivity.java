package com.fqxd.gftools.features.proxy;

import android.app.AlertDialog;

import android.content.pm.PackageManager;
import android.os.Bundle;

import android.util.Patterns;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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
        Switch PAC_Enabled = findViewById(R.id.pac_proxy_toggle);
        EditText PAC_Address = findViewById(R.id.pac_proxy_address);

        try {
            JSONObject json1 = ProxyUtils.getProxyJsonFromPrefs(Package, this);
            if (json1 != null) {
                ProxyConfig cfg = ProxyConfig.getProxyConfigFromJson(json1);
                Enabled.setChecked(cfg.getEnabled());
                Address.setText(cfg.getAddress());
                Port.setText(cfg.getPort());
            }

            JSONObject json2 = ProxyUtils.getPacProxyJsonFromPrefs(Package, this);
            if (json2 != null) {
                PacProxyConfig cfg = PacProxyConfig.getProxyConfigFromJson(json2);
                PAC_Enabled.setChecked(cfg.getEnabled());
                PAC_Address.setText(cfg.getAddress());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(Enabled.isChecked()) {
            Address.setEnabled(false);
            Port.setEnabled(false);
        }
        PAC_Address.setEnabled(!PAC_Enabled.isChecked());

        Enabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                try {
                    if (!Global.checkRootPermission()) {
                        Enabled.setChecked(false);
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("특수 권한이 필요합니다").setMessage("이 기능을 사용하려면 ROOT 권한이 필요합니다");
                        builder.setPositiveButton("슈퍼유저 사용", (dialog, id) -> {
                            try {
                                Runtime.getRuntime().exec("su -c pm grant com.fqxd.gftools android.permission.WRITE_SECURE_SETTINGS");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).setNegativeButton("취소", (dialog, which) -> {
                        }).show();
                    } else {
                        if (Global.checkAccessibilityPermissions(this)) {
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
                                        ProxyUtils.saveProxyJsonInPrefs(ProxyConfig.getJsonFromProxyConfig(config), this);

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
                            Global.setAccessibilityPermissions(this);
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Error!").setMessage("Can't get Root permission! Please check if su is installed on your device and try again!");
                    builder.setPositiveButton("OK", (dialog, id) -> { });
                    builder.create().show();
                    e.printStackTrace();
                    e.printStackTrace();
                }
            } else {
                try {
                    ProxyConfig config = new ProxyConfig();
                    config.setEnabled(false);
                    config.setPackage(Package);
                    config.setAddress(Address.getText().toString());
                    config.setPort(Port.getText().toString());
                    ProxyUtils.saveProxyJsonInPrefs(ProxyConfig.getJsonFromProxyConfig(config), this);

                    Address.setEnabled(true);
                    Port.setEnabled(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        PAC_Enabled.setOnCheckedChangeListener((ButtonView,isChecked) -> {
            if (isChecked) {
                try {
                    if (!Global.checkRootPermission()) {
                        PAC_Enabled.setChecked(false);
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("특수 권한이 필요합니다").setMessage("이 기능을 사용하려면 ROOT 권한이 필요합니다");
                        builder.setPositiveButton("슈퍼유저 사용", (dialog, id) -> {
                            try {
                                Runtime.getRuntime().exec("su -c pm grant com.fqxd.gftools android.permission.WRITE_SECURE_SETTINGS");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).setNegativeButton("취소", (dialog, which) -> { }).show();
                    } else {
                        if (Global.checkAccessibilityPermissions(this)) {
                            if(PAC_Address.getText().toString().equals("")) {
                                PAC_Address.setError("Input Address");
                                PAC_Enabled.setChecked(false);
                            } else {
                                if (Patterns.WEB_URL.matcher(PAC_Address.getText()).matches()) {
                                    try {
                                        PacProxyConfig config = new PacProxyConfig();
                                        config.setEnabled(true);
                                        config.setPackage(Package);
                                        config.setAddress(PAC_Address.getText().toString());
                                        ProxyUtils.savePacProxyJsonInPrefs(PacProxyConfig.getJsonFromProxyConfig(config), this);
                                        PAC_Address.setEnabled(false);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    PAC_Address.setError("Invalid Url Address");
                                }
                            }
                        } else {
                            PAC_Enabled.setChecked(false);
                            Global.setAccessibilityPermissions(this);
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Error!").setMessage("Can't get Root permission! Please check if su is installed on your device and try again!");
                    builder.setPositiveButton("OK", (dialog, id) -> { });
                    builder.create().show();
                    e.printStackTrace();
                    e.printStackTrace();
                }
            } else {
                try {
                    PacProxyConfig config = new PacProxyConfig();
                    config.setEnabled(false);
                    config.setPackage(Package);
                    config.setAddress(PAC_Address.getText().toString());
                    ProxyUtils.savePacProxyJsonInPrefs(PacProxyConfig.getJsonFromProxyConfig(config), this);
                    PAC_Address.setEnabled(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}