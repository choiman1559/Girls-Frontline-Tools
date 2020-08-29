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
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

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

public class HomeFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_prefs, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {

        switch (preference.getKey()) {
            case "Button_XD":
                XD();
                break;

            case "Button_XAPK":
                startActivity(new Intent(getContext(), XapkActivity.class));
                break;

            case "Button_ZAS":
                startActivity(new Intent(getContext(), JasActivity.class));
                break;

            case "Button_GFDIC":
                startActivity(new Intent(getContext(), GFDActivity.class));
                break;

            case "Button_NEKO":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(getContext())) {
                        startActivity(new Intent(getContext(), GFNekoActivity.class));
                    } else {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
                        Toast.makeText(getContext(), "이 기능을 사용하려면 다른 앱 위에 그리기 기능이 필요합니다!", Toast.LENGTH_SHORT).show();
                        getContext().startActivity(intent);
                    }
                }
                break;

            case "Button_NOTI":
                if (isOffline(getContext())) {
                    Snackbar.make(getView(), "Check Internet and Try Again", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Intent intent = new Intent(getContext(), NotiActivity.class);
                    startActivity(intent);
                }
                break;

            case "Button_ALARM":
                startActivity(new Intent(getContext(), AlarmListActivity.class));
                break;
        }

        return super.onPreferenceTreeClick(preference);
    }

    public boolean isOffline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        if (cm.getActiveNetworkInfo() != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni == null || !ni.isConnected();
        }
        return true;
    }

    void XD() {
        String TAG = HomeFragment.class.getSimpleName();
        if (isOffline(getContext())) {
            Snackbar.make(getView(), "Check Internet and Try Again", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            XDSDK.setCallback(new XDCallback() {
                @Override
                public void onInitSucceed() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                    Toast.makeText(getContext(), "Initialization Succeed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onInitFailed(String msg) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + msg);
                    Toast.makeText(getContext(), "Initialization Failed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLoginSucceed(String token) {
                    Toast.makeText(getContext(), XDSDK.getAccessToken(), Toast.LENGTH_LONG).show();
                    XDSDK.openUserCenter();
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + token);
                    Toast.makeText(getContext(), "Login Succeed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLoginFailed(String msg) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + msg);
                    Toast.makeText(getContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLoginCanceled() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                    Toast.makeText(getContext(), "Login Canceled", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onGuestBindSucceed(String token) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + token);
                    Toast.makeText(getContext(), "onGuestBindSucceed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLogoutSucceed() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                    Toast.makeText(getContext(), "Logout Succeed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPayCompleted() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                    Toast.makeText(getContext(), "onPayCompleted", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPayFailed(String msg) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + msg);
                    Toast.makeText(getContext(), "onPayFailed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPayCanceled() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                    Toast.makeText(getContext(), "onPayCanceled", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRealNameSucceed() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                    Toast.makeText(getContext(), "onRealNameSucceed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRealNameFailed(String msg) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                    Toast.makeText(getContext(), "onRealNameFailed", Toast.LENGTH_SHORT).show();
                }
            });

            XDSDK.initSDK(getActivity(), "a4d6xky5gt4c80s", 1, "AndroidChannel", "AndroidVersion", true);
            XDSDK.login();
        }
    }
}
