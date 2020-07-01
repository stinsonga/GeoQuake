package com.geo.geoquake

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import com.geo.geoquake.R


class WebInfoActivity : Activity() {

    lateinit var mWebView: WebView
    var mInfoUrl: String? = ""

    @SuppressLint("SetJavaScriptEnabled")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.web_info_activity_layout)

        mWebView = findViewById(R.id.webView)
        mWebView.settings.javaScriptEnabled = true
        mWebView.settings.builtInZoomControls = true
        val intent = intent
        intent.getStringExtra("url")?.let {
            mInfoUrl = intent.getStringExtra("url")
            mWebView.loadUrl(mInfoUrl)
        }
    }

}