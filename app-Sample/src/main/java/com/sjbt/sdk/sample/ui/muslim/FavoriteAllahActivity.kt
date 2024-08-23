package com.sjbt.sdk.sample.ui.muslim

import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmAllah
import com.base.sdk.entity.apps.WmAllahCollect
import com.blankj.utilcode.util.LogUtils
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.base.BaseActivity
import com.sjbt.sdk.sample.data.device.SJDataConvertTools
import com.sjbt.sdk.sample.databinding.ActivityNameOfAllahBinding
import com.sjbt.sdk.sample.model.MuslimAllahInfo
import com.sjbt.sdk.sample.utils.CacheDataHelper
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.utils.log.GsonUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow

class FavoriteAllahActivity : BaseActivity() {

    private lateinit var nameOfAllahAdapter: NameOfAllahAdapter
    private var nameList: MutableList<MuslimAllahInfo> = ArrayList()
    private var favoriteList: MutableList<MuslimAllahInfo> = ArrayList()
    private val TAG = "FavoriteAllahActivity"

    private lateinit var binding: ActivityNameOfAllahBinding

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        binding = initBinding(layoutInflater)
        setContentView(binding?.root)

        nameList = CacheDataHelper.getNameOfAllahList().toMutableList()

        nameList.forEach {
            if (it.isFavorite) {
                favoriteList.add(it)
            }
        }
        nameOfAllahAdapter = NameOfAllahAdapter(this, favoriteList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = nameOfAllahAdapter

        nameOfAllahAdapter.setOnItemClickListener({ adapter, view, position ->
            favoriteList[position].isFavorite = !favoriteList[position].isFavorite
            favoriteList.removeAt(position)

            nameOfAllahAdapter.notifyDataSetChanged()

            val wmList = arrayListOf<WmAllah>()

            nameList.forEach {
                wmList.add(
                    WmAllah(
                        it.id, if (it.isFavorite) {
                            1
                        } else {
                            0
                        }
                    )
                )
            }

            val wmAllahCollect = WmAllahCollect(1, wmList)
            UNIWatchMate.wmApps.appMuslim.updateAllahList(wmAllahCollect).subscribe({ result ->
                CacheDataHelper.setNameOfAllahList(nameList)
                LogUtils.i(TAG, "设置Allah列表结果:$result")
            }, { e ->
                LogUtils.e(TAG, "设置Allah列表异常${e.message}")
            })
        })

        MyApplication.instance.applicationScope.launch {
            launchWithLog {
                UNIWatchMate.wmApps.appMuslim.observeAllahList.asFlow().collect {
                    runOnUiThread {
                        LogUtils.i(TAG, "observeAllahList:" + GsonUtil.toJson(it))
                        val muslimAllahInfos = SJDataConvertTools.instance.convertAllahList(it)
                        CacheDataHelper.setNameOfAllahList(muslimAllahInfos)
                        nameList.clear()
                        nameList.addAll(muslimAllahInfos)
                        nameOfAllahAdapter.notifyDataSetChanged()
                        updateAllahInfo()
                    }
                }
            }
        }
    }

    private fun updateAllahInfo() {

        nameList.forEach {
            CacheDataHelper.allahInfoMap[it.id] = it
        }
    }

    fun initBinding(inflater: LayoutInflater): ActivityNameOfAllahBinding {
        return ActivityNameOfAllahBinding.inflate(inflater)
    }

//    override fun distributeEvent(event: BaseEvent<*>) {
//        super.distributeEvent(event)
//
//        if (event.type == MUSLIM_ALLAH_CHANGE) {
//
//            val allahJson = DeviceCache.getNameOfAllahStr()
//
////            LogUtil.iSave(TAG, "Allah列表更新 ：$allahJson")
//            nameList.clear()
//            nameList.addAll(
//                GsonUtil.formatJson2List(
//                    allahJson,
//                    MuslimAllahInfo::class.java
//                )
//            )
//            favoriteList.clear()
//            nameList.forEach {
//                if (it.isFavorite) {
//                    favoriteList.add(it)
//                }
//            }
//            nameOfAllahAdapter.notifyDataSetChanged()
//        }
//    }


}