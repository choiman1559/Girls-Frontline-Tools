package com.fqxd.gftools.features.decom;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

import org.apache.commons.io.FileUtils;

import java.io.File;

import com.fqxd.gftools.implement.AsyncTask;
import com.kellinwood.security.zipsigner.ZipSigner;

import static com.fqxd.gftools.features.icon.IconChangeActivity.copyObbDirectory;

@SuppressLint("StaticFieldLeak")
final class PatchTask extends AsyncTask<Runnable, Void, Void> {

    public static final int REQUEST_INSTALL = 0x00;
    private final DecActivity main;
    private final TextView status;
    private final TextView log;
    private final ProgressBar progress;
    @Nullable
    private final CompressionLevel level;
    private final String target;
    private final Boolean ifErr;

    PatchTask(DecActivity main, TextView status, TextView log, ProgressBar progress, Boolean ifErr, @Nullable CompressionLevel level, String target) {
        this.main = main;
        this.status = status;
        this.log = log;
        this.progress = progress;
        this.target = target;
        this.level = level;
        this.ifErr = ifErr;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.updateStatus("이전 작업이 완료될때까지 대기중...");
    }

    @Override
    protected Void doInBackground(Runnable[] runnable) {
        try {
            final ProgressBar p = this.main.findViewById(R.id.running);
            p.post(() -> p.setVisibility(View.VISIBLE));
            status.post(() -> status.setVisibility(View.VISIBLE));

            this.updateProgress(0);
            this.updateLog(this.main.getString(R.string.info_patch_started));
            this.updateLog("Target: " + this.target);

            File temp = this.main.getExternalFilesDir(null);
            FileUtils.deleteDirectory(temp);
            temp.mkdir();

            this.updateStatus("extracting obb");
            final File obb = new File(temp.getAbsolutePath() + "/obb");
            Log.d("obb", obb.toString());
            obb.mkdir();

            File originalObb = (new File(Global.Storage + "/Android/obb/" + this.target)).listFiles()[0];
            Log.d("origin", originalObb.toString());
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
            if (level != null) {
                Log.e("level", "compress level : " + level.getLevel());
                parameters.setCompressionMethod(CompressionMethod.DEFLATE);
                parameters.setCompressionLevel(level);
            } else parameters.setCompressionMethod(CompressionMethod.STORE);
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
            if(!ifErr) FileUtils.deleteDirectory(new File(apk.getAbsolutePath() + "/assets/bin/Data/Managed"));
            for (File f : apk.listFiles()) {
                Log.d("list", f.getAbsolutePath());
                if (f.getName().equals("res")) continue;
                if (f.isFile()) {
                    zipFile.addFile(f, parameters);
                } else {
                    zipFile.addFolder(f, parameters);
                }
            }

            if(ifErr) {
                this.updateProgress(90);
                this.updateStatus("resigning apk");
                File SignedApk = new File(temp.getAbsolutePath() + "/signed.apk");
                ZipSigner zipSigner = new ZipSigner();
                zipSigner.setKeymode(ZipSigner.KEY_TESTKEY);
                zipSigner.signZip(originalApk.getAbsolutePath(), SignedApk.getAbsolutePath());

                this.updateLog("apk repackaged");
                this.updateProgress(95);
                this.updateLog("installing apk");

                copyObbDirectory(this.target, obb);
                main.startActivityForResult(new Intent(Intent.ACTION_UNINSTALL_PACKAGE).setData(Uri.parse("package:" + target)), 5555);
            } else {
                this.updateLog("apk repackaged");
                this.updateStatus("finished");
                FileUtils.deleteDirectory(apk);
                this.updateProgress(100);
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
            }

            FileUtils.deleteDirectory(apk);
            p.post(() -> p.setVisibility(View.INVISIBLE));
            status.post(() -> status.setVisibility(View.INVISIBLE));
            ((Runnable) runnable[0]).run();
            this.updateProgress(100);
            updateStatus("\n");

        } catch (Exception e) {
            this.updateLog(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return null;
    }

   public void updateStatus(final String str) {
        this.status.post(() -> status.setText(str));
        this.updateLog(str);
    }

    public void updateLog(final String str) {
        this.log.post(() -> {
            log.append(str + "\r\n");
            Log.d("patch", str);
        });
    }

    public void updateProgress(final int percent) {
        this.progress.post(() -> progress.setProgress(percent));
    }
}

