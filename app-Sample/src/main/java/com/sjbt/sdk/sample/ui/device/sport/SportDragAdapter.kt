package com.sjbt.sdk.sample.ui.device.sport

import com.base.sdk.entity.apps.WmSport
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sjbt.sdk.sample.R

/**
 * Created by luoxw on 2016/6/20.
 */
class SportDragAdapter(data: List<WmSport>?, val viewModel: SportInstalledViewModel) :
    BaseItemDraggableAdapter<WmSport?, BaseViewHolder?>(R.layout.item_sport_installed, data) {
    override fun convert(helper: BaseViewHolder, item: WmSport?) {
        item?.let {
            val name = viewModel.getNameById(it.id)
            helper.setText(R.id.tv_sport_id, "${it.id} $name")
            helper.setVisible(R.id.img_delete, false)
            helper.setVisible(R.id.tv_dial_built_in, false)
            helper.setVisible(R.id.iv_drag, true)
        }

    }

}