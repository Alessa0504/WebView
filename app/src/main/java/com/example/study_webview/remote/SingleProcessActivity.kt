package com.example.study_webview.remote

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.remote.CalculateInterface
import com.example.study_webview.JsToAndroid
import com.example.study_webview.R
import com.example.study_webview.util.ProcessUtil
import kotlinx.android.synthetic.main.activity_single_process.*
import listener.IRemoteListener
import org.json.JSONException
import org.json.JSONObject

/**
 * @Description: 此Activity位于独立进程remoteWeb中
 * @author zouji
 * @date 2023/1/14
 */
class SingleProcessActivity : AppCompatActivity(), IRemoteListener {

    companion object {
        // kt的const val就是java中的static final，相比于val的区别是:const val是编译时常量，而val是运行时常量，仍可以通过反射修改值
        const val CONTENT_SCHEME = "file:///android_asset/remote_web.html"
    }

    private val TAG = SingleProcessActivity::class.java.simpleName
    private var mRemoteService: CalculateInterface? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_process)
        val settings = webViewRemote?.settings
        settings?.javaScriptEnabled = true  //允许js交互
        val jsToAndroid = JsToAndroid()
        jsToAndroid.setListener(this)
        webViewRemote?.addJavascriptInterface(jsToAndroid, "jsToAndroid")  //注册JS交互接口
        webViewRemote?.loadUrl(CONTENT_SCHEME)
    }

    override fun onResume() {
        super.onResume()
        webViewRemote?.let {
            webViewRemote?.onResume()
        }
        initService()
    }

    override fun onPause() {
        super.onPause()
        webViewRemote?.let {
            webViewRemote?.onPause()
        }
        unbindService(serviceConnection)
    }

    override fun onDestroy() {
        // 销毁webView，释放资源
        if (webViewRemote != null) {
            // mimeType为媒体类型，text/html:超文本文件
            webViewRemote?.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            webViewRemote?.clearHistory()

            (webViewRemote?.parent as ViewGroup).removeView(webViewRemote)
            webViewRemote?.destroy()
        }
        super.onDestroy()
    }

    /**
     * 绑定远程服务RemoteService
     */
    private fun initService() {
        val intent = Intent()
        intent.component = ComponentName(
            "com.example.study_webview",
            "com.example.study_webview.remote.RemoteService"
        )
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    private val serviceConnection = object : ServiceConnection {
        // *该方法中的IBinder参数就是 RemoteService onBind返回出来的Binder
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // 当Service绑定成功时，通过Binder获取到远程服务代理
            mRemoteService = CalculateInterface.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mRemoteService = null
        }
    }

    override fun deal(cmd: String, param: String) {
        Log.d(
            TAG,
            "当前进程：${
                ProcessUtil.getProcessName(this)
            } ,主进程：${ProcessUtil.isMainProcess(this)}"
        )
        dealWithPost(cmd, param)
    }

    private fun dealWithPost(cmd: String, param: String) {
        if (mRemoteService == null) {
            return
        }
        when (cmd) {
            "showToast" -> {
                mRemoteService?.showToast()
            }
            "appCalculate" -> {
                Log.d(TAG, "appCalculate --> $param")
                try {
                    val jsonObject = JSONObject(param)
                    // json解析:参数和js中对应上
                    val firstNum = jsonObject.optString("firstNum").toDouble()
                    val secondNum = jsonObject.optString("secondNum").toDouble()
                    mRemoteService?.doCalculate(firstNum, secondNum)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            else -> {
                Log.d(TAG, "Native端暂未实现该方法")
            }
        }
    }
}