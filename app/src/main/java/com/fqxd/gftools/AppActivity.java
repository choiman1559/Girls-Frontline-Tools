package com.fqxd.gftools;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AppActivity extends Activity {

    String packagename = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        final Button run = findViewById(R.id.Runapp);
        final Button del = findViewById(R.id.DeleteApp);
        final Button deld = findViewById(R.id.DeleteData);
        final ImageView icon = findViewById(R.id.IconView);

        final Button DBak = findViewById(R.id.DBak);
        final Button RBak = findViewById(R.id.RBak);
        final Button ABak = findViewById(R.id.ABak);

        icon.setImageResource(R.drawable.ic_icon_background);
        run.setEnabled(false);
        del.setEnabled(false);
        DBak.setEnabled(false);
        RBak.setEnabled(false);
        ABak.setEnabled(false);

        if (Build.VERSION.SDK_INT >= 26 && !this.getPackageManager().canRequestPackageInstalls()) {
            this.startActivityForResult(
                    new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse("package:" + this.getPackageName())),
                    MainActivity.REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES
            );
        }

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
        targetPackages.setOnItemSelectedListener(new OnTargetSelectedListener(this));

        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getPackageManager().getLaunchIntentForPackage(packagename);
                startActivity(intent);
            }
        });

        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DELETE);
                intent.setData(Uri.parse("package:" + packagename));
                startActivity(intent);
            }
        });

        deld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = "/sdcard/Android/data/" + packagename;
                if (!new File(path).exists())
                    Toast.makeText(getApplicationContext(), "Can't Find Data!", Toast.LENGTH_SHORT).show();
                else {
                    FileUtil.deleteDir dd = new FileUtil.deleteDir(AppActivity.this, path);
                    dd.execute();
                }
            }
        });

        ABak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backup();
            }
        });

        RBak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restore();
            }
        });

        DBak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });
    }

    void backup() {
        if (!new File("/sdcard/GF_Tool/").exists()) new File("/sdcard/GF_Tool/").mkdir();
        if (!new File("/sdcard/GF_Tool/backup/").exists())
            new File("/sdcard/GF_Tool/backup/").mkdir();
        File dir = new File("/sdcard/GF_Tool/backup/" + packagename + "/");
        if (!dir.exists()) dir.mkdir();
        File data = new File("/sdcard/Android/data/" + packagename + "/");
        if (!data.exists())
            throw new NullPointerException("\"/sdcard/Android/data" + packagename + "/\" not found!");

        FileUtil.copyDir cpd = new FileUtil.copyDir(AppActivity.this, data, dir);
        cpd.execute();
        Log.i("Backup", "complete");
    }

    void restore() {
        File dir = new File("/sdcard/GF_Tool/backup/" + packagename + "/");
        File data = new File("/sdcard/Android/data/" + packagename + "/");

        if (!dir.exists())
            throw new NullPointerException("\"/sdcard/GF_Tool/backup/" + packagename + "/\" not found!");
        if (!data.exists()) data.mkdir();
        FileUtil.copyDir cd = new FileUtil.copyDir(AppActivity.this, dir, data);
        cd.execute();
        Log.i("Restore", "complete");
    }

    void delete() {
        FileUtil.deleteDir dd = new FileUtil.deleteDir(AppActivity.this, "/sdcard/GF_Tool/backup/" + packagename + "/");
        dd.execute();
        Log.i("Delete", "complete");
    }


    public static class FileUtil {

        public static class deleteDir extends AsyncTask {

            AppActivity main;
            String dirname;
            ProgressDialog progressDialog;

            deleteDir(AppActivity main, String dirname) {
                this.main = main;
                this.dirname = dirname;
                progressDialog = new ProgressDialog(this.main);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.setProgressStyle(R.style.Widget_AppCompat_ProgressBar_Horizontal);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("working...");
                progressDialog.show();
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                setDirEmpty(dirname);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                new File(dirname).delete();
                progressDialog.dismiss();
                Toast.makeText(main, "done", Toast.LENGTH_SHORT).show();
                main.recreate();
            }

            static void setDirEmpty(String dirname) {
                String path = dirname;

                File dir = new File(path);
                File[] child = dir.listFiles();

                if (dir.exists()) {
                    for (File childfile : child) {
                        if (childfile.isDirectory()) {
                            setDirEmpty(childfile.getAbsolutePath());
                        } else childfile.delete();
                    }
                }
                dir.delete();
            }
        }

        public static class copyDir extends AsyncTask {

            AppActivity main;
            File sourceF;
            File targetF;
            ProgressDialog progressDialog;

            copyDir(AppActivity main, File sourceF, File targetF) {
                this.main = main;
                this.sourceF = sourceF;
                this.targetF = targetF;
                this.progressDialog = new ProgressDialog(this.main);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.setProgressStyle(R.style.Widget_AppCompat_ProgressBar_Horizontal);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("working...");
                progressDialog.show();
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                copy(sourceF, targetF, main.getApplicationContext());
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                progressDialog.dismiss();
                Toast.makeText(main, "done", Toast.LENGTH_SHORT).show();
                main.recreate();
            }

            static void copy(File sourceF, File targetF, Context context) {

                File[] ff = sourceF.listFiles();
                for (File file : ff) {
                    File temp = new File(targetF.getAbsolutePath() + File.separator + file.getName());
                    if (file.isDirectory()) {
                        temp.mkdir();
                        copy(file, temp, context);
                    } else {
                        FileInputStream fis = null;
                        FileOutputStream fos = null;
                        try {
                            fis = new FileInputStream(file);
                            fos = new FileOutputStream(temp);
                            byte[] b = new byte[4096];
                            int cnt = 0;
                            while ((cnt = fis.read(b)) != -1) {
                                fos.write(b, 0, cnt);
                            }
                        } catch (Exception e) {
                            ExceptionCatchClass ecc = new ExceptionCatchClass();
                            ecc.CatchException(context, e);
                            e.printStackTrace();
                        } finally {
                            try {
                                fis.close();
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }

            }
        }
    }

    final class OnTargetSelectedListener implements AdapterView.OnItemSelectedListener {
        private AppActivity main;

        OnTargetSelectedListener(AppActivity main) {
            this.main = main;
        }

        @Override
        public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {

            final Spinner spinner = findViewById(R.id.targetPackage);
            final TextView version = findViewById(R.id.version);
            final TextView appname = findViewById(R.id.appname);
            final TextView server = findViewById(R.id.server);
            final Button run = findViewById(R.id.Runapp);
            final Button del = findViewById(R.id.DeleteApp);
            final Button deld = findViewById(R.id.DeleteData);
            final Button DBak = findViewById(R.id.DBak);
            final Button RBak = findViewById(R.id.RBak);
            final Button ABak = findViewById(R.id.ABak);
            final ImageView icon = findViewById(R.id.IconView);

            if (spinner.getSelectedItem().toString() == "...") {
                icon.setImageResource(R.drawable.ic_icon_background);
                appname.setText("app name : UNKNOWN");
                version.setText("version : UNKNOWN");
                server.setText("server : UNKNOWN");
                run.setEnabled(false);
                del.setEnabled(false);
                deld.setEnabled(false);
                DBak.setEnabled(false);
                RBak.setEnabled(false);
                ABak.setEnabled(false);
            } else {
                packagename = spinner.getSelectedItem().toString();

                run.setEnabled(true);
                del.setEnabled(true);
                deld.setEnabled(true);

                if (new File("/sdcard/GF_Tool/backup/" + packagename + "/").exists()) {
                    RBak.setEnabled(true);
                    DBak.setEnabled(true);
                    ABak.setEnabled(false);
                } else {
                    RBak.setEnabled(false);
                    DBak.setEnabled(false);
                    ABak.setEnabled(true);
                }

                try {
                    PackageManager pm = getApplicationContext().getPackageManager();
                    icon.setImageDrawable(getPackageManager().getApplicationIcon(packagename));
                    appname.setText("app name : " + pm.getApplicationLabel(pm.getApplicationInfo(packagename, PackageManager.GET_META_DATA)));
                    version.setText("version : " + pm.getPackageInfo(packagename, 0).versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    ExceptionCatchClass ecc = new ExceptionCatchClass();
                    ecc.CatchException(main.getApplicationContext(), e);
                }

                if (packagename == getString(R.string.target_cn_uc) ||
                        packagename == getString(R.string.target_cn_bili) ||
                        packagename == getString(R.string.target_tw)) {
                    server.setText("server : china");
                } else if (packagename == getString(R.string.target_en))
                    server.setText("server : global");
                else if (packagename == getString(R.string.target_kr))
                    server.setText("server : korea");
                else if (packagename == getString(R.string.target_jp))
                    server.setText("server : japan");

                else server.setText("server : UNKNOWN");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
