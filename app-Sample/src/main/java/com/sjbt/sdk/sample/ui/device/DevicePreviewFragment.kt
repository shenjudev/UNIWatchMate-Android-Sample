package com.sjbt.sdk.sample.ui.device

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmVideoFrameInfo
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentDevicePreviewBinding
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.Executors

class DevicePreviewFragment : BaseFragment(R.layout.fragment_device_preview),
    TextureView.SurfaceTextureListener {

    private val TAG = "DevicePreviewFragment"

    private val viewBind: FragmentDevicePreviewBinding by viewBinding()
    private var decodeSurface: Surface? = null
    private var codec: MediaCodec? = null
    private var isCodecConfigured = false
    var isOpenVideoPreview = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.btnPlay.setOnClickListener(blockClick)
        viewBind.textureView.surfaceTextureListener = this@DevicePreviewFragment

        viewLifecycle.launchRepeatOnStarted {
            launch {
                UNIWatchMate.wmApps.appVideoPreview.observeVideoPreviewState.subscribe { status: Int ->
                    Log.e(TAG, "observeVideoPreviewState：$status   $isOpenVideoPreview")
                    viewBind.btnPlay.isClickable = true
                    hideInfoDialog()
                    if (status == 2) {
                        //忙
                        isOpenVideoPreview = false
                        return@subscribe
                    }
                    isOpenVideoPreview = status == 1
                }
            }

            launch {
                UNIWatchMate.wmApps.appVideoPreview.observeVideoFrame.observeOn(AndroidSchedulers.mainThread())
                    .subscribe { frameInfo: WmVideoFrameInfo ->
                        frameInfo.frameData?.let {
                            decodeJpgAndDisplay(it)
                        }
                    }
            }
        }

    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.btnPlay -> {
                ivPlayClick(1)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (isOpenVideoPreview)
            ivPlayClick(0)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden && isOpenVideoPreview) {
            ivPlayClick(0)
        }
    }


    private fun ivPlayClick(isIvPlay: Int) {
        if (!viewBind.btnPlay.isClickable)
            return
        if (isIvPlay == 1)
            showInfoDialog("Loading...")
        viewBind.btnPlay.isClickable = false
        isOpenVideoPreview = !isOpenVideoPreview
        UNIWatchMate.wmApps.appVideoPreview.toggleVideoPreview(isOpenVideoPreview)
            .subscribe { resultCode: Int ->
                Log.e(TAG, "toggleVideoPreview：$resultCode")
                if (resultCode != 0) {
                    if (isIvPlay == 1) {
                        when (resultCode) {
                            1 -> {
                                //失败
                                ToastUtil.showToast("fail")
                            }

                            2 -> {
                                //忙
                                ToastUtil.showToast("busy")
                            }

                            else -> {
                                //暂时无法打开
                                ToastUtil.showToast("open error")
                            }
                        }
                    }
                    isOpenVideoPreview = false
                }
                viewBind.btnPlay.text =
                    if (isOpenVideoPreview) getString(R.string.action_stop) else getString(R.string.action_start)
            }
    }

    private fun decodeJpgAndDisplay(jpegData: ByteArray) {
        if (!isOpenVideoPreview) {
            return
        }
        if (!isCodecConfigured) {
            startDecoder()
        }
        decodeFrame(jpegData)
    }


    private fun startDecoder() {
        try {
            // 创建MediaFormat，并配置视频参数
            val format = MediaFormat.createVideoFormat("video/avc", 360, 240) // 替换为视频的实际分辨率
            format.setInteger(MediaFormat.KEY_BIT_RATE, 2000000)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
            format.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)

            // 使用MediaCodecList来获取设备支持的解码器，并选择合适的解码器
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos
            var codecName: String? = null

            // 查找是否有硬件解码器支持 H.264
            for (i in codecList.indices) {
                val codecInfo = codecList[i]
                if (!codecInfo.isEncoder && codecInfo.name.contains("h264")) {
                    // 判断硬件解码器（排除软件解码器）
                    if (codecInfo.name.startsWith("OMX") && !codecInfo.name.startsWith("OMX.google")) {
                        codecName = codecInfo.name // 发现硬件解码器
                        break
                    }
                }
            }

            // 如果没有找到硬件解码器，使用软件解码器
            if (codecName == null) {
                codecName = "OMX.google.h264.decoder" // 使用软件解码器
            }

            // 使用指定的解码器名称来创建解码器
            codec = MediaCodec.createByCodecName(codecName!!)
            codec!!.configure(format, decodeSurface, null, 0)
            codec!!.start()
            isCodecConfigured = true

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun decodeFrame(frameData: ByteArray) {
        if (!isCodecConfigured) {
            Log.e(TAG, "Codec is not configured")
            return
        }
        val singleThreadDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        CoroutineScope(singleThreadDispatcher).launch {
            try {
                // 输入缓冲区
                val inputBufferIndex = codec!!.dequeueInputBuffer(20000)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = codec!!.getInputBuffer(inputBufferIndex)
                    inputBuffer?.clear()
                    inputBuffer?.put(frameData)
                    codec!!.queueInputBuffer(
                        inputBufferIndex,
                        0,
                        frameData.size,
                        System.currentTimeMillis(),
                        0
                    )
                }

                // 输出缓冲区处理
                val bufferInfo = MediaCodec.BufferInfo()
                var outputBufferIndex = codec!!.dequeueOutputBuffer(bufferInfo, 10000)

                var attempts = 0
                while (outputBufferIndex >= 0 && attempts < 5) {
                    if (bufferInfo.size > 0) {
                        codec?.releaseOutputBuffer(outputBufferIndex, true) // 输出已渲染到Surface
                    } else {
                        codec?.releaseOutputBuffer(outputBufferIndex, false)
                    }
                    outputBufferIndex = codec!!.dequeueOutputBuffer(bufferInfo, 10000)
                    attempts++
                }

            } catch (e: Exception) {
                Log.e(TAG, e.printStackTrace().toString())
            }
        }
    }

    override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
        decodeSurface?.release()
        decodeSurface = Surface(p0)
        Log.e("surface", "onSurfaceTextureAvailable")

    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
        Log.e("surface", "onSurfaceTextureSizeChanged")
    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
        decodeSurface?.release()
        decodeSurface = null
        codec?.let {
            it.stop()
            it.release()
            isCodecConfigured = false
        }
        Log.e("surface", "onSurfaceTextureDestroyed")
        return true
    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
        Log.e("surface", "onSurfaceTextureUpdated")
    }

    private fun stopDecoder() {
        try {
            codec?.stop()
            codec?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping MediaCodec: ${e.message}")
        } finally {
            codec = null
            isCodecConfigured = false
        }
    }

}