package com.fqxd.gftools.features.xapk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.fqxd.gftools.implement.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;

import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.global.Global;
import com.fqxd.gftools.R;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressLint("StaticFieldLeak")
public class XapkActivity extends AppCompatActivity {

    String apk;
    ProgressDialog progressDialog;
    public static Activity activity;
    ActivityResultLauncher<Intent> startApkInstallComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        if (checkPermissions()) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/octet-stream");

            ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Please Wait!");
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Working...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent resultIntent = result.getData();
                    if (resultIntent != null) {
                        Uri uri = resultIntent.getData();
                        if (uri == null | (uri != null && !uri.getPath().toLowerCase().contains(".xapk"))) {
                            AlertDialog.Builder b = new AlertDialog.Builder(XapkActivity.this);
                            b.setTitle("Error!");
                            b.setMessage("File not selected or file is not xapk file!");
                            b.setPositiveButton("close", (dialog, which) -> finish());
                            AlertDialog d = b.create();
                            d.show();
                        } else {
                            AlertDialog.Builder b = new AlertDialog.Builder(XapkActivity.this);
                            b.setTitle("Notice");
                            b.setMessage("Do you want to Install ?");
                            b.setPositiveButton("Yes", (dialogInterface, i) -> new work(new File(Objects.requireNonNull(FilePathUtil.getPath(this, uri)))).execute());
                            b.setNegativeButton("No", (dialogInterface, i) -> finish());
                            b.setOnCancelListener(dialog -> finish());
                            AlertDialog d = b.create();
                            d.show();
                        }
                    }
                }
            });

            startApkInstallComplete = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        copyDirectory(new File(Global.Storage + "/GF_Tool/xapk/Android/obb"), new File(Global.Storage + "/Android/obb"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                OBBextrack my = new OBBextrack();
                my.deleteDirectory(Global.Storage + "/GF_Tool/xapk/");
                finish();
            });

            startActivityResult.launch(intent);
        } else {
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
                    (new String[0]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            Log.d("TAG", "Permission " + "" + false);
            return false;
        }
        Log.d("Permission", "Permission " + "\n" + true);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public class work extends AsyncTask<String, String, String> {
        File lol;

        public work(File lol) {
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
            if (s.equals("true")) {
                String name = readTextFile(Global.Storage + "/GF_Tool/xapk/manifest.json");
                name = name.substring(name.indexOf("\"name\":"), name.lastIndexOf("\"locales_name\":"));
                name = name.replace("\"name\":\"", "");
                name = name.replace("\",", "");
                s = "Now, you need to Install" + "\n" + name + " APK";
                btn = "Install";

                File n = new File(Global.Storage + "/GF_Tool/xapk/");
                File[] n1 = n.listFiles();
                assert n1 != null;
                for (File file : n1) {
                    if (file.toString().endsWith(".apk")) {
                        apk = file.toString();
                    }
                }
            } else {
                btn = "OK";
                s = "Failed!";
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(XapkActivity.this);
            builder.setTitle("Attention!");
            builder.setMessage(s);
            final String finalBtn = btn;
            builder.setPositiveButton(btn, (dialogInterface, i) -> {
                if (finalBtn.equals("Install")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    File originalApk = new File(apk);
                    Uri uri = Build.VERSION.SDK_INT <= 23 ? Uri.fromFile(originalApk) : FileProvider.getUriForFile(XapkActivity.this, BuildConfig.APPLICATION_ID + ".provider", originalApk);
                    if (Build.VERSION.SDK_INT <= 23) {
                        intent.setDataAndType(uri, "application/vnd.android.package-archive");
                    } else {
                        intent.setData(uri);
                    }

                    intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startApkInstallComplete.launch(intent);

                    AlertDialog.Builder builders = new AlertDialog.Builder(XapkActivity.this);
                    builders.setTitle("Cleaning up...");
                    builders.setMessage("Please wait until the task is finished...");
                    builders.setCancelable(false);
                    builders.show();
                } else finish();
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            OBBextrack my = new OBBextrack();
            my.deleteDirectory(Global.Storage + "/GF_Tool");

            File file = new File(Global.Storage + "/GF_Tool/xapk/");
            if (!file.exists()) {
                file.mkdir();
            }
            boolean b = my.unZip(lol.toString(), file.toString());
            return String.valueOf(b);
        }
    }

    public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) targetLocation.mkdir();
            String[] children = sourceLocation.list();
            for (int i = 0; i < Objects.requireNonNull(sourceLocation.listFiles()).length; i++) {
                assert children != null;
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else FileUtils.copyFile(sourceLocation, targetLocation);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }
}