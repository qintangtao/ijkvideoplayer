package me.tang.xvideoplayer

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class XVideoController : FrameLayout, View.OnTouchListener,
    IVideoPlayer.OnPlayStateListener, IVideoPlayer.OnPlayModeListener {

    companion object {
        const val THRESHOLD = 80
    }

    protected val mainScope = MainScope()

    private var _updateProgressJob: Job? = null

    protected var _videoPlayer: IVideoPlayer? = null
    val videoPlayer get() = _videoPlayer!!

    private var _downX = 0f
    private var _downY = 0f

    private var _needChangePosition = false
    private var _needChangeVolume = false
    private var _needChangeBrightness = false

    private var _gestureDownPosition: Long = 0
    private var _gestureDownBrightness = 0f
    private var _gestureDownVolume = 0

    private var _newPosition: Long = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.setOnTouchListener(this)
    }

    open fun setVideoPlayer(videoPlayer: IVideoPlayer) {
        _videoPlayer = videoPlayer
        _videoPlayer?.setOnPlayStateListener(this)
        _videoPlayer?.setOnPlayModeListener(this)
    }

    fun startUpdateProgressTimer() {
        cancelUpdateProgressTimer()
        _updateProgressJob = flow {
            while (true) {
                emit(1)
                delay(1000)
            }
        }.flowOn(Dispatchers.IO)
            .onEach { updateProgress() }
            .launchIn(mainScope)
    }

    fun cancelUpdateProgressTimer() {
        _updateProgressJob?.cancel()
        _updateProgressJob = null
    }

    /**
     * 设置播放的视频的标题
     *
     * @param title 视频标题
     */
    abstract fun setTitle(title: String)

    /**
     * 视频底图
     *
     * @param resId 视频底图资源
     */
    abstract fun setImage(@DrawableRes resId: Int)

    /**
     * 视频底图ImageView控件，提供给外部用图片加载工具来加载网络图片
     *
     * @return 底图ImageView
     */
    abstract fun imageView(): ImageView?

    /**
     * 设置总时长.
     */
    abstract fun setLenght(length: Long)

    /**
     * 重置控制器，将控制器恢复到初始状态。
     */
    abstract fun reset()

    /**
     * 更新进度，包括进度条进度，展示的当前播放位置时长，总时长等。
     */
    protected abstract fun updateProgress()

    /**
     * 手势左右滑动改变播放位置时，显示控制器中间的播放位置变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     *
     * @param duration            视频总时长ms
     * @param newPositionProgress 新的位置进度，取值0到100。
     */
    protected abstract fun showChangePosition(duration: Long, newPositionProgress: Int)

    /**
     * 手势左右滑动改变播放位置后，手势up或者cancel时，隐藏控制器中间的播放位置变化视图，
     * 在手势ACTION_UP或ACTION_CANCEL时调用。
     */
    protected abstract fun hideChangePosition()

    /**
     * 手势在右侧上下滑动改变音量时，显示控制器中间的音量变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     *
     * @param newVolumeProgress 新的音量进度，取值1到100。
     */
    protected abstract fun showChangeVolume(newVolumeProgress: Int)

    /**
     * 手势在左侧上下滑动改变音量后，手势up或者cancel时，隐藏控制器中间的音量变化视图，
     * 在手势ACTION_UP或ACTION_CANCEL时调用。
     */
    protected abstract fun hideChangeVolume()

    /**
     * 手势在左侧上下滑动改变亮度时，显示控制器中间的亮度变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     *
     * @param newBrightnessProgress 新的亮度进度，取值1到100。
     */
    protected abstract fun showChangeBrightness(newBrightnessProgress: Int)

    /**
     * 手势在左侧上下滑动改变亮度后，手势up或者cancel时，隐藏控制器中间的亮度变化视图，
     * 在手势ACTION_UP或ACTION_CANCEL时调用。
     */
    protected abstract fun hideChangeBrightness()

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        // 只有全屏的时候才能拖动位置、亮度、声音
        if (!videoPlayer.isFullScreen)
            return false

        // 只有在播放、暂停、缓冲的时候能够拖动改变位置、亮度和声音
        if (videoPlayer.isIdle
            || videoPlayer.isError
            || videoPlayer.isPreparing
            || videoPlayer.isPrepared
            || videoPlayer.isCompleted
        ) {
            hideChangePosition()
            hideChangeBrightness()
            hideChangeVolume()
            return false
        }

        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                _downX = x
                _downY = y
                _needChangePosition = false
                _needChangeVolume = false
                _needChangeBrightness = false
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - _downX
                var deltaY = y - _downY
                val absDeltaX = Math.abs(deltaX)
                val absDeltaY = Math.abs(deltaY)
                if (!_needChangePosition && !_needChangeVolume && !_needChangeBrightness) {
                    // 只有在播放、暂停、缓冲的时候能够拖动改变位置、亮度和声音
                    if (absDeltaX >= THRESHOLD) {
                        // 改变进度
                        cancelUpdateProgressTimer()
                        _needChangePosition = true
                        _gestureDownPosition = videoPlayer.currentPosition
                    } else if (absDeltaY >= THRESHOLD) {
                        if (_downX < (width * 0.5f)) {
                            // 左侧改变亮度
                            _needChangeBrightness = true
                            _gestureDownBrightness = XUtil.screenBrightness(this.context)
                        } else {
                            // 右侧改变声音
                            _needChangeVolume = true
                            _gestureDownVolume = videoPlayer.volume
                        }
                    }
                }

                if (_needChangePosition) {
                    val duration = videoPlayer.duration
                    val toPosition = (_gestureDownPosition + duration * deltaX / width).toLong()
                    _newPosition = Math.max(0, Math.min(duration, toPosition))
                    val newPositionProgress: Int = (100f * _newPosition / duration).toInt()
                    showChangePosition(duration, newPositionProgress)
                }
                if (_needChangeBrightness) {
                    deltaY = -deltaY
                    val deltaBrightness = deltaY * 3 / height
                    var newBrightness = _gestureDownBrightness + deltaBrightness
                    newBrightness = Math.max(0f, Math.min(newBrightness, 1f))
                    val newBrightnessPercentage = newBrightness
                    XUtil.screenBrightness(context, newBrightnessPercentage)
                    val newBrightnessProgress = (100f * newBrightnessPercentage).toInt()
                    showChangeBrightness(newBrightnessProgress)
                }
                if (_needChangeVolume) {
                    deltaY = -deltaY
                    val maxVolume: Int = videoPlayer.maxVolume
                    val deltaVolume = (maxVolume * deltaY * 3 / height).toInt()
                    var newVolume: Int = _gestureDownVolume + deltaVolume
                    newVolume = Math.max(0, Math.min(maxVolume, newVolume))
                    videoPlayer.volume = newVolume
                    val newVolumeProgress = (100f * newVolume / maxVolume).toInt()
                    showChangeVolume(newVolumeProgress)
                }
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                if (_needChangePosition) {
                    videoPlayer.seekTo(_newPosition)
                    hideChangePosition()
                    startUpdateProgressTimer()
                    return true
                }
                if (_needChangeBrightness) {
                    hideChangeBrightness()
                    return true
                }
                if (_needChangeVolume) {
                    hideChangeVolume()
                    return true
                }
            }
        }

        return false
    }
}