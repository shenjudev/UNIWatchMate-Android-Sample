package com.sjbt.sdk.sample.ui.device

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.base.api.UNIWatchMate
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentDeviceInfoBinding
import com.sjbt.sdk.sample.databinding.FragmentFeatureListBinding
import com.sjbt.sdk.sample.model.device.FeaturesModel
import com.sjbt.sdk.sample.utils.FeatureData
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch


class FeatureListFragment : BaseFragment(R.layout.fragment_feature_list) {
    private val viewBind: FragmentFeatureListBinding by viewBinding()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycle.launchRepeatOnStarted {
            launch {
               val featureList = FeatureData.getFeatureList(requireContext(), UNIWatchMate.getFunctionSupportState())
                viewBind.recyclerView.adapter = FeatureListAdapter(R.layout.item_feature,featureList)
            }
        }
    }


    class FeatureListAdapter(ids:Int,featureList:MutableList<FeaturesModel>) : BaseQuickAdapter<FeaturesModel,BaseViewHolder>(ids, featureList) {
        override fun convert(helper: BaseViewHolder, item: FeaturesModel?) {
            item?.let {
                helper.setText(R.id.tv_feature_name, it.featureName)
                helper.setText(R.id.tv_feature_id, it.id.toString())
                helper.getView<ImageView>(R.id.iv_feature_support).setImageResource(if (it.isSupport) R.mipmap.ic_menu_checked else R.mipmap.ic_close_32)
            }
        }

    }
    /**
     * 天气同步开关
     * 健身记录
     * 心率
     * 相机遥控器（V1）
     * 通知管理
     * 闹钟设置
     * 本地音乐同步
     * 联系人同步
     * 查找手表
     * 查找手机
     * 应用视图                         【设置】
     * 来电响铃                         【设置】
     * 通知触感                         【设置】
     * 表冠触感反馈                     【设置】
     * 系统触感反馈                     【设置】
     * 抬腕亮屏                        【设置】
     * 血氧
     * 血压
     * 血糖
     * 睡眠（设置+数据）
     * 电子书同步
     * 是否是慢速模式（w20a）
     * 相机遥控器支持预览
     * 视频文件同步（avi）
     * 收款码
     * 表盘市场
     * 通知列表是否全部展开
     * 通话蓝牙
     * 显示关闭通话蓝牙
     * 紧急联系人
     *  同步收藏联系人
     * 快捷回复
     * 步数目标
     * 卡路里目标
     * 活动时长目标
     * 久坐提醒
     * 喝水提醒
     * 洗手提醒
     * 心率自动检测
     * REM快速眼动
     * 是否支持多种运动
     * 显示固定运动类型
     * 运动自识别开始
     * 运动自识别结束
     * 闹钟标签
     * 闹钟备注
     * 世界时钟
     * app切换设备语言
     * 小部件
     * App调整设备音量
     * 安静心率过高提醒                  (与日常心率过高提醒互斥，支持日常心率提醒，则不细分运动心率、安静心率过高提醒)
     * 运动心率过高提醒                  (与日常心率过高提醒互斥，支持日常心率提醒，则不细分运动心率、安静心率过高提醒)
     * 日常心率过高提醒                  (与运动心率、安静心率过高提醒互斥，支持日常心率提醒，则不细分运动心率、安静心率过高提醒)
     * 连续血氧
     * 蓝牙断连提醒设置
     * 通话蓝牙与BLE是否同名
     * 事件提醒
     * 亮屏提醒
     * 重启设备
     * 导航
     * 指南针
     *
     */
}

