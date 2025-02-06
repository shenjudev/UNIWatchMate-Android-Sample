package com.sjbt.sdk.sample.utils

import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaPlayer
import android.util.Log
import java.io.IOException

class AudioPlayer(private var audioPlayerListener: AudioPlayerListener? = null) {

    interface AudioPlayerListener {
        fun onComplete()
        fun onError(error: String)
    }

    private var audioTrack: AudioTrack? = null
    private var mediaPlayer: MediaPlayer?  = null
    private var mediaCodec: MediaCodec? = null

    fun setListener(listener: AudioPlayerListener) {
        audioPlayerListener = listener
    }

    fun playFile(filePath: String) {
        Log.d("AudioPlayer", "playFile: $filePath")
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        } else {
            mediaPlayer?.reset()
        }
        try {
            mediaPlayer?.setDataSource(filePath)
            mediaPlayer?.setOnPreparedListener {
                mediaPlayer?.start()
            }
            mediaPlayer?.prepareAsync() // 使用 prepareAsync 进行异步准备
        } catch (e: IOException) {
            e.printStackTrace()
            audioPlayerListener?.onError("Error playing audio")
//            ToastUtil.showToast("Error playing audio", true)
            release()
        }

        // 设置播放完成监听器
        mediaPlayer?.setOnCompletionListener {
            audioPlayerListener?.onComplete()
//            ToastUtil.showToast("Playback completed", true)
            release()
        }

        // 设置错误监听器
        mediaPlayer?.setOnErrorListener { mp, what, extra ->
            audioPlayerListener?.onError("Error playing audio")
//            ToastUtil.showToast("Error playing audio", true)
            release()
            true
        }

    }


    fun stop(){
        if (mediaPlayer?.isPlaying!!){
            mediaPlayer?.stop()
            audioPlayerListener?.onComplete()
            release()
        }
    }

    fun isPlaying():Boolean{
        return if (mediaPlayer != null){
            mediaPlayer?.isPlaying!!
        }else{
            false
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
