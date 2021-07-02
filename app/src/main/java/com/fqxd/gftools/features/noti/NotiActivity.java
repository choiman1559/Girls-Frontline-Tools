package com.fqxd.gftools.features.noti;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.application.isradeleon.notify.Notify;
import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.global.Global;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import com.fqxd.gftools.R;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class NotiActivity extends AppCompatActivity{

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences prefs;
    ActivityResultLauncher<Intent> startLoginComplete;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noti);
        prefs = getSharedPreferences(Global.Prefs, MODE_PRIVATE);

        ChipGroup select_Group = findViewById(R.id.select_group);
        Chip select_Send = findViewById(R.id.select_send);
        Chip select_Receive = findViewById(R.id.select_receive);

        select_Send.setChecked(prefs.getString("notiMode", "").equals("send"));
        select_Receive.setChecked(prefs.getString("notiMode", "").equals("receive"));

        select_Group.setOnCheckedChangeListener((group, checkedId) -> {
            switch(checkedId) {
                case R.id.select_send:
                    prefs.edit().putString("notiMode", "send").apply();
                    break;

                case R.id.select_receive:
                    prefs.edit().putString("notiMode", "receive").apply();
                    break;
            }
        });

        startLoginComplete = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            GoogleSignInResult loginResult = Auth.GoogleSignInApi.getSignInResultFromIntent(result.getData());
            if (loginResult != null && loginResult.isSuccess()) {
                GoogleSignInAccount account = loginResult.getSignInAccount();
                assert account != null;
                firebaseAuthWithGoogle(account);
            }
        });

        switch (Global.getSHA1Hash(this)) {
            case "cf:61:36:5e:71:42:fa:21:7c:b5:5f:52:6d:e3:d9:06:57:f5:5e:01":
            case "d5:5c:2e:6a:58:4c:3d:4f:4a:3a:08:cd:1c:7e:6a:eb:ee:ea:46:10":
            case "36:47:5f:49:ce:2d:bc:cb:b8:59:30:e3:86:17:85:6c:78:cf:86:53":
                break;

            default:
                new AlertDialog.Builder(this)
                        .setTitle("Error!")
                        .setCancelable(false)
                        .setMessage("기술적 문제로, 이 앱에 사용된 서명키는 이 기능을 사용할 수 없습니다")
                        .setPositiveButton("확인", (d, w) -> finish())
                        .create().show();
                break;
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        mAuth = FirebaseAuth.getInstance();

        final SharedPreferences prefs = getSharedPreferences(Global.Prefs, MODE_PRIVATE);

        final SwitchMaterial onoff = findViewById(R.id.NotiOnoff);
        final Button glogin = findViewById(R.id.glogin);
        final TextView guid = findViewById(R.id.guid);
        final Button test = findViewById(R.id.testrun);

        test.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        test.setOnClickListener(v -> Notify.create(this)
                .setTitle("TestRun")
                .setContent("Test Notification")
                .setLargeIcon(R.drawable.gf_icon)
                .circleLargeIcon()
                .setImportance(Notify.NotificationImportance.MAX)
                .setSmallIcon(R.drawable.start_xd)
                .enableVibration(true)
                .setAutoCancel(true)
                .show());

        if (!prefs.getString("uid", "").equals(""))
            guid.setText(MessageFormat.format("Logined as {0}", mAuth.getCurrentUser().getEmail()));
        else guid.setVisibility(View.GONE);

        TextView HTU = findViewById(R.id.HTU);
        Linkify.TransformFilter mTransform = (match, url) -> "";
        Pattern pattern1 = Pattern.compile("Noti Sender");
        Linkify.addLinks(HTU, pattern1, "https://play.google.com/store/apps/details?id=com.noti.main",null,mTransform);

        onoff.setChecked(prefs.getBoolean("Enabled", false));
        onoff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!getSharedPreferences(Global.Prefs, MODE_PRIVATE).getString("uid", "").equals("")) {
                if(select_Send.isChecked()) {
                    if(onoff.isChecked() && Build.VERSION.SDK_INT > 28 && !Settings.canDrawOverlays(NotiActivity.this)) {
                        Toast.makeText(NotiActivity.this, "이 기능을 사용하기 위해 다른 앱 위에 그리기 권한이 필요합니다!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        onoff.setChecked(false);
                    } else {
                        Set<String> sets = NotificationManagerCompat.getEnabledListenerPackages(NotiActivity.this);
                        if (sets.contains(getPackageName())) {
                            FirebaseMessaging.getInstance().subscribeToTopic(Objects.requireNonNull(prefs.getString("uid", "")));
                            prefs.edit().putBoolean("Enabled", onoff.isChecked()).apply();
                        } else {
                            Toast.makeText(NotiActivity.this, "이 기능을 사용하기 위해 알람 엑세스 권한이 필요합니다!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                            prefs.edit().putBoolean("Enabled", false).apply();
                            onoff.setChecked(false);
                        }
                    }
                } else if(select_Receive.isChecked()) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Objects.requireNonNull(prefs.getString("uid", "")));
                    prefs.edit().putBoolean("Enabled", onoff.isChecked()).apply();
                } else {
                    onoff.setChecked(false);
                    Toast.makeText(NotiActivity.this, "모드를 선택해 주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (!prefs.getString("uid", "").equals("")) {
            glogin.setText(R.string.glogin);
            onoff.setEnabled(true);
        }
        else  {
            glogin.setText(R.string.glogout);
            onoff.setChecked(false);
            onoff.setEnabled(false);
            prefs.edit().putBoolean("Enabled", false).apply();
        }
        glogin.setOnClickListener(v -> {
            if (prefs.getString("uid", "").equals("")) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startLoginComplete.launch(signInIntent);
            } else signOut(onoff);
        });
    }

    public void signOut(SwitchMaterial onoff) {
        mAuth.signOut();
        mGoogleSignInClient.signOut();
        NotiActivity.this.recreate();
        prefs.edit().remove("uid").apply();

        onoff.setChecked(false);
        onoff.setEnabled(false);
        prefs.edit().putBoolean("Enabled", false).apply();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(NotiActivity.this, "인증 실패", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(NotiActivity.this, "구글 로그인 인증 성공", Toast.LENGTH_SHORT).show();
                        final TextView guid = findViewById(R.id.guid);
                        guid.setText(MessageFormat.format("User Id : {0}", mAuth.getUid()));
                        getSharedPreferences(Global.Prefs, MODE_PRIVATE).edit().putString("uid", mAuth.getUid()).apply();
                        NotiActivity.this.recreate();
                    }
                });
    }
}
