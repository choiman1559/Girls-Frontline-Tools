package com.fqxd.gftools;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class Cen extends AppCompatActivity {
    String pacname = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cen);
        Button runpatch = findViewById(R.id.runPatch);
        runpatch.setEnabled(false);

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
        targetPackages.setOnItemSelectedListener(new Cen.OnTargetSelectedListener(this));

        final Button CEN = findViewById(R.id.runPatch);
        CEN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeXML();
                RunCommand();
            }
        });

    }

    void makeXML() {
        File file = new File("/sdcard/XAPK_Installer/");
        if (!file.exists()) {
            file.mkdir();
        }

        File XML = new File("/sdcard/XAPK_Installer/" + pacname + ".v2.playerprefs.xml");
        if (!XML.exists()) {
            try {
                updateLog("=== New Task ===");
                updateLog("making XML file...");
                FileWriter writer = new FileWriter(XML);
                writer.append("<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n");
                writer.append("<map>\n");
                writer.append("    <int name=\"Normal\" value=\"0\" />\n");
                writer.append("</map>");
                writer.flush();
                writer.close();

            } catch (Exception e) {
            }

        } else if (file.exists()) {
            XML.delete();
            makeXML();
        }

    }

    void RunCommand() {
        try {

            updateLog("getting su permission...");
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            updateLog("mounting /data...");
            dos.writeBytes("mount -o remount,rw /data\n");
            updateLog("copying XML file...");
            dos.writeBytes("cp -fp /sdcard/XAPK_Installer/" + pacname + ".v2.playerprefs.xml /data/data/" + pacname + "/shared_prefs/\n");
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            p.waitFor();

            updateLog("cleaning up...");
            File file = new File("/sdcard/XAPK_Installer/" + pacname + ".v2.playerprefs.xml");
            file.delete();


            updateLog("done!\n");
        } catch (Exception e) {
        }
    }

    final class OnTargetSelectedListener implements AdapterView.OnItemSelectedListener {
        private Cen main;
        Button runpatch = findViewById(R.id.runPatch);


        OnTargetSelectedListener(Cen main) {
            this.main = main;
        }
        final Spinner spinner = findViewById(R.id.targetPackage);

        @Override
        public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {
            if (((TextView)view).getText().equals("...")) { runpatch.setEnabled(false); } else {
                runpatch.setEnabled(true);
                pacname = spinner.getSelectedItem().toString();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }


    private void updateLog(final String str) {
        TextView log = findViewById(R.id.log);
        log.post(() -> {
            log.append(str + "\r\n");
            Log.d("patch", str);
        });
    }
}
