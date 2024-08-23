package com.sjbt.sdk.sample.utils

import io.reactivex.rxjava3.disposables.Disposable

class DisposableCancellationSignal : Disposable {

    @Volatile
    private var isCanceled = false

    override fun dispose() {
        isCanceled = true
    }

    override fun isDisposed(): Boolean {
        return isCanceled
    }

}