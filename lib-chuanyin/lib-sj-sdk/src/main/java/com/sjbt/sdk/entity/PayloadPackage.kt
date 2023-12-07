package com.sjbt.sdk.entity

import com.sjbt.sdk.spp.cmd.MAX_BUSINESS_BUFFER_SIZE
import java.nio.ByteBuffer

class PayloadPackage {
    var _id: Short = 0
    var packageSeq: Int = 0
    var actionType: Int = 0
    var packageLimit: Short = 0
    var itemCount: Int = 0
    var itemList: MutableList<NodeData> = mutableListOf()

    init {
        this._id = RequestIdGenerator.instance.generateRequestId()
    }

    companion object {

        /**
         * 从字节数组中解析出"首包"PayloadPackage
         *
         * @param data
         * @return
         */
        fun fromByteArray(payloadData: ByteArray): PayloadPackage {
            val payload = PayloadPackage()
            val bytes = ByteBuffer.wrap(payloadData)
            bytes.order(java.nio.ByteOrder.LITTLE_ENDIAN)
            payload._id = bytes.short
            payload.packageSeq = bytes.int

            payload.actionType = bytes.get().toInt()
            payload.packageLimit = bytes.short
            payload.itemCount = bytes.get().toInt()

            //判断bytes是否读完
            while (bytes.hasRemaining()) {
                val nextNode = NodeData.fromByteBuffer(bytes, payload.actionType)
                payload.itemList.add(nextNode)
            }

//            if (payload.itemList.size != payload.itemCount) {
//                throw Exception("itemList.size != itemCount")
//            }

            return payload
        }
    }

    private fun buildPackageHeader(bytes: ByteBuffer) {
        bytes.order(java.nio.ByteOrder.LITTLE_ENDIAN)
        bytes.putShort(_id)
        bytes.putInt(packageSeq++)

        bytes.put(actionType.toByte())
        bytes.putShort(packageLimit)
//        bytes.put(itemCount.toByte())
    }

    /**
     * 判断是否还有下一个包
     * @return
     */
    fun hasNext(): Boolean {
        return packageSeq != 0xFFFFFFFF.toInt()
    }

    /**
     * 从字节数组中解析出"非首包"PayloadPackage
     *
     * @param data
     */
    fun next(data: ByteArray) {
        val bytes = ByteBuffer.wrap(data)
        bytes.order(java.nio.ByteOrder.LITTLE_ENDIAN)
        _id = bytes.short
        packageSeq = bytes.int
        actionType = bytes.get().toInt()
        packageLimit = bytes.short
        val count = bytes.get()
        itemCount += count

        //判断bytes是否读完
        while (bytes.hasRemaining()) {
            val nextNode = NodeData.fromByteBuffer(bytes, actionType)
            itemList.add(nextNode)
        }
        if (itemList.size != itemCount) {
            throw Exception("itemList.size != itemCount")
        }
    }

    /**
     * 添加数据
     * @param urn 资源名
     * @param data 数据
     * @param dataFmt 数据格式
     */
    fun putData(urn: ByteArray, data: ByteArray, dataFmt: DataFormat = DataFormat.FMT_BIN) {
        val nodeData = NodeData(urn, data, dataFmt)
        itemList.add(nodeData)
        itemCount++
    }

