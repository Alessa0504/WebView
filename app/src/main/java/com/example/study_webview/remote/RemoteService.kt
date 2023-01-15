package com.example.study_webview.remote

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.remote.CalculateInterface
import com.example.study_webview.util.ProcessUtil

/**
 * @Description: 远程Service【Server】, 此Service位于主进程中
 * @author zouji
 * @date 2023/1/14
 */
class RemoteService : Service() {

    private val TAG = RemoteService::class.java.simpleName

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private val mBinder = object : CalculateInterface.Stub() {

        /**
         * remoteWeb进程调用主进程的doCalculate方法，实现进程间通信
         * @param a
         * @param b
         * @return
         */
        override fun doCalculate(a: Double, b: Double): Double {
            val calculate = Calculate()
            val result = calculate.calculateSum(a, b)
            val handler = Handler(Looper.getMainLooper())
            // 切到主线程ui展示结果
            handler.post {
                Toast.makeText(
                    applicationContext,
                    "remoteWeb进程调用了主进程的doCalculate方法, 计算结果为：$result",
                    Toast.LENGTH_LONG
                ).show()
            }
            return result
        }

        /**
         * remoteWeb进程调用主进程的showToast方法，实现进程间的通信
         */
        override fun showToast() {
            Log.d(
                TAG, "showToast processName: ${
                    ProcessUtil.getProcessName(
                        applicationContext
                    )
                }, isMainProcess: ${ProcessUtil.isMainProcess(applicationContext)}"
            )
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                Toast.makeText(
                    applicationContext,
                    "remoteWeb进程调用了主进程的showToast方法",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}