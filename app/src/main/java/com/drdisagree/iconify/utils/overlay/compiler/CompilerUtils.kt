package com.drdisagree.iconify.utils.overlay.compiler

import android.os.Build
import android.util.Log
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.common.References
import java.io.File
import java.io.StringWriter
import java.util.Locale
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.Result
import javax.xml.transform.Source
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object CompilerUtils {

    private val TAG = CompilerUtils::class.java.simpleName

    fun createManifestContent(
        overlayName: String?,
        targetPackage: String?
    ): String {
        var mOverlayName = overlayName

        try {
            val category = getCategory(mOverlayName)

            if (!mOverlayName!!.startsWith("IconifyComponent")) {
                mOverlayName = "IconifyComponent$mOverlayName"
            }

            if (!mOverlayName.endsWith(".overlay")) {
                mOverlayName = "$mOverlayName.overlay"
            }

            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder = documentBuilderFactory.newDocumentBuilder()
            val document = documentBuilder.newDocument()

            val rootElement = document.createElement("manifest")
            rootElement.setAttribute("xmlns:android", "http://schemas.android.com/apk/res/android")
            rootElement.setAttribute("package", mOverlayName)
            rootElement.setAttribute("android:versionName", "v" + BuildConfig.VERSION_NAME)

            val usesSdkElement = document.createElement("uses-sdk")
            usesSdkElement.setAttribute(
                "android:minSdkVersion",
                BuildConfig.MIN_SDK_VERSION.toString()
            )
            usesSdkElement.setAttribute(
                "android:targetSdkVersion",
                Build.VERSION.SDK_INT.toString()
            )
            rootElement.appendChild(usesSdkElement)

            val overlayElement = document.createElement("overlay")
            overlayElement.setAttribute("android:category", category)
            overlayElement.setAttribute("android:priority", 1.toString())
            overlayElement.setAttribute("android:targetPackage", targetPackage)
            overlayElement.setAttribute("android:isStatic", "false")
            rootElement.appendChild(overlayElement)

            val applicationElement = document.createElement("application")
            applicationElement.setAttribute("android:label", mOverlayName.replace(".overlay", ""))
            applicationElement.setAttribute("allowBackup", "false")
            applicationElement.setAttribute("android:hasCode", "false")

            val metadataNameToValueMap = HashMap<String, String?>()
            metadataNameToValueMap[References.METADATA_OVERLAY_PARENT] = BuildConfig.APPLICATION_ID
            metadataNameToValueMap[References.METADATA_OVERLAY_TARGET] = targetPackage
            metadataNameToValueMap[References.METADATA_THEME_VERSION] =
                BuildConfig.VERSION_CODE.toString()
            metadataNameToValueMap[References.METADATA_THEME_CATEGORY] = category
            metadataNameToValueMap.forEach { (key: String?, value: String?) ->
                val element = document.createElement("meta-data")
                element.setAttribute("android:name", key)
                element.setAttribute("android:value", value)
                applicationElement.appendChild(element)
            }

            rootElement.appendChild(applicationElement)
            document.appendChild(rootElement)

            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            val domSource: Source = DOMSource(document)
            val outWriter = StringWriter()
            val streamResult: Result = StreamResult(outWriter)
            transformer.transform(domSource, streamResult)

            return outWriter.buffer.toString()
        } catch (e: ParserConfigurationException) {
            Log.i(TAG, "Failed to create manifest for $mOverlayName", e)
        } catch (e: TransformerException) {
            Log.i(TAG, "Failed to create manifest for $mOverlayName", e)
        }

        return ""
    }

    fun getOverlayName(filePath: String): String {
        val file = File(filePath)
        val fileName = file.getName()

        return fileName.replace("IconifyComponent|-unsigned|-unaligned|.apk".toRegex(), "")
    }

    private fun getCategory(pkgName: String?): String {
        var mPackageName = pkgName
        var category = References.OVERLAY_CATEGORY_PREFIX

        mPackageName = mPackageName!!.replace("IconifyComponent", "").replace(".overlay", "")

        if (mPackageName.contains("MPIP")) {
            mPackageName = keepFirstDigit(mPackageName)

            category += "media_player_icon_pack_" + mPackageName.lowercase(Locale.getDefault())
        } else {
            mPackageName = removeAllDigits(mPackageName)

            category += when (mPackageName) {
                "AMAC", "AMGC" -> {
                    "stock_monet_colors"
                }

                "BBN", "BBP" -> {
                    "brightness_bar_style"
                }

                "MPA", "MPB", "MPS" -> {
                    "media_player_style"
                }

                "NFN", "NFP" -> {
                    "notification_style"
                }

                "QSNT", "QSPT" -> {
                    "qs_tile_text_style"
                }

                "QSSN", "QSSP" -> {
                    "qs_shape_style"
                }

                "IPAS" -> {
                    "icon_pack_android_style"
                }

                "IPSUI" -> {
                    "icon_pack_sysui_style"
                }

                "WIFI" -> {
                    "icon_pack_wifi_icons"
                }

                "SGIC" -> {
                    "icon_pack_signal_icons"
                }

                else -> {
                    "iconify_component_" + mPackageName.lowercase(Locale.getDefault())
                }
            }
        }

        return category
    }

    private fun removeAllDigits(input: String): String {
        val regex = "\\d+"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(input)

        return matcher.replaceAll("")
    }

    private fun keepFirstDigit(input: String): String {
        val output = StringBuilder()
        var firstDigitFound = false

        for (c in input.toCharArray()) {
            if (Character.isDigit(c)) {
                if (!firstDigitFound) {
                    output.append(c)
                    firstDigitFound = true
                }
            } else {
                output.append(c)
            }
        }

        return output.toString()
    }
}
