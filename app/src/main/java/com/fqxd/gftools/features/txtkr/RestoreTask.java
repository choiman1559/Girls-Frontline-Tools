package com.fqxd.gftools.features.txtkr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fqxd.gftools.R;
import com.fqxd.gftools.implement.AsyncTask;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class RestoreTask extends AsyncTask<Void,Void,Void> {
    private PowerManager.WakeLock mWakeLock;
    private final Activity context;
    private final TextView progressTextView;

    public RestoreTask(Activity context,TextView textView) {
        this.context = context;
        this.progressTextView = textView;
    }

    @SuppressLint("WakelockTimeout")
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        context.findViewById(R.id.Progress_Layout).setVisibility(View.VISIBLE);
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire();
        progressTextView.setText("에셋 백업 중...");
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mWakeLock.release();
        Toast.makeText(context, "작업 성공!", Toast.LENGTH_SHORT).show();
        context.findViewById(R.id.Progress_Layout).setVisibility(View.GONE);
        context.recreate();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            File backup = new File(TxtKrPatchActivity.BackupData + "/asset_textes.ab");
            FileUtils.copyFile(backup, TxtKrPatchActivity.OriginalData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}