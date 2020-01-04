package com.fqxd.gftools;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class BQMActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bqm);

        Button BrenMK = findViewById(R.id.BrenMK);
        Button PSG1 = findViewById(R.id.PSG1);
        Button Welrod_S = findViewById(R.id.Welrod);

        Button APS = findViewById(R.id.APS);
        Button C96_M = findViewById(R.id.C96);
        Button M14 = findViewById(R.id.M14);
        Button HK416 = findViewById(R.id.HK416);
        Button M1891 = findViewById(R.id.M1891);
        Button M1895 = findViewById(R.id.M1895);
        Button M1918 = findViewById(R.id.M1918);
        Button MP5_M = findViewById(R.id.MP5);
        Button NTW20 = findViewById(R.id.NTW20);
        Button UMP9 = findViewById(R.id.UMP9);
        Button UMP45 = findViewById(R.id.UMP45);

        Button C96_C = findViewById(R.id.C96_);
        Button SV98 = findViewById(R.id.SV98);
        Button MP5_C = findViewById(R.id.MP5_);

        Button StenMK2 = findViewById(R.id.StenMK2);
        Button Welrod_O = findViewById(R.id.Welrod_);
        Button dorm = findViewById(R.id.dorm);

        setEnable(false);

        ArrayList<String> packageNames = new ArrayList<>();
        packageNames.add(getString(R.string.target_cn_uc));
        packageNames.add(getString(R.string.target_cn_bili));
        packageNames.add(getString(R.string.target_en));
        packageNames.add(getString(R.string.target_jp));
        packageNames.add(getString(R.string.target_tw));
        packageNames.add(getString(R.string.target_kr));

        ArrayList<String> p2 = new ArrayList<>();
        p2.add("...");
        for (String s : packageNames) {
            try {
                this.getPackageManager().getPackageInfo(s, 0);
                p2.add(s);

            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        ArrayAdapter<String> packages = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, p2);
        Spinner targetPackages = this.findViewById(R.id.targetPackage);
        packages.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        targetPackages.setAdapter(packages);
        targetPackages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {
                if(targetPackages.getSelectedItem().toString() != "...") setEnable(true);
                else setEnable(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        Button.OnClickListener onClickListener = v -> {
            String pk = targetPackages.getSelectedItem().toString();
            switch (v.getId()) {

                case R.id.PSG1:
                    progress("silent","PSG1",pk);
                    break;

                case R.id.BrenMK:
                    progress("silent","BrenMK",pk);
                    break;

                case R.id.Welrod:
                    progress("silent","Welrod",pk);
                    break;



                case R.id.APS:
                    progress("mod","APSMod",pk);
                    break;

                case R.id.C96:
                    progress("mod","C96Mod",pk);
                    break;

                case R.id.M14:
                    progress("mod","M14Mod",pk);
                    break;

                case R.id.HK416:
                    progress("mod","HK416Mod",pk);
                    break;

                case R.id.M1891:
                    progress("mod","M1891Mod",pk);
                    break;

                case R.id.M1895:
                    progress("mod","M1895Mod",pk);
                    break;

                case R.id.M1918:
                    progress("mod","M1918Mod",pk);
                    break;

                case R.id.MP5:
                    progress("mod","MP5Mod",pk);
                    break;

                case R.id.NTW20:
                    progress("mod","NTW20Mod",pk);
                    break;

                case R.id.UMP9:
                    progress("mod","UMP9Mod",pk);
                    break;

                case R.id.UMP45:
                    progress("mod","UMP45Mod",pk);
                    break;



                case R.id.C96_:
                    progress("cbt","C96",pk);
                    break;

                case R.id.SV98:
                    progress("cbt","SV98",pk);
                    break;

                case R.id.MP5_:
                    progress("cbt","MP5",pk);
                    break;



                case R.id.Welrod_:
                    progress("other","Welrod_",pk);
                    break;

                case R.id.StenMK2:
                    progress("other","StenMK2",pk);
                    break;

                case R.id.dorm:
                    progress("other","dorm",pk);
                    break;
            }
        };

        BrenMK.setOnClickListener(onClickListener);
        Welrod_S.setOnClickListener(onClickListener);
        PSG1.setOnClickListener(onClickListener);

        APS.setOnClickListener(onClickListener);
        C96_M.setOnClickListener(onClickListener);
        M14.setOnClickListener(onClickListener);
        HK416.setOnClickListener(onClickListener);
        M1891.setOnClickListener(onClickListener);
        M1895.setOnClickListener(onClickListener);
        M1918.setOnClickListener(onClickListener);
        MP5_M.setOnClickListener(onClickListener);
        NTW20.setOnClickListener(onClickListener);
        UMP9.setOnClickListener(onClickListener);
        UMP45.setOnClickListener(onClickListener);

        C96_C.setOnClickListener(onClickListener);
        SV98.setOnClickListener(onClickListener);
        MP5_C.setOnClickListener(onClickListener);

        StenMK2.setOnClickListener(onClickListener);
        Welrod_O.setOnClickListener(onClickListener);
        dorm.setOnClickListener(onClickListener);

    }

    void progress(String type,String name,String packagename) {

        if(!new File("/sdcard/XAPK_Installer").exists()) new File("/sdcard/XAPK_Installer").mkdir();
        if(!new File("/sdcard/XAPK_Installer/BQM").exists()) new File("/sdcard/XAPK_Installer/BQM").mkdir();

        File dir = new File("/sdcard/XAPK_Installer/BQM/" + type + "/");
        if(!dir.exists()) dir.mkdir();
        String moveto = "/sdcard/Android/data/" + packagename + "/files/Android/New/";
        File asset = new File("/sdcard/XAPK_Installer/BQM/" + type + "/" + name + ".acb.bytes");

        if(!asset.exists()) {
            if(!isOnline()) {
                Toast.makeText(getApplicationContext(), "Check Internet and Try again!", Toast.LENGTH_SHORT).show();
            } else {
                String u = "https://github.com/choiman1559/Girls-Frontline-Tools/raw/master/BQM/" + type + "/" + name + ".acb.bytes";
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(u));
                request.setDescription("downloading " + name + "'s audio asset...");
                request.setTitle(name + ".acb.bytes");
                request.setVisibleInDownloadsUi(false);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                request.allowScanningByMediaScanner();
                request.setDestinationInExternalPublicDir("/XAPK_Installer/BQM/" + type + "/",name + ".acb.bytes");

                DownloadManager manager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
                manager.enqueue(request);

                if(DownloadManager.STATUS_SUCCESSFUL == 8) {
                    if(new File(moveto).exists()) {
                        new File(moveto + name + ".acb.bytes").delete();
                        copy(asset.getPath(),moveto,name);
                    }
                    Toast.makeText(getApplicationContext(), "task completed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Check Internet and Try again!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else {
            if(new File(moveto).exists()) {
                boolean a = new File(moveto + name + ".acb.bytes").delete();
                copy(asset.getPath(),moveto,name);
                Toast.makeText(getApplicationContext(), "task completed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void copy(String from,String to,String name){
        File file = new File(from);
        try {
            FileInputStream inputStream = new FileInputStream(file);
            FileOutputStream outputStream = new FileOutputStream(to + name + ".acb.bytes");

            FileChannel fcin = inputStream.getChannel();
            FileChannel fcout = outputStream.getChannel();

            long size = fcin.size();
            fcin.transferTo(0, size, fcout);

            fcout.close();
            fcin.close();

            outputStream.close();
            inputStream.close();
        }catch (IOException e) { }
    }

    void setEnable(boolean trueorfalse){
        Button BrenMK = findViewById(R.id.BrenMK);
        BrenMK.setEnabled(trueorfalse);
        Button PSG1 = findViewById(R.id.PSG1);
        PSG1.setEnabled(trueorfalse);
        Button Welrod_S = findViewById(R.id.Welrod);
        Welrod_S.setEnabled(trueorfalse);

        Button APS = findViewById(R.id.APS);
        APS.setEnabled(trueorfalse);
        Button C96_M = findViewById(R.id.C96);
        C96_M.setEnabled(trueorfalse);
        Button M14 = findViewById(R.id.M14);
        M14.setEnabled(trueorfalse);
        Button HK416 = findViewById(R.id.HK416);
        HK416.setEnabled(trueorfalse);
        Button M1891 = findViewById(R.id.M1891);
        M1891.setEnabled(trueorfalse);
        Button M1895 = findViewById(R.id.M1895);
        M1895.setEnabled(trueorfalse);
        Button M1918 = findViewById(R.id.M1918);
        M1918.setEnabled(trueorfalse);
        Button MP5_M = findViewById(R.id.MP5);
        MP5_M.setEnabled(trueorfalse);
        Button NTW20 = findViewById(R.id.NTW20);
        NTW20.setEnabled(trueorfalse);
        Button UMP9 = findViewById(R.id.UMP9);
        UMP9.setEnabled(trueorfalse);
        Button UMP45 = findViewById(R.id.UMP45);
        UMP45.setEnabled(trueorfalse);

        Button C96_C = findViewById(R.id.C96_);
        C96_C.setEnabled(trueorfalse);
        Button SV98 = findViewById(R.id.SV98);
        SV98.setEnabled(trueorfalse);
        Button MP5_C = findViewById(R.id.MP5_);
        MP5_C.setEnabled(trueorfalse);

        Button StenMK2 = findViewById(R.id.StenMK2);
        StenMK2.setEnabled(trueorfalse);
        Button Welrod_O = findViewById(R.id.Welrod_);
        Welrod_O.setEnabled(trueorfalse);
        Button dorm = findViewById(R.id.dorm);
        dorm.setEnabled(trueorfalse);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        }
        return false;
    }
}
