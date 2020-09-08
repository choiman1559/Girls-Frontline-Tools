package com.fqxd.gftools.features;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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
import java.io.InputStreamReader;

public class CenActivity extends AppCompatActivity {
    String Package;
    boolean isData;
    boolean isCened;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cen);

        findViewById(R.id.progressbarLayout).setVisibility(View.GONE);
        this.Package = getIntent().getStringExtra("pkg");
        PackageManager pm = getPackageManager();
        TextView pkgInfo = findViewById(R.id.PkgInfo);

        isData = false;
        isCened = true;
        int CenValue = -1;

        try {
            if (!Global.checkRootPermission()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("특수 권한이 필요합니다").setMessage("이 기능을 사용하려면 ROOT 권한이 필요합니다");
                builder.setPositiveButton("슈퍼유저 사용", (dialog, id) -> {
                    try {
                        Runtime.getRuntime().exec("su").waitFor();
                        recreate();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }).setNegativeButton("취소", (dialog, which) -> finish()).show();
            } else {
                isData = isDataAvailable();
                CenValue = checkDatabase();
                if (isData) isCened = CenValue == 1;
                else {
                    AlertDialog.Builder ab = new AlertDialog.Builder(this);
                    ab.setTitle("데이터를 읽어오던중 에러 발생!");
                    ab.setMessage("소전의 데이터를 다운로드 받은 후 다시 시도하세요.");
                    ab.setPositiveButton("OK", ((dialog, which) -> { }));
                    ab.show();
                }
            }
            pkgInfo.setText("target : " + pm.getApplicationLabel(pm.getApplicationInfo(Package, PackageManager.GET_META_DATA)) + " (" + Package + ")");
        } catch (PackageManager.NameNotFoundException | InterruptedException | IOException e) {
            e.printStackTrace();
        }

        final TextView status = findViewById(R.id.status);
        if (!isData) {
            status.setTextColor(Color.parseColor("#78909C"));
            status.setText("데이터 파일 없음");
        } else {
            if (isCened) {
                status.setTextColor(Color.parseColor("#F44336"));
                status.setText("검열 적용됨");
            } else if(CenValue != -1){
                status.setTextColor(Color.parseColor("#448AFF"));
                status.setText("검열 해제됨");
            }
            else {
                status.setTextColor(Color.parseColor("#78909C"));
                status.setText("데이터 항목 없음");
                isData = false;
            }
        }

        final Button CEN = findViewById(R.id.centask);
        CEN.setText(isData ? (isCened ? "검열 해제" : "검열 재적용") : "새로 고침");
        CEN.setOnClickListener(v -> {
            if (isData) checkRootAndRunTask(isCened);
            else recreate();
        });
    }

    int checkDatabase() throws IOException, InterruptedException {
        if (!new File("/sdcard/GF_Tool/").exists()) new File("/sdcard/GF_Tool/").mkdir();
        else if (new File("/sdcard/GF_Tool/newXML.xml").exists())
            new File("/sdcard/GF_Tool/newXML.xml").delete();

        Process p = Runtime.getRuntime().exec("su");
        DataOutputStream dos = new DataOutputStream(p.getOutputStream());
        dos.writeBytes("mount -o remount,rw /data\n");
        dos.writeBytes("cp -f /data/data/" + Package + "/shared_prefs/" + Package + ".v2.playerprefs.xml /sdcard/GF_Tool/\n");
        dos.writeBytes("exit\n");
        dos.flush();
        dos.close();
        p.waitFor();

        File Xml = new File("/sdcard/GF_Tool/" + Package + ".v2.playerprefs.xml");
        while (true) {
            if (Xml.exists()) break;
        }
        String line;

        BufferedReader bufferedReader = new BufferedReader(new FileReader(Xml));
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("Normal")) {
                if (line.contains("value=\"1\"")) {
                    Xml.delete();
                    return 0;
                } else if (line.contains("value=\"0\"")) {
                    Xml.delete();
                    return 1;
                }
            }
        }
        Xml.delete();
        return -1;
    }

    void checkRootAndRunTask(boolean istrue) {
        try {
            if (isDataAvailable()) {
                findViewById(R.id.progressbarLayout).setVisibility(View.VISIBLE);
                editTask(istrue);
            } else {
                AlertDialog.Builder ab = new AlertDialog.Builder(this);
                ab.setTitle("데이터 복사 에러!");
                ab.setMessage("소전의 데이터를 다운로드 받은 후 다시 시도하세요.");
                ab.setPositiveButton("OK", ((dialog, which) -> {
                    this.finish();
                }));
                ab.show();
            }
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error!").setMessage("Can't get Root permission! Please check if su is installed on your device and try again!");
            builder.setPositiveButton("OK", (dialog, id) -> {
            });
            builder.create().show();
            e.printStackTrace();
        }
    }

    boolean isDataAvailable() throws InterruptedException, IOException {
        Process process = Runtime.getRuntime().exec("su -c find /data/data/" + Package + "/shared_prefs/" + Package + ".v2.playerprefs.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        boolean value = false;
        while ((line = reader.readLine()) != null) {
            if (line.contains("/data/data/" + Package + "/shared_prefs/" + Package + ".v2.playerprefs.xml"))
                value = true;
        }
        process.waitFor();
        return value;
    }

    void editTask(boolean istrue) {
        try {
            if (!new File("/sdcard/GF_Tool/").exists()) new File("/sdcard/GF_Tool/").mkdir();
            else if (new File("/sdcard/GF_Tool/newXML.xml").exists())
                new File("/sdcard/GF_Tool/newXML.xml").delete();

            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes("mount -o remount,rw /data\n");
            dos.writeBytes("cp -f /data/data/" + Package + "/shared_prefs/" + Package + ".v2.playerprefs.xml /sdcard/GF_Tool/\n");
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            p.waitFor();

            Xmledit(istrue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void copyXml() {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes("mount -o remount,rw /data\n");
            dos.writeBytes("cp -fp /sdcard/GF_Tool/" + Package + ".v2.playerprefs.xml /data/data/" + Package + "/shared_prefs/\n");
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            p.waitFor();

            new File("/sdcard/GF_Tool/" + Package + ".v2.playerprefs.xml").delete();
            findViewById(R.id.progressbarLayout).setVisibility(View.GONE);
            recreate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void Xmledit(boolean istrue) {
        final int cen = istrue ? 1 : 0;

        File newXml = new File("/sdcard/GF_Tool/newXML.xml");
        File Xml = new File("/sdcard/GF_Tool/" + Package + ".v2.playerprefs.xml");

        String line;
        try {
            while (true) {
                if (Xml.exists()) break;
            }

            FileWriter Xmlwrite = new FileWriter(newXml);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(Xml));

            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("Normal")) {
                    Xmlwrite.append("    <int name=\"Normal\" value=\"" + cen + "\" />\n");
                    Xmlwrite.flush();
                    continue;
                } else Xmlwrite.append(line + "\n");
                Xmlwrite.flush();
            }

            bufferedReader.close();
            Xmlwrite.close();

            Xml.delete();
            newXml.renameTo(Xml);
            copyXml();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
