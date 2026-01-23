package com.ota.sdk.utils

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.*
import java.nio.channels.FileChannel

/**
 * OTA 文件处理工具类
 * 提供文件读取、解压等功能
 */
class OtaFileProcessor {
    
    companion object {
        private const val TAG = "OtaFileProcessor"
        private const val BUFFER_SIZE = 1024
    }
    
    /**
     * 解压 tar 文件（用于 .upex 文件）
     * 
     * @param tarFilePath tar 文件路径
     * @param outputDirPath 输出目录路径
     * @return true 如果解压成功
     */
    fun untarFile(tarFilePath: String, outputDirPath: String): Boolean {
        val outputDir = File(outputDirPath)
        if (!outputDir.exists()) {
            outputDir.mkdirs() // 创建输出目录，如果它不存在的话
        }
        
        try {
            TarArchiveInputStream(FileInputStream(tarFilePath)).use { fin ->
                var entry: TarArchiveEntry?
                while (fin.nextTarEntry.also { entry = it } != null) {
                    val currentEntry = entry ?: continue
                    if (currentEntry.isDirectory) {
                        continue // 如果是目录，则跳过
                    }
                    
                    val curfile = File(outputDir, currentEntry.name)
                    val parent = curfile.parentFile
                    if (!parent.exists()) {
                        parent.mkdirs() // 创建当前文件的父目录
                    }
                    
                    BufferedOutputStream(FileOutputStream(curfile)).use { out ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var len: Int
                        while (fin.read(buffer, 0, BUFFER_SIZE).also { len = it } != -1) {
                            out.write(buffer, 0, len)
                        }
                    }
                }
            }
            return true
        } catch (e: IOException) {
            LogUtil.e(TAG, "解压 tar 文件失败: ${e.message}", e)
            return false
        }
    }
    
    /**
     * 读取文件字节数组
     * 
     * @param file 文件对象
     * @return 文件内容的字节数组，如果读取失败返回 null
     */
    fun readFileBytes(file: File): ByteArray? {
        if (!file.exists() || !file.isFile) {
            LogUtil.e(TAG, "文件不存在或不是文件: ${file.absolutePath}")
            return null
        }
        
        return try {
            FileInputStream(file).use { inputStream ->
                val length = inputStream.available()
                val buffer = ByteArray(length)
                inputStream.read(buffer)
                buffer
            }
        } catch (e: IOException) {
            LogUtil.e(TAG, "读取文件失败: ${e.message}", e)
            null
        }
    }
    
    /**
     * 删除目录中的所有文件
     * 
     * @param dir 目录
     */
    fun deleteAllInDir(dir: File) {
        if (!dir.exists() || !dir.isDirectory) {
            return
        }
        
        val files = dir.listFiles()
        files?.forEach { file ->
            if (file.isFile) {
                file.delete()
            } else if (file.isDirectory) {
                deleteAllInDir(file)
                file.delete()
            }
        }
    }
}
