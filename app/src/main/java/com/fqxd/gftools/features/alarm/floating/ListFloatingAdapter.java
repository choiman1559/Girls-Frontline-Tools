package com.fqxd.gftools.features.alarm.floating;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fqxd.gftools.R;
import com.fqxd.gftools.features.alarm.utils.GFAlarmObjectClass;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.MessageFormat;

public class ListFloatingAdapter extends RecyclerView.Adapter<ListFloatingAdapter.ViewHolder>{

    private final JSONArray data;
    ListFloatingAdapter(JSONArray data) {
        this.data = data;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView sector;
        TextView squad;
        TextView time;

        ViewHolder(View itemView) {
            super(itemView) ;
            sector = itemView.findViewById(R.id.sectorInfo);
            squad = itemView.findViewById(R.id.SquadNumber);
            time = itemView.findViewById(R.id.timeInfo);
        }
    }

    @NonNull
    @Override
    public ListFloatingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
        View view = inflater.inflate(R.layout.cardview_alarmview, parent, false) ;
        return new ListFloatingAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            new GFAlarmObjectClass();
            GFAlarmObjectClass object = GFAlarmObjectClass.getGFAlarmObjectClassFromJson(new JSONObject(data.get(position).toString()));

            holder.sector.setText(MessageFormat.format("군수지원 - {0}-{1} 구역", object.getSector().getH(), object.getSector().getM()));
            holder.squad.setText(object.getSquadNumber());

            long time = object.getTimeToTrigger() - System.currentTimeMillis();
            time /= 60000;
            int Hour = (int)(time / 60);
            int Minute = (int)(time - (Hour * 60));
            holder.time.setText(MessageFormat.format("{0}시간 {1}분 남음  (총 {2}시간 {3}분)", Hour, Minute, object.getHour(), object.getMinute()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return data.length();
    }
}
