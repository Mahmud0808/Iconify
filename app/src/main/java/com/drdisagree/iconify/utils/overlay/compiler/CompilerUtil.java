package com.drdisagree.iconify.utils.overlay.compiler;

import android.os.Build;
import android.util.Log;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.common.References;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class CompilerUtil {

    private static final String TAG = CompilerUtil.class.getSimpleName();

    public static String createManifestContent(
            String overlayName,
            String targetPackage

    ) {
        try {
            String category = getCategory(overlayName);
            if (!overlayName.startsWith("IconifyComponent")) {
                overlayName = "IconifyComponent" + overlayName;
            }
            if (!overlayName.endsWith(".overlay")) {
                overlayName = overlayName + ".overlay";
            }

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document document = documentBuilder.newDocument();
            Element rootElement = document.createElement("manifest");
            rootElement.setAttribute("xmlns:android", "http://schemas.android.com/apk/res/android");
            rootElement.setAttribute("package", overlayName);
            rootElement.setAttribute("android:versionName", "v" + BuildConfig.VERSION_NAME);

            Element usesSdkElement = document.createElement("uses-sdk");
            usesSdkElement.setAttribute("android:minSdkVersion", String.valueOf(BuildConfig.MIN_SDK_VERSION));
            usesSdkElement.setAttribute("android:targetSdkVersion", String.valueOf(Build.VERSION.SDK_INT));
            rootElement.appendChild(usesSdkElement);

            Element overlayElement = document.createElement("overlay");
            overlayElement.setAttribute("android:category", category);
            overlayElement.setAttribute("android:priority", String.valueOf(1));
            overlayElement.setAttribute("android:targetPackage", targetPackage);
            overlayElement.setAttribute("android:isStatic", "false");
            rootElement.appendChild(overlayElement);

            Element applicationElement = document.createElement("application");
            applicationElement.setAttribute("android:label", overlayName.replace(".overlay", ""));
            applicationElement.setAttribute("allowBackup", "false");
            applicationElement.setAttribute("android:hasCode", "false");

            final HashMap<String, String> metadataNameToValueMap = new HashMap<>();
            metadataNameToValueMap.put(References.METADATA_OVERLAY_PARENT, BuildConfig.APPLICATION_ID);
            metadataNameToValueMap.put(References.METADATA_OVERLAY_TARGET, targetPackage);
            metadataNameToValueMap.put(References.METADATA_THEME_VERSION, String.valueOf(BuildConfig.VERSION_CODE));
            metadataNameToValueMap.put(References.METADATA_THEME_CATEGORY, category);

            metadataNameToValueMap.forEach((key, value) -> {
                Element element = document.createElement("meta-data");
                element.setAttribute("android:name", key);
                element.setAttribute("android:value", value);
                applicationElement.appendChild(element);
            });

            rootElement.appendChild(applicationElement);
            document.appendChild(rootElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            Source domSource = new DOMSource(document);
            StringWriter outWriter = new StringWriter();
            Result streamResult = new StreamResult(outWriter);
            transformer.transform(domSource, streamResult);

            return outWriter.getBuffer().toString();
        } catch (ParserConfigurationException | TransformerException e) {
            Log.i(TAG, "Failed to create manifest for " + overlayName, e);
        }
        return "";
    }

    public static String getOverlayName(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();

        return fileName.replaceAll("IconifyComponent|-unsigned|-unaligned|.apk", "");
    }

    private static String getCategory(String pkgName) {
        String category = References.OVERLAY_CATEGORY_PREFIX;
        pkgName = pkgName.replace("IconifyComponent", "").replace(".overlay", "");

        if (pkgName.contains("MPIP")) {
            pkgName = keepFirstDigit(pkgName);
            category += "media_player_icon_pack_" + pkgName.toLowerCase();
        } else {
            pkgName = removeAllDigits(pkgName);

            switch (pkgName) {
                case "AMAC", "AMGC" -> category += "stock_monet_colors";
                case "BBN", "BBP" -> category += "brightness_bar_style";
                case "MPA", "MPB", "MPS" -> category += "media_player_style";
                case "NFN", "NFP" -> category += "notification_style";
                case "QSNT", "QSPT" -> category += "qs_tile_text_style";
                case "QSSN", "QSSP" -> category += "qs_shape_style";
                case "IPAS" -> category += "icon_pack_android_style";
                case "IPSUI" -> category += "icon_pack_sysui_style";
                default -> category += "iconify_component_" + pkgName.toLowerCase();
            }
        }

        return category;
    }

    private static String removeAllDigits(String input) {
        String regex = "\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll("");
    }

    private static String keepFirstDigit(String input) {
        StringBuilder output = new StringBuilder();
        boolean firstDigitFound = false;

        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                if (!firstDigitFound) {
                    output.append(c);
                    firstDigitFound = true;
                }
            } else {
                output.append(c);
            }
        }

        return output.toString();
    }
}
