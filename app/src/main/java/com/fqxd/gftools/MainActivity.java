package com.fqxd.gftools;

import android.content.Intent;
import android.os.Bundle;

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

import com.xd.xdsdk.XDCallback;
import com.xd.xdsdk.XDSDK;

public class MainActivity extends AppCompatActivity {
    public static int REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES = 0x01;
    public static final String TAG = MainActivity.class.getSimpleName();

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        }
        return false;
    }

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23 && this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            if (Build.VERSION.SDK_INT >= 23 && this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Storage Permission was rejected", Toast.LENGTH_SHORT).show();
                this.finish();
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
                startActivity(new Intent(getApplicationContext(), ICCActivity.class));
            }
        });

        Button UCB = findViewById(R.id.UCButton);
        UCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOnline()) {
                    Snackbar.make(v, "Check Internet and Try Again", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                     }

                else {
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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
}
