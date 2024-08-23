//[lib-interface](../../../index.md)/[com.base.sdk.entity.settings](../index.md)/[WmEmergencyCall](index.md)

# WmEmergencyCall

[androidJvm]\
class [WmEmergencyCall](index.md)(var isEnabled: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), val emergencyContacts: [MutableList](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-list/index.html)&lt;[WmContact](../../com.base.sdk.entity.apps/-wm-contact/index.md)&gt;)

Emergency call(紧急联系人)

## Constructors

| | |
|---|---|
| [WmEmergencyCall](-wm-emergency-call.md) | [androidJvm]<br>constructor(isEnabled: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), emergencyContacts: [MutableList](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-list/index.html)&lt;[WmContact](../../com.base.sdk.entity.apps/-wm-contact/index.md)&gt;) |

## Properties

| Name | Summary |
|---|---|
| [emergencyContacts](emergency-contacts.md) | [androidJvm]<br>val [emergencyContacts](emergency-contacts.md): [MutableList](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-list/index.html)&lt;[WmContact](../../com.base.sdk.entity.apps/-wm-contact/index.md)&gt;<br>Emergency contacts(紧急联系人列表) |
| [isEnabled](is-enabled.md) | [androidJvm]<br>var [isEnabled](is-enabled.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Whether to enable emergency contact(是否启用紧急联系人) |

## Functions

| Name | Summary |
|---|---|
| [addContact](add-contact.md) | [androidJvm]<br>fun [addContact](add-contact.md)(contact: [WmContact](../../com.base.sdk.entity.apps/-wm-contact/index.md)) |
| [removeContact](remove-contact.md) | [androidJvm]<br>fun [removeContact](remove-contact.md)(contact: [WmContact](../../com.base.sdk.entity.apps/-wm-contact/index.md)) |
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
