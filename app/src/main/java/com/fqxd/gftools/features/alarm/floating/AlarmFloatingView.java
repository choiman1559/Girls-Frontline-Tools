package com.fqxd.gftools.features.alarm.floating;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;

import org.jetbrains.annotations.TestOnly;
import org.json.JSONArray;
import org.json.JSONException;

import io.hamed.floatinglayout.FloatingLayout;
import io.hamed.floatinglayout.callback.FloatingCallBack;
import io.hamed.floatinglayout.service.FloatingService;

@VisibleForTesting
@SuppressLint("NonConstantResourceId")
public class AlarmFloatingView extends FloatingService {
    protected RecyclerView recyclerView;
    protected TextView dataEmpty;
    protected SharedPreferences preferences;
    protected Context context;
    protected volatile FloatingLayout floatingLayout = null;

    public AlarmFloatingView(Context context) {
        this.context = context;
    }

    @TestOnly
    public void CreateView() {
        if(BuildConfig.DEBUG) {
            preferences = context.getSharedPreferences(Global.Prefs, Context.MODE_PRIVATE);
            if (floatingLayout == null) {
                FloatingCallBack floatingCallBack = new FloatingCallBack() {
                    @Override
                    public void onCreateListener(View view) {
                        recyclerView = view.findViewById(R.id.ListView);
                        dataEmpty = view.findViewById(R.id.dataEmpty);
                        initListView();
                    }
                    @Override
                    public void onCloseListener() {
                    }
                };
                floatingLayout = new FloatingLayout(context, R.layout.view_alarmlist, floatingCallBack);
            }
            floatingLayout.create();
        } else Log.e(this.getClass().getSimpleName(),"called stub method : AlarmFloatingView.CreateView");
    }

    @TestOnly
    public void initListView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        try {
            JSONArray array = new JSONArray(preferences.getString("AlarmData", "[ ]"));
            if (array.length() > 0) {
                ListFloatingAdapter adapter = new ListFloatingAdapter(array);
                recyclerView.setAdapter(adapter);
                dataEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.GONE);
                dataEmpty.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @TestOnly
    public void destroy() {
        if(BuildConfig.DEBUG) {
            floatingLayout.close();
        }  else Log.e(this.getClass().getSimpleName(),"called stub method : AlarmFloatingView.destroy");
    }
}
