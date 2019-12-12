package com.fqxd.gftools;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class AppActivity extends Activity {

    String packagename = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        final Button run = findViewById(R.id.Runapp);
        final Button del = findViewById(R.id.DeleteApp);
        final ImageView icon = findViewById(R.id.IconView);

        icon.setImageResource(R.drawable.ic_icon_background);
        run.setEnabled(false);
        del.setEnabled(false);

        if (Build.VERSION.SDK_INT >= 26 && !this.getPackageManager().canRequestPackageInstalls()) {
            this.startActivityForResult(
                    new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse("package:" + this.getPackageName())),
                    MainActivity.REQUEST_ACTION_MANAGE_UNKNOWN_APP_SOURCES
            );
        }

        ArrayList<String> packageNames = new ArrayList<>();
        packageNames.add(getString(R.string.target_cn_uc));
        packageNames.add(getString(R.string.target_cn_bili));
        packageNames.add(getString(R.string.target_en));
        packageNames.add(getString(R.string.target_jp));
        packageNames.add(getString(R.string.target_tw));
        packageNames.add(getString(R.string.target_kr));

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

        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getPackageManager().getLaunchIntentForPackage(packagename);
                startActivity(intent);
            }
        });

        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DELETE);
                intent.setData(Uri.parse("package:" + packagename));
                startActivity(intent);
            }
        });
    }

    final class OnTargetSelectedListener implements AdapterView.OnItemSelectedListener {
        private AppActivity main;

        OnTargetSelectedListener(AppActivity main) {
            this.main = main;
        }

        @Override
        public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {

            final Spinner spinner = findViewById(R.id.targetPackage);
            final TextView version = findViewById(R.id.version);
            final TextView appname = findViewById(R.id.appname);
            final TextView server = findViewById(R.id.server);
            final Button run = findViewById(R.id.Runapp);
            final Button del = findViewById(R.id.DeleteApp);
            final ImageView icon = findViewById(R.id.IconView);

            if (((TextView)view).getText().equals("...")) {
                icon.setImageResource(R.drawable.ic_icon_background);
                appname.setText("app name : UNKNOWN");
                version.setText("version : UNKNOWN");
                server.setText("server : UNKNOWN");
                run.setEnabled(false);
                del.setEnabled(false);
                return;
            } else {
                packagename = spinner.getSelectedItem().toString();

                run.setEnabled(true);
                del.setEnabled(true);

                try {
                    PackageManager pm = getApplicationContext().getPackageManager();
                    icon.setImageDrawable(getPackageManager().getApplicationIcon(packagename));
                    appname.setText("app name : " + pm.getApplicationLabel(pm.getApplicationInfo(packagename,PackageManager.GET_META_DATA)));
                    version.setText("version : " + pm.getPackageInfo(packagename, 0).versionName);
                } catch (PackageManager.NameNotFoundException e) { }

                if(packagename == getString(R.string.target_cn_uc) ||
                        packagename == getString(R.string.target_cn_bili) ||
                        packagename == getString(R.string.target_tw)) {
                    server.setText("server : china");
                }

                else if(packagename == getString(R.string.target_en)) server.setText("server : global");
                else if(packagename == getString(R.string.target_kr)) server.setText("server : korea");
                else if(packagename == getString(R.string.target_jp)) server.setText("server : japan");

                else server.setText("server : UNKNOWN");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
