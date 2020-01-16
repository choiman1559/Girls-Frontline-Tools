package com.fqxd.gftools.alarm.alarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.fqxd.gftools.R;

public class AddAlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences defaults = getSharedPreferences("ListAlarm", MODE_PRIVATE);
        Intent getInt = getIntent();
        int Num = getInt.getIntExtra("Prefsnum", defaults.getInt("AlarmCount", -1));
        boolean isAdd = getInt.getBooleanExtra("isAdd", true);
        SharedPreferences sharedPreferences = getSharedPreferences(Integer.toString(Num), MODE_PRIVATE);

        setContentView(R.layout.activity_add_alarm);
        Spinner H = findViewById(R.id.HSpinner);
        Spinner M = findViewById(R.id.MSpinner);
        Button Save = findViewById(R.id.Save);
        Button Cancel = findViewById(R.id.Cancel);
        EditText NameEdit = findViewById(R.id.name);

        if (!isAdd) NameEdit.setText(sharedPreferences.getString("name", ""));

        ArrayList<String> packageNames = new ArrayList<>();
        packageNames.add(getString(R.string.target_cn_uc));
        packageNames.add(getString(R.string.target_cn_bili));
        packageNames.add(getString(R.string.target_en));
        packageNames.add(getString(R.string.target_jp));
        packageNames.add(getString(R.string.target_tw));
        packageNames.add(getString(R.string.target_kr));

        Save.setEnabled(false);
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

        NameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                setText(sharedPreferences);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setText(sharedPreferences);
            }

            @Override
            public void afterTextChanged(Editable s) {
                setText(sharedPreferences);
            }
        });

        targetPackages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {
                if (targetPackages.getSelectedItem().toString() != "...") {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("package", targetPackages.getSelectedItem().toString());
                    editor.apply();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ArrayList<String> Hlist = new ArrayList<>();
        Hlist.add("...");
        for (int i = 0; i <= 12; i++) {
            Hlist.add(Integer.toString(i));
        }
        ArrayAdapter<String> Hadpt = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, Hlist);
        H.setAdapter(Hadpt);

        ArrayList<String> Mlist = new ArrayList<>();
        Mlist.add("...");
        for (int i = 1; i <= 4; i++) {
            Mlist.add(Integer.toString(i));
        }
        ArrayAdapter<String> Madpt = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, Mlist);
        M.setAdapter(Madpt);

        H.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {
                setText(sharedPreferences);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        M.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {
                setText(sharedPreferences);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (NameEdit.getText().toString() == "") {
                    Snackbar.make(v, "이름을 입력해 주십시오!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {

                    for (int i = 1; i <= getSharedPreferences("ListAlarm", MODE_PRIVATE).getInt("AlarmCount", 0); i++) {
                        if (getSharedPreferences(Integer.toString(i), MODE_PRIVATE).getString("name", "") == NameEdit.getText().toString()) {
                            Snackbar.make(v, "이미 동일한 이름의 알림이 있습니다!", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            return;
                        }
                    }

                    AlarmUtills alarmUtills = new AlarmUtills();
                    alarmUtills.editSharedPrefs(sharedPreferences,
                            Integer.parseInt(H.getSelectedItem().toString()), Integer.parseInt(M.getSelectedItem().toString()),
                            alarmUtills.calculate(Integer.parseInt(H.getSelectedItem().toString()),
                                    Integer.parseInt(M.getSelectedItem().toString()), AddAlarmActivity.this),
                            targetPackages.getSelectedItem().toString(), NameEdit.getText().toString(), AddAlarmActivity.this);

                    if (isAdd) {
                        SharedPreferences a = AddAlarmActivity.this.getSharedPreferences("ListAlarm", MODE_PRIVATE);
                        SharedPreferences.Editor b = a.edit();

                        b.putInt("AlarmCount", a.getInt("AlarmCount", 0) + 1);
                        b.apply();
                    }
                    startActivity(new Intent(AddAlarmActivity.this, AlarmListActivity.class));
                    AddAlarmActivity.this.finish();
                }
            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddAlarmActivity.this, AlarmListActivity.class));
                AddAlarmActivity.this.finish();
            }
        });
    }

    void setText(SharedPreferences sharedPreferences) {
        Spinner H = findViewById(R.id.HSpinner);
        Spinner M = findViewById(R.id.MSpinner);
        Spinner T = findViewById(R.id.targetPackage);
        EditText N = findViewById(R.id.name);

        TextView etime = findViewById(R.id.etime);
        TextView textView = findViewById(R.id.textView7);
        Button Save = findViewById(R.id.Save);

        String Htext = H.getSelectedItem().toString();
        String Mtext = M.getSelectedItem().toString();
        String Ttext = T.getSelectedItem().toString();
        String Ntext = N.getText().toString();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        if (!(Htext == "..." || Mtext == "..." || Ttext == "..." || Ntext == "")) {
            LSDTableClass lsdTableClass = new LSDTableClass();
            String HHMM = lsdTableClass.GetLSDTable(Integer.parseInt(Htext), Integer.parseInt(Mtext));
            char[] array = HHMM.toCharArray();

            String HH = Character.toString(array[0]) + Character.toString(array[1]);
            String MM = Character.toString(array[2]) + Character.toString(array[3]);

            AlarmUtills alarmUtills = new AlarmUtills();
            Calendar cal = alarmUtills.calculate(Integer.parseInt(Htext), Integer.parseInt(Mtext), AddAlarmActivity.this);
            etime.setText("Estimated end time : " + simpleDateFormat.format(cal.getTime()));
            textView.setText("Estimated elapsed time : " + HH + ":" + MM);
            Save.setEnabled(true);
        } else {
            textView.setText("Estimated elapsed time : UNKNOWN");
            etime.setText("Estimated end time : UNKNOWN");
            Save.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AddAlarmActivity.this, AlarmListActivity.class));
        finish();
    }
}
