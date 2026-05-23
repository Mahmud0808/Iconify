import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.parcelize)
}

android {
    namespace = "com.drdisagree.iconify"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.drdisagree.iconify"
        minSdk = 36
        targetSdk = 36
        versionCode = 26
        versionName = "8.0.0"
        multiDexEnabled = true
        buildConfigField("int", "MIN_SDK_VERSION", "$minSdk")
        buildConfigField("int", "OVERLAY_VERSION_CODE", "4")
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    var releaseSigning = signingConfigs.getByName("debug")

    try {
        val keystoreProperties = Properties()
        FileInputStream(keystorePropertiesFile).use { inputStream ->
            keystoreProperties.load(inputStream)
        }

        releaseSigning = signingConfigs.create("release") {
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
            storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
        }
    } catch (_: Exception) {
    }

    val isCiBuild = System.getenv("CI") == "true"

    buildTypes {
        debug {
            isMinifyEnabled = isCiBuild
            isShrinkResources = isCiBuild
            applicationIdSuffix = ".debug"
            signingConfig = releaseSigning
            resValue("string", "derived_app_name", "Iconify (Debug)")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro", "proguard-debug.pro"
            )
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = releaseSigning
            resValue("string", "derived_app_name", "Iconify")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro", "proguard-release.pro"
            )
        }
    }

    if (hasProperty("splitApks")) {
        splits {
            abi {
                isEnable = true
                reset()
                include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
                isUniversalApk = false
            }
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
        aidl = true
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/*",
                "/META-INF/versions/**",
                "/org/bouncycastle/**",
                "/kotlin/**",
                "/kotlinx/**",
                "rebel.xml",
                "/*.txt",
                "/*.bin",
                "/*.json"
            )
        }

        jniLibs {
            excludes += setOf(
                "/META-INF/*",
                "/META-INF/versions/**",
                "/org/bouncycastle/**",
                "/kotlin/**",
                "/kotlinx/**"
            )

            useLegacyPackaging = true
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

base {
    archivesName = "Iconify v${android.defaultConfig.versionName}"
}

tasks.withType<KotlinCompile>().configureEach {
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_3
        jvmTarget = JvmTarget.JVM_17
    }
}

tasks.register("renameApks") {
    dependsOn("assembleDebug", "assembleRelease")

    doLast {
        val variants = listOf("debug", "release")

        variants.forEach { variant ->
            val apkDir = layout.buildDirectory
                .dir("outputs/apk/$variant")
                .get()
                .asFile

            val apk = apkDir.listFiles()
                ?.firstOrNull { it.extension == "apk" }
                ?: return@forEach

            val versionName = android.defaultConfig.versionName
            val newName = "Iconify v${versionName}.apk"

            val renamed = File(apkDir, newName)

            apk.renameTo(renamed)
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:-deprecation")
}

gradle.taskGraph.whenReady {
    gradle.startParameter.showStacktrace = ShowStacktrace.ALWAYS
    gradle.startParameter.warningMode = WarningMode.Summary
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material3.window.size.class1)
    implementation(libs.androidx.asynclayoutinflater)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Core Library Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Material icons
    implementation(libs.androidx.compose.material.icons.extended)

    // Datastore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore)

    // Splashscreen
    implementation(libs.androidx.core.splashscreen)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Navigation Animation
    implementation(libs.accompanist.navigation.animation)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)

    // Xposed API
    // F-Droid disallow `api.xposed.info` since it's not a "Trusted Maven Repository".
    // So we create a mirror GitHub repository and obtain the library from `jitpack.io` instead.
    // Equivalent to `implementation 'de.robv.android.xposed:api:82'`.
    compileOnly(libs.xposedbridge)

    // The core module that provides APIs to a shell
    implementation(libs.su.core)
    // Optional: APIs for creating root services. Depends on ":core"
    implementation(libs.su.service)
    // Optional: Provides remote file system support
    implementation(libs.su.nio)

    // Zip Util
    implementation(libs.zip4j)

    // Remote Preference
    implementation(libs.remotepreferences)

    // Google Subject Segmentation - MLKit
    implementation(libs.com.google.android.gms.play.services.mlkit.subject.segmentation)
    implementation(libs.play.services.base)

    // APK Signer
    implementation(libs.bcpkix.jdk18on)

    // Liquid Glass
    implementation(libs.backdrop)

    // Haze Blur
    implementation(libs.haze.jetpack.compose)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Lottie animation
    implementation(libs.lottie.compose)

    // Clip shape
    implementation(libs.androidx.graphics.shapes)

    // Drawable painter
    implementation(libs.accompanist.drawablepainter)

    // Material3 Color Scheme
    implementation(libs.material.kolor)

    // Work Manager
    implementation(libs.androidx.work.runtime.ktx)

    // Concurrency
    implementation(libs.androidx.concurrent.futures)

    // OkHttp
    implementation(libs.okhttp)

    // Fading Edge Layout
    implementation(libs.fadingedgelayout)

    // Color Picker
    implementation(libs.colorpicker.compose)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Class initializer
    implementation (libs.objenesis)

    // Compose Markdown
    implementation(libs.compose.markdown)
}

tasks.register("printVersionName") {
    println(android.defaultConfig.versionName?.replace("-(Stable|Beta)".toRegex(), ""))
}