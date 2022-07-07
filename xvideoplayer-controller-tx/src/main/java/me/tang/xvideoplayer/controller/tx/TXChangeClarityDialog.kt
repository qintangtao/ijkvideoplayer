package me.tang.xvideoplayer.controller.tx

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import me.tang.xvideoplayer.XUtil
import me.tang.xvideoplayer.controller.tx.databinding.ItemChangeClarityBinding

class TXChangeClarityDialog : Dialog {

    private lateinit var linearLayout: LinearLayout
    private var currentIndex = 0

    private var _listener: OnClarityChangedListener? = null

    constructor(context: Context): super(context) {
        init(context)
    }

    fun setOnClarityChangedListener(listener: OnClarityChangedListener) {
        _listener = listener
    }

    fun setClarityGrade(items: List<String>, defaultChecked: Int) {
        currentIndex = defaultChecked
        val inflater = LayoutInflater.from(context)
        for (i in items.indices) {
            val binding = ItemChangeClarityBinding.inflate(inflater, linearLayout, false)
            binding.itemView.run {
                setTag(i)
                setOnClickListener {
                    val checkIndex: Int = it?.tag as? Int ?: 0
                    _listener?.let {
                        if (checkIndex != currentIndex) {

                            it.onClarityChanged(checkIndex)
                            currentIndex = checkIndex
                        } else {
                            it.onClarityNotChanged()
                        }
                    }
                    this@TXChangeClarityDialog.dismiss()
                }
                setText(items.get(i))
                isSelected = i == defaultChecked
            }

            val params = binding.itemView.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = if (i == 0) 0 else SizeUtils.dp2px(16f)
            linearLayout.addView(binding.root, params)
        }
    }


    override fun onBackPressed() {
        // 按返回键时回调清晰度没有变化
        _listener?.onClarityNotChanged()
        super.onBackPressed()
    }


    private fun init(context: Context) {
        linearLayout = LinearLayout(context).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL
            setOnClickListener {
                _listener?.onClarityNotChanged()
                this@TXChangeClarityDialog.dismiss()
            }
        }

        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.MarginLayoutParams.MATCH_PARENT)
        setContentView(linearLayout, params)

        val windowParams = window?.attributes
        windowParams?.run {
            width = ScreenUtils.getScreenHeight()
            height = ScreenUtils.getScreenWidth()
        }
        window?.attributes = windowParams
    }

    interface OnClarityChangedListener {
        /**
         * 切换清晰度后回调
         *
         * @param clarityIndex 切换到的清晰度的索引值
         */
        fun onClarityChanged(index: Int)
        /**
         * 清晰度没有切换，比如点击了空白位置，或者点击的是之前的清晰度
         */
        fun onClarityNotChanged()
    }
}