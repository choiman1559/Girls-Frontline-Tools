package com.fqxd.gftools.features.decom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fqxd.gftools.MainActivity;
import com.fqxd.gftools.R;

import java.io.File;

public final class DecActivity extends Activity {

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

        File obbDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/obb/" + pkg + "/");
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
            PatchTask patchTask = new PatchTask(this, status, log, progress, pkg);
            patchTask.execute(new Object[]{(Runnable) () -> runPatch.post(() -> runPatch.setEnabled(true))});
        });
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
