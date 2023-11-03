package com.sjbt.sdk.sample.model

import com.sjbt.sdk.sample.MyApplication

/**
 * Created by qiyachao
 * on 2023_10_21
 */
class LocalSportLibrary {
    val sports = mutableListOf<LocalSport>()

    class LocalSport {
        val id = 0
        var buildIn = false
        var installed = false
        val type = 0
        val sportType = 0
        val names = hashMapOf<String, String>()
    }

    fun getNameById(id: Int):String {
        val locale = MyApplication.instance.resources.configuration.locale;
        val language = locale.language;
        for (bean in sports) {
            if (bean.id == id) {
                if (bean.names.contains(language)) {
                    return bean.names[language] ?: ""
                }
                val iterator = bean.names.iterator()
                while (iterator.hasNext()) {
                    val entity = iterator.next()
                    if (entity.key.lowercase() == language.lowercase() || entity.key.lowercase() == locale.toLanguageTag()
                            .lowercase()
                    ) {
                        return entity.value
                    }
                }
//        如果没有的话，就去获取en的
                return bean.names["en"] ?: ""
            }
        }
        return ""
    }

    fun getTypeById(id: Int):String {
        val locale = MyApplication.instance.resources.configuration.locale;
        val language = locale.language;
        for (bean in sports) {
            if (bean.id == id) {
                if (bean.names.contains(language)) {
                    return getTypeName(bean.type)
                }

            }
        }
        return ""
    }

    private fun getTypeName(type: Int): String {
            return ""
    }
}

