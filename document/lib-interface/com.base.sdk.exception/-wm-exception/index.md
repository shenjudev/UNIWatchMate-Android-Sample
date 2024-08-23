//[lib-interface](../../../index.md)/[com.base.sdk.exception](../index.md)/[WmException](index.md)

# WmException

abstract class [WmException](index.md) : [RuntimeException](https://developer.android.com/reference/kotlin/java/lang/RuntimeException.html)

统一抛出的异常，其他SDK抛出的异常最好转换为 WmException或其子类 (Unified exception, the exception thrown by other SDKs should be converted to WmException or its subclass)

#### Inheritors

| |
|---|
| [WmDisconnectedException](../-wm-disconnected-exception/index.md) |
| [WmInvalidParamsException](../-wm-invalid-params-exception/index.md) |
| [WmTimeOutException](../-wm-time-out-exception/index.md) |
| [WmTransferException](../-wm-transfer-exception/index.md) |

## Constructors

| | |
|---|---|
| [WmException](-wm-exception.md) | [androidJvm]<br>constructor()constructor(message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?)constructor(cause: [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)?)constructor(message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, cause: [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)?) |

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
