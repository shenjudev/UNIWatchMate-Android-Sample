package com.base.sdk.port.app

import com.base.sdk.entity.apps.WmLanguage
import io.reactivex.rxjava3.core.Single

/**
 * App - Language（应用模块 - 语言）
 */
abstract class AbAppLanguage {
    /**
     * 同步语言列表
     */
    abstract val syncLanguageList : Single<List<WmLanguage>>

    /**
     * 设定语言
     */
    abstract fun setLanguage(language: WmLanguage): Single<WmLanguage>
}