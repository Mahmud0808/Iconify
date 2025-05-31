import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.parcelize)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "com.drdisagree.iconify"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.drdisagree.iconify"
        minSdk = 31
        targetSdk = 35
        versionCode = 24
        versionName = "7.2.0"
        multiDexEnabled = true
        setProperty("archivesBaseName", "Iconify v${defaultConfig.versionName}")
        buildConfigField("int", "MIN_SDK_VERSION", "$minSdk")
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

    buildTypes {
        debug {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = false
            proguardFiles("proguard-android-optimize.txt", "proguard.pro", "proguard-rules.pro")
            applicationIdSuffix = ".debug"
            resValue("string", "derived_app_name", "Iconify (Debug)")
            signingConfig = releaseSigning
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = false
            proguardFiles("proguard-android-optimize.txt", "proguard.pro", "proguard-rules.pro")
            resValue("string", "derived_app_name", "Iconify")
            signingConfig = releaseSigning
        }
    }

    flavorDimensions += "distribution"

    productFlavors {
        create("standard") {
            isDefault = true
            dimension = "distribution"
            resValue("string", "derived_app_name", "Iconify")
        }

        create("foss") {
            dimension = "distribution"
            applicationIdSuffix = ".foss"
            resValue("string", "derived_app_name", "Iconify (FOSS)")
        }
    }

    sourceSets {
        getByName("standard") {
            java.srcDirs("src/standard/java")
        }

        getByName("foss") {
            java.srcDirs("src/foss/java")
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

    buildFeatures {
        viewBinding = true
        buildConfig = true
        aidl = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        jniLibs.excludes += setOf(
            "/META-INF/*",
            "/META-INF/versions/**",
            "/org/bouncycastle/**",
            "/kotlin/**",
            "/kotlinx/**"
        )

        resources.excludes += setOf(
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

        jniLibs.useLegacyPackaging = true
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:-deprecation")
}

gradle.taskGraph.whenReady {
    gradle.startParameter.showStacktrace = ShowStacktrace.ALWAYS
    gradle.startParameter.warningMode = WarningMode.Summary
}

val fossImplementation by configurations
val standardImplementation by configurations

dependencies {
    // Kotlin
    implementation(libs.androidx.core.ktx)

    // Core Library Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Data Binding
    implementation(libs.library)
    implementation(libs.androidx.palette.ktx)

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

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Color Picker
    implementation(libs.jaredrummler.colorpicker)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Material Components
    implementation(libs.material)

    // APK Signer
    implementation(libs.bcpkix.jdk18on)

    // Zip Util
    implementation(libs.zip4j)

    // Preference
    implementation(libs.androidx.preference.ktx)

    // Remote Preference
    implementation(libs.remotepreferences)

    // Flexbox
    implementation(libs.flexbox)

    // Glide
    implementation(libs.glide)
    ksp(libs.glide.compiler)

    // RecyclerView
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.recyclerview.selection)

    // ViewPager2
    implementation(libs.androidx.viewpager2)

    // Circle Indicator
    implementation(libs.circleindicator)

    // Lottie Animation
    implementation(libs.lottie)

    // HTML Parser
    implementation(libs.jsoup)

    // Collapsing Toolbar with subtitle
    implementation(libs.collapsingtoolbarlayout.subtitle)

    // Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Concurrency
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.concurrent.futures)
    implementation(libs.guava)

    // Event Bus
    implementation(libs.eventbus)

    // Dots Indicator
    implementation(libs.dotsindicator)

    // Fading Edge Layout
    implementation(libs.fadingedgelayout)

    // Google Subject Segmentation - MLKit
    standardImplementation(libs.com.google.android.gms.play.services.mlkit.subject.segmentation)
    standardImplementation(libs.play.services.base)

    // Blur View
    implementation(libs.blurview)

    // Misc
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.slf4j.api)
    implementation(libs.commons.text)

    // OkHttp
    implementation(libs.okhttp)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}

tasks.register("printVersionName") {
    println(android.defaultConfig.versionName?.replace("-(Stable|Beta)".toRegex(), ""))
}