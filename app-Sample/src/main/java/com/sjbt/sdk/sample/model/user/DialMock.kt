package com.sjbt.sdk.sample.model.user

import android.os.Parcel
import android.os.Parcelable

/**
 *
 * @property dialCoverRes 封面资源文件id，如果为-1则为存储中选择的表盘文件(The cover resource file id, if -1, is the dial file selected in storage)
 * @property dialAssert  assert文件中内置的表盘的名称(The name of the dial built into the assert file)
 * @property installed 已安装 1(Installed 1)
 * @property id
 */
data class DialMock(val dialCoverRes:Int, val dialAssert: String?,val installed:Int,val id: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString()!!
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(dialCoverRes)
        dest.writeString(dialAssert)
        dest.writeInt(installed)
        dest.writeString(id)
    }

    companion object CREATOR : Parcelable.Creator<DialMock> {
        override fun createFromParcel(parcel: Parcel): DialMock {
            return DialMock(parcel)
        }

        override fun newArray(size: Int): Array<DialMock?> {
            return arrayOfNulls(size)
        }
    }
}