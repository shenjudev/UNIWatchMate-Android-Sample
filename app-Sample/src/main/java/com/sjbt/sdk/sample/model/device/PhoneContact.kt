package com.sjbt.sdk.sample.model.device

import android.os.Parcel
import android.os.Parcelable

data class PhoneContact(val name: String?,
                        val number: String?,var checked:Boolean) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(number)
        parcel.writeByte(if (checked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PhoneContact> {
        override fun createFromParcel(parcel: Parcel): PhoneContact {
            return PhoneContact(parcel)
        }

        override fun newArray(size: Int): Array<PhoneContact?> {
            return arrayOfNulls(size)
        }
    }


}