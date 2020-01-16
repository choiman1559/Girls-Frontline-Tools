package com.fqxd.gftools.features.xapk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;

import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.ExceptionCatchClass;
import com.fqxd.gftools.MainActivity;
import com.fqxd.gftools.R;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class XapkActivity extends AppCompatActivity {

    String apk;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (checkPermissions()){
            Intent i = new Intent(this, FilePicker.class);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE,false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR,false);
            startActivityForResult(i, 5217);
        }else{
            Toast.makeText(this, "You need to Allow WRITE STORAGE Permission!", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean checkPermissions() {
        int REQUEST_ID_MULTIPLE_PERMISSIONS = 5217;
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            Log.d("TAG","Permission"+"\n"+String.valueOf(false));
            return false;
        }
        Log.d("Permission","Permission"+"\n"+String.valueOf(true));
        return true;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait!");
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Working...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        super.onActivityResult(requestCode,resultCode,intent);
        if (requestCode == 5217 && resultCode == Activity.RESULT_OK) {
            List<Uri> files = Utils.getSelectedFilesFromResult(intent);
            File file = null;
            for (Uri uri: files) {
                file = Utils.getFileForUri(uri);
            }


            AlertDialog.Builder b = new AlertDialog.Builder(XapkActivity.this);
            b.setTitle("Notice");
            b.setMessage("Do you want to Install ?");
            final File finalFile = file;
            b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new work(finalFile).execute();
                }
            });
            b.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            AlertDialog d = b.create();
            d.show();
        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    public class work extends AsyncTask<String,String,String>{
        File lol;
        public work(File lol){
            this.lol=lol;
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
            String btn = null;
            if (s.equals("true")){
                String name = readTextFile("/sdcard/GF_Tool/manifest.json");
                name = name.substring(name.indexOf("\"name\":"),name.lastIndexOf("\"locales_name\":"));
                name = name.replace("\"name\":\"","");
                name = name.replace("\",","");
                s = "Now, you need to Install"+"\n"+name+" APK";
                btn = "Install";

                File n = new File("/sdcard/GF_Tool/");
                File [] n1 = n.listFiles();
                for (int ii=0;ii<n1.length;ii++){
                    if (n1[ii].toString().endsWith(".apk")){
                        apk = n1[ii].toString();
                    }
                }
            }else{
                btn = "OK";
                s = "Failed!";
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(XapkActivity.this);
            builder.setTitle("Attention!");
            builder.setMessage(s);
            final String finalBtn = btn;
            builder.setPositiveButton(btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (finalBtn.equals("Install")){
                        File toInstall = new File(apk);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Uri apkUri = FileProvider.getUriForFile(XapkActivity.this, BuildConfig.APPLICATION_ID + ".provider", toInstall);
                            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                            intent.setData(apkUri);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            XapkActivity.this.startActivity(intent);
                        } else {
                            Uri apkUri = Uri.fromFile(toInstall);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            XapkActivity.this.startActivity(intent);
                        }

                        OBBextrack my = new OBBextrack();
                        my.deleteDirectory("/sdcard/GF_Tool/Android");
                        my.deleteFile("/sdcard/GF_Tool/manifest.json");
                        my.deleteFile("/sdcard/GF_Tool/icon.png");
                        finish();
                    }
                    else finish();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            OBBextrack my = new OBBextrack();
            my.deleteDirectory("/sdcard/GF_Tool");

            File file = new File("/sdcard/GF_Tool/");
            if (!file.exists()){
                file.mkdir();
            }
            boolean b = my.unZip(lol.toString(),file.toString());
            try {
                copyDirectory(new File("/sdcard/GF_Tool/Android"),new File("/sdcard/Android/"));
            } catch (IOException e) {
                ExceptionCatchClass ecc = new ExceptionCatchClass();
                ecc.CatchException(XapkActivity.this,e);
                e.printStackTrace();
            }

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
        catch (IOException e) {
            ExceptionCatchClass ecc = new ExceptionCatchClass();
            ecc.CatchException(XapkActivity.this,e);
        }
        return text.toString();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}