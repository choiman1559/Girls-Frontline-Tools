package com.fqxd.gftools.features.calculator;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.fqxd.gftools.R;

public class CalculatorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.prefs, new SettingFragment())
                .commit();
    }

    public static class SettingFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.calculator_prefs,rootKey);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if(!preference.getKey().equals("Button_gunsu")) startWebView(preference.getKey());
            return super.onPreferenceTreeClick(preference);
        }

        void startWebView(String key) {
            startActivity(new Intent(this.getContext(),WebViewActivity.class).putExtra("type",key));
        }
    }
}
