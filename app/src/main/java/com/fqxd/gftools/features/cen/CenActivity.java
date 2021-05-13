package com.fqxd.gftools.features.cen;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.fqxd.gftools.global.Global;
import com.fqxd.gftools.R;
import com.fqxd.gftools.implement.AsyncTask;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

public class CenActivity extends AppCompatActivity {
    private static String Package;
    private static boolean isData;
    private static boolean isCened;
    private static int CenValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cen);

        Package = getIntent().getStringExtra("pkg");
        PackageManager pm = getPackageManager();
        TextView pkgInfo = findViewById(R.id.PkgInfo);

        isData = false;
        isCened = true;
        CenValue = -1;

        try {
            pkgInfo.setText(String.format("target : %s (%s)", pm.getApplicationLabel(pm.getApplicationInfo(Package, PackageManager.GET_META_DATA)), Package));
        } catch (PackageManager.NameNotFoundException e) {
          e.printStackTrace();
        }

        new InitTask(this).execute();
    }

    private void init() {
        final TextInputEditText status = findViewById(R.id.status);
        if (!isData) {
            status.setTextColor(Color.parseColor("#78909C"));
            status.setText("데이터 파일 없음");
        } else {
            if (isCened) {
                status.setTextColor(Color.parseColor("#F44336"));
                status.setText("검열 적용됨");
            } else if (CenValue != -1) {
                status.setTextColor(Color.parseColor("#448AFF"));
                status.setText("검열 해제됨");
            } else {
                status.setTextColor(Color.parseColor("#78909C"));
                status.setText("데이터 항목 없음");
                isData = false;
            }
        }

        final MaterialButton CEN = findViewById(R.id.centask);
        CEN.setText(isData ? (isCened ? "검열 해제" : "검열 재적용") : "새로 고침");
        CEN.setOnClickListener(v -> {
            if (isData) new PatchTask(this).execute();
            else recreate();
        });
    }

    private class PatchTask extends AsyncTask<Void, Void, Integer> {
        Context context;

        public PatchTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.progressbarLayout).setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return CenUtils.checkRootAndRunTask(isCened, Package);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            switch (integer) {
                case -1:
                    AlertDialog.Builder ab = new AlertDialog.Builder(context);
                    ab.setTitle("데이터 복사 에러!");
                    ab.setMessage("소전의 데이터를 다운로드 받은 후 다시 시도하세요.");
                    ab.setPositiveButton("OK", ((dialog, which) -> CenActivity.this.finish()));
                    ab.show();
                    break;

                case -2:
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Error!").setMessage("Can't get Root permission! Please check if su is installed on your device and try again!");
                    builder.setPositiveButton("OK", (dialog, id) -> findViewById(R.id.progressbarLayout).setVisibility(View.GONE));
                    builder.create().show();
                    break;

                default:
                    recreate();
                    break;
            }
        }
    }

    private class InitTask extends AsyncTask<Void, Void, Integer> {
        Context context;

        public InitTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.progressbarLayout).setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                if (!Global.checkRootPermission()) {
                    return -1;
                } else {
                    isData = CenUtils.isDataAvailable(Package);
                    CenValue = CenUtils.checkDatabase(Package);
                    if (isData) isCened = CenValue == 1;
                    else {
                        return -2;
                    }
                }
            } catch (Exception e) {
                return -3;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer value) {
            super.onPostExecute(value);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);

            switch (value) {
                case -1:
                    builder.setTitle("특수 권한이 필요합니다");
                    builder.setMessage("이 기능을 사용하려면 ROOT 권한이 필요합니다");
                    builder.setPositiveButton("슈퍼유저 사용", (dialog, id) -> {
                        try {
                            Runtime.getRuntime().exec("su").waitFor();
                            recreate();
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).setNegativeButton("취소", (dialog, which) -> finish()).show();
                    break;

                case -2:
                    builder.setTitle("데이터를 읽어오던중 에러 발생!");
                    builder.setMessage("소전의 데이터를 다운로드 받은 후 다시 시도하세요.");
                    builder.setPositiveButton("OK", ((dialog, which) -> finish()));
                    builder.show();
                    break;

                case -3:
                    builder.setTitle("Error!");
                    builder.setMessage("루트 권한을 인식할수 없습니다! 기기가 루팅이 되어있는지 확인 후 다시 시도하십시오!");
                    builder.setPositiveButton("OK", (dialog, id) -> finish());
                    builder.create().show();
                    break;

                default:
                    findViewById(R.id.progressbarLayout).setVisibility(View.GONE);
                    break;
            }
            init();
        }
    }
}
