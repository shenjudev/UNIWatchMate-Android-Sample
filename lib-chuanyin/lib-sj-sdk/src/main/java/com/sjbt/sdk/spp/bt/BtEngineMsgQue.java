package com.sjbt.sdk.spp.bt;

import static com.sjbt.sdk.spp.cmd.CmdConfigKt.BT_MSG_BASE_LEN;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_8015;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.DIVIDE_N_2;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.DIVIDE_N_JSON;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.DIVIDE_Y_E_2;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.DIVIDE_Y_E_JSON;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.HEAD_COMMON;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.HEAD_DEVICE_ERROR;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.sjbt.sdk.SJUniWatch;
import com.sjbt.sdk.entity.MsgBean;
import com.sjbt.sdk.log.SJLog;
import com.sjbt.sdk.utils.BtUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 客户端和服务端的基类，用于管理socket长连接
 */
@SuppressWarnings("MissingPermission")
public class BtEngineMsgQue {

    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String TAG = "BtEngine";
    public static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private static BluetoothSocket mSocket;
    private static DataOutputStream mOut;
    private static BtStateListener mBtStateListener;
    private static boolean isRunning;
    private static boolean deviceBusing;

    public static final int TRANSFER_END_TIMEOUT = 15000;
    private static int DEFAULT_MSG_TIMEOUT = 10 * 1000;
    private static int MIN_MSG_TIMEOUT = 5 * 1000;
    private static Map<String, Runnable> sendingMsgQueueMap = new ConcurrentHashMap<>();
    private static LinkedHashMap<String, MsgBean> cachedMsgLinkedMap = new LinkedHashMap<>();
    private static Handler mHandler = new Handler(Looper.myLooper());
    private static Handler mUIHandler = new Handler(Looper.getMainLooper());

    protected static BluetoothDevice mDevice;

    protected static final int TYPE_MSG = 0x11;
    protected static final int TYPE_CONNECT = 0x12;
    protected static MyHandlerThread myHandlerThread = new MyHandlerThread("bt_thread");

    public static final int SOCKET_STATE_NONE = 0;
    public static final int SOCKET_STATE_CONNECTING = 1;
    public static final int SOCKET_STATE_CONNECTED = 2;

    private static final Map<String, Integer> mSocketStateMap = new HashMap<>();

    public void clearStateMap() {
        mSocketStateMap.clear();
    }

    public int getSocketState(String mac) {
        if (mSocketStateMap.get(mac) == null) {
            return 0;
        }

        return mSocketStateMap.get(mac);
    }

    public static void setDefaultMsgTimeout(int defaultMsgTimeout) {
        if (defaultMsgTimeout < MIN_MSG_TIMEOUT) {
            defaultMsgTimeout = MIN_MSG_TIMEOUT;
        }

        DEFAULT_MSG_TIMEOUT = defaultMsgTimeout;
    }

    private static SJUniWatch mSjUniWatch;

    private BtEngineMsgQue() {
    }

    public BtEngineMsgQue(SJUniWatch sjUniWatch) {
        mSjUniWatch = sjUniWatch;
        logD("BtEngine() 构建");
        myHandlerThread.startThread();
    }

    public BtStateListener getListener() {
        return mBtStateListener;
    }

    private static void logD(String msg) {
        ((SJLog) mSjUniWatch.getWmLog()).logD(TAG, msg);
    }

