package com.fqxd.gftools.ui;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fqxd.gftools.R;


import org.apache.commons.io.FileUtils;

public class GFDownloadActivity extends AppCompatActivity {
    private ProgressBar bar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        bar = findViewById(R.id.progressBar);
        bar.setVisibility(View.VISIBLE);

        String uri = getUri(getIntent().getStringExtra("pkg"));
        findViewById(R.id.openInWebLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.openInWebButton).setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(browserIntent);
        });

        CookieManager cm = CookieManager.getInstance();
        cm.removeAllCookies(value -> { });
        cm.flush();

        WebView webView = findViewById(R.id.webView);
        webView.clearCache(true);
        webView.clearFormData();
        webView.clearHistory();
        webView.clearMatches();
        webView.clearSslPreferences();

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);


        if(uri.contains("apkpure")) webView.getSettings().setUserAgentString("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:80.0) Gecko/20100101 Firefox/80.0");
        webView.setWebViewClient(new myWebClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.setNetworkAvailable(false);
        webView.loadUrl(uri);
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
            String Extension = FileUtils.getExtension(fileName);

            if (Extension.equals("xapk") || Extension.equals("apk")) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.allowScanningByMediaScanner();
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setTitle(fileName);
                request.setDescription("Downloading File...");
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
            } else Toast.makeText(getApplicationContext(), "Not APK or XAPK File!", Toast.LENGTH_LONG).show();
        });
    }

    private String getUri(String Package) {
        switch (Package) {
            case "com.digitalsky.girlsfrontline.cn.uc":
                return "https://apps.qoo-app.com/ko/app/7629";

            case "com.sunborn.girlsfrontline.jp":
                return "https://apps.qoo-app.com/ko/app/6742";

            case "com.sunborn.girlsfrontline.en":
                return "https://apps.qoo-app.com/ko/app/6647";

            case "com.sunborn.girlsfrontline.cn":
                return "https://gf-cn.sunborngame.com/";

            case "com.digitalsky.girlsfrontline.cn.bili":
                return "https://app.biligame.com/page/detail_game.html?id=73";

            default:
                return "https://apkpure.com/kr/" + Package;
        }
    }

    public class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            bar.setVisibility(View.GONE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}