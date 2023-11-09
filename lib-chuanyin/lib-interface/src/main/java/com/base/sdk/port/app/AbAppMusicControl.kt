package com.base.sdk.port.app

import com.base.sdk.entity.apps.WmMusicControlType
import io.reactivex.rxjava3.core.Observable
/**
 * 应用模块-音乐控制
 */
abstract class AbAppMusicControl {
    /**
     * 监听音乐控制
     */
    abstract val observableMusicControl: Observable<WmMusicControlType>

}