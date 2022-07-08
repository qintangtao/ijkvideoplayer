package me.tang.xvideoplayer.binding

import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import me.tang.xvideoplayer.XLog
import me.tang.xvideoplayer.XUtil
import me.tang.xvideoplayer.XVideoPlayer
import me.tang.xvideoplayer.controller.tx.R
import me.tang.xvideoplayer.controller.tx.TXVideoController

object XVideoPlayerAdapter {

    @JvmStatic
    @BindingAdapter(value = ["title", "length", "imageUrl", "videoUrl"], requireAll = false)
    fun setVideo(videoPlayer: XVideoPlayer, title: String?, length: Int, imageUrl: String?, videoUrl: String?) {
        //XLog.d("title:$title, length:$length, imageUrl:$imageUrl, videoUrl:$videoUrl")
        val controller = videoPlayer.videoController ?: TXVideoController(videoPlayer.context)
        title?.let {
            controller.setTitle(it)
        }
        controller.setLenght(length.toLong())
        imageUrl?.let {
            controller.imageView()?.let { it1 ->
                Glide.with(controller)
                    .load(it)
                    .placeholder(R.drawable.img_default)
                    .into(it1)
            }
        }
        videoUrl?.let {
            videoPlayer.setUp(videoUrl, null)
        }
        videoPlayer.setVideoController(controller)

        val params = videoPlayer.layoutParams
        params.width = XUtil.getScreenWidth(videoPlayer.context)
        params.height = (params.width * 9f / 16f).toInt()
        videoPlayer.layoutParams = params
    }

}