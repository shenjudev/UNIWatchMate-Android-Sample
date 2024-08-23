//[lib-interface](../../../index.md)/[com.base.sdk.port](../index.md)/[AbWmTransferFile](index.md)/[startTransfer](start-transfer.md)

# startTransfer

[androidJvm]\
abstract fun [startTransfer](start-transfer.md)(fileType: [FileType](../-file-type/index.md), files: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[File](https://developer.android.com/reference/kotlin/java/io/File.html)&gt;, appendixSize: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0): Observable&lt;[WmTransferState](../-wm-transfer-state/index.md)&gt;

传输文件（File transfer）

#### Parameters

androidJvm

| | |
|---|---|
| fileType | 文件类型(File type) |
| files | 文件列表(List of files) |
| appendixSize | 附加数据大小(Additional data size) |
