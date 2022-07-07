package me.tang.xvideoplayer

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.net.Uri
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import tv.danmaku.ijk.media.player.AndroidMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.IOException

class XVideoPlayer : FrameLayout
    , IVideoPlayer
    , TextureView.SurfaceTextureListener
    , IMediaPlayer.OnPreparedListener
    , IMediaPlayer.OnVideoSizeChangedListener
    , IMediaPlayer.OnErrorListener
    , IMediaPlayer.OnCompletionListener
    , IMediaPlayer.OnBufferingUpdateListener
    , IMediaPlayer.OnInfoListener{

    companion object {
        const val WINDOW_MODE_NORMAL = 10
        const val WINDOW_MODE_FULLSCREEN = 11
        const val WINDOW_MODE_TINY = 12

        const val DISPLAY_TYPE_ADAPTER = 0
        const val DISPLAY_TYPE_FILL_PARENT = 1
        const val DISPLAY_TYPE_FILL_SCROP = 2
        const val DISPLAY_TYPE_ORIGINAL = 3

        /**
         * IjkPlayer
         */
        val TYPE_IJK = 111

        /**
         * MediaPlayer
         */
        val TYPE_NATIVE = 222
    }

    //view
    private lateinit var container: FrameLayout

    private var _textureView: XTextureView? = null
    private val textureView get() = _textureView!!

    private var _videoController: XVideoController? = null
    private val videoController get() = _videoController!!

    // video and audio
    private var _mediaPlayer: IMediaPlayer? = null
    private val mediaPlayer get() = _mediaPlayer!!

    private var _audioManager: AudioManager? = null
    private val audioManager get() = _audioManager!!

    // surface
    private var _surfaceTexture: SurfaceTexture? = null
    private val surfaceTexture get() = _surfaceTexture!!

    private var _surface: Surface? = null
    private val surface get() = _surface!!

    //data
    private var mUrl: String? = null
    private var mHeaders: Map<String, String>? = null

    private var skipToPosition: Long = 0

    private var continueFromLastPosition = true

    private var bufferPercentage = 0


    //
    private var _displayType = DISPLAY_TYPE_ADAPTER
    val displayType get() = _displayType

    private var mOnPlayModeListener: IVideoPlayer.OnPlayModeListener? = null
    private var _windowMode = WINDOW_MODE_NORMAL
        set(value) {
            if (field != value) {
                field = value
                mOnPlayModeListener?.onPlayModeChanged(field)
            }
        }
    override val windowMode get() = _windowMode

    private var mOnPlayStateListener: IVideoPlayer.OnPlayStateListener? = null
    private var _playState: Int = IVideoPlayer.PLAY_STATE_IDLE
            set(value) {
                  if (field != value) {
                      field = value
                      mOnPlayStateListener?.onPlayStateChanged(field)
                  }
            }
    override val playState get() = _playState


    private var _playerType = TYPE_IJK
    override val playType get() = _playerType


    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    fun setDisplayType(type: Int) {
        if (displayType != type) {
            _displayType = type
            _textureView?.setDisplayType(type)
        }
    }

    fun setPlayerType(type: Int) {
        _playerType = type
    }

    fun setVideoController(controller2: XVideoController) {
        _videoController?.let {
            container.removeView(it)
        }
        _videoController = controller2
        _videoController?.let {
            it.setVideoPlayer(this)
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
            container.addView(it, params)
        }
    }

    override fun setUp(url: String, headers: Map<String, String>?) {
        mUrl = url
        mHeaders = headers
    }

    override fun start() {
        if (_textureView == null) {
            initAudioManager()
            initMediaPlayer()
            initTextureView()
            addTextureView()
        }
    }

    override fun start(position: Long) {
        skipToPosition = position
        start()
    }

    override fun restart() {
        XLog.d("restart -> state:${playState}")

        when (playState) {
            IVideoPlayer.PLAY_STATE_PAUSED -> {
                mediaPlayer.start()
                _playState = IVideoPlayer.PLAY_STATE_PLAYING
            }
            IVideoPlayer.PLAY_STATE_BUFFERING_PAUSED -> {
                mediaPlayer.start()
                _playState = IVideoPlayer.PLAY_STATE_BUFFERING_PLAYING
            }
            IVideoPlayer.PLAY_STATE_COMPLETED,
            IVideoPlayer.PLAY_STATE_ERROR -> {
                mediaPlayer.reset()
                openMediaPlayer()
            }
            else -> {
                XLog.d("NiceVideoPlayer在mCurrentState == " + playState + "时不能调用restart()方法.");
            }
        }
    }

    override fun pause() {
        when (playState) {
            IVideoPlayer.PLAY_STATE_PLAYING -> {
                mediaPlayer.pause()
                _playState = IVideoPlayer.PLAY_STATE_PAUSED
            }
            IVideoPlayer.PLAY_STATE_BUFFERING_PLAYING -> {
                mediaPlayer.pause()
                _playState = IVideoPlayer.PLAY_STATE_BUFFERING_PAUSED
            }
        }
    }

    override fun seekTo(pos: Long) {
        _mediaPlayer?.seekTo(pos)
    }

    override fun setVolume(volume: Int) {
        _audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
    }

    override fun setSpeed(speed: Float) {
        if (mediaPlayer is IjkMediaPlayer) {
            (mediaPlayer as IjkMediaPlayer).setSpeed(speed)
        }
    }

    override fun continueFromLastPosition(continueFromLastPosition: Boolean) {
        this.continueFromLastPosition = continueFromLastPosition
    }

    override fun isIdle(): Boolean =
        playState == IVideoPlayer.PLAY_STATE_IDLE

    override fun isPreparing(): Boolean =
        playState == IVideoPlayer.PLAY_STATE_PREPARING

    override fun isPrepared(): Boolean =
        playState == IVideoPlayer.PLAY_STATE_PREPARED

    override fun isBufferingPlaying(): Boolean =
        playState == IVideoPlayer.PLAY_STATE_BUFFERING_PLAYING

    override fun isBufferingPaused(): Boolean =
        playState == IVideoPlayer.PLAY_STATE_BUFFERING_PAUSED

    override fun isPlaying(): Boolean =
        playState == IVideoPlayer.PLAY_STATE_PLAYING

    override fun isPaused(): Boolean =
        playState == IVideoPlayer.PLAY_STATE_PAUSED

    override fun isError(): Boolean =
        playState == IVideoPlayer.PLAY_STATE_ERROR

    override fun isCompleted(): Boolean =
        playState == IVideoPlayer.PLAY_STATE_COMPLETED

    override fun isFullScreen(): Boolean =
        windowMode == WINDOW_MODE_FULLSCREEN

    override fun isTinyWindow(): Boolean =
        windowMode == WINDOW_MODE_TINY

    override fun isNormal(): Boolean =
        windowMode == WINDOW_MODE_NORMAL

    override fun getMaxVolume(): Int {
        return _audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 0
    }

    override fun getVolume(): Int {
        return _audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
    }

    override fun getDuration(): Long {
        return _mediaPlayer?.duration ?: 0
    }

    override fun getCurrentPosition(): Long {
        return _mediaPlayer?.currentPosition ?: 0
    }

    override fun getBufferPercentage(): Int = bufferPercentage

    override fun getSpeed(speed: Float): Float {
        if (mediaPlayer is IjkMediaPlayer) {
            return (mediaPlayer as IjkMediaPlayer).getSpeed(speed)
        }
        return 0f
    }

    override fun getTcpSpeed(): Long {
        if (mediaPlayer is IjkMediaPlayer) {
            return (mediaPlayer as IjkMediaPlayer).getTcpSpeed()
        }
        return 0
    }

    override fun enterFullScreen() {
        if (windowMode == WINDOW_MODE_FULLSCREEN)
            return

        XUtil.hideActionBar(this.context)
        XUtil.setOrientation(this.context, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

        val contentView = XUtil.scanForActivity(this.context)?.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        check(contentView != null) { "ID_ANDROID_CONTENT not found" }

        if (windowMode == WINDOW_MODE_TINY)
            contentView.removeView(container)
        else
            removeView(container)

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        contentView.addView(container, params)

        _windowMode = WINDOW_MODE_FULLSCREEN
    }

    override fun exitFullScreen(): Boolean{
        if (windowMode != WINDOW_MODE_FULLSCREEN)
            return false

        XUtil.showActionBar(this.context)
        XUtil.setOrientation(this.context, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        val contentView = XUtil.scanForActivity(this.context)?.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        check(contentView != null) { "ID_ANDROID_CONTENT not found" }

        contentView.removeView(container)

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        addView(container, params)

        _windowMode = WINDOW_MODE_NORMAL
        return true
    }

    override fun enterTinyWindow() {
        if (windowMode == WINDOW_MODE_TINY)
            return

        removeView(container)

        val contentView = XUtil.scanForActivity(this.context)?.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        check(contentView != null) { "ID_ANDROID_CONTENT not found" }

        val params = FrameLayout.LayoutParams(
            (ScreenUtils.getScreenWidth() * 0.5f).toInt(),
            (ScreenUtils.getScreenWidth() * 0.5f * 9f / 16f).toInt()).apply {
                gravity = Gravity.BOTTOM or Gravity.END
            rightMargin = SizeUtils.dp2px(14f)
            bottomMargin = SizeUtils.dp2px(14f)
        }
        contentView.addView(container, params)

        _windowMode = WINDOW_MODE_TINY
    }

    override fun exitTinyWindow(): Boolean{
        if (windowMode != WINDOW_MODE_TINY)
            return false

        val contentView = XUtil.scanForActivity(this.context)?.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        check(contentView != null) { "ID_ANDROID_CONTENT not found" }

        contentView.removeView(container)

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        addView(container, params)

        _windowMode = WINDOW_MODE_NORMAL
        return true
    }

    override fun releasePlayer() {
        _audioManager?.run {
            abandonAudioFocus(null)
            _audioManager = null
        }
        _mediaPlayer?.run {
            release()
            _mediaPlayer = null
        }
        _textureView?.let {
            container.removeView(it)
        }
        _surface?.run {
            release()
            _surface = null
        }
        _surfaceTexture?.run {
            release()
            _surfaceTexture = null
        }
        _playState = IVideoPlayer.PLAY_STATE_IDLE
    }

    override fun release() {
        // 保存播放位置

        // 退出全屏或小窗口
        if (isFullScreen())
            exitFullScreen()

        if (isTinyWindow())
            exitTinyWindow()

        _windowMode = WINDOW_MODE_NORMAL

        // 释放播放器
        releasePlayer()

        // 恢复控制器
        _videoController?.reset()

        Runtime.getRuntime().gc()
    }

    override fun setOnPlayStateListener(listener: IVideoPlayer.OnPlayStateListener?) {
        mOnPlayStateListener = listener
    }

    override fun setOnPlayModeListener(listener: IVideoPlayer.OnPlayModeListener?) {
        mOnPlayModeListener = listener
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        container = FrameLayout(context).apply {
            setBackgroundColor(Color.BLACK)
        }
        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        addView(container, params)
    }

    private fun initAudioManager() {
        _audioManager ?: context.getSystemService(Context.AUDIO_SERVICE).also {
            _audioManager = it as AudioManager?
            _audioManager?.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
    }

    private fun initMediaPlayer() {
       if (_mediaPlayer == null) {
           when(playType) {
               TYPE_NATIVE -> {
                   _mediaPlayer = AndroidMediaPlayer()
               }
               else -> {
                   _mediaPlayer = IjkMediaPlayer()
               }
           }
           mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
       }
    }

    private fun initTextureView() {
        _textureView ?: XTextureView(this.context).also {
            it.surfaceTextureListener = this
            it.setDisplayType(displayType)
            _textureView = it
        }
    }

    private fun addTextureView() {
        container.removeView(textureView)
        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.CENTER)
        container.addView(textureView, 0, params)
    }

    private fun openMediaPlayer() {

        _surface ?: Surface(surfaceTexture).also {
            _surface = it
        }

        // 屏幕常亮
        container.keepScreenOn = true
        //设置监听
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnVideoSizeChangedListener(this)
        mediaPlayer.setOnErrorListener(this)
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setOnBufferingUpdateListener(this)
        mediaPlayer.setOnInfoListener(this)

        try {
            mediaPlayer.setDataSource(this.context.applicationContext, Uri.parse(mUrl), mHeaders)
            mediaPlayer.setSurface(surface)
            mediaPlayer.prepareAsync()
            _playState = IVideoPlayer.PLAY_STATE_PREPARING
            XLog.d( "openMediaPlayer -> STATE_PREPARING")
        } catch (e: IOException) {
            e.printStackTrace()
            XLog.e( "openMediaPlayer", e)
        }
    }

    override fun onSurfaceTextureAvailable(surface2: SurfaceTexture, width: Int, height: Int) {
        if (_surfaceTexture == null) {
            _surfaceTexture = surface2
            openMediaPlayer()
        } else {
            textureView.setSurfaceTexture(surfaceTexture)
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return _surfaceTexture == null
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    override fun onPrepared(mp: IMediaPlayer) {
        XLog.d( "onPrepared -> STATE_PREPARED")
        _playState = IVideoPlayer.PLAY_STATE_PREPARED
        mp.start()
        // 跳到指定位置播放
        if (skipToPosition != 0L) {
            mp.seekTo(skipToPosition)
        }
    }

    override fun onVideoSizeChanged(mp: IMediaPlayer, width: Int, height: Int, sar_num: Int, sar_den: Int) {
        XLog.d( "onVideoSizeChanged -> width:$width, height:$height, sar_num:$sar_num, sar_den:$sar_den")
        textureView.setVideoSize(width, height)
    }

    override fun onError(mp: IMediaPlayer, what: Int, extra: Int): Boolean {
        // 直播流播放时去调用mediaPlayer.getDuration会导致-38和-2147483648错误，忽略该错误
        if (what != -38 && what != -2147483648 && extra != -38 && extra != -2147483648) {
            _playState = IVideoPlayer.PLAY_STATE_ERROR
            XLog.d("onError -> STATE_ERROR -- what:$what, extra:$extra")
        }
        return true
    }

    override fun onCompletion(mp: IMediaPlayer) {
        XLog.d("onCompletion -> STATE_COMPLETED")
        _playState = IVideoPlayer.PLAY_STATE_COMPLETED
        // 清除屏幕常亮
        container.setKeepScreenOn(false)
    }

    override fun onBufferingUpdate( mp: IMediaPlayer, percent: Int) {
        bufferPercentage = percent
    }

    override fun onInfo(mp: IMediaPlayer, what: Int, extra: Int): Boolean {
        when (what) {
            IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                // 播放器开始渲染
                _playState = IVideoPlayer.PLAY_STATE_PLAYING
                XLog.d("onInfo ——> MEDIA_INFO_VIDEO_RENDERING_START：STATE_PLAYING");
            }
            IMediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                // MediaPlayer暂时不播放，以缓冲更多的数据
                when (playState) {
                    IVideoPlayer.PLAY_STATE_PAUSED,
                    IVideoPlayer.PLAY_STATE_BUFFERING_PAUSED -> {
                        _playState = IVideoPlayer.PLAY_STATE_BUFFERING_PAUSED
                        XLog.d("onInfo ——> MEDIA_INFO_BUFFERING_START：STATE_BUFFERING_PAUSED");
                    }
                    else -> {
                        _playState = IVideoPlayer.PLAY_STATE_BUFFERING_PLAYING
                        XLog.d("onInfo ——> MEDIA_INFO_BUFFERING_START：STATE_BUFFERING_PLAYING");
                    }
                }
            }
            IMediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                // 填充缓冲区后，MediaPlayer恢复播放/暂停
                when(playState) {
                    IVideoPlayer.PLAY_STATE_BUFFERING_PLAYING -> {
                        _playState = IVideoPlayer.PLAY_STATE_PLAYING
                        XLog.d("onInfo ——> MEDIA_INFO_BUFFERING_END：STATE_PLAYING");
                    }
                    IVideoPlayer.PLAY_STATE_BUFFERING_PAUSED -> {
                        _playState = IVideoPlayer.PLAY_STATE_PAUSED
                        XLog.d("onInfo ——> MEDIA_INFO_BUFFERING_END：STATE_PAUSED");
                    }
                }
            }
            IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED -> {
                _textureView?.rotation = extra.toFloat()
                XLog.d("onInfo ——> MEDIA_INFO_VIDEO_ROTATION_CHANGED：$extra");
            }
            IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE -> {
                XLog.d("onInfo ——> MEDIA_INFO_NOT_SEEKABLE");
            }
            else -> {
                XLog.d("onInfo ——> what:$what");
            }
        }

        return true
    }


}