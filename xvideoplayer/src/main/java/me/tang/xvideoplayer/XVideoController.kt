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
    IVideoPlayer.OnPlayStateListener, IVideoPlayer.OnWindowModeListener {

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
        _videoPlayer?.setOnWindowModeListener(this)
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
     * ??????????????????????????????
     *
     * @param title ????????????
     */
    abstract fun setTitle(title: String)

    /**
     * ????????????
     *
     * @param resId ??????????????????
     */
    abstract fun setImage(@DrawableRes resId: Int)

    /**
     * ????????????ImageView??????????????????????????????????????????????????????????????????
     *
     * @return ??????ImageView
     */
    abstract fun imageView(): ImageView?

    /**
     * ???????????????.
     */
    abstract fun setLenght(length: Long)

    /**
     * ??????????????????????????????????????????????????????
     */
    abstract fun reset()

    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????
     */
    protected abstract fun updateProgress()

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????
     * ???????????????ACTION_MOVE??????????????????????????????????????????
     *
     * @param duration            ???????????????ms
     * @param newPositionProgress ???????????????????????????0???100???
     */
    protected abstract fun showChangePosition(duration: Long, newPositionProgress: Int)

    /**
     * ????????????????????????????????????????????????up??????cancel?????????????????????????????????????????????????????????
     * ?????????ACTION_UP???ACTION_CANCEL????????????
     */
    protected abstract fun hideChangePosition()

    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????
     * ???????????????ACTION_MOVE??????????????????????????????????????????
     *
     * @param newVolumeProgress ???????????????????????????1???100???
     */
    protected abstract fun showChangeVolume(newVolumeProgress: Int)

    /**
     * ???????????????????????????????????????????????????up??????cancel???????????????????????????????????????????????????
     * ?????????ACTION_UP???ACTION_CANCEL????????????
     */
    protected abstract fun hideChangeVolume()

    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????
     * ???????????????ACTION_MOVE??????????????????????????????????????????
     *
     * @param newBrightnessProgress ???????????????????????????1???100???
     */
    protected abstract fun showChangeBrightness(newBrightnessProgress: Int)

    /**
     * ???????????????????????????????????????????????????up??????cancel???????????????????????????????????????????????????
     * ?????????ACTION_UP???ACTION_CANCEL????????????
     */
    protected abstract fun hideChangeBrightness()

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        // ?????????????????????????????????????????????????????????
        if (!videoPlayer.isFullScreen)
            return false

        // ????????????????????????????????????????????????????????????????????????????????????
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
                    // ????????????????????????????????????????????????????????????????????????????????????
                    if (absDeltaX >= THRESHOLD) {
                        // ????????????
                        cancelUpdateProgressTimer()
                        _needChangePosition = true
                        _gestureDownPosition = videoPlayer.currentPosition
                    } else if (absDeltaY >= THRESHOLD) {
                        if (_downX < (width * 0.5f)) {
                            // ??????????????????
                            _needChangeBrightness = true
                            _gestureDownBrightness = XUtil.screenBrightness(this.context)
                        } else {
                            // ??????????????????
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