    /**
     * 循环读取对方数据(若没有数据，则阻塞等待)
     */
    private static void socketConnectRead() throws IOException {

        if (!mSocket.isConnected()) {
            logD("start to connect -->:" + mDevice.getAddress());

            mSocketStateMap.put(mDevice.getAddress(), SOCKET_STATE_CONNECTING);
            mSocket.connect();
            deviceBusing = false;
        }

        if (mSocket.isConnected()) {
            mSocketStateMap.put(mDevice.getAddress(), SOCKET_STATE_CONNECTED);
            BluetoothDevice device = mSocket.getRemoteDevice();
            clearMessageQueue();
            logD("connect success:" + device.getAddress());

            notifyUI(BtStateListener.CONNECTED, device);

            EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {

                    try {
                        mOut = new DataOutputStream(mSocket.getOutputStream());
                        InputStream inputStream = mSocket.getInputStream();
                        isRunning = true;

                        byte[] result = new byte[0];

                        while (isRunning) {
                            byte[] buffer = new byte[256];
                            // 等待有数据
                            while (inputStream.available() == 0 && isRunning) {
                                if (System.currentTimeMillis() < 0)
                                    break;
                            }
                            while (isRunning) {//循环读取
                                try {
                                    int num = inputStream.read(buffer);
//                            logD(TAG,"容许最大长度Transmit:" + socket.getMaxTransmitPacketSize());
                                    byte[] temp = new byte[result.length + num];
                                    System.arraycopy(result, 0, temp, 0, result.length);
                                    System.arraycopy(buffer, 0, temp, result.length, num);
                                    result = temp;
                                    if (inputStream.available() == 0)
                                        break;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    notifyErrorOnUI("1-" + e.getMessage());
                                    break;
                                }
                            }

                            try {

                                logD("back message：" + byte2Hex(result));
                                if (result.length == 0) {
                                    return;
                                }

                                parseMsg(result);
                                // 清空
                                result = new byte[0];
                            } catch (Exception e) {
                                e.printStackTrace();
                                notifyErrorOnUI("2-" + e.getMessage());

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        notifyUI(BtStateListener.ON_SOCKET_CLOSE, mDevice);
                    }
                }
            });
        }
    }

    public BluetoothSocket getmSocket() {
        return mSocket;
    }

    public static class MyHandlerThread extends HandlerThread {
        Handler mBzyHandler;

        public MyHandlerThread(String name) {
            super(name);
        }

        public void startThread() {
            logD("startThread");
            start();
            mBzyHandler = new Handler(getLooper()) {
                @Override
                public void handleMessage(Message msg) {

//                    logD(TAG,"handleMessage:" + msg.what);
                    // 在这里处理消息
                    switch (msg.what) {
                        case TYPE_MSG:
                            sendMsg((byte[]) msg.obj);
                            break;

                        case TYPE_CONNECT:
                            if (mDevice != null) {
                                logD("create BluetoothSocket：" + mDevice.getAddress());
                                try {
//                                    if (!mSocket.isConnected()) {
                                    mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
//                                    }
                                    logD("start thread to read " + mDevice.getAddress());
                                    socketConnectRead();
                                } catch (IOException e) {
//                                    closeSocket("loopRead异常 " + e, true);
                                    e.printStackTrace();
                                    notifyErrorOnUI("3-" + e.getMessage());
                                }
                            } else {
                                logD("Exception Device = null");
                            }
                            break;
                    }
                }
            };
        }

        public void sendMessage(int what, Object obj) {
//            logD("sendMessage:" + what);
            if (mBzyHandler != null) {
                Message message = mBzyHandler.obtainMessage();
                message.what = what;
                message.obj = obj;
                boolean success = mBzyHandler.sendMessage(message);
//                logD("sendMessage结果:" + success);
                if (!success) {
                    startThread();
                }
            }
        }
    }

    /**
     * 与远端设备建立长连接
     *
     * @param dev 远端设备
     */
    public void connect(BluetoothDevice dev) {
        try {
            if (getSocketState(dev.getAddress()) == SOCKET_STATE_CONNECTED) {
                closeSocket("BtClient connect ：" + dev.getAddress(), false);
            }

            mDevice = dev;
            sendHandleMessage(TYPE_CONNECT, dev);
        } catch (Throwable e) {
            closeSocket("BtClient Exception ", true);
            e.printStackTrace();
            notifyErrorOnUI("5-" + e.getMessage());
        }
    }

    private void sendHandleMessage(int what, Object obj) {
        myHandlerThread.sendMessage(what, obj);
    }

    /**
     * 发送短消息
     */
    public void sendMsgOnWorkThread(byte[] bytes) {
        if (deviceBusing) {
            MsgBean msgBean = MsgBean.Companion.fromByteArrayToMsgBean(bytes);
            notifyUI(BtStateListener.BUSY, msgBean);
            return;
        }

        sendHandleMessage(TYPE_MSG, bytes);
    }

