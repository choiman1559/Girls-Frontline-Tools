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
            status.setText(R.string.Cen_Status_NoDataFile);
        } else {
            if (isCened) {
                status.setTextColor(Color.parseColor("#F44336"));
                status.setText(R.string.Cen_Status_Applied);
            } else if (CenValue != -1) {
                status.setTextColor(Color.parseColor("#448AFF"));
                status.setText(R.string.Cen_Status_Disabled);
            } else {
                status.setTextColor(Color.parseColor("#78909C"));
                status.setText(R.string.Cen_Status_NoDataItem);
                isData = false;
            }
        }

        final MaterialButton CEN = findViewById(R.id.centask);
        CEN.setText(isData ? (isCened ? getString(R.string.Cen_Button_Disable) : getString(R.string.Cen_Button_Apply)) : getString(R.string.Cen_Button_Refresh));
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
                    ab.setTitle(R.string.Cen_Error_Post_Copy_Title);
                    ab.setMessage(R.string.Cen_Error_Post_Copy_Content);
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
                    builder.setTitle(R.string.Cen_Button_NeedRoot_Title);
                    builder.setMessage(R.string.Cen_Button_NeedRoot_Content);
                    builder.setPositiveButton(R.string.Cen_Button_NeedRoot_OK, (dialog, id) -> {
                        try {
                            Runtime.getRuntime().exec("su").waitFor();
                            recreate();
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).setNegativeButton(R.string.Global_Cancel, (dialog, which) -> finish()).show();
                    break;

                case -2:
                    builder.setTitle(R.string.Cen_Error_Post_ReadData_Title);
                    builder.setMessage(R.string.Cen_Error_Post_ReadData_Content);
                    builder.setPositiveButton(R.string.Global_OK, ((dialog, which) -> finish()));
                    builder.show();
                    break;

                case -3:
                    builder.setTitle(R.string.Cen_Error_Post_Root_Title);
                    builder.setMessage(R.string.Cen_Error_Post_Root_Content);
                    builder.setPositiveButton(R.string.Global_OK, (dialog, id) -> finish());
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
