package com.base.api

import com.base.sdk.AbUniWatch
import com.base.sdk.entity.data.*
import com.base.sdk.entity.settings.*
import com.base.sdk.port.AbWmTransferFile
import com.base.sdk.port.FileType
import com.base.sdk.port.WmTransferState
import com.base.sdk.port.app.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.io.File

internal class AbWmTransferDelegate(
    private val watchObservable: BehaviorObservable<AbUniWatch>
) : AbWmTransferFile() {

    override fun startTransfer(fileType: FileType, file: List<File>): Observable<WmTransferState> {
        return watchObservable.value!!.wmTransferFile.startTransfer(fileType, file)
    }

    override fun cancelTransfer(): Single<Boolean> {
        return watchObservable.value!!.wmTransferFile.cancelTransfer()
    }

}