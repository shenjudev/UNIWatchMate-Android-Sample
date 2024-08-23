package com.sjbt.sdk.sample.utils

import android.content.Context
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import com.sjbt.sdk.sample.base.Config
import com.sjbt.sdk.sample.base.Constant
import com.sjbt.sdk.sample.ui.fileTrans.LocalFileBean
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Locale

val TAG = "GetVideoListUtils"
object GetVideoListUtils {

    val instance: GetVideoListUtils = this

    private val QUERY_URI = MediaStore.Files.getContentUri("external")
    private val ORDER_BY = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC" //按最近修改时间排序
    private var isAndroidQ = false


    /**
     * Media file database field
     */
    private val PROJECTION = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.MediaColumns.DATA,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.WIDTH,
        MediaStore.MediaColumns.HEIGHT,
        MediaStore.MediaColumns.DURATION,
        MediaStore.MediaColumns.SIZE,
        MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.BUCKET_ID,
        MediaStore.MediaColumns.DATE_MODIFIED,
        MediaStore.MediaColumns.DATE_TAKEN
    )

    private const val NOT_GIF_UNKNOWN = "!='image/*'"
    private val NOT_GIF =
        "!='image/gif' AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF_UNKNOWN

    /**
     * Get pictures or videos
     */
    private val SELECTION_ALL_ARGS_VIDEO =
        arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())

    /**
     * Android Q
     *
     * @param id
     * @return
     */
    private fun getRealPathAndroid_Q(id: Long): String? {
        return QUERY_URI.buildUpon()
            .appendPath(id.toString() + "").build().toString()
    }

    /**
     * 获取所有图片/视频
     *
     * @return
     */
    private fun getSelection(): String? {
        // Get all, not including audio
        return getDurationCondition(0, 2000)?.let { getSelectionArgsForAllMediaCondition(it) }
    }

    /**
     * Get video (maximum or minimum time)
     *
     * @param exMaxLimit
     * @param exMinLimit
     * @return
     */
    private fun getDurationCondition(exMaxLimit: Long, exMinLimit: Long): String? {
        var maxS = Long.MAX_VALUE
        if (exMaxLimit != 0L) {
            maxS = Math.min(maxS, exMaxLimit)
        }
        return String.format(
            Locale.CHINA,
            "%d <%s " + MediaStore.MediaColumns.DURATION + " and " + MediaStore.MediaColumns.DURATION + " <= %d",
            exMinLimit.coerceAtLeast(0),
            if (exMinLimit.coerceAtLeast(0) == 0L) "" else "=",
            maxS
        )
    }


    /**
     * Query conditions in all modes
     *
     * @param time_condition
     * @return
     */
    private fun getSelectionArgsForAllMediaCondition(time_condition: String): String? {
        return ("(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF
                + " OR "
                + (MediaStore.Files.FileColumns.MEDIA_TYPE + "=? AND " + time_condition) + ")"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0")
    }


    /**
     * 加载图库
     *
     * @param context
     * @return
     */
    fun loadPageMediaData(context: Context): List<LocalFileBean>? {
        val data = context.contentResolver.query(
            QUERY_URI,
            PROJECTION,
            getSelection(),
            SELECTION_ALL_ARGS_VIDEO,
            ORDER_BY
        )
        var beanList = listOf<LocalFileBean>()
        try {
            if (data != null) {
                val count = data.count
                if (count > 0) {
                    data.moveToFirst()
                    do {
                        val id = data.getLong(
                            data.getColumnIndexOrThrow(
                                PROJECTION[0]
                            )
                        )
                        val absolutePath = data.getString(
                            data.getColumnIndexOrThrow(
                                PROJECTION[1]
                            )
                        )
                        val url = if (isAndroidQ) getRealPathAndroid_Q(id) else absolutePath
                        var mimeType = data.getString(
                            data.getColumnIndexOrThrow(
                                PROJECTION[2]
                            )
                        )
                        Log.e(TAG,"mimeType: $mimeType  path: $absolutePath")
                        if (mimeType == "video/avi") {
                            val isSjjc = isSJAVI(absolutePath)
                            if (!isSjjc) {
                                continue
                            } else {
                                val isW20 = findISFTBytes(absolutePath)
                                if (!isW20)
                                    continue
                            }
                        } else {
                            continue
                        }

                        val width = data.getInt(
                            data.getColumnIndexOrThrow(
                                PROJECTION[3]
                            )
                        )
                        val height = data.getInt(
                            data.getColumnIndexOrThrow(
                                PROJECTION[4]
                            )
                        )
                        val duration = data.getLong(
                            data.getColumnIndexOrThrow(
                                PROJECTION[5]
                            )
                        )
                        val size = data.getLong(
                            data.getColumnIndexOrThrow(
                                PROJECTION[6]
                            )
                        )
                        val folderName = data.getString(
                            data.getColumnIndexOrThrow(
                                PROJECTION[7]
                            )
                        )
                        val fileName = data.getString(
                            data.getColumnIndexOrThrow(
                                PROJECTION[8]
                            )
                        )
                        val bucketId = data.getLong(
                            data.getColumnIndexOrThrow(
                                PROJECTION[9]
                            )
                        )
                        val addTime = data.getLong(
                            data.getColumnIndexOrThrow(
                                PROJECTION[10]
                            )
                        ) * 1000
                        if (addTime <= 0) {
                            continue
                        }
                        var createTime = data.getLong(
                            data.getColumnIndexOrThrow(
                                PROJECTION[11]
                            )
                        )
                        //Google 手机特有
                        if (createTime <= 0) {
                            createTime = addTime
                        }

                        if (!TextUtils.isEmpty(absolutePath) && !File(absolutePath).exists()) {
                        } else {
                            var bean = LocalFileBean()
                            bean.type = Config.FILE_TYPE_VIDEO
                            bean.mediaId = id.toString()
                            bean.duration = duration.toInt()
                            bean.fileUrl = absolutePath
                            bean.fileName = fileName
                            bean.size = AudioUtils.formatFileSize(size)
                            beanList += bean
                        }
                    } while (data.moveToNext())
                }
                Log.i(
                    "LoadMedia",
                    "loadPageMediaData:分组完成 " + +System.currentTimeMillis() + "数量" + count
                )
                return beanList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("TAG", "loadAllMedia Data Error: " + e.message)
        } finally {
            if (data != null && !data.isClosed) {
                data.close()
            }
        }
        return beanList
    }

    private fun isSJAVI(absPath: String): Boolean {
        val file = File(absPath)
        val inputStream: InputStream = FileInputStream(file)
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var bytesRead: Int
        var isSJJC = false
        try {
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                if (outputStream.toByteArray().size > (0x28.toInt() + 4) && !isSJJC) {
                    isSJJC = true
                    // 找到了索引为 0x28 开始的后四位 byte
                    val result = outputStream.toByteArray()
                    val startIndex = 0x28.toInt() // 将索引0x28转换为Int类型
                    val endIndex = startIndex + 4 // 结束索引为起始索引加4
                    val subArray = result.copyOfRange(startIndex, endIndex) // 截取子数组
                    val str = String(subArray, StandardCharsets.UTF_8)
                Log.e(TAG, "Found the required byte at index $bytesRead: $str")
                    return str.isNotEmpty() && str == Constant.SJJC
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return false
    }

    private fun findISFTBytes(aviFilePath: String?): Boolean {
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(aviFilePath)
            // AVI文件INFO区域的标识
            val infoTag = byteArrayOf(
                'I'.code.toByte(),
                'N'.code.toByte(),
                'F'.code.toByte(),
                'O'.code.toByte()
            )
            val buffer = ByteArray(4)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                if (bytesRead == 4 && isByteArrayEqual(buffer, infoTag)) {
                    // 找到INFO区域，接下来读取ISFT信息
                    val infoBytes = ByteArray(12)
                    fis.read(infoBytes)
                    // 仅保留从length到length-4的4个字节
                    val trimmedInfoBytes = infoBytes.copyOfRange(infoBytes.size - 4, infoBytes.size)
                    val str = String(trimmedInfoBytes, StandardCharsets.UTF_8)
                    Log.e(TAG,"校验格式 str : $str")
                    return str.isNotEmpty() && str.contains(Constant.W20)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            if (fis != null) {
                try {
                    fis.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return false
    }

    private fun isByteArrayEqual(array1: ByteArray, array2: ByteArray): Boolean {
        if (array1.size != array2.size) {
            return false
        }
        for (i in array1.indices) {
            if (array1[i] != array2[i]) {
                return false
            }
        }
        return true
    }


}