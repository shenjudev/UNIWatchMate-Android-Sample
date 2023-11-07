package com.sjbt.sdk.entity

import android.util.Log
import com.sjbt.sdk.TAG_SJ
import com.sjbt.sdk.log.SJLog
import java.nio.ByteBuffer

class NodeData(
    /**
     * unique resource name, 4 bytes
     */
    var urn: ByteArray,
    var data: ByteArray,
    var dataFmt: DataFormat = DataFormat.FMT_BIN
) {
    var dataLen: Short = 0

    init {
        this.dataLen = data.size.toShort()
    }

    constructor() : this(byteArrayOf(), byteArrayOf(), DataFormat.FMT_BIN) {
    }

    companion object {
        fun fromByteBuffer(byteBuffer: ByteBuffer, type: Int): NodeData {
            val nodeData = NodeData()
            nodeData.urn = ByteArray(4)
            byteBuffer.get(nodeData.urn)

            if (type != RequestType.REQ_TYPE_READ.type && byteBuffer.limit() > 17) {//
                val fmt = byteBuffer.get().toInt()
//                Log.e(TAG_SJ, "fmt:$fmt")
                nodeData.dataFmt = DataFormat.values()[fmt]
//                Log.e(TAG_SJ, "dataFmt:${nodeData.dataFmt}")
                nodeData.dataLen = byteBuffer.short
//                Log.e(TAG_SJ, "dataLen:${byteBuffer.remaining()}")
                nodeData.data = ByteArray(byteBuffer.remaining())
                byteBuffer.get(nodeData.data)
//                Log.e(TAG_SJ, "nodeData:${nodeData}")
            }else{
                byteBuffer.position(byteBuffer.limit())
            }
            return nodeData
        }
    }

    override fun toString(): String {
        return "BaseNodeData(urn=${urn.contentToString()}, dataFmt=$dataFmt, dataLen=$dataLen, data=$data)"
    }

    fun toBytes(): ByteArray {
        val size = 4 + 3 + data.size
        val bytes: ByteBuffer = ByteBuffer.allocate(size)
        bytes.order(java.nio.ByteOrder.LITTLE_ENDIAN)
        bytes.put(urn)
        bytes.put(dataFmt.ordinal.toByte())
        bytes.putShort(dataLen)
        for (item in data) {
            bytes.put(item)
        }

        return bytes.array()
    }
}

enum class DataFormat {
    FMT_BIN,
    FMT_PLAIN_TXT,
    FMT_JSON,
    FMT_NODATA,
    FMT_ERRCODE
}

enum class ErrorCode {
    ERR_CODE_OK,
    ERR_CODE_FAIL,
    ERR_CODE_NODATA,
    ERR_CODE_INVALID_PARAM,
    ERR_CODE_INVALID_URN,
    ERR_CODE_INVALID_DATA,
    ERR_CODE_INVALID_CMD,
    ERR_CODE_INVALID_PACKAGE,
    ERR_CODE_INVALID_PACKAGE_SEQ,
    ERR_CODE_INVALID_PACKAGE_LIMIT,
    ERR_CODE_INVALID_ITEM_COUNT,
    ERR_CODE_INVALID_ITEM_LIST,
    ERR_CODE_INVALID_ITEM_DATA,
    ERR_CODE_INVALID_ITEM_DATA_LEN,
    ERR_CODE_INVALID_ITEM_DATA_FMT,
    ERR_CODE_INVALID_ITEM_DATA_URN,
}

enum class RequestType(val type: Int) {
    REQ_TYPE_INVALID(0),
    REQ_TYPE_READ(1),
    REQ_TYPE_WRITE(2),
    REQ_TYPE_EXECUTE(3),
}

enum class ResponseResultType(val type: Int) {
    RESPONSE_EACH(100),
    RESPONSE_ALL_OK(101),
    RESPONSE_ALL_FAIL(102),
}
