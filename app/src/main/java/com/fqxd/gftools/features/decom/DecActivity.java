package com.fqxd.gftools.features.decom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fqxd.gftools.Global;
import com.fqxd.gftools.MainActivity;
import com.fqxd.gftools.R;

import net.lingala.zip4j.model.enums.CompressionLevel;

import java.io.File;

public final class DecActivity extends AppCompatActivity {
    private static boolean isTaskRunning = false;

    @Override
    public void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_dec);
        String pkg = getIntent().getStringExtra("pkg");

        if (Build.VERSION.SDK_INT >= 23 && this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return;
        }
        if (Build.VERSION.SDK_INT >= 26 && !this.getPackageManager().canRequestPackageInstalls()) {
            this.startActivityForResult(
                    new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse("package:" + this.getPackageName())),
                    MainActivity.REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES
            );
            finish();
        }
        TextView packageinfo = this.findViewById(R.id.packageinfo);
        try {
            PackageManager pm = this.getPackageManager();
            packageinfo.setText("target : " + pm.getApplicationLabel(pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA)) + " (" + pkg + ")");
        } catch (PackageManager.NameNotFoundException ignored) { }

        final Button runPatch = findViewById(R.id.centrue);
        final TextView status = findViewById(R.id.status);
        final TextView log = findViewById(R.id.log);
        final ProgressBar progress = findViewById(R.id.progress);
        final SeekBar level = findViewById(R.id.compressLevel);
        final LinearLayout layout = findViewById(R.id.progressLayout);
        final CheckBox IfErr = findViewById(R.id.checkBox_IfErr);

        if(Build.VERSION.SDK_INT < 26) {
            level.setProgress(0);
            level.setEnabled(false);
            level.setOnClickListener(v -> Toast.makeText(this,"안드로이드 7 이하에서는 레벨 설정이 불가능합니다!",Toast.LENGTH_SHORT));
        }

        layout.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        File obbDir = new File(Global.Storage + "/Android/obb/" + pkg + "/");
        File[] files = obbDir.listFiles((dir, name) -> name.substring(name.length() - 4).equals(".obb"));
        if (files == null || files.length == 0) {
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
            PatchTask patchTask = new PatchTask(this, status, log, progress,IfErr.isChecked(), getLevel(level), pkg);
            patchTask.execute(new Object[]{(Runnable) () -> runPatch.post(() -> {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.finish();
            this.startActivity(this.getIntent());
        }
        ActivityCompat.requestPermissions(this, permissions, 0);
    }

    @Override
    public void onBackPressed() {
        if(!isTaskRunning) finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !isTaskRunning && super.onTouchEvent(event);
    }
}
