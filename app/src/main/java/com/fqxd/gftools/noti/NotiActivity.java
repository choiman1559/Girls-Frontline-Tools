package com.fqxd.gftools.noti;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import com.fqxd.gftools.R;
import com.google.firebase.messaging.FirebaseMessaging;

public class NotiActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final int RC_SIGN_IN = 100;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noti);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mAuth = FirebaseAuth.getInstance();

        final SharedPreferences prefs = getSharedPreferences("NotiPrefs", MODE_PRIVATE);

        final Switch onoff = findViewById(R.id.NotiOnoff);
        final Button glogin = findViewById(R.id.glogin);
        final TextView guid = findViewById(R.id.guid);

        if (!prefs.getString("uid", "").equals(""))
            guid.setText("User Id : " + prefs.getString("uid", ""));
        else guid.setText(R.string.guid);

        onoff.setChecked(prefs.getBoolean("Enabled", false));
        onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!getSharedPreferences("NotiPrefs", MODE_PRIVATE).getString("uid", "").equals(""))
                    FirebaseMessaging.getInstance().subscribeToTopic(getSharedPreferences("NotiPrefs", MODE_PRIVATE).getString("uid", ""));
                prefs.edit().putBoolean("Enabled",onoff.isChecked()).apply();
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
        glogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prefs.getString("uid", "").equals("")) {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                } else signOut(onoff,prefs);
            }
        });
    }

    public void signOut(Switch onoff,SharedPreferences prefs) {
        mAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
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
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(NotiActivity.this, "인증 실패", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NotiActivity.this, "구글 로그인 인증 성공", Toast.LENGTH_SHORT).show();
                            final TextView guid = findViewById(R.id.guid);
                            guid.setText("User Id : " + mAuth.getUid());
                            getSharedPreferences("NotiPrefs", MODE_PRIVATE).edit().putString("uid", mAuth.getUid()).apply();
                            NotiActivity.this.recreate();
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }
}
