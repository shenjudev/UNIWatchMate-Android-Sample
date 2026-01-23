# Consumer ProGuard rules
# These rules will be applied to consumers of this library

-keep interface com.ota.sdk.api.** { *; }
-keep class com.ota.sdk.core.OtaTransferManager { *; }
-keep class com.ota.sdk.model.** { *; }
-keep class com.ota.sdk.constants.** { *; }
