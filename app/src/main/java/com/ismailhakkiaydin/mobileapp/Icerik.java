package com.ismailhakkiaydin.mobileapp;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class Icerik  extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String link=getIntent().getStringExtra("link");

        WebView webView=new WebView(this);
        WebSettings ws=webView.getSettings();
        ws.setJavaScriptEnabled(true);
        webView.loadUrl(link);

        setContentView(webView);
    }
}
