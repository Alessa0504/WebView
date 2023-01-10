package com.example.study_webview

import android.webkit.JavascriptInterface

/**
 * @Description: Android调用js
 * @author zouji
 * @date 2023/1/10
 */
class AndroidToJS {
    // 定义JS需要调用的方法，被JS调用的方法必须加入@JavascriptInterface注解
    @JavascriptInterface
    fun hello(msg: String?) {
        println(msg)
    }
}