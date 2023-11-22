package com.base.sdk.port.app

import com.base.sdk.entity.apps.WmCameraFrameInfo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * 应用模块-相机
 */
abstract class AbAppCamera {

    /**
     * 监听设备端相机开启状态
     */
    abstract val observeCameraOpenState: Observable<Boolean>

    /**
     * 监听拍照状态
     */
    abstract val observeCameraTakePhoto: Observable<Any>

    /**
     * App打开/关闭相机
     */
    abstract fun openCloseCamera(open: Boolean): Single<Boolean>

    /**
     * 监听相机端闪光灯状态
     */
    abstract val observeCameraFlash: Observable<WMCameraFlashMode>

    /**
     * 相机闪光灯设置
     */
    abstract fun cameraFlashSwitch(type: WMCameraFlashMode): Single<WMCameraFlashMode>

    /**
     * 相机前后摄像头监听
     */
    abstract val observeCameraFrontBack: Observable<WMCameraPosition>

    /**
     * 设置相机前后摄像头
     */
    abstract fun cameraBackSwitch(isBack: WMCameraPosition): Single<WMCameraPosition>

    /**camera preview 相机预览相关**/

    /**
     * 相机预览是否准备好
     */
    abstract fun startCameraPreview(): Single<Boolean>

    /**
     * 更新相机预览
     */
    abstract fun updateCameraPreview(data: WmCameraFrameInfo)

}

enum class WMCameraPosition {
    WMCameraPositionFront,   /// 前置摄像头
    WMCameraPositionRear     /// 后置摄像头
}

enum class WMCameraFlashMode {
    WMCameraFlashModeOff,    /// 闪光灯关闭
    WMCameraFlashModeOn,     /// 闪光灯开启
    WMCameraFlashModeAuto    /// 闪光灯自动
}

