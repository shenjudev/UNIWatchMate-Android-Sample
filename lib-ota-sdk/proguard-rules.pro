# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep OTA SDK interfaces
-keep interface com.ota.sdk.api.** { *; }

# Keep OTA SDK public classes
-keep class com.ota.sdk.core.OtaTransferManager { *; }
-keep class com.ota.sdk.model.** { *; }
-keep class com.ota.sdk.constants.** { *; }

# Keep RxJava
-keep class io.reactivex.rxjava3.** { *; }
-dontwarn io.reactivex.rxjava3.**
