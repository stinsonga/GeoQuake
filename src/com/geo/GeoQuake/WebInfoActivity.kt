package com.geo.GeoQuake

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView


class WebInfoActivity : Activity() {

    lateinit var mWebView: WebView
    lateinit var mInfoUrl: String

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.web_info_activity_layout)

        mWebView = findViewById(R.id.webView)
        mWebView.settings.javaScriptEnabled = true
        mWebView.settings.builtInZoomControls = true
        val intent = intent
        mInfoUrl = intent.getStringExtra("url")

        mWebView.loadUrl(mInfoUrl)

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}