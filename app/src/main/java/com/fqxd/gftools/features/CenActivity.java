package com.fqxd.gftools.features;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CenActivity extends AppCompatActivity {
    String Package;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cen);

        this.Package = getIntent().getStringExtra("pkg");
        PackageManager pm = getPackageManager();
        TextView pkgInfo = findViewById(R.id.PkgInfo);

        try {
            pkgInfo.setText("target : " + pm.getApplicationLabel(pm.getApplicationInfo(Package, PackageManager.GET_META_DATA)) + " (" + Package + ")");
        } catch (PackageManager.NameNotFoundException ignored) { }

        updateLog("task Log should appear here.");

        final Button CEN = findViewById(R.id.centrue);
        CEN.setText("Censorship off");
        CEN.setOnClickListener(v -> checkRootAndRunTask(true));

        final Button reCEN = findViewById(R.id.cenfalse);
        reCEN.setText("Censorship on");
        reCEN.setOnClickListener(v -> checkRootAndRunTask(false));
    }

    void checkRootAndRunTask(boolean istrue) {
        try {
            if(!Global.checkRootPermission()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("특수 권한이 필요합니다").setMessage("이 기능을 사용하려면 ROOT 권한이 필요합니다");
                builder.setPositiveButton("슈퍼유저 사용", (dialog, id) -> {
                    try {
                        Runtime.getRuntime().exec("su");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).setNegativeButton("취소", (dialog, which) -> { }).show();
            } else editTask(istrue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   void editTask(boolean istrue) {
       updateLog("=== New Task ===");
       updateLog("Task : Censorship " + (istrue ? "off" : "on"));

        try {
            if(!new File("/sdcard/GF_Tool/").exists()) new File("/sdcard/GF_Tool/").mkdir();
            else if(new File("/sdcard/GF_Tool/newXML.xml").exists()) new File("/sdcard/GF_Tool/newXML.xml").delete();

            updateLog("getting su permission...");
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            updateLog("mounting /data...");
            dos.writeBytes("mount -o remount,rw /data\n");
            updateLog("copying XML file from /data/data...");
            dos.writeBytes("cp -f /data/data/" + Package + "/shared_prefs/" + Package + ".v2.playerprefs.xml /sdcard/GF_Tool/\n");
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();

            Xmledit(istrue);
        } catch (Exception ignored) { }
   }

   void copyXml(){
        try{
            updateLog("getting su permission...");
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            updateLog("mounting /data...");
            dos.writeBytes("mount -o remount,rw /data\n");
            updateLog("copying XML file from storage...");
            dos.writeBytes("cp -fp /sdcard/GF_Tool/" + Package + ".v2.playerprefs.xml /data/data/" + Package + "/shared_prefs/\n");
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            p.waitFor();

            updateLog("cleaning up...");
            new File("/sdcard/GF_Tool/" + Package + ".v2.playerprefs.xml").delete();
            updateLog("Task Done!\n");

        } catch (Exception ignored) { }
   }

    void Xmledit(boolean istrue){
        int cen = istrue ? 1 : 0;

        updateLog("editing XML...");
        File newXml = new File("/sdcard/GF_Tool/newXML.xml");
        File Xml = new File("/sdcard/GF_Tool/" + Package + ".v2.playerprefs.xml");

        String line;
        try {
            while(true) { if(Xml.exists()) break; }

            FileWriter Xmlwrite = new FileWriter(newXml);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(Xml));

            while ((line = bufferedReader.readLine()) != null) {
                if(line.contains("Normal")) {
                    Xmlwrite.append("    <int name=\"Normal\" value=\"" + cen + "\" />\n");
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

        } catch (IOException ignored) { }
    }


    private void updateLog(final String str) {
        TextView log = findViewById(R.id.log);
        log.post(() -> {
            log.append(str + "\r\n");
            Log.d("patch", str);
        });
    }
}
