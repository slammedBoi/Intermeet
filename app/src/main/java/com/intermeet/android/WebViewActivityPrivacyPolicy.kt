package com.intermeet.android

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class WebViewActivityPrivacyPolicy : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val webView: WebView = findViewById(R.id.webview)

        // Get the URL from the intent extras
        val url = intent.getStringExtra("url")
        webView.loadUrl(url ?: "https://intermeetiate.github.io/InterMeetiatePrivatePolicy") // Load a default URL if none provided
    }
}
