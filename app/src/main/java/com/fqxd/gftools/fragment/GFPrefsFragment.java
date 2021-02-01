package com.fqxd.gftools.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.fqxd.gftools.features.icon.IconChangeActivity;
import com.fqxd.gftools.features.txtkr.TxtKrPatchActivity;
import com.fqxd.gftools.implement.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;
import com.fqxd.gftools.features.cen.CenActivity;
import com.fqxd.gftools.features.decom.DecActivity;
import com.fqxd.gftools.features.proxy.ProxyActivity;
import com.fqxd.gftools.features.rotation.RotationActivity;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;

public class GFPrefsFragment extends PreferenceFragmentCompat {
    FragmentActivity mContext;
    String Package;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof FragmentActivity) mContext = (FragmentActivity) context;
        else throw new RuntimeException("Can't context instanceof activity!");
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.gf_prefs, rootKey);
        Package = getArguments().getString("Package");

        Preference BAD = findPreference("Button_BAD");
        Preference BRT = findPreference("Button_BRT");
        Preference BDT = findPreference("Button_BDT");
        Preference TXT = findPreference("Button_TXT");
        Preference SZE = findPreference("TextView_SIZE");
        Preference BSZ = findPreference("TextView_BSIZE");

        TXT.setVisible(Package.equals(getString(R.string.target_cn_bili)));
        Thread T1 = new Thread(() -> {
            if (new File(Global.Storage + "/GF_Tool/backup/" + Package + "/").exists()) {
                changeVisibility(BAD, false);
                changeVisibility(BRT, true);
                changeVisibility(BDT, true);
                changeVisibility(BSZ, true);

                String text = "백업 크기 : 약 " + String.format(Locale.getDefault(), "%.2f", (float) (FileUtils.sizeOfDirectory(new File(Global.Storage + "/GF_Tool/backup/" + Package + "/"))) / 1073741824) + "GB";
                changeSummary(BSZ, text);
            } else {
                changeVisibility(BAD, true);
                changeVisibility(BRT, false);
                changeVisibility(BDT, false);
                changeVisibility(BSZ, false);
            }
        });

        Thread T2 = new Thread(() -> {
            try {
                File DATA = new File(Global.Storage + "/Android/data/" + Package);
                if (DATA.exists()) {
                    String text = "데이터 크기 : 약 " + String.format(Locale.getDefault(), "%.2f", (float) (FileUtils.sizeOfDirectory(DATA)) / 1073741824) + "GB";
                    changeSummary(SZE, text);
                } else changeSummary(SZE, "데이터 크기 : 약 0.00GB");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread mainTask = new Thread(() -> {
            while (true) {
                if(mContext == null) continue;
                T1.start();
                T2.start();

                try {
                    PackageInfo pm = mContext.getPackageManager().getPackageInfo(Package, 0);
                    long ver = Build.VERSION.SDK_INT > 28 ? pm.getLongVersionCode() : pm.versionCode;
                    String obb = "main." + ver + "." + Package + ".obb";
                    changeVisibility(findPreference("TextView_OBBNF"), !new File(Global.Storage + "/Android/obb/" + Package + "/" + obb).exists());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }

            while (true) {
                if(T1.isAlive() || T2.isAlive()) continue;
                mContext.runOnUiThread(() -> mContext.findViewById(R.id.progressbarLayout).setVisibility(View.GONE));
                break;
            }
        });
        mainTask.start();
    }

    public void changeSummary(Preference p, String s) {
       mContext.runOnUiThread(() -> p.setSummary(s));
    }

    public void changeVisibility(Preference p, Boolean b) {
        mContext.runOnUiThread(() -> p.setVisible(b));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        PackageManager pm = mContext.getPackageManager();
        switch (preference.getKey()) {
            case "Button_RUN":
                startActivity(pm.getLaunchIntentForPackage(Package));
                break;

            case "Button_DEL":
                startActivityForResult(new Intent(Intent.ACTION_DELETE).setData(Uri.parse("package:" + Package)), 5);
                break;

            case "Button_DELD":
                AlertDialog.Builder b = new AlertDialog.Builder(this.getContext());
                String path = Global.Storage + "/Android/data/" + Package;
                b.setTitle("삭제 확인");
                b.setMessage("정말로 데이터를 전부 삭제하시겠습니까?");
                b.setPositiveButton("삭제", (dialogInterface, i) -> {
                    if (!new File(path).exists())
                        Toast.makeText(mContext, "데이터를 찾을수 없습니다!", Toast.LENGTH_SHORT).show();
                    else {
                        FileUtil.deleteDir dd = new FileUtil.deleteDir(mContext, path, Package);
                        dd.execute();
                    }
                });
                b.setNegativeButton("취소", (dialogInterface, i) -> { });
                b.create().show();
                break;

            case "Button_DEC":
                startActivity(DecActivity.class);
                break;

            case "Button_ICO":
                startActivity(IconChangeActivity.class);
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

            case "Button_TXT":
                startActivity(TxtKrPatchActivity.class);
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
                a.setNegativeButton("취소", (dialogInterface, i) -> { });
                a.create().show();
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5) {
            mContext.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.prefs, GFFragment.newInstance(Package))
                    .commitNowAllowingStateLoss();
        }
    }

    void startActivity(Class<?> cls) {
        startActivity(new Intent(requireView().getContext(), cls).putExtra("pkg", Package));
    }

    void backup(String pkg) {
        if (!new File(Global.Storage + "/GF_Tool/").exists()) new File(Global.Storage + "/GF_Tool/").mkdir();
        if (!new File(Global.Storage + "/GF_Tool/backup/").exists())
            new File(Global.Storage + "/GF_Tool/backup/").mkdir();
        File dir = new File(Global.Storage + "/GF_Tool/backup/" + pkg + "/");
        if (!dir.exists()) dir.mkdir();
        File data = new File(Global.Storage + "/Android/data/" + pkg + "/");
        if (!data.exists()) {
            Toast.makeText(mContext, "백업할 데이터를 찾을수 없습니다!", Toast.LENGTH_SHORT).show();
            return;
        }
        FileUtil.copyDir cpd = new FileUtil.copyDir(getActivity(), data, dir, Package);
        cpd.execute();
    }

    void restore(String pkg) {
        File dir = new File(Global.Storage + "/GF_Tool/backup/" + pkg + "/");
        File data = new File(Global.Storage + "/Android/data/" + pkg + "/");

        if (!dir.exists())
            throw new NullPointerException(Global.Storage + "/GF_Tool/backup/" + pkg + "/\" not found!");
        if (!data.exists()) data.mkdir();
        FileUtil.copyDir cd = new FileUtil.copyDir(mContext, dir, data, Package);
        cd.execute();
        Log.i("Restore", "complete");
    }

    void delete(String pkg) {
        FileUtil.deleteDir dd = new FileUtil.deleteDir(mContext, Global.Storage + "/GF_Tool/backup/" + pkg + "/", Package);
        dd.execute();
        Log.i("Delete", "complete");
    }

    @SuppressLint("StaticFieldLeak")
    public static class FileUtil {
        public static class deleteDir extends AsyncTask<Void,Void,String> {
            Activity main;
            String dirname;
            String Package;
            ProgressDialog progressDialog;

            deleteDir(Activity main, String dirname, String Package) {
                this.main = main;
                this.dirname = dirname;
                this.Package = Package;
                progressDialog = new ProgressDialog(this.main);
            }

            @Override
            protected String doInBackground(Void... voids) {
                setDirEmpty(dirname);
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.setProgressStyle(R.style.Widget_AppCompat_ProgressBar_Horizontal);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Working...");
                progressDialog.show();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                new File(dirname).delete();
                progressDialog.dismiss();
                Toast.makeText(main, "done", Toast.LENGTH_SHORT).show();

                ((FragmentActivity) main).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.prefs, GFFragment.newInstance(Package))
                        .commitNowAllowingStateLoss();
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
            FragmentActivity main;
            File sourceF;
            File targetF;
            String Package;
            ProgressDialog progressDialog;

            copyDir(FragmentActivity main, File sourceF, File targetF, String Package) {
                this.main = main;
                this.sourceF = sourceF;
                this.targetF = targetF;
                this.progressDialog = new ProgressDialog(this.main);
                this.Package = Package;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.setProgressStyle(R.style.Widget_AppCompat_ProgressBar_Horizontal);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Working...");
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

                main.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.prefs, GFFragment.newInstance(Package))
                        .commitNowAllowingStateLoss();
            }

            static void copy(File sourceF, File targetF) {
                File[] ff = sourceF.listFiles();
                for (File file : ff) {
                    File temp = new File(targetF.getAbsolutePath() + File.separator + file.getName());
                    if (file.isDirectory()) {
                        temp.mkdir();
                        copy(file, temp);
                    } else {
                        try {
                            FileInputStream fis = new FileInputStream(file);
                            FileOutputStream fos = new FileOutputStream(temp);
                            byte[] b = new byte[4096];
                            int cnt;
                            while ((cnt = fis.read(b)) != -1) {
                                fos.write(b, 0, cnt);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
