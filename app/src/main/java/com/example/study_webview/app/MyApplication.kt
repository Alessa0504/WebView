package com.example.study_webview.app

import android.app.Application
import android.os.Build
import android.webkit.WebView


/**
 * @Description: 自定义Application
 * @author zouji
 * @date 2023/1/15
 */
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // 修复WebView的多进程加载的crash:
        // Using WebView from more than one process at once with the same data directory is not supported
        initWebView()
    }

    private fun initWebView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName()
            WebView.setDataDirectorySuffix(processName)
        }
    }
}