package com.fqxd.gftools.features.gfd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import com.fqxd.gftools.R;

public class GFDActivity extends AppCompatActivity {

    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Button button5;
    Button button6;
    Button button7;
    Button button8;
    Button button9;
    Button button10;
    Button button11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gfd);

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        button6 = findViewById(R.id.button6);
        button7 = findViewById(R.id.button7);
        button8 = findViewById(R.id.button8);
        button9 = findViewById(R.id.button9);
        button10 = findViewById(R.id.button10);
        button11 = findViewById(R.id.button11);

        Button.OnClickListener onClickListener = v -> {
            switch (v.getId()) {
                case R.id.button1:
                    SendIntent(1);
                    break;

                case R.id.button2:
                    SendIntent(2);
                    break;

                case R.id.button3:
                    SendIntent(3);
                    break;

                case R.id.button4:
                    SendIntent(4);
                    break;

                case R.id.button5:
                    SendIntent(5);
                    break;

                case R.id.button6:
                    SendIntent(6);
                    break;

                case R.id.button7:
                    SendIntent(7);
                    break;

                case R.id.button8:
                    SendIntent(8);
                    break;

                case R.id.button9:
                    SendIntent(9);
                    break;

                case R.id.button10:
                    SendIntent(10);
                    break;

                case R.id.button11:
                    RunGFDv2();
                    break;
            }
        };

        button1.setOnClickListener(onClickListener);
        button2.setOnClickListener(onClickListener);
        button3.setOnClickListener(onClickListener);
        button4.setOnClickListener(onClickListener);
        button5.setOnClickListener(onClickListener);
        button6.setOnClickListener(onClickListener);
        button7.setOnClickListener(onClickListener);
        button8.setOnClickListener(onClickListener);
        button9.setOnClickListener(onClickListener);
        button10.setOnClickListener(onClickListener);
        button11.setOnClickListener(onClickListener);

    }


    public void RunGFDv2(){
        try{
            getPackageManager().getPackageInfo("com.gfl.dic", PackageManager.GET_ACTIVITIES);
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.gfl.dic");
            startActivity(intent);

        } catch(PackageManager.NameNotFoundException e) {
            Intent intend = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.gfl.dic"));
            startActivity(intend);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void SendIntent(int butnum) {
        Intent intent = new Intent(this, GFDViewer.class);
        intent.putExtra("buttonnum",butnum);
        startActivityForResult(intent,0x00);
        finish();
    }
}
