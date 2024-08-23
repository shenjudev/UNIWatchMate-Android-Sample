package com.sjbt.sdk.sample.ui.device.dial.diyDial.editVideo;

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.ReturnCode
import com.blankj.utilcode.util.LogUtils
import kotlin.math.abs

object CropVideoUtils {
    val TAG = "CropVideoUtils"
    fun cropFrames(
        inputPath: String,
        outputPathPattern: String,
        touchX: Float,
        touchY: Float,
        startTime: String,
        fps: Int,
        cropWidth: Int,
        cropHeight: Int,
        scaleWidth: Int,
        scaleHeight: Int,
        deviceW:Int,
        deviceH:Int
    ): Int {

//        val scaleWidth =
//            (DeviceEditVideoActivity.mOriginalWidth * DeviceEditVideoActivity.scale)// 请替换为有效的宽度
//        val scaleHeight =
//            (DeviceEditVideoActivity.mOriginalHeight * DeviceEditVideoActivity.scale) // 请替换为有效的高度

//      -ss [start_time] -i input.mp4 -t 6 -vf "crop=320:240:50:100,scale=320:240,fps=25" output_%04d.jpg
//      val cmd = "-y -ss $startTime -i $inputPath -t $duration -vf \"crop=$cropParams,scale=$scaleParams,unsharp=5:5:1.0:3:3:0.5,fps=$fps\" -qscale:v 1 $outputPathPattern"
//        -y -ss 00:00:00.000 -i /storage/emulated/0/7ad8e3936f96d7fcbd927ef3fb9768d1.mp4 -t 5 -vf "scale=2163:3847,crop=153:603:850.5219:506.56696,scale=120:140,unsharp=5:5:1.0:3:3:0.5,fps=20" -qscale:v 1 /storage/emulated/0/Android/data/com.transsion.oraimohealth/files/oraimoHealth/dial/customDial/bg/frame_%04d.jpg
        val cmd =
            "-y -ss $startTime -i $inputPath -t ${5} -vf " +
                    "\"scale=${scaleWidth}:${scaleHeight}," +
                    "crop=${cropWidth}:${cropHeight}:" +
                    "${abs((touchX))}:${abs((touchY))}," +
                    "scale=${deviceW}:${
                        deviceH
                    }" +
                    ",unsharp=5:5:1.0:3:3:0.5,fps=$fps\" -qscale:v 1 $outputPathPattern"

        LogUtils.e("cmd:$cmd")
        val session: FFmpegSession = FFmpegKit.execute(cmd)
        if (ReturnCode.isSuccess(session.returnCode)) {
            // SUCCESS
            return 1
        } else if (ReturnCode.isCancel(session.returnCode)) {
            // CANCEL
            return 0
        } else {
            // FAILURE
            LogUtils.d(
                TAG,
                java.lang.String.format(
                    "Command failed with state %s and rc %s.%s",
                    session.state,
                    session.returnCode,
                    session.failStackTrace
                )
            )
            return -1
        }
    }
}