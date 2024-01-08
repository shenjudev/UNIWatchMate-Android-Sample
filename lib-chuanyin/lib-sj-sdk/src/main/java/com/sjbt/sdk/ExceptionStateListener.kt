package com.sjbt.sdk

import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData

interface ExceptionStateListener {
    fun observeDisconnectState()
    fun onTimeOut(msgBean: MsgBean, nodeData: NodeData)
}