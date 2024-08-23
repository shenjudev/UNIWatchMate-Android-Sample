package com.transsion.oraimohealth.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import com.sjbt.sdk.sample.utils.DensityUtil
import com.sjbt.sdk.sample.widget.ScaleGestureDetectorFixed
import java.util.Timer
import java.util.TimerTask


class CustomVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener {


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 取消SurfaceTextureListener的监听
        surfaceTextureListener = null

        // 在这里执行其他必要的清理工作，如释放资源等
    }

    val mMatrix = Matrix()
    var mScaleFactor = 1f

    val maxScale = 3f

    var minScale = 1f;

    var mMediaPlayer: MediaPlayer? = null


    private var mViewH = 0
    private var mViewW = 0


    private var mIsTouch = true

    private var videoListener: VideoListener? = null

    private var mId = -1
    var mOriginalWidth = 0
    var mOriginalHeight = 0

    private var cropW = 0
    private var cropH = 0
    var scale = 0f

    var videoScaleW = 0
    var videoScaleH = 0
    var cropTop = 0
    var cropLeft = 0
    var startVideoTime: Long = 0
    var endVideoTime: Long = 0

    var touchX = 0f
    var touchY = 0f

    var videoUri: Uri? = null

    var viewCreateH = 0

    init {
        surfaceTextureListener = this
        setOnTouchListener { _, event -> onTouchEvent(event) }
    }


    fun setVideoUri(
        id: Int, videoUri: Uri, viewW: Int, viewH: Int, isTouch: Boolean,
        mOriginalWidth: Int, mOriginalHeight: Int, cropW: Int, cropH: Int, scale: Float
    ) {
        mId = id

        mViewH = viewH
        mViewW = viewW

        mIsTouch = isTouch

        this.mOriginalWidth = mOriginalWidth
        this.mOriginalHeight = mOriginalHeight
        this.cropW = cropW
        this.cropH = cropH
        this.scale = scale
        this.videoUri = videoUri
        Log.e("what_1", "初始化CustomVideoView  ${Thread.currentThread().name}")
        mMediaPlayer = MediaPlayer()

        mMediaPlayer?.setOnInfoListener { mp, what, extra ->
            Log.e("what", "onInfo  ${Thread.currentThread().name}    $what    $extra")
            if (what == 805) {
                mp.reset()
//                changeVideoSize()
                startVideo()
            }
            true
        }
        mMediaPlayer?.setOnErrorListener { mediaPlayer, i, i2 ->
            Log.e("what", "setOnErrorListener  ${Thread.currentThread().name}")
            false
        }

        mMediaPlayer!!.setOnVideoSizeChangedListener { mp, width, height ->
            changeVideoSize()
            Log.e("what", "setOnVideoSizeChangedListener ${Thread.currentThread().name}")
        }

        mMediaPlayer?.setOnPreparedListener {
            Log.e("what", "setOnPreparedListener  ${Thread.currentThread().name}")
            mMediaPlayer!!.start()
            Log.e("握草", "width:$width,height:$height  ${Thread.currentThread().name}")
            val videoDuration = mMediaPlayer?.duration?.div(1000)!!.toInt() ?: 0
            if (videoDuration >= 5000) {
                setSeekTo(0, 5000, oldTime)
            } else {
                setSeekTo(0, mMediaPlayer?.duration!!.toLong(), oldTime)
            }
        }

    }

    var xOri = 0f
    var yOri = 0f


    private fun startVideo() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)

        mMediaPlayer?.setVolume(0f, 0f)

        mMediaPlayer?.setDataSource(context, videoUri!!)

        mMediaPlayer?.prepareAsync()

        mMediaPlayer?.isLooping = true
    }

    /**
     * 修改预览View的大小,以用来适配屏幕
     */
    private fun changeVideoSize(): Float {
        val videoWidth = mMediaPlayer!!.videoWidth
        val videoHeight = mMediaPlayer!!.videoHeight
        val viewWidth = width
        val viewHeight = height

        if (viewCreateH == 0) {
            viewCreateH = height
        }

        //获得全屏高度，包含隐藏的状态栏、导航栏等
        val hp = 1.0 * viewHeight / videoHeight
        val wp = 1.0 * viewWidth / videoWidth
        val sp = Math.max(hp, wp)

        //通过算法，计算出最大比例，赋值给SurfaceView，并使其居中，来解决适配/拉伸问题
        val realHeight = (videoHeight * sp).toInt()
        val realWidth = (videoWidth * sp).toInt()

        val params = layoutParams as FrameLayout.LayoutParams
        params.width = realWidth
        params.height = realHeight
        layoutParams = params


        mOriginalWidth = params.width
        mOriginalHeight = params.height

        val cropW = cropW
        val cropH = cropH
        mScaleFactor = Math.max(cropW / realWidth.toFloat(), cropH / realHeight.toFloat())

//        if (videoWidth > videoHeight) {
        xOri = (DensityUtil.getScreenWidth(context) - cropW) / 2f
        yOri = (viewCreateH - cropH).toFloat() / 2f
//        } else {
//            xOri = (DensityUtil.getScreenWidth(context) - cropW) / 2f
//            yOri = (viewCreateH - cropH).toFloat() / 2f
//        }

        minScale = mScaleFactor


        mMatrix.reset()
        if (width > height){
            mMatrix.setScale(mScaleFactor, mScaleFactor)
            if (width >= (DensityUtil.getScreenWidth(context) + xOri)){
                mMatrix.postTranslate(-xOri, yOri)
            }else if(width == DensityUtil.getScreenWidth(context)){
                mMatrix.postTranslate(0f, yOri)
            }else{
                mMatrix.postTranslate(xOri, yOri)
            }
        } else {
            mMatrix.setScale(1f, 1f)
        }

        setTransform(mMatrix)
        scale = mScaleFactor
        videoScaleW = realWidth
        videoScaleH = realHeight

        cropTop = yOri.toInt()
        cropLeft = xOri.toInt()

        videoListener?.onTouchXY(mMatrix)

        val matrixValues = FloatArray(9)
        mMatrix.getValues(matrixValues)

        internalListener = InternalGesturesListener(this, mMatrix)
        gestureDetector = GestureDetector(context, internalListener!!)
        scaleDetector = ScaleGestureDetectorFixed(context, internalListener!!)
        return sp.toFloat()
    }

    fun setCropScale(matrixValues: FloatArray) {
        val currentTranslationX = matrixValues[Matrix.MTRANS_X]
        val currentTranslationY = matrixValues[Matrix.MTRANS_Y]
        val scaleX = matrixValues[Matrix.MSCALE_X]
        touchX = currentTranslationX
        touchY = currentTranslationY
        scale = scaleX
    }

    private var internalListener: InternalGesturesListener? = null
    private var gestureDetector: GestureDetector? = null
    private var scaleDetector: ScaleGestureDetectorFixed? = null
    private var currentMotionEvent: MotionEvent? = null

    private val tmpPointArray = FloatArray(2)
    private val matrixInverse = Matrix()
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {

        currentMotionEvent = event
        // We should remap given event back to original coordinates
        // so that children can correctly respond to it
        // We should remap given event back to original coordinates
        // so that children can correctly respond to it
        val invertedEvent: MotionEvent? = event?.let { applyMatrix(it, matrixInverse) }

        invertedEvent?.let {
            return try {
                super.dispatchTouchEvent(it)
            } finally {
                it.recycle()
            }
        }

        return false

    }

    private fun applyMatrix(event: MotionEvent, matrix: Matrix): MotionEvent? {
        tmpPointArray[0] = event.x
        tmpPointArray[1] = event.y
        matrix.mapPoints(tmpPointArray)
        val copy = MotionEvent.obtain(event)
        copy.setLocation(tmpPointArray[0], tmpPointArray[1])
        return copy
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector?.onTouchEvent(event)
        scaleDetector?.onTouchEvent(event)
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        mMediaPlayer?.setSurface(Surface(surface))
        startVideo()
        Log.e(
            "what",
            "onSurfaceTextureAvailable:${surface == null}  ${Thread.currentThread().name}"
        )
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        // Ignored, the size is set via setTransform
        Log.e("what", "onSurfaceTextureSizeChanged  ${Thread.currentThread().name}")
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        Log.e("what", "onSurfaceTextureDestroyed  ${Thread.currentThread().name}")
        release()
        surface.release()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // Invoked every time there's a new frame available
//        Log.e("what", "onSurfaceTextureUpdated  ${Thread.currentThread().name}")
    }

    fun setVideoListener(listener: VideoListener) {
        this.videoListener = listener
    }

    interface VideoListener {
        fun onTouchXY(matrix: Matrix)
        fun onSeekTo(left: Long, right: Long)
        fun onSeekToAnim(left: Long, right: Long)
//        fun onError(errorCode:Int)
    }

    fun release() {
        try {
            if (mMediaPlayer != null) {
                clearTimerTask()
                mMediaPlayer?.stop()
                mMediaPlayer?.release()
                mMediaPlayer = null
//            release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    fun clearTimerTask() {
        if (timer != null) {
            timer!!.cancel()
        }
        if (timerTask != null) {
            timerTask!!.cancel()
        }
        timerTask = null
        timer = null
    }

    private var oldTime: Long = 0


    private fun startTimerTask(leftVideoPosition: Long, rightVideoPosition: Long) {
        clearTimerTask()
        timer = Timer()
        timerTask = object : TimerTask() {
            @SuppressLint("NewApi")
            override fun run() {
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && Build.MANUFACTURER == "OnePlus") {
                        var differenceTime: Long = 0
                        if (oldTime != 0L) {
                            differenceTime = System.currentTimeMillis() - oldTime
                        }

                        if (differenceTime >= rightVideoPosition) {

                            oldTime = 0
                            if (!mMediaPlayer?.isLooping!!) {
                                clearTimerTask()
//                            ThreadUtils.runOnUiThread(Runnable { mMediaPlayer.onCompletion() })
                            } else {
                                seekToVersionCode(leftVideoPosition, true, oldTime)
                            }
                        } else {
                            mMediaPlayer?.setOnCompletionListener(MediaPlayer.OnCompletionListener {
                                if (!mMediaPlayer?.isLooping!!) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        clearTimerTask()
                                        mMediaPlayer?.pause()
//                                    ThreadUtils.runOnUiThread(Runnable { surVideoPlay.onCompletion() })
                                    }
                                } else {
                                    seekToVersionCode(leftVideoPosition, true, oldTime)
                                }
                            })
                        }
                    } else {
                        if (mMediaPlayer?.currentPosition!! >= rightVideoPosition) {
                            pause()
                            seekToVersionCode(leftVideoPosition, true, oldTime)
                        } else {
                            mMediaPlayer?.setOnCompletionListener(MediaPlayer.OnCompletionListener {
                                pause()
                                seekToVersionCode(leftVideoPosition, true, oldTime)
                            })
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    clearTimerTask()
                }

            }
        }
        timer?.scheduleAtFixedRate(timerTask, 0, 1000)
    }

    @SuppressLint("NewApi")
    fun setSeekTo(leftVideoPosition: Long, rightVideoPosition: Long, OldTime: Long) {
        seekToVersionCode(leftVideoPosition, true, OldTime)
        startTimerTask(leftVideoPosition, rightVideoPosition)

        startVideoTime = leftVideoPosition
        endVideoTime = rightVideoPosition

        if (videoListener != null && mIsTouch)
            videoListener?.onSeekTo(leftVideoPosition, rightVideoPosition)
    }

    fun seekToVersionCode(leftVideoPosition: Long, isLoop: Boolean, OldTime: Long) {
        oldTime = OldTime
        start()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mMediaPlayer?.seekTo(leftVideoPosition.toInt().toLong(), MediaPlayer.SEEK_CLOSEST)
            } else {
                mMediaPlayer?.seekTo(leftVideoPosition.toInt())
            }
            videoListener?.onSeekToAnim(leftVideoPosition, -1)
            if (videoListener != null && mIsTouch && !isLoop)
                videoListener?.onSeekTo(leftVideoPosition, -1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentPosition(): Int {
        return mMediaPlayer?.currentPosition!!
    }


    fun isVideoPlaying(): Boolean {
        if (mMediaPlayer != null && mMediaPlayer?.isPlaying == true) {
            return true
        }
        return false
    }


    fun pause() {
        if (mMediaPlayer != null && mMediaPlayer?.isPlaying!!) {
            mMediaPlayer?.pause()
        }
    }

    fun start() {
        if (mMediaPlayer != null && !mMediaPlayer?.isPlaying!!) {
            mMediaPlayer?.start()
        }
    }


    private class InternalGesturesListener(textureView: CustomVideoView, matrix: Matrix) :
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        ScaleGestureDetector.OnScaleGestureListener {
        private val textureView = textureView
        private val mMatrix = matrix

        //当一个动作事件（例如触摸、点击、拖动等）被按下时触发。
        override fun onDown(p0: MotionEvent): Boolean {
            return false
        }

        //当用户按下并持续按住不放时触发，但尚未确认。
        override fun onShowPress(p0: MotionEvent) {
        }

        //当用户在屏幕上完成一次单击操作并抬起时触发。
        override fun onSingleTapUp(p0: MotionEvent): Boolean {
            return false
        }

        //当用户在屏幕上进行滚动操作时触发。
        override fun onScroll(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float): Boolean {
            if (p1.pointerCount == 1) {
                val matrixValues = FloatArray(9)
                mMatrix.getValues(matrixValues)
                var currentTranslationX = matrixValues[Matrix.MTRANS_X]
                var currentTranslationY = matrixValues[Matrix.MTRANS_Y]

                currentTranslationX -= textureView.xOri

                currentTranslationY -= textureView.yOri


                val scaleY = matrixValues[Matrix.MSCALE_Y]
                val scaleX = matrixValues[Matrix.MSCALE_X]

                // 边界检查，确保拖拽位置不超出指定区间
                val newX = Math.abs(currentTranslationX) + p2
                // 边界检查，确保拖拽位置不超出指定区间
                val newY = Math.abs(currentTranslationY) + p3

                val viewScaleW = textureView.width * scaleX
                val viewScaleH = textureView.height * scaleY

                var dy = 0f
                if (p0 != null) {
                    if (p0.y > p1.y) {
                        //向上滑动
                        if (currentTranslationY <= 0 && viewScaleH >= textureView.cropH + Math.abs(
                                newY
                            )
                        ) {
                            dy = p3
                        } else {
                            dy = viewScaleH - textureView.cropH - Math.abs(
                                currentTranslationY
                            )
                        }
                    } else {
                        //向下滑动
                        dy =
                            if (currentTranslationY < 0 && Math.abs(currentTranslationY) >= Math.abs(p3)) {
                                p3
                            } else {
                                currentTranslationY
                            }

                    }
                }
                var dx = 0f

                if (p0 != null) {
                    if (p0.x > p1.x) {
                        //向左滑动
            //                    LogUtil.e("what之XY向左","currX:${currentTranslationX}  X:${p2} currY:${currentTranslationY}  Y:${p3}")

                        dx =
                            if (currentTranslationX <= 0 && viewScaleW >= textureView.cropW + Math.abs(
                                    newX
                                )
                            ) {
                                p2
                            } else {
                                viewScaleW - textureView.cropW - Math.abs(
                                    currentTranslationX
                                )
                            }
                    } else {
                        //向右滑动
            //                    LogUtil.e("what之XY向右","currX:${currentTranslationX}  X:${p2} currY:${currentTranslationY}  Y:${p3}")
                        dx =
                            if (currentTranslationX < 0 && Math.abs(currentTranslationX) >= Math.abs(p2)) {
                                p2
                            } else {
                                currentTranslationX
                            }

                    }
                }

                mMatrix.postTranslate(-dx, -dy)
                textureView.setCropScale(matrixValues)
                textureView.videoListener?.onTouchXY(textureView.mMatrix)

                textureView.setTransform(mMatrix)
            }
            return true
        }

        //当用户在屏幕上长时间按下不放时触发。
        override fun onLongPress(p0: MotionEvent) {
        }

        //当用户在屏幕上进行滑动操作并释放时触发。
        override fun onFling(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float): Boolean {
            return false
        }

        //当用户在屏幕上完成一次单击操作，且系统确认此次点击是单次点击而不是双击时触发。
        override fun onSingleTapConfirmed(p0: MotionEvent): Boolean {
            return false
        }

        //当用户在屏幕上完成一次双击操作时触发。
        override fun onDoubleTap(p0: MotionEvent): Boolean {
            return false
        }

        //当用户在屏幕上完成一次双击操作时的回调方法。
        override fun onDoubleTapEvent(p0: MotionEvent): Boolean {
            return false
        }

        //当用户在支持缩放的View上进行缩放操作时触发。
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val matrixValues = FloatArray(9)
            mMatrix.getValues(matrixValues)
            val newScale: Float = matrixValues[Matrix.MSCALE_X] * detector.scaleFactor
            // 限制缩放范围
            val clampedScale = newScale.coerceIn(textureView.minScale, textureView.maxScale)

            if (newScale < textureView.minScale || newScale > textureView.maxScale) {
                return false
            }
            mMatrix.postScale(
                detector.scaleFactor,
                detector.scaleFactor,
                detector.focusY,
                detector.focusY
            )
            val matrixValues2 = FloatArray(9)
            mMatrix.getValues(matrixValues2)
            textureView.setTransform(mMatrix)
            textureView.setCropScale(matrixValues2)
            textureView.videoListener?.onTouchXY(textureView.mMatrix)
            return true
        }


        //当用户开始在支持缩放的View上进行缩放操作时触发。
        override fun onScaleBegin(p0: ScaleGestureDetector): Boolean {
            return true
        }

        //当用户结束在支持缩放的View上进行缩放操作时触发。
        override fun onScaleEnd(p0: ScaleGestureDetector) {
            val matrixValues2 = FloatArray(9)
            mMatrix.getValues(matrixValues2)
            var currX: Float = matrixValues2[Matrix.MTRANS_X]
            var currY: Float = matrixValues2[Matrix.MTRANS_Y]
            var currScale: Float = matrixValues2[Matrix.MSCALE_X]

            val viewWidth = textureView.width * currScale
            val viewHeight = textureView.height * currScale

            var x = 0f
            var y = 0f
            if (currX >= textureView.xOri) {
                x -= currX - textureView.xOri
            } else if (currX < 0 && viewWidth < textureView.cropW + Math.abs(currX) + textureView.xOri) {
                val right = viewWidth - textureView.xOri - textureView.cropW
                if (right > 0) {
                    x = Math.abs(currX) - right
                } else {
                    x = currX + right
                }
            } else if (currX > 0 && viewWidth - Math.abs(currX) - textureView.xOri < textureView.cropW) {

                val right = viewWidth - textureView.xOri - textureView.cropW
                if (right > 0) {
                    x -= right - currX
                } else {
                    x = currX + right
                }
            }
            if (currY >= textureView.yOri) {
                y -= currY - textureView.yOri
            } else if (currY <= 0 && viewHeight - textureView.yOri - Math.abs(currY) < textureView.cropH) {
//                currY -= textureView.yOri
                val top =
                    viewHeight - textureView.yOri - Math.abs(currY) - textureView.cropH
                if (Math.abs(top) > Math.abs(currY)) {
                    y = Math.abs(top)
                } else {
                    y = Math.abs(currY) - Math.abs(top)
                }
            } else if (currY > 0 && viewHeight - (textureView.yOri - Math.abs(currY)) < textureView.cropH) {
                val top =
                    viewHeight - (textureView.yOri - Math.abs(currY)) - textureView.cropH
                y = Math.abs(top)
            }
            if (x != 0f || y != 0f) {
                if (Math.abs(currScale - textureView.minScale) <= 0.08f) {
                    mMatrix.setScale(textureView.minScale, textureView.minScale)
                    mMatrix.postTranslate(textureView.xOri, textureView.yOri)
                } else {
                    mMatrix.postTranslate(x, y)
                }
                textureView.setTransform(mMatrix)
            }

            textureView.videoScaleW = viewWidth.toInt()
            textureView.videoScaleH = viewHeight.toInt()
        }
    }

}
