package com.sjbt.sdk.sample.model

import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.base.Config

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

    fun getNameById(id: Int): String {
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

    fun getTypeById(id: Int): String {
        val locale = MyApplication.instance.resources.configuration.locale;
        val language = locale.language;
        for (bean in sports) {
            if (bean.id == id) {
                    return getTypeName(bean.sportType)
            }
        }
        return ""
    }

    private fun getTypeName(type: Int): String {
        when (type) {
            Config.SportTypeName.SPORT_RUN.id -> return "Run"
            Config.SportTypeName.SPORT_WALKING.id -> return "Walking"
            Config.SportTypeName.SPORT_RIDING.id -> return "Riding"
            Config.SportTypeName.SPORT_FITNESS.id -> return "Fitness"
            Config.SportTypeName.SPORT_OUTDOOR.id -> return "Outdoor"
            Config.SportTypeName.SPORT_BALL.id -> return "Ball"
            Config.SportTypeName.SPORT_YOGA.id -> return "Yoga"
            Config.SportTypeName.SPORT_ICE.id -> return "Ice"
            Config.SportTypeName.SPORT_DANCE.id -> return "Dance"
            Config.SportTypeName.SPORT_LEISURE.id -> return "Leisure"
            Config.SportTypeName.SPORT_OTHERS.id -> return "Others"
        }
        return ""
    }
}

