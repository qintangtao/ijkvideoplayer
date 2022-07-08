package me.tang.xvideoplayer.ui

import android.os.Bundle
import me.tang.mvvm.base.BaseActivity
import me.tang.xvideoplayer.databinding.ActivityRecyclerViewBinding

class RecyclerViewActivity : BaseActivity<RecyclerViewViewModel, ActivityRecyclerViewBinding>() {


    override fun initView(savedInstanceState: Bundle?) {
        mBinding.viewModel = viewModel
    }

    override fun initData() {
        viewModel.initData()
    }
}