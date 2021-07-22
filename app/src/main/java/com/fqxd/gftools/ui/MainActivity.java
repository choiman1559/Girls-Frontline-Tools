package com.fqxd.gftools.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.global.Global;
import com.fqxd.gftools.R;
import com.fqxd.gftools.ui.fragment.HomeFragment;
import com.fqxd.gftools.ui.fragment.GFFragment;

import com.fqxd.gftools.ui.fragment.PackageFragment;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {
    public static int REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES = 0x01;
    public static DocumentFile DataFolder = null;
    BottomNavigationView bottomNavigationView;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);
        prefs = getSharedPreferences(Global.Prefs, MODE_PRIVATE);

        if(!prefs.getString("DataUri", "").equals("")){
            DataFolder = DocumentFile.fromTreeUri(this, Uri.parse(prefs.getString("DataUri", "")));
        }

        if(Global.isAmazonBuild) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.Amazon_Info_Title))
                    .setMessage(getString(R.string.Amazon_Info_Description))
                    .setNegativeButton("Cancel", (dialog, which) -> {})
                    .setPositiveButton("GO TO GITHUB", (dialog, which) -> {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/choiman1559/Girls-Frontline-Tools/releases/latest"));
                        startActivity(browserIntent);
                    }).show();
        }

        if (Build.VERSION.SDK_INT >= 23 && checkStoragePermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            if(Build.VERSION.SDK_INT > 29 && (DataFolder == null || !DataFolder.exists())) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.Permission_Special_Title))
                        .setMessage(getString(R.string.Permission_Special_Description))
                        .setNegativeButton("Cancel", (dialog, which) -> this.finish())
                        .setPositiveButton("Grant", (dialog, which) -> {
                            Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT_TREE");
                            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                            intent.putExtra("android.provider.extra.INITIAL_URI", Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata"));
                            startActivityForResult(intent, 42);
                        }).setCancelable(false).show();
            } else init();
        }
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
            Toast.makeText(getApplicationContext(), R.string.Permission_Special_Toast, Toast.LENGTH_SHORT).show();
            this.finish();
        } else {
            if(Build.VERSION.SDK_INT > 29 && (DataFolder == null || !DataFolder.exists())){
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.Permission_Special_Title))
                        .setMessage(getString(R.string.Permission_Special_Description))
                        .setNegativeButton("Cancel", (dialog, which) -> this.finish())
                        .setPositiveButton("Grant", (dialog, which) -> {
                            Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT_TREE");
                            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                            intent.putExtra("android.provider.extra.INITIAL_URI", Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata"));
                            startActivityForResult(intent, 42);
                        }).setCancelable(false).show();
            } else init();
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
                fragment = new PackageFragment();
                break;

            case R.id.modulesFragment:
                fragment = new GFFragment();
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

        checkUpdate();

        SharedPreferences p = getSharedPreferences(Global.Prefs, MODE_PRIVATE);
        if (p.getBoolean("isFirstRun", true)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.Recommended_Spec_Title)).setMessage(getString(R.string.Recommended_Spec_Description));
            builder.setPositiveButton(R.string.Recommended_Spec_Not_Remind, (dialog, which) -> p.edit().putBoolean("isFirstRun", false).apply()).setNegativeButton(R.string.Recommended_Spec_OK, (dialog, which) -> { }).show();
        }
    }

    private void checkUpdate() {
        AppUpdater appUpdater = new AppUpdater(this)
                .setDisplay(Display.DIALOG)
                .setButtonDismissClickListener((d,w) -> finish())
                .setTitleOnUpdateAvailable(getString(R.string.Update_Info_Title))
                .setCancelable(false)
                .setContentOnUpdateAvailable(getString(R.string.Update_Info_Description))
                .setButtonDismiss(getString(R.string.Update_Info_Button_NO))
                .setButtonUpdate(getString(R.string.Update_Info_Button_OK))
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
        } else if(requestCode == 42) {
            if(data.getData() != null) {
                DataFolder = DocumentFile.fromTreeUri(this, data.getData());
                if (Build.VERSION.SDK_INT > 29 && DataFolder.listFiles().length <= 0) {
                    Toast.makeText(this, "Can't get /Android/data access permission!", Toast.LENGTH_SHORT).show();
                    this.finish();
                } else {
                    String[] var = DataFolder.getUri().getPath().split("/");
                    String foo = var[var.length - 1];
                    if (foo.contains("data")) {
                        prefs.edit().putString("DataUri", data.getData().toString()).apply();
                        getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        init();
                    } else {
                        Toast.makeText(this, "Not valid path!", Toast.LENGTH_SHORT).show();
                        this.finish();
                    }
                }
            } else {
                Toast.makeText(this, "Can't get /Android/data access permission!", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(bottomNavigationView != null) bottomNavigationView.setSelectedItemId(R.id.homeFragment);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finishAffinity();
        System.exit(0);
    }
}