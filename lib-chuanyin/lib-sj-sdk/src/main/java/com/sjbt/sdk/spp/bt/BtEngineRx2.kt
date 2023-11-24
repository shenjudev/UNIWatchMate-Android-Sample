package com.sjbt.sdk.spp.bt

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.MsgBean.Companion.fromByteArrayToMsgBean
import com.sjbt.sdk.spp.cmd.BT_MSG_BASE_LEN
import com.sjbt.sdk.spp.cmd.CMD_ID_8015
import com.sjbt.sdk.spp.cmd.HEAD_COMMON
import com.sjbt.sdk.spp.cmd.HEAD_DEVICE_ERROR
import com.sjbt.sdk.utils.BtUtils
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * 客户端和服务端的基类，用于管理socket长连接
 */
class BtEngineRx2 {

    fun clearStateMap() {
        mSocketStateMap.clear()
    }

    fun getSocketState(mac: String): Int {
        return if (mSocketStateMap.get(mac) == null) {
            0
        } else mSocketStateMap.get(
            mac
        )!!
    }

    private constructor() {}

    constructor(sjUniWatch: SJUniWatch?) {
        mSjUniWatch = sjUniWatch
        logD("BtEngine() 构建")
        myHandlerThread.startThread()
    }

    fun getmSocket(): BluetoothSocket? {
        return mSocket
    }

    class MyHandlerThread(name: String?) : HandlerThread(name) {
        var mBzyHandler: Handler? = null
        fun startThread() {
            logD("startThread")
            start()
            mBzyHandler = object : Handler(looper) {
                override fun handleMessage(msg: Message) {

//                    logD(TAG,"handleMessage:" + msg.what);
                    // 在这里处理消息
                    when (msg.what) {
                        TYPE_MSG -> sendMsg(msg.obj as ByteArray)
                        TYPE_CONNECT -> if (mDevice != null) {
                            logD("create BluetoothSocket：" + mDevice!!.address)
                            try {
//                                    if (!mSocket.isConnected()) {
                                mSocket =
                                    mDevice!!.createInsecureRfcommSocketToServiceRecord(SPP_UUID)
                                //                                    }
                                logD("start thread to read " + mDevice!!.address)
                                socketConnectRead()
                            } catch (e: IOException) {
//                                    closeSocket("loopRead异常 " + e, true);
                                e.printStackTrace()
                                notifyErrorOnUI("3-" + e.message)
                            }
                        } else {
                            logD("Exception Device = null")
                        }
                    }
                }
            }
        }

        fun sendMessage(what: Int, obj: Any?) {
            if (mBzyHandler != null) {
                val message = mBzyHandler!!.obtainMessage()
                message.what = what
                message.obj = obj
                val success = mBzyHandler!!.sendMessage(message)
                if (!success) {
                    startThread()
                }
            }
        }
    }

    /**
     * 与远端设备建立长连接
     *
     * @param dev 远端设备
     */
    fun connect(dev: BluetoothDevice) {
        try {
            if (getSocketState(dev.address) == SOCKET_STATE_CONNECTED) {
                closeSocket("BtClient connect ：" + dev.address, false)
            }
            mDevice = dev
            sendHandleMessage(TYPE_CONNECT, dev)
        } catch (e: Throwable) {
            closeSocket("BtClient Exception ", true)
            e.printStackTrace()
            notifyErrorOnUI("5-" + e.message)
        }
    }

    private fun sendHandleMessage(what: Int, obj: Any) {
        myHandlerThread.sendMessage(what, obj)
    }

    /**
     * 发送短消息
     */
    fun sendMsgOnWorkThread(bytes: ByteArray) {
        if (isDeviceBusy) {
            val msgBean = fromByteArrayToMsgBean(bytes)
            notifyUI(BtStateListener.BUSY, msgBean)
            return
        }
        sendHandleMessage(TYPE_MSG, bytes)
    }

    /**
     * 释放监听引用(例如释放对Activity引用，避免内存泄漏)
     */
    fun unListener() {
        listener = null
    }

    /**
     * 设置监听功能
     *
     * @param mBtStateListener
     */
    fun setListener(mBtStateListener: BtStateListener) {
        listener = mBtStateListener
    }

