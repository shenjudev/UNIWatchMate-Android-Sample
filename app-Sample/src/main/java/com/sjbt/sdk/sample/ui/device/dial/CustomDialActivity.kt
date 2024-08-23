package com.sjbt.sdk.sample.ui.device.dial

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.base.api.UNIWatchMate
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseActivity
import com.sjbt.sdk.sample.databinding.ActivityCustomDialBinding
import com.sjbt.sdk.sample.ui.device.dial.diyDial.editVideo.EditSpacingItemDecoration
import com.sjbt.sdk.sample.ui.device.dial.diyDial.editVideo.ExtractFrameWorkThread
import com.sjbt.sdk.sample.ui.device.dial.diyDial.editVideo.ExtractVideoInfoUtil
import com.sjbt.sdk.sample.ui.device.dial.diyDial.editVideo.PictureUtils
import com.sjbt.sdk.sample.ui.device.dial.diyDial.editVideo.VideoEditAdapter
import com.sjbt.sdk.sample.ui.device.dial.diyDial.editVideo.VideoEditInfo
import com.sjbt.sdk.sample.utils.DensityUtil
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.widget.RangeSeekBar
import com.transsion.oraimohealth.widget.CustomVideoView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import java.io.File
import java.lang.ref.WeakReference
import java.util.HashMap
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class CustomDialActivity : BaseActivity() {

    companion object {
        const val TAG = "CustomDialActivity"

        val MIN_CUT_DURATION = 5 * 1000L // 最小剪辑时间2s
        val VIDEO_URI = "video_uri"
        val CUSTOM_VIDEO = "custom_video"

        const val RESULT_CODE_EDIT_VIDEO = 10086

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

        const val VIDEO_EDIT_MAX_TIME = 5
        const val VIDEO_SEND_FRAMES = 20

        fun launchCustomDialActivity(context: Context?,videoPath:String) {
            context?.let {
                val intent = Intent(context, CustomDialActivity::class.java)
                intent.putExtra("videoPath",videoPath)
                it.startActivity(intent)
            }
        }
    }

    private lateinit var binding: ActivityCustomDialBinding

    private var deviceW = 0
    private var deviceH = 0
    private var cropW = 0
    private var cropH = 0
    private var scale = 0f

    private var videoPath = ""
    private var firstVideo = true
    private var itemScale: Float = 0f
    private var customVideo: CustomVideoView? = null
    private var viewW = 0
    private var viewH = 0


    private var leftProgress: Long = 0
    private var rightProgress: Long = 0
    private var videoDuration = 0
    private var mOriginalWidth = 0
    private var mOriginalHeight = 0

    private var videoSelectBitmapPath: String? = null
    private var videoSelectMatrix: Matrix? = null

    private var editSpacingItemDecoration: EditSpacingItemDecoration? = null
    private var videoUri:Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomDialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.ds_dial_custom)

        videoPath = intent.getStringExtra("selectPath") ?: ""

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoPath) // 替换为你的视频文件路径
        val duration =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        if (duration != null) {
            val durationInMs = duration.toInt()
            // 你可以根据需要处理这个时长，例如显示它
            // 打印时长和大小
            LogUtils.d(
                "Video Info",
                "Path: $videoPath, Duration: $duration ms"
            )
            if (durationInMs <= 0) {
                return
            }
        }


        lifecycle.launchRepeatOnStarted {
            launch {
                val it = UNIWatchMate.getDeviceInfo().await()
                it.screen.split("h").let {
                    deviceW = it[0].replace("w","").toInt()
                    deviceH = it[1].toInt()
                    setUIData()
                }
            }
        }

        binding.tvFinish.setOnClickListener {
            if (!TextUtils.isEmpty(videoPath)) {
                videoSelectBitmapPath = videoEditAdapter?.getItemPath(position)
                videoSelectMatrix = customVideo?.mMatrix
                setResultFinish(videoUri!!)
            }
        }
    }

    private fun setUIData() {
        val dHeight = (DensityUtil.getScreenHeight(this)).div(2.5).toFloat()
        val dWidth = DensityUtil.getScreenWidth(this)
        // 计算目标高度

        val targetAspectRatio = deviceW.div(deviceH.toFloat())

        // 计算目标高度
        val targetHeight = Math.min(dHeight, dWidth.div(targetAspectRatio))
        // 计算目标宽度
        val targetWidth = targetHeight * targetAspectRatio
        var finalWidth = 0
        var finalHeight = 0
        // 如果目标宽度超过设备屏幕宽度，则使用设备屏幕宽度重新计算目标高度
        if (targetWidth > dWidth) {
            // 重新计算目标高度
            val adjustedTargetHeight = dWidth / targetAspectRatio
            // 使用重新计算的高度
            val adjustedTargetWidth = adjustedTargetHeight * targetAspectRatio

            // 最终宽高
            finalWidth = adjustedTargetWidth.toInt()
            finalHeight = adjustedTargetHeight.toInt()
        } else {
            // 如果目标宽度未超过设备屏幕宽度，则使用原始计算的宽度和高度
            finalWidth = targetWidth.toInt()
            finalHeight = targetHeight.toInt()
        }
        // 在这里使用 finalWidth 和 finalHeight 进行相应的布局或显示操作
        viewW = finalWidth
        viewH = finalHeight

        val itemWidth = (DensityUtil.getScreenWidth(this)).div(3).toFloat()
        itemScale = itemWidth.div(finalWidth)

        var params = FrameLayout.LayoutParams(
            viewW,
            viewH
        )

        cropW = viewW
        cropH = viewH
        params.gravity = Gravity.CENTER
        binding.cropImageCoverViewHole.layoutParams = params

        if (!TextUtils.isEmpty(videoPath)) {
            val file = File(videoPath)
            videoUri = Uri.fromFile(file)
            LogUtils.e(TAG, "Uri VideoPath:${videoUri!!.path}")
            setEditVideoPath(videoUri!!)
        }

    }

    private fun setEditVideoPath(videoUri: Uri) {
        binding.flVideo.visibility = View.VISIBLE
        if (customVideo != null){
            customVideo?.release()
        }
        binding.flVideo.removeAllViews()

        customVideo = CustomVideoView(this)


        customVideo?.setVideoUri(
            0,
            videoUri,
            binding.flVideo.layoutParams.width,
            binding.flVideo.layoutParams.height,
            true,
            mOriginalWidth,
            mOriginalHeight,
            cropW,
            cropH,
            scale
        )
        binding.flVideo.addView(customVideo)

        getVideoThumbnail()

        customVideo?.setVideoListener(object :
            CustomVideoView.VideoListener {

            override fun onTouchXY(matrix: Matrix) {
                val matrixValues = FloatArray(9)
                matrix.getValues(matrixValues)

                // 获取平移分量
                val transX = matrixValues[Matrix.MTRANS_X] - customVideo?.xOri!!
                val transY = matrixValues[Matrix.MTRANS_Y] - customVideo?.yOri!!

                customVideo?.touchX = transX
                customVideo?.touchY = transY
            }

            override fun onSeekTo(left: Long, right: Long) {
                runOnUiThread {

                    Observable.timer(300, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            anim()
                        }
                }
            }

            override fun onSeekToAnim(left: Long, right: Long) {
                runOnUiThread {
                    Observable.timer(300, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            anim()
                        }
                }
            }
        })
    }

    private var index: Int = 0

    private var videoEditAdapter: VideoEditAdapter? = null
    private var isOverScaledTouchSlop = false
    private var position = 0
    private var lastScrollX = 0
    private var mScaledTouchSlop = 0
    private var scrollPos: Long = 0
    private var averageMsPx //每毫秒所占的px
            = 0f
    private var averagePxMs //每px所占用的ms毫秒
            = 0f
    private var oldTime: Long = 0

    private var mExtractVideoInfoUtil: ExtractVideoInfoUtil? = null
    private var duration: Long = 0

    //private static final long MAX_CUT_DURATION = 30 * 1000L;//视频最多剪切多长时间
    private val MAX_CUT_DURATION = 5 * 1000L //视频最多剪切多长时间

    private val MAX_COUNT_RANGE = 10 //seekBar的区域内一共有多少张图片

    private var mMaxWidth = 0

    private var seekBar: RangeSeekBar? = null

    private var mExtractFrameWorkThread: ExtractFrameWorkThread? = null

    private var OutPutFileDirPath: String? = null

    private var firstStartVideo = true
    private fun getVideoThumbnail() {
        isOverScaledTouchSlop = false
        position = 0
        lastScrollX = 0
        mScaledTouchSlop = 0
        scrollPos = 0
        averageMsPx //每毫秒所占的px
        averagePxMs //每px所占用的ms毫秒
        leftProgress = 0
        rightProgress = 0
        oldTime = 0
        mMaxWidth = 0
        firstStartVideo = true

        binding.idRvId.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        videoEditAdapter = VideoEditAdapter(
            this,
            (DensityUtil.getScreenWidth(this) - DensityUtil.dip2px(28F)) / 10
        )
        binding.idRvId.adapter = videoEditAdapter

        binding.idRvId.addOnScrollListener(mOnScrollListener)

//        customVideo?.mMediaPlayer!!.setOnErrorListener { mediaPlayer, i, i2 ->
//            false
//        }

        if (Build.MANUFACTURER == PHONE_OnePlus) {
            oldTime = System.currentTimeMillis()
        }

        initEditVideo();
    }

    private fun initEditVideo() {
        try {
            mExtractVideoInfoUtil =
                ExtractVideoInfoUtil(
                    videoPath
                )
            duration = mExtractVideoInfoUtil?.videoLength!!.toLong()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        mMaxWidth = DensityUtil.getScreenWidth(this) - DensityUtil.dip2px(28f)
        mScaledTouchSlop = ViewConfiguration.get(this).scaledTouchSlop

        //for video edit
        val startPosition: Long = 0
        val endPosition: Long = duration
        val thumbnailsCount: Int
        val rangeWidth: Int
        val isOver_60_s: Boolean
        if (endPosition <= MAX_CUT_DURATION) {
            isOver_60_s = false
            thumbnailsCount = MAX_COUNT_RANGE
            rangeWidth = mMaxWidth
        } else {
            isOver_60_s = true
            thumbnailsCount =
                (endPosition * 1.0f / (MAX_CUT_DURATION * 1.0f) * MAX_COUNT_RANGE).toInt()
            rangeWidth =
                mMaxWidth / MAX_COUNT_RANGE * thumbnailsCount
        }
        if (editSpacingItemDecoration != null) {
            binding.idRvId.removeItemDecoration(editSpacingItemDecoration!!)
            editSpacingItemDecoration = null
        }

        editSpacingItemDecoration = EditSpacingItemDecoration(
            DensityUtil.dip2px(17f),
            thumbnailsCount
        )

        binding.idRvId.addItemDecoration(
            editSpacingItemDecoration!!
        )

        //init seekBar
        if (isOver_60_s) {
            seekBar =
                RangeSeekBar(this, 0L, MAX_CUT_DURATION)
            seekBar?.selectedMinValue = 0L
            seekBar?.selectedMaxValue = MAX_CUT_DURATION
        } else {
            seekBar = RangeSeekBar(this, 0L, endPosition)
            seekBar?.selectedMinValue = 0L
            seekBar?.selectedMaxValue = endPosition
        }
        seekBar?.setMin_cut_time(MIN_CUT_DURATION) //设置最小裁剪时间

        seekBar?.isNotifyWhileDragging = true
        binding.idSeekBarLayout.addView(seekBar)


        averageMsPx = duration * 1.0f / rangeWidth * 1.0f

        OutPutFileDirPath = PictureUtils.getSaveEditThumbnailDir(this)

//        val extractW: Int = (UIUtil.getScreenWidth() - UIUtil.dip2px(
//            this,
//            28
//        )) / MAX_COUNT_RANGE
//        val extractH = UIUtil.dip2px(this, 50)

        videoEditAdapter?.clearData()

        mExtractFrameWorkThread = ExtractFrameWorkThread(
            cropW,
            cropH,
            mUIHandler,
            videoPath,
            OutPutFileDirPath,
            startPosition,
            endPosition,
            thumbnailsCount
        )
        mExtractFrameWorkThread?.start()

        //init pos icon start
        leftProgress = 0
        rightProgress = if (isOver_60_s) {
            MAX_CUT_DURATION
        } else {
            endPosition
        }
//        tvCancel.text = UIUtil.formatDurationTime(rightProgress)
        averagePxMs = mMaxWidth * 1.0f / (rightProgress - leftProgress)
    }

    private val mOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    videoPause()
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    videoStart()
                }
            }

            @SuppressLint("NewApi")
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val scrollX: Int = getScrollXDistance()
                //达不到滑动的距离
                if (abs(lastScrollX - scrollX) < mScaledTouchSlop) {
                    isOverScaledTouchSlop = false
                    return
                }
                isOverScaledTouchSlop = true
                //初始状态,why ? 因为默认的时候有14dp的空白！
                if (scrollX == -DensityUtil.dip2px(14F)) {
                    scrollPos = 0
                } else {
                    scrollPos = (averageMsPx * (DensityUtil.dip2px(
                        14F
                    ) + scrollX)).toLong()

                    leftProgress = seekBar?.selectedMinValue?.plus(scrollPos) ?: 0
                    if (leftProgress < 0) {
                        leftProgress = 0
                    }
                    rightProgress = seekBar?.selectedMaxValue?.plus(scrollPos) ?: 0
                    if (Build.MANUFACTURER == PHONE_OnePlus) {
                        oldTime = System.currentTimeMillis()
                    }
                }
                lastScrollX = scrollX
            }
        }

    /**
     * 水平滑动了多少px
     *
     * @return int px
     */
    private fun getScrollXDistance(): Int {
        if (isFinishing)
            return 0
        val layoutManager = binding.idRvId.layoutManager as LinearLayoutManager
        position = layoutManager.findFirstVisibleItemPosition()
        val firstVisibleChildView = layoutManager.findViewByPosition(position)
        val itemWidth = firstVisibleChildView!!.width
        return position * itemWidth - firstVisibleChildView.left
    }

    private var mUIHandler = MainHandler(this)

    private class MainHandler(activity: CustomDialActivity) :
        Handler(Looper.getMainLooper()) {
        private val mActivity: WeakReference<CustomDialActivity>

        init {
            mActivity = WeakReference<CustomDialActivity>(activity)
        }

        override fun handleMessage(msg: Message) {
            val activity: CustomDialActivity? = mActivity.get()
            if (activity != null) {
                if (msg.what == ExtractFrameWorkThread.MSG_SAVE_SUCCESS) {
                    if (activity.videoEditAdapter != null) {
                        val info: VideoEditInfo = msg.obj as VideoEditInfo
                        activity.videoEditAdapter!!.addItemVideoInfo(info)
                    }
                }
            }
        }
    }

    private fun videoPause() {
        if (customVideo?.isVideoPlaying()!!) {
            customVideo?.pause()
            handler.removeCallbacks(run)
        }
        if (binding.positionIcon.visibility == View.VISIBLE) {
            binding.positionIcon.visibility = View.GONE
        }
        binding.positionIcon.clearAnimation()
        if (animator != null && animator?.isRunning == true) {
            animator?.cancel()
        }
    }

    private fun stopAnim() {
        if ( binding.positionIcon.visibility == View.VISIBLE) {
            binding.positionIcon.visibility = View.GONE
        }
        binding.positionIcon.clearAnimation()
        if (animator != null && animator?.isRunning == true) {
            animator?.cancel()
        }
    }

    private val PHONE_OnePlus = "OnePlus"
    private val handler = Handler(Looper.getMainLooper())
    private val run: Runnable = object : Runnable {
        override fun run() {
            if (oldTime == 0L) {
                oldTime = System.currentTimeMillis()
            }
            videoProgressUpdate()
            handler.postDelayed(this, 1000)
        }
    }

    @SuppressLint("NewApi")
    private fun videoProgressUpdate() {
        if (Build.MANUFACTURER == PHONE_OnePlus) {
            val differenceTime = System.currentTimeMillis() - oldTime

            if (differenceTime >= rightProgress - leftProgress) {

                oldTime = System.currentTimeMillis()
                customVideo?.seekToVersionCode(leftProgress, true, oldTime)
                binding.positionIcon.clearAnimation()
                if (animator != null && animator?.isRunning == true) {
                    animator?.cancel()
                }
                anim()
            }
        } else {
            var currentPosition: Long = 0
            try {
                currentPosition = customVideo?.getCurrentPosition()!!.toLong()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            if (currentPosition >= rightProgress) {
                customVideo?.seekToVersionCode(leftProgress, true, oldTime)
                binding.positionIcon.clearAnimation()
                if (animator != null && animator?.isRunning == true) {
                    animator?.cancel()
                }
                anim()
            }
        }
    }

    private var animator: ValueAnimator? = null

    private fun anim() {
        if (binding.positionIcon.visibility == View.GONE) {
            binding.positionIcon.visibility = View.VISIBLE
        }
        val params = binding.positionIcon.layoutParams as FrameLayout.LayoutParams
        val start = (DensityUtil.dip2px(
            14F
        ) + (leftProgress - scrollPos) * averagePxMs).toInt()
        val end = (DensityUtil.dip2px(14F) + (rightProgress - scrollPos) * averagePxMs).toInt()
        animator = ValueAnimator
            .ofInt(start, end)
            .setDuration(rightProgress - scrollPos - (leftProgress - scrollPos))
        animator?.interpolator = LinearInterpolator()
        animator?.addUpdateListener { animation ->
            params.leftMargin = animation.animatedValue as Int
            binding.positionIcon.layoutParams = params

        }
        animator?.start()
    }

    private fun videoStart() {
        binding.positionIcon.visibility == View.GONE
        binding.positionIcon.clearAnimation()
        if (animator != null && animator!!.isRunning) {
            animator!!.cancel()
        }
        if (firstVideo) {
            firstVideo = false
            customVideo?.start()
        } else {
            videoSeekTo()
        }
        if (Build.MANUFACTURER == PHONE_OnePlus) {
            handler.removeCallbacks(run)
            handler.post(run)
        }
    }

    private fun videoSeekTo() {
        if (customVideo != null)
            customVideo?.setSeekTo(leftProgress, rightProgress, oldTime)
    }


    private fun setResultFinish(uri: Uri) {
        val map = HashMap<String, Any>()
        map["TOUCH_X"] = customVideo?.touchX!!
        map["TOUCH_Y"] = customVideo?.touchY!!
        map["LEFT_PROGRESS"] = leftProgress
        map["RIGHT_PROGRESS"] = rightProgress
        map["VIDEO_SELECT_BITMAP_PATH"] = videoSelectBitmapPath!!

        val matrixValues = FloatArray(9)
        videoSelectMatrix!!.getValues(matrixValues)
        map["VIDEO_SELECT_MATRIX"] = matrixValues

        map["CROP_W"] = cropW
        map["CROP_H"] = cropH
        map["SCALE_WIDTH"] = customVideo?.mOriginalWidth!! * customVideo?.scaleX!!
        map["SCALE_HEIGHT"] = customVideo?.mOriginalHeight!! * customVideo?.scaleY!!


        map["VIDEO_SCALE_W"] = customVideo?.videoScaleW!!
        map["VIDEO_SCALE_H"] = customVideo?.videoScaleH!!
        map["MARGIN_LEFT"] = customVideo?.xOri!!.toInt()
        map["MARGIN_TOP"] = customVideo?.yOri!!.toInt()

        val intent = Intent()
        intent.putExtra(VIDEO_URI, uri)
        intent.putExtra(CUSTOM_VIDEO, map)
        setResult(RESULT_CODE_EDIT_VIDEO, intent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        if (mExtractFrameWorkThread != null) {
            mExtractFrameWorkThread?.stopExtract()
        }
    }

    override fun onDestroy() {
//        ThreadUtils.getMainHandler().removeCallbacks(videoSeekToRun)
        if (mExtractFrameWorkThread != null) {
            mExtractFrameWorkThread?.stopExtract()
        }
        if (mOnScrollListener != null)
            binding.idRvId.removeOnScrollListener(mOnScrollListener)

        if (customVideo != null) {
            customVideo?.release()
            customVideo = null
        }

        super.onDestroy()

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}