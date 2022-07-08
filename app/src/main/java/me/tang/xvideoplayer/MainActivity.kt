package me.tang.xvideoplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import me.tang.xvideoplayer.controller.tx.R
import me.tang.xvideoplayer.controller.tx.TXVideoController
import me.tang.xvideoplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root)

        XLog.d("MainActivity onCreate")

        val controller = TXVideoController(this)
        controller.setTitle("之心恋人")
        controller.setClarity(getClarites(), 0)
        controller.imageView().let {
            XLog.d("MainActivity Glide")
            Glide.with(this)
                .load("http://imgsrc.baidu.com/image/c0%3Dshijue%2C0%2C0%2C245%2C40/sign=304dee3ab299a9012f38537575fc600e/91529822720e0cf3f8b77cd50046f21fbe09aa5f.jpg")
                .placeholder(R.drawable.img_default)
                .into(it)
        }

        binding.xVideoPlayer.run {
            setMediaType(XVideoPlayer.MEDIA_TYPE_NATIVE)
            setVideoController(controller)
            //setUp("rtsp://wowzaec2demo.streamlock.net/vod/mp4", null)
            //setUp("rtsp://admin:br123456789@192.168.1.39:554/avstream", null)
            //setUp("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4", null)
            //setUp("/data/local/tmp/v1080.mp4", null)
        }


        binding.btnMediaAndroid.setOnClickListener {
            binding.xVideoPlayer.setMediaType(XVideoPlayer.MEDIA_TYPE_NATIVE)
        }
        binding.btnMediaFFmpeg.setOnClickListener {
            binding.xVideoPlayer.setMediaType(XVideoPlayer.MEDIA_TYPE_IJK)
        }
        binding.btnFillParent.setOnClickListener {
            binding.xVideoPlayer.setDisplayType(XVideoPlayer.DISPLAY_TYPE_FILL_PARENT)
        }
        binding.btnFillCrop.setOnClickListener {
            binding.xVideoPlayer.setDisplayType(XVideoPlayer.DISPLAY_TYPE_FILL_SCROP)
        }
        binding.btnOriginal.setOnClickListener {
            binding.xVideoPlayer.setDisplayType(XVideoPlayer.DISPLAY_TYPE_ORIGINAL)
        }
        binding.btnAdapter.setOnClickListener {
            binding.xVideoPlayer.setDisplayType(XVideoPlayer.DISPLAY_TYPE_ADAPTER)
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

    private fun getClarites() : List<XClarity> {
        val clarities = ArrayList<XClarity>()
        clarities.add(
            XClarity(
                "标清",
                "270P",
                "http://play.g3proxy.lecloud.com/vod/v2/MjUxLzE2LzgvbGV0di11dHMvMTQvdmVyXzAwXzIyLTExMDc2NDEzODctYXZjLTE5OTgxOS1hYWMtNDgwMDAtNTI2MTEwLTE3MDg3NjEzLWY1OGY2YzM1NjkwZTA2ZGFmYjg2MTVlYzc5MjEyZjU4LTE0OTg1NTc2ODY4MjMubXA0?b=259&mmsid=65565355&tm=1499247143&key=f0eadb4f30c404d49ff8ebad673d3742&platid=3&splatid=345&playid=0&tss=no&vtype=21&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super"
            )
        )
        clarities.add(
            XClarity(
                "高清",
                "480P",
                "http://play.g3proxy.lecloud.com/vod/v2/MjQ5LzM3LzIwL2xldHYtdXRzLzE0L3Zlcl8wMF8yMi0xMTA3NjQxMzkwLWF2Yy00MTk4MTAtYWFjLTQ4MDAwLTUyNjExMC0zMTU1NTY1Mi00ZmJjYzFkNzA1NWMyNDc4MDc5OTYxODg1N2RjNzEwMi0xNDk4NTU3OTYxNzQ4Lm1wNA==?b=479&mmsid=65565355&tm=1499247143&key=98c7e781f1145aba07cb0d6ec06f6c12&platid=3&splatid=345&playid=0&tss=no&vtype=13&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super"
            )
        )
        clarities.add(
            XClarity(
                "超清",
                "720P",
                "http://play.g3proxy.lecloud.com/vod/v2/MjQ5LzM3LzIwL2xldHYtdXRzLzE0L3Zlcl8wMF8yMi0xMTA3NjQxMzkwLWF2Yy00MTk4MTAtYWFjLTQ4MDAwLTUyNjExMC0zMTU1NTY1Mi00ZmJjYzFkNzA1NWMyNDc4MDc5OTYxODg1N2RjNzEwMi0xNDk4NTU3OTYxNzQ4Lm1wNA==?b=479&mmsid=65565355&tm=1499247143&key=98c7e781f1145aba07cb0d6ec06f6c12&platid=3&splatid=345&playid=0&tss=no&vtype=13&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super"
            )
        )
        clarities.add(
            XClarity(
                "蓝光",
                "1080P",
                "http://play.g3proxy.lecloud.com/vod/v2/MjQ5LzM3LzIwL2xldHYtdXRzLzE0L3Zlcl8wMF8yMi0xMTA3NjQxMzkwLWF2Yy00MTk4MTAtYWFjLTQ4MDAwLTUyNjExMC0zMTU1NTY1Mi00ZmJjYzFkNzA1NWMyNDc4MDc5OTYxODg1N2RjNzEwMi0xNDk4NTU3OTYxNzQ4Lm1wNA==?b=479&mmsid=65565355&tm=1499247143&key=98c7e781f1145aba07cb0d6ec06f6c12&platid=3&splatid=345&playid=0&tss=no&vtype=13&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super"
            )
        )

        return clarities
    }
}