    public void sendCommunicateMsg(byte[] bytes) {
        try {
            mOut.write(bytes);
            mOut.flush();
            logD("send communicate message：" + BtUtils.bytesToHexString(bytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized static void sendMsg(byte[] bytes) {
        MsgBean msgBean = MsgBean.Companion.fromByteArrayToMsgBean(bytes);
        String msgTimeCode = msgBean.getTimeOutCode();

        if (!sendingMsgQueueMap.isEmpty()) {
            cachedMsgLinkedMap.put(msgBean.getTimeOutCode(), msgBean);
            return;
        } else {
            sendingMsgQueueMap.put(msgTimeCode, new Runnable() {
                @Override
                public void run() {
                    notifyUI(BtStateListener.TIME_OUT, msgBean);
                    removeMsgQue(msgTimeCode);

                    sendCachedMsgBusiness();
                }
            });
        }

        try {
            if (msgBean.isNeedTimeOut()) {
                logD("send message timeout code：" + msgTimeCode);
                mHandler.postDelayed(sendingMsgQueueMap.get(msgTimeCode), DEFAULT_MSG_TIMEOUT);
            } else {
                removeMsgQue(msgTimeCode);
            }

            mOut.write(bytes);
            mOut.flush();
            logD("send message：" + BtUtils.bytesToHexString(bytes));
        } catch (Throwable e) {
            e.printStackTrace();
            notifyErrorOnUI("4-" + e.getMessage());
        }
    }

    private static void removeMsgQue(String msgTimeCode) {

        Runnable runnable = sendingMsgQueueMap.get(msgTimeCode);
        if (runnable != null) {
            mHandler.removeCallbacks(sendingMsgQueueMap.get(msgTimeCode));
        }
        sendingMsgQueueMap.remove(msgTimeCode);

        if (cachedMsgLinkedMap.containsKey(msgTimeCode)) {
            cachedMsgLinkedMap.remove(msgTimeCode);
        }
    }

    private static Runnable busyRun = new Runnable() {
        @Override
        public void run() {
            deviceBusing = false;
        }
    };

    /**
     * 释放监听引用(例如释放对Activity引用，避免内存泄漏)
     */
    public void unListener() {
        mBtStateListener = null;
    }

    /**
     * 设置监听功能
     *
     * @param mBtStateListener
     */
    public void setListener(BtStateListener mBtStateListener) {
        this.mBtStateListener = mBtStateListener;
    }

    /**
     * 关闭Socket连接
     */
    public void closeSocket(String name, boolean isNotify) {
        try {
            mSocketStateMap.clear();
            isRunning = false;
            deviceBusing = false;

            if (mSocket != null && mSocket.isConnected()) {
                mSocket.close();
                logD(name + " close Socket");

                if (isNotify) {
                    notifyUI(BtStateListener.ON_SOCKET_CLOSE, null);
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // ============================================通知UI===========================================================

    private static void notifyUI(final int state, final Object obj) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mBtStateListener != null) {
                        if (state == BtStateListener.MSG) {

                            MsgBean msgBean = (MsgBean) obj;

                            if (msgBean.head == HEAD_DEVICE_ERROR) {
                                mBtStateListener.socketNotifyError(msgBean);
                            } else {
                                try {
                                    if (msgBean.cmdId == CMD_ID_8015 && msgBean.head == HEAD_COMMON) {

                                        deviceBusing = msgBean.getPayload()[16] == 1;

                                        logD("msg busy：" + deviceBusing);

                                        if (deviceBusing) {
                                            mHandler.postDelayed(busyRun, 15000);
                                        } else {
                                            mHandler.removeCallbacks(busyRun);
                                        }

                                    } else {
                                        mBtStateListener.socketNotify(state, msgBean);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        } else {
                            mBtStateListener.socketNotify(state, obj);
                        }
                    } else {
                        logD("BtStateListener is null, cannot dispatch");
                    }

                } catch (Throwable e) {
                    logD("333." + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean isDeviceBusy() {
        return deviceBusing;
    }

    private static void parseMsg(byte[] msg) {
        byte[] lenArray = new byte[4];
        System.arraycopy(msg, 4, lenArray, 0, 4);
        int payloadLen = ((lenArray[2]) & 0XFF) | ((lenArray[3] & 0XFF) << 8);

        if (payloadLen == msg.length - BT_MSG_BASE_LEN) {
            MsgBean msgBean = MsgBean.Companion.fromByteArrayToMsgBean(msg);

            if (msgBean.isNeedTimeOut()) {
                String msgTimeCode = msgBean.getTimeOutCode();
                logD("back message timeout code 1：" + msgTimeCode + " divide type:" + msgBean.getDivideType());
                removeMsgQue(msgTimeCode);
                logD("back message sending map size 1：" + sendingMsgQueueMap.size());
            }

            if (msgBean.getDivideType() == DIVIDE_N_2 || msgBean.getDivideType() == DIVIDE_N_JSON || msgBean.getDivideType() == DIVIDE_Y_E_2 || msgBean.getDivideType() == DIVIDE_Y_E_JSON) {
                sendCachedMsgBusiness();
            }

            notifyUI(BtStateListener.MSG, msgBean);

        } else {
            int tempPosition = 0;
            while (tempPosition != msg.length) {

                byte[] tempLenArray = new byte[4];
                System.arraycopy(msg, tempPosition + 4, tempLenArray, 0, 4);
                payloadLen = ((tempLenArray[2]) & 0XFF) | ((tempLenArray[3] & 0XFF) << 8);

                logD("payLoad2 len hex:" + BtUtils.bytesToHexString(tempLenArray));
                logD("payLoad2 len：" + payloadLen);

                byte[] singleMsg = new byte[payloadLen + BT_MSG_BASE_LEN];
                System.arraycopy(msg, tempPosition, singleMsg, 0, singleMsg.length);
                tempPosition = tempPosition + singleMsg.length;

                logD("split msg：" + BtUtils.bytesToHexString(singleMsg));

                MsgBean msgBean = MsgBean.Companion.fromByteArrayToMsgBean(singleMsg);

                if (msgBean.isNeedTimeOut()) {
                    String msgTimeCode = msgBean.getTimeOutCode();
                    logD("back message timeout code 2：" + msgTimeCode);
                    logD("back message sending size 2：" + sendingMsgQueueMap.size());

                    removeMsgQue(msgTimeCode);
                }

                if (msgBean.getDivideType() == DIVIDE_N_2 || msgBean.getDivideType() == DIVIDE_Y_E_2 || msgBean.getDivideType() == DIVIDE_Y_E_JSON) {
                    sendCachedMsgBusiness();
                }

                notifyUI(BtStateListener.MSG, msgBean);
            }
        }

    }

    private static void sendCachedMsgBusiness() {
        if (sendingMsgQueueMap.isEmpty()) {

            logD("cached msg queue:" + cachedMsgLinkedMap);

            if (!cachedMsgLinkedMap.isEmpty()) {
                Object[] msgKeySet = cachedMsgLinkedMap.keySet().toArray();
                logD("send cached msg");
                sendMsg(cachedMsgLinkedMap.get(msgKeySet[0].toString()).originData);
            }
        }
    }

    protected static void notifyErrorOnUI(String msg) {
        if (mDevice != null) {
            mSocketStateMap.put(mDevice.getAddress(), 0);
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mBtStateListener != null) {
                            mBtStateListener.onConnectFailed(mDevice, msg);
                        } else {
                            logD("BtEngine listener was destroyed");
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void clearMsgQueue() {
        if (sendingMsgQueueMap.size() > 0) {
            for (String str : sendingMsgQueueMap.keySet()) {
                mHandler.removeCallbacks(sendingMsgQueueMap.get(str));
            }
            sendingMsgQueueMap.clear();
        }

        cachedMsgLinkedMap.clear();
    }

    private static void clearMessageQueue() {
        if (sendingMsgQueueMap.size() > 0) {
            for (String str : sendingMsgQueueMap.keySet()) {
                mHandler.removeCallbacks(sendingMsgQueueMap.get(str));
            }
            sendingMsgQueueMap.clear();
        }
        cachedMsgLinkedMap.clear();
    }

    /**
     * 字节数组转换为 16 进制字符串
     *
     * @param bytes 字节数组
     * @return Hex 字符串
     */
    private static String byte2Hex(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String hash = formatter.toString();
        formatter.close();
        return hash;
    }

}
