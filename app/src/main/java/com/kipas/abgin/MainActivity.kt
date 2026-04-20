package com.kipas.abgin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var wv: WebView
    private var fileCallback: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_REQUEST = 1001
    private val PERM_REQUEST = 1002

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#ffffff")
        }
        // Request runtime permissions
        val permsToRequest = arrayOf("android.permission.CAMERA", "android.permission.RECORD_AUDIO", "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.POST_NOTIFICATIONS", "android.permission.READ_CONTACTS", "android.permission.WRITE_EXTERNAL_STORAGE").filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        if (permsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permsToRequest, PERM_REQUEST)
        }
        wv = WebView(this)
        wv.layoutParams = FrameLayout.LayoutParams(-1, -1)
        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportMultipleWindows(true)
        }
        wv.webViewClient = WebViewClient()
        wv.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = filePathCallback
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                val chooser = Intent.createChooser(intent, "Pilih File")
                startActivityForResult(chooser, FILE_CHOOSER_REQUEST)
                return true
            }
        }
        setContentView(wv)
        wv.loadUrl("file:///android_asset/index.html")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_REQUEST) {
            val result = if (resultCode == Activity.RESULT_OK && data != null) {
                data.data?.let { arrayOf(it) }
            } else null
            fileCallback?.onReceiveValue(result)
            fileCallback = null
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @Deprecated("Deprecated")
    override fun onBackPressed() {
        if (wv.canGoBack()) wv.goBack() else super.onBackPressed()
    }
}