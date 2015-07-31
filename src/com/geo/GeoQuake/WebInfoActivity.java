package com.geo.GeoQuake;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;


public class WebInfoActivity extends Activity {

    WebView mWebView;
    String mInfoUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_info_activity_layout);

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        Intent intent = getIntent();
        mInfoUrl = intent.getStringExtra("url");

        mWebView.loadUrl(mInfoUrl);

    }

    protected void onResume(){
        super.onResume();
    }

    protected void onDestroy(){
        super.onDestroy();
    }

}