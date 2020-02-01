package com.fqxd.gftools.features.decom;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.R;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionMethod;

import org.apache.commons.io.FileUtils;

import java.io.File;

final class PatchTask extends AsyncTask {
    public static final int REQUEST_INSTALL = 0x00;
    private static final int REQUEST_CODE = 0;
    private DecActivity main;
    private TextView status;
    private TextView log;
    private ProgressBar progress;
    private String target;
    PatchTask(DecActivity main, TextView status, TextView log, ProgressBar progress, String target) {
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
            Uri uri = Build.VERSION.SDK_INT <= 23 ? Uri.fromFile(originalApk) : FileProvider.getUriForFile(main, BuildConfig.APPLICATION_ID + ".provider", originalApk);
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

