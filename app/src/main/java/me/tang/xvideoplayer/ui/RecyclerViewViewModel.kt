package me.tang.xvideoplayer.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.tang.mvvm.BR
import me.tang.mvvm.base.BaseViewModel
import me.tang.xvideoplayer.R
import me.tang.xvideoplayer.bean.Video
import me.tatarka.bindingcollectionadapter2.ItemBinding

class RecyclerViewViewModel : BaseViewModel() {


    private val _items = MutableLiveData<MutableList<Video>>()
    val items: LiveData<MutableList<Video>> = _items

    val itemBinding = ItemBinding.of<Video>(BR.itemBean, R.layout.item_video)


    fun initData() {

        val datas = listOf(
            Video(
                "办公室小野开番外了，居然在办公室开澡堂！老板还点赞？",
                98000,
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-30-43.jpg",
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4"
            ),
            Video(
                "小野在办公室用丝袜做茶叶蛋 边上班边看《外科风云》",
                413000,
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-10_10-09-58.jpg",
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-10_10-20-26.mp4"
            ),
            Video(
                "花盆叫花鸡，怀念玩泥巴，过家家，捡根竹竿当打狗棒的小时候",
                439000,
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-03_12-52-08.jpg",
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-03_13-02-41.mp4"
            ),
            Video(
                "针织方便面，这可能是史上最不方便的方便面",
                178000,
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-28_18-18-22.jpg",
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-28_18-20-56.mp4"
            ),
            Video(
                "小野的下午茶，办公室不只有KPI，也有诗和远方",
                450000,
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-26_10-00-28.jpg",
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-26_10-06-25.mp4"
            ),
            Video(
                "可乐爆米花，嘭嘭嘭......收花的人说要把我娶回家",
                176000,
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-21_16-37-16.jpg",
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-21_16-41-07.mp4"
            ),
            Video(
                "可乐爆米花，嘭嘭嘭......收花的人说要把我娶回家",
                176000,
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-21_16-37-16.jpg",
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-21_16-41-07.mp4"
            ),
            Video(
                "可乐爆米花，嘭嘭嘭......收花的人说要把我娶回家",
                176000,
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-21_16-37-16.jpg",
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-21_16-41-07.mp4"
            ),
            Video(
                "可乐爆米花，嘭嘭嘭......收花的人说要把我娶回家",
                176000,
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-21_16-37-16.jpg",
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-21_16-41-07.mp4"
            ),
            Video(
                "可乐爆米花，嘭嘭嘭......收花的人说要把我娶回家",
                176000,
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-21_16-37-16.jpg",
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-21_16-41-07.mp4"
            ),
            Video(
                "v1080.mp4",
                1000,
                "http://imgsrc.baidu.com/image/c0%3Dshijue%2C0%2C0%2C245%2C40/sign=304dee3ab299a9012f38537575fc600e/91529822720e0cf3f8b77cd50046f21fbe09aa5f.jpg",
                "/data/local/tmp/v1080.mp4"
            ),
            Video(
                "wowzaec2demo",
                1000,
                "http://imgsrc.baidu.com/image/c0%3Dshijue%2C0%2C0%2C245%2C40/sign=304dee3ab299a9012f38537575fc600e/91529822720e0cf3f8b77cd50046f21fbe09aa5f.jpg",
                "rtsp://wowzaec2demo.streamlock.net/vod/mp4"
            ),
            Video(
                "192.168.1.39",
                1000,
                "http://imgsrc.baidu.com/image/c0%3Dshijue%2C0%2C0%2C245%2C40/sign=304dee3ab299a9012f38537575fc600e/91529822720e0cf3f8b77cd50046f21fbe09aa5f.jpg",
                "rtsp://admin:br123456789@192.168.1.39:554/avstream"
            )
        )

        _items.value = datas.toMutableList()
    }

}