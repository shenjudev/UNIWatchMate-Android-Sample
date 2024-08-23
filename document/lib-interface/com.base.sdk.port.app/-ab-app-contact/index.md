//[lib-interface](../../../index.md)/[com.base.sdk.port.app](../index.md)/[AbAppContact](index.md)

# AbAppContact

[androidJvm]\
abstract class [AbAppContact](index.md)

应用模块-通讯录(Application module - contact)

## Constructors

| | |
|---|---|
| [AbAppContact](-ab-app-contact.md) | [androidJvm]<br>constructor() |

## Properties

| Name | Summary |
|---|---|
| [getContactList](get-contact-list.md) | [androidJvm]<br>abstract val [getContactList](get-contact-list.md): Single&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmContact](../../com.base.sdk.entity.apps/-wm-contact/index.md)&gt;&gt;<br>从设备端获取通讯录列表(Get contact list from device) |
| [observableContactDelete](observable-contact-delete.md) | [androidJvm]<br>abstract val [observableContactDelete](observable-contact-delete.md): Observable&lt;[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)&gt;<br>监听设备端删除通讯录（Listen for the device to delete the contact list) |
| [observableContactList](observable-contact-list.md) | [androidJvm]<br>abstract val [observableContactList](observable-contact-list.md): Observable&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmContact](../../com.base.sdk.entity.apps/-wm-contact/index.md)&gt;&gt;<br>App监听设备端修改通讯录(Listen for the device to modify the contact list) |

## Functions

| Name | Summary |
|---|---|
| [observableEmergencyContacts](observable-emergency-contacts.md) | [androidJvm]<br>abstract fun [observableEmergencyContacts](observable-emergency-contacts.md)(): Observable&lt;[WmEmergencyCall](../../com.base.sdk.entity.settings/-wm-emergency-call/index.md)&gt;<br>获取紧急联系人(Get emergency contact) |
| [updateContactList](update-contact-list.md) | [androidJvm]<br>abstract fun [updateContactList](update-contact-list.md)(contactList: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmContact](../../com.base.sdk.entity.apps/-wm-contact/index.md)&gt;): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>App同步通讯录到设备(App synchronizes the contact list to the device) |
| [updateEmergencyContact](update-emergency-contact.md) | [androidJvm]<br>abstract fun [updateEmergencyContact](update-emergency-contact.md)(contacts: [WmEmergencyCall](../../com.base.sdk.entity.settings/-wm-emergency-call/index.md)): Single&lt;[WmEmergencyCall](../../com.base.sdk.entity.settings/-wm-emergency-call/index.md)&gt;<br>设置紧急联系人(Set emergency contact) |
