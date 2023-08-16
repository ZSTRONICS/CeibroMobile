package com.zstronics.ceibro.ui.tasks.v2.fileviewer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentFileViewerBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FileViewerFragment :
    BaseNavViewModelFragment<FragmentFileViewerBinding, IFileViewer.State, FileViewerVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: FileViewerVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_file_viewer
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewDataBinding.webView.visibility = View.VISIBLE

        val settings: WebSettings = mViewDataBinding.webView.settings
        settings.userAgentString =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
//        "Mozilla/5.0 (Linux; Android 12; SM-G975F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Mobile Safari/537.36"

        settings.javaScriptEnabled = true
        settings.databaseEnabled = true
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        settings.pluginState = WebSettings.PluginState.ON
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.allowContentAccess = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.loadsImagesAutomatically = true
        settings.allowFileAccess = true

        mViewDataBinding.webView.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                println("webView-onJsAlert: $message")
                return super.onJsAlert(view, url, message, result)
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                println("webView-onProgressChanged: $newProgress")
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                println("webView-onConsoleMessage: $consoleMessage")
                return super.onConsoleMessage(consoleMessage)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                println("webView-onReceivedTitle: $title")
            }
        }

        mViewDataBinding.webView.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                println("webView-onReceivedSslError: $error")
                handler?.proceed()
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                println("webView-onPageStarted: $url")
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                println("webView-shouldOverrideUrlLoading: ${request?.url.toString()}")
                view?.loadUrl(request?.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                println("webView-onPageFinished: $url")
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                println("webView-onReceivedError: ${error?.errorCode} // ${request?.url.toString()}")
            }
        }

        val fileLoaderUrl = "http://drive.google.com/viewerng/viewer?embedded=true&url=${viewModel.fileUrl}"
        val fileLoaderUrl1 = "https://docs.google.com/gview?embedded=true&url=${viewModel.fileUrl}"
        mViewDataBinding.webView.loadUrl(fileLoaderUrl1)
        println("webView-FileUrl: ${viewModel.fileUrl}")

    }
}