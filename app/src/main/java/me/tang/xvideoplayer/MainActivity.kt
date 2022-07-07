package me.tang.xvideoplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import me.tang.xvideoplayer.controller.tx.TXVideoController
import me.tang.xvideoplayer.databinding.ActivityMainBinding
import tv.danmaku.ijk.media.player.AndroidMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root)

        XLog.d("MainActivity onCreate")

        val controller = TXVideoController(this)
        controller.setTitle("之心恋人")

        binding.xVideoPlayer.run {
            setPlayerType(XVideoPlayer.TYPE_IJK)
            setVideoController(controller)
            //setUp("rtsp://wowzaec2demo.streamlock.net/vod/mp4", null)
            //setUp("rtsp://admin:br123456789@192.168.1.39:554/avstream", null)
            //setUp("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4", null)
            setUp("/data/local/tmp/v1080.mp4", null)
        }

        binding.btnFillParent.setOnClickListener {
            binding.xVideoPlayer.setDisplayType(XTextureView.DISPLAY_TYPE_FILL_PARENT)
        }
        binding.btnFillCrop.setOnClickListener {
            binding.xVideoPlayer.setDisplayType(XTextureView.DISPLAY_TYPE_FILL_SCROP)
        }
        binding.btnOriginal.setOnClickListener {
            binding.xVideoPlayer.setDisplayType(XTextureView.DISPLAY_TYPE_ORIGINAL)
        }
        binding.btnAdapter.setOnClickListener {
            binding.xVideoPlayer.setDisplayType(XTextureView.DISPLAY_TYPE_ADAPTER)
        }
    }

    override fun onBackPressed() {
        XLog.d("MainActivity onBackPressed")
        if (binding.xVideoPlayer.exitFullScreen())
            return
        if (binding.xVideoPlayer.exitTinyWindow())
            return
        super.onBackPressed()
    }
}