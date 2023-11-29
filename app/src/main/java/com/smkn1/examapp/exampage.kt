package com.smkn1.examapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
//import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout


class exampage: Fragment() {

    private lateinit var formview: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_exampage, container, false)
        formview = view.findViewById(R.id.webview)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        progressBar = view.findViewById(R.id.progressBar)

        val bundle = arguments
        val url = bundle!!.getString("URL")

        setupWebView(url)

        swipeRefreshLayout.setOnRefreshListener {
            formview.reload()
        }

        return view

//    @SuppressLint("SuspiciousIndentation", "SetJavaScriptEnabled")
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//
//        val view: View = inflater.inflate(R.layout.fragment_exampage, container, false)
//        formview = view.findViewById(R.id.webview)
//
//        val bundle = arguments
//        val url = bundle!!.getString("URL")
//
//        formview.webViewClient = WebViewClient()
//            formview.loadUrl(url.toString())
//            formview.settings.javaScriptEnabled = true
//            formview.settings.loadWithOverviewMode = true
//            formview.settings.useWideViewPort = true
//            formview.settings.builtInZoomControls = true
////            formview.settings.pluginState = WebSettings.PluginState.ON
//        return view

    }

    private fun setupWebView(url: String?) {
        formview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Hide the progress bar when the page is finished loading
                progressBar.visibility = View.GONE
                // Stop the refreshing animation
                swipeRefreshLayout.isRefreshing = false
            }
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                // Implement your logic to inspect and modify requests
                // Example: Check for malicious content and block if necessary

                return super.shouldInterceptRequest(view, request)
            }
        }
        formview.loadUrl(url.toString())
        formview.settings.javaScriptEnabled = true
        formview.settings.loadWithOverviewMode = true
        formview.settings.useWideViewPort = true
        formview.settings.builtInZoomControls = true

    }
    @Override
    override fun onDetach() {
        super.onDetach()
        //
    }
}