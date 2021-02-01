package com.fqxd.gftools.features.txtkr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;
import com.fqxd.gftools.implement.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.MessageFormat;

public class TxtKrPatchActivity extends AppCompatActivity {
    protected static JSONArray RawData;
    protected static File OriginalData;
    protected static File BackupData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_txtkr);
        findViewById(R.id.Progress_Layout).setVisibility(View.VISIBLE);
        OriginalData = new File(Global.Storage + "/Android/data/com.digitalsky.girlsfrontline.cn.bili/files/Android/New/asset_textes.ab");
        BackupData = new File(Global.Storage + "/GF_Tool/TextKR/backup/");
        if(!BackupData.exists()) BackupData.mkdirs();
        if(isOnline()) {
            if(OriginalData.getParentFile().exists()) {
                new GetDataSet(this).execute();
            } else {
                showInternetDialog(this,"데이터 에셋 파일 없음","소녀전선에서 데이터를 다운받은 후 재시도 바랍니다");
            }
        }
        else showInternetDialog(this,"인터넷 연결 없음","인터넷 연결 확인후 재시도 바랍니다");
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        if (cm.getActiveNetworkInfo() != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        }
        return false;
    }

    private static void init(Activity context) {
        try {
            String latestVersion = RawData.getJSONObject(0).getString("date");
            String currentMD5 = getMD5Checksum(OriginalData);
            String currentVersion = "알수 없음";

            for(int i = 0;i < RawData.length();i++) {
                JSONObject object = RawData.getJSONObject(i);
                if(currentMD5.equals(object.getString("md5"))) currentVersion = object.getString("date");
            }

            TextView LatestVersion = context.findViewById(R.id.latest_Version);
            TextView CurrentVersion = context.findViewById(R.id.current_Version);
            TextView CurrentStatus = context.findViewById(R.id.Progress_State);
            Button download = context.findViewById(R.id.Button_RunTask);
            Button restore = context.findViewById(R.id.Button_RunRestore);
            Button article = context.findViewById(R.id.Button_OpenArticle);

            File backup = new File(TxtKrPatchActivity.BackupData + "/asset_textes.ab");
            download.setEnabled(!latestVersion.equals(currentVersion));
            if(!currentVersion.equals("알수 없음") && Integer.parseInt(latestVersion) > Integer.parseInt(currentVersion)) {
                download.setText(context.getString(R.string.TXTKR_Update));
            }
            restore.setEnabled(backup.exists() && !currentVersion.equals("알수 없음"));

            download.setOnClickListener((v) -> {
                try {
                    new DownloadTask(context,CurrentStatus,RawData.getJSONObject(0).getString("file")).execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            restore.setOnClickListener((v) -> new RestoreTask(context,CurrentStatus).execute());
            article.setOnClickListener((v) -> {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(RawData.getJSONObject(0).getString("article")));
                    context.startActivity(browserIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            LatestVersion.setText(MessageFormat.format("최신 버전 : {0}", latestVersion));
            CurrentVersion.setText(MessageFormat.format("현재 버전 : {0}", currentVersion));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static String getMD5Checksum(File file) throws Exception {
        InputStream fis =  new FileInputStream(file);
        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();

        byte[] b = complete.digest();
        StringBuilder result = new StringBuilder();
        for (byte value : b) {
            result.append(Integer.toString((value & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    private static void showInternetDialog(Activity context,String title,String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("확인",(d,w) -> context.finish())
                .show();
    }

    private static class GetDataSet extends AsyncTask<Void,Void,String> {

        Activity context;
        boolean isConnected;

        protected GetDataSet(Activity context) {
            this.context = context;
        }

        public static boolean netIsAvailable() {
            try {
                final URL url = new URL("http://www.github.com");
                final URLConnection conn = url.openConnection();
                conn.connect();
                conn.getInputStream().close();
                return true;
            } catch (MalformedURLException e) {
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        private static String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }

        public static JSONArray readJsonFromUrl(String url) throws IOException, JSONException {
            try (InputStream is = new URL(url).openStream()) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String jsonText = readAll(rd);
                return new JSONArray(jsonText);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!isConnected) {
                showInternetDialog(context, "인터넷 연결 없음", "인터넷 연결 확인후 재시도 바랍니다");
            } else {
                init(context);
                context.findViewById(R.id.Progress_Layout).setVisibility(View.GONE);
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                isConnected = netIsAvailable();
                RawData = readJsonFromUrl("https://raw.githubusercontent.com/choiman1559/GFTools-TextKR-Data/master/data.json");
            } catch (Exception e) {
                return e.toString();
            }
            return "";
        }
    }
}
