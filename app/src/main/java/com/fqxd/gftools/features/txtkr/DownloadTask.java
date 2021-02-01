package com.fqxd.gftools.features.txtkr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;

@SuppressLint("StaticFieldLeak")
public class DownloadTask extends AsyncTask<Void, Integer, String> {

    private PowerManager.WakeLock mWakeLock;
    private final Activity context;
    private final TextView progressTextView;
    private final String FileLink;
    private final File file;

    public DownloadTask(Activity context,TextView textView,String FileLink) {
        this.context = context;
        this.progressTextView = textView;
        this.FileLink = FileLink;
        file = new File(Global.Storage + "/GF_Tool/TextKR/download/" + FileLink.replace("https://github.com/choiman1559/GFTools-TextKR-Data/raw/master/",""));
    }

    @SuppressLint("WakelockTimeout")
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        context.findViewById(R.id.Progress_Layout).setVisibility(View.VISIBLE);
        if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if(file.exists()) file.delete();

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire();
        progressTextView.setText("에셋 다운로드 중... 0%");
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        progressTextView.setText(MessageFormat.format("에셋 다운로드 중... {0}%", progress[0]));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        progressTextView.setText("에셋 다운로드 중... 100%");
        if (result != null) {
            if(file.exists()) file.delete();
            new AlertDialog.Builder(context)
                    .setTitle("에러!")
                    .setMessage("다운로드 중 에러 발생 : " + result)
                    .setPositiveButton("Close",(d,w) -> context.finish())
                    .show();
        } else {
            try {
                progressTextView.setText("에셋 확인 중...");
                boolean isNeedBackup = true;
                String AssetFileMD5 = TxtKrPatchActivity.getMD5Checksum(TxtKrPatchActivity.OriginalData);
                for(int i = 0;i < TxtKrPatchActivity.RawData.length();i++) {
                    JSONObject object = TxtKrPatchActivity.RawData.getJSONObject(i);
                    if(AssetFileMD5.equals(object.getString("md5"))) {
                        isNeedBackup = false;
                        break;
                    }
                }

                if(isNeedBackup) {
                    File Backup_Asset = new File(TxtKrPatchActivity.BackupData + "/asset_textes.ab");
                    progressTextView.setText("원본 에셋 백업중...");
                    if (Backup_Asset.exists()) Backup_Asset.delete();
                    FileUtils.copyFile(TxtKrPatchActivity.OriginalData, Backup_Asset);
                }

                progressTextView.setText("에셋 복사중...");
                FileUtils.copyFile(file,TxtKrPatchActivity.OriginalData);

                context.findViewById(R.id.Progress_Layout).setVisibility(View.GONE);
                Toast.makeText(context,"작업 완료됨!",Toast.LENGTH_SHORT).show();
                context.recreate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String doInBackground(Void... voids) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(FileLink);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
            }

            int fileLength = connection.getContentLength();
            input = connection.getInputStream();
            output = new FileOutputStream(file);

            byte[] data = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                if (fileLength > 0)
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (connection != null)
                connection.disconnect();
        }
        return null;
    }
}