package com.example.ui

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MiniBrowserScreen() {
    var url by remember { mutableStateOf("https://www.duckduckgo.com") }
    var inputUrl by remember { mutableStateOf("") }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { webViewRef?.goBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                IconButton(onClick = { webViewRef?.goForward() }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Forward")
                }
                IconButton(onClick = { webViewRef?.reload() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reload")
                }
                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    placeholder = { Text("Enter URL to Test") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(50.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            val targetUrl = if (inputUrl.startsWith("http://") || inputUrl.startsWith("https://")) {
                                inputUrl
                            } else if (inputUrl.contains(".")) {
                                "https://$inputUrl"
                            } else {
                                "https://duckduckgo.com/?q=$inputUrl"
                            }
                            url = targetUrl
                            webViewRef?.loadUrl(targetUrl)
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )
            }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                TextButton(onClick = { 
                    url = "https://www.google.com"
                    webViewRef?.loadUrl(url)
                }) { Text("Google") }
                TextButton(onClick = { 
                    url = "https://duckduckgo.com"
                    webViewRef?.loadUrl(url)
                }) { Text("DuckDuckGo") }
                TextButton(onClick = { 
                    url = "view-source:$url"
                    webViewRef?.loadUrl(url)
                }) { Text("Source Code") }
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun doUpdateVisitedHistory(view: WebView?, _url: String?, isReload: Boolean) {
                            if (_url != null && !_url.startsWith("view-source:")) {
                                inputUrl = _url
                            }
                            super.doUpdateVisitedHistory(view, _url, isReload)
                        }
                    }
                    webChromeClient = WebChromeClient()
                    loadUrl(url)
                    webViewRef = this
                }
            },
            update = { view ->
                // Updating is handled internally by WebView loadUrl when the state changes
            }
        )
    }
}
