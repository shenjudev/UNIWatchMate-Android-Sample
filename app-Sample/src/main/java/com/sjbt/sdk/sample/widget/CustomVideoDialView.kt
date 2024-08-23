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
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit


class CustomVideoDialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener {

    private val mMatrix = Matrix()

    private var mOriginalWidth: Float = 0f
    private var mOriginalHeight: Float = 0f

    public var mMediaPlayer: MediaPlayer? = null


    private var mViewH = 0
    private var mViewW = 0

    private var mId = -1

    private var itemScale: Float = 0f
    private var videoUri: Uri? = null
 private var scaledScaleX = 0f
    init {
        surfaceTextureListener = this
        setOnTouchListener { _, event -> onTouchEvent(event) }
    }

    fun setVideoUri(
        id: Int,
        videoUri: Uri,
        videoW: Int,
        videoH: Int,
        left: Long,
        right: Long,
        itemScale: Float,
        matrix: Matrix,
        marginLeft: Int,
        marginTop: Int
    ) {
        mId = id

        mViewW = videoW
        mViewH = videoH
        this.videoUri = videoUri

        this.itemScale = itemScale

        // 获取顶部视图的矩阵值
        val matrixValues = FloatArray(9)
        matrix.getValues(matrixValues)
        // 获取平移分量
        val transX = matrixValues[Matrix.MTRANS_X]
        val transY = matrixValues[Matrix.MTRANS_Y]
        // 获取缩放分量
        val scaleX = matrixValues[Matrix.MSCALE_X]
        val scaleY = matrixValues[Matrix.MSCALE_Y]
        // 应用缩放和平移的比例
        var scaledTransX = (transX - marginLeft) * itemScale
        var scaledTransY = (transY - marginTop) * itemScale

        var x = 0f
        var y = 0f
        if (transX < 0) {
            x = transX - marginLeft
        } else {
            x -= marginLeft - transX
        }
        if (transY < 0) {
            y = transY - marginTop
        } else {
            y -= marginTop - transY
        }
        scaledTransX = x * itemScale
        scaledTransY = y * itemScale

        scaledScaleX = 1f
        val scaledScaleY = 1f

        mMatrix.reset()
        mMatrix.setScale(scaledScaleX, scaledScaleY)
        mMatrix.postTranslate(scaledTransX, scaledTransY)

        mMediaPlayer = MediaPlayer()
        mMediaPlayer?.setOnInfoListener { mp, what, extra ->
            Log.e("what", "onInfo  ${Thread.currentThread().name}    $what    $extra")
            if (what == 805){
                mp.reset()
//                changeVideoSize()
                startVideo()
            }
            true
        }

        mMediaPlayer!!.setOnVideoSizeChangedListener { mp, width, height -> changeVideoSize() }
        mMediaPlayer?.setOnPreparedListener { mp ->
            mOriginalWidth = mp.videoWidth.toFloat()
            mOriginalHeight = mp.videoHeight.toFloat()
            mp.start()
            setSeekTo(left, right)
        }
    }


    private fun startVideo(){
        setLayerType(View.LAYER_TYPE_HARDWARE,null)

        mMediaPlayer?.setVolume(0f, 0f)

        mMediaPlayer?.setDataSource(context, videoUri!!)

        mMediaPlayer?.prepareAsync()

        mMediaPlayer?.isLooping = true
    }

    /**
     * 修改预览View的大小,以用来适配屏幕
     */
    private fun changeVideoSize() {
        val videoWidth = mMediaPlayer!!.videoWidth
        val videoHeight = mMediaPlayer!!.videoHeight
        val viewWidth = mViewW  * itemScale//DensityUtil.getScreenWidth(context) * itemScale
        val viewHeight = mViewH  * itemScale // DensityUtil.getScreenHeight(context) * itemScale

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

        setTransform(mMatrix)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        mMediaPlayer?.setSurface(Surface(surface))
        startVideo()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        // Ignored, the size is set via setTransform
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        mMediaPlayer?.release()
        surface.release()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // Invoked every time there's a new frame available
    }


    fun release() {
        if (mMediaPlayer != null) {
            clearTimerTask()
            mMediaPlayer?.stop()
            mMediaPlayer?.release()
            mMediaPlayer = null
//            release()
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
                                seekToVersionCode(leftVideoPosition, true)
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
                                    seekToVersionCode(leftVideoPosition, true)
                                }
                            })
                        }
                    } else {
                        if (mMediaPlayer?.currentPosition!! >= rightVideoPosition) {
                            pause()
                            seekToVersionCode(leftVideoPosition, true)
                        } else {
                            mMediaPlayer?.setOnCompletionListener(MediaPlayer.OnCompletionListener {
                                pause()
                                seekToVersionCode(leftVideoPosition, true)
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
    fun setSeekTo(leftVideoPosition: Long, rightVideoPosition: Long) {
        seekToVersionCode(leftVideoPosition, true)
        startTimerTask(leftVideoPosition, rightVideoPosition)
    }


    @SuppressLint("NewApi")
    fun setSeekTo(currtPosition: Int, leftVideoPosition: Long, rightVideoPosition: Long) {
        seekToVersionCode(currtPosition.toLong(), true)
        startTimerTask(leftVideoPosition, rightVideoPosition)
    }

    fun seekToVersionCode(leftVideoPosition: Long, isLoop: Boolean) {
        start()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mMediaPlayer?.seekTo(leftVideoPosition.toInt().toLong(), MediaPlayer.SEEK_CLOSEST)
            } else {
                mMediaPlayer?.seekTo(leftVideoPosition.toInt())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
}
