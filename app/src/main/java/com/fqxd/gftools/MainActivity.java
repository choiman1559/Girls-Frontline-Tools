package com.fqxd.gftools;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import com.fqxd.gftools.fragment.HomeFragment;
import com.fqxd.gftools.fragment.GFFragment;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.xd.xdsdk.XDSDK;

import java.util.Arrays;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {
    public static int REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES = 0x01;
    public AdView adview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build();
        Fabric.with(this, crashlyticsKit);

        if (Build.VERSION.SDK_INT >= 23 && this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "저장용량 접근 권한이 없습니다!", Toast.LENGTH_SHORT).show();
            this.finish();
        } else {
            init();
        }
    }

    private void init() {
        ((ImageView) findViewById(R.id.MainImageView)).setImageResource(R.mipmap.ic_icon_round);
        adview = new AdView(this);
        adview.setAdSize(AdSize.BANNER);
        adview.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d("AdService","ad Loaded successfully!");
                adview.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d("AdService","Failed while loading ad!");
                adview.setVisibility(View.GONE);
            }
        });

        switch(Global.getSHA1Hash(this)) {
            case "cf:61:36:5e:71:42:fa:21:7c:b5:5f:52:6d:e3:d9:06:57:f5:5e:01":
            case "d5:5c:2e:6a:58:4c:3d:4f:4a:3a:08:cd:1c:7e:6a:eb:ee:ea:46:10":
                if(!BuildConfig.DEBUG){
                    MobileAds.initialize(this, i -> { });
                    adview.setAdUnitId(getString(R.string.ad_id_pub));
                    adview.loadAd(new AdRequest.Builder().build());
                    Log.d("ADInit","Ad Load Finished as Release Mode!");
                }
                break;

            //Ad Config for Test : you can delete this case
            case "36:47:5f:49:ce:2d:bc:cb:b8:59:30:e3:86:17:85:6c:78:cf:86:53":
                if(BuildConfig.DEBUG) {
                    MobileAds.initialize(this, i -> { });
                    adview.setAdUnitId(getString(R.string.ad_id_test));
                    List<String> testDeviceIds = Arrays.asList("8F3F0D8FE070C4498B1C6B5B3C754B4E");
                    RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
                    MobileAds.setRequestConfiguration(configuration);
                    adview.loadAd(new AdRequest.Builder().build());
                    Log.d("ADInit","Ad Load Finished as DEBUG Mode!");
                }
                break;

            default:
                Log.d("ADInit","No specific detectable SHA1 hash, skipping load ad...");
                adview.setVisibility(View.GONE);
                break;
        }

        ((LinearLayout)findViewById(R.id.mainLayout)).addView(adview,0);
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("메인", HomeFragment.class)
                .add("한섭", GFFragment.class)
                .add("대만섭", GFFragment.class)
                .add("중섭", GFFragment.class)
                .add("일섭", GFFragment.class)
                .add("글섭", GFFragment.class)
                .add("비리섭", GFFragment.class)
                .create());

        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(viewPager);
        viewPagerTab.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                viewPager.setCurrentItem(position);
                adapter.notifyDataSetChanged();
                switch (position) {
                    case 1:
                        changeImg(R.drawable.ic_south_korea);
                        break;

                    case 2:
                        changeImg(R.drawable.ic_taiwan_foreground);
                        break;

                    case 3:
                        changeImg(R.drawable.ic_china);
                        break;

                    case 4:
                        changeImg(R.drawable.ic_japan);
                        break;

                    case 5:
                        changeImg(R.drawable.ic_global);
                        break;

                    case 6:
                        changeImg(R.mipmap.ic_bilbil);
                        break;

                    default:
                        changeImg(R.mipmap.ic_icon_round);
                }
            }

            void changeImg(int resId) {
                ((ImageView) findViewById(R.id.MainImageView)).setImageResource(resId);
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        findViewById(R.id.action_a).setOnClickListener(v -> {
            if (!isOnline()) {
                Snackbar.make(v, "Check Internet and Try Again", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                AppUpdater appUpdater = new AppUpdater(MainActivity.this)
                        .showAppUpdated(true)
                        .setDisplay(Display.DIALOG)
                        .setUpdateFrom(UpdateFrom.GOOGLE_PLAY)
                        .setButtonDoNotShowAgain(null);
                appUpdater.start();
            }
        });

        OssLicensesMenuActivity.setActivityTitle("OSS License Notice");
        findViewById(R.id.action_b).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/choiman1559/Girls-Frontline-Tools"))));
        findViewById(R.id.action_c).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, OssLicensesMenuActivity.class)));
        checkUpdate();

        SharedPreferences p = getSharedPreferences("com.fqxd.gftools_preferences", MODE_PRIVATE);
        if (p.getBoolean("isFirstRun", true)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("안내").setMessage("이 앱은 다음 환경에 가장 최적화되어 있습니다 : \n - Android 10 (AOSP, GSI)\n - EAS Kernel (Linux 4.3+)\n - Ram 3GB 이상\n - 1080x2140 (403dpi)\n - ARMv8a, x86_64\n - Magisk 20.4\n");
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

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        if (cm.getActiveNetworkInfo() != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        }
        return false;
    }
}