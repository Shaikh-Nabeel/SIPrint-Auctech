package com.auctech.siprint

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.auctech.siprint.databinding.ActivityCommonWebBinding

class CommonWebActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommonWebBinding
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommonWebBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra("url") ?: finish()

        val webView: WebView = findViewById(R.id.webview)

        // Enable JavaScript for the form submission
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Handle form submissions in the WebView
        webView.webChromeClient = object : WebChromeClient() {
            // Handle progress updates here if needed
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return true // Allow loading the URL in the WebView
            }
        }

        // Load your contact us page URL
//        val contactUsUrl = "https://www.yourwebsite.com/contact-us"
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                // This method will be called when the page starts loading
                binding.loading.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                // This method will be called when the page finishes loading
                binding.loading.visibility = View.GONE
            }

            @Deprecated("Deprecated in Java")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                // Handle errors here
                binding.loading.visibility = View.GONE
                Toast.makeText(
                    applicationContext,
                    "Error loading the page: $description",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        webView.loadUrl(url as String)
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val webView: WebView = findViewById(R.id.webview)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}