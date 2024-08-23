package com.sjbt.sdk.sample.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.location.LocationManager
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.fragment.NavHostFragment
import com.base.sdk.entity.apps.TodayWeather
import com.base.sdk.entity.apps.WmLocation
import com.base.sdk.entity.apps.WmWeather
import com.base.sdk.entity.apps.WmWeatherForecast
import com.base.sdk.entity.apps.WmWeatherTime
import com.base.sdk.entity.common.WmWeek
import com.base.sdk.entity.settings.WmUnitInfo
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ResourceUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.github.kilnn.tool.dialog.prompt.PromptAutoCancel
import com.github.kilnn.tool.dialog.prompt.PromptDialogHolder
import com.github.kilnn.tool.system.SystemUtil
import com.github.kilnn.tool.widget.item.PreferenceItem
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.model.LocalSportLibrary
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Action
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException


/**
 * All Parcelable objects passed by oneself can use this as a Key
 */
const val PARCEL_ARGS = "parcelArgs"
const val FILE_PROVIDER_AUTHORITY = "com.topstep.fitcloud.sample2.fileprovider"

fun PromptDialogHolder.showFailed(
    throwable: Throwable,
    intercept: Boolean = false,
    cancelable: Boolean = false,
    autoCancel: PromptAutoCancel = PromptAutoCancel.DEFAULT,
    promptId: Int = 0,
) {
    val text = throwable.toReadableMessage(context)
    showFailed(text, intercept, cancelable, autoCancel, promptId)
}

fun Throwable.toReadableMessage(context: Context): String {
    val resId = when (this) {
//        is AccountException -> {
//            when (errorCode) {
//                AccountException.ERROR_CODE_USER_EXIST -> R.string.error_user_exist
//                AccountException.ERROR_CODE_USER_NOT_EXIST -> R.string.error_user_not_exist
//                AccountException.ERROR_CODE_PASSWORD -> R.string.error_password_incorrect
//                else -> 0
//            }
//        }
//        is FcUnSupportFeatureException -> {
//            R.string.error_device_un_support
//        }
//        is BleDisconnectedException -> {
//            R.string.device_state_disconnected
//        }
        else -> {
            0
        }
    }
    return if (resId != 0) {
        context.getString(resId)
    } else {
        message ?: this::class.java.name
    }
}

@MainThread
fun Fragment.promptToast(tag: String? = null): Lazy<PromptDialogHolder> =
    lazy(LazyThreadSafetyMode.NONE) {
        PromptDialogHolder(
            requireContext(),
            childFragmentManager,
            if (tag.isNullOrEmpty()) this::class.simpleName + "toast" else tag,
            theme = R.style.PromptToastTheme
        )
    }

@MainThread
fun FragmentActivity.promptToast(tag: String? = null): Lazy<PromptDialogHolder> =
    lazy(LazyThreadSafetyMode.NONE) {
        PromptDialogHolder(
            this,
            supportFragmentManager,
            if (tag.isNullOrEmpty()) this::class.simpleName + "toast" else tag,
            theme = R.style.PromptToastTheme
        )
    }

@MainThread
fun Fragment.promptProgress(tag: String? = null): Lazy<PromptDialogHolder> =
    lazy(LazyThreadSafetyMode.NONE) {
        PromptDialogHolder(
            requireContext(),
            childFragmentManager,
            if (tag.isNullOrEmpty()) this::class.simpleName + "progress" else tag,
            theme = R.style.PromptProgressTheme
        )
    }

@MainThread
fun FragmentActivity.promptProgress(tag: String? = null): Lazy<PromptDialogHolder> =
    lazy(LazyThreadSafetyMode.NONE) {
        PromptDialogHolder(
            this,
            supportFragmentManager,
            if (tag.isNullOrEmpty()) this::class.simpleName + "progress" else tag,
            theme = R.style.PromptProgressTheme
        )
    }

fun Lifecycle.launchRepeatOnStarted(
    block: suspend CoroutineScope.() -> Unit,
) {
    coroutineScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED, block)
    }
}

val Fragment.viewLifecycle
    get() = viewLifecycleOwner.lifecycle

val Fragment.viewLifecycleScope
    get() = viewLifecycleOwner.lifecycleScope

fun AppCompatActivity.findNavControllerInNavHost(@IdRes containerViewId: Int): NavHostController {
    val navHost = supportFragmentManager.findFragmentById(containerViewId) as NavHostFragment
    return navHost.navController as NavHostController
}

