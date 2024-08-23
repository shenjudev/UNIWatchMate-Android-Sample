//[lib-interface](../../../index.md)/[com.base.sdk.entity.apps](../index.md)/[WmSport](index.md)

# WmSport

data class [WmSport](index.md)(val id: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val type: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val buildIn: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))

运动,运动id和类型需要终端、设备端、云端统一，APP根据需求自行定义 (Sport id and type need to be unified with the terminal, device, cloud, and APP according to requirements)

#### Parameters

androidJvm

| | |
|---|---|
| id | 运动id(Sport ID) |
| type | 可以作为二级分类标识(Type can be used as a secondary classification identifier) |
| buildIn | 是否内置运动(Whether the sport is built-in) |

## Constructors

| | |
|---|---|
| [WmSport](-wm-sport.md) | [androidJvm]<br>constructor(id: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), type: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), buildIn: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [buildIn](build-in.md) | [androidJvm]<br>val [buildIn](build-in.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [id](id.md) | [androidJvm]<br>val [id](id.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [type](type.md) | [androidJvm]<br>val [type](type.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
