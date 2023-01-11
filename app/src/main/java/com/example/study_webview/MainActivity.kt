package com.example.study_webview

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

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
        // 方式1: version<4.4 loadUrl
        button_loadUrl.setOnClickListener {
            // 通过Handler发送消息
            webView.post {
                webView?.loadUrl("javascript:callJS()")  // 调用javascript的callJS()方法, 注意调用的JS方法名要对应上
            }
        }
        // 方式2: version>=4.4 evaluateJavascript
        button_evaluateJS.setOnClickListener {
            webView.post {
                webView?.evaluateJavascript("javascript:callJS()") { resultCallback -> // 此处为js return的结果
                    Log.d(TAG, resultCallback)
                }
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
            // ---- Android调用Js 完 ----

            // ---- Js调用Android 方式3 ----
            // 方式3 -通过WebChromeClient拦截
            override fun onJsPrompt(
                view: WebView?,
                url: String?,
                message: String?,
                defaultValue: String?,
                result: JsPromptResult?
            ): Boolean {
                // 根据协议的参数，判断是否是所需要的url(同方式2)
                val uri = Uri.parse(message)   // 注意这里传入的是message不是url
                if (uri.scheme == "js") {
                    // 拦截url,下面js开始调用Android需要的方法
                    if (uri.authority == "android") {
                        println("js调用了Android的方法")
                        // 可以在协议上带有参数并传递到Android上
                        val parameterNames = uri.queryParameterNames
                        Log.d(
                            TAG,
                            "params =${uri.getQueryParameter(parameterNames.elementAt(0))} , ${
                                uri.getQueryParameter(parameterNames.elementAt(1))
                            }"
                        )
                        // 参数result代表消息框的返回值(输入值)
                        result?.confirm("js调用Android的方法成功啦")  // result会传入js的alert("demo " + result); 又由于alert被拦截，会触发onJsAlert
                    }
                    return true
                }
                return super.onJsPrompt(view, url, message, defaultValue, result)
            }
            // ---- Js调用Android 方式3 完 ----
        }

        // ---- Js调用Android 方式1&2 ----
        // 方式1 -通过addJavascriptInterface()将Java对象映射到JS对象
        // 参数1：Java对象名, 参数2：Javascript对象名
        webView.addJavascriptInterface(
            JsToAndroid(),
            "jsToAndroid"
        ) // JsToAndroid类对象映射到js的jsToAndroid对象

        // 方式2 -通过 WebViewClient的方法shouldOverrideUrlLoading()回调拦截url
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // 步骤1. 根据协议的参数，判断是否是所需要的url
                // 一般根据scheme(协议格式) & authority(协议名)判断 (前两个参数)
                // 假定传入进来的 url = "js://webview?arg1=111&arg2=222" (同时也是约定好的需要拦截的)
                val uri = Uri.parse(url)
                // 如果url的协议 == 预先约定的 js 协议, 就解析往下解析参数
                if (uri.scheme == "js") {
                    // 如果 authority == 预先约定协议里的 webview，即代表都符合约定的协议
                    // 所以拦截url,下面js开始调用Android需要的方法
                    if (uri.authority == "webview") {
                        // 步骤2. 执行js所需要调用Android的逻辑
                        println("js调用了Android的方法")
                        // 可以在协议上带有参数并传递到Android上
                        val parameterNames = uri.queryParameterNames
                        Log.d(
                            TAG,
                            "params =${uri.getQueryParameter(parameterNames.elementAt(0))} , ${
                                uri.getQueryParameter(parameterNames.elementAt(1))
                            }"
                        )
                    }
                    return true  // 这里必须返回true表示Android端拦截了url
                }
                return super.shouldOverrideUrlLoading(view, url)
            }
        }
        // ---- Js调用Android 方式1&2 完 ----
    }
}