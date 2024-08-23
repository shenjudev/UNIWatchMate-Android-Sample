//[lib-interface](../../../index.md)/[com.base.sdk.port](../index.md)/[AbWmTransferFile](index.md)

# AbWmTransferFile

[androidJvm]\
abstract class [AbWmTransferFile](index.md)

传输文件功能抽象类(Abstract class for file transfer)

## Constructors

| | |
|---|---|
| [AbWmTransferFile](-ab-wm-transfer-file.md) | [androidJvm]<br>constructor() |

## Functions

| Name | Summary |
|---|---|
| [cancelTransfer](cancel-transfer.md) | [androidJvm]<br>abstract fun [cancelTransfer](cancel-transfer.md)(): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt; |
| [startTransfer](start-transfer.md) | [androidJvm]<br>abstract fun [startTransfer](start-transfer.md)(fileType: [FileType](../-file-type/index.md), files: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[File](https://developer.android.com/reference/kotlin/java/io/File.html)&gt;, appendixSize: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0): Observable&lt;[WmTransferState](../-wm-transfer-state/index.md)&gt;<br>传输文件（File transfer） |
