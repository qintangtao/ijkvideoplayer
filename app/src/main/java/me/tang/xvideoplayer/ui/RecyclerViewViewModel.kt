package me.tang.xvideoplayer.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.tang.mvvm.BR
import me.tang.mvvm.base.BaseViewModel
import me.tang.xvideoplayer.R
import me.tang.xvideoplayer.bean.Video
import me.tatarka.bindingcollectionadapter2.ItemBinding

class RecyclerViewViewModel: BaseViewModel() {


    private val _items = MutableLiveData<MutableList<Video>>()
    val items: LiveData<MutableList<Video>> = _items

    val itemBinding = ItemBinding.of<Video>(BR.itemBean, R.layout.item_video)


    fun initData() {

        val datas = listOf(
                Video("v1080.mp4", 1000
                    , "http://imgsrc.baidu.com/image/c0%3Dshijue%2C0%2C0%2C245%2C40/sign=304dee3ab299a9012f38537575fc600e/91529822720e0cf3f8b77cd50046f21fbe09aa5f.jpg"
                    , "/data/local/tmp/v1080.mp4"),
                Video("wowzaec2demo", 1000
                    , "http://imgsrc.baidu.com/image/c0%3Dshijue%2C0%2C0%2C245%2C40/sign=304dee3ab299a9012f38537575fc600e/91529822720e0cf3f8b77cd50046f21fbe09aa5f.jpg"
                    , "rtsp://wowzaec2demo.streamlock.net/vod/mp4"),
                Video("192.168.1.39", 1000
                    , "http://imgsrc.baidu.com/image/c0%3Dshijue%2C0%2C0%2C245%2C40/sign=304dee3ab299a9012f38537575fc600e/91529822720e0cf3f8b77cd50046f21fbe09aa5f.jpg"
                    , "rtsp://admin:br123456789@192.168.1.39:554/avstream"),
                Video("2017-05-17_17-33-30", 1000
                    , "http://imgsrc.baidu.com/image/c0%3Dshijue%2C0%2C0%2C245%2C40/sign=304dee3ab299a9012f38537575fc600e/91529822720e0cf3f8b77cd50046f21fbe09aa5f.jpg"
                    , "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4")
            )

        _items.value = datas.toMutableList()
    }

}