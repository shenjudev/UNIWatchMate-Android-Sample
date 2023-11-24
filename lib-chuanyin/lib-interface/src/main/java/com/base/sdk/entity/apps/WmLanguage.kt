package com.base.sdk.entity.apps

/**
 * 语言数据结构定义
 * bcp 语言代码 en zh
 */
data class WmLanguage(val bcp: String, val name: String?, var curr_lang: Boolean)