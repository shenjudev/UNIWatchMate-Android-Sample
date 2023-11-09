package com.base.sdk.port.app

import com.base.sdk.entity.apps.WmContact
import com.base.sdk.entity.settings.WmEmergencyCall
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * 应用模块-通讯录
 */
abstract class AbAppContact {

    /**
     * 从设备端获取通讯录列表
     */
    abstract val getContactList: Observable<List<WmContact>>

    /**
     * 设置联系人个数
     */
//    abstract fun setContactCount(count: Int): Single<Boolean>

    /**
     * App同步通讯录到设备
     */
    abstract fun updateContactList(contactList: List<WmContact>): Single<Boolean>

    /**
     * syncEmergencyContacts 获取紧急联系人
     */
    abstract fun observableEmergencyContacts(): Observable<WmEmergencyCall>

    /**
     * updateEmergencyContact 设置紧急联系人 null 为删除
     */
    abstract fun updateEmergencyContact(contacts: WmEmergencyCall): Single<WmEmergencyCall>

}