package com.fqxd.gftools.features.noti;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fqxd.gftools.Global;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import com.fqxd.gftools.R;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.MessageFormat;
import java.util.regex.Pattern;

public class NotiActivity extends AppCompatActivity{

    private static final int RC_SIGN_IN = 100;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noti);

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

        TextView HTU = findViewById(R.id.HTU);
        Linkify.TransformFilter mTransform = (match, url) -> "";
        Pattern pattern1 = Pattern.compile("플러그인");
        Linkify.addLinks(HTU, pattern1, "https://github.com/choiman1559/GF-Tools-Noti-Plugin/releases",null,mTransform);

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

        if (!prefs.getString("uid", "").equals(""))
            guid.setText(MessageFormat.format("Logined as {0}", mAuth.getCurrentUser().getEmail()));
        else guid.setVisibility(View.GONE);

        onoff.setChecked(prefs.getBoolean("Enabled", false));
        onoff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!getSharedPreferences(Global.Prefs, MODE_PRIVATE).getString("uid", "").equals(""))
                FirebaseMessaging.getInstance().subscribeToTopic(getSharedPreferences(Global.Prefs, MODE_PRIVATE).getString("uid", ""));
            prefs.edit().putBoolean("Enabled",onoff.isChecked()).apply();
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
                startActivityForResult(signInIntent, RC_SIGN_IN);
            } else signOut(onoff,prefs);
        });
    }

    public void signOut(SwitchMaterial onoff,SharedPreferences prefs) {
        mAuth.signOut();
        mGoogleSignInClient.signOut();
        NotiActivity.this.recreate();
        prefs.edit().remove("uid").apply();

        onoff.setChecked(false);
        onoff.setEnabled(false);
        prefs.edit().putBoolean("Enabled", false).apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        }
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
