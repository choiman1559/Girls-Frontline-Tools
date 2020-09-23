package com.fqxd.gftools.features.cen;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;

import java.io.IOException;

public class CenActivity extends AppCompatActivity {
    String Package;
    boolean isData;
    boolean isCened;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cen);

        findViewById(R.id.progressbarLayout).setVisibility(View.GONE);
        this.Package = getIntent().getStringExtra("pkg");
        PackageManager pm = getPackageManager();
        TextView pkgInfo = findViewById(R.id.PkgInfo);

        isData = false;
        isCened = true;
        int CenValue = -1;

        try {
            if (!Global.checkRootPermission()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("특수 권한이 필요합니다").setMessage("이 기능을 사용하려면 ROOT 권한이 필요합니다");
                builder.setPositiveButton("슈퍼유저 사용", (dialog, id) -> {
                    try {
                        Runtime.getRuntime().exec("su").waitFor();
                        recreate();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }).setNegativeButton("취소", (dialog, which) -> finish()).show();
            } else {
                isData = CenUtils.isDataAvailable(Package);
                CenValue = CenUtils.checkDatabase(Package);
                if (isData) isCened = CenValue == 1;
                else {
                    AlertDialog.Builder ab = new AlertDialog.Builder(this);
                    ab.setTitle("데이터를 읽어오던중 에러 발생!");
                    ab.setMessage("소전의 데이터를 다운로드 받은 후 다시 시도하세요.");
                    ab.setPositiveButton("OK", ((dialog, which) -> {
                    }));
                    ab.show();
                }
            }
            pkgInfo.setText("target : " + pm.getApplicationLabel(pm.getApplicationInfo(Package, PackageManager.GET_META_DATA)) + " (" + Package + ")");
        } catch (PackageManager.NameNotFoundException | InterruptedException | IOException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error!").setMessage("루트 권한을 인식할수 없습니다! 기기가 루팅이 되어있는지 확인 후 다시 시도하십시오!");
            builder.setPositiveButton("OK", (dialog, id) -> { finish(); });
            builder.create().show();
            e.printStackTrace();
        }

        final TextView status = findViewById(R.id.status);
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

        final Button CEN = findViewById(R.id.centask);
        CEN.setText(isData ? (isCened ? "검열 해제" : "검열 재적용") : "새로 고침");
        CEN.setOnClickListener(v -> {
            if (isData) CenUtils.checkRootAndRunTask(isCened,Package,findViewById(R.id.progressbarLayout),this);
            else recreate();
        });
    }
}
