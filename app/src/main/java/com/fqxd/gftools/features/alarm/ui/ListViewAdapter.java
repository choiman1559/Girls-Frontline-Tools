package com.fqxd.gftools.features.alarm.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fqxd.gftools.R;
import com.fqxd.gftools.features.alarm.receiver.AlarmReceiver;
import com.fqxd.gftools.features.alarm.receiver.BootCompleteReceiver;
import com.fqxd.gftools.features.alarm.utils.GFAlarmObjectClass;

import org.json.JSONArray;
import org.json.JSONObject;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ViewHolder> {

    private JSONArray data;
    private Context context;
    OnDataChangedListener Listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView sector;
        TextView squad;
        ImageButton delete;
        TextView time;
        TextView Package;

        ViewHolder(View itemView) {
            super(itemView) ;
            sector = itemView.findViewById(R.id.sectorInfo);
            squad = itemView.findViewById(R.id.squadInfo);
            delete = itemView.findViewById(R.id.deleteAlarm);
            time = itemView.findViewById(R.id.timeInfo);
            Package = itemView.findViewById(R.id.appInfo);
        }
    }

    ListViewAdapter(JSONArray data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ListViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
        View view = inflater.inflate(R.layout.cardview_alarmlist, parent, false) ;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewAdapter.ViewHolder holder, int position) {
        try {
            new GFAlarmObjectClass();
            GFAlarmObjectClass object = GFAlarmObjectClass.getGFAlarmObjectClassFromJson(new JSONObject(data.get(position).toString()));
            PackageManager pm = context.getPackageManager();
            holder.sector.setText(object.getSector().getH() + "-" + object.getSector().getM() + " 구역");
            holder.squad.setText(object.getSquadNumber() + "제대");
            holder.Package.setText(pm.getApplicationLabel(pm.getApplicationInfo(object.getPackage(), PackageManager.GET_META_DATA)) +  " (" + object.getPackage() + ")");

            long time = object.getTimeToTrigger() - System.currentTimeMillis();
            time /= 60000;
            int Hour = (int)(time / 60);
            int Minute = (int)(time - (Hour * 60));
            holder.time.setText(Hour + "시간 " +  Minute + "분 남음 " + " (총 " + object.getHour() + "시간 " + object.getMinute() + "분)");

            holder.delete.setOnClickListener(v -> {
                try {
                    JSONObject obj = object.parse();
                    Intent intent = new Intent(context, AlarmReceiver.class);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,obj.getInt("ID"),intent,PendingIntent.FLAG_UPDATE_CURRENT);
                    ComponentName componentName = new ComponentName(context, BootCompleteReceiver.class);

                    if(PendingIntent.getBroadcast(context,obj.getInt("ID"),intent,PendingIntent.FLAG_UPDATE_CURRENT) != null && alarmManager != null) {
                        alarmManager.cancel(pendingIntent);
                    }
                    pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);

                    SharedPreferences prefs = context.getSharedPreferences("MainActivity",Context.MODE_PRIVATE);
                    JSONArray array = new JSONArray(prefs.getString("AlarmData","[ ]"));
                    for(int i = 0;i < array.length();i++) {
                        if(array.getJSONObject(i).getLong("ID") == obj.getLong("ID")) {
                            array.remove(i);
                            break;
                        }
                    }
                    prefs.edit().putString("AlarmData",array.toString()).apply();
                    Listener.onDataChangedEvent();
                    notifyItemRemoved(position);
                    notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Listener.onDataChangedEvent();
                notifyDataSetChanged();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnDataChangedListener{
        void onDataChangedEvent();
    }

    public void setOnDataChangedListener(OnDataChangedListener listener){
        Listener = listener;
    }

    @Override
    public int getItemCount() {
        return data.length();
    }
}
