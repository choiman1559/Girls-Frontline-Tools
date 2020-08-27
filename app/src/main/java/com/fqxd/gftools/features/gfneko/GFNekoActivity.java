package com.fqxd.gftools.features.gfneko;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.fqxd.gftools.R;

import java.io.File;

public class GFNekoActivity extends AppCompatActivity {
  @Override
  public void onCreate(Bundle savedState) {
    super.onCreate(savedState);
    setContentView(R.layout.activity_neko);
    getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.neko_prefs, new SettingsFragment(GFNekoActivity.this))
            .commit();
  }

  public static class SettingsFragment extends PreferenceFragmentCompat {
    Context context;

    SettingsFragment(Context context) { this.context = context; }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.neko_prefs, rootKey);
      SharedPreferences prefs = context.getSharedPreferences(context.getPackageName() + "preferences", MODE_PRIVATE);
      Preference Service_Enable = findPreference("motion.enable");
      ListPreference Skin = findPreference("motion.skin");

      if(prefs.getBoolean("motion.enable",false)) startAnimationService();
      Service_Enable.setOnPreferenceChangeListener((preference, newValue) -> {
        if((Boolean)newValue) startAnimationService();
        return true;
      });

      Skin.setEntries(getEntries("IDW the Many"));
      Skin.setEntryValues(getEntries(""));
    }

    private void startAnimationService() {
      SharedPreferences.Editor edit = context.getSharedPreferences(context.getPackageName() + "_preferences",MODE_PRIVATE).edit();
      edit.putBoolean(AnimationService.PREF_KEY_VISIBLE, true);
      edit.apply();
      context.startService(new Intent(context, AnimationService.class).setAction(AnimationService.ACTION_START));
    }

    private CharSequence[] getEntries(String PreValue) {
      String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GF_Tool/skins/";
      CharSequence[] list = new CharSequence[new File(dir).exists() ? new File(dir).listFiles().length : 1];
      int count = 0;
      list[count++] = PreValue;

      if(new File(dir).exists()) {
        for(File skindir : new File(dir).listFiles()) {
          if(skindir.isDirectory()) {
            for(File files : skindir.listFiles()) {
              String name = files.getName();
              if(name.substring(name.lastIndexOf(".") + 1).equals("xml")) {
                list[count++] = files.getAbsolutePath().replace(dir,"");
                break;
              }
            }
          }
        }
      }
      return list;
    }
  }
}
