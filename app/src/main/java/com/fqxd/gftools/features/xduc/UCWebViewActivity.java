package com.fqxd.gftools.features.xduc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fqxd.gftools.R;

import java.util.Locale;

public class UCWebViewActivity extends AppCompatActivity {
    ProgressBar bar;
    ValueCallback mFilePathCallback;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        bar = findViewById(R.id.progressBar);
        bar.setVisibility(View.VISIBLE);

        CookieManager cm = CookieManager.getInstance();
        cm.removeAllCookies(value -> { });
        cm.flush();

        WebView webView = findViewById(R.id.webView);
        webView.clearCache(true);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);

        webView.setWebViewClient(new myWebClient());
        webView.setWebChromeClient(new myChromeClient());
        webView.setNetworkAvailable(true);
        webView.setBackgroundColor(0);
        webView.loadUrl(getIntent().getStringExtra("uri"));
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
            Uri uri = Uri.parse(url);
            if (uri.toString().toLowerCase(Locale.CHINESE).indexOf("comet://sdk_should_take_photos") == 0) {
                view.loadUrl(String.format("javascript:sdk_should_take_snapshot_new('%s');", "ok"));
                return true;
            } else if (uri.toString().toLowerCase(Locale.CHINESE).indexOf("comet://sdk_should_guest_bind_gamecenter") == 0) {
                view.loadUrl(String.format("javascript:sdk_should_guest_bind_gamecenter('%s');", "ok"));
                return true;
            } else if (uri.toString().toLowerCase(Locale.CHINESE).indexOf("comet://sdk_should_guest_bind_line") == 0) {
                view.loadUrl(String.format("javascript:sdk_should_guest_bind_line('%s');", "ok"));
                return true;
            } else if (uri.toString().toLowerCase(Locale.CHINESE).indexOf("comet://sdk_should_guest_bind_apple") == 0) {
                view.loadUrl(String.format("javascript:sdk_should_guest_bind_apple('%s');", "ok"));
                return true;
            } else if (uri.toString().toLowerCase(Locale.CHINESE).indexOf("comet://sdk_should_guest_bind_wx") == 0) {
                view.loadUrl(String.format("javascript:sdk_should_guest_bind_wx('%s');", "ok"));
                return true;
            } else if (uri.toString().toLowerCase(Locale.CHINESE).indexOf("comet://sdk_should_guest_bind_facebook") == 0) {
                view.loadUrl(String.format("javascript:sdk_should_guest_bind_facebook('%s');", "ok"));
                return true;
            } else if (uri.toString().toLowerCase(Locale.CHINESE).indexOf("comet://sdk_should_guest_bind_google") == 0) {
                view.loadUrl(String.format("javascript:sdk_should_guest_bind_google('%s');", "ok"));
                return true;
            } else if (uri.toString().toLowerCase(Locale.CHINESE).indexOf("comet://sdk_should_guest_bind_twitter") == 0) {
                view.loadUrl(String.format("javascript:sdk_should_guest_bind_twitter('%s');", "ok"));
                return true;
            }
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,requestCode,data);
        if(requestCode == 10000 && resultCode == Activity.RESULT_OK){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mFilePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            } else mFilePathCallback.onReceiveValue(new Uri[]{data.getData()});
            mFilePathCallback = null;
        } else mFilePathCallback.onReceiveValue(null);
    }

    class myChromeClient extends WebChromeClient {
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback filePathCallback, FileChooserParams fileChooserParams) {
            mFilePathCallback = filePathCallback;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Image Chooser"), 10000);
            return true;
        }
    }
}
