package me.tang.xvideoplayer.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import me.tang.mvvm.base.BaseActivity
import me.tang.xvideoplayer.R
import me.tang.xvideoplayer.XLog
import me.tang.xvideoplayer.XVideoPlayer
import me.tang.xvideoplayer.databinding.ActivityRecyclerViewBinding

class RecyclerViewActivity : BaseActivity<RecyclerViewViewModel, ActivityRecyclerViewBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.viewModel = viewModel

        mBinding.recyclerView.setRecyclerListener {
            XLog.d("setRecyclerListener -> ${it.itemView}")
            //val videoPlayer = it.itemView.findViewById<XVideoPlayer>(R.id.videoPlayer)
            //videoPlayer?.release()
        }

        mBinding.recyclerView.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {

            }

            override fun onChildViewDetachedFromWindow(v: View) {
                if (v is me.tang.xrecyclerview.ArrowRefreshHeader)
                    return

                XLog.d("onChildViewDetachedFromWindow -> ${v}")
                val videoPlayer = v.findViewById<XVideoPlayer>(R.id.videoPlayer)
                videoPlayer?.release()
            }

        })
    }

    override fun initData() {
        viewModel.initData()
    }
}