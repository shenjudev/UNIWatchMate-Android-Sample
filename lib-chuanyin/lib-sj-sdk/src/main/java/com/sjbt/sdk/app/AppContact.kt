package com.sjbt.sdk.app

import android.text.TextUtils
import com.base.sdk.entity.apps.WmContact
import com.base.sdk.entity.apps.WmContact.Companion.MAX_NAME_LEN_LIMIT
import com.base.sdk.entity.apps.WmContact.Companion.MAX_NUMBER_LEN_LIMIT
import com.base.sdk.entity.settings.WmEmergencyCall
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.app.AbAppContact
import com.sjbt.sdk.*
import com.sjbt.sdk.entity.*
import com.sjbt.sdk.spp.cmd.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.disposables.Disposable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class AppContact(val sjUniWatch: SJUniWatch) : AbAppContact(), ReadSubPkMsg,
    ExceptionStateListener {
    private var getContactListEmitter: SingleEmitter<List<WmContact>>? = null
    private var observableContactListEmitter: ObservableEmitter<List<WmContact>>? = null
    private var updateContactEmitter: SingleEmitter<Boolean>? = null
    private var updateEmergencyEmitter: SingleEmitter<WmEmergencyCall>? = null
    private var getAndObserveEmergencyNumberEmitter: ObservableEmitter<WmEmergencyCall>? = null
    private var mEmergencyCall: WmEmergencyCall = WmEmergencyCall(false, mutableListOf())
    private val mContacts = mutableListOf<WmContact>()
    private val msgList = mutableSetOf<MsgBean>()
    private var getData = false
    private var hasNext = false
    private val TAG = "AppContact"

    override fun setHasNext(hasNext: Boolean) {
        this.hasNext = hasNext
    }

    override fun getHasNext(): Boolean {
        return hasNext
    }

    override fun observeDisconnectState() {

        updateContactEmitter?.let { emitter ->
            if (!emitter.isDisposed) {
                emitter.onError(WmTimeOutException("time out exception"))
            }
        }

        getContactListEmitter?.let { emitter ->
            if (!emitter.isDisposed) {
                emitter.onError(WmTimeOutException("time out exception"))
            }
        }

        updateEmergencyEmitter?.let { emitter ->
            if (!emitter.isDisposed) {
                emitter.onError(WmTimeOutException("time out exception"))
            }
        }

        getAndObserveEmergencyNumberEmitter?.let { emitter ->
            if (!emitter.isDisposed) {
                emitter.onError(WmTimeOutException("time out exception"))
            }
        }
    }

    override fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {
        sjUniWatch.wmLog.logE(TAG, "onTimeOut:$msgBean")

        when (nodeData.urn[2]) {

            URN_APP_CONTACT_LIST -> {

            }

            URN_APP_CONTACT_EMERGENCY -> {

            }
        }
    }

    fun appUpdateContacts() {
        getData = false
        startGetContacts()
    }

    override val observableContactList: Observable<List<WmContact>> = Observable.create {
        observableContactListEmitter = it
    }

    override val getContactList: Single<List<WmContact>> = Single.create {
        mContacts.clear()
        getContactListEmitter = it
        msgList.clear()
        getData = true

        startGetContacts()
    }

    private fun startGetContacts() {
        sjUniWatch.sendReadSubPkObserveNode(this, getReadContactListCmd())
            .subscribe(object : Observer<MsgBean> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: MsgBean) {
                    msgList.add(t)
                }

                override fun onError(e: Throwable) {

                }

                override fun onComplete() {
                    if (msgList.isNotEmpty()) {
                        var byteBuffer = ByteBuffer.allocate(MAX_BUSINESS_BUFFER_SIZE * 10).order(
                            ByteOrder.LITTLE_ENDIAN
                        )

                        msgList.forEachIndexed { index, msgBean ->
                            if (msgBean.divideType == DIVIDE_Y_F_2 || msgBean.divideType == DIVIDE_N_2) {
                                msgBean.payloadPackage?.let {
                                    it.itemList.forEach {
                                        byteBuffer.put(it.data)
                                    }
                                }
                            } else {
                                msgBean.payloadPackage?.let {
                                    it.itemList.forEach {
                                        byteBuffer.put(it.data)
                                    }
                                }
                            }
                        }

//                        val chunkSize = MAX_NAME_LEN_LIMIT + MAX_NUMBER_LEN_LIMIT
//                        var i = 17
//                        while ((i + chunkSize) < byteBuffer.array().size) {
//                            val nameBytes = byteBuffer.array().copyOfRange(i, i + MAX_NAME_LEN_LIMIT)
//                                .takeWhile { it.toInt() != 0 }.toByteArray()
//
//                            val numBytes =
//                                byteBuffer.array()
//                                    .copyOfRange(i + MAX_NUMBER_LEN_LIMIT, i + chunkSize)
//
//                            val name = String(nameBytes, StandardCharsets.UTF_8)
//                            val num = String(numBytes, StandardCharsets.UTF_8)
//
//                            sjUniWatch.wmLog.logE(TAG, "name:" + name + " num:" + num)
//
//                            if (!TextUtils.isEmpty(name)) {
//                                val contact = WmContact.create(name, num)
//                                contact?.let {
//                                    mContacts.add(it)
//                                }
//                                i += chunkSize
//                            } else {
//                                break
//                            }
//                        }

                        mContacts.addAll(parseContactList(byteBuffer))

                        if (getData) {
                            getData = false
                            getContactListEmitter?.onSuccess(mContacts)
                        } else {
                            observableContactListEmitter?.onNext(mContacts)
                        }
                    }
                }
            })
    }

    private fun parseContactList(byteBuffer: ByteBuffer): MutableList<WmContact> {
        val contactList = mutableListOf<WmContact>()
        val count = byteBuffer.limit() / (MAX_NAME_LEN_LIMIT + MAX_NUMBER_LEN_LIMIT)
        byteBuffer.rewind()
        sjUniWatch.wmLog.logD(TAG, "Contact Count：$count")

        if (count > 0) {
            while (byteBuffer.hasRemaining()) {
                val nameArray = ByteArray(MAX_NAME_LEN_LIMIT)
                val numArray = ByteArray(MAX_NUMBER_LEN_LIMIT)
                byteBuffer.get(nameArray)
                byteBuffer.get(numArray)

                val name = String(
                    nameArray.filter { it != 0.toByte() }.toByteArray(),
                    StandardCharsets.UTF_8
                ).replace("\\s".toRegex(), "")

                val num = String(
                    numArray.filter { it != 0.toByte() }.toByteArray(),
                    StandardCharsets.UTF_8
                ).replace("\\s".toRegex(), "")

                sjUniWatch.wmLog.logE(TAG, "name:${name.length} num:${num.length}")

                if (!TextUtils.isEmpty(name)) {
                    val contact = WmContact.create(name, num)
                    contact?.let {
                        mContacts.add(it)
                    }
                } else {
                    byteBuffer.flip()
                    break
                }
            }
        }
        return contactList
    }

    override fun updateContactList(contactList: List<WmContact>): Single<Boolean> = Single.create {
        updateContactEmitter = it

        sjUniWatch.observableMtu.subscribe { mtu ->
            val payloadPackage = getWriteContactListCmd(contactList)

            sjUniWatch.sendWriteSubpackageNodeCmdList(
                mtu,
                payloadPackage
            )
        }
    }

    private fun updateContactListBack(success: Boolean) {
        updateContactEmitter?.onSuccess(success)
    }

    override fun observableEmergencyContacts(): Observable<WmEmergencyCall> = Observable.create {
        getAndObserveEmergencyNumberEmitter = it
        sjUniWatch.sendReadNodeCmdList(getReadEmergencyNumberCmd())
    }

    override fun updateEmergencyContact(emergencyCall: WmEmergencyCall): Single<WmEmergencyCall> =
        Single.create {
            mEmergencyCall = emergencyCall
            updateEmergencyEmitter = it
            sjUniWatch.sendWriteNodeCmdList(getWriteEmergencyNumberCmd(emergencyCall))
        }

    private fun updateEmergencyContactBack(success: Boolean) {
        updateEmergencyEmitter?.onSuccess(mEmergencyCall)
    }

    fun contactBusiness(
        payload: PayloadPackage,
        it: NodeData,
        msgBean: MsgBean
    ) {
        when (it.urn[2]) {

            URN_APP_CONTACT_COUNT -> {
//                contactCountSetEmitter?.onSuccess(it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
            }

            URN_APP_CONTACT_LIST -> {

                if (it.data.size == 1) {
                    if (it.data[0].toInt() == 2 && it.dataFmt == DataFormat.FMT_ERRCODE) {
                        getContactListEmitter?.onSuccess(mContacts)
                    } else {
                        updateContactListBack(it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
                    }

                } else {
                    if (payload.packageSeq == 0) {
                        mContacts.clear()
                    }

                    if (msgBean.divideType == DIVIDE_N_2) {
                        mContacts.addAll(
                            parseContactList(
                                ByteBuffer.wrap(it.data).order(ByteOrder.LITTLE_ENDIAN)
                            )
                        )
                        getContactListEmitter?.onSuccess(mContacts)
                    }
                }
            }

            URN_APP_CONTACT_EMERGENCY -> {

                sjUniWatch.wmLog.logD(TAG, "emergency contact msg!")

                if (it.data.size == 1) {
                    updateEmergencyContactBack(it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
                } else {
                    if (it.dataLen >= MAX_NAME_LEN_LIMIT + MAX_NUMBER_LEN_LIMIT + 1) {
                        val emergencyByteArray = it.data
                        val enable = it.data[0].toInt() == 1
                        mEmergencyCall.isEnabled = enable
                        mEmergencyCall.emergencyContacts.clear()
                        val name = String(
                            emergencyByteArray.copyOfRange(1, MAX_NAME_LEN_LIMIT + 1)
                                .filter { it != 0.toByte() }.toByteArray(),
                            StandardCharsets.UTF_8
                        )

                        val num = String(
                            emergencyByteArray.copyOfRange(
                                MAX_NAME_LEN_LIMIT,
                                MAX_NAME_LEN_LIMIT + MAX_NUMBER_LEN_LIMIT + 1
                            ),
                            StandardCharsets.UTF_8
                        )

                        sjUniWatch.wmLog.logD(TAG, "emergency contact name:$name number:$num")

                        if (!TextUtils.isEmpty(name)) {
                            WmContact.create(name, num)?.let {
                                mEmergencyCall.emergencyContacts.add(it)
                            }

                        } else {
                            mEmergencyCall.isEnabled = false
                        }
                    }

                    getAndObserveEmergencyNumberEmitter?.onNext(mEmergencyCall)
                }
            }
        }
    }

    /**
     * 获取通讯录
     */
    private fun getReadContactListCmd(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(CmdHelper.getUrnId(URN_4, URN_3, URN_2), ByteArray(0))
        return payloadPackage
    }

    /**
     * 更新通讯录
     */
    private fun getWriteContactListCmd(contacts: List<WmContact>): PayloadPackage {
        val payloadPackage = PayloadPackage()

        if (contacts.isNotEmpty()) {
            val count = MAX_BUSINESS_BUFFER_SIZE / (MAX_NAME_LEN_LIMIT + MAX_NUMBER_LEN_LIMIT)

            val contactGroup = contacts.chunked(count)

            for (i in contactGroup.indices) {

                val byteBuffer: ByteBuffer =
                    ByteBuffer.allocate(contactGroup[i].size * (MAX_NAME_LEN_LIMIT + MAX_NUMBER_LEN_LIMIT))

                contactGroup[i].forEach {
                    byteBuffer.put(
                        it.name.toByteArray().copyOf(MAX_NAME_LEN_LIMIT)
                    )

                    byteBuffer.put(
                        it.number.toByteArray().copyOf(MAX_NUMBER_LEN_LIMIT)
                    )
                }

                payloadPackage.putData(CmdHelper.getUrnId(URN_4, URN_3, URN_2), byteBuffer.array())
            }
        } else {
            payloadPackage.putData(CmdHelper.getUrnId(URN_4, URN_3, URN_2), ByteArray(0))
        }

        return payloadPackage
    }

    /**
     * 更新紧急联系人
     */
    private fun getWriteEmergencyNumberCmd(number: WmEmergencyCall): PayloadPackage {
        val payloadPackage = PayloadPackage()

        val byteBuffer: ByteBuffer =
            ByteBuffer.allocate(
                1 + (MAX_NAME_LEN_LIMIT + MAX_NUMBER_LEN_LIMIT) * if (number.emergencyContacts.size == 0) {
                    1
                } else {
                    number.emergencyContacts.size
                }
            )

        byteBuffer.put(
            if (number.isEnabled) {
                1
            } else {
                0
            }.toByte()
        )

        number.emergencyContacts.forEach {
            byteBuffer.put(it.name.toByteArray().copyOf(MAX_NAME_LEN_LIMIT))
            byteBuffer.put(it.number.toByteArray().copyOf(MAX_NUMBER_LEN_LIMIT))
        }

        payloadPackage.putData(CmdHelper.getUrnId(URN_4, URN_3, URN_3), byteBuffer.array())

        return payloadPackage
    }

    /**
     * 获取紧急联系人
     */
    private fun getReadEmergencyNumberCmd(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(0)
        payloadPackage.putData(CmdHelper.getUrnId(URN_4, URN_3, URN_3), byteBuffer.array())
        return payloadPackage
    }
}