fun <T : Any> Observable<T>.doOnFinish(onFinish: Action): Observable<T> {
    return this.doOnTerminate(onFinish).doOnDispose(onFinish)
}

fun CoroutineScope.launchWithLog(block: suspend CoroutineScope.() -> Unit): Job {
    return launch(CoroutineExceptionHandler { _, exception ->
        Timber.w(exception)
    }, block = block)
}

inline fun <T, R> T.runCatchingWithLog(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        Timber.w(e)
        Result.failure(e)
    }
}


fun flowBluetoothAdapterState(context: Context) = callbackFlow {
    val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    trySend(manager.adapter.isEnabled)
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
            trySend(state == BluetoothAdapter.STATE_ON)
        }
    }
    context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    awaitClose { context.unregisterReceiver(receiver) }
}.distinctUntilChanged()

fun flowLocationServiceState(context: Context) = callbackFlow {
    trySend(SystemUtil.isLocationEnabled(context))
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            trySend(false)
            trySend(SystemUtil.isLocationEnabled(context))
        }
    }
    context.registerReceiver(receiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))
    awaitClose { context.unregisterReceiver(receiver) }
}

/**
 * Set all child enabled or disabled.(Such as duplicateParentState)
 */
fun ViewGroup.setAllChildEnabled(enabled: Boolean) {
    if (this.isEnabled != enabled) {
        this.isEnabled = enabled
    }
    for (idx in 0 until this.childCount) {
        val child = this.getChildAt(idx)
        val tag = child.tag
        if (tag is String && tag.contains("ignoreParentState")) {
            continue
        }
        if (child.isEnabled != enabled) {
            child.isEnabled = enabled
        }
        if (child is ViewGroup && child !is PreferenceItem) {
            child.setAllChildEnabled(enabled)
        }
        if (child is ViewGroup) {
            child.setAllChildEnabled(enabled)
        }
    }
}

fun <T> Flow<T>.shareInView(
    scope: CoroutineScope,
): SharedFlow<T> {
    return shareIn(scope, SharingStarted.WhileSubscribed(5000, 0), 1)
}

fun Context.isGif(uri: Uri): Boolean {
    val mimeType = contentResolver.getType(uri)
    return mimeType != null && mimeType.startsWith("image/gif")
}

//*****************文件*****************//
const val KB: Long = 1024

fun fileSizeStr(bytes: Long, showZero: Boolean = false): String {
    val mb = KB * KB
    val gb = mb * KB
    val tb = gb * KB
    return if (bytes <= 0) {
        if (showZero) {
            "0KB"
        } else {
            "?.?KB"
        }
    } else if (bytes < KB * 0.1f) {
        "0.1KB"
    } else if (bytes < mb) {
        FormatterUtil.decimal1Str(bytes / KB.toFloat()) + "KB"
    } else if (bytes < gb) {
        FormatterUtil.decimal1Str(bytes / mb.toFloat()) + "MB"
    } else if (bytes < tb) {
        FormatterUtil.decimal1Str(bytes / gb.toFloat()) + "GB"
    } else {
        FormatterUtil.decimal1Str(bytes / tb.toFloat()) + "TB"
    }
}

internal const val KM_TO_MI = 0.6213712f //1 km=0.6213712 mi

fun Float.km2mi(): Float {
    return this * KM_TO_MI
}

fun Float.mi2km(): Float {
    return this / KM_TO_MI
}

fun Float.celsius2Fahrenheit(): Float {
    return this * 1.8f + 32
}

/**
 * Calculate calories based on step length and number of steps
 *
 * @param km     Distance(km)
 * @param weight Body weight(kg)
 * @return Calories (kcal)
 */
fun km2Calories(km: Float, weight: Float): Float {
    return 0.78f * weight * km
}

/**
 * Calculate distance based on step length and number of steps
 *
 * @param step       step number
 * @param stepLength Step length(m)
 * @return Distance(km)
 */
fun step2Km(step: Int, stepLength: Float): Float {
    return stepLength * step / 1000
}

private var localSportLibrary: LocalSportLibrary? = null
fun getSportLibrary(): LocalSportLibrary {
    if (localSportLibrary == null) {
        val sportsData = ResourceUtils.readAssets2String("sport_type_names.json")
        localSportLibrary =
            GsonUtils.fromJson(sportsData, LocalSportLibrary::class.java)
    }
    return localSportLibrary!!
}

