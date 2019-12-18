package com.fqxd.gftools;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

public class AddAlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);
        Spinner H = findViewById(R.id.Spinner1);
        Spinner M = findViewById(R.id.Spinner2);

        ArrayList<String> Hlist = new ArrayList<>();
        for(int i = 0;i <= 11;i++) {Hlist.add(Integer.toString(i));}
        ArrayAdapter<String> Hadpt = new ArrayAdapter<>(this,R.layout.activity_add_alarm,Hlist);
        H.setAdapter(Hadpt);

        ArrayList<String> Mlist = new ArrayList<>();
        for(int i = 1;i <= 4;i++) {Mlist.add(Integer.toString(i));}
        ArrayAdapter<String> Madpt = new ArrayAdapter<>(this,R.layout.activity_add_alarm,Mlist);
        M.setAdapter(Madpt);

        ArrayList<String> p2 = new ArrayList<>();
        p2.add("...");
    }
}
