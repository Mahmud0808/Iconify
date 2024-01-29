# Kotlin
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
	public static void check*(...);
	public static void throw*(...);
}
-assumenosideeffects class java.util.Objects {
    public static ** requireNonNull(...);
}

# Strip debug log
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

# Xposed
-keep class de.robv.android.xposed.**
-keep class com.drdisagree.iconify.xposed.InitHook
-keepnames class com.drdisagree.iconify.xposed.**
-keepnames class com.drdisagree.iconify.config.XPrefs
-keep class com.drdisagree.iconify.xposed.** {
    <init>(android.content.Context);
}

# EventBus
-keepattributes *Annotation*
-keepclassmembers,allowoptimization,allowobfuscation class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep,allowoptimization,allowobfuscation enum org.greenrobot.eventbus.ThreadMode { *; }

# If using AsyncExecutord, keep required constructor of default event used.
# Adjust the class name if a custom failure event type is used.
-keepclassmembers,allowoptimization,allowobfuscation class org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# Accessed via reflection, avoid renaming or removal
-keep,allowoptimization,allowobfuscation class org.greenrobot.eventbus.android.AndroidComponentsImpl

# Keep the ConstraintLayout Motion class
-keep,allowoptimization,allowobfuscation class androidx.constraintlayout.motion.widget.** { *; }

# Obfuscation
-repackageclasses
-allowaccessmodification