fun glideShowImage(
    imageView: ImageView,
    uri: Any?,
    inRecyclerView: Boolean = true,
    placeholder: Int = R.mipmap.ic_default_image_place_holder,
) {
    val builder = Glide.with(imageView.context)
        .load(uri)
        .apply(RequestOptions.placeholderOf(placeholder))
    if (inRecyclerView) {
        builder.into(DrawableImageViewTarget(imageView).waitForLayout())
    } else {
        builder.into(imageView)
    }
}

fun glideShowMipmapImage(
    imageView: ImageView,
    res: Int,
    inRecyclerView: Boolean = true,
    placeholder: Int = R.mipmap.ic_default_image_place_holder,
) {
    val builder = Glide.with(imageView.context)
        .load(res)
        .apply(RequestOptions.placeholderOf(placeholder))
    if (inRecyclerView) {
        builder.into(DrawableImageViewTarget(imageView).waitForLayout())
    } else {
        builder.into(imageView)
    }
}

fun getTestWeatherdata(wmWeatherTime: WmWeatherTime, code: Int): WmWeather {
    val weatherForecastList = mutableListOf<WmWeatherForecast>()
    val todayWeatherList = mutableListOf<TodayWeather>()

//                 uvindex   0—15
    if (wmWeatherTime == WmWeatherTime.TODAY) {
        for (index in 0..23) {
            val toDayWeather = TodayWeather(
                10f, WmUnitInfo.TemperatureUnit.CELSIUS, 66, 5,2,
                code, "晴", System.currentTimeMillis() + index * 3600 * 1000, index
            )
            todayWeatherList.add(toDayWeather)
        }
    } else {
        for (index in 0..6) {
            val wmWeatherForecast = WmWeatherForecast(
                10f,
                30f,
                20f,
                WmUnitInfo.TemperatureUnit.CELSIUS,
                90f,
                95f,
                5,
                6,
                2,
                code,
                code,
                "白天天气描述",
                "夜晚天气描述",
                System.currentTimeMillis() + index * 3600 * 24 * 1000,
                WmWeek.values()[index]
            )
            weatherForecastList.add(wmWeatherForecast)
        }
    }

    val wmLocation = WmLocation("cn", "xi'an", "district", 10.12345, 10.12345)

    return WmWeather(1,
        System.currentTimeMillis(),
        wmLocation,
        weatherForecastList,
        todayWeatherList
    )
}
//fun glideLoadDialBackground(context: Context, dialView: DialView, uri: Any) {
//    Glide.with(context)
//        .asBitmap()
//        .load(uri)
//        .into(object : CustomTarget<Bitmap?>() {
//            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
//                dialView.backgroundBitmap = resource
//            }
//
//            override fun onLoadCleared(placeholder: Drawable?) {
//                dialView.backgroundBitmap = null
//            }
//        })
//}
//
//fun glideLoadDialStyle(context: Context, dialView: DialView, uri: Any, styleBaseOnWidth: Int) {
//    Glide.with(context)
//        .asBitmap()
//        .load(uri)
//        .into(object : CustomTarget<Bitmap?>() {
//            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
//                dialView.setStyleBitmap(resource, styleBaseOnWidth)
//            }
//
//            override fun onLoadCleared(placeholder: Drawable?) {
//                dialView.setStyleBitmap(null, styleBaseOnWidth)
//            }
//        })
//}

//suspend fun glideGetBitmap(context: Context, uri: Uri) = suspendCancellableCoroutine {
//    val request = Glide.with(context)
//    val target = request
//        .asBitmap()
//        .load(uri)
//        .into(object : CustomTarget<Bitmap?>() {
//            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
//                it.resume(resource)
//            }
//
//            override fun onLoadFailed(errorDrawable: Drawable?) {
//                it.resumeWithException(IOException())
//            }
//
//            override fun onLoadCleared(placeholder: Drawable?) {
//
//            }
//        })
//    it.invokeOnCancellation {
//        request.clear(target)
//    }
//}
fun sendKeyCode(keyCode: Int) {
    try {
        val keyCommand = "input keyevent $keyCode"
        val runtime = Runtime.getRuntime()
        val proc = runtime.exec(keyCommand)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun getGridSpanCount(context: Context, baseSpanCount: Int = 3): Int {
    val orientation = context.resources.configuration.orientation
    return if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        baseSpanCount
    } else {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.widthPixels / (displayMetrics.heightPixels / baseSpanCount)
    }
}