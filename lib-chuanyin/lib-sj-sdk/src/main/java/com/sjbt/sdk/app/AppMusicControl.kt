package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmMusicControlType
import com.base.sdk.port.app.AbAppMusicControl
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import io.reactivex.rxjava3.subjects.PublishSubject

class AppMusicControl(val sjUniWatch: SJUniWatch) : AbAppMusicControl() {

    private val musicControlSub: PublishSubject<WmMusicControlType> = PublishSubject.create()

    override var observableMusicControl: PublishSubject<WmMusicControlType> = musicControlSub

    fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {
        TODO("Not yet implemented")
    }

    fun musicControlBusiness(it: NodeData) {
        when (it.data[0]) {
            WmMusicControlType.PREV_SONG.type -> {
                observeMusicControl(WmMusicControlType.PREV_SONG)
            }

            WmMusicControlType.NEXT_SONG.type -> {
                observeMusicControl(WmMusicControlType.NEXT_SONG)
            }

            WmMusicControlType.PLAY.type -> {
                observeMusicControl(WmMusicControlType.PLAY)
            }

            WmMusicControlType.PAUSE.type -> {
                observeMusicControl(WmMusicControlType.PAUSE)
            }

            WmMusicControlType.VOLUME_UP.type -> {
                observeMusicControl(WmMusicControlType.VOLUME_UP)
            }

            WmMusicControlType.VOLUME_DOWN.type -> {
                observeMusicControl(WmMusicControlType.VOLUME_DOWN)
            }
        }
    }

    private fun observeMusicControl(musicControl: WmMusicControlType) {
        musicControlSub.onNext(musicControl)
    }

}