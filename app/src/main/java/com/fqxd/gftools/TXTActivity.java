package com.fqxd.gftools;

import com.nononsenseapps.filepicker.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class TXTActivity extends AppCompatActivity {

    File file = null;
    String packagename = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_txt);

        if (checkPermissions()) {
            Intent i = new Intent(this, TextPicker.class);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
            startActivityForResult(i, 5217);
        } else {
            Toast.makeText(this, "You need to Allow WRITE STORAGE Permission!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermissions() {
        int REQUEST_ID_MULTIPLE_PERMISSIONS = 5217;
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            Log.d("TAG", "Permission" + "\n" + String.valueOf(false));
            return false;
        }
        Log.d("Permission", "Permission" + "\n" + String.valueOf(true));
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 5217 && resultCode == Activity.RESULT_OK) {
            List<Uri> files = Utils.getSelectedFilesFromResult(intent);
            for (Uri uri : files) {
                file = Utils.getFileForUri(uri);
                TextView Fileuri = findViewById(R.id.FileUri);
                Fileuri.setText(file.toString());

                if(file.toString().contains("asset_textes.ab")) onScreen();
                else {
                    Toast.makeText(getApplicationContext(), "Please select asset_textes.ab!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        } else finish();
    }

    public void onScreen() {

        Button runpatch = findViewById(R.id.centrue);
        runpatch.setEnabled(false);

        ArrayList<String> packageNames = new ArrayList<>();
        packageNames.add(getString(R.string.target_cn_uc));
        packageNames.add(getString(R.string.target_cn_bili));
        packageNames.add(getString(R.string.target_en));
        packageNames.add(getString(R.string.target_jp));
        packageNames.add(getString(R.string.target_tw));

        ArrayList<String> p2 = new ArrayList<>();
        p2.add("...");
        for (String s : packageNames) {
            try {
                this.getPackageManager().getPackageInfo(s, 0);
                p2.add(s);

            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        ArrayAdapter<String> packages = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, p2);
        Spinner targetPackages = this.findViewById(R.id.targetPackage);
        packages.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        targetPackages.setAdapter(packages);
        targetPackages.setOnItemSelectedListener(new OnTargetSelectedListener(this));

        final Button run = findViewById(R.id.centrue);
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File f = new File("/storage/emulated/0/Android/data/" + packagename + "/files/Android/New/asset_textes.ab");
                f.delete();
                filecopy(file.toString(),"/storage/emulated/0/Android/data/" + packagename + "/files/Android/New/");
            }
        });
    }

    public void filecopy(String from,String to){
        File file = new File(from);
        try {
            FileInputStream inputStream = new FileInputStream(file);
            FileOutputStream outputStream = new FileOutputStream(to + "asset_textes.ab");

            FileChannel fcin = inputStream.getChannel();
            FileChannel fcout = outputStream.getChannel();

            long size = fcin.size();
            fcin.transferTo(0, size, fcout);

            fcout.close();
            fcin.close();

            outputStream.close();
            inputStream.close();
        }catch (IOException e) {
            ExceptionCatchClass ecc = new ExceptionCatchClass();
            ecc.CatchException(TXTActivity.this,e);
        }
    }

    final class OnTargetSelectedListener implements AdapterView.OnItemSelectedListener {
        private TXTActivity main;

        OnTargetSelectedListener(TXTActivity main) {
            this.main = main;
        }

        @Override
        public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {
            final Spinner spinner = findViewById(R.id.targetPackage);

            if (((TextView)view).getText().equals("...")) {
                return;
            } else {
                packagename = spinner.getSelectedItem().toString();
                Button runpatch = findViewById(R.id.centrue);
                runpatch.setEnabled(true);
            }
        }


        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

            @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
}
