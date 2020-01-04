package com.fqxd.gftools;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionMethod;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public final class ICCActivity extends Activity {

    @Override
    public void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_icc);

        if (Build.VERSION.SDK_INT >= 23 && this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return;
        }
        if (Build.VERSION.SDK_INT >= 26 && !this.getPackageManager().canRequestPackageInstalls()) {
            this.startActivityForResult(
                    new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse("package:" + this.getPackageName())),
                    MainActivity.REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES
            );
        }
        TextView version = this.findViewById(R.id.version);
        try {
            version.setText(this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {}
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PatchTask.REQUEST_INSTALL) {
            new File(this.getExternalFilesDir(null).getAbsolutePath() + "/base.apk").delete();
        }
        if (requestCode == MainActivity.REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES) {
            if (Build.VERSION.SDK_INT < 26 || resultCode == Activity.RESULT_OK) {
                this.finish();
                this.startActivity(this.getIntent());
            } else {
                this.startActivityForResult(
                        new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse("package:" + this.getPackageName())),
                        MainActivity.REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.finish();
            this.startActivity(this.getIntent());
        }
        ActivityCompat.requestPermissions(this, permissions, 0);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
}

final class PatchTask extends AsyncTask {
    public static final int REQUEST_INSTALL = 0x00;
    private static final int REQUEST_CODE = 0;
    private ICCActivity main;
    private TextView status;
    private TextView log;
    private ProgressBar progress;
    private String target;
    PatchTask(ICCActivity main, TextView status, TextView log, ProgressBar progress, String target) {
        this.main = main;
        this.status = status;
        this.log = log;
        this.progress = progress;
        this.target = target;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            final ProgressBar p = this.main.findViewById(R.id.running);
            p.post(new Runnable() {
                @Override
                public void run() {
                    p.setVisibility(View.VISIBLE);
                }
            });
            this.updateLog(this.main.getString(R.string.info_patch_started));
            this.updateLog("Target: " + this.target);
            File temp = this.main.getExternalFilesDir(null);
            FileUtils.deleteDirectory(temp);
            temp.mkdir();
            this.updateStatus("extracting obb");
            final File obb = new File(temp.getAbsolutePath() + "/obb");
            obb.mkdir();
            File originalObb = (new File(Environment.getExternalStorageDirectory() + "/Android/obb/" + this.target)).listFiles()[0];
            ZipFile zipFile = new ZipFile(originalObb);
            zipFile.extractAll(obb.getAbsolutePath());
            if (obb.listFiles() == null) {
                this.updateLog("unknown error while extracting obb");
            }
            this.updateLog("obb extracted");
            this.updateProgress(25);
            this.updateStatus("patching obb");
            zipFile = new ZipFile(originalObb);
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(CompressionMethod.STORE);
            parameters.setOverrideExistingFilesInZip(true);
            parameters.setEncryptFiles(false);
            originalObb.delete();
            for (File f : obb.listFiles()) {
                if (f.isFile()) {
                    zipFile.addFile(f, parameters);
                } else {
                    zipFile.addFolder(f, parameters);
                }
            }
            this.updateLog("patched obb");
            this.updateStatus("cleaning up...");
            FileUtils.deleteDirectory(obb);
            this.updateProgress(50);
            this.updateLog("replaced original obb");
            this.updateStatus("extracting apk");
            File originalApk = new File(temp.getAbsolutePath() + "/base.apk");
            FileUtils.copyFile(new File(this.main.getPackageManager().getPackageInfo(target, 0).applicationInfo.publicSourceDir), originalApk);
            File apk = new File(temp.getAbsolutePath() + "/apk");
            zipFile = new ZipFile(originalApk);
            zipFile.extractAll(apk.getAbsolutePath());
            this.updateLog("apk extracted");
            this.updateProgress(75);
            this.updateStatus("repackaging apk");
           FileUtils.deleteDirectory(new File(apk.getAbsolutePath() + "/assets/bin/Data/Managed"));
            for (File f : apk.listFiles()) {
                if (f.getName().equals("res")) continue;
                if (f.isFile()) {
                    zipFile.addFile(f, parameters);
                } else {
                    zipFile.addFolder(f, parameters);
                }
            }
            this.updateLog("apk repackaged");
            this.updateStatus("finished");
            FileUtils.deleteDirectory(apk);
            this.updateLog("installing apk");

            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Build.VERSION.SDK_INT <= 23 ? Uri.fromFile(originalApk) : FileProvider.getUriForFile(main, BuildConfig.APPLICATION_ID + ".fileprovider", originalApk);
            if (Build.VERSION.SDK_INT <= 23) {
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
            } else {
                intent.setData(uri);
            }
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            main.startActivityForResult(intent, REQUEST_INSTALL);

            FileUtils.deleteDirectory(apk);
            p.post(new Runnable() {
                @Override
                public void run() {
                    p.setVisibility(View.INVISIBLE);
                    status.setVisibility(View.INVISIBLE);
                }
            });
            ((Runnable)objects[0]).run();

            this.updateProgress(100);

        } catch (Exception e) {
            this.updateLog(e.getLocalizedMessage());
        }
        return null;
    }
    private void updateStatus(final String str) {
        this.status.post(new Runnable() {
            @Override
            public void run() {
                status.setText(str);
            }
        });
        this.updateLog(str);
    }
    private void updateLog(final String str) {
        this.log.post(() -> {
            log.append(str + "\r\n");
            Log.d("patch", str);
        });
    }
    private void updateProgress(final int percent) {
        this.progress.post(new Runnable() {
            @Override
            public void run() {
                progress.setProgress(percent);
            }
        });
    }
}

final class OnTargetSelectedListener implements AdapterView.OnItemSelectedListener {
    private ICCActivity main;
    OnTargetSelectedListener(ICCActivity main) {
        this.main = main;
    }

    @Override
    public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {
        if (view == null) return;
        final Button runPatch = this.main.findViewById(R.id.centrue);
        final TextView status = this.main.findViewById(R.id.status);
        final TextView log = this.main.findViewById(R.id.log);
        final ProgressBar progress = this.main.findViewById(R.id.progress);
        if (((TextView)view).getText().equals("...")) {
            runPatch.setVisibility(View.GONE);
            status.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            return;
        } else {
            runPatch.setVisibility(View.VISIBLE);
            status.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
        }
        File obbDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/obb/" + ((TextView) view).getText());
        File[] files = obbDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.substring(name.length() - 4).equals(".obb");
            }
        });
        boolean patch_obb = true;
        if (files == null || files.length == 0) {
            patch_obb = false;
            AlertDialog.Builder alert = new AlertDialog.Builder(this.main);
            alert.setMessage(R.string.info_no_obb);
            alert.setPositiveButton(Resources.getSystem().getText(android.R.string.ok), null);
            alert.setCancelable(false);
            alert.show();
        }
        runPatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                runPatch.setEnabled(false);
                parent.setEnabled(false);
                PatchTask patchTask = new PatchTask(main, status, log, progress, ((TextView)view).getText().toString());
                patchTask.execute(new Object[]{ new Runnable() {
                    @Override
                    public void run() {
                        runPatch.post(new Runnable() {
                            @Override
                            public void run() {
                                runPatch.setEnabled(true);
                                parent.setSelection(0);
                                parent.setEnabled(true);
                            }
                        });
                    }
                }});
            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}


