package com.geo.GeoQuake;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;


public class WebInfoActivity extends Activity {

    WebView webView;
    String infoURL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_info_activity_layout);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        Intent intent = getIntent();
        infoURL = intent.getStringExtra("url");

        webView.loadUrl(infoURL);

    }

    protected void onResume(){
        super.onResume();
    }

    protected void onDestroy(){
        super.onDestroy();
    }

}