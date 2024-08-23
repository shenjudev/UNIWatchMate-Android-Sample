package com.sjbt.sdk.sample.model.device


data class FeaturesModel(
    val id:Int,
    val featureName: String?,
    var isSupport: Boolean = false
)  {
   constructor(): this(0,"")

}