package com.fqxd.gftools;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AddAlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);
        Spinner H = findViewById(R.id.HSpinner);
        Spinner M = findViewById(R.id.MSpinner);

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
                String Htext = H.getSelectedItem().toString();
                String Mtext = M.getSelectedItem().toString();

                if(!(Htext == "..." || Mtext == "...")) {
                    calculate(Integer.parseInt(Htext),Integer.parseInt(Mtext));
                } else {
                    TextView textView = findViewById(R.id.textView7);
                    TextView etime = findViewById(R.id.etime);
                    textView.setText("prediction time : UNKNOWN");
                    etime.setText("end time : UNKNOWN");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        M.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, int position, long id) {
                String Htext = H.getSelectedItem().toString();
                String Mtext = M.getSelectedItem().toString();

                if(!(Htext == "..." || Mtext == "...")) {
                    calculate(Integer.parseInt(Htext),Integer.parseInt(Mtext));
                } else {
                    TextView textView = findViewById(R.id.textView7);
                    TextView etime = findViewById(R.id.etime);
                    textView.setText("prediction time : UNKNOWN");
                    etime.setText("end time : UNKNOWN");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

    }

    public void calculate(int H,int M){
        LSDTableClass lsdTableClass = new LSDTableClass();
        String HHMM = lsdTableClass.GetLSDTable(H,M);
        char[] array = HHMM.toCharArray();

        String HH = Character.toString(array[0]) + Character.toString(array[1]);
        String MM = Character.toString(array[2]) + Character.toString(array[3]);

        TextView textView = findViewById(R.id.textView7);
        textView.setText("prediction time : " + HH + ":" + MM);

        TextView etime = findViewById(R.id.etime);
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.add(cal.HOUR,Integer.parseInt(HH));
        cal.add(cal.MINUTE,Integer.parseInt(MM));

        etime.setText("end time : " + simpleDateFormat.format(cal.getTime()));
    }
}
