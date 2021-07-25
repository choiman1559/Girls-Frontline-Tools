package com.fqxd.gftools.features.xduc;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fqxd.gftools.R;
import com.fqxd.gftools.ui.fragment.HomeFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.xd.xdsdk.XDCallback;
import com.xd.xdsdk.XDSDK;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lib.xdsdk.passport.CometPassport;

public class XDUCActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xduc);
        MaterialButton Google = findViewById(R.id.Button_Google);
        MaterialButton Facebook = findViewById(R.id.Button_FaceBook);
        MaterialButton XDSdk = findViewById(R.id.Button_XDSDK);
        CometPassport passport = CometPassport.model();

        Google.setOnClickListener((v) -> new Thread(() -> passport.signWithGoogle(this)).start());
        passport.setOnGoogleLoginCompleteListener((result -> {
            processJson(result);
            Toast.makeText(this, R.string.Google_Login_Query_OK, Toast.LENGTH_SHORT).show();
        }));

        Facebook.setOnClickListener((v) -> new Thread(() -> passport.signWithFacebook(this)).start());
        passport.setOnFacebookLoginCompleteListener((result -> {
            processJson(result);
            Toast.makeText(this, R.string.Facebook_Login_Query_OK, Toast.LENGTH_SHORT).show();
        }));

        XDSdk.setOnClickListener((v) -> XD());
    }

    @SuppressLint("MissingPermission")
    public boolean isOffline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        if (cm.getActiveNetworkInfo() != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni == null || !ni.isConnected();
        }
        return true;
    }

    private void processJson(JSONObject object) {
        if(object.has("uid") && !object.isNull("uid")) {
            try {
                String appId = "158714";
                String serverId = "c42608fa22eeeee14a5e61db8362134f";
                String uId = object.getString("uid");
                String uName = object.getString("username");
                String nickName = "Example1234";
                long timestamp = object.getLong("time");

                String sig = getBugReportSig(serverId, nickName, timestamp, appId, uId, uName);
                String uri = "http://csapp.playcomet.com/index.php?appid=" + appId + "&s=" + serverId + "&uid=" + uId + "&uname=" + uName + "&nickname=" + nickName + "&sig=" + sig + "&time=" + timestamp + "&l=kr";
                this.startActivity(new Intent(this, UCWebViewActivity.class).putExtra("uri", uri));
            } catch (Exception e) {
                printErrorMessage("Error occurred while processing login info : " + e.toString());
                e.printStackTrace();
            }
        } else printErrorMessage("Login failed! Login failed!\nDoes the logged-in account exist on the Girls' Frontline's server?");
    }

    void printErrorMessage(String message) {
        new AlertDialog.Builder(this)
                .setPositiveButton("OK", (dialog, which) -> { })
                .setTitle("Error!").setMessage(message).show();
    }

    public static String getBugReportSig(String svrId, String nickname, long timestamp, String appid, String uid, String uname) {
        String key = appid + '|' + svrId + '|' + uid + '|' + uname + '|' + nickname + "|sHtsFhdssDF";
        return md5(md5(key) + timestamp);
    }

    public static String md5(String string) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(string.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 255) < 16) {
                    hex.append("0");
                }
                hex.append(Integer.toHexString(b & 255));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CometPassport.model().registerOnActivityResult(this,requestCode,resultCode,data);
    }

    void XD() {
        String TAG = HomeFragment.class.getSimpleName();
        if (isOffline(this)) {
            Snackbar.make(findViewById(R.id.layout), "Check Internet and Try Again", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            XDSDK.setCallback(new XDCallback() {
                @Override
                public void onInitSucceed() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                }

                @Override
                public void onInitFailed(String msg) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + msg);
                }

                @Override
                public void onLoginSucceed(String token) {
                    XDSDK.openUserCenter();
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + token);
                }

                @Override
                public void onLoginFailed(String msg) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + msg);
                }

                @Override
                public void onLoginCanceled() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                }

                @Override
                public void onGuestBindSucceed(String token) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + token);
                }

                @Override
                public void onLogoutSucceed() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                }

                @Override
                public void onPayCompleted() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                }

                @Override
                public void onPayFailed(String msg) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + msg);
                }

                @Override
                public void onPayCanceled() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                }

                @Override
                public void onRealNameSucceed() {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                }

                @Override
                public void onRealNameFailed(String msg) {
                    Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                }
            });

            XDSDK.initSDK(this, "a4d6xky5gt4c80s", 1, "AndroidChannel", "AndroidVersion", true);
            XDSDK.login();
        }
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
