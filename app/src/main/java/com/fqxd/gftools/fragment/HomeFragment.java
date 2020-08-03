package com.fqxd.gftools.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fqxd.gftools.R;
import com.fqxd.gftools.features.JasActivity;
import com.fqxd.gftools.features.alarm.ui.AlarmListActivity;
import com.fqxd.gftools.features.gfd.GFDActivity;
import com.fqxd.gftools.features.gfneko.GFNekoActivity;
import com.fqxd.gftools.features.noti.NotiActivity;
import com.fqxd.gftools.features.xapk.XapkActivity;
import com.google.android.material.snackbar.Snackbar;

import com.xd.xdsdk.XDCallback;
import com.xd.xdsdk.XDSDK;

public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.UCButton).setOnClickListener(v -> XD(view));
        view.findViewById(R.id.XAPKInstall).setOnClickListener(v -> startActivity(new Intent(v.getContext(), XapkActivity.class)));
        view.findViewById(R.id.GFDButton).setOnClickListener(v -> startActivity(new Intent(v.getContext(), GFDActivity.class)));
        view.findViewById(R.id.ZASButton).setOnClickListener(v -> startActivity(new Intent(v.getContext(), JasActivity.class)));
        view.findViewById(R.id.AlarmButton).setOnClickListener(v -> startActivity(new Intent(v.getContext(), AlarmListActivity.class)));
        view.findViewById(R.id.NekoButton).setOnClickListener(v -> {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(Settings.canDrawOverlays(v.getContext())) {
                    startActivity(new Intent(v.getContext(), GFNekoActivity.class));
                }else{
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + v.getContext().getPackageName()));
                    Toast.makeText(v.getContext().getApplicationContext(), "이 기능을 사용하려면 다른 앱 위에 그리기 기능이 필요합니다!", Toast.LENGTH_SHORT).show();
                    v.getContext().startActivity(intent);
                }
            }
        });
        view.findViewById(R.id.NotiButton).setOnClickListener(v -> {
            if (isOffline(v.getContext())) {
                Snackbar.make(v, "Check Internet and Try Again", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                Intent intent = new Intent(v.getContext(), NotiActivity.class);
                startActivity(intent);
            }
        });

        //int visibility = view.getContext().getSharedPreferences("MainActivity", Context.MODE_PRIVATE).getBoolean("debug", false) ? View.VISIBLE : View.GONE;
        //view.findViewById(R.id.AlarmButton).setVisibility(visibility);
    }

    public boolean isOffline(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        if (cm.getActiveNetworkInfo() != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni == null || !ni.isConnected();
        }
        return true;
    }

    void XD(View view) {
        String TAG = HomeFragment.class.getSimpleName();
        if (isOffline(view.getContext())) {
            Snackbar.make(view, "Check Internet and Try Again", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            XDSDK.setCallback(new XDCallback() {
                @Override
                public void onInitSucceed() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                    Toast.makeText(view.getContext(), "Initialization Succeed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onInitFailed(String msg) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + msg);
                    Toast.makeText(view.getContext(), "Initialization Failed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLoginSucceed(String token) {
                    Toast.makeText(view.getContext(), XDSDK.getAccessToken(), Toast.LENGTH_LONG).show();
                    XDSDK.openUserCenter();
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + token);
                    Toast.makeText(view.getContext(), "Login Succeed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLoginFailed(String msg) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + msg);
                    Toast.makeText(view.getContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLoginCanceled() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                    Toast.makeText(view.getContext(), "Login Canceled", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onGuestBindSucceed(String token) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + token);
                    Toast.makeText(view.getContext(), "onGuestBindSucceed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLogoutSucceed() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                    Toast.makeText(view.getContext(), "Logout Succeed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPayCompleted() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                    Toast.makeText(view.getContext(), "onPayCompleted", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPayFailed(String msg) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + msg);
                    Toast.makeText(view.getContext(), "onPayFailed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPayCanceled() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                    Toast.makeText(view.getContext(), "onPayCanceled", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRealNameSucceed() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                    Toast.makeText(view.getContext(), "onRealNameSucceed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRealNameFailed(String msg) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                    Toast.makeText(view.getContext(), "onRealNameFailed", Toast.LENGTH_SHORT).show();
                }
            });

            XDSDK.initSDK(getActivity(), "a4d6xky5gt4c80s", 1, "AndroidChannel", "AndroidVersion", true);
            XDSDK.login();
        }
    }
}
