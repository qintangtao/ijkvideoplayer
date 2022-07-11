package me.tang.xvideoplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object XUtil {

    fun scanForActivity(context: Context): Activity? {
        if (context is Activity) {
            return context
        } else if (context is ContextWrapper) {
            return scanForActivity(context.baseContext)
        }
        return null
    }

    private fun getAppCompActivity(context: Context): AppCompatActivity? {
        if (context is AppCompatActivity) {
            return context
        } else if (context is ContextThemeWrapper) {
            return getAppCompActivity(context.baseContext)
        }
        return null
    }

    fun setOrientation(context: Context, orientation: Int) {
        scanForActivity(context)?.requestedOrientation = orientation
    }

    fun screenBrightness(context: Context): Float {
        return scanForActivity(context)?.window?.attributes?.screenBrightness ?: 0f
    }

    fun screenBrightness(context: Context, brightness: Float) {
        val window = scanForActivity(context)?.window
        window?.let {
            val params = it.attributes
            params?.screenBrightness = brightness
            it.attributes = params
        }
    }


    @SuppressLint("RestrictedApi")
    fun showActionBar(context: Context) {
        getAppCompActivity(context)?.supportActionBar?.run {
            setShowHideAnimationEnabled(false)
            show()
        }
        scanForActivity(context)?.window?.let {
            //WindowCompat.setDecorFitsSystemWindows(it, false)
            ViewCompat.getWindowInsetsController(it.decorView)?.run {
                show(WindowInsetsCompat.Type.systemBars())
                XLog.d("showActionBar -> ")
            }
        }
        return
        scanForActivity(context)?.window?.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                insetsController?.show(WindowInsets.Type.statusBars())
            else
                clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    @SuppressLint("RestrictedApi")
    fun hideActionBar(context: Context) {
        getAppCompActivity(context)?.supportActionBar?.run {
            setShowHideAnimationEnabled(false)
            hide()
        }

        scanForActivity(context)?.window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            ViewCompat.getWindowInsetsController(it.decorView)?.run {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                XLog.d("hideActionBar -> ")
            }
        }

        return
        scanForActivity(context)?.window?.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insetsController?.hide(WindowInsets.Type.statusBars())
            }else
                setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
        }
    }

    fun formatTime(milliseconds: Long): String {
        var mins: Int
        var secs: Int = (milliseconds / 1000).toInt()
        val us: Int = (milliseconds % 1000).toInt()
        mins = secs / 60
        secs %= 60
        val hours = mins / 60
        mins %= 60
        //val msecs = (100 * us) / 1000

        val _mins = String.format("%02d", mins)
        val _secs = String.format("%02d", secs)
        //val _msecs = String.format("%02d", msecs)
        if (hours > 0) {
            val _hours = String.format("%02d", hours)
            return "$_hours:$_mins:$_secs" //.$_msecs
        } else {
            return "$_mins:$_secs"
        }
    }

    /**
     * 保存播放位置，以便下次播放时接着上次的位置继续播放.
     *
     * @param context
     * @param url     视频链接url
     */
    fun savePlayPosition(context: Context, url: String, position: Long) {
        context.getSharedPreferences("X_VIDEO_PALYER_PLAY_POSITION", Context.MODE_PRIVATE)
            .edit()
            .putLong(url, position)
            .apply()
    }

    /**
     * 取出上次保存的播放位置
     *
     * @param context
     * @param url     视频链接url
     * @return 上次保存的播放位置
     */
    fun getSavedPlayPosition(context: Context, url: String): Long {
        return context.getSharedPreferences("X_VIDEO_PALYER_PLAY_POSITION", Context.MODE_PRIVATE)
            .getLong(url, 0)
    }

    fun dp2px(dpValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun getScreenWidth(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            return wm.currentWindowMetrics.bounds.width()
        }
        val point = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.defaultDisplay.getRealSize(point)
        } else {
            wm.defaultDisplay.getSize(point)
        }
        return point.x
    }

    /**
     * Return the height of screen, in pixel.
     *
     * @return the height of screen, in pixel
     */
    fun getScreenHeight(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            return wm.currentWindowMetrics.bounds.height()
        }
        val point = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.defaultDisplay.getRealSize(point)
        } else {
            wm.defaultDisplay.getSize(point)
        }
        return point.y
    }
}