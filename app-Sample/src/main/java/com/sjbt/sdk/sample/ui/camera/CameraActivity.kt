package com.sjbt.sdk.sample.ui.camera

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.Image.Plane
import android.media.MediaActionSound
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmVideoFrameInfo
import com.base.sdk.port.app.WMCameraFlashMode
import com.base.sdk.port.app.WMCameraPosition
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ScreenUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.shenju.cameracapturer.FrameData
import com.shenju.cameracapturer.OSIJni
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseActivity
import com.sjbt.sdk.sample.data.device.flowStateConnected
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.dialog.CallBack
import com.sjbt.sdk.sample.dialog.CameraBusDialog
import com.sjbt.sdk.sample.dialog.CameraBusDialog.Companion.TIP_TYPE_OPEN_CAMERA
import com.sjbt.sdk.sample.utils.CacheDataHelper
import com.sjbt.sdk.sample.utils.SingleMediaScanner
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.viewLifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CameraActivity : BaseActivity() {
    private val TAG = "CameraActivity"
    private var previewView: PreviewView? = null
    private var img_switch: ImageView? = null
    private var image_flash: ImageView? = null
    private var ivTook: ImageView? = null
    private var ivTookPic: ImageView? = null
    private var tv_back: ImageView? = null
    private var img_take_photo: ImageView? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var mCameraControl: CameraControl? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var front = false
    private var mCameraBusDialog: CameraBusDialog? = null
    private var flashOn = false
    private var vibrator: Vibrator? = null
    private var mediaActionSound: MediaActionSound? = null
    private val osiJni = OSIJni()
    private var startAnalsis = false
    private var isPausedCamera = false
    private var isCameraOpened = false
    private val encoderThread = HandlerThread("encoder")
    private var encoderHandler: Handler? = null
    private var myOrientationListener: MyOrientationListener? = null
    private val isSupportCameraPreview = true
    private val deviceManager = Injector.getDeviceManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        EventBus.getDefault().register(this);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_camera)
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        mediaActionSound = MediaActionSound()
        mediaActionSound!!.load(MediaActionSound.SHUTTER_CLICK)
        myOrientationListener = MyOrientationListener(this)

        var width = 320
        var height = 240
        val basicInfo = CacheDataHelper.getCurrentDeiceBean()

        if (basicInfo != null) {
            width = basicInfo.cw
            height = basicInfo.ch
        }


        lifecycle.launchRepeatOnStarted {
            launch {
                val it = UNIWatchMate.getDeviceInfo().await()
                CacheDataHelper.setCurrentDeviceInfo(it)

                val basicInfo = CacheDataHelper.getCurrentDeiceBean()

                if (basicInfo != null) {
                    width = basicInfo.cw
                    height = basicInfo.ch
                }

                osiJni.initEncoder(width, height)
                initView()
                intCamera()

            }

            launch {
                deviceManager.flowStateConnected().collect {
                    if (it) {
                        Toast.makeText(
                            this@CameraActivity,
                            getString(R.string.device_connected),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        showDisableView()
                    }
                }
            }

            launch {
                UNIWatchMate.wmApps.appCamera.observeCameraTakePhoto.asFlow()
                    .collect { takePhoto() }
            }

            launch {
                UNIWatchMate.wmApps.appCamera.observeCameraFrontBack.asFlow().collect {
                    front = it === WMCameraPosition.WMCameraPositionFront
                    bindCameraUseCases()
                }
            }

            launch {
                UNIWatchMate.wmApps.appCamera.observeCameraFlash.asFlow().collect {
                    flashOn = it === WMCameraFlashMode.WMCameraFlashModeOn
                    mCameraControl!!.enableTorch(flashOn)
                    if (flashOn) {
                        image_flash!!.setImageResource(R.mipmap.biu_icon_flash_on)
                    } else {
                        image_flash!!.setImageResource(R.mipmap.biu_icon_flash_off)
                    }
                }

            }

            launch {
                UNIWatchMate.wmApps.appCamera.observeCameraOpenState.subscribe { aBoolean: Boolean ->
                    isCameraOpened = aBoolean
                    UNIWatchMate.wmLog.logE(TAG, "设备相机状态2：$isCameraOpened")
                    if (isCameraOpened) {
                        checkCameraPreview()
                    } else {
//                    showCameraBusDialog(CameraBusDialog.TIP_TYPE_OPEN_CAMERA)
                        finish()
                    }
                }

            }
        }
    }

    override fun onStart() {
        super.onStart()
        myOrientationListener!!.enable()
    }

    override fun onRestart() {
        super.onRestart()
        myOrientationListener!!.enable()
    }

    override fun onStop() {
        super.onStop()
        myOrientationListener!!.disable()
        isCameraOpened = false
    }

    private fun checkCameraPreview() {
        if (!isSupportCameraPreview) {
            return
        }

        mHandler.postDelayed({

            if (startAnalsis) {
                if (CacheDataHelper.cameraLaunchedByDevice || CacheDataHelper.cameraLaunchedBySelf) {
                    UNIWatchMate.wmApps.appCamera.startCameraPreview()
                        .subscribe { result: Boolean ->
                            UNIWatchMate.wmLog.logD(
                                TAG,
                                "isCameraPreviewReady:$result"
                            )
                        }
                }
            } else {
                checkCameraPreview()
            }

        }, 300)
    }

    private fun showCameraBusDialog(type: Int) {
        if (mCameraBusDialog != null) {
            mCameraBusDialog!!.dismiss()
            mCameraBusDialog = null
        }
        mCameraBusDialog = CameraBusDialog(this, type, object : CallBack<Int> {
            override fun callBack(o: Int) {
            }

        })
        mCameraBusDialog!!.show()
    }

    private fun initView() {
        previewView = findViewById(R.id.previewView)
        img_switch = findViewById(R.id.image_switch)
        image_flash = findViewById(R.id.image_flash)
        ivTook = findViewById(R.id.iv_took)
        tv_back = findViewById(R.id.iv_close)
        ivTookPic = findViewById(R.id.iv_took_pic)
        img_take_photo = findViewById(R.id.img_take_photo)
        previewView?.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE)
        previewView?.setScaleType(PreviewView.ScaleType.FILL_CENTER)

        //切换相机
        img_switch?.setOnClickListener(View.OnClickListener {
            switchCamera()
            bindCameraUseCases()
        })

        //还没拍照就点取消
        tv_back?.setOnClickListener(View.OnClickListener { v: View? -> finish() })

        //点击拍照
        img_take_photo?.setOnClickListener(View.OnClickListener { v: View? -> takePhoto() })
        image_flash?.setOnClickListener(View.OnClickListener {
            flashOn = !flashOn
            mCameraControl!!.enableTorch(flashOn)
            if (flashOn) {
                image_flash?.setImageResource(R.mipmap.biu_icon_flash_on)
            } else {
                image_flash?.setImageResource(R.mipmap.biu_icon_flash_off)
            }
            UNIWatchMate.wmApps.appCamera.cameraFlashSwitch(if (flashOn) WMCameraFlashMode.WMCameraFlashModeOn else WMCameraFlashMode.WMCameraFlashModeOff)
                .subscribe()
        })

        ivTook?.setOnClickListener(View.OnClickListener {
            if (ivTookPic?.visibility == View.GONE) {
                ivTookPic?.setVisibility(View.VISIBLE)
            } else {
                ivTookPic?.setVisibility(View.GONE)
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            autoFocus(event.x.toInt(), event.y.toInt(), false)
        }
        return super.onTouchEvent(event)
    }

    private fun switchCamera() {
        front = !front
        UNIWatchMate.wmApps.appCamera.cameraBackSwitch(if (front) WMCameraPosition.WMCameraPositionFront else WMCameraPosition.WMCameraPositionRear)
            .subscribe()
    }

    private fun intCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                if (ActivityUtils.isActivityAlive(this)) {
                    bindCameraUseCases()
                }
            } catch (e: Exception) {
                Log.d("wld________", e.toString())
            }
        }, ContextCompat.getMainExecutor(this))

        encoderThread.start()
        encoderHandler = Handler(encoderThread.looper)
    }

    private fun bindCameraUseCases() {
        val cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderListenableFuture.addListener({
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build()

            imageCapture = ImageCapture.Builder() //优化捕获速度，可能降低图片质量
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build()

            imageAnalysis = ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888).build()

            // 在重新绑定之前取消绑定用例
            cameraProvider!!.unbindAll()

            val cameraSelector =
                if (front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

            val camera = cameraProvider!!.bindToLifecycle(
                this@CameraActivity,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalysis
            )

            preview.setSurfaceProvider(previewView!!.surfaceProvider)
            mCameraControl = camera.cameraControl
            mCameraControl!!.enableTorch(flashOn)
            if (flashOn) {
                image_flash!!.setImageResource(R.mipmap.biu_icon_flash_on)
            } else {
                image_flash!!.setImageResource(R.mipmap.biu_icon_flash_off)
            }

            if (isSupportCameraPreview) {
                imageAnalysis!!.setAnalyzer(ContextCompat.getMainExecutor(this@CameraActivity)) { imageProxy -> //                        ImageProxy.PlaneProxy planeProxy = imageProxy.getPlanes()[0];
                    encoderHandler!!.post {
                        val cameraOrientation = imageProxy.imageInfo.rotationDegrees
                        runEncoder(imageProxy, cameraOrientation)
                    }
                }
            }
        }, ContextCompat.getMainExecutor(this))

        autoFocus(ScreenUtils.getScreenWidth() / 2, ScreenUtils.getScreenHeight() / 2, true)
    }

    inner class MyOrientationListener(context: Context?) : OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
            if (orientation == ORIENTATION_UNKNOWN) {
                return
            }
            val rotation: Int
            rotation = if (orientation >= 45 && orientation < 135) {
                Surface.ROTATION_270
            } else if (orientation >= 135 && orientation < 225) {
                Surface.ROTATION_180
            } else if (orientation >= 225 && orientation < 315) {
                Surface.ROTATION_90
            } else {
                Surface.ROTATION_0
            }
            if (imageAnalysis != null && imageCapture != null) {
                imageAnalysis!!.targetRotation = rotation
                imageCapture!!.targetRotation = rotation
            }
        }
    }

    private fun runEncoder(imageProxy: ImageProxy, orientation: Int?) {
        val h264Data = FrameData()
        var ret = 0
        val image = imageProxy.image
        val planes = image!!.planes
        Timber.d(
            "FRONT:" + front + " YUV1 Width:" + image.width + "HEIGHT:" + image.height
        )
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]
        val yBytes = extractPlaneData(yPlane, image.width, image.height)
        var uBytes: ByteArray? = null
        var vBytes: ByteArray? = null
        if (uPlane.pixelStride == 2) { // UV数据交错
            uBytes = extractUVPlaneData(uPlane, image.width / 2, image.height / 2) // 注意宽度和高度
        } else {
            uBytes = extractPlaneData(uPlane, image.width / 2, image.height / 2) // 注意宽度和高度
            vBytes = extractPlaneData(vPlane, image.width / 2, image.height / 2) // 注意宽度和高度
        }
        synchronized(osiJni) {
            ret = osiJni.runEncoder(
                yBytes, uBytes, vBytes, image.width, image.height,
                orientation ?: 90, h264Data
            )
        }

        // Prevent concurrent operations on h264Writer
        synchronized(osiJni) {
            if (ret == 0 && !isPausedCamera) {
                startAnalsis = true

                val cameraFrameInfo = WmVideoFrameInfo()
                cameraFrameInfo.frameData = h264Data.frameData
                cameraFrameInfo.frameType = h264Data.frameType
                cameraFrameInfo.frameId = System.currentTimeMillis()

                UNIWatchMate.wmApps.appCamera.updateCameraPreview(cameraFrameInfo)
            } else {
                startAnalsis = false
            }
        }
        imageProxy.close()
    }

    private fun extractPlaneData(plane: Plane, width: Int, height: Int): ByteArray {
        val buffer = plane.buffer
        val stride = plane.rowStride
        val pixelStride = plane.pixelStride
        val data = ByteArray(width * height * pixelStride)
        var offset = 0
        for (y in 0 until height) {
            var x = 0
            while (x < width * pixelStride) {
                data[offset++] = buffer[y * stride + x]
                x += pixelStride
            }
        }
        return data
    }

    private fun extractUVPlaneData(plane: Plane, width: Int, height: Int): ByteArray {
        val buffer = plane.buffer
        val stride = plane.rowStride
        val pixelStride = plane.pixelStride
        val data = ByteArray(width * height * pixelStride)
        var offset = 0
        for (y in 0 until height) {
            var x = 0
            while (x < width * pixelStride) {
                data[offset] = buffer[y * stride + x]
                val v_pos = y * stride + x + 1
                data[offset + 1] =
                    buffer[(if (v_pos >= buffer.limit()) buffer.limit() - 1 else v_pos)]

                offset += pixelStride
                x += pixelStride
            }
        }
        return data
    }


    private fun takePhoto() {
        // 保证相机可用
        if (imageCapture == null) return
        val outputOptions: ImageCapture.OutputFileOptions
        outputOptions =
            if (Build.MANUFACTURER.contains("vivo") && Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
                val photoFile = Tools.getPicturePath(this)
                ImageCapture.OutputFileOptions.Builder(File(photoFile)).build()
            } else {
                // Create time stamped name and MediaStore entry.
                val name = SimpleDateFormat(FILENAME_FORMAT, Locale.CHINA)
                    .format(System.currentTimeMillis())
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    contentValues.put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "Pictures/CameraX-Image"
                    )
                }
                val contentResolver = contentResolver
                ImageCapture.OutputFileOptions.Builder(
                    contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                    .build()
            }

        //  设置图像捕获监听器，在拍照后触发
        imageCapture!!.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val localUri = outputFileResults.savedUri
                    val localIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri)
                    sendBroadcast(localIntent)

                    runOnUiThread {
                        Toast.makeText(
                            this@CameraActivity,
                            getString(R.string.pic_save_to_gallery),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    SingleMediaScanner(
                        this@CameraActivity,
                        localUri!!.path,
                        "image/jpeg"
                    ) { path, uri ->
                        Timber.d("路径path1：$path")
                        if (uri != null) {
                            Timber.d("路径path：" + path + " -- Uri:" + uri.path)
                        }
                    }

                    val transformation = RoundedCorners(16)
                    Glide.with(this@CameraActivity)
                        .load(localUri) //不是本地资源就改为url即可
                        .optionalTransform(transformation)
                        .into(ivTook!!)
                    Glide.with(this@CameraActivity).load(localUri).into(ivTookPic!!)
                    Timber.d("照片存储路径:" + localUri.path)
                    try {
                        vibrator!!.vibrate(10L) // 参数是震动时间(long类型)
                        mediaActionSound!!.play(MediaActionSound.SHUTTER_CLICK)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Timber.d("拍照出错")
                    //                        Toast.makeText(CameraActivity.this, R.string.camera_err, Toast.LENGTH_SHORT).show();
                }
            })
    }

    private fun autoFocus(x: Int, y: Int, first: Boolean) {
        val factory: MeteringPointFactory =
            SurfaceOrientedMeteringPointFactory(x.toFloat(), y.toFloat())
        val point = factory.createPoint(x.toFloat(), y.toFloat())
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
            .setAutoCancelDuration(10, TimeUnit.SECONDS)
            .build()
        val future = mCameraControl!!.startFocusAndMetering(action)
        future.addListener({}, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("$localClassName onDestroy")
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        unregisterSensor()
        cameraProvider?.unbindAll()
        mediaActionSound?.release()
        encoderThread.quitSafely()
        imageAnalysis?.clearAnalyzer()
        isPausedCamera = true

        CacheDataHelper.cameraLaunchedByDevice = false
        CacheDataHelper.cameraLaunchedBySelf = false
    }

    private fun unregisterSensor() {
        synchronized(osiJni) { osiJni.closeEncoder() }
    }

    override fun onPause() {
        super.onPause()
        Timber.d("$localClassName -> onPause")
        isPausedCamera = true
        isCameraOpened = false
        UNIWatchMate.wmApps.appCamera.openCloseCamera(false).subscribe()
    }

    override fun onResume() {
        super.onResume()
        if (CacheDataHelper.cameraLaunchedByDevice && !isPausedCamera) {
            checkCameraPreview()
        } else if (isPausedCamera || CacheDataHelper.cameraLaunchedBySelf) {
            UNIWatchMate.wmApps.appCamera.openCloseCamera(true).subscribe { open: Boolean ->
                isCameraOpened = open
                if (open) {
                    checkCameraPreview();
                } else {
                    showCameraBusDialog(TIP_TYPE_OPEN_CAMERA)
                }
            }
        }

        isPausedCamera = false
    }

    private fun showDisableView() {
        CacheDataHelper.setTransferring(false)
        Toast.makeText(this, getString(R.string.disconnect_tips), Toast.LENGTH_SHORT).show()
        finish()
    }


    companion object {
        private const val CHANGE_CAMERA: Int = 0
        private const val CHANGE_FLASH: Int = 1
        private const val FILENAME_FORMAT = "yyyy-MM-dd HH:mm:ss"
        const val REQUEST_CAMERA = 0x1001
        fun launchActivity(context: Context?) {
            context?.let {
                val intent = Intent(it, CameraActivity::class.java)
                it.startActivity(intent)
            }
        }

        fun finishCamera(activity: CameraActivity) {
            activity.finish()
        }
    }
}