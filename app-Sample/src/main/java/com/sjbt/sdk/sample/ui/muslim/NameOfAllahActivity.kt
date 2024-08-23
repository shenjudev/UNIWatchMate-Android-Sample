package com.sjbt.sdk.sample.ui.muslim

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.text.TextUtils
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
import com.sjbt.sdk.sample.utils.AssetUtils
import com.sjbt.sdk.sample.utils.CacheDataHelper
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.utils.log.GsonUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow

class NameOfAllahActivity : BaseActivity() {

    private lateinit var nameOfAllahAdapter: NameOfAllahAdapter
    private var nameList: MutableList<MuslimAllahInfo> = ArrayList()
    private val TAG = "NameOfAllahActivity"
    private lateinit var binding: ActivityNameOfAllahBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNameOfAllahBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UNIWatchMate.wmApps.appMuslim.getAllAhList.subscribe({
            LogUtils.i(TAG, "获取Allah列表：" + GsonUtil.toJson(it))
            nameList.clear()
            nameList.addAll(SJDataConvertTools.instance.convertAllahList(it))

            updateAllahList(nameList)
        }, {
            LogUtils.i(TAG, "获取Allah列表异常：$it")
        })

        updateAllahInfo()

        nameOfAllahAdapter = NameOfAllahAdapter(this, nameList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = nameOfAllahAdapter

        nameOfAllahAdapter.setOnItemClickListener { adapter, view, it ->
//            if (!isBleOpen || !isDeviceConnected) {
//                nameOfAllahAdapter.notifyDataSetChanged()
//                return@setFavoriteListener
//            }

            if (nameList.isNotEmpty() && it >= 0 && it < nameList.size) {
                nameList[it].isFavorite = !nameList[it].isFavorite
                nameOfAllahAdapter.notifyItemChanged(it)
                CacheDataHelper.setNameOfAllahList(nameList)

                LogUtils.i(TAG, "设置Allah列表：" + GsonUtil.toJson(nameList))

                val wmAllahList = ArrayList<WmAllah>()
                nameList.forEach {
                    wmAllahList.add(
                        WmAllah(
                            it.id, if (it.isFavorite) {
                                1
                            } else {
                                0
                            }
                        )
                    )
                }

                val wmAllahCollect = WmAllahCollect(1, wmAllahList)
                UNIWatchMate.wmApps.appMuslim.updateAllahList(wmAllahCollect)
            }
        }


        MyApplication.instance.applicationScope.launch {
            launchWithLog {
                UNIWatchMate.wmApps.appMuslim.observeAllahList.asFlow().collect {
                    runOnUiThread {
                        LogUtils.i("observeAllahList: ${GsonUtil.toJson(it)}")
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

    private fun updateAllahList(it: MutableList<MuslimAllahInfo>) {
        nameList.clear()
        if (it == null || it.isEmpty()) {
            nameList.addAll(
                CacheDataHelper.getNameOfAllahList()
            )

        } else {
            nameList.addAll(it)
            CacheDataHelper.setNameOfAllahList(nameList)
        }

        nameOfAllahAdapter.notifyDataSetChanged()
    }

    fun onRightClicked() {
//        super.onRightClicked()
        val intent = Intent(this, FavoriteAllahActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if (nameList == null) {
            nameList = ArrayList()
        }
        nameList.clear()
        if (CacheDataHelper.getNameOfAllahList().isNotEmpty()) {
            nameList.addAll(
                CacheDataHelper.getNameOfAllahList()
            )
            nameOfAllahAdapter.notifyDataSetChanged()
            updateAllahInfo()
        } else {

            val nameJson = AssetUtils.getJSONFromAssetRoot(
                this,
                "nameOfAllah.json"
            )

            if (!TextUtils.isEmpty(nameJson)) {
                nameList.addAll(
                    GsonUtil.formatJson2List(
                        nameJson,
                        MuslimAllahInfo::class.java
                    )
                )
                nameOfAllahAdapter.notifyDataSetChanged()
                updateAllahInfo()
                CacheDataHelper.setNameOfAllahList(nameList)
            }
        }
    }

    private fun updateAllahInfo() {

        nameList.forEach {
            CacheDataHelper.allahInfoMap[it.id] = it
        }
    }

}