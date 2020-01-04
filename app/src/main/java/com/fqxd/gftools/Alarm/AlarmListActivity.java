package com.fqxd.gftools;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AlarmListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);

        Spinner list = findViewById(R.id.List);
        Button Delete = findViewById(R.id.Delete);
        Button Edit = findViewById(R.id.Edit);
        Button Add = findViewById(R.id.Add);
        Switch Set = findViewById(R.id.switch2);
        Switch Cal = findViewById(R.id.CaliSwitch);

        Delete.setEnabled(false);
        Edit.setEnabled(false);
        Set.setEnabled(false);
        Set.setEnabled(false);

        SharedPreferences sharedPreferences = getSharedPreferences("ListAlarm",MODE_PRIVATE);
        int count = sharedPreferences.getInt("AlarmCount",0);

        ArrayList<String> Mlist = new ArrayList<>();
        Mlist.add("...");
        for(int i = 1;i <= count;i++) {
            SharedPreferences a = getSharedPreferences(Integer.toString(i),MODE_PRIVATE);
            Mlist.add(a.getString("name",""));
        }
        ArrayAdapter<String> Madpt = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,Mlist);
        list.setAdapter(Madpt);

        Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlarmListActivity.this,AddAlarmActivity.class);
                for(int i = 1;i <= count;i++){
                    if(getSharedPreferences(Integer.toString(i),MODE_PRIVATE).getString("name","") == list.getSelectedItem().toString()) {
                        intent.putExtra("Prefsnum",i);
                        intent.putExtra("isAdd",false);
                    }
                }
                startActivityForResult(intent,0x01);
                AlarmListActivity.this.finish();
            }
        });

        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count > 1) {
                    for(int i = 1;i <= count;i++){
                    if(getSharedPreferences(Integer.toString(i),MODE_PRIVATE).getString("name","") == list.getSelectedItem().toString()) {
                        if(count == i) {
                            SharedPreferences c = getSharedPreferences(Integer.toString(count),MODE_PRIVATE);
                            SharedPreferences.Editor o = c.edit();

                            o.clear();
                            o.apply();
                        } else {
                                for(int j = i + 1;j <= count;j++) {
                                    SharedPreferences.Editor o = getSharedPreferences(Integer.toString(j - 1), MODE_PRIVATE).edit();
                                    SharedPreferences c = getSharedPreferences(Integer.toString(j), MODE_PRIVATE);

                                    o.putString("name", c.getString("name", ""));
                                    o.putString("package", c.getString("package", ""));
                                    o.putInt("H", c.getInt("H", -1));
                                    o.putInt("M", c.getInt("M", -1));
                                    o.putLong("nextAlarm", c.getLong("nextAlarm", -1));
                                    o.putBoolean("isChecked", c.getBoolean("isChecked", false));

                                    o.apply();
                                   SharedPreferences.Editor s = c.edit();
                                    s.clear().apply();
                                }
                            }
                        }
                    }
                } else if(count == 1) {
                    SharedPreferences c = getSharedPreferences("1",MODE_PRIVATE);
                    SharedPreferences.Editor o = c.edit();

                    o.clear();
                    o.apply();
                }

                SharedPreferences a = getSharedPreferences("ListAlarm",MODE_PRIVATE);
                SharedPreferences.Editor b = a.edit();

                int an = a.getInt("AlarmCount",1);
                b.putInt("AlarmCount",an - 1);
                b.apply();
                AlarmListActivity.this.recreate();
            }
        });

        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlarmListActivity.this,AddAlarmActivity.class);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("AlarmCount",count);
                editor.apply();

                intent.putExtra("Prefsnum",count + 1);
                intent.putExtra("isAdd",true);
                startActivityForResult(intent,0x01);
                AlarmListActivity.this.finish();
            }
        });

        list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {
                if(list.getSelectedItem().toString() != "...") {
                    Delete.setEnabled(true);
                    Edit.setEnabled(true);
                    Set.setEnabled(true);
                    //Cal.setEnabled(true);
                    setText();
                } else {
                    Delete.setEnabled(false);
                    Edit.setEnabled(false);
                    Set.setEnabled(false);
                    Cal.setEnabled(false);

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

        Set.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int count = 0;

                for(int i = 1;i <= getSharedPreferences("ListAlarm",MODE_PRIVATE).getInt("AlarmCount",0);i++) {
                    if (getSharedPreferences(Integer.toString(i), MODE_PRIVATE).getString("name", "") == list.getSelectedItem().toString()) {
                        count = i;
                        break;
                    }
                }

                SharedPreferences.Editor e = getSharedPreferences(Integer.toString(count),MODE_PRIVATE).edit();
                AlarmUtills alarmUtills = new AlarmUtills();
                if(Set.isChecked() && count != 0) {
                    alarmUtills.repeat(getSharedPreferences(Integer.toString(count),MODE_PRIVATE),AlarmListActivity.this,count);
                    e.putBoolean("isChecked",true);
                    e.apply();
                } else if(count != 0) {
                    alarmUtills.cancel(getSharedPreferences(Integer.toString(count),MODE_PRIVATE),AlarmListActivity.this,count);
                    e.putBoolean("isChecked",false);
                    e.apply();
                } else throw new IllegalArgumentException("Unknown error : AlarmListActivity.java - at line 141");
            }
        });

        Cal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int count = 0;

                for(int i = 1;i <= getSharedPreferences("ListAlarm",MODE_PRIVATE).getInt("AlarmCount",0);i++) {
                    if (getSharedPreferences(Integer.toString(i), MODE_PRIVATE).getString("name", "") == list.getSelectedItem().toString()) {
                        count = i;
                        break;
                    }
                }
                SharedPreferences.Editor e = getSharedPreferences(Integer.toString(count),MODE_PRIVATE).edit();
                if(Cal.isChecked() && count != 0) {
                    e.putBoolean("isCal",true);
                    e.apply();
                } else if(count != 0) {
                    e.putBoolean("isCal",false);
                    e.apply();
                } else throw new IllegalArgumentException("Unknown error : AlarmListActivity.java - at line 167");
            }
        });
    }

    void setText() {
        Spinner list = findViewById(R.id.List);
        TextView t1 = findViewById(R.id.HMText);
        TextView t2 = findViewById(R.id.textView7);
        TextView t3 = findViewById(R.id.etime);
        Switch e1 = findViewById(R.id.switch2);
        Switch e2 = findViewById(R.id.CaliSwitch);

        for(int i = 1;i <= getSharedPreferences("ListAlarm",MODE_PRIVATE).getInt("AlarmCount",0);i++){
            if(getSharedPreferences(Integer.toString(i),MODE_PRIVATE).getString("name","") == list.getSelectedItem().toString()) {
                SharedPreferences a = getSharedPreferences(Integer.toString(i),MODE_PRIVATE);

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

                if(a.getBoolean("isChecked",false)) e1.setChecked(true);
                else e1.setChecked(false);

                if(a.getBoolean("isCal",false)) e2.setChecked(true);
                else e2.setChecked(false);

                break;
            } else {
                t1.setText("Selected Local : UNKNOWN");
                t2.setText("Estimated elapsed time : UNKNOWN");
                t3.setText("Estimated end time : UNKNOWN");
            }
        }
    }
}
