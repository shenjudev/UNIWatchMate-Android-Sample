//[lib-interface](../../../index.md)/[com.base.sdk.port.app](../index.md)/[AbAppCamera](index.md)

# AbAppCamera

[androidJvm]\
abstract class [AbAppCamera](index.md)

应用模块-相机(Application module - camera)

## Constructors

| | |
|---|---|
| [AbAppCamera](-ab-app-camera.md) | [androidJvm]<br>constructor() |

## Properties

| Name | Summary |
|---|---|
| [observeCameraFlash](observe-camera-flash.md) | [androidJvm]<br>abstract val [observeCameraFlash](observe-camera-flash.md): Observable&lt;[WMCameraFlashMode](../-w-m-camera-flash-mode/index.md)&gt;<br>监听相机端闪光灯状态(Listen for the state of the camera flash) |
| [observeCameraFrontBack](observe-camera-front-back.md) | [androidJvm]<br>abstract val [observeCameraFrontBack](observe-camera-front-back.md): Observable&lt;[WMCameraPosition](../-w-m-camera-position/index.md)&gt;<br>相机前后摄像头监听(Camera front/back monitoring) |
| [observeCameraOpenState](observe-camera-open-state.md) | [androidJvm]<br>abstract val [observeCameraOpenState](observe-camera-open-state.md): Observable&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>监听设备端相机开启状态(Listen for the state of the camera on the device) |
| [observeCameraTakePhoto](observe-camera-take-photo.md) | [androidJvm]<br>abstract val [observeCameraTakePhoto](observe-camera-take-photo.md): Observable&lt;[Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;<br>监听拍照状态(Listen for the state of the photo) |

## Functions

| Name | Summary |
|---|---|
| [cameraBackSwitch](camera-back-switch.md) | [androidJvm]<br>abstract fun [cameraBackSwitch](camera-back-switch.md)(isBack: [WMCameraPosition](../-w-m-camera-position/index.md)): Single&lt;[WMCameraPosition](../-w-m-camera-position/index.md)&gt;<br>设置相机前后摄像头(Camera front/back settings) |
| [cameraFlashSwitch](camera-flash-switch.md) | [androidJvm]<br>abstract fun [cameraFlashSwitch](camera-flash-switch.md)(type: [WMCameraFlashMode](../-w-m-camera-flash-mode/index.md)): Single&lt;[WMCameraFlashMode](../-w-m-camera-flash-mode/index.md)&gt;<br>相机闪光灯设置(Camera flash settings) |
| [openCloseCamera](open-close-camera.md) | [androidJvm]<br>abstract fun [openCloseCamera](open-close-camera.md)(open: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>App打开/关闭相机(App opens/closes the camera) |
| [respondCameraOpen](respond-camera-open.md) | [androidJvm]<br>abstract fun [respondCameraOpen](respond-camera-open.md)(appFront: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))<br>响应设备端打开相机的请求（Respond to the request to open the camera on the device) |
| [startCameraPreview](start-camera-preview.md) | [androidJvm]<br>abstract fun [startCameraPreview](start-camera-preview.md)(): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>相机预览是否准备好(Camera preview is ready) |
| [updateCameraPreview](update-camera-preview.md) | [androidJvm]<br>abstract fun [updateCameraPreview](update-camera-preview.md)(data: [WmVideoFrameInfo](../../com.base.sdk.entity.apps/-wm-video-frame-info/index.md))<br>更新相机预览(Update camera preview) |
