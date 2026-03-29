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
-keep class de.robv.android.xposed.** { *; }

# Xposed entry points (called directly by the framework)
-keep class com.drdisagree.iconify.xposed.InitHook {
    public <init>();
}
-keep class * implements de.robv.android.xposed.IXposedHookLoadPackage
-keep class * implements de.robv.android.xposed.IXposedHookInitPackageResources

# Optimize method bodies, preserve Xposed lifecycle signatures
-keepclassmembers,allowoptimization,allowobfuscation class com.drdisagree.iconify.xposed.** {
    public <init>();
    public <init>(android.content.Context);
    public void initZygote(de.robv.android.xposed.IXposedHookZygoteInit$StartupParam);
    public void handleLoadPackage(de.robv.android.xposed.callbacks.XC_LoadPackage$LoadPackageParam);
    public void handleInitPackageResources(de.robv.android.xposed.callbacks.XC_InitPackageResources$InitPackageResourcesParam);
}

# Hook callbacks
-keepclassmembers,allowoptimization,allowobfuscation class * extends de.robv.android.xposed.XC_MethodHook {
    protected void beforeHookedMethod(de.robv.android.xposed.XC_MethodHook$MethodHookParam);
    protected void afterHookedMethod(de.robv.android.xposed.XC_MethodHook$MethodHookParam);
}
-keepclassmembers,allowoptimization,allowobfuscation class * extends de.robv.android.xposed.XC_MethodReplacement {
    protected java.lang.Object replaceHookedMethod(de.robv.android.xposed.XC_MethodHook$MethodHookParam);
}

# XPrefs: name and public API must be stable for cross-process access
-keepnames class com.drdisagree.iconify.xposed.utils.XPrefs
-keepclassmembers,allowoptimization,allowobfuscation class com.drdisagree.iconify.xposed.utils.XPrefs {
    public *;
}

# Xposed logs
-keep class de.robv.android.xposed.XposedBridge {
    public static void log(java.lang.String);
    public static void log(java.lang.Throwable);
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