    /**
     * 将payload转换为byte数组，返回业务单元字节流，如果是多业务分包，就会返回多个
     * @param mtu MTU
     * @return
     */
    fun toByteArray(
        requestType: RequestType
    ): List<ByteArray> {
        //val limitation = DEFAULT_ITEM_MAX_LEN*3/2
        val payloadList = mutableListOf<ByteArray>() //payload列表
        val bytes: ByteBuffer = ByteBuffer.allocate(MAX_BUSINESS_BUFFER_SIZE*3/2) //payload
//        var tempByteArray = ByteArray(0)
        actionType = requestType.type
//        buildPackageHeader(bytes)
        var total_item_count = 0
        var count = 1
        itemList.mapIndexed() { index, item ->
            val nextNode = item.toBytes()
            //count++
            total_item_count++
            //如果现有的payload长度加上当前item的长度超过了限制，则将现有的payload加入到payloadList中，
            // 并重新计算payload长度
//            if (bytes.position() + tempByteArray.size + nextNode.size > limitation) {
//                bytes.put(count.toByte()) //将itemCount写入bytes
//                bytes.put(tempByteArray) //将tempByteArray写入bytes
//
//                bytes.flip() // Now the limit is set to position
//                val actualData = ByteArray(bytes.limit())
//                bytes.get(actualData)
//                payloadList.add(actualData)
//
//                count = 0
//                bytes.clear()
//                tempByteArray = ByteArray(0)
//                buildPackageHeader(bytes)
//                tempByteArray = tempByteArray.plus(nextNode)
//            } else {
//                tempByteArray = tempByteArray.plus(nextNode)
//            }

            bytes.clear()
            buildPackageHeader(bytes)

            bytes.put(count.toByte()) //将itemCount写入bytes
            bytes.put(nextNode) //将tempByteArray写入bytes
            // 如果是最后一个payload，则将packageSeq重置为0xFFFF,并将payload加入到payloadList中
            if (index == itemList.size-1)
                bytes.putInt(2, 0xFFFFFFFF.toInt())

            bytes.flip() // Now the limit is set to position
            val actualData = ByteArray(bytes.limit())
            bytes.get(actualData)
            payloadList.add(actualData)
        }


//        if (total_item_count != itemCount) {
//            throw Exception("total_item_count != itemCount")
//        }

        return payloadList
    }


    fun toResponseByteArray(
        mtu: Int = MAX_BUSINESS_BUFFER_SIZE,
        requestType: ResponseResultType
    ): List<ByteArray> {
        val limitation = mtu
        val payloadList = mutableListOf<ByteArray>() //payload列表
        val bytes: ByteBuffer = ByteBuffer.allocate(limitation) //payload
        var tempByteArray = ByteArray(0)
        actionType = requestType.type
        buildPackageHeader(bytes)
        var total_item_count = 0
        var count = 0
        itemList.mapIndexed() { index, item ->
            val nextNode = item.toBytes()
            count++
            total_item_count++
            //如果现有的payload长度加上当前item的长度超过了限制，则将现有的payload加入到payloadList中，
            // 并重新计算payload长度
            if (bytes.position() + tempByteArray.size + nextNode.size > limitation) {
                bytes.put(count.toByte()) //将itemCount写入bytes
                bytes.put(tempByteArray) //将tempByteArray写入bytes

                bytes.flip() // Now the limit is set to position
                val actualData = ByteArray(bytes.limit())
                bytes.get(actualData)
                payloadList.add(actualData)

                count = 0
                bytes.clear()
                tempByteArray = ByteArray(0)
                buildPackageHeader(bytes)
            } else {
                tempByteArray = tempByteArray.plus(nextNode)
            }
        }

        // 如果是最后一个payload，则将packageSeq重置为0xFFFF,并将payload加入到payloadList中
        if (bytes.position() > 0) {
            bytes.putInt(2, 0xFFFFFFFF.toInt())
        }

        bytes.put(count.toByte()) //将itemCount写入bytes
        bytes.put(tempByteArray) //将tempByteArray写入bytes
        bytes.flip() // Now the limit is set to position
        val actualData = ByteArray(bytes.limit())
        bytes.get(actualData)
        payloadList.add(actualData)

//        if (total_item_count != itemCount) {
//            throw Exception("total_item_count != itemCount")
//        }

        return payloadList
    }
}

/**
 * requestId生成器
 */
class RequestIdGenerator {
    companion object {
        val instance: RequestIdGenerator by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RequestIdGenerator()
        }
        var currentRequestId: Short = 0
    }

    /**
     * 生成请求ID,避免多线程并发问题
     * @return 请求ID
     */
    @Synchronized
    fun generateRequestId(): Short {
        //自动生成双字节范围的ID，从0开始逐次递增,到65535后重新开始
        currentRequestId++
        if (currentRequestId == Short.MAX_VALUE) {
            currentRequestId = 0
        }
        return currentRequestId
    }

}