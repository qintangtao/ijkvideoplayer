package me.tang.xvideoplayer.ui

import android.os.Bundle
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
            val videoPlayer = it.itemView.findViewById<XVideoPlayer>(R.id.videoPlayer)
            videoPlayer?.release()
        }
    }

    override fun initData() {
        viewModel.initData()
    }
}