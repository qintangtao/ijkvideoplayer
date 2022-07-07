package me.tang.xvideoplayer

import android.util.Log


object XLog {

    private val TAG = "XVideoPlayer"

    fun d(msg: String) = Log.d(TAG, msg)

    fun i(msg: String) = Log.i(TAG, msg)

    fun e(msg: String, throwable: Throwable) = Log.e(TAG, msg, throwable)

}