package com.sjbt.sdk.sample.ui.device.dial.diyDial

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup.GONE
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.base.api.UNIWatchMate
import com.base.sdk.port.FileType
import com.base.sdk.port.WmTransferState
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.obsez.android.lib.filechooser.internals.UiUtil
import com.shenju.cameracapturer.OSIJni
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentDiyDialBinding
import com.sjbt.sdk.sample.ui.device.dial.CustomDialActivity
import com.sjbt.sdk.sample.ui.device.dial.CustomJsonBean
import com.sjbt.sdk.sample.ui.device.dial.MediaGridActivity
import com.sjbt.sdk.sample.ui.device.dial.diyDial.editVideo.CropVideoUtils
import com.sjbt.sdk.sample.ui.device.dial.diyDial.editVideo.DateTimeUtils
import com.sjbt.sdk.sample.utils.DensityUtil
import com.sjbt.sdk.sample.utils.GlideImageLoader
import com.sjbt.sdk.sample.utils.PermissionHelper
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.widget.LoadingView
import com.transsion.imagepicker.ImagePicker
import com.transsion.imagepicker.ImagePicker.REQUEST_CODE_SELECT_PATH
import com.transsion.imagepicker.ImagePicker.RESULT_CODE_ITEMS
import com.transsion.imagepicker.PickerConfig
import com.transsion.imagepicker.PickerConfig.EXTRAS_TAKE_PICKERS
import com.transsion.imagepicker.bean.ImageItem
import com.transsion.imagepicker.ui.ImageCropActivity
import com.transsion.imagepicker.ui.ImageGridActivity
import com.transsion.imagepicker.view.CropImageView
import com.transsion.oraimohealth.widget.CustomVideoDialView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 * Use the [DiyDialFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DiyDialFragment : BaseFragment(R.layout.fragment_diy_dial) {

    companion object {
        const val TAG = "DiyDialFragment"
        const val SELECT_PHOTO = 11010
        const val CROP_IMAGE_RESULT = 10198

        const val CUSTOM_VIDEO_FILE_NAME = "/frame_%04d.jpg"
        const val CUSTOM_BASE = "customDial"
        const val CUSTOM_DIAL_VIDEO = "$CUSTOM_BASE/base_custom_video.json"
        const val CUSTOM_DIAL_IMAGE = "$CUSTOM_BASE/base_custom_image.json"

        const val CUSTOM_TOP_LEFT = "$CUSTOM_BASE/custom_dial_top_left.json"
        const val CUSTOM_TOP_RIGHT = "$CUSTOM_BASE/custom_dial_top_right.json"
        const val CUSTOM_BOTTOM_LEFT = "$CUSTOM_BASE/custom_dial_bottom_left.json"
        const val CUSTOM_BOTTOM_RIGHT = "$CUSTOM_BASE/custom_dial_bottom_right.json"

        const val CUSTOM_PREVIEW = "preview.jpg"
        const val CUSTOM_BACKGROUND = "background.jpg"
        const val CUSTOM_BACKGROUND_ = "bg"

        const val CUSTOM_CONFIG = "config.json"
        const val CUSTOM_DIR = "customDial/"

        const val VIDEO_SEND_FRAMES = 20

    }

    private var customMap: MutableMap<String, Any> = hashMapOf()
    private val viewBinding: FragmentDiyDialBinding by viewBinding()
    private var videoUri: Uri? = null

    private var videoSelectW = 0
    private var videoSelectH = 0
    private var videoScaleW = 0
    private var videoScaleH = 0
    private var startVideoTime: Long = 0
    private var endVideoTime: Long = 0
    private var selectselectPath: String? = null
    private var videoSelectMatrix: Matrix? = null
    private var marginTop = 0
    private var marginLeft = 0
    private var touchX = 0f
    private var touchY = 0f

    private var deviceW = 0
    private var deviceH = 0

    private var croppedBitmap: Bitmap? = null

    private var timeShowTypeAdapter: DiyDialTimeShowTypeAdapter? = null

    private var currPos = 0

    var selectPath = ""
    var isVideo = false
    var currColorStr = "#ffffff"

    private lateinit var colorPickerView: ColorPickerView

    private lateinit var imagePicker: ImagePicker
    private lateinit var loadingView: LoadingView

    private val time_position_imgs = mutableListOf<Int>(
        R.mipmap.ic_dial_time_pos_upper_left_n,
        R.mipmap.ic_dial_time_pos_lower_left_n,
        R.mipmap.ic_dial_time_pos_upper_right_n,
        R.mipmap.ic_dial_time_pos_lower_right_n
    )


    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 设置屏幕常亮
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        viewBinding.tvSelectPhoto.setOnClickListener {
            // 选择图片
//            val imageIntent = Intent(Intent.ACTION_PICK)
//            imageIntent.type = "image/* video/*"
//            startActivityForResult(imageIntent, SELECT_PHOTO)

            PermissionHelper.requestAppStorage(this@DiyDialFragment) {
                if (it) {
                    viewBinding.tvSelectPhoto.visibility = View.GONE
                    ImagePicker.getInstance().isShowCamera = false
                    val intent = Intent(requireActivity(), MediaGridActivity::class.java)
                    intent.putExtra(EXTRAS_TAKE_PICKERS, false)
                    // 是否是直接打开相机
                    startForResult.launch(intent)
                } else {
                    ToastUtil.showToast(getString(R.string.permission_fail));
                }
            }

        }

        viewLifecycle.launchRepeatOnStarted {
            launch {
                if (deviceW != 0 || deviceH != 0)
                    return@launch
                LogUtils.e(TAG, "$TAG launch")
                val it = UNIWatchMate.getDeviceInfo().await()
                it.screen.split("h").let {
                    deviceW = it[0].replace("w", "").toInt()
                    deviceH = it[1].toInt()
                    val params = ConstraintLayout.LayoutParams(
                        (deviceW * 1.5f).toInt(),
                        (deviceH * 1.5f).toInt()
                    )
                    params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                    params.setMargins(0, UiUtil.dip2px(10), 0, 0)

                    viewBinding.cardView.layoutParams = params

                    loadingView = viewBinding.root.findViewById(R.id.loading_view)

                    viewBinding.ivDiyDialTime.setImageResource(time_position_imgs[0])
                    initTimeShowTyPeAdapter()
                    colorPicker()

                    initImagePicker();

                    viewBinding.btInstallDial.setOnClickListener {

                        if (selectPath.isNullOrEmpty()) {
                            ToastUtil.showToast(getString(R.string.select_photo))
                            return@setOnClickListener
                        }

                        loadingView.visibility = View.VISIBLE
                        loadingView.showLoading(getString(R.string.generate_dial))
                        Observable.create(ObservableOnSubscribe<Any> {
                            val isDialImageGenerate = generateDialFile()

                            val isDialBgGenerate = if (isVideo) generateDialVideo() else true
                            if (isDialBgGenerate && isDialImageGenerate) {
                                val osiJni = OSIJni()
                                val fileArray: ByteArray = osiJni.makeDialNative(
                                    "${requireContext().filesDir}/$CUSTOM_DIR",
                                    deviceW,
                                    deviceH
                                )
                                it.onNext(fileArray)
                            } else {
                                it.onNext("File format exception!")
                            }
                        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ it ->
                                when (it) {
                                    is String -> {
                                        ToastUtil.showToast(it)
                                        loadingView.visibility = GONE
                                    }

                                    is ByteArray -> {
                                        val dialFilePath = saveDialByteArrayToFile(it, "dial.dial")
                                        loadingView.showLoading(
                                            String.format(
                                                getString(R.string.install_dial_progress),
                                                0
                                            ) + "%"
                                        )
                                        var coverPath =
                                            MyApplication.instance.filesDir.absolutePath + "/" + System.currentTimeMillis() + ".jpg"
                                        val dialCoverArray =
                                            UNIWatchMate.wmApps.appDial.parseDialThumpJpg(
                                                dialFilePath!!
                                            )
                                        FileIOUtils.writeFileFromBytesByChannel(
                                            coverPath,
                                            dialCoverArray,
                                            true
                                        )

                                        val coverList = mutableListOf<File>()
                                        val dialList = mutableListOf<File>()
                                        coverList.add(File(coverPath))
                                        dialList.add(File(dialFilePath))
                                        UNIWatchMate.wmTransferFile.startTransfer(
                                            FileType.DIAL_COVER, coverList,
                                            dialList[0].length().toInt()
                                        ).subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(object :
                                                Observer<WmTransferState> {
                                                override fun onSubscribe(d: Disposable) {
                                                }

                                                override fun onError(e: Throwable) {

                                                }

                                                override fun onComplete() {
                                                    UNIWatchMate.wmTransferFile.startTransfer(
                                                        FileType.DIAL,
                                                        dialList
                                                    ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(object :Observer<WmTransferState>{
                                                        override fun onSubscribe(d: Disposable) {
                                                        }

                                                        override fun onError(e: Throwable) {
                                                        }

                                                        override fun onComplete() {
                                                            loadingView.showLoading(getString(R.string.install_dial_success))
                                                            Observable.timer(2, TimeUnit.SECONDS)
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe {
                                                                    loadingView.visibility =
                                                                        View.GONE
                                                                }
                                                        }

                                                        override fun onNext(it2: WmTransferState) {
                                                            loadingView.showLoading(
                                                                String.format(
                                                                    getString(R.string.install_dial_progress),
                                                                    it2.progress
                                                                ) + "%"
                                                            )
                                                        }
                                                    })
                                                }

                                                override fun onNext(t: WmTransferState) {

                                                }
                                            })

                                    }

                                    is Error -> {

                                    }
                                }
                            }, { error ->
                                LogUtils.e("error : ${error.printStackTrace()}")
                            })
                    }
                }
            }
        }
    }

    private fun colorPicker() {
        colorPickerView = viewBinding.root.findViewById(R.id.color_picker_view)
        colorPickerView.setOnColorChangedListener {
            val currColor = Color.HSVToColor(it)
            currColorStr = String.format("#%06X", 0xFFFFFF and currColor)
            viewBinding.ivDiyDialTime.setColorFilter(Color.parseColor(currColorStr))
            timeShowTypeAdapter?.setCurrColor(currColorStr)
        }
    }

    /**
     * 初始化
     */
    private fun initImagePicker() {
        imagePicker = ImagePicker.getInstance()
        imagePicker.imageLoader = GlideImageLoader()
        imagePicker.isCrop = true //允许裁剪（单选才有效）
        imagePicker.selectLimit = 1 //选中数量限制
        imagePicker.isMultiMode = false
        val width = (DensityUtil.getScreenWidth(requireContext()) * 0.6f).toInt()
        imagePicker.focusWidth = width //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.focusHeight =
            (width * deviceH / deviceW.toFloat()).toInt() //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.outPutX = deviceW
        imagePicker.outPutY = deviceH
        if (deviceW == deviceH) {
            val max: Int = Math.max(imagePicker.outPutX, imagePicker.outPutY)
            imagePicker.outPutX = max
            imagePicker.outPutY = max
        }
        //裁剪框的形状
        imagePicker.style =
            if (deviceW == deviceH) CropImageView.Style.CIRCLE else CropImageView.Style.RECTANGLE
    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PHOTO) {

        }
    }

    @SuppressLint("Range")
    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->

        // 处理返回的结果
        if (result.resultCode == CustomDialActivity.RESULT_CODE_EDIT_VIDEO) {
            // 成功的结果处理
            result.data?.let {
                if (it.hasExtra(CustomDialActivity.CUSTOM_VIDEO)) {
                    videoUri =
                        it.getParcelableExtra(CustomDialActivity.VIDEO_URI)
                    customMap.clear()
                    customMap =
                        it.extras?.get(CustomDialActivity.CUSTOM_VIDEO) as MutableMap<String, Any>

                    if (customMap != null) {
                        val vMatrixArray = customMap["VIDEO_SELECT_MATRIX"] as FloatArray?
                        // 将FloatArray转换为Matrix对象
                        // 将FloatArray转换为Matrix对象
                        videoSelectMatrix = Matrix()
                        videoSelectMatrix!!.setValues(vMatrixArray)

                        if (videoSelectMatrix == null) {
                            return@let
                        }

                        marginLeft = customMap["MARGIN_LEFT"] as Int
                        marginTop = customMap["MARGIN_TOP"] as Int

                        selectselectPath = customMap["VIDEO_SELECT_BITMAP_PATH"] as String?

                        videoSelectW = customMap["CROP_W"] as Int
                        videoSelectH = customMap["CROP_H"] as Int

                        videoScaleW = customMap["VIDEO_SCALE_W"] as Int
                        videoScaleH = customMap["VIDEO_SCALE_H"] as Int

                        startVideoTime = customMap["LEFT_PROGRESS"] as Long
                        endVideoTime = customMap["RIGHT_PROGRESS"] as Long


                        touchX = customMap["TOUCH_X"] as Float
                        touchY = customMap["TOUCH_Y"] as Float

                        viewBinding.flVideo.removeAllViews()

                        val customDialVideo = CustomVideoDialView(requireContext())
                        viewBinding.flVideo.addView(customDialVideo)

                        customDialVideo.setVideoUri(
                            -1,
                            videoUri!!,
                            videoScaleW,
                            videoScaleH,
                            startVideoTime,
                            endVideoTime,
                            (deviceH * 1.5f) / videoSelectH,
                            videoSelectMatrix!!,
                            marginLeft,
                            marginTop
                        )


                        val matrixValues2 = FloatArray(9)
                        videoSelectMatrix!!.getValues(matrixValues2)
                        val currX = matrixValues2[Matrix.MTRANS_X]
                        val currY = matrixValues2[Matrix.MTRANS_Y]

                        Observable.create<Bitmap> { emitter ->
                            var bitmap: Bitmap? = null
                            try {
                                bitmap = Glide.with(viewBinding.ivDiyDial).asBitmap()
                                    .override(videoScaleW / 2, videoScaleH / 2)
                                    .load(selectselectPath)
                                    .skipMemoryCache(true).submit().get()
                            } catch (e: Exception) {
                                emitter.onNext(null)
                            }
                            emitter.onNext(bitmap)
                        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe { bitmap: Bitmap? ->
                                if (bitmap != null) {
                                    var x = 0
                                    var y = 0
                                    x = if (currX < 0) {
                                        Math.abs(currX.toInt()) + marginLeft
                                    } else {
                                        marginLeft - currX.toInt()
                                    }
                                    y = if (currY < 0) {
                                        Math.abs(currY.toInt()) + marginTop
                                    } else {
                                        marginTop - currY.toInt()
                                    }
                                    if (croppedBitmap != null) {
                                        croppedBitmap = null
                                    }

                                    // 使用 createBitmap 方法裁剪 Bitmap
                                    croppedBitmap = Bitmap.createBitmap(
                                        bitmap,
                                        x / 2,
                                        y / 2,
                                        videoSelectW / 2,
                                        videoSelectH / 2
                                    )
                                    if (bitmap != null && !bitmap.isRecycled) {
                                        bitmap?.recycle()
                                    }
                                    viewBinding.ivDiyDial.setImageBitmap(croppedBitmap)
                                    timeShowTypeAdapter?.setBitmap(croppedBitmap)
                                }
                            }
                    }
                } else {
                    // 失败的结果处理
                    ToastUtil.showToast(getString(R.string.video_load_fail))
                }
            }
        } else if (result.resultCode == RESULT_CODE_ITEMS) {
            result.data.let {
                val list: ArrayList<ImageItem> =
                    it!!.extras?.get(ImagePicker.EXTRA_RESULT_ITEMS) as ArrayList<ImageItem>
                if (list == null || list.isEmpty()) {
                    return@let
                }
                val imageItem = list[0]
                viewBinding.flVideo.removeAllViews()
                viewBinding.flVideo.visibility = GONE
                Glide.with(viewBinding.ivDiyDial).asBitmap().load(imageItem.path).into(object :
                    CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        croppedBitmap = resource
                        viewBinding.ivDiyDial.setImageBitmap(croppedBitmap)
                        timeShowTypeAdapter?.setBitmap(croppedBitmap)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        ToastUtil.showToast(getString(R.string.video_load_fail))
                    }
                })
            }
        } else if (result.resultCode == REQUEST_CODE_SELECT_PATH) {
            result.data.let {
                selectPath = it!!.extras?.get("selectPath") as String
                isVideo = it!!.extras?.get("isVideo") as Boolean

                // 获取私有目录路径
                val videoAssetsPath: String =
                    requireContext().filesDir.absolutePath + File.separator + CUSTOM_DIR + File.separator + CUSTOM_BACKGROUND_
                val videoAssetsFile = File(videoAssetsPath)
                if (videoAssetsFile.exists()) {
                    videoAssetsFile.deleteRecursively()
                }
                setCrop()
            }
        }
    }


    private fun setCrop() {
        if (isVideo) {
            isVideo = true
            // 使用 path
            val intent = Intent(context, CustomDialActivity::class.java)
            intent.putExtra("selectPath", selectPath)
            startForResult.launch(intent);
        } else {
            isVideo = false
            val intent =
                Intent(requireContext(), ImageCropActivity::class.java)
            val imageItem = ImageItem()
            imageItem.path = selectPath
            imagePicker.selectedImages = arrayListOf(imageItem)
            startForResult.launch(intent);
        }
    }

    private fun initTimeShowTyPeAdapter() {

        val params = ConstraintLayout.LayoutParams(
            LayoutParams.MATCH_PARENT,
            deviceH
        )
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        params.topToBottom = viewBinding.cardView.id
        params.setMargins(0, UiUtil.dip2px(5), 0, 0)

        viewBinding.recycleTimeShowType.layoutParams = params

        timeShowTypeAdapter = DiyDialTimeShowTypeAdapter(
            requireContext(),
            R.layout.item_diy_dial_time_show_type_layout,
            time_position_imgs,
            deviceW,
            deviceH
        )

        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        viewBinding.recycleTimeShowType.layoutManager = linearLayoutManager
        // 设置一次性加载4个Item
        viewBinding.recycleTimeShowType.layoutManager?.isAutoMeasureEnabled = false
        val recycledViewPoolVideo = RecyclerView.RecycledViewPool()
        recycledViewPoolVideo.setMaxRecycledViews(0, 4)
        viewBinding.recycleTimeShowType.setRecycledViewPool(recycledViewPoolVideo)

        viewBinding.recycleTimeShowType.adapter = timeShowTypeAdapter
        timeShowTypeAdapter?.setOnItemClickListener { adapter, view, position ->
            viewBinding.ivDiyDialTime.setImageResource(time_position_imgs[position])
            currPos = position
        }
    }


    private fun generateDialVideo(): Boolean {
        // 获取私有目录路径

        // 获取私有目录路径
        val privateDirectory: String =
            requireContext().filesDir.absolutePath + File.separator + CUSTOM_DIR
        val privateDirectoryFile = File(privateDirectory)
        if (!privateDirectoryFile.exists()) {
            privateDirectoryFile.mkdirs()
        }

        val outputFile = File(privateDirectoryFile, CUSTOM_BACKGROUND_)
        if (!outputFile.exists()) {
            outputFile.mkdirs()
        }
        val code: Int = CropVideoUtils.cropFrames(
            selectPath!!,
            outputFile.absolutePath + File.separator + CUSTOM_VIDEO_FILE_NAME,
            touchX,
            touchY,
            DateTimeUtils.formatMilliseconds(startVideoTime)!!,
            VIDEO_SEND_FRAMES,
            videoSelectW,
            videoSelectH,
            videoScaleW,
            videoScaleH,
            deviceW,
            deviceH
        )
        return code == 1
    }


    private fun generateDialFile(): Boolean {
        var previewBmp: Bitmap? = null
        var backBmp: Bitmap? = null
        timeShowTypeAdapter?.getViewByPosition(
            viewBinding.recycleTimeShowType,
            currPos,
            R.id.card_view
        )?.let {
            val cardView = it.findViewById<CardView>(R.id.card_view)
            //表盘列表显示
            cardView.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
            cardView.isDrawingCacheEnabled = true
            previewBmp = cardView.drawingCache
        }
        timeShowTypeAdapter?.getViewByPosition(viewBinding.recycleTimeShowType, currPos, R.id.iv_bg)
            ?.let {
                val ivBg = it.findViewById<ImageView>(R.id.iv_bg)
                //表盘列表显示
                ivBg.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
                ivBg.isDrawingCacheEnabled = true
                backBmp = ivBg.drawingCache
            }

        val customDial = File(requireContext().filesDir, CUSTOM_DIR)
        if (customDial.exists()) {
            customDial.delete()
        }
        customDial.mkdir()

        storeFile(CUSTOM_BACKGROUND, backBmp!!)
        storeFile(CUSTOM_PREVIEW, previewBmp!!)

        var customBaseStyle: String = CUSTOM_DIAL_IMAGE
        if (isVideo) {
            customBaseStyle = CUSTOM_DIAL_VIDEO
        }

        val baseCustomJson: String = getCustomJson(customBaseStyle, true)!!
        var customStyle = ""
        when (currPos) {
            0 -> customStyle = CUSTOM_TOP_LEFT
            1 -> customStyle = CUSTOM_BOTTOM_LEFT
            2 -> customStyle = CUSTOM_TOP_RIGHT
            3 -> customStyle = CUSTOM_BOTTOM_RIGHT
        }

        try {
            val customStyleJson: String = getCustomJson(customStyle, false)!!

            val jsonObject = JSONObject(baseCustomJson)
            val jsonArray = JSONArray(customStyleJson)
            jsonObject.put("active", jsonArray)
            val bean: CustomJsonBean =
                Gson().fromJson(jsonObject.toString(), CustomJsonBean::class.java)
            bean.dial?.provider = "USER-A"
            bean.dial?.create = DateTimeUtils.getCurrDataTime()
            bean.dial?.lastupdate = DateTimeUtils.getCurrDataTime()
            bean.dial?.width = intArrayOf(deviceW, deviceW)
            bean.dial?.height = intArrayOf(deviceH, deviceH)
            bean.dial?.uuid = UUID.randomUUID().toString().replace("-", "")

            for (i in 0 until bean.active?.size!!) {
                val active: CustomJsonBean.Active = bean.active?.get(i)!!
                active.font_color = hexToRgb(currColorStr)
            }
            val configJson = Gson().toJson(bean)
            storeFile(CUSTOM_CONFIG, configJson)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }


    @Synchronized
    private fun storeFile(fileName: String, data: Any): String? {
        // 获取私有目录路径
        val privateDirectory = "${requireContext().filesDir.absolutePath}/$CUSTOM_DIR"
        val outputFile = File(privateDirectory, fileName)
        return if (data is Bitmap) {
            //表盘内显示
            val scaledBitmap =
                Bitmap.createScaledBitmap(data, deviceW, deviceH, true)
            try {
                val outputStream = FileOutputStream(outputFile)
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                privateDirectory
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                ""
            }
        } else {
            // JSON数据
            try {
                // 写入数据到文件
                val fileOutputStream = FileOutputStream(outputFile)
                val outputStreamWriter = OutputStreamWriter(fileOutputStream)
                outputStreamWriter.write(data as String)
                outputStreamWriter.close()
                fileOutputStream.close()
                // 数据写入成功
                println("JSON数据已保存到私有目录")
                privateDirectory
            } catch (e: IOException) {
                e.printStackTrace()
                // 处理异常
                ""
            }
        }
    }

    private fun getCustomJson(path: String, isBase: Boolean): String? {
        val assetManager: AssetManager = requireContext().assets
        try {
            assetManager.open(path).use { inputStream ->
                val reader =
                    BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                val jsonString = stringBuilder.toString()
                val tokener = JSONTokener(jsonString)
                return if (!isBase) {
                    val jsonObject = JSONArray(tokener)
                    jsonObject.toString()
                } else {
                    val jsonObject = JSONObject(tokener)
                    jsonObject.toString()
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            // Handle the exception...
            return null
        }
    }

    private fun hexToRgb(colorStr: String): IntArray? {
        val rgb = IntArray(4)
        if (colorStr.length == 7) {
            rgb[2] = Integer.valueOf(colorStr.substring(1, 3), 16)
            rgb[1] = Integer.valueOf(colorStr.substring(3, 5), 16)
            rgb[0] = Integer.valueOf(colorStr.substring(5, 7), 16)
            rgb[3] = 255
        } else if (colorStr.length == 4) {
            val intValue = colorStr.substring(1).toInt(16)
            rgb[2] = intValue shr 16 and 0xFF
            rgb[1] = intValue shr 8 and 0xFF
            rgb[0] = intValue and 0xFF
            rgb[3] = 255
            return rgb
        }
        return rgb
    }

    private fun saveDialByteArrayToFile(data: ByteArray?, fileName: String?): String? {
        val file = File(requireContext().filesDir, fileName)
        if (file.exists()) {
            file.delete() // 删除已存在文件
        }
        try {
            val fos = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE)
            fos.write(data)
            fos.close()
            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    override fun onDestroyView() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroyView()
    }

}