-keep,allowoptimization,allowobfuscation class javax.annotation.Nullable
-dontwarn androidx.window.extensions.area.ExtensionWindowAreaPresentation
-dontwarn androidx.window.extensions.core.util.function.Consumer
-dontwarn androidx.window.extensions.core.util.function.Function
-dontwarn androidx.window.extensions.core.util.function.Predicate

-keepattributes Exceptions,LineNumberTable,Signature,SourceFile

-keepclasseswithmembernames,allowoptimization,allowobfuscation class * {
    native <methods>;
}

-keepclassmembers,allowoptimization,allowobfuscation enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Kotlin
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
    public static void throw*(...);
}
-assumenosideeffects class java.util.Objects {
    public static ** requireNonNull(...);
}

# Activity and Fragment names
-keep class com.drdisagree.iconify.ui.activities.**
-keep class com.drdisagree.iconify.ui.fragments.**

# Xposed framework stubs
-dontwarn io.github.libxposed.annotation.**
-adaptresourcefilecontents META-INF/xposed/java_init.list

# Xposed entry points (called directly by the framework)
-keep,allowoptimization class * extends io.github.libxposed.api.XposedModule {
    public <init>();
}

# Optimize method bodies, preserve Xposed lifecycle signatures
-keepclassmembers class * extends com.drdisagree.iconify.xposed.ModPack {
    public <init>(android.content.Context);
}

# XPrefs: name and public API must be stable for cross-process access
-keepnames class com.drdisagree.iconify.xposed.utils.XPrefs
-keepclassmembers,allowoptimization,allowobfuscation class com.drdisagree.iconify.xposed.utils.XPrefs {
    public *;
}

# MLKit
-keep class com.google.mlkit.common.internal.** { *; }
-keep class com.google.mlkit.vision.common.internal.** { *; }
-keep class com.google.mlkit.vision.segmentation.subject.** { *; }
-keep class com.google.mlkit.vision.segmentation.subject.internal.** { *; }

# Weather
-keepnames class com.drdisagree.iconify.utils.weather.**
-keep class com.drdisagree.iconify.utils.weather.** { *; }

# Obfuscation
-repackageclasses
-allowaccessmodification

# Root Service
-keep class com.drdisagree.iconify.services.providers.RootProviderProxy { *; }
-keep class com.drdisagree.iconify.services.providers.IRootProviderProxy { *; }

# AIDL Classes (scoped to your package to avoid matching SDK interfaces)
-keep interface com.drdisagree.iconify.**.I* { *; }
-keep class com.drdisagree.iconify.**.I*$Stub { *; }
-keep class com.drdisagree.iconify.**.I*$Stub$Proxy { *; }

# Keep all drawable resources
-keep class androidx.compose.ui.res.** { *; }
-keepclassmembers class **.R$drawable { <fields>; }

# Preference keys
-keepclassmembers enum com.drdisagree.iconify.data.keys.** {
    <fields>;
}