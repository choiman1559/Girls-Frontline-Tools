package com.fqxd.gftools.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;
import com.fqxd.gftools.features.calculator.CalculatorActivity;
import com.fqxd.gftools.features.alarm.ui.AlarmListActivity;
import com.fqxd.gftools.features.gfd.GFDActivity;
import com.fqxd.gftools.features.gfneko.GFNekoActivity;
import com.fqxd.gftools.features.noti.NotiActivity;
import com.fqxd.gftools.features.proxy.CAFilePicker;
import com.fqxd.gftools.features.xapk.XapkActivity;
import com.fqxd.gftools.features.xduc.XDUCActivity;
import com.google.android.material.snackbar.Snackbar;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_prefs, rootKey);
        findPreference("Button_RATE").setVisible(new Random().nextInt(100) < 30);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {

        switch (preference.getKey()) {
            case "Button_XD":
                startActivity(new Intent(getContext(), XDUCActivity.class));
                break;

            case "Button_XAPK":
                startActivity(new Intent(getContext(), XapkActivity.class));
                break;

            case "Button_CAL":
                startActivity(new Intent(getContext(), CalculatorActivity.class));
                break;

            case "Button_GFDIC":
                startActivity(new Intent(getContext(), GFDActivity.class));
                break;

            case "Button_NEKO":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(getContext())) {
                        startActivity(new Intent(getContext(), GFNekoActivity.class));
                    } else {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
                        Toast.makeText(getContext(), "이 기능을 사용하려면 다른 앱 위에 그리기 기능이 필요합니다!", Toast.LENGTH_SHORT).show();
                        getContext().startActivity(intent);
                    }
                }
                break;

            case "Button_NOTI":
                if (isOffline(getContext())) {
                    Snackbar.make(getView(), "Check Internet and Try Again", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Intent intent = new Intent(getContext(), NotiActivity.class);
                    startActivity(intent);
                }
                break;

            case "Button_ALARM":
                startActivity(new Intent(getContext(), AlarmListActivity.class));
                break;

            case "Button_CA":
                try {
                    if(!Global.checkRootPermission()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("특수 권한이 필요합니다").setMessage("이 기능을 사용하려면 ROOT 권한이 필요합니다");
                        builder.setPositiveButton("슈퍼유저 사용", (dialog, id) -> {
                            try {
                                Runtime.getRuntime().exec("su");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).setNegativeButton("취소", (dialog, which) -> { }).show();
                    } else {
                        Intent i = new Intent(getContext(), CAFilePicker.class);
                        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE,false);
                        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR,false);
                        startActivityForResult(i, 5217);
                    }
                } catch (IOException | InterruptedException e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Error!").setMessage("루트 권한을 인식할수 없습니다! 기기가 루팅이 되어있는지 확인 후 다시 시도하십시오!");
                    builder.setPositiveButton("OK", (dialog, id) -> { });
                    builder.create().show();
                    e.printStackTrace();
                }
                break;

            case "Button_RATE":
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=" + getContext().getPackageName())));
                preference.setVisible(false);
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 5217 && resultCode == RESULT_OK) {
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            File file = null;
            for (Uri uri: files) {
                file = Utils.getFileForUri(uri);
            }

            AlertDialog.Builder b = new AlertDialog.Builder(getContext());
            b.setTitle("Notice");
            b.setMessage("Do you want to Install?");
            File finalFile = file;
            b.setPositiveButton("Yes", (dialogInterface, i) -> moveCA(finalFile));
            b.setNegativeButton("No", (dialogInterface, i) -> { });
            b.create().show();
        }
    }

    private void moveCA(File file) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes("mount -o remount,rw /\n");
            dos.writeBytes("cp " + file.getAbsolutePath() + " /system/etc/security/cacerts\n");
            dos.writeBytes("chmod 644 /system/etc/security/cacerts/" + file.getName() + "\n");
            dos.writeBytes("mount -o remount,ro /\n");
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            p.waitFor();

            AlertDialog.Builder b = new AlertDialog.Builder(getContext());
            b.setTitle("Waring");
            b.setMessage("You Need Reboot to apply the CA.\nAre you want to reboot?");
            b.setPositiveButton("Reboot", (dialogInterface, i) -> {
                try {
                    Runtime.getRuntime().exec("su -c reboot");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            b.setNegativeButton("Later", (dialogInterface, i) -> {});
            b.create().show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public boolean isOffline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        if (cm.getActiveNetworkInfo() != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni == null || !ni.isConnected();
        }
        return true;
    }
}
