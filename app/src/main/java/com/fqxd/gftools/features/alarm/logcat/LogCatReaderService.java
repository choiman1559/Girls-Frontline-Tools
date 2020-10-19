package com.fqxd.gftools.features.alarm.logcat;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.fqxd.gftools.features.alarm.floating.AlarmFloatingView;
import com.fqxd.gftools.features.alarm.ui.AlarmListActivity;
import com.fqxd.gftools.features.alarm.utils.AlarmUtils;
import com.fqxd.gftools.DetectGFService;
import com.fqxd.gftools.features.alarm.utils.GFAlarmObjectClass;
import com.fqxd.gftools.features.alarm.utils.Sector;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.fqxd.gftools.Global.floatingView;

public class LogCatReaderService extends Service {
    public static Logcat logcat;
    public volatile int LastOptId = -1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification());
        floatingView = new AlarmFloatingView(this);
        floatingView.CreateView();

        logcat.bind(AlarmListActivity.appCompatActivity);
        logcat.addEventListener(this::onReceivedLogs);
        logcat.start();

        new Thread(() -> {
            while (true) if (!logcat.isRunning()) logcat.restart();
        }).start();
        return START_STICKY;
    }

    Notification createNotification() {
        Intent intent = new Intent(this, AlarmListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .setContentTitle("Logcat Reader is running")
                .setContentText("click here to open")
                .setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT > 25) builder.setChannelId("GFPacketService");
        return builder.build();
    }

    @Override
    public void onCreate() {
        logcat = new Logcat(250000);
        logcat.setMaxLogsCount(250000);
        logcat.setPollInterval(1);
        logcat.setLogcatBuffers(Logcat.Companion.getDEFAULT_BUFFERS());
        super.onCreate();
    }

    public void onReceivedLogs(@NotNull List<Log> logs) {
        String msg = logs.get(logs.size() - 1).getMsg();
        if (msg.contains("The referenced script on this Behaviour (Game Object '<null>') is missing!"))
            return;
        try {
            if (isGF(DetectGFService.lastPackage)) {
                if (msg.contains("Dequeue: Operation/startOperation\t{")) {
                    JSONObject obj = new JSONObject("{" + substringBetween(msg, "Dequeue: Operation/startOperation\t{", "}") + "}");
                    JSONObject o = new AlarmUtils().checkOverlap(obj.getInt("operation_id"), DetectGFService.lastPackage, getApplicationContext());
                    if(obj.getInt("operation_id") != LastOptId) {
                        if (o == null) {
                            android.util.Log.d("json", obj.toString());
                            GFAlarmObjectClass objectClass = new GFAlarmObjectClass();
                            objectClass.setSector(Sector.getSectorFromOpId(obj.getInt("operation_id")));
                            objectClass.setTimeToTriggerAndHourAndMinuteFromSector();
                            objectClass.setPackage(DetectGFService.lastPackage);
                            objectClass.setSquadNumber(obj.getInt("team_id"));
                            new AlarmUtils().setAlarm(objectClass.parse(), this);
                            floatingView.initListView();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                            builder.setTitle("중복된 군수알람이 있습니다!").setMessage("덮어 씌우시겠습니까?");
                            builder.setNegativeButton("취소", (dialog, id) -> { });
                            builder.setPositiveButton("덮어쓰기", (dialog, id) -> {
                                try {
                                    new AlarmUtils().cancel(o, getApplicationContext());
                                    GFAlarmObjectClass objectClass = new GFAlarmObjectClass();
                                    objectClass.setSector(Sector.getSectorFromOpId(obj.getInt("operation_id")));
                                    objectClass.setTimeToTriggerAndHourAndMinuteFromSector();
                                    objectClass.setPackage(DetectGFService.lastPackage);
                                    objectClass.setSquadNumber(obj.getInt("team_id"));
                                    new AlarmUtils().setAlarm(objectClass.parse(), this);
                                    floatingView.initListView();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.getWindow().setType((Build.VERSION.SDK_INT > 25 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                            alertDialog.show();
                        }
                    }
                    LastOptId = obj.getInt("operation_id");
                }

                if (msg.contains("Dequeue: Operation/abortOperation\t{")) {
                    JSONObject obj = new JSONObject("{" + substringBetween(msg, "Dequeue: Operation/abortOperation\t{", "}") + "}");
                    JSONObject o = new AlarmUtils().checkOverlap(obj.getInt("operation_id"), DetectGFService.lastPackage, getApplicationContext());

                    if (o != null) {
                        new AlarmUtils().cancel(o, getApplicationContext());
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            try {
                                GFAlarmObjectClass ob = GFAlarmObjectClass.getGFAlarmObjectClassFromJson(o);
                                Toast.makeText(getApplicationContext(), "제 " + ob.getSquadNumber() + "제대의 " + ob.getSector().getH() + "-" + ob.getSector().getM() + " 지 군수지원 알람이 삭제되었습니다!", Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, 0);
                    }
                    LastOptId = -1;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isGF(String Package) {
        ArrayList<String> PackageNames = new ArrayList<>();
        PackageNames.add("com.digitalsky.girlsfrontline.cn.uc");
        PackageNames.add("com.digitalsky.girlsfrontline.cn.bili");
        PackageNames.add("com.sunborn.girlsfrontline.en");
        PackageNames.add("com.sunborn.girlsfrontline.jp");
        PackageNames.add("tw.txwy.and.snqx");
        PackageNames.add("kr.txwy.and.snqx");

        for (String i : PackageNames) {
            if (i.equals(Package)) return true;
        }
        return false;
    }

    private String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        floatingView.destroy();
        logcat.stop();
    }
}
