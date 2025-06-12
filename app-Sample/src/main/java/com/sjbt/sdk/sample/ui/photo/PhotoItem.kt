package com.sjbt.sdk.sample.ui.photo

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import java.util.Date

@Parcelize
@TypeParceler<Date, DateParceler>
data class PhotoItem(
    val id: String ,
    val uri: Uri,
    val date: Date,
    val name: String
) : Parcelable 