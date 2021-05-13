package com.fqxd.gftools.features.decom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.global.Global;
import com.fqxd.gftools.ui.MainActivity;
import com.fqxd.gftools.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

import net.lingala.zip4j.model.enums.CompressionLevel;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

@SuppressWarnings("deprecation")
public final class DecActivity extends AppCompatActivity {
    private static boolean isTaskRunning = false;
    public static boolean isOBBExists = true;
    String pkg;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_dec);
        pkg = getIntent().getStringExtra("pkg");

        if (Build.VERSION.SDK_INT >= 23 && this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return;
        }

        if (Build.VERSION.SDK_INT >= 26 && !this.getPackageManager().canRequestPackageInstalls()) {
            this.startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse("package:" + this.getPackageName())),
                    MainActivity.REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            finish();
        }

        TextView PackageInfo = this.findViewById(R.id.packageinfo);
        try {
            PackageManager pm = this.getPackageManager();
            PackageInfo.setText(MessageFormat.format("target : {0} ({1})", pm.getApplicationLabel(pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA)), pkg));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        final MaterialButton runPatch = findViewById(R.id.centrue);
        final TextView status = findViewById(R.id.status);
        final TextView log = findViewById(R.id.log);
        final ProgressBar progress = findViewById(R.id.progress);
        final SeekBar level = findViewById(R.id.compressLevel);
        final LinearLayout layout = findViewById(R.id.progressLayout);
        final MaterialCheckBox IfErr = findViewById(R.id.checkBox_IfErr);

        IfErr.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                new AlertDialog.Builder(this)
                        .setTitle("경고!")
                        .setMessage("이 기능을 사용하면 다시 공식 클라이언트로 재설치하기 이전까지 결재 등의 Play Service 를 요하는 기능은 작동하지 않을것이며,\"계속\" 버튼을 눌러 이 기능을 사용할경우 이 기능을 사용하다 발생한 불이익은 순전히 이 기능을 사용한 본인에게 있음을 인지하고 동의한것으로 간주합니다.")
                        .setPositiveButton("계속", (dialog, which) -> {})
                        .setNegativeButton("취소",((dialog, which) -> IfErr.setChecked(false)))
                        .setCancelable(false).show();
            }
        });

        if (Build.VERSION.SDK_INT < 26) {
            level.setProgress(0);
            level.setEnabled(false);
            level.setOnClickListener(v -> Toast.makeText(this, "안드로이드 7 이하에서는 레벨 설정이 불가능합니다!", Toast.LENGTH_SHORT));
        }

        layout.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        File obbDir = new File(Global.Storage + "/Android/obb/" + pkg + "/");
        File[] files = obbDir.listFiles((dir, name) -> name.endsWith(".obb"));
        if (files == null || files.length == 0) {
            isOBBExists = false;
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage(R.string.info_no_obb);
            alert.setPositiveButton(Resources.getSystem().getText(android.R.string.ok), null);
            alert.setCancelable(false);
            alert.show();
        }

        runPatch.setOnClickListener(v -> {
            runPatch.setEnabled(false);
            level.setEnabled(false);
            IfErr.setEnabled(false);
            layout.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
            isTaskRunning = true;
            PatchTask patchTask = new PatchTask(this, status, log, progress, IfErr.isChecked(), getLevel(level), pkg);
            patchTask.execute(new Runnable[]{(Runnable) () -> runPatch.post(() -> {
                runPatch.setEnabled(true);
                level.setEnabled(true);
                IfErr.setEnabled(true);
                layout.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
                isTaskRunning = false;
            })});
        });
    }

    @Nullable
    private CompressionLevel getLevel(SeekBar bar) {
        switch (bar.getProgress()) {
            case 1:
                return CompressionLevel.FASTEST;

            case 2:
                return CompressionLevel.FAST;

            case 3:
                return CompressionLevel.NORMAL;

            case 4:
                return CompressionLevel.MAXIMUM;

            case 5:
                return CompressionLevel.ULTRA;

            default:
                return null;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 5555) {
            File temp = getExternalFilesDir(null);
            if(isOBBExists) {
                File obb = new File(temp.getAbsolutePath() + "/obb");
                copyObbDirectory(pkg, obb);
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            File originalApk = new File(temp.getAbsolutePath() + "/signed.apk");
            Uri uri = Build.VERSION.SDK_INT <= 23 ? Uri.fromFile(originalApk) : FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", originalApk);
            if (Build.VERSION.SDK_INT <= 23) {
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
            } else {
                intent.setData(uri);
            }
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, 0x00);
        }

        if (requestCode == 0x00) {
            try {
                FileUtils.deleteDirectory(this.getExternalFilesDir(null));
                TextView status = findViewById(R.id.status);
                ProgressBar progress = findViewById(R.id.progress);
                status.setText("finished");
                progress.setProgress(100);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public static void copyObbDirectory(String target,File from) {
        try {
            File originalOBB = new File(Global.Storage + "/Android/obb/" + target);
            if (from.exists() && from.isDirectory()) {
                if (!originalOBB.exists()) originalOBB.mkdirs();
                File[] list = from.listFiles();
                for (File file : list) {
                    FileUtils.copyFile(file, new File(originalOBB.getAbsolutePath() + "/" + file.getName()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.finish();
            this.startActivity(this.getIntent());
        }
        ActivityCompat.requestPermissions(this, permissions, 0);
    }

    @Override
    public void onBackPressed() {
        if (!isTaskRunning) finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !isTaskRunning && super.onTouchEvent(event);
    }
}