    /**
     * 关闭Socket连接
     */
    fun closeSocket(name: String, isNotify: Boolean) {
        try {
            mSocketStateMap.clear()
            isDeviceBusy = false
            lock.lock()
            isRunning = false
            lock.unlock()
            if (mSocket != null && mSocket!!.isConnected) {
                mSocket!!.close()
                logD("$name close Socket")
                if (isNotify) {
                    notifyUI(BtStateListener.ON_SOCKET_CLOSE, null)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun clearMsgQueue() {
        if (msgQueueMap.size > 0) {
            for (str: String? in msgQueueMap.keys) {
                mHandler.removeCallbacks((msgQueueMap[str])!!)
            }
            msgQueueMap.clear()
        }
    }

    companion object {
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private val TAG = "BtEngine"
        val EXECUTOR: Executor = Executors.newSingleThreadExecutor()
        private var mSocket: BluetoothSocket? = null
        private var mOut: DataOutputStream? = null
        var listener: BtStateListener? = null
        private var isRunning = false
        private var isSending = false
        var isDeviceBusy = false
        private val lock: Lock = ReentrantLock() // 锁对象
        private var DEFAULT_MSG_TIMEOUT = 10 * 1000
        private val MIN_MSG_TIMEOUT = 5 * 1000
        private val msgQueueMap = HashMap<String?, Runnable>()
        private val mHandler = Handler((Looper.myLooper())!!)
        private val mUIHandler = Handler(Looper.getMainLooper())
        protected var mDevice: BluetoothDevice? = null
        protected val TYPE_MSG = 0x11
        protected val TYPE_CONNECT = 0x12
        protected var myHandlerThread = MyHandlerThread("bt_thread")

        val SOCKET_STATE_NONE = 0
        val SOCKET_STATE_CONNECTING = 1
        val SOCKET_STATE_CONNECTED = 2
        private val mSocketStateMap: MutableMap<String, Int?> = HashMap()

        fun setDefaultMsgTimeout(defaultMsgTimeout: Int) {
            var defaultMsgTimeout = defaultMsgTimeout
            if (defaultMsgTimeout < MIN_MSG_TIMEOUT) {
                defaultMsgTimeout = MIN_MSG_TIMEOUT
            }
            DEFAULT_MSG_TIMEOUT = defaultMsgTimeout
        }

        private var mSjUniWatch: SJUniWatch? = null

        private fun logD(msg: String) {
            mSjUniWatch!!.wmLog.logD(TAG, msg)
        }

        /**
         * 循环读取对方数据(若没有数据，则阻塞等待)
         */
        @Throws(IOException::class)
        private fun socketConnectRead() {
            if (!mSocket!!.isConnected) {
                logD("start to connect -->:" + mDevice!!.address)
                mSocketStateMap[mDevice!!.address] = SOCKET_STATE_CONNECTING
                mSocket!!.connect()
                isDeviceBusy = false
            }
            if (mSocket!!.isConnected) {
                mSocketStateMap[mDevice!!.address] = SOCKET_STATE_CONNECTED
                val device = mSocket!!.remoteDevice
                clearMessageQueue()
                logD("connect success:" + device.address)
                notifyUI(BtStateListener.CONNECTED, device)
                EXECUTOR.execute(object : Runnable {
                    override fun run() {
                        try {
                            mOut = DataOutputStream(mSocket!!.outputStream)
                            val inputStream = mSocket!!.inputStream
                            lock.lock()
                            isRunning = true
                            lock.unlock()
                            var result = ByteArray(0)
                            while (isRunning) {
                                val buffer = ByteArray(256)
                                // 等待有数据
                                while (inputStream.available() == 0 && isRunning) {
                                    if (System.currentTimeMillis() < 0) break
                                }
                                while (isRunning) { //循环读取
                                    try {
                                        val num = inputStream.read(buffer)
                                        //                            logD(TAG,"容许最大长度Transmit:" + socket.getMaxTransmitPacketSize());
                                        val temp = ByteArray(result.size + num)
                                        System.arraycopy(result, 0, temp, 0, result.size)
                                        System.arraycopy(buffer, 0, temp, result.size, num)
                                        result = temp
                                        if (inputStream.available() == 0) break
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        notifyErrorOnUI("1-" + e.message)
                                        break
                                    }
                                }
                                try {
                                    logD("back message：" + byte2Hex(result))
                                    if (result.size == 0) {
                                        return
                                    }
                                    parseMsg(result)
                                    // 清空
                                    result = ByteArray(0)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    notifyErrorOnUI("2-" + e.message)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            notifyUI(BtStateListener.ON_SOCKET_CLOSE, mDevice)
                        }
                    }
                })
            }
        }

        fun sendMsg(bytes: ByteArray?) {
            lock.lock()
            isSending = true
            lock.unlock()
            try {
                val msgBean = fromByteArrayToMsgBean((bytes)!!)
                if (!msgBean.isNotTimeOut) {
                    val msgTimeCode = msgBean.timeOutCode
                    logD("send message timeout code：$msgTimeCode")
                    msgQueueMap[msgTimeCode] = object : Runnable {
                        override fun run() {
                            notifyUI(BtStateListener.TIME_OUT, msgBean)
                            mHandler.removeCallbacks((msgQueueMap.get(msgTimeCode))!!)
                            msgQueueMap.remove(msgTimeCode)
                        }
                    }
                    mHandler.postDelayed((msgQueueMap[msgTimeCode])!!, DEFAULT_MSG_TIMEOUT.toLong())
                }
                mSocket!!.outputStream.write(bytes)
                mSocket!!.outputStream.flush()
                logD("send message：" + BtUtils.bytesToHexString(bytes))
            } catch (e: Throwable) {
//            closeSocket("发送过程 " + e.getMessage(), true);
                e.printStackTrace()
                notifyErrorOnUI("4-" + e.message)
            }
            lock.lock()
            isSending = false
            lock.unlock()
        }

        private val busyRun: Runnable = object : Runnable {
            override fun run() {
                isDeviceBusy = false
            }
        }

        private fun notifyUI(state: Int, obj: Any?) {
            mUIHandler.post(object : Runnable {
                override fun run() {
                    try {
                        if (listener != null) {
                            if (state == BtStateListener.MSG) {
                                val msgBean = obj as MsgBean?
                                if (msgBean!!.head == HEAD_DEVICE_ERROR) {
                                    listener!!.socketNotifyError(msgBean)
                                } else {
                                    try {
                                        if (msgBean.cmdId == CMD_ID_8015.toInt() && msgBean.head == HEAD_COMMON) {
                                            isDeviceBusy = msgBean.payload[16].toInt() == 1
                                            logD("msg busy：" + isDeviceBusy)
                                            if (isDeviceBusy) {
                                                mHandler.postDelayed(busyRun, 15000)
                                            } else {
                                                mHandler.removeCallbacks(busyRun)
                                            }
                                        } else {
                                            listener!!.socketNotify(state, msgBean)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            } else {
                                listener!!.socketNotify(state, obj)
                            }
                        } else {
                            logD("BtStateListener is null, cannot dispatch")
                        }
                    } catch (e: Throwable) {
                        logD("333." + e.message)
                        e.printStackTrace()
                    }
                }
            })
        }

        private fun parseMsg(msg: ByteArray) {
            val lenArray = ByteArray(4)
            System.arraycopy(msg, 4, lenArray, 0, 4)
            var payloadLen =
                ((lenArray[2]).toInt() and 0XFF) or ((lenArray[3].toInt() and 0XFF) shl 8)
            if (payloadLen == msg.size - BT_MSG_BASE_LEN) {
                val msgBean = fromByteArrayToMsgBean(msg)
                if (!msgBean.isNotTimeOut) {
                    val msgTimeCode = msgBean.timeOutCode
                    logD("back message timeout code 1：$msgTimeCode")
                    val runnable = msgQueueMap[msgTimeCode]
                    if (runnable != null) {
                        mHandler.removeCallbacks(runnable)
                        msgQueueMap.remove(msgTimeCode)
                        logD("remove back message timeout code 1：$msgTimeCode")
                    }
                }
                notifyUI(BtStateListener.MSG, msgBean)
            } else {
                var tempPosition = 0
                while (tempPosition != msg.size) {
                    val tempLenArray = ByteArray(4)
                    System.arraycopy(msg, tempPosition + 4, tempLenArray, 0, 4)
                    payloadLen =
                        ((tempLenArray[2]).toInt() and 0XFF) or ((tempLenArray[3].toInt() and 0XFF) shl 8)
                    logD("payLoad2 len hex:" + BtUtils.bytesToHexString(tempLenArray))
                    logD("payLoad2 len：$payloadLen")
                    val singleMsg = ByteArray(payloadLen + BT_MSG_BASE_LEN)
                    System.arraycopy(msg, tempPosition, singleMsg, 0, singleMsg.size)
                    tempPosition = tempPosition + singleMsg.size
                    logD("split msg：" + BtUtils.bytesToHexString(singleMsg))
                    val msgBean = fromByteArrayToMsgBean(singleMsg)
                    if (!msgBean.isNotTimeOut) {
                        val msgTimeCode = msgBean.timeOutCode
                        logD("back message timeout code 2：$msgTimeCode")
                        val runnable = msgQueueMap[msgTimeCode]
                        if (runnable != null) {
                            mHandler.removeCallbacks(runnable)
                            msgQueueMap.remove(msgTimeCode)
                            logD("remove back message timeout code 2：$msgTimeCode")
                        }
                    }
                    notifyUI(BtStateListener.MSG, msgBean)

//                logD("tempPosition：" + tempPosition);
                }
            }
        }

        protected fun notifyErrorOnUI(msg: String?) {
            if (mDevice != null) {
                mSocketStateMap[mDevice!!.address] = 0
                mUIHandler.post(object : Runnable {
                    override fun run() {
                        try {
                            if (listener != null) {
                                listener!!.onConnectFailed(mDevice, msg)
                            } else {
                                logD("BtEngine listener was destroyed")
                            }
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                })
            }
        }

        private fun clearMessageQueue() {
            if (msgQueueMap.size > 0) {
                for (str: String? in msgQueueMap.keys) {
                    mHandler.removeCallbacks((msgQueueMap[str])!!)
                }
                msgQueueMap.clear()
            }
        }

        /**
         * 字节数组转换为 16 进制字符串
         *
         * @param bytes 字节数组
         * @return Hex 字符串
         */
        private fun byte2Hex(bytes: ByteArray): String {
            val formatter = Formatter()
            for (b: Byte in bytes) {
                formatter.format("%02x", b)
            }
            val hash = formatter.toString()
            formatter.close()
            return hash
        }
    }
}