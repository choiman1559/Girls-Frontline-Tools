package com.fqxd.gftools.features.alarm;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.R;

import org.json.JSONException;

@VisibleForTesting
public class AddAlarmManuallyClass extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addalarmmanually);

        if(!BuildConfig.DEBUG) {
            Toast.makeText(this, "add alarm manually is reserved for debug purpose only!!!", Toast.LENGTH_LONG).show();
            finish();
        }
        GFAlarmObjectClass object = new GFAlarmObjectClass();
        findViewById(R.id.input).setOnClickListener(v -> {
            Sector sector = new Sector(Integer.parseInt(((EditText)findViewById(R.id.Sector_H)).getText() + ""),Integer.parseInt(((EditText)findViewById(R.id.Sector_M)).getText() + ""));
            object.setSector(sector);
            object.setTimeToTriggerAndHourAndMinuteFromSector();
            object.setSquadNumber(Integer.parseInt(((EditText)findViewById(R.id.SquadNumber)).getText() + ""));
            object.setPackage(((EditText)findViewById(R.id.Package)).getText() + "");

            try {
                new AlarmUtils().setAlarm(object.parse(),this);
                this.finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
}
