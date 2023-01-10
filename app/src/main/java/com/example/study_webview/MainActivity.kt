package com.example.study_webview

import android.os.Bundle
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // setting
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true    // 设置与Js交互的权限
        webSettings.javaScriptCanOpenWindowsAutomatically = true    // 设置允许JS弹窗

        // ---- Android调用Js ----
        // 载入JS代码
        // 本地格式规定为:file:///android_asset/文件名.html。服务器文件可以直接将url写入
        webView.loadUrl("file:///android_asset/javascript.html")
        button_loadUrl.setOnClickListener {
            // 通过Handler发送消息
            webView.post {
                webView?.loadUrl("javascript:callJS()")  // 调用javascript的callJS()方法, 注意调用的JS方法名要对应上
            }
        }

        // 需要支持js对话框, 通过设置WebChromeClient对象处理JavaScript的对话框,设置响应js的Alert()函数
        // webview只是载体，内容的渲染需要使用webviewChromeClient类去实现
        webView.webChromeClient = object : WebChromeClient() {  // kt匿名内部类 -重写多个方法
            // 使用onJsAlert()拦截js的alert()函数
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                //创建一个AlertDialog来显示网页中的对话框
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Alert dialog")
                    .setMessage(message)  // message就是js中的alert传入的"callJs method is invoked from Javascript.html"
                    // 当我们设置拦截弹窗后，需要我们调用系统方法，通知系统我们弹窗的动态
                    // 例如，当我们的弹窗关闭，如果没有通知系统，系统会以为我们的弹窗还存在，下次点击就不会再走回调。
                    .setPositiveButton("ok") { dialog, which ->
                        result?.confirm()  // 当点击确定时，调用result?.confirm()
                    }
                    .setNegativeButton("cancel") { dialog, which ->
                        result?.cancel()   // 当点击取消时，调用result?.cancel()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
                return true  // 方法的返回值设置true，会拦截系统的弹窗
//                return super.onJsAlert(view, url, message, result)
            }
        }
        // ---- Android调用Js 完 ----

        // ---- todo Js调用Android ----
        // 通过addJavascriptInterface()将Java对象映射到JS对象
        // 参数1：Java对象名, 参数2：Javascript对象名
        webView.addJavascriptInterface(
            AndroidToJS(),
            "androidToJs"
        ) //AndroidToJS类对象映射到js的androidToJs对象
    }
}