package com.fqxd.gftools.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fqxd.gftools.R;
import com.fqxd.gftools.features.BQMActivity;
import com.fqxd.gftools.features.CenActivity;
import com.fqxd.gftools.features.decom.DecActivity;
import com.google.android.material.snackbar.Snackbar;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class GFFragment extends Fragment {
    int LayoutMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(indexToPackage(FragmentPagerItem.getPosition(getArguments())));
        LayoutMode = intent == null ? 0 : 1;
        return inflater.inflate(LayoutMode == 1 ? R.layout.fragment_gf : R.layout.fragment_gfnone,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if(LayoutMode == 1) {
            while (true) {
                if (getActivity() != null && isAdded()) break;
            }
            super.onViewCreated(view, savedInstanceState);
            String pkg = indexToPackage(FragmentPagerItem.getPosition(getArguments()));
            PackageManager pm = view.getContext().getApplicationContext().getPackageManager();
            TextView name = view.findViewById(R.id.appname);
            TextView ver = view.findViewById(R.id.version);
            TextView pk = view.findViewById(R.id.packagename);
            ImageView icon = view.findViewById(R.id.IconView);

            Button run = view.findViewById(R.id.Runapp);
            Button del = view.findViewById(R.id.DeleteApp);
            Button deld = view.findViewById(R.id.DeleteData);
            Button DBak = view.findViewById(R.id.DBak);
            Button RBak = view.findViewById(R.id.RBak);
            Button ABak = view.findViewById(R.id.ABak);

            Button BQM = view.findViewById(R.id.BQMButton);
            Button DEC = view.findViewById(R.id.DECButton);
            Button CEN = view.findViewById(R.id.CENButton);

            if (new File("/sdcard/GF_Tool/backup/" + pkg + "/").exists()) {
                RBak.setEnabled(true);
                DBak.setEnabled(true);
                ABak.setEnabled(false);
            } else {
                RBak.setEnabled(false);
                DBak.setEnabled(false);
                ABak.setEnabled(true);
            }

            int visiblity = view.getContext().getSharedPreferences("MainActivity", Context.MODE_PRIVATE).getBoolean("debug", false) ? View.VISIBLE : View.GONE;
            BQM.setVisibility(visiblity);

            try {
                name.setText(pm.getApplicationLabel(pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA)));
                ver.setText("version : " + pm.getPackageInfo(pkg, 0).versionName);
                pk.setText("package : " + pkg);

                icon.setImageDrawable(pm.getApplicationIcon(pkg));
                new GetVersionCode().execute();
            } catch (PackageManager.NameNotFoundException ignored) {
                run.setEnabled(false);
                del.setEnabled(false);
                deld.setEnabled(false);
                DBak.setEnabled(false);
                RBak.setEnabled(false);
                ABak.setEnabled(false);

                BQM.setEnabled(false);
                DEC.setEnabled(false);
                CEN.setEnabled(false);
            }

            run.setOnClickListener(v -> {
                Intent intent = pm.getLaunchIntentForPackage(pkg);
                startActivity(intent);
            });

            del.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DELETE);
                intent.setData(Uri.parse("package:" + pkg));
                startActivity(intent);
            });

            deld.setOnClickListener(v -> {
                String path = "/sdcard/Android/data/" + pkg;
                if (!new File(path).exists())
                    Toast.makeText(view.getContext().getApplicationContext(), "Can't Find Data!", Toast.LENGTH_SHORT).show();
                else {
                    FileUtil.deleteDir dd = new FileUtil.deleteDir(getActivity(), path);
                    dd.execute();
                }
            });

            ABak.setOnClickListener(v -> backup(pkg));
            RBak.setOnClickListener(v -> restore(pkg));
            DBak.setOnClickListener(v -> delete(pkg));
            BQM.setOnClickListener(v -> startActivity(BQMActivity.class));
            DEC.setOnClickListener(v -> startActivity(DecActivity.class));
            CEN.setOnClickListener(v -> startActivity(CenActivity.class));
        }
        else ((TextView)view.findViewById(R.id.NoneMessage)).setText("Can't find package \"" + indexToPackage(FragmentPagerItem.getPosition(getArguments())) + "\"");
    }

    void startActivity(Class<?> cls) {
        startActivity(new Intent(requireView().getContext(), cls).putExtra("pkg", indexToPackage(FragmentPagerItem.getPosition(getArguments()))));
    }

    class GetVersionCode extends AsyncTask<Void, String, String> {
        @Override
        protected String doInBackground(Void... voids) {

            String pkg = indexToPackage(FragmentPagerItem.getPosition(getArguments()));
            String newVersion = null;
            try {
                Document document = Jsoup.connect("https://play.google.com/store/apps/details?id=" + pkg + "&hl=en")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get();
                if (document != null) {
                    Elements element = document.getElementsContainingOwnText("Current Version");
                    for (Element ele : element) {
                        if (ele.siblingElements() != null) {
                            Elements sibElemets = ele.siblingElements();
                            for (Element sibElemet : sibElemets) {
                                newVersion = sibElemet.text();
                            }
                        }
                    }
                }
                return newVersion;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String newV) {
            super.onPostExecute(newV);
            String pkg = indexToPackage(FragmentPagerItem.getPosition(getArguments()));
            try {
                if (newV.equals("")) return;
                PackageManager pm = getContext().getPackageManager();
                String nowV = pm.getPackageInfo(pkg, 0).versionName;

                String[] i = nowV.split("_");
                int now = Integer.parseInt(i[1]);
                String[] j = newV.split("_");
                int newi = Integer.parseInt(j[1]);

                if (!newV.isEmpty() && now < newi) {
                    Snackbar.make(getView(), pm.getApplicationLabel(pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA)) + "의 새 업데이트가 있습니다!", Snackbar.LENGTH_LONG)
                            .setAction("업데이트", v -> {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg)));
                            }).show();
                }
            } catch (PackageManager.NameNotFoundException ignore) {
            }
        }
    }

    String indexToPackage(int index) {
        String i;
        switch (index) {
            case 1:
                i = "kr.txwy.and.snqx";
                break;

            case 2:
                i = "tw.txwy.and.snqx";
                break;

            case 3:
                i = "com.digitalsky.girlsfrontline.cn.uc";
                break;

            case 4:
                i = "com.sunborn.girlsfrontline.jp";
                break;

            case 5:
                i = "com.sunborn.girlsfrontline.en";
                break;

            case 6:
                i = "com.digitalsky.girlsfrontline.cn.bili";
                break;

            default:
                return "";
        }
        return i;
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
                main.recreate();
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