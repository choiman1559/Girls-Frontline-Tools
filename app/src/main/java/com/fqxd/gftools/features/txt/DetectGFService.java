package com.fqxd.gftools.features.txt;

import android.accessibilityservice.AccessibilityService;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.fqxd.gftools.MainActivity;
import com.fqxd.gftools.R;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

public class DetectGFService extends AccessibilityService {

    static boolean isrunning = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        SharedPreferences prefs = getSharedPreferences("TxtKRPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("isChecked", false)) {
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if (event.getPackageName() != null && !isrunning) {

                    if (getString(R.string.target_kr).equals(event.getPackageName().toString())) {
                        Log.d("Access", "gf on face");
                    }

                    if (getString(R.string.target_cn_bili).equals(event.getPackageName().toString()) && prefs.getBoolean("bill", false)) {
                        isrunning = true;
                        new UpdateTask(getString(R.string.target_cn_bili), DetectGFService.this).execute();
                    }
                    if (getString(R.string.target_cn_uc).equals(event.getPackageName().toString()) && prefs.getBoolean("dgts", false)) {
                        isrunning = true;
                        new UpdateTask(getString(R.string.target_cn_uc), DetectGFService.this).execute();
                    }
                    if (getString(R.string.target_tw).equals(event.getPackageName().toString()) && prefs.getBoolean("chna", false)) {
                        isrunning = true;
                        new UpdateTask(getString(R.string.target_tw), DetectGFService.this).execute();
                    }
                    if (getString(R.string.target_jp).equals(event.getPackageName().toString()) && prefs.getBoolean("jpan", false)) {
                        isrunning = true;
                        new UpdateTask(getString(R.string.target_jp), DetectGFService.this).execute();
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }
}

class UpdateTask extends AsyncTask {

    Context context;
    ProgressDialog dialog;
    String PackageName;
    int Req_id = 0;

    UpdateTask(String Packagename, Context context) {
        this.PackageName = Packagename;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(R.style.Widget_AppCompat_ProgressBar_Horizontal);
        dialog.setCancelable(false);
        dialog.setMessage("Checking Files...");
        dialog.show();
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        if (!new File("/sdcard/Android/data/" + PackageName + "/files/Android/New/asset_textes.ab").exists()) {
            Req_id = 1;
            return null;
        }

        if (!new MainActivity().isOnline()) {
            Req_id = 2;
            return null;
        }

        String OriginMD5 = calculateMD5(new File("/sdcard/Android/data/" + PackageName + "/files/Android/New/asset_textes.ab"));
        SharedPreferences prefs = context.getSharedPreferences("TxtKRVer", Context.MODE_PRIVATE);
        if (!OriginMD5.equals(getUpdateMD5(getName(PackageName))) || prefs.getInt(PackageName, 0) < getUpdateDate(PackageName)) {

            if(new File("/sdcard/GF_Tool/asset_textes.ab").exists()) new File("/sdcard/GF_Tool/asset_textes.ab").delete();

            String u = "https://github.com/choiman1559/choiman1559.github.io/tree/master/asset" + getName(PackageName) + "/asset_textes.ab";
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(u));
            request.setDescription("downloading " + getName(PackageName) + "'s text asset...");
            request.setTitle(getName(PackageName));
            request.setVisibleInDownloadsUi(false);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.allowScanningByMediaScanner();
            request.setDestinationInExternalPublicDir("/GF_Tool/","asset_textes.ab");

            DownloadManager manager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);

            while(!new File("/sdcard/GF_Tool/asset_textes.ab").exists()) {}
            if(DownloadManager.STATUS_SUCCESSFUL == 8) {
                File from = new File("/sdcard/GF_Tool/asset_textes.ab");
                File to = new File("/sdcard/Android/data/" + PackageName + "/files/Android/New/");

                copy(from.toString(),to.toString());
                if(prefs.getInt(PackageName, 0) != getUpdateDate(PackageName)) prefs.edit().putInt(PackageName,getUpdateDate(PackageName)).apply();
            } else {
                Req_id = 3;
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        dialog.dismiss();

        DetectGFService.isrunning = false;
        String Req_message;

        switch (Req_id) {
            case 0:
                return;

            case 1:
                Req_message = "데이터를 모두 받은 후 소전을 재시작해 주십시오!";
                break;

            case 2:
                Req_message = "인터넷을 확인 후 소전을 재시작해 주십시오!";
                break;

            case 3:
                Req_message = "데이터 다운로드에 실패했습니다!";
                break;

            default:
                Req_message = "Other Undefined error";
                break;
        }

        new AlertDialog.Builder(context)
                .setMessage(Req_message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    private String getName(String Packagename){
        if(Packagename.equals(context.getString(R.string.target_cn_bili))) return "bill";
        else if(Packagename.equals(context.getString(R.string.target_cn_uc))) return "dgts";
        else if(Packagename.equals(context.getString(R.string.target_tw))) return "chna";
        else if(Packagename.equals(context.getString(R.string.target_jp))) return "jpan";
        else return null;
    }

    private int getUpdateDate(String name) {
        Document doc = null;
        try { doc = Jsoup.connect("https://choiman1559.github.io/").get(); } catch (IOException e) { e.printStackTrace(); }
        Elements element = doc.select("div.container-lg.px-3.my-5.markdown-body");
        Iterator<Element> ie1 = element.select("p").iterator();
        String json = ie1.next().text();

        try {
            JSONObject foo = new JSONObject(json);
            JSONObject boo = foo.getJSONObject(name);
            return Integer.parseInt(boo.getString("date"));
        } catch (Exception e) {}
        return 1;
    }

    private String getUpdateMD5(String name) {
        Document doc = null;
        try { doc = Jsoup.connect("https://choiman1559.github.io/").get(); } catch (IOException e) { e.printStackTrace(); }
        Elements element = doc.select("div.container-lg.px-3.my-5.markdown-body");
        Iterator<Element> ie1 = element.select("p").iterator();
        String json = ie1.next().text();

        try {
            JSONObject foo = new JSONObject(json);
            JSONObject boo = foo.getJSONObject(name);
            return boo.getString("md5");
        } catch (Exception e) {}
        return null;
    }


    public static String calculateMD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e("MD5", "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e("MD5", "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e("MD5", "Exception on closing MD5 input stream", e);
            }
        }
    }

    void copy(String from,String to){
        File file = new File(from);
        try {
            FileInputStream inputStream = new FileInputStream(file);
            FileOutputStream outputStream = new FileOutputStream(to + "asset_textes.ab");

            FileChannel fcin = inputStream.getChannel();
            FileChannel fcout = outputStream.getChannel();

            long size = fcin.size();
            fcin.transferTo(0, size, fcout);

            fcout.close();
            fcin.close();

            outputStream.close();
            inputStream.close();
        }catch (IOException e) {

        }
        new File(from).delete();
    }
}
