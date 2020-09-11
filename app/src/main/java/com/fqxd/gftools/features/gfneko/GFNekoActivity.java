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
      Preference Service_Enable = findPreference(AnimationService.PREF_KEY_ENABLE);
      ListPreference Skin = findPreference("motion.skin");

      if(prefs.getBoolean(AnimationService.PREF_KEY_ENABLE,false)) startAnimationService();
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

    private boolean isXMLContain(File dir) {
      if(dir.isDirectory()) {
        for(File file : dir.listFiles()) {
          String name = file.getName();
          if (name.substring(name.lastIndexOf(".") + 1).equals("xml")) {
                return true;
          }
        }
      }
      return false;
    }

    private int checkFiles(){
      String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GF_Tool/skins/";
      int count = 1;

      if(new File(dir).exists()) {
        for(File skinDir : new File(dir).listFiles()) {
          if(!skinDir.isDirectory()) {
            count -= 1;
            continue;
          }
          if(!isXMLContain(skinDir)) count -= 1;
        }
      }
      return count;
    }

    private CharSequence[] getEntries(String PreValue) {
      String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GF_Tool/skins/";
      CharSequence[] list = new CharSequence[new File(dir).exists() ? new File(dir).listFiles().length + checkFiles(): 1];
      list[0] = PreValue;
      int count = 1;

      if(new File(dir).exists()) {
        for (File skinObj : new File(dir).listFiles()) {
          if (skinObj.isDirectory()) {
            for (File files : skinObj.listFiles()) {
              if(files.isFile()) {
                String name = files.getName();
                if (name.substring(name.lastIndexOf(".") + 1).equals("xml")) {
                  list[count++] = files.getAbsolutePath().replace(dir, "");
                  break;
                }
              }
            }
          }
        }
      }
      return list;
    }
  }
}
