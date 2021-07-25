package com.fqxd.gftools.features.gfneko;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.fqxd.gftools.global.Global;
import com.fqxd.gftools.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class GFNekoActivity extends AppCompatActivity {
  @Override
  public void onCreate(Bundle savedState) {
    super.onCreate(savedState);
    setContentView(R.layout.activity_neko);
    getSupportFragmentManager()
            .beginTransaction()  
            .replace(R.id.neko_prefs, new SettingsFragment(GFNekoActivity.this))
            .commitNowAllowingStateLoss();
  }

  public static class SettingsFragment extends PreferenceFragmentCompat {
    Context context;

    SettingsFragment(Context context) {
      this.context = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.neko_prefs, rootKey);
      SharedPreferences prefs = context.getSharedPreferences(context.getPackageName() + "preferences", MODE_PRIVATE);
      Preference Service_Enable = findPreference(AnimationService.PREF_KEY_ENABLE);
      ListPreference Skin = findPreference("motion.skin");

      assert Service_Enable != null;
      Service_Enable.setOnPreferenceChangeListener((preference, newValue) -> {
        if ((Boolean) newValue) startAnimationService();
        return true;
      });

      assert Skin != null;
      Skin.setEntries(getEntries("IDW the Many"));
      Skin.setEntryValues(getEntries(""));
      if (Skin.getEntries().length < 2) Skin.setValueIndex(0);
      if (prefs.getBoolean(Service_Enable.getKey(), false)) startAnimationService();
    }

    private void startAnimationService() {
      SharedPreferences.Editor edit = context.getSharedPreferences(Global.Prefs, MODE_PRIVATE).edit();
      edit.putBoolean(AnimationService.PREF_KEY_VISIBLE, true).apply();
      context.startService(new Intent(context, AnimationService.class).setAction(AnimationService.ACTION_START));
    }

    private CharSequence[] getEntries(String PreValue) {
      String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GF_Tool/skins/";
      ArrayList<String> list = new ArrayList<>();
      list.add(PreValue);

      if (new File(dir).exists()) {
        for (File skinObj : Objects.requireNonNull(new File(dir).listFiles())) {
          if (skinObj.isDirectory()) {
            for (File files : Objects.requireNonNull(skinObj.listFiles())) {
              if (files.isFile()) {
                String name = files.getName();
                if (name.substring(name.lastIndexOf(".") + 1).equals("xml")) {
                  list.add(files.getAbsolutePath().replace(dir, ""));
                  break;
                }
              }
            }
          }
        }
      }

      CharSequence[] listChar = new CharSequence[list.size()];
      for (int i = 0; i < list.size(); i++) {
        listChar[i] = list.get(i);
      }
      return listChar;
    }
  }
}
