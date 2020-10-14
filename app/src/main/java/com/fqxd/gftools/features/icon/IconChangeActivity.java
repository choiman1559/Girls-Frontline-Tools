package com.fqxd.gftools.features.icon;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.Global;
import com.fqxd.gftools.MainActivity;
import com.fqxd.gftools.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import gun0912.tedbottompicker.TedBottomPicker;

public class IconChangeActivity extends AppCompatActivity {
    static String Package = null;
    static Uri ImageUri = null;
    static Boolean isTaskRunning = false;

    ImageView imagePreview;
    Button patchButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ico);
        new AlertDialog.Builder(this)
                .setTitle("경고!")
                .setMessage("이 기능을 사용하면 다시 공식 클라이언트로 재설치하기 이전까지 결재 등의 Play Service 를 요하는 기능은 작동하지 않을것이며,\"계속\" 버튼을 눌러 이 기능을 사용할경우 이 기능을 사용하다 발생한 불이익은 순전히 이 기능을 사용한 본인에게 있음을 인지하고 동의한것으로 간주합니다.")
                .setPositiveButton("계속", (dialog, which) -> {})
                .setNegativeButton("취소",((dialog, which) -> finish()))
                .setCancelable(false).show();
        Package = getIntent().getStringExtra("pkg");

        if (Build.VERSION.SDK_INT >= 23 && this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return;
        }
        if (Build.VERSION.SDK_INT >= 26 && !this.getPackageManager().canRequestPackageInstalls()) {
            this.startActivityForResult(
                    new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse("package:" + this.getPackageName())),
                    MainActivity.REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            finish();
        }
        TextView packageinfo = this.findViewById(R.id.packageinfo);
        try {
            PackageManager pm = this.getPackageManager();
            packageinfo.setText(String.format("target : %s (%s)", pm.getApplicationLabel(pm.getApplicationInfo(Package, PackageManager.GET_META_DATA)), Package));
        } catch (PackageManager.NameNotFoundException ignored) { }

        final TextView status = findViewById(R.id.status);
        final TextView log = findViewById(R.id.log);
        final ProgressBar progress = findViewById(R.id.progress);
        final LinearLayout layout = findViewById(R.id.progressLayout);

        layout.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        imagePreview = findViewById(R.id.iconPreview);
        patchButton = findViewById(R.id.centrue);
        patchButton.setOnClickListener(v -> {
            if(ImageUri == null) selectImage();
            else {
                patchButton.setEnabled(false);
                layout.setVisibility(View.VISIBLE);
                progress.setVisibility(View.VISIBLE);
                isTaskRunning = true;
                IconChangeTask patchTask = new IconChangeTask(this, status, log, progress,Package,new File(ImageUri.getPath()));
                patchTask.execute(new Object[]{(Runnable) () -> patchButton.post(() -> {
                    patchButton.setEnabled(true);
                    layout.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                    isTaskRunning = false;
                })});
            }
        });
    }

    private void selectImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 3333);
        else runPicker();
    }

    private void runPicker() {
        TedBottomPicker.with(IconChangeActivity.this)
                .setOnErrorListener(message -> finish())
                .show(uri -> CropImage.activity(uri)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setRequestedSize(192,192)
                        .setFixAspectRatio(true)
                        .setAspectRatio(1,1)
                        .start(IconChangeActivity.this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) Toast.makeText(this,"카메라 권한이 없습니다!",Toast.LENGTH_SHORT).show();
        else runPicker();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                ImageUri = result.getUri();
                imagePreview.setImageURI(ImageUri);
                patchButton.setText(getString(R.string.run_patch));
            }

            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                result.getError().printStackTrace();
                new AlertDialog.Builder(this)
                        .setTitle("에러 발생!")
                        .setMessage(result.getError().toString())
                        .setNegativeButton("확인",((dialog, which) -> finish()))
                        .setCancelable(false).show();
            } else finish();
        }

        if(requestCode == 5555) {
            File temp = getExternalFilesDir(null);
            File originalObb = (new File(Global.Storage + "/Android/obb/" + Package));
            File obb = new File(temp.getAbsolutePath() + "/obb");
            copyDirectory(obb,originalObb);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            File originalApk = new File(temp.getAbsolutePath() + "/signed.apk");
            Uri uri = Build.VERSION.SDK_INT <= 23 ? Uri.fromFile(originalApk) : FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", originalApk);
            if (Build.VERSION.SDK_INT <= 23) {
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
            } else {
                intent.setData(uri);
            }
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, 0x00);
        }

        if (requestCode == 0x00) {
            new File(this.getExternalFilesDir(null).getAbsolutePath() + "/signed.apk").delete();
            new File(this.getExternalFilesDir(null).getAbsolutePath() + "/base.apk").delete();
        }
        if (requestCode == MainActivity.REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES) {
            if (Build.VERSION.SDK_INT < 26 || resultCode == Activity.RESULT_OK) {
                this.finish();
                this.startActivity(this.getIntent());
            } else {
                this.startActivityForResult(
                        new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse("package:" + this.getPackageName())),
                        MainActivity.REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES
                );
            }
        }
    }

    public static void copyDirectory(File sourceF, File targetF){
        File[] target_file = sourceF.listFiles();
        for (File file : target_file) {
            File temp = new File(targetF.getAbsolutePath() + File.separator + file.getName());
            if(file.isDirectory()){
                temp.mkdir();
                copyDirectory(file, temp);
            } else {
                FileInputStream fis = null;
                FileOutputStream fos = null;
                try {
                    fis = new FileInputStream(file);
                    fos = new FileOutputStream(temp) ;
                    byte[] b = new byte[4096];
                    int cnt = 0;
                    while((cnt=fis.read(b)) != -1){
                        fos.write(b, 0, cnt);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally{
                    try {
                        fis.close();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(!isTaskRunning) finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !isTaskRunning && super.onTouchEvent(event);
    }
}
