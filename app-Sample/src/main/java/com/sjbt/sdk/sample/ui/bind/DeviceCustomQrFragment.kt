package com.sjbt.sdk.sample.ui.bind

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import cn.bertsir.zbar.CameraPreview
import cn.bertsir.zbar.Qr.Symbol
import cn.bertsir.zbar.QrConfig
import cn.bertsir.zbar.ScanCallback
import cn.bertsir.zbar.utils.QRUtils
import cn.bertsir.zbar.view.ScanLineView
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentDeviceCustomQrBinding
import com.sjbt.sdk.sample.ui.device.bind.DeviceBindFragment
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import timber.log.Timber

class DeviceCustomQrFragment : BaseFragment(R.layout.fragment_device_custom_qr), SensorEventListener,
    View.OnClickListener {
    //    , PromptDialogFragment.OnPromptListener {
    private /*const*/ val promptBindSuccessId = 1

    private val viewBind: FragmentDeviceCustomQrBinding by viewBinding()
    private var options: QrConfig? = null
    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private var cp: CameraPreview? = null
    private val AUTOLIGHTMIN = 10f
    private var soundPool: SoundPool? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //        Log.i("zBarLibary", "version: "+BuildConfig.VERSION_NAME);
        val scan_type = QrConfig.TYPE_QRCODE
        var scan_view_type = 0
        var screen = 1
        val line_style = ScanLineView.style_radar

        screen = QrConfig.SCREEN_PORTRAIT
        scan_view_type = QrConfig.SCANVIEW_TYPE_QRCODE

        options = QrConfig.Builder()
            .setDesText(getString(R.string.scan_device))
            .setShowDes(false)
            .setShowLight(true)
            .setShowTitle(false)
            .setShowAlbum(false)
            .setNeedCrop(false)
            .setCornerColor(Color.parseColor("#FFFFFF"))
            .setLineColor(Color.parseColor("#e631436c"))
            .setLineSpeed(QrConfig.LINE_MEDIUM)
            .setScanType(scan_type)
            .setScanViewType(scan_view_type)
            .setCustombarcodeformat(QrConfig.BARCODE_PDF417)
            .setPlaySound(true)
            .setDingPath(R.raw.qrcode)
            .setIsOnlyCenter(false)
            .setTitleText("")
            .setTitleBackgroudColor(Color.parseColor("#FFFFFF"))
            .setTitleTextColor(Color.WHITE)
            .setShowZoom(false)
            .setAutoZoom(false)
            .setFingerZoom(false)
            .setDoubleEngine(true)
            .setScreenOrientation(screen)
            .setOpenAlbumText("")
            .setLooperScan(false)
            .setLooperWaitTime(5 * 1000)
            .setScanLineStyle(line_style)
            .setAutoLight(false)
            .setShowVibrator(true)
            .create()
        initView(view)
    }

    private fun initView(view: View) {
        cp = view.findViewById<View>(cn.bertsir.zbar.R.id.cp) as CameraPreview
        //bi~
        soundPool = SoundPool(10, AudioManager.STREAM_SYSTEM, 5)

        viewBind.vsbZoom.setVisibility(if (options!!.isShow_zoom) View.VISIBLE else View.GONE)
        viewBind.sv.setCornerColor(options!!.corneR_COLOR)
        viewBind.sv.setLineSpeed(options!!.getLine_speed())
        viewBind.sv.setLineColor(options!!.linE_COLOR)
        viewBind.sv.setScanLineStyle(options!!.getLine_style())
//        viewBind.ivClose.setOnClickListener(this)
        initParam()
    }
    override
    fun onClick(v: View) {
        if (v.id == cn.bertsir.zbar.R.id.iv_album) {
//            fromAlbum()
        } else if (v.id == R.id.iv_flash) {
            if (cp != null) {
                cp!!.setFlash()
            }
        }/* else if (v.id == R.id.iv_close) {
//            setFragmentResult()
//            setResult(com.metawatch.app.ui.device.CustomQRActivity.RESULT_CANCELED) //兼容混合开发
//            finish()
            findNavController().popBackStack()
        }*/
    }

    override fun onResume() {
        super.onResume()
        if (cp != null) {
            cp!!.setScanCallback(resultCallback)
            cp!!.start()
        }

        if (sensorManager != null) {
            //一般在Resume方法中注册
            /**
             * 第三个参数决定传感器信息更新速度
             * SensorManager.SENSOR_DELAY_NORMAL:一般
             * SENSOR_DELAY_FASTEST:最快
             * SENSOR_DELAY_GAME:比较快,适合游戏
             * SENSOR_DELAY_UI:慢
             */
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun initParam() {
        when (options!!.screeN_ORIENTATION) {
            QrConfig.SCREEN_LANDSCAPE -> activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            QrConfig.SCREEN_PORTRAIT -> activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            QrConfig.SCREEN_SENSOR -> activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            else -> activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        Symbol.scanType = options!!.getScan_type()
        Symbol.scanFormat = options!!.getCustombarcodeformat()
        Symbol.is_only_scan_center = options!!.isOnly_center
        Symbol.is_auto_zoom = options!!.isAuto_zoom
        Symbol.doubleEngine = options!!.isDouble_engine
        Symbol.looperScan = options!!.isLoop_scan
        Symbol.looperWaitTime = options!!.getLoop_wait_time()
        Symbol.screenWidth = QRUtils.getInstance().getScreenWidth(requireContext())
        Symbol.screenHeight = QRUtils.getInstance().getScreenHeight(requireContext())
        if (options!!.isAuto_light) {
            getSensorManager()
        }
    }

    /**
     * 获取光线传感器
     */
    fun getSensorManager() {
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager != null) {
            sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        }
    }

    /**
     * 识别结果回调
     */
    private val resultCallback = ScanCallback { result ->
        if (options!!.isPlay_sound) {
            soundPool?.play(1, 1f, 1f, 0, 0, 1f)
        }
        if (options!!.isShow_vibrator) {
            QRUtils.getInstance().getVibrator(requireContext().applicationContext)
        }
        if (cp != null) {
            cp!!.setFlash(false)
        }
//        this::class.simpleName?.let { Timber.tag(it).i("scanResult=$result") }
        setFragmentResult(DeviceBindFragment.DEVICE_QR_CODE, Bundle().apply {
            putSerializable(DeviceBindFragment.EXTRA_SCAN_RESULT, result)
        })
        findNavController().popBackStack()
    }
    companion object {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val light = event!!.values[0]
        if (light < AUTOLIGHTMIN) { //暂定值
            if (cp!!.isPreviewStart) {
                cp!!.setFlash(true)
                sensorManager!!.unregisterListener(this, sensor)
                sensor = null
                sensorManager = null
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

}