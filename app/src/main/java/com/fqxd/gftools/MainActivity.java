package com.fqxd.gftools;

import android.content.Intent;
import android.os.Bundle;

import com.fqxd.gftools.alarm.alarm.AlarmListActivity;
import com.fqxd.gftools.alarm.palarm.PACAlarmActivity;
import com.fqxd.gftools.alarm.palarm.PacketClass;

import com.fqxd.gftools.features.BQMActivity;
import com.fqxd.gftools.features.CenActivity;
import com.fqxd.gftools.features.decom.DecActivity;
import com.fqxd.gftools.features.JasActivity;
import com.fqxd.gftools.features.gfd.GFDActivity;
import com.fqxd.gftools.features.txt.TxtKRActivity;
import com.fqxd.gftools.features.xapk.XapkActivity;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.net.*;
import android.widget.*;
import android.util.Log;

import com.fqxd.gftools.vpn.utils.VpnServiceHelper;
import com.xd.xdsdk.XDCallback;
import com.xd.xdsdk.XDSDK;

public class MainActivity extends AppCompatActivity {
    public static int REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES = 0x01;
    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!getSharedPreferences("ListAlarm",MODE_PRIVATE).getBoolean("isChecked",false) && VpnServiceHelper.vpnRunningStatus()) new PacketClass().endVpn(MainActivity.this);
        if (Build.VERSION.SDK_INT >= 23 && this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            if (Build.VERSION.SDK_INT >= 23 && this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Storage Permission was rejected", Toast.LENGTH_SHORT).show();
                this.recreate();
            }
            return;
        }

        if (Build.VERSION.SDK_INT >= 26 && !this.getPackageManager().canRequestPackageInstalls()) {
            this.startActivityForResult(
                    new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse("package:" + this.getPackageName())),
                    MainActivity.REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES
            );
            if (Build.VERSION.SDK_INT >= 26 && !this.getPackageManager().canRequestPackageInstalls()) {
                Toast.makeText(getApplicationContext(), "Package Install Permission was rejected", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }

        final Button OSS = findViewById(R.id.ORSCButton);
        OSS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), OSSActivity.class));
            }
        });

        final Button ICC = findViewById(R.id.ICCButton);
        ICC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DecActivity.class);
                startActivity(intent);
            }
        });

        final Button GFD = findViewById(R.id.GFDButton);
        GFD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GFDActivity.class);
                startActivity(intent);
            }
        });

        final Button TXT = findViewById(R.id.TextKRButton);
        TXT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TxtKRActivity.class);
                startActivity(intent);
            }
        });

        final Button XAPK = findViewById(R.id.XAPKInstaller);
        XAPK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, XapkActivity.class);
                startActivity(intent);
            }
        });

        final Button Alarm = findViewById(R.id.AlarmButton);
        //Alarm.setEnabled(false);
        Alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AlarmListActivity.class);
                startActivity(intent);
            }
        });

        final Button PAM = findViewById(R.id.PAlarmButton);
        PAM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PACAlarmActivity.class);
                startActivity(intent);
            }
        });

        final Button CEN = findViewById(R.id.CenButton);
        CEN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CenActivity.class);
                startActivity(intent);
            }
        });

        final Button ZAS = findViewById(R.id.ZasButton);
        ZAS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, JasActivity.class);
                startActivity(intent);
            }
        });

        final Button BQM = findViewById(R.id.BQMButton);
        BQM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOnline()) {
                    Snackbar.make(v, "Check Internet and Try Again", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, BQMActivity.class);
                    startActivity(intent);
                }
            }
        });

        final Button Chk = findViewById(R.id.ChkButton);
        Chk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isOnline()) {
                    Snackbar.make(v, "Check Internet and Try Again", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    AppUpdater appUpdater = new AppUpdater(MainActivity.this)
                            .showAppUpdated(true)
                            .setDisplay(Display.DIALOG)
                            .setUpdateFrom(UpdateFrom.GITHUB)
                            .setGitHubUserAndRepo("choiman1559", "Girls-Frontline-Tools")
                            .setButtonDoNotShowAgain(null);
                    appUpdater.start();
                }
            }
        });


        Button UCB = findViewById(R.id.UCButton);
        UCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOnline()) {
                    Snackbar.make(v, "Check Internet and Try Again", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    XDSDK.setCallback(new XDCallback() {
                        @Override
                        public void onInitSucceed() {
                            Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                            Toast.makeText(getApplicationContext(), "Initialization Succeed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onInitFailed(String msg) {
                            Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + msg);
                            Toast.makeText(getApplicationContext(), "Initialization Failed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLoginSucceed(String token) {
                            Toast.makeText(getApplicationContext(), XDSDK.getAccessToken(), Toast.LENGTH_LONG).show();
                            XDSDK.openUserCenter();
                            Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + token);
                            Toast.makeText(getApplicationContext(), "Login Succeed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLoginFailed(String msg) {
                            Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + msg);
                            Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLoginCanceled() {
                            Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                            Toast.makeText(getApplicationContext(), "Login Canceled", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onGuestBindSucceed(String token) {
                            Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + token);
                            Toast.makeText(getApplicationContext(), "onGuestBindSucceed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLogoutSucceed() {
                            Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                            Toast.makeText(getApplicationContext(), "Logout Succeed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPayCompleted() {
                            Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                            Toast.makeText(getApplicationContext(), "onPayCompleted", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPayFailed(String msg) {
                            Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + msg);
                            Toast.makeText(getApplicationContext(), "onPayFailed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPayCanceled() {
                            Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                            Toast.makeText(getApplicationContext(), "onPayCanceled", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRealNameSucceed() {
                            Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                            Toast.makeText(getApplicationContext(), "onRealNameSucceed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRealNameFailed(String msg) {
                            Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                            Toast.makeText(getApplicationContext(), "onRealNameFailed", Toast.LENGTH_SHORT).show();
                        }
                    });

                    XDSDK.initSDK(MainActivity.this, "a4d6xky5gt4c80s", 1, "AndroidChannel", "AndroidVersion", true);
                    XDSDK.login();
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AppActivity.class);
                startActivity(intent);
            }
        });

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
        if (cm.getActiveNetworkInfo() != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        }
        return false;
    }
}
