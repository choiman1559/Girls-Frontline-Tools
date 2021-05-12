package com.fqxd.gftools.features.icon;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bigzhao.xml2axml.AxmlUtils;
import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionMethod;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtil;
import org.jetbrains.annotations.TestOnly;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fqxd.gftools.implement.AsyncTask;
import com.kellinwood.security.zipsigner.ZipSigner;

@SuppressLint("StaticFieldLeak")
final class IconChangeTask extends AsyncTask {
    private final AppCompatActivity main;
    private final TextView status;
    private final TextView log;
    private final ProgressBar progress;
    private String target;
    private String CName;
    private File imageFile;

    IconChangeTask(AppCompatActivity main, TextView status, TextView log, ProgressBar progress, String target, File imageFile, String CName) {
        this.main = main;
        this.status = status;
        this.log = log;
        this.progress = progress;
        this.target = target;
        this.imageFile = imageFile;
        this.CName = CName;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.updateStatus("이전 작업이 완료될때까지 대기중...");
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            //TODO: implement Manifest edit code
            /*
            if (BuildConfig.DEBUG) {
                //Codes for change girls frontline's app_name
                //stopping development cause Permission denied error when executing aapt
                File aaptFolder = new File(main.getApplicationInfo().nativeLibraryDir);
                if (!aaptFolder.exists()) aaptFolder.mkdirs();
                File aapt = new File(aaptFolder.getPath() + "/aapt7.1");
                if (!aapt.exists()) {
                    AssetManager assetManager = main.getResources().getAssets();
                    for (String f : assetManager.list("aapt")) {
                        InputStream is = assetManager.open("aapt/" + f);
                        String[] sl = f.split("/");
                        OutputStream os = new FileOutputStream(aaptFolder + "/" + sl[sl.length - 1]);
                        IOUtil.copy(is, os);
                        is.close();
                        os.close();
                    }
                }
                aapt.setExecutable(true);
                //Runtime.getRuntime().exec("chmod 777 " + aapt.getAbsolutePath()).waitFor();
                Process process = Runtime.getRuntime().exec(aapt.getAbsolutePath() + " -?");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.d("EXECLog", line);
                }
                process.waitFor();
            }
            */

            final ProgressBar p = this.main.findViewById(R.id.running);
            p.post(() -> p.setVisibility(View.VISIBLE));
            status.post(() -> status.setVisibility(View.VISIBLE));

            this.updateProgress(0);
            this.updateLog(this.main.getString(R.string.info_patch_started));
            this.updateLog("Target: " + this.target);

            File temp = this.main.getExternalFilesDir(null);
            FileUtils.deleteDirectory(temp);
            temp.mkdir();

            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(CompressionMethod.STORE);
            parameters.setOverrideExistingFilesInZip(true);
            parameters.setEncryptFiles(false);
            this.updateStatus("extracting apk");
            this.updateProgress(25);

            File originalApk = new File(temp.getAbsolutePath() + "/base.apk");
            FileUtils.copyFile(new File(this.main.getPackageManager().getPackageInfo(target, 0).applicationInfo.publicSourceDir), originalApk);
            File apk = new File(temp.getAbsolutePath() + "/apk");
            ZipFile zipFile = new ZipFile(originalApk);
            zipFile.extractAll(apk.getAbsolutePath());

            this.updateLog("apk extracted");
            this.updateStatus("adding icon file");
            this.updateProgress(50);

            //TODO: implement Manifest edit code
            //Codes for change girls frontline's app_name
            //stopping development cause lack of technology for encoding android binary xml in android runtime
            File base_xml = new File(apk.getAbsolutePath() + "/AndroidManifest.xml");
            File edit_xml = new File(temp.getAbsolutePath() + "/AndroidManifest.xml");
            File decode_xml = new File(temp.getAbsolutePath() + "/AndroidManifest_decoded.xml");
            if (BuildConfig.DEBUG) {
                FileUtils.copyFile(base_xml, edit_xml);
            }

            FileUtils.deleteDirectory(new File(apk.getAbsolutePath() + "/res/"));
            List<File> originalImages = new ArrayList<>();
            originalImages.add(new File(apk.getAbsolutePath() + "/res/drawable-xxxhdpi/app_icon.png"));
            originalImages.add(new File(apk.getAbsolutePath() + "/res/drawable-xxhdpi/app_icon.png"));
            originalImages.add(new File(apk.getAbsolutePath() + "/res/drawable-xhdpi/app_icon.png"));
            originalImages.add(new File(apk.getAbsolutePath() + "/res/drawable-hdpi/app_icon.png"));
            originalImages.add(new File(apk.getAbsolutePath() + "/res/drawable-mdpi/app_icon.png"));
            originalImages.add(new File(apk.getAbsolutePath() + "/res/drawable-ldpi/app_icon.png"));
            originalImages.add(new File(apk.getAbsolutePath() + "/res/drawable/app_icon.png"));

            File convertedImage = imageFile;
            if (imageFile.getName().contains("jpg")) {
                try {
                    Bitmap bmp = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    convertedImage = new File(Environment.getExternalStorageDirectory() + "/Converted.png");
                    FileOutputStream outStream = new FileOutputStream(convertedImage);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.flush();
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            for (File f : originalImages) {
                if (f.exists()) f.delete();
                FileUtils.copyFile(convertedImage, f);
            }
            convertedImage.delete();

            if(BuildConfig.DEBUG) {
                //TODO: implement Manifest edit code
                //Codes for change girls frontline's app_name
                //stopping development cause lack of technology for encoding android binary xml in android runtime
                ///*
                this.updateProgress(55);
                this.updateStatus("editing app name");

                String decoded = AxmlUtils.decode(edit_xml);
                editFileXmlTo(decoded, decode_xml, CName);
                byte[] encoded = AxmlUtils.encode(decoded);
                edit_xml.delete();
                try (FileOutputStream fos = new FileOutputStream(edit_xml)) {
                    fos.write(encoded);
                }
                decode_xml.delete();
                FileUtils.copyFile(edit_xml, base_xml);
                //*/
            }

            this.updateProgress(60);
            this.updateStatus("repacking apk file");

            for (File f : apk.listFiles()) {
                Log.d("list", f.getAbsolutePath());
                if (f.isFile()) {
                    zipFile.addFile(f, parameters);
                } else {
                    zipFile.addFolder(f, parameters);
                }
            }

            this.updateProgress(90);
            this.updateStatus("singing apk file");

            File SignedApk = new File(temp.getAbsolutePath() + "/signed.apk");
            ZipSigner zipSigner = new ZipSigner();
            zipSigner.setKeymode(ZipSigner.KEY_TESTKEY);
            zipSigner.signZip(originalApk.getAbsolutePath(), SignedApk.getAbsolutePath());

            this.updateLog("apk repackaged");
            this.updateStatus("finished");
            this.updateProgress(100);
            this.updateLog("installing apk");

            File obb = new File(temp.getAbsolutePath() + "/obb");
            copyObbDirectory(this.target, obb);
            main.startActivityForResult(new Intent(Intent.ACTION_UNINSTALL_PACKAGE).setData(Uri.parse("package:" + target)), 5555);

            p.post(() -> p.setVisibility(View.INVISIBLE));
            status.post(() -> status.setVisibility(View.INVISIBLE));
            ((Runnable) objects[0]).run();
            this.updateProgress(100);
            updateStatus("\n");
            FileUtils.deleteDirectory(apk);
        } catch (Exception e) {
            this.updateLog(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static void copyObbDirectory(String target, File togo) throws Exception {
        File originalOBB = new File(Global.Storage + "/Android/obb/" + target);
        if (originalOBB.exists() && originalOBB.isDirectory()) {
            if (!togo.exists()) togo.mkdirs();
            File[] list = originalOBB.listFiles();
            for (File file : list) {
                FileUtils.copyFile(file, new File(togo.getAbsolutePath() + "/" + file.getName()));
            }
        }
    }

    @TestOnly
    //TODO: implement Manifest edit code
    //Codes for change girls frontline's app_name
    //stopping development cause lack of technology for encoding android binary xml in android runtime
    private void editFileXmlTo(String string,File output, String ChangeLineTo) throws IOException {
        String[] lines = string.split(System.getProperty("line.separator"));
        StringBuilder Lines = new StringBuilder();
        for(String line : lines) {
            if (!line.contains("label=\"")) {
                Lines.append(line);
                continue;
            }
            Lines.append(line.replace(substringBetween(line,"label=\"","\""), ChangeLineTo));
            Lines.append("\r\n");
            Log.d("line",line);
        }

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output, false), StandardCharsets.UTF_8));
        bw.write(Lines.toString());
        bw.close();
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

    private void updateStatus(final String str) {
        this.status.post(() -> status.setText(str));
        this.updateLog(str);
    }

    private void updateLog(final String str) {
        this.log.post(() -> {
            log.append(str + "\r\n");
            Log.d("patch", str);
        });
    }

    private void updateProgress(final int percent) {
        this.progress.post(() -> progress.setProgress(percent));
    }
}

