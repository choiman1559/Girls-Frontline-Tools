package com.fqxd.gftools.features;

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

import com.fqxd.gftools.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CenActivity extends AppCompatActivity {
    String pacname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cen);
        Button runpatch = findViewById(R.id.centrue);
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

            } catch (PackageManager.NameNotFoundException e) { }
        }
        ArrayAdapter<String> packages = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, p2);
        Spinner targetPackages = this.findViewById(R.id.targetPackage);
        packages.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        targetPackages.setAdapter(packages);
        targetPackages.setOnItemSelectedListener(new CenActivity.OnTargetSelectedListener(this));

        final Button CEN = findViewById(R.id.centrue);
        CEN.setText("Censorship off");
        CEN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLog("=== New Task ===");
                updateLog("Task : Censorship off");
                editTask(true);
            }
        });

        final Button reCEN = findViewById(R.id.cenfalse);
        reCEN.setText("Censorship on");
        reCEN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLog("=== New Task ===");
                updateLog("Task : Censorship on");
                editTask(false);
            }
        });


    }

   void editTask(boolean istrue) {
        try {
            if(!new File("/sdcard/GF_Tool/").exists()) new File("/sdcard/GF_Tool/").mkdir();
            else if(new File("/sdcard/GF_Tool/newXML.xml").exists()) new File("/sdcard/GF_Tool/newXML.xml").delete();

            updateLog("getting su permission...");
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            updateLog("mounting /data...");
            dos.writeBytes("mount -o remount,rw /data\n");
            updateLog("copying XML file from /data/data...");
            dos.writeBytes("cp -f /data/data/" + pacname + "/shared_prefs/" + pacname + ".v2.playerprefs.xml /sdcard/GF_Tool/\n");
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();

            Xmledit(istrue);
        } catch (Exception e) { }
   }

   void copyXml(){
        try{
            updateLog("getting su permission...");
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            updateLog("mounting /data...");
            dos.writeBytes("mount -o remount,rw /data\n");
            updateLog("copying XML file from storage...");
            dos.writeBytes("cp -fp /sdcard/GF_Tool/" + pacname + ".v2.playerprefs.xml /data/data/" + pacname + "/shared_prefs/\n");
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            p.waitFor();

            updateLog("cleaning up...");
            new File("/sdcard/GF_Tool/" + pacname + ".v2.playerprefs.xml").delete();
            updateLog("Task Done!\n");

        } catch (Exception e) { }
   }

    void Xmledit(boolean istrue){
        int cen = istrue ? 1 : 0;

        updateLog("editing XML...");
        File newXml = new File("/sdcard/GF_Tool/newXML.xml");
        File Xml = new File("/sdcard/GF_Tool/" + pacname + ".v2.playerprefs.xml");

        String line = null;
        try {
            while(!Xml.exists()) { continue; }

            FileWriter Xmlwrite = new FileWriter(newXml);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(Xml));

            while ((line = bufferedReader.readLine()) != null) {
                if(line.contains("Normal")) {
                    Xmlwrite.append("    <int name=\"Normal\" value=\"" + Integer.toString(cen) + "\" />\n");
                    Xmlwrite.flush();
                    continue;
                }
                    else Xmlwrite.append(line + "\n");
                    Xmlwrite.flush();
            }

            bufferedReader.close();
            Xmlwrite.close();

            updateLog("deleting old XML file...");
            Xml.delete();
            updateLog("renameing to new Xml file...");
            newXml.renameTo(Xml);

            copyXml();

        } catch (IOException e) { }
    }

    final class OnTargetSelectedListener implements AdapterView.OnItemSelectedListener {
        private CenActivity main;
        Button CEN = findViewById(R.id.centrue);
        Button reCEN= findViewById(R.id.cenfalse);


        OnTargetSelectedListener(CenActivity main) {
            this.main = main;
        }
        final Spinner spinner = findViewById(R.id.targetPackage);

        @Override
        public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {
            if (((TextView)view).getText().equals("...")) {
                CEN.setEnabled(false);
                reCEN.setEnabled(false);

            } else {
                CEN.setEnabled(true);
                reCEN.setEnabled(true);
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
