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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fqxd.gftools.global.AdHelper;
import com.fqxd.gftools.global.Global;
import com.fqxd.gftools.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.switchmaterial.SwitchMaterial;

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
        AdHelper.init(this, findViewById(R.id.parent));
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
        SwitchMaterial Enabled = findViewById(R.id.proxy_toggle);
        MaterialCheckBox Continuing = findViewById(R.id.proxy_continuing);
        EditText Address = findViewById(R.id.proxy_address);
        EditText Port = findViewById(R.id.proxy_port);
        MaterialButton Add_Favorites = findViewById(R.id.add_favorites);
        RecyclerView Favorites = findViewById(R.id.favorites);
        FavoriteViewAdapter adapter = new FavoriteViewAdapter(list, this);

        adapter.setOnClickListener((Address1, Port1, Name1) -> {
            if (!Enabled.isChecked()) {
                Address.setText(Address1);
                Port.setText(Port1);
                Toast.makeText(this, String.format(getString(R.string.Apply_FavoriteProxy_OK), Name1), Toast.LENGTH_SHORT).show();
            } else Toast.makeText(this, R.string.Apply_FavoriteProxy_Error, Toast.LENGTH_SHORT).show();
        });

        Favorites.setAdapter(adapter);
        Favorites.setLayoutManager(new LinearLayoutManagerWrapper(this));
        Favorites.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener());

        JSONArray finalList = list;
        Add_Favorites.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_editfavoritesproxy, null, false);
            builder.setView(view);
            builder.setTitle(R.string.Add_FavoriteProxy_Title);

            EditText address_edit = view.findViewById(R.id.proxy_address);
            EditText port_edit = view.findViewById(R.id.proxy_port);
            EditText name_edit = view.findViewById(R.id.proxy_name);
            Button cancel = view.findViewById(R.id.proxy_cancel);
            Button submit = view.findViewById(R.id.proxy_submit);

            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            submit.setText(R.string.Add_FavoriteProxy_OK);
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
        builder.setTitle(R.string.Apply_Button_NeedSecure_Title).setMessage(R.string.Apply_Button_NeedSecure_Content);
        builder.setPositiveButton(R.string.Apply_Button_NeedSecure_Root_OK, (dialog, id) -> {
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
                b.setTitle("Error!").setMessage(R.string.Apply_Button_NeedSecure_Root_Error);
                b.setPositiveButton(R.string.Global_OK, (a, i) -> {
                });
                b.create().show();
                e.printStackTrace();
            }
        });

        builder.setNegativeButton(R.string.Apply_Button_NeedSecure_Adb_OK, (dialog, id) -> {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle(R.string.Apply_Button_NeedSecure_Adb_OK).setMessage(R.string.Apply_Button_NeedSecure_Adb_Instrument);
            adb.setPositiveButton(R.string.Apply_Button_NeedSecure_Adb_DoCopy, (d, i) -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Command", "adb shell pm grant com.fqxd.gftools android.permission.WRITE_SECURE_SETTINGS && am force-stop com.fqxd.gftools");
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, R.string.Apply_Button_NeedSecure_Adb_DoneCopy, Toast.LENGTH_SHORT).show();
            });

            adb.setNeutralButton(R.string.Global_Cancel, (d, i) -> {
            });
            AlertDialog alertDialog = adb.create();
            alertDialog.show();
        });
        builder.setNeutralButton(R.string.Global_Cancel, (dialog, id) -> {
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}