package me.tang.xvideoplayer.controller.tx

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.tang.xvideoplayer.*
import me.tang.xvideoplayer.controller.tx.databinding.TxVideoPlayerControllerBinding
import java.text.SimpleDateFormat
import java.util.*

class TXVideoController : XVideoController
    , View.OnClickListener
    , SeekBar.OnSeekBarChangeListener
    , TXChangeClarityDialog.OnClarityChangedListener {

    private lateinit var binding: TxVideoPlayerControllerBinding

    private var _topBottomVisible = false

    // 是否已经注册了电池广播
    private var _hasRegisterBatteryReceiver = false

    private var _dismissTopBottomJob: Job? = null

    // 视频链接地址
    private var _clarities: List<XClarity>? = null
    private val clarities get() = _clarities!!

    private var _defaultClaritieIndex = 0

    private val _clarityDialog by lazy { TXChangeClarityDialog(this.context) }

    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        init(context)
    }

    fun setClarity(clarities: List<XClarity>, defaultClarityIndex: Int) {
        if (clarities.isEmpty()) return

        _clarities = clarities
        _defaultClaritieIndex = defaultClarityIndex

        val clarityGrades = ArrayList<String>()
        clarities.forEach {
            clarityGrades.add("${it.grade} ${it.p}")
        }

        binding.clarity.text = clarities.get(_defaultClaritieIndex).grade

        // 初始化切换清晰度对话框
        _clarityDialog.setClarityGrade(clarityGrades, defaultClarityIndex)
        _clarityDialog.setOnClarityChangedListener(this)

        // 给播放器配置视频链接地址
        _videoPlayer?.let {
            it.setUp(clarities.get(_defaultClaritieIndex).url, null)
        }
    }

    override fun setVideoPlayer(videoPlayer: IVideoPlayer) {
        super.setVideoPlayer(videoPlayer)
        if (_clarities?.size ?: 0 > 1) {
            videoPlayer.setUp(clarities.get(_defaultClaritieIndex).url, null)
        }
    }

    override fun setTitle(title: String) {
        binding.title.text = title
    }

    override fun setImage(resId: Int) {
        binding.image.setImageResource(resId)
    }

    override fun imageView(): ImageView? = binding.image

    override fun setLenght(length: Long) {
        binding.length.text = XUtil.formatTime(length)
    }

    override fun reset() {
        _topBottomVisible = false

        cancelUpdateProgressTimer()
        cancelDismissTopBottomTimer()

        binding.run {
            seek.progress = 0
            seek.secondaryProgress = 0
            centerStart.visibility = View.VISIBLE
            image.visibility = View.VISIBLE
            bottom.visibility = View.GONE
            fullScreen.setImageResource(R.drawable.ic_player_enlarge)
            length.visibility = View.VISIBLE
            top.visibility = View.VISIBLE
            back.visibility = View.GONE
            loading.visibility = View.GONE
            error.visibility = View.GONE
            completed.visibility = View.GONE
        }
    }

    override fun updateProgress() {
        val currentDuration = videoPlayer.currentPosition
        val totalDuration = videoPlayer.duration
        val progress = (100f * currentDuration / totalDuration).toInt()
        binding.run {
            seek.setProgress(progress)
            seek.secondaryProgress = videoPlayer.bufferPercentage
            position.text = XUtil.formatTime(currentDuration)
            duration.text = XUtil.formatTime(totalDuration)
            time.text = SimpleDateFormat("HH:mm", Locale.CHINA).format(Date())
        }
    }

    override fun showChangePosition(duration: Long, newPositionProgress: Int) {
        val newPosition = (duration * newPositionProgress / 100f).toLong()
        binding.run {
            changePosition.visibility = View.VISIBLE
            changePositionCurrent.text = XUtil.formatTime(newPosition)
            changePositionProgress.progress = newPositionProgress
            seek.progress = newPositionProgress
            position.text = changePositionCurrent.text
        }
    }

    override fun hideChangePosition() {
        binding.changePosition.visibility = View.GONE
    }

    override fun showChangeVolume(newVolumeProgress: Int) {
        binding.run {
            changeVolume.visibility = View.VISIBLE
            changeVolumeProgress.progress = newVolumeProgress
        }
    }

    override fun hideChangeVolume() {
        binding.changeVolume.visibility = View.GONE
    }

    override fun showChangeBrightness(newBrightnessProgress: Int) {
        binding.run {
            changeBrightness.visibility = View.VISIBLE
            changeBrightnessProgress.progress = newBrightnessProgress
        }
    }

    override fun hideChangeBrightness() {
        binding.changeBrightness.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when(v) {
            binding.centerStart -> {
                if (videoPlayer.isIdle)
                    videoPlayer.start()
            }
            binding.back -> {
                if (videoPlayer.isFullScreen)
                    videoPlayer.exitFullScreen()
                else if (videoPlayer.isTinyWindow)
                    videoPlayer.exitTinyWindow()
            }
            binding.restartOrPause -> {
                XLog.d("onClick -> restartOrPause -- state:${videoPlayer.playState}")
                if (videoPlayer.isPlaying || videoPlayer.isBufferingPlaying)
                    videoPlayer.pause()
                else if (videoPlayer.isPaused || videoPlayer.isBufferingPaused)
                    videoPlayer.restart()
            }
            binding.fullScreen -> {
                if (videoPlayer.isNormal || videoPlayer.isTinyWindow)
                    videoPlayer.enterFullScreen()
                else if (videoPlayer.isFullScreen)
                    videoPlayer.exitFullScreen()
            }
            binding.clarity -> {
                setTopBottomVisible(false)
                _clarityDialog.show()
            }
            binding.retry -> videoPlayer.restart()
            binding.replay -> binding.retry.performClick()
            this -> {
                if (videoPlayer.isPlaying
                    || videoPlayer.isPaused
                    || videoPlayer.isBufferingPlaying
                    || videoPlayer.isBufferingPaused ){
                    setTopBottomVisible(!_topBottomVisible)
                }
            }
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    }
    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }
    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if (videoPlayer.isBufferingPaused || videoPlayer.isPaused)
            videoPlayer.restart()
        val position = (videoPlayer.duration * seekBar!!.progress / 100f).toLong()
        videoPlayer.seekTo(position)
        startDismissTopBottomTimer()
    }

    override fun onPlayStateChanged(state: Int) {
        when (state) {
            XVideoPlayer.PLAY_STATE_PREPARING -> {
                binding.run {
                    image.visibility = View.GONE
                    loading.visibility = View.VISIBLE
                    error.visibility = View.GONE
                    completed.visibility = View.GONE
                    top.visibility = View.GONE
                    bottom.visibility = View.GONE
                    centerStart.visibility = View.GONE
                    length.visibility = View.GONE
                    loadText.text = "正在准备..."
                }
            }
            XVideoPlayer.PLAY_STATE_PREPARED -> {
                startUpdateProgressTimer()
            }
            XVideoPlayer.PLAY_STATE_PLAYING -> {
                startDismissTopBottomTimer()
                binding.run {
                    loading.visibility = View.GONE
                    restartOrPause.setImageResource(R.drawable.ic_player_pause)
                }
            }
            XVideoPlayer.PLAY_STATE_PAUSED -> {
                cancelDismissTopBottomTimer()
                binding.run {
                    loading.visibility = View.GONE
                    restartOrPause.setImageResource(R.drawable.ic_player_start)
                }
            }
            XVideoPlayer.PLAY_STATE_BUFFERING_PLAYING -> {
                startDismissTopBottomTimer()
                binding.run {
                    loading.visibility = View.VISIBLE
                    restartOrPause.setImageResource(R.drawable.ic_player_pause)
                    loadText.text = "正在缓冲..."
                }
            }
            XVideoPlayer.PLAY_STATE_BUFFERING_PAUSED -> {
                cancelDismissTopBottomTimer()
                binding.run {
                    loading.visibility = View.VISIBLE
                    restartOrPause.setImageResource(R.drawable.ic_player_start)
                    loadText.text = "正在缓冲..."
                }
            }
            XVideoPlayer.PLAY_STATE_ERROR -> {
                cancelUpdateProgressTimer()
                setTopBottomVisible(false)
                binding.run {
                    top.visibility = View.VISIBLE
                    error.visibility = View.VISIBLE
                }
            }
            XVideoPlayer.PLAY_STATE_COMPLETED -> {
                cancelUpdateProgressTimer()
                setTopBottomVisible(false)
                binding.run {
                    image.visibility = View.VISIBLE
                    completed.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onPlayModeChanged(mode: Int) {
        when (mode) {
            XVideoPlayer.WINDOW_MODE_NORMAL -> {
                binding.run {
                    back.visibility = View.GONE
                    fullScreen.setImageResource(R.drawable.ic_player_enlarge)
                    fullScreen.visibility = View.VISIBLE
                    clarity.visibility = View.GONE
                    batteryTime.visibility = View.GONE
                }
                if (_hasRegisterBatteryReceiver) {
                    this.context.unregisterReceiver(_batterReceiver)
                    _hasRegisterBatteryReceiver = false
                }
            }
            XVideoPlayer.WINDOW_MODE_FULLSCREEN -> {
                binding.run {
                    back.visibility = View.VISIBLE
                    fullScreen.visibility = View.GONE
                    fullScreen.setImageResource(R.drawable.ic_player_shrink)
                    batteryTime.visibility = View.VISIBLE
                    _clarities?.let {
                        if (it.size > 1)
                            clarity.visibility = View.VISIBLE
                    }
                }
                if (!_hasRegisterBatteryReceiver) {
                    this.context.registerReceiver(_batterReceiver,
                        IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    _hasRegisterBatteryReceiver = true
                }
            }
            XVideoPlayer.WINDOW_MODE_TINY -> {
                binding.run {
                    back.visibility = View.VISIBLE
                    clarity.visibility = View.GONE
                }
            }
        }
    }

    override  fun onClarityChanged(clarityIndex: Int) {
        XLog.d("onClarityChanged -> clarityIndex:$clarityIndex")
        // 根据切换后的清晰度索引值，设置对应的视频链接地址，并从当前播放位置接着播放
        val clarity = clarities.get(clarityIndex)
        binding.clarity.setText(clarity.grade)

        val currentPosition = videoPlayer.currentPosition
        videoPlayer.run {
            releasePlayer()
            setUp(clarity.url, null)
            start(currentPosition)
        }
    }

    override fun onClarityNotChanged() {
        // 清晰度没有变化，对话框消失后，需要重新显示出top、bottom
        setTopBottomVisible(true)
    }


    private fun init(context: Context) {
        binding = TxVideoPlayerControllerBinding.inflate(LayoutInflater.from(context), this, true)
        binding.centerStart.setOnClickListener(this)
        binding.back.setOnClickListener(this)
        binding.restartOrPause.setOnClickListener(this)
        binding.fullScreen.setOnClickListener(this)
        binding.clarity.setOnClickListener(this)
        binding.retry.setOnClickListener(this)
        binding.replay.setOnClickListener(this)
        binding.share.setOnClickListener(this)
        binding.seek.setOnSeekBarChangeListener(this)
        this.setOnClickListener(this)
    }

    private fun setTopBottomVisible(visible: Boolean) {
        binding.run {
            top.visibility = if (visible) View.VISIBLE else View.GONE
            bottom.visibility = top.visibility
        }
        _topBottomVisible = visible
        if (visible) {
            if (!videoPlayer.isPaused && !videoPlayer.isBufferingPaused)
                startDismissTopBottomTimer()
        } else {
            cancelDismissTopBottomTimer()
        }
    }

    /**
     * 开启top、bottom自动消失的timer
     */
    private fun startDismissTopBottomTimer() {
        cancelDismissTopBottomTimer()
        _dismissTopBottomJob = flow<Int> {
            delay(3000)
            emit(1)
        }.flowOn(Dispatchers.IO)
        .onEach {
            setTopBottomVisible(false)
        }.launchIn(mainScope)
    }

    /**
     * 取消top、bottom自动消失的timer
     */
    private fun cancelDismissTopBottomTimer() {
        _dismissTopBottomJob?.cancel()
        _dismissTopBottomJob = null
    }

    /**
     * 电池状态即电量变化广播接收器
     */
    private val _batterReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN
            )
            when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> // 充电中
                    binding.battery.setImageResource(R.drawable.battery_charging)
                BatteryManager.BATTERY_STATUS_FULL -> // 充电完成
                    binding.battery.setImageResource(R.drawable.battery_full)
                else -> {
                    val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
                    val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 0) ?: 0
                    val percentage = (level.toFloat() / scale * 100).toInt()
                    if (percentage <= 10) {
                        binding.battery.setImageResource(R.drawable.battery_10)
                    } else if (percentage <= 20) {
                        binding.battery.setImageResource(R.drawable.battery_20)
                    } else if (percentage <= 50) {
                        binding.battery.setImageResource(R.drawable.battery_50)
                    } else if (percentage <= 80) {
                        binding.battery.setImageResource(R.drawable.battery_80)
                    } else if (percentage <= 100) {
                        binding.battery.setImageResource(R.drawable.battery_100)
                    }
                }
            }
        }
    }
}