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
-keep,allowobfuscation,allowoptimization class com.drdisagree.iconify.app.MainActivity { *; }
-keepclassmembers,allowobfuscation,allowoptimization class com.drdisagree.iconify.ui.activities.**
-keepclassmembers,allowobfuscation,allowoptimization class com.drdisagree.iconify.ui.fragments.**

# Xposed
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keepattributes RuntimeVisibleAnnotations
-keep,allowobfuscation,allowoptimization public class * extends io.github.libxposed.api.XposedModule {
    public <init>(...);
    public void onModuleLoaded(...);
    public void onPackageLoaded(...);
    public void onPackageReady(...);
    public void onSystemServerStarting(...);
}

# Xposed framework stubs
-keep class de.robv.android.xposed.** { *; }

# XPrefs: name and public API must be stable for cross-process access
-keepnames class com.drdisagree.iconify.xposed.utils.XPrefs
-keepclassmembers,allowoptimization,allowobfuscation class com.drdisagree.iconify.xposed.utils.XPrefs {
    public *;
}

# Xposed logs
-keep class io.github.libxposed.api.XposedInterface {
    public void log(int, java.lang.String, java.lang.String);
    public void log(int, java.lang.String, java.lang.String, java.lang.Throwable);
}

# Service connection
-keepclassmembers class * implements android.content.ServiceConnection {
    public void onServiceConnected(...);
    public void onServiceDisconnected(...);
}

# MLKit
-keep class com.google.mlkit.common.internal.** { *; }
-keep class com.google.mlkit.vision.common.internal.** { *; }
-keep class com.google.mlkit.vision.segmentation.subject.** { *; }
-keep class com.google.mlkit.vision.segmentation.subject.internal.** { *; }

# Weather
-keep,allowoptimization class com.drdisagree.iconify.core.utils.weather.** { *; }

# Obfuscation
-repackageclasses
-allowaccessmodification

# Root Service
-keepclassmembers,allowoptimization class com.drdisagree.iconify.services.providers.RootProviderProxy {
    public *;
}
-keepclassmembers,allowoptimization interface com.drdisagree.iconify.services.providers.IRootProviderProxy {
    public *;
}

# AIDL Classes
-keepclassmembers,allowoptimization interface com.drdisagree.iconify.**.I* {
    public *;
}
-keepclassmembers,allowoptimization class com.drdisagree.iconify.**.I*$Stub {
    public *;
}
-keepclassmembers,allowoptimization class com.drdisagree.iconify.**.I*$Stub$Proxy {
    public *;
}

# Keep all drawable resources
-keep class androidx.compose.ui.res.** { *; }
-keepclassmembers class **.R$drawable { <fields>; }