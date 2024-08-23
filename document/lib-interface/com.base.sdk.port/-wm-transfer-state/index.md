//[lib-interface](../../../index.md)/[com.base.sdk.port](../index.md)/[WmTransferState](index.md)

# WmTransferState

[androidJvm]\
class [WmTransferState](index.md)(var total: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))

传输状态(Transfer state)

## Constructors

| | |
|---|---|
| [WmTransferState](-wm-transfer-state.md) | [androidJvm]<br>constructor(total: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [index](--index--.md) | [androidJvm]<br>var [index](--index--.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>正在传输第几个文件(Which file is being transferred) |
| [progress](progress.md) | [androidJvm]<br>var [progress](progress.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>当前文件传输进度(Current file transfer progress) |
| [sendingFile](sending-file.md) | [androidJvm]<br>var [sendingFile](sending-file.md): [File](https://developer.android.com/reference/kotlin/java/io/File.html)?<br>返回当前传输的文件(The current file being transferred) |
| [state](state.md) | [androidJvm]<br>var [state](state.md): [State](../-state/index.md)<br>传输任务总体状态(Transfer task total state) |
| [total](total.md) | [androidJvm]<br>var [total](total.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
