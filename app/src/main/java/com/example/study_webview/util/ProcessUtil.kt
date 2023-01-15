package com.example.study_webview.util

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.util.Log
import com.example.study_webview.BuildConfig

/**
 * @Description: 进程工具类
 * @author zouji
 * @date 2023/1/15
 */
object ProcessUtil {

    private val TAG = ProcessUtil::class.java.simpleName

    /**
     * 获取当前进程名
     * @param context
     * @return
     */
    fun getProcessName(context: Context): String? {
        val pid = Process.myPid()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses
        val processInfoList = runningAppProcesses?.filter {
            it.pid == pid
        }
        processInfoList?.let {
            return processInfoList.first().processName
        } ?: return null
    }

    /**
     * 判断是否为主进程
     * @param context
     * @return
     */
    fun isMainProcess(context: Context): Boolean {
        val processName = getProcessName(context)
        Log.d(TAG, "当前进程 processName= $processName")
        return BuildConfig.APPLICATION_ID == processName
    }
}