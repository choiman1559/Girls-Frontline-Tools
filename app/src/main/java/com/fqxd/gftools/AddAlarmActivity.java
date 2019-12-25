package com.fqxd.gftools;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AddAlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("LSD_Alarm",Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);
        Spinner H = findViewById(R.id.HSpinner);
        Spinner M = findViewById(R.id.MSpinner);
        Switch aSwitch = findViewById(R.id.switch1);

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

        targetPackages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {
                SharedPreferences.Editor sharedPreferences = AddAlarmActivity.this.getSharedPreferences("LSD_Alarm", Context.MODE_PRIVATE).edit();
                sharedPreferences.putString("package",targetPackages.getSelectedItem().toString());
                sharedPreferences.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        ArrayList<String> Hlist = new ArrayList<>();
        Hlist.add("...");
        for(int i = 0;i <= 11;i++) {Hlist.add(Integer.toString(i));}
        ArrayAdapter<String> Hadpt = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,Hlist);
        H.setAdapter(Hadpt);

        ArrayList<String> Mlist = new ArrayList<>();
        Mlist.add("...");
        for(int i = 1;i <= 4;i++) {Mlist.add(Integer.toString(i));}
        ArrayAdapter<String> Madpt = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,Mlist);
        M.setAdapter(Madpt);

        H.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {
                setText();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        M.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {
                setText();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPreferences = getSharedPreferences("LSD_Alarm",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                int H = sharedPreferences.getInt("H",-1);
                int M = sharedPreferences.getInt("M",-1);

                Spinner sH = findViewById(R.id.HSpinner);
                Spinner sM = findViewById(R.id.MSpinner);
                Spinner sT = findViewById(R.id.targetPackage);

                if(aSwitch.isChecked()) {
                    editor.putBoolean("isChecked",true);
                    editor.apply();
                    sH.setEnabled(false);
                    sM.setEnabled(false);
                    sT.setEnabled(false);
                    repeat();
                 } else {
                    editor.putBoolean("isChecked",false);
                    editor.apply();
                    sH.setEnabled(true);
                    sM.setEnabled(true);
                    sT.setEnabled(true);

                    PackageManager pm = AddAlarmActivity.this.getPackageManager();
                    ComponentName componentName = new ComponentName(AddAlarmActivity.this,DeviceBootReceiver.class);
                    Intent intent = new Intent(AddAlarmActivity.this,AlarmReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(AddAlarmActivity.this,0,intent,0);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    if(PendingIntent.getBroadcast(AddAlarmActivity.this,0,intent,0) != null && alarmManager != null) {
                        alarmManager.cancel(pendingIntent);
                        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
                    }
                }
            }
        });


        if(sharedPreferences.getBoolean("isChecked",false)) {
               aSwitch.setChecked(true);
               aSwitch.setEnabled(true);
        }
    }

    void repeat(){
        SharedPreferences sharedPreferences = getSharedPreferences("LSD_Alarm",Context.MODE_PRIVATE);
        PackageManager pm = AddAlarmActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(AddAlarmActivity.this,DeviceBootReceiver.class);
        Intent intent = new Intent(AddAlarmActivity.this,AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(AddAlarmActivity.this,0,intent,0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC,sharedPreferences.getLong("nextAlarm",0),AlarmManager.INTERVAL_DAY,pendingIntent);
        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC,sharedPreferences.getLong("nextAlarm",0),pendingIntent);
        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
    }

    public Calendar calculate(Context ctx,int H,int M){
        LSDTableClass lsdTableClass = new LSDTableClass();
        String HHMM = lsdTableClass.GetLSDTable(H,M);
        char[] array = HHMM.toCharArray();

        String HH = Character.toString(array[0]) + Character.toString(array[1]);
        String MM = Character.toString(array[2]) + Character.toString(array[3]);


        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.add(cal.HOUR,Integer.parseInt(HH));
        cal.add(cal.MINUTE,Integer.parseInt(MM));

        SharedPreferences sharedPreferences = ctx.getSharedPreferences("LSD_Alarm", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("H",H);
        editor.putInt("M",M);
        editor.putLong("nextAlarm",cal.getTimeInMillis());

        editor.apply();
        return cal;
    }

    void setText(){
        Spinner H = findViewById(R.id.HSpinner);
        Spinner M = findViewById(R.id.MSpinner);
        Spinner T = findViewById(R.id.targetPackage);

        TextView etime = findViewById(R.id.etime);
        TextView textView = findViewById(R.id.textView7);

        Switch aSwitch = findViewById(R.id.switch1);

        String Htext = H.getSelectedItem().toString();
        String Mtext = M.getSelectedItem().toString();
        String Ttext = T.getSelectedItem().toString();


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        if(!(Htext == "..." || Mtext == "..." || Ttext == "...")) {
            LSDTableClass lsdTableClass = new LSDTableClass();
            String HHMM = lsdTableClass.GetLSDTable(Integer.parseInt(Htext),Integer.parseInt(Mtext));
            char[] array = HHMM.toCharArray();

            String HH = Character.toString(array[0]) + Character.toString(array[1]);
            String MM = Character.toString(array[2]) + Character.toString(array[3]);

            aSwitch.setEnabled(true);
            Calendar cal = calculate(AddAlarmActivity.this,Integer.parseInt(Htext),Integer.parseInt(Mtext));
            etime.setText("end time : " + simpleDateFormat.format(cal.getTime()));
            textView.setText("prediction time : " + HH + ":" + MM);
        } else {
            textView.setText("prediction time : UNKNOWN");
            etime.setText("end time : UNKNOWN");
            aSwitch.setEnabled(false);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("LSD_Alarm",Context.MODE_PRIVATE);
        if(sharedPreferences.getBoolean("isChecked",false)) {
            aSwitch.setChecked(true);
            aSwitch.setEnabled(true);
        }
    }
}
