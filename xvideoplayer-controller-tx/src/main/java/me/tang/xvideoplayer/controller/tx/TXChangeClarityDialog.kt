package me.tang.xvideoplayer.controller.tx

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.*
import android.widget.LinearLayout
import androidx.core.view.WindowInsetsCompat
import me.tang.xvideoplayer.XUtil
import me.tang.xvideoplayer.controller.tx.databinding.ItemChangeClarityBinding

class TXChangeClarityDialog(context: Context) : Dialog(context, R.style.fullDialog) {

    private lateinit var linearLayout: LinearLayout

    private var currentIndex = 0

    private var listener: OnClarityChangedListener? = null

    init {
        init(context)
    }

    fun setOnClarityChangedListener(listener: OnClarityChangedListener) {
        this.listener = listener
    }

    fun setClarityGrade(items: List<String>, defaultIndex: Int) {
        currentIndex = defaultIndex
        val inflater = LayoutInflater.from(context)
        for (i in items.indices) {
            val binding = ItemChangeClarityBinding.inflate(inflater, linearLayout, false)
            binding.root.run {
                tag = i
                text = items.get(i)
                isSelected = i == currentIndex
                setOnClickListener { view ->
                    val checkIndex = view.tag as Int
                    listener?.let {
                        if (checkIndex != currentIndex) {
                            val childCount = linearLayout.childCount - 1
                            for (j in 0..childCount) {
                                linearLayout.getChildAt(j).isSelected = checkIndex == j
                            }
                            it.onClarityChanged(checkIndex)
                            currentIndex = checkIndex
                        } else {
                            it.onClarityNotChanged()
                        }
                    }
                    this@TXChangeClarityDialog.dismiss()
                }
            }

            val params = binding.root.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = if (i == 0) 0 else XUtil.dp2px(16f)
            linearLayout.addView(binding.root, params)
        }
    }

    override fun onBackPressed() {
        // 按返回键时回调清晰度没有变化
        listener?.onClarityNotChanged()
        super.onBackPressed()
    }

    private fun init(context: Context) {
        linearLayout = LinearLayout(context).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL
            setOnClickListener {
                listener?.onClarityNotChanged()
                this@TXChangeClarityDialog.dismiss()
            }
        }

        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.MarginLayoutParams.MATCH_PARENT)
        setContentView(linearLayout, params)

        //https://www.jianshu.com/p/9797d6448ad3
        window?.decorView?.setPadding(0,0,0,0)

        window?.attributes?.run {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            horizontalMargin = 0F
        }

        // DecorView 设置背景颜色，很重要，不然导致 Dialog 内容显示不全，有一部分内容会充当 padding
        window?.decorView?.setBackgroundColor(Color.WHITE)
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