package com.fqxd.gftools.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import com.fqxd.gftools.implement.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.Global;
import com.fqxd.gftools.features.xapk.OBBextrack;
import com.nononsenseapps.filepicker.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GFXapkInstallActivity extends AppCompatActivity {

    String apk;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        if(uri != null && uri.isHierarchical()) {
            File file = Utils.getFileForUri(uri);

            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Please Wait!");
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Working...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Notice");
            b.setMessage("Do you want to Install ?\nFile name : " + file.getName());
            b.setPositiveButton("Yes", (dialogInterface, i) -> new work(file).execute());
            b.setNegativeButton("No", (dialogInterface, i) -> finish());
            AlertDialog d = b.create();
            d.show();
        } else finish();
    }

    public class work extends AsyncTask<String,String,String> {
        File lol;

        public work(File lol){
            this.lol = lol;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        public void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            String btn;
            if (s.equals("true")){
                String name = readTextFile(Global.Storage + "/GF_Tool/manifest.json");
                name = name.substring(name.indexOf("\"name\":"),name.lastIndexOf("\"locales_name\":"));
                name = name.replace("\"name\":\"","");
                name = name.replace("\",","");
                s = "Now, you need to Install"+"\n"+name+" APK";
                btn = "Install";

                File n = new File(Global.Storage + "/GF_Tool/");
                File [] n1 = n.listFiles();
                for (int ii=0;ii<n1.length;ii++){
                    if (n1[ii].toString().endsWith(".apk")){
                        apk = n1[ii].toString();
                    }
                }
            }else{
                btn = "OK";
                s = "Failed!\n저장용량 접근 권한이 부여되었는지 확인 후 다시 시도하십시오!";
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(GFXapkInstallActivity.this);
            builder.setTitle("Attention!");
            builder.setMessage(s);
            final String finalBtn = btn;
            builder.setPositiveButton(btn, (dialogInterface, i) -> {
                if (finalBtn.equals("Install")){
                    File toInstall = new File(apk);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Uri apkUri = FileProvider.getUriForFile(GFXapkInstallActivity.this, BuildConfig.APPLICATION_ID + ".provider", toInstall);
                        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                        intent.setData(apkUri);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        GFXapkInstallActivity.this.startActivity(intent);
                    } else {
                        Uri apkUri = Uri.fromFile(toInstall);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        GFXapkInstallActivity.this.startActivity(intent);
                    }

                    OBBextrack my = new OBBextrack();
                    my.deleteDirectory(Global.Storage + "/GF_Tool/Android");
                    my.deleteFile(Global.Storage + "/GF_Tool/manifest.json");
                    my.deleteFile(Global.Storage + "/GF_Tool/icon.png");
                    finish();
                }
                else finish();
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            OBBextrack my = new OBBextrack();
            my.deleteDirectory(Global.Storage + "/GF_Tool");

            File file = new File(Global.Storage + "/GF_Tool/");
            if (!file.exists()){
                file.mkdir();
            }
            boolean b = my.unZip(lol.toString(),file.toString());
            try {
                copyDirectory(new File(Global.Storage + "/GF_Tool/Android"),new File(Global.Storage + "/Android/"));
            } catch (IOException ignored) { }

            return String.valueOf(b);
        }
    }

    public static void copyDirectory(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {

                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }

    }


    public String readTextFile(String path) {
        File file = new File(path);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException ignored) { }
        return text.toString();
    }
}
