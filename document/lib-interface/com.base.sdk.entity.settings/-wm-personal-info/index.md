//[lib-interface](../../../index.md)/[com.base.sdk.entity.settings](../index.md)/[WmPersonalInfo](index.md)

# WmPersonalInfo

[androidJvm]\
data class [WmPersonalInfo](index.md)(val height: [Short](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short/index.html), val weight: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val gender: [WmPersonalInfo.Gender](-gender/index.md), val birthDate: [WmPersonalInfo.BirthDate](-birth-date/index.md))

Personal information(个人信息)

## Constructors

| | |
|---|---|
| [WmPersonalInfo](-wm-personal-info.md) | [androidJvm]<br>constructor(height: [Short](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short/index.html), weight: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), gender: [WmPersonalInfo.Gender](-gender/index.md), birthDate: [WmPersonalInfo.BirthDate](-birth-date/index.md)) |

## Types

| Name | Summary |
|---|---|
| [BirthDate](-birth-date/index.md) | [androidJvm]<br>data class [BirthDate](-birth-date/index.md)(val year: [Short](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short/index.html), val month: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html), val day: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html)) |
| [Gender](-gender/index.md) | [androidJvm]<br>enum [Gender](-gender/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[WmPersonalInfo.Gender](-gender/index.md)&gt; |

## Properties

| Name | Summary |
|---|---|
| [birthDate](birth-date.md) | [androidJvm]<br>val [birthDate](birth-date.md): [WmPersonalInfo.BirthDate](-birth-date/index.md)<br>Birth date(出生日期) |
| [gender](gender.md) | [androidJvm]<br>val [gender](gender.md): [WmPersonalInfo.Gender](-gender/index.md)<br>Gender(性别) |
| [height](height.md) | [androidJvm]<br>val [height](height.md): [Short](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short/index.html)<br>Height(身高) |
| [weight](weight.md) | [androidJvm]<br>val [weight](weight.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Weight(体重 克) |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
