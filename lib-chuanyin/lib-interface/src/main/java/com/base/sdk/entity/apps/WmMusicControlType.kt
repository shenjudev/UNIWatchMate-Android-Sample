package com.base.sdk.entity.apps

/**
 * 音乐控制
 */
enum class WmMusicControlType(val type:Byte) {
    PREV_SONG(0),
    NEXT_SONG(1),
    PLAY(2),
    PAUSE(3),
    VOLUME_UP(4),
    VOLUME_DOWN(5), 
}