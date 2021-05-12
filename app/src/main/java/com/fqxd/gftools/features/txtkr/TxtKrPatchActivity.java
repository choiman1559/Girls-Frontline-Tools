package com.fqxd.gftools.features.txtkr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.fqxd.gftools.MainActivity;
import com.fqxd.gftools.R;
import com.fqxd.gftools.implement.AsyncTask;
import com.google.android.material.textfield.TextInputEditText;

import net.lingala.zip4j.ZipFile;

import org.apache.commons.io.IOUtil;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class TxtKrPatchActivity extends AppCompatActivity {
    private static DocumentFile OriginalData;
    private static DocumentFile OriginalJSONFile;

    private static File TranslatedDataFolder;
    private static JSONObject TranslatedDataJSON;
    private static String Package;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_txtkr);
        findViewById(R.id.Progress_Layout).setVisibility(View.VISIBLE);
        Package = getIntent().getStringExtra("pkg");
        new initDocumentFile( Package + "/files/Android/New", this).execute();
    }

    private static void init(Activity context) {
        try {
            TextInputEditText LatestVersion = context.findViewById(R.id.latest_Version);
            TextInputEditText CurrentVersion = context.findViewById(R.id.current_Version);
            Button download = context.findViewById(R.id.Button_RunTask);
            Button article = context.findViewById(R.id.Button_OpenArticle);

            if (OriginalJSONFile.exists()) {
                String value = IOUtil.toString(context.getContentResolver().openInputStream(OriginalJSONFile.getUri()), "UTF-8");
                if(!value.equals("")) {
                    JSONObject OriginalJSON = new JSONObject(value);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());

                    Date OriginalDate = dateFormat.parse(OriginalJSON.getString("update_time"));
                    Date LastUpdatedDate = dateFormat.parse(TranslatedDataJSON.getString("update_time"));

                    if (OriginalDate != null && OriginalDate.compareTo(LastUpdatedDate) >= 0) {
                        download.setEnabled(false);
                    }

                    LatestVersion.setText(TranslatedDataJSON.getString("update_time"));
                    CurrentVersion.setText(OriginalJSON.getString("update_time"));
                } else {
                    LatestVersion.setText(TranslatedDataJSON.getString("update_time"));
                    CurrentVersion.setText("한글패치 설치되지 않음");
                }
            } else {
                LatestVersion.setText(TranslatedDataJSON.getString("update_time"));
                CurrentVersion.setText("한글패치가 설치되지 않음");
            }

            download.setOnClickListener((v) -> new PatchTask(context).execute());
            article.setOnClickListener((v) -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://gall.dcinside.com/mgallery/board/view?id=micateam&no=1668728"));
                context.startActivity(browserIntent);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showInternetDialog(Activity context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("확인", (d, w) -> context.finish())
                .show();
    }

    public static class PatchTask extends AsyncTask<Void, String, String> {

        Activity context;
        TextView CurrentStatus;

        public PatchTask(Activity activity) {
            this.context = activity;
            this.CurrentStatus = context.findViewById(R.id.Progress_State);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            CurrentStatus.setText(String.format("에셋 %s 복사중...", values[0]));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            context.findViewById(R.id.Progress_Layout).setVisibility(View.VISIBLE);
            CurrentStatus.setText("에셋 복사 준비중...");
        }

        @Override
        protected void onPostExecute(String value) {
            super.onPostExecute(value);
            if(value.equals("")) {
                Toast.makeText(context, "Task Done!", Toast.LENGTH_SHORT).show();
                context.recreate();
            } else {
                showInternetDialog(context, "에러 발생", "에러 코드 : \n" + value);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        protected String doInBackground(Void... voids) {
            try {
                for (File file : Objects.requireNonNull(TranslatedDataFolder.listFiles())) {
                    publishProgress(file.getName());
                    for(DocumentFile file1 : OriginalData.listFiles()) {
                        if(file1.isFile() && file1.getName().equals(file.getName())) {
                            DocumentsContract.deleteDocument(context.getApplicationContext().getContentResolver(), file1.getUri());
                            break;
                        }
                    }

                    DocumentFile documentFile = OriginalData.createFile("application/octet-stream", file.getName());
                    FileInputStream inputStream = new FileInputStream(file);
                    OutputStream outputStream = context.getContentResolver().openOutputStream(documentFile.getUri());
                    IOUtil.copy(inputStream, outputStream);

                    inputStream.close();
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }
            return "";
        }
    }

    public static class initDocumentFile extends AsyncTask<Void,String,Integer> {
        String path;
        Activity context;
        TextView CurrentStatus;

        public initDocumentFile(String path, Activity context) {
            this.path = path;
            this.context = context;
            this.CurrentStatus = context.findViewById(R.id.Progress_State);
        }

        private boolean isOnline() {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert cm != null;
            if (cm.getActiveNetworkInfo() != null) {
                NetworkInfo ni = cm.getActiveNetworkInfo();
                return ni != null && ni.isConnected();
            }
            return false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CurrentStatus.setText("데이터셋 초기화중...");
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer != null && integer == -1) {
                showInternetDialog(context, "데이터 에셋 파일 없음", "소녀전선에서 데이터를 다운받은 후 재시도 바랍니다");
            } else if (isOnline()) {
                if (OriginalData.exists() && Objects.requireNonNull(OriginalData.listFiles()).length > 0) {
                    new GetDataSet(context, CurrentStatus).execute();
                } else {
                    showInternetDialog(context, "데이터 에셋 파일 없음", "소녀전선에서 데이터를 다운받은 후 재시도 바랍니다");
                }
            } else showInternetDialog(context, "인터넷 연결 없음", "인터넷 연결 확인후 재시도 바랍니다");
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if(values.length < 2) CurrentStatus.setText(String.format("파일/폴더 %s 검색중...", values[0]));
            else CurrentStatus.setText(String.format("파일/폴더 %s 검색중...\n매칭 : %s", values[0], values[1]));
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            String[] foo = path.split("/");
            OriginalData = findDocumentDir(foo[0],MainActivity.DataFolder,true);
            if(OriginalData == null || !OriginalData.exists()) return -1;

            for(int i = 1; i < foo.length; i++) {
                OriginalData = findDocumentDir(foo[i], OriginalData,true);
                if(OriginalData == null) return -1;
            }

            OriginalJSONFile = findDocumentDir("files.json", OriginalData,false);
            if(OriginalJSONFile == null) OriginalJSONFile = OriginalData.createFile("application/json","files.json");
            return 0;
        }

        private DocumentFile findDocumentDir(String s, DocumentFile files, boolean isDirectory) {
            if(files != null) {
                if(isDirectory) publishProgress(s);
                for (DocumentFile file : files.listFiles()) {
                    if(!isDirectory) publishProgress(s, file.getName());
                    if ((isDirectory ? file.isDirectory() : file.isFile()) && file.getName().equals(s)) {
                        return file;
                    }
                }
            }
            return null;
        }
    }

    public static class GetDataSet extends AsyncTask<Void, Integer, String> {

        Activity context;
        TextView CurrentStatus;
        boolean isConnected;

        protected GetDataSet(Activity context, TextView CurrentStatus) {
            this.context = context;
            this.CurrentStatus = CurrentStatus;
        }

        public boolean netIsAvailable() {
            try {
                final URL url = new URL(getFileUrl().replace(".zip","/files.json"));
                final URLConnection conn = url.openConnection();
                conn.connect();
                conn.getInputStream().close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!s.equals("")) {
                showInternetDialog(context, "에러 발생", "에러 코드 : \n" + s);
            } else if (!isConnected) {
                showInternetDialog(context, "인터넷 연결 없음", "인터넷 연결 확인후 재시도 바랍니다");
            } else {
                init(context);
                context.findViewById(R.id.Progress_Layout).setVisibility(View.GONE);
            }
        }

        private String getFileUrl() throws PackageManager.NameNotFoundException {
            switch (Package) {
                case "com.sunborn.girlsfrontline.cn":
                case "com.digitalsky.girlsfrontline.cn.bili":
                    if (new VersionCompare("2.0700").compareTo(new VersionCompare(context.getPackageManager().getPackageInfo(Package, 0).versionName.split("_")[0])) < 0) {
                        return "http://klanet.duckdns.org:406/cn-android.zip";
                    } else return "http://klanet.duckdns.org:406/cn-android-20700.zip";

                case "com.sunborn.girlsfrontline.en":
                    return "http://klanet.duckdns.org:406/en-android.zip";

                case "com.sunborn.girlsfrontline.jp":
                    return "http://klanet.duckdns.org:406/jp-android.zip";

                default:
                    return "";
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            switch (progress[0]) {
                case -1:
                    CurrentStatus.setText("에셋 다운로드 중... 0%");
                    break;

                case -2:
                    CurrentStatus.setText("에셋 압축 해제중...");
                    break;

                case -3:
                    CurrentStatus.setText("에셋 데이터 파싱중...");
                    break;

                default:
                    CurrentStatus.setText(MessageFormat.format("에셋 다운로드 중... {0}%", progress[0]));
                    break;
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                isConnected = netIsAvailable();
                String FileUrl = getFileUrl();

                if (isConnected) {
                    if (!FileUrl.equals("")) {
                        InputStream input = null;
                        OutputStream output = null;
                        HttpURLConnection connection = null;

                        String[] foo = FileUrl.split("/");
                        File var = new File(context.getCacheDir() + "/download/" + foo[foo.length - 1]);
                        if(var.exists()) var.delete();
                        if(!var.getParentFile().exists()) var.getParentFile().mkdirs();
                        var.createNewFile();
                        publishProgress(-1);

                        try {
                            URL url = new URL(FileUrl);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.connect();
                            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                                return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                            }

                            int fileLength = connection.getContentLength();
                            input = connection.getInputStream();
                            output = new FileOutputStream(var);

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
                            e.printStackTrace();
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

                        publishProgress(-2);
                        TranslatedDataFolder = new File(context.getCacheDir() + "/archives/" + foo[foo.length - 1].split("\\.")[0]);
                        if (!TranslatedDataFolder.isDirectory() || TranslatedDataFolder.exists())
                            TranslatedDataFolder.delete();
                        TranslatedDataFolder.mkdirs();

                        ZipFile zipFile = new ZipFile(var);
                        zipFile.extractAll(TranslatedDataFolder.getAbsolutePath());

                        publishProgress(-3);
                        TranslatedDataJSON = new JSONObject(IOUtil.toString(new FileInputStream(TranslatedDataFolder + "/files.json"), "UTF-8"));
                    } else
                        throw new RuntimeException("Package " + Package + " is not valid package name!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }
            return "";
        }
    }

    @Override
    public void onBackPressed() {
        if(!(findViewById(R.id.Progress_Layout).getVisibility() == View.VISIBLE)) {
            super.onBackPressed();
        }
    }
}