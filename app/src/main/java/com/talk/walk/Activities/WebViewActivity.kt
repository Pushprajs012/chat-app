package com.talk.walk.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.TextView
import com.talk.walk.R
import android.webkit.WebViewClient





class WebViewActivity : AppCompatActivity() {

    private lateinit var ibBack: ImageButton
    private lateinit var tvChatPersonName: TextView
    private lateinit var wb: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        var url = intent.getStringExtra("url")
        var type = intent.getStringExtra("type")

        ibBack = findViewById(R.id.ibBack)
        tvChatPersonName = findViewById(R.id.tvChatPersonName)
        wb = findViewById(R.id.webView)

        tvChatPersonName.text = type

        wb.getSettings().setJavaScriptEnabled(true)
        wb.getSettings().setLoadWithOverviewMode(true)
        wb.getSettings().setUseWideViewPort(true)
        wb.getSettings().setBuiltInZoomControls(true)
        wb.getSettings().setPluginState(WebSettings.PluginState.ON)
//        wb.getSettings().setPluginsEnabled(true)
        wb.setWebViewClient(HelloWebViewClient())
        url?.let { wb.loadUrl(it) }


        ibBack.setOnClickListener {
            onBackPressed()
        }

    }

    private class HelloWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return false
        }
    }
}