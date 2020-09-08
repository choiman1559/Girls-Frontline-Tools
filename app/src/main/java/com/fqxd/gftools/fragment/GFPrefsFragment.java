package com.fqxd.gftools.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.fqxd.gftools.R;
import com.fqxd.gftools.features.CenActivity;
import com.fqxd.gftools.features.decom.DecActivity;
import com.fqxd.gftools.features.proxy.ProxyActivity;
import com.fqxd.gftools.features.rotation.RotationActivity;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;

public class GFPrefsFragment extends PreferenceFragmentCompat {
    String Package;

    GFPrefsFragment(String Package) {
        this.Package = Package;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.gf_prefs,rootKey);

        Preference BAD = findPreference("Button_BAD");
        Preference BRT = findPreference("Button_BRT");
        Preference BDT = findPreference("Button_BDT");
        Preference SZE = findPreference("TextView_SIZE");
        Preference BSZ = findPreference("TextView_BSIZE");

        File DATA = new File("/sdcard/Android/data/" + Package);
        if(DATA.exists()) SZE.setSummary("데이터 크기 : 약 " + String.format(Locale.getDefault(), "%.2f", (float)(FileUtils.sizeOfDirectory(DATA)) / 1073741824)+ "GB");
        else SZE.setSummary("데이터 크기 : 약 0GB");

        if(new File("/sdcard/GF_Tool/backup/" + Package + "/").exists()) {
            BAD.setVisible(false);
            BRT.setVisible(true);
            BDT.setVisible(true);
            BSZ.setVisible(true);
            BSZ.setSummary("백업 크기 : 약 " + String.format(Locale.getDefault(), "%.2f", (float)(FileUtils.sizeOfDirectory("/sdcard/GF_Tool/backup/" + Package + "/")) / 1073741824)+ "GB");
        } else {
            BAD.setVisible(true);
            BRT.setVisible(false);
            BDT.setVisible(false);
            BSZ.setVisible(false);
        }

        try {
            PackageInfo pm = getActivity().getPackageManager().getPackageInfo(Package, 0);
            long ver = Build.VERSION.SDK_INT > 28 ? pm.getLongVersionCode() : pm.versionCode;
            String obb = "main." + ver + "." + Package + ".obb";
            findPreference("TextView_OBBNF").setVisible(!new File("/sdcard/Android/obb/" + Package + "/" + obb).exists());
            getActivity().findViewById(R.id.progressbarLayout).setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {

        PackageManager pm = getContext().getPackageManager();
        switch (preference.getKey()) {
            case "Button_RUN":
                startActivity(pm.getLaunchIntentForPackage(Package));
                break;

            case "Button_DEL":
                startActivity(new Intent(Intent.ACTION_DELETE).setData(Uri.parse("package:" + Package)));
                break;

            case "Button_DELD":
                AlertDialog.Builder b = new AlertDialog.Builder(this.getContext());
                String path = "/sdcard/Android/data/" + Package;
                b.setTitle("삭제 확인");
                b.setMessage("정말로 데이터를 전부 삭제하시겠습니까?");
                b.setPositiveButton("삭제", (dialogInterface, i) -> {
                    if (!new File(path).exists())
                        Toast.makeText(getContext().getApplicationContext(), "Can't Find Data!", Toast.LENGTH_SHORT).show();
                    else {
                        FileUtil.deleteDir dd = new FileUtil.deleteDir(getActivity(), path);
                        dd.execute();
                    }
                });
                b.setNegativeButton("취소", (dialogInterface, i) -> {});
                b.create().show();
                break;

            case "Button_DEC":
                startActivity(DecActivity.class);
                break;

            case "Button_ROT":
                startActivity(RotationActivity.class);
                break;

            case "Button_CEN":
                startActivity(CenActivity.class);
                break;

            case "Button_PXY":
                startActivity(ProxyActivity.class);
                break;

            case "Button_BAD":
                backup(Package);
                break;

            case "Button_BRT":
                restore(Package);
                break;

            case "Button_BDT":
                AlertDialog.Builder a = new AlertDialog.Builder(this.getContext());
                a.setTitle("삭제 확인");
                a.setMessage("정말로 백업된 데이터를 전부 삭제하시겠습니까?");
                a.setPositiveButton("삭제", (dialogInterface, i) -> delete(Package));
                a.setNegativeButton("취소", (dialogInterface, i) -> {});
                a.create().show();
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    void startActivity(Class<?> cls) {
        startActivity(new Intent(requireView().getContext(), cls).putExtra("pkg", Package));
    }

    void backup(String pkg) {
        if (!new File("/sdcard/GF_Tool/").exists()) new File("/sdcard/GF_Tool/").mkdir();
        if (!new File("/sdcard/GF_Tool/backup/").exists())
            new File("/sdcard/GF_Tool/backup/").mkdir();
        File dir = new File("/sdcard/GF_Tool/backup/" + pkg + "/");
        if (!dir.exists()) dir.mkdir();
        File data = new File("/sdcard/Android/data/" + pkg + "/");
        if (!data.exists())
            throw new NullPointerException("\"/sdcard/Android/data" + pkg + "/\" not found!");

        FileUtil.copyDir cpd = new FileUtil.copyDir(getActivity(), data, dir);
        cpd.execute();
    }

    void restore(String pkg) {
        File dir = new File("/sdcard/GF_Tool/backup/" + pkg + "/");
        File data = new File("/sdcard/Android/data/" + pkg + "/");

        if (!dir.exists())
            throw new NullPointerException("\"/sdcard/GF_Tool/backup/" + pkg + "/\" not found!");
        if (!data.exists()) data.mkdir();
        FileUtil.copyDir cd = new FileUtil.copyDir(getActivity(), dir, data);
        cd.execute();
        Log.i("Restore", "complete");
    }

    void delete(String pkg) {
        FileUtil.deleteDir dd = new FileUtil.deleteDir(getActivity(), "/sdcard/GF_Tool/backup/" + pkg + "/");
        dd.execute();
        Log.i("Delete", "complete");
    }

    public static class FileUtil {
        public static class deleteDir extends AsyncTask {
            Activity main;
            String dirname;
            ProgressDialog progressDialog;

            deleteDir(Activity main, String dirname) {
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

                new Handler().post(() -> {
                    Intent intent = main.getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    main.overridePendingTransition(0, 0);
                    main.finish();

                    main.overridePendingTransition(0, 0);
                    main.startActivity(intent);
                });
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

            Activity main;
            File sourceF;
            File targetF;
            ProgressDialog progressDialog;

            copyDir(Activity main, File sourceF, File targetF) {
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
                copy(sourceF, targetF);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                progressDialog.dismiss();
                Toast.makeText(main, "done", Toast.LENGTH_SHORT).show();

                new Handler().post(() -> {
                    Intent intent = main.getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    main.overridePendingTransition(0, 0);
                    main.finish();

                    main.overridePendingTransition(0, 0);
                    main.startActivity(intent);
                });
            }

            static void copy(File sourceF, File targetF) {
                File[] ff = sourceF.listFiles();
                for (File file : ff) {
                    File temp = new File(targetF.getAbsolutePath() + File.separator + file.getName());
                    if (file.isDirectory()) {
                        temp.mkdir();
                        copy(file, temp);
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
                        } finally {
                            try {
                                fis.close();
                                fos.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }
        }
    }
}
