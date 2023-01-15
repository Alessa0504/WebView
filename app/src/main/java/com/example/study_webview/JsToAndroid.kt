package com.example.study_webview

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import listener.IRemoteListener

/**
 * @Description: Js调用Android
 * @author zouji
 * @date 2023/1/10
 */
class JsToAndroid {
    private var listener: IRemoteListener? = null
    private val mHandler = Handler(Looper.getMainLooper())

    // 定义JS需要调用的方法，被JS调用的方法必须加入@JavascriptInterface注解
    @JavascriptInterface
    fun hello(msg: String?) {
        println(msg)
    }

    @JavascriptInterface
    fun post(cmd: String, param: String) {
        listener?.let {
            mHandler.post {
                listener?.deal(cmd, param)
            }
        }
    }

    fun setListener(listener: IRemoteListener) {
        this.listener = listener
    }
}