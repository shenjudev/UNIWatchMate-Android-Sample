package com.base.sdk.port.app

import com.base.sdk.entity.apps.WmNotification
import io.reactivex.rxjava3.core.Single

/**
 * App-notification 应用模块-通知
 */
abstract class AbAppNotification {
    /**
     * sendNotification 发送通知
     */
    abstract fun sendNotification(notification: WmNotification): Single<Boolean>
}