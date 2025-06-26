package com.shenju.opus

object OpusDecoderJni {

    init {
        System.loadLibrary("opus-lib")
    }

    external fun createDecoder(sampleRate: Int, channels: Int): Long
    external fun decode(decoder: Long, opusData: ByteArray, pcmData: ByteArray, frameSize: Int): Int
    external fun destroyDecoder(decoder: Long)

}