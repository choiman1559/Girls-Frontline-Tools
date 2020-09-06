package com.fqxd.gftools;

import android.content.Intent;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import com.fqxd.gftools.features.calculator.CalculatorActivity;
import com.fqxd.gftools.fragment.HomeFragment;
import com.fqxd.gftools.fragment.GFFragment;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.net.*;
import android.widget.*;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.xd.xdsdk.XDSDK;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {
    public static int REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build();
        Fabric.with(this, crashlyticsKit);

        if (Build.VERSION.SDK_INT >= 23 && this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Storage Permission was rejected", Toast.LENGTH_SHORT).show();
            }
            this.finish();
        }

        ((ImageView) findViewById(R.id.MainImageView)).setImageResource(R.mipmap.ic_icon_round);
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
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

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
            public void onPageScrollStateChanged(int state) {
            }
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

        AppUpdater appUpdater = new AppUpdater(this)
                .setDisplay(Display.SNACKBAR)
                .setUpdateFrom(UpdateFrom.GITHUB)
                .setGitHubUserAndRepo("choiman1559", "Girls-Frontline-Tools")
                .showAppUpdated(true);
        appUpdater.start();
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
