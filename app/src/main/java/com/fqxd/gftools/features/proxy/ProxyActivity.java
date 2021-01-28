package com.fqxd.gftools.features.proxy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;

import org.json.JSONArray;
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
            target.setText(String.format("target : %s (%s)", pm.getApplicationLabel(pm.getApplicationInfo(Package, PackageManager.GET_META_DATA)), Package));
        } catch (PackageManager.NameNotFoundException ignored) {
            target.setText(String.format("Unknown (%s)", Package));
        }

        SharedPreferences prefs = getSharedPreferences(Global.Prefs, MODE_PRIVATE);
        JSONArray list;
        try {
            list = new JSONArray(prefs.getString("Favorite_proxy", "{}"));
        } catch (Exception e) {
            list = new JSONArray();
        }

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch Enabled = findViewById(R.id.proxy_toggle);
        CheckBox Continuing = findViewById(R.id.proxy_continuing);
        EditText Address = findViewById(R.id.proxy_address);
        EditText Port = findViewById(R.id.proxy_port);
        ImageButton Add_Favorites = findViewById(R.id.add_favorites);
        RecyclerView Favorites = findViewById(R.id.favorites);
        FavoriteViewAdapter adapter = new FavoriteViewAdapter(list, this);

        adapter.setOnClickListener((Address1, Port1, Name1) -> {
            if (!Enabled.isChecked()) {
                Address.setText(Address1);
                Port.setText(Port1);
                Toast.makeText(this, "즐겨찾기 \"" + Name1 + "\" (으)로부터 프록시 입력됨", Toast.LENGTH_SHORT).show();
            } else Toast.makeText(this, "프록시를 비활성화 후 적용 가능합니다", Toast.LENGTH_SHORT).show();
        });

        Favorites.setAdapter(adapter);
        Favorites.setLayoutManager(new LinearLayoutManagerWrapper(this));
        Favorites.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener());

        JSONArray finalList = list;
        Add_Favorites.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_editfavoritesproxy, null, false);
            builder.setView(view);
            builder.setTitle("즐겨찾기 추가");

            EditText address_edit = view.findViewById(R.id.proxy_address);
            EditText port_edit = view.findViewById(R.id.proxy_port);
            EditText name_edit = view.findViewById(R.id.proxy_name);
            Button cancel = view.findViewById(R.id.proxy_cancel);
            Button submit = view.findViewById(R.id.proxy_submit);

            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            submit.setText("저장");
            submit.setOnClickListener(v2 -> {
                String address = address_edit.getText().toString();
                String port = port_edit.getText().toString();
                String name = name_edit.getText().toString();

                boolean Duplicate = isNameDuplicate(name, finalList);
                if (address.equals("") || port.equals("") || Integer.parseInt(port) > 65535 || name.equals("") || Duplicate) {
                    if (address.equals(""))
                        address_edit.setError("Input Address");
                    if (name.equals(""))
                        name_edit.setError("Input Name");
                    else if (Duplicate)
                        name_edit.setError("Already exists name");
                    if (port.equals(""))
                        port_edit.setError("Input Port");
                    else if (Integer.parseInt(port) > 65535)
                        port_edit.setError("Limit value is 65535");
                } else {
                    if (Patterns.IP_ADDRESS.matcher(address).matches()) {
                        try {
                            JSONObject obj = new JSONObject();
                            obj.put("name", name_edit.getText().toString());
                            obj.put("address", address_edit.getText().toString());
                            obj.put("port", port_edit.getText().toString());
                            finalList.put(obj);
                            prefs.edit().putString("Favorite_proxy", finalList.toString()).apply();
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            dialog.dismiss();
                        }
                    } else {
                        address_edit.setError("Invalid IP Address");
                    }
                }
            });
            cancel.setOnClickListener(v2 -> dialog.dismiss());
            dialog.show();
        });

        try {
            JSONObject json1 = ProxyUtils.getProxyJsonFromPrefs(Package, this);
            if (json1 != null) {
                ProxyConfig cfg = ProxyConfig.getProxyConfigFromJson(json1);
                Enabled.setChecked(cfg.getEnabled());
                Address.setText(cfg.getAddress());
                Port.setText(cfg.getPort());
                Continuing.setChecked(cfg.getContinuing());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (Enabled.isChecked()) {
            Address.setEnabled(false);
            Port.setEnabled(false);
            Continuing.setEnabled(false);
        }

        Enabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!checkSettingsPermission()) {
                    Enabled.setChecked(false);
                    Run_WRITE_SECURE_SEIINGS();
                } else {
                    if (Global.checkAccessibilityPermissions(this)) {
                        if (Address.getText().toString().equals("") || Port.getText().toString().equals("") || Integer.parseInt(Port.getText().toString()) > 65535) {
                            if (Address.getText().toString().equals(""))
                                Address.setError("Input Address");
                            if (Port.getText().toString().equals("")) Port.setError("Input Port");
                            else if (Integer.parseInt(Port.getText().toString()) > 65535)
                                Port.setError("Limit value is 65535");
                            Enabled.setChecked(false);
                        } else {
                            if (Patterns.IP_ADDRESS.matcher(Address.getText()).matches()) {
                                try {
                                    ProxyConfig config = new ProxyConfig();
                                    config.setEnabled(true);
                                    config.setPackage(Package);
                                    config.setAddress(Address.getText().toString());
                                    config.setPort(Port.getText().toString());
                                    config.setContinuing(Continuing.isChecked());
                                    ProxyUtils.saveProxyJsonInPrefs(ProxyConfig.getJsonFromProxyConfig(config), this);

                                    Address.setEnabled(false);
                                    Port.setEnabled(false);
                                    Continuing.setEnabled(false);

                                    if(Continuing.isChecked()) {
                                        ProxyUtils.setProxy(config,this);
                                    }
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
                    if(checkSettingsPermission()) {
                        if (Continuing.isChecked()) {
                            ProxyUtils.undoProxy(this);
                        }
                    }

                    ProxyConfig config = new ProxyConfig();
                    config.setEnabled(false);
                    config.setPackage(Package);
                    config.setAddress(Address.getText().toString());
                    config.setPort(Port.getText().toString());
                    config.setContinuing(Continuing.isChecked());
                    ProxyUtils.saveProxyJsonInPrefs(ProxyConfig.getJsonFromProxyConfig(config), this);

                    Address.setEnabled(true);
                    Port.setEnabled(true);
                    Continuing.setEnabled(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Enabled.setChecked(Global.checkAccessibilityPermissions(this) && Enabled.isChecked());
    }

    protected static boolean isNameDuplicate(String name, JSONArray list) {
        for (int i = 0; i < list.length(); i++) {
            try {
                JSONObject obj = list.getJSONObject(i);
                if (obj.get("name").equals(name)) return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static class LinearLayoutManagerWrapper extends LinearLayoutManager {
        public LinearLayoutManagerWrapper(Context context) {
            super(context);
        }

        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }
    }


    boolean checkSettingsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
    }

    void Run_WRITE_SECURE_SEIINGS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("특수 권한이 필요합니다").setMessage("이 기능을 사용하려면 WRITE_SECURE_SETTINGS 권한이 필요합니다");
        builder.setPositiveButton("슈퍼유저 사용", (dialog, id) -> {
            try {
                if (Global.checkRootPermission()) {
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
                b.setPositiveButton("OK", (a, i) -> {
                });
                b.create().show();
                e.printStackTrace();
            }
        });

        builder.setNegativeButton("adb 사용", (dialog, id) -> {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle("adb 사용").setMessage("1. adb와 컴퓨터를 연결합니다.\n2. 터미널(이나 cmd)에 다음과 같이 입력합니다 : \nadb shell \"pm grant com.fqxd.gftools android.permission.WRITE_SECURE_SETTINGS && am force-stop com.fqxd.gftools\"\n");
            adb.setPositiveButton("복사", (d, i) -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("명령어", "adb shell pm grant com.fqxd.gftools android.permission.WRITE_SECURE_SETTINGS && am force-stop com.fqxd.gftools");
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "클립보드에 복사됨", Toast.LENGTH_SHORT).show();
            });

            adb.setNeutralButton("취소", (d, i) -> {
            });
            AlertDialog alertDialog = adb.create();
            alertDialog.show();
        });
        builder.setNeutralButton("취소", (dialog, id) -> {
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}