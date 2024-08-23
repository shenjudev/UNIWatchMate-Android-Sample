//[lib-interface](../../../index.md)/[com.base.sdk.exception](../index.md)/[WmDisconnectedException](index.md)

# WmDisconnectedException

[androidJvm]\
class [WmDisconnectedException](index.md)(address: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null) : [WmException](../-wm-exception/index.md)

表示设备连接的异常，一般在SDK不能识别此设备的时候出现 (Exception when the device is not recognized by the SDK)

## Constructors

| | |
|---|---|
| [WmDisconnectedException](-wm-disconnected-exception.md) | [androidJvm]<br>constructor(address: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null) |

## Properties

| Name | Summary |
|---|---|
| [cause](../-wm-transfer-exception/index.md#-654012527%2FProperties%2F-721212597) | [androidJvm]<br>open val [cause](../-wm-transfer-exception/index.md#-654012527%2FProperties%2F-721212597): [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)? |
| [message](../-wm-transfer-exception/index.md#1824300659%2FProperties%2F-721212597) | [androidJvm]<br>open val [message](../-wm-transfer-exception/index.md#1824300659%2FProperties%2F-721212597): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? |

## Functions

| Name | Summary |
|---|---|
| [addSuppressed](../-wm-transfer-exception/index.md#282858770%2FFunctions%2F-721212597) | [androidJvm]<br>fun [addSuppressed](../-wm-transfer-exception/index.md#282858770%2FFunctions%2F-721212597)(p0: [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)) |
| [fillInStackTrace](../-wm-transfer-exception/index.md#-1102069925%2FFunctions%2F-721212597) | [androidJvm]<br>open fun [fillInStackTrace](../-wm-transfer-exception/index.md#-1102069925%2FFunctions%2F-721212597)(): [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html) |
| [getLocalizedMessage](../-wm-transfer-exception/index.md#1043865560%2FFunctions%2F-721212597) | [androidJvm]<br>open fun [getLocalizedMessage](../-wm-transfer-exception/index.md#1043865560%2FFunctions%2F-721212597)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [getStackTrace](../-wm-transfer-exception/index.md#2050903719%2FFunctions%2F-721212597) | [androidJvm]<br>open fun [getStackTrace](../-wm-transfer-exception/index.md#2050903719%2FFunctions%2F-721212597)(): [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[StackTraceElement](https://developer.android.com/reference/kotlin/java/lang/StackTraceElement.html)&gt; |
| [getSuppressed](../-wm-transfer-exception/index.md#672492560%2FFunctions%2F-721212597) | [androidJvm]<br>fun [getSuppressed](../-wm-transfer-exception/index.md#672492560%2FFunctions%2F-721212597)(): [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)&gt; |
| [initCause](../-wm-transfer-exception/index.md#-418225042%2FFunctions%2F-721212597) | [androidJvm]<br>open fun [initCause](../-wm-transfer-exception/index.md#-418225042%2FFunctions%2F-721212597)(p0: [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)): [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html) |
| [printStackTrace](../-wm-transfer-exception/index.md#-1769529168%2FFunctions%2F-721212597) | [androidJvm]<br>open fun [printStackTrace](../-wm-transfer-exception/index.md#-1769529168%2FFunctions%2F-721212597)()<br>open fun [printStackTrace](../-wm-transfer-exception/index.md#1841853697%2FFunctions%2F-721212597)(p0: [PrintStream](https://developer.android.com/reference/kotlin/java/io/PrintStream.html))<br>open fun [printStackTrace](../-wm-transfer-exception/index.md#1175535278%2FFunctions%2F-721212597)(p0: [PrintWriter](https://developer.android.com/reference/kotlin/java/io/PrintWriter.html)) |
| [setStackTrace](../-wm-transfer-exception/index.md#2135801318%2FFunctions%2F-721212597) | [androidJvm]<br>open fun [setStackTrace](../-wm-transfer-exception/index.md#2135801318%2FFunctions%2F-721212597)(p0: [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[StackTraceElement](https://developer.android.com/reference/kotlin/java/lang/StackTraceElement.html)&gt;) |
