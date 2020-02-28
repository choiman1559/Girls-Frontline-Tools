package com.fqxd.gftools.alarm.palarm;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.fqxd.gftools.R;
import com.fqxd.gftools.alarm.alarm.AlarmUtills;
import com.fqxd.gftools.alarm.alarm.LSDTableClass;
import com.fqxd.gftools.vpn.VPNConstants;
import com.fqxd.gftools.vpn.utils.VpnServiceHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class PACAlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pacalarm);
        PAlarmAddClass.isasking = false;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(PACAlarmActivity.this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
            if(!Settings.canDrawOverlays(PACAlarmActivity.this)) {
                Toast.makeText(getApplicationContext(), "다른 앱 위에 그리기 권한 없음", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
        SharedPreferences setEnable = getSharedPreferences("ListAlarm", MODE_PRIVATE);
        SharedPreferences.Editor editEnable = setEnable.edit();

        Button deletecache = findViewById(R.id.DeleteCache);
        if(!new File(VPNConstants.BASE_DIR).exists()) deletecache.setEnabled(false);
        deletecache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new deleteCache(PACAlarmActivity.this).execute();
            }
        });

        Switch isEnabled = findViewById(R.id.serviceonoff);
        isEnabled.setChecked(setEnable.getBoolean("isChecked", false));
        if(setEnable.getBoolean("isChecked",false) && !VpnServiceHelper.vpnRunningStatus()) isEnabled.setChecked(false);

        isEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PacketClass pacls = new PacketClass();

                if (!isEnabled.isChecked()) {
                    pacls.endVpn(PACAlarmActivity.this);
                    editEnable.putBoolean("isChecked", false).apply();
                    deletecache.setEnabled(new File(VPNConstants.BASE_DIR).exists());
                } else {
                    pacls.setVpn(PACAlarmActivity.this);
                    pacls.runVpn(PACAlarmActivity.this);
                    editEnable.putBoolean("isChecked", true).apply();
                    PAlarmAddClass.context = PACAlarmActivity.this;
                    PAlarmAddClass.isNO = false;
                }
            }

        });

        Spinner list = findViewById(R.id.List);
        Button Delete = findViewById(R.id.Delete);
        Delete.setEnabled(false);

        int count = setEnable.getInt("PAlarmCount",0);
        ArrayList<String> Mlist = new ArrayList<>();
        Mlist.add("...");
        for(int i = 1;i <= count;i++) {
            SharedPreferences a = getSharedPreferences("p" + Integer.toString(i),MODE_PRIVATE);
            Mlist.add(a.getString("name",""));
        }
        ArrayAdapter<String> Madpt = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,Mlist);
        list.setAdapter(Madpt);

        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PacketClass().cancel(PACAlarmActivity.this,list.getSelectedItem().toString());
                PACAlarmActivity.this.recreate();
            }
        });

        list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {
                if(list.getSelectedItem().toString() != "...") {
                    Delete.setEnabled(true);
                    setText();
                } else {
                    Delete.setEnabled(false);

                    TextView t1 = findViewById(R.id.HMText);
                    TextView t2 = findViewById(R.id.textView7);
                    TextView t3 = findViewById(R.id.etime);

                    t1.setText("Selected Local : UNKNOWN");
                    t2.setText("Estimated elapsed time : UNKNOWN");
                    t3.setText("Estimated end time : UNKNOWN");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        Button test = findViewById(R.id.test);
        test.setCursorVisible(true);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //addSharedprefs(PACAlarmActivity.this,0,0);
                PACAlarmActivity.this.recreate();
            }
        });

    }

    public void addSharedprefs(Context context,int local1, int local2) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("ListAlarm", MODE_PRIVATE);
        int count = sharedPreferences.getInt("PAlarmCount", 0);
        SharedPreferences.Editor editor = context.getSharedPreferences("p" + Integer.toString(count += 1), MODE_PRIVATE).edit();

        editor.putString("package", "kr.txwy.and.snqx");
        editor.putString("name", Integer.toString(local1) + "-" + Integer.toString(local2));
        editor.putInt("H", local1);
        editor.putInt("M", local2);
        editor.putLong("nextAlarm", new AlarmUtills().calculate(local1, local2, context).getTimeInMillis());
        editor.apply();

        sharedPreferences.edit().putInt("PAlarmCount", count).apply();

        new PacketClass().repeat(context,Integer.toString(local1) + "-" + Integer.toString(local2));
    }

    void setText() {
        Spinner list = findViewById(R.id.List);
        TextView t1 = findViewById(R.id.HMText);
        TextView t2 = findViewById(R.id.textView7);
        TextView t3 = findViewById(R.id.etime);

        for(int i = 1;i <= getSharedPreferences("ListAlarm",MODE_PRIVATE).getInt("PAlarmCount",0);i++){
            if(getSharedPreferences("p" + Integer.toString(i),MODE_PRIVATE).getString("name","") == list.getSelectedItem().toString()) {
                SharedPreferences a = getSharedPreferences("p" + Integer.toString(i),MODE_PRIVATE);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(a.getLong("nextAlarm",0));

                LSDTableClass lsdTableClass = new LSDTableClass();
                String HHMM = lsdTableClass.GetLSDTable(a.getInt("H",0),a.getInt("M",0));
                char[] array = HHMM.toCharArray();

                String HH = Character.toString(array[0]) + Character.toString(array[1]);
                String MM = Character.toString(array[2]) + Character.toString(array[3]);

                t1.setText("Selected Local : " + Integer.toString(a.getInt("H",-1)) + "-" + Integer.toString(a.getInt("M",-1)));
                t2.setText("Estimated elapsed time : " + HH + ":" + MM);
                t3.setText("Estimated end time : " + simpleDateFormat.format(cal.getTime()));

                break;
            } else {
                t1.setText("Selected Local : UNKNOWN");
                t2.setText("Estimated elapsed time : UNKNOWN");
                t3.setText("Estimated end time : UNKNOWN");
            }
        }
    }

    public static class deleteCache extends AsyncTask {

        PACAlarmActivity main;
        ProgressDialog progressDialog;

        deleteCache(PACAlarmActivity main) {
            this.main = main;
            progressDialog = new ProgressDialog(this.main);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setProgressStyle(R.style.Widget_AppCompat_ProgressBar_Horizontal);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("working...");
            progressDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            setDirEmpty(VPNConstants.BASE_DIR);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            new File(VPNConstants.BASE_DIR).delete();
            progressDialog.dismiss();
            Toast.makeText(main, "done", Toast.LENGTH_SHORT).show();
            main.recreate();
        }

        static void setDirEmpty(String dirname) {
            String path = dirname;

            File dir = new File(path);
            File[] child = dir.listFiles();

            if (dir.exists()) {
                for (File childfile : child) {
                    if (childfile.isDirectory()) {
                        setDirEmpty(childfile.getAbsolutePath());
                    } else childfile.delete();
                }
            }
            dir.delete();
        }
    }
}