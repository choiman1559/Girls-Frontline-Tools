package com.fqxd.gftools.features.decom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.fqxd.gftools.MainActivity;
import com.fqxd.gftools.R;

import java.io.File;
import java.util.ArrayList;

public final class DecActivity extends Activity {

    @Override
    public void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_dec);

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
        } catch (PackageManager.NameNotFoundException e) {
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
