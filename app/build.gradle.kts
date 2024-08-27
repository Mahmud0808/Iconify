import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.drdisagree.iconify"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.drdisagree.iconify"
        minSdk = 31
        targetSdk = 34
        versionCode = 20
        versionName = "6.8.0"
        setProperty("archivesBaseName", "Iconify v$versionName")
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
    } catch (ignored: Exception) {
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = true
            proguardFiles("proguard-android-optimize.txt", "proguard.pro", "proguard-rules.pro")
            applicationIdSuffix = ".debug"
            resValue("string", "derived_app_name", "Iconify (Debug)")
            signingConfig = releaseSigning
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = true
            proguardFiles("proguard-android-optimize.txt", "proguard.pro", "proguard-rules.pro")
            resValue("string", "derived_app_name", "Iconify")
            signingConfig = releaseSigning
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
        aidl = true
    }

    compileOptions {
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

dependencies {
    // Kotlin
    implementation(libs.androidx.core.ktx)

    // Data Binding
    implementation(libs.library)

    // Xposed API
    compileOnly(files("libs/api-82.jar"))
    compileOnly(files("libs/api-82-sources.jar"))

    // The core module that provides APIs to a shell
    implementation(libs.core)
    // Optional: APIs for creating root services. Depends on ":core"
    implementation(libs.service)
    // Optional: Provides remote file system support
    implementation(libs.nio)

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
    //noinspection KaptUsageInsteadOfKsp
    kapt(libs.compiler)

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
    implementation(libs.com.google.android.gms.play.services.mlkit.subject.segmentation)
    implementation(libs.play.services.base)

    // Blur View
    implementation(libs.blurview)

    // Misc
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.slf4j.api)
    implementation(libs.commons.text)
}

tasks.register("printVersionName") {
    println(android.defaultConfig.versionName?.replace("-(Stable|Beta)".toRegex(), ""))
}
