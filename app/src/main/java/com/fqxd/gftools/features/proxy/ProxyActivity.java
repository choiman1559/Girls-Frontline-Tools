package com.fqxd.gftools.features.proxy;

import android.Manifest;
import android.app.AlertDialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.util.Patterns;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
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
                if (checkSettingsPermission()) {
                    Enabled.setChecked(false);
                    Run_WRITE_SECURE_SEIINGS();
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
                if (checkSettingsPermission()) {
                    PAC_Enabled.setChecked(false);
                    Run_WRITE_SECURE_SEIINGS();
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

        Enabled.setChecked(Global.checkAccessibilityPermissions(this) && Enabled.isChecked());
        PAC_Enabled.setChecked(Global.checkAccessibilityPermissions(this) && PAC_Enabled.isChecked());
    }

    boolean checkSettingsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED;
    }

    void Run_WRITE_SECURE_SEIINGS(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("특수 권한이 필요합니다").setMessage("이 기능을 사용하려면 WRITE_SECURE_SETTINGS 권한이 필요합니다");
        builder.setPositiveButton("슈퍼유저 사용", (dialog, id) -> {
            try {
                if(Global.checkRootPermission()) {
                    Process p = Runtime.getRuntime().exec("su");
                    DataOutputStream dos = new DataOutputStream(p.getOutputStream());
                    dos.writeBytes("pm grant com.fqxd.gftools android.permission.WRITE_SECURE_SETTINGS");
                    dos.writeBytes(" am force-stop com.fqxd.gftools");
                    dos.flush();
                    dos.close();
                    p.waitFor();
                }
            } catch (IOException | InterruptedException e) {
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setTitle("Error!").setMessage("루트 권한을 인식할수 없습니다! 기기가 루팅이 되어있는지 확인 후 다시 시도하십시오!");
                b.setPositiveButton("OK", (a, i) -> { });
                b.create().show();
                e.printStackTrace();
            }
        });

        builder.setNegativeButton("adb 사용", (dialog, id) -> {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle("adb 사용").setMessage("1. adb와 컴퓨터를 연결합니다.\n2. 터미널(이나 cmd)에 다음과 같이 입력합니다 : \nadb shell \"pm grant com.fqxd.gftools android.permission.WRITE_SECURE_SETTINGS && am force-stop com.fqxd.gftools\"\n");
            adb.setPositiveButton("복사", (d, i) -> {
                ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("명령어","adb shell pm grant com.fqxd.gftools android.permission.WRITE_SECURE_SETTINGS && am force-stop com.fqxd.gftools");
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this,"클립보드에 복사됨",Toast.LENGTH_SHORT).show();
            });

            adb.setNeutralButton("취소", (d, i) -> { });
            AlertDialog alertDialog = adb.create();
            alertDialog.show();
        });
        builder.setNeutralButton("취소", (dialog, id) -> { });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}