package com.smkn1.examapp

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
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
    }

    private fun setupWebView(url: String?) {
        formview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progressBar.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                return super.shouldInterceptRequest(view, request)
            }

            @Suppress("DEPRECATION")
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                if (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT || errorCode == ERROR_TIMEOUT) {
                    handleError()
                }
            }


            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (error?.errorCode == ERROR_HOST_LOOKUP || error?.errorCode == ERROR_CONNECT || error?.errorCode == ERROR_TIMEOUT) {
                    handleError()
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                formview.visibility = View.VISIBLE
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


    private fun handleError() {
        Toast.makeText(context, "Halaman tidak dapat diakses", Toast.LENGTH_LONG).show()
    }
}