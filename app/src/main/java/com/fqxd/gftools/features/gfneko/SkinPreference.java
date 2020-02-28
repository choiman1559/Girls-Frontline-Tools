package com.fqxd.gftools.features.gfneko;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.os.Environment;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.fqxd.gftools.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkinPreference extends DialogPreference {
  private static final String KEY_LABEL = "label";
  private static final String KEY_CHECK = "check";
  private static final String KEY_COMPONENT = "component";

  private static final String[] ITEM_FROM = {
//      KEY_ICON,
          KEY_LABEL, KEY_CHECK
  };
  private static final int[] ITEM_TO = {
//      R.id.item_icon,
          R.id.item_label, R.id.item_check
  };

  private List<Map<String, Object>> data;
  private int clicked_index;

  //  private ComponentName val;
  private String val;

  public SkinPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setValue(String str) {
//    this.val = ComponentName.unflattenFromString(str);
    this.val = str;
    persistString(str);
  }

//  public void setValue(ComponentName val) {
//    setValue(val != null ? val.flattenToString() : "");
//  }

  //  public ComponentName getValue() {
//    return val;
//  }
  public String getValue() {
    return val;
  }

  protected Object onGetDefaultValue(TypedArray a, int index) {
    return a.getString(index);
  }

  @Override
  protected void onSetInitialValue(boolean restore, Object defaultValue) {
    setValue(restore ? getPersistedString("") : (String) defaultValue);
  }

  @Override
  protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
    super.onPrepareDialogBuilder(builder);

    data = createListData();
    clicked_index = -1;

    SimpleAdapter adapter = new SimpleAdapter(
            getContext(), data, R.layout.preference_skin_item,
            ITEM_FROM, ITEM_TO);
//    adapter.setViewBinder(new IconViewBinder());

    builder
            .setAdapter(
                    adapter,
                    (dialog, witch) -> {
                      clicked_index = witch;

                      SkinPreference.this.onClick(
                              dialog, DialogInterface.BUTTON_POSITIVE);
                      dialog.dismiss();
                    })
            .setPositiveButton(null, null)
    ;
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    super.onDialogClosed(positiveResult);

    if (positiveResult && clicked_index >= 0) {

//      Toast.makeText(getContext(), "idx=" + clicked_index, Toast.LENGTH_LONG).show();

//      ComponentName comp =
//          (ComponentName) data.get(clicked_index).get(KEY_COMPONENT);

      String path =
              (String) data.get(clicked_index).get(KEY_COMPONENT);
      if (callChangeListener(path)) {

        Toast.makeText(getContext(), path, Toast.LENGTH_LONG).show();

        setValue(path);
      }
    }
  }

  private List<Map<String, Object>> createListData() {
    Context context = getContext();
    PackageManager pm = context.getPackageManager();

    Intent[] internals = {
            new Intent(context, NekoSkin.class),
    };
    Intent intent = new Intent(AnimationService.ACTION_GET_SKIN);
    List<ResolveInfo> activities = pm.queryIntentActivityOptions(
            null, internals, intent, 0);

    List<Map<String, Object>> list = new ArrayList<>();

    if (!activities.isEmpty()) {
      ResolveInfo info = activities.get(0);
//      ComponentName comp = new ComponentName(
//          info.activityInfo.packageName, info.activityInfo.name);

      Map<String, Object> data = new HashMap<>();
//      data.put(KEY_ICON, info.loadIcon(pm));
//      data.put(KEY_LABEL, info.loadLabel(pm));
      data.put(KEY_LABEL, info.loadLabel(pm));
      data.put(KEY_CHECK, "".equals(val)); //Boolean.valueOf(comp.equals(val)));
      data.put(KEY_COMPONENT, "");
      list.add(data);
    }


    try {

      File externalStorageDirectory = Environment.getExternalStorageDirectory();
      File skinsRootDir = new File(externalStorageDirectory, AnimationService.GFNEKO_SKINS);

      if (!skinsRootDir.exists()) {
        Toast.makeText(getContext(), "GFNeko/skins Folder Not Found !!", Toast.LENGTH_LONG)
                .show();
        return list;
      }

      File[] skinDirs = skinsRootDir.listFiles(File::isDirectory);

      for (File skinDir : skinDirs) {
        String[] xmls = skinDir.list((dir, filename) -> filename.endsWith(".xml"));

        String dirName = skinDir.getName();

        for (String xml : xmls) {
          String n = dirName + "/" + xml;

          Map<String, Object> data = new HashMap<>();
          //        data.put(KEY_ICON, info.loadIcon(pm));
          data.put(KEY_LABEL, n);
          //        data.put(KEY_ICON, info.loadIcon(pm));
          //        data.put(KEY_LABEL, info.loadLabel(pm));
          //        data.put(KEY_CHECK, Boolean.valueOf(comp.equals(val)));
          data.put(KEY_CHECK, n.equals(val));
          data.put(KEY_COMPONENT, n);

          list.add(data);
        }


      }

    } catch (Exception e) {
      Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
      e.printStackTrace();
    }


    return list;
  }

//  private class IconViewBinder implements SimpleAdapter.ViewBinder {
//    @Override
//    public boolean setViewValue(View view, Object data, String text) {
//      if (view.getId() != R.id.item_icon) {
//        return false;
//      }
//
//      ((ImageView) view).setImageDrawable((Drawable) data);
//      return true;
//    }
//  }


}