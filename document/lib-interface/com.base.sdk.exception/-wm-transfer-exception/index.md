//[lib-interface](../../../index.md)/[com.base.sdk.exception](../index.md)/[WmTransferException](index.md)

# WmTransferException

[androidJvm]\
class [WmTransferException](index.md)(val error: [WmTransferError](../-wm-transfer-error/index.md), val msg: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [WmException](../-wm-exception/index.md)

表示设备端传输引起的异常（Exception when the device is transferred by the device)

## Constructors

| | |
|---|---|
| [WmTransferException](-wm-transfer-exception.md) | [androidJvm]<br>constructor(error: [WmTransferError](../-wm-transfer-error/index.md), msg: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [cause](index.md#-654012527%2FProperties%2F-721212597) | [androidJvm]<br>open val [cause](index.md#-654012527%2FProperties%2F-721212597): [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)? |
| [error](error.md) | [androidJvm]<br>val [error](error.md): [WmTransferError](../-wm-transfer-error/index.md) |
| [message](index.md#1824300659%2FProperties%2F-721212597) | [androidJvm]<br>open val [message](index.md#1824300659%2FProperties%2F-721212597): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? |
| [msg](msg.md) | [androidJvm]<br>val [msg](msg.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |

## Functions

| Name | Summary |
|---|---|
| [addSuppressed](index.md#282858770%2FFunctions%2F-721212597) | [androidJvm]<br>fun [addSuppressed](index.md#282858770%2FFunctions%2F-721212597)(p0: [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)) |
| [fillInStackTrace](index.md#-1102069925%2FFunctions%2F-721212597) | [androidJvm]<br>open fun [fillInStackTrace](index.md#-1102069925%2FFunctions%2F-721212597)(): [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html) |
| [getLocalizedMessage](index.md#1043865560%2FFunctions%2F-721212597) | [androidJvm]<br>open fun [getLocalizedMessage](index.md#1043865560%2FFunctions%2F-721212597)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [getStackTrace](index.md#2050903719%2FFunctions%2F-721212597) | [androidJvm]<br>open fun [getStackTrace](index.md#2050903719%2FFunctions%2F-721212597)(): [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[StackTraceElement](https://developer.android.com/reference/kotlin/java/lang/StackTraceElement.html)&gt; |
| [getSuppressed](index.md#672492560%2FFunctions%2F-721212597) | [androidJvm]<br>fun [getSuppressed](index.md#672492560%2FFunctions%2F-721212597)(): [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)&gt; |
| [initCause](index.md#-418225042%2FFunctions%2F-721212597) | [androidJvm]<br>open fun [initCause](index.md#-418225042%2FFunctions%2F-721212597)(p0: [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)): [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html) |
| [printStackTrace](index.md#-1769529168%2FFunctions%2F-721212597) | [androidJvm]<br>open fun [printStackTrace](index.md#-1769529168%2FFunctions%2F-721212597)()<br>open fun [printStackTrace](index.md#1841853697%2FFunctions%2F-721212597)(p0: [PrintStream](https://developer.android.com/reference/kotlin/java/io/PrintStream.html))<br>open fun [printStackTrace](index.md#1175535278%2FFunctions%2F-721212597)(p0: [PrintWriter](https://developer.android.com/reference/kotlin/java/io/PrintWriter.html)) |
| [setStackTrace](index.md#2135801318%2FFunctions%2F-721212597) | [androidJvm]<br>open fun [setStackTrace](index.md#2135801318%2FFunctions%2F-721212597)(p0: [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[StackTraceElement](https://developer.android.com/reference/kotlin/java/lang/StackTraceElement.html)&gt;) |
