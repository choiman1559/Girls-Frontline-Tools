package com.fqxd.gftools;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import com.fqxd.gftools.ui.HomeFragment;
import com.fqxd.gftools.ui.GFFragment;

import com.fqxd.gftools.ui.PackageFragment;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.xd.xdsdk.XDSDK;

import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {
    public static int REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES = 0x01;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);

        if (Build.VERSION.SDK_INT >= 23 && checkStoragePermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else init();
    }

    @RequiresApi(23)
    boolean checkStoragePermission() {
        boolean b1 = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        boolean b2 = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        return b1 && b2;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && Build.VERSION.SDK_INT >= 23 && checkStoragePermission()) {
            Toast.makeText(getApplicationContext(), "저장용량 접근 권한이 없습니다!", Toast.LENGTH_SHORT).show();
            this.finish();
        } else {
            init();
        }
    }

    @SuppressLint({"MissingPermission", "NonConstantResourceId"})
    private void init() {
        AtomicReference<String> itemTitle = new AtomicReference<>("");
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            String itemId = item.getTitle().toString();
            if(!itemTitle.get().equals(itemId)) {
                Fragment fragment;
                switch (itemId) {
                    case "Features":
                        fragment = new GFFragment();
                        break;

                    case "Packages":
                        fragment = new PackageFragment();
                        break;

                    default:
                        fragment = new HomeFragment();
                        break;
                }

                fragment.setRetainInstance(true);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, fragment)
                        .commitNowAllowingStateLoss();
            }
            itemTitle.set(itemId);
            return true;
        });

        Fragment fragment;
        switch (bottomNavigationView.getSelectedItemId()) {
            case R.id.packageManagerFragment:
                fragment = new GFFragment();
                break;

            case R.id.modulesFragment:
                fragment = new GFFragment();
                break;

            default:
                fragment = new HomeFragment();
                break;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, fragment)
                .commitNowAllowingStateLoss();

        checkUpdate();

        SharedPreferences p = getSharedPreferences(Global.Prefs, MODE_PRIVATE);
        if (p.getBoolean("isFirstRun", true)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("안내").setMessage("이 앱은 다음 환경에 가장 최적화되어 있습니다 : \n - Android 10 (AOSP, GSI)\n - EAS Kernel (Linux 4.3+)\n - Ram 3GB 이상\n - 1080x2140 (403dpi)\n - ARMv8a, x86_64\n - Magisk 20.4+\n");
            builder.setPositiveButton("다시 보지 않기", (d, i) -> p.edit().putBoolean("isFirstRun", false).apply()).setNegativeButton("확인", (dialog, which) -> { }).show();
        }
    }

    private void checkUpdate() {
        AppUpdater appUpdater = new AppUpdater(this)
                .setDisplay(Display.DIALOG)
                .setButtonDismissClickListener((d,w) -> finish())
                .setTitleOnUpdateAvailable("업데이트 발견!")
                .setCancelable(false)
                .setContentOnUpdateAvailable("업데이트 후 앱 사용이 가능합니다!")
                .setButtonDismiss("앱 종료")
                .setButtonUpdate("업데이트")
                .setButtonUpdateClickListener((d,w) -> startActivityForResult(new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=" + getPackageName())),1))
                .setButtonDoNotShowAgain(null)
                .setUpdateFrom(UpdateFrom.GOOGLE_PLAY)
                .showAppUpdated(false);
        appUpdater.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1) {
           checkUpdate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(bottomNavigationView != null) bottomNavigationView.setSelectedItemId(R.id.homeFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        XDSDK.onResume(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        XDSDK.onStop(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finishAffinity();
        System.exit(0);
    }
}