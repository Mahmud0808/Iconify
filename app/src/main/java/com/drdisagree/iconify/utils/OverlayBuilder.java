package com.drdisagree.iconify.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import com.android.apksig.ApkSigner;
import com.drdisagree.iconify.Iconify;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

public class OverlayBuilder {

    public static final boolean ENABLE_DIRECT_ASSETS_LOGGING = false; // Self explanatory
    public static final boolean BYPASS_SUBSTRATUM_BUILDER_DELETION = false; // Do not delete cache?
    // These strings control the directories that Overlay uses
    public static final String EXTERNAL_STORAGE_CACHE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.iconify/";
    public static final String SUBSTRATUM_BUILDER_CACHE = "/OverlayBuilder/";
    // These are specific log tags for different classes
    public static final String SUBSTRATUM_BUILDER = "OverlayBuilder";
    public static final String FRAMEWORK = "android";
    public static final String SETTINGS = "com.android.settings";
    public static final String SYSTEMUI = "com.android.systemui";
    public static final String SYSTEMUI_HEADERS = "com.android.systemui.headers";
    public static final String SYSTEMUI_NAVBARS = "com.android.systemui.navbars";
    public static final String SYSTEMUI_STATUSBARS = "com.android.systemui.statusbars";
    public static final String SYSTEMUI_QSTILES = "com.android.systemui.tiles";
    public static final String SETTINGS_ICONS = "com.android.settings.icons";
    // Filter to adjust Settings elements
    public static final String[] ALLOWED_SETTINGS_ELEMENTS = {
            SETTINGS_ICONS,
    };
    // These are package names for our backend systems
    public static final String COMMON_PACKAGE = "com.iconify";
    // These strings control the current filter for themes
    public static final String metadataName = "Overlay_Name";
    public static final String metadataLegacy = "Overlay_Legacy";
    public static final String metadataEncryption = "Overlay_Encryption";
    public static final String metadataEncryptionValue = "onCompileVerify";
    public static final String metadataOverlayDevice = "Overlay_Device";
    public static final String metadataOverlayParent = "Overlay_Parent";
    public static final String metadataOverlayTarget = "Overlay_Target";
    public static final String metadataOverlayType1a = "Overlay_Type1a";
    public static final String metadataOverlayType1b = "Overlay_Type1b";
    public static final String metadataOverlayType1c = "Overlay_Type1c";
    public static final String metadataOverlayType2 = "Overlay_Type2";
    public static final String metadataOverlayType3 = "Overlay_Type3";
    public static final String metadataOverlayType4 = "Overlay_Type4";
    public static final String[] metadataOverlayTypes = new String[]{
            metadataOverlayType1a,
            metadataOverlayType1b,
            metadataOverlayType1c,
            metadataOverlayType2,
            metadataOverlayType3,
            metadataOverlayType4
    };
    public static final String metadataOverlayVersion = "Overlay_OverlayVersion";
    public static final String metadataThemeVersion = "Overlay_ThemeVersion";
    public static final boolean ENABLE_AAPT_OUTPUT = false; // WARNING, DEVELOPERS - BREAKS COMPILE
    // Filter to adjust SystemUI elements
    private static final String[] ALLOWED_SYSTEMUI_ELEMENTS = {
            SYSTEMUI_HEADERS,
            SYSTEMUI_NAVBARS,
            SYSTEMUI_NAVBARS,
            SYSTEMUI_QSTILES,
            SYSTEMUI_STATUSBARS
    };
    public boolean hasErroredOut = false;
    private boolean debug = false;
    private String errorLogs = "";
    private Context context;

    public OverlayBuilder(final Context context) {
        this.context = context;
    }

    // This string array contains all the SystemUI acceptable overlay packs
    public static Boolean allowedSystemUIOverlay(final String current) {
        return Arrays.asList(ALLOWED_SYSTEMUI_ELEMENTS).contains(current);
    }

    // This string array contains all the Settings acceptable overlay packs
    public static Boolean allowedSettingsOverlay(final String current) {
        return Arrays.asList(ALLOWED_SETTINGS_ELEMENTS).contains(current);
    }

    public static int getLiveOverlayVersion(Context context,
                                            String themePackageName,
                                            String packageName) {
        try {
            Context otherContext = context.createPackageContext(themePackageName, 0);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(otherContext.getAssets().open(
                            "overlays/" + packageName + "/version")))) {
                return Integer.parseInt(reader.readLine());
            } catch (IOException e) {
                if (e instanceof FileNotFoundException)
                    return 0;
                e.printStackTrace();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String createOverlayManifest(Context context,
                                               String themeName,
                                               String versionName,
                                               String overlayVersion,
                                               String targetPackage,
                                               String themeParent,
                                               String type1a,
                                               String type1b,
                                               String type1c,
                                               String type2,
                                               String type3,
                                               String type4,
                                               String packageNameOverride) {
        String packageName = "IconifyComponent" + themeName + ".overlay";

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // root elements
            Document document = documentBuilder.newDocument();
            Element rootElement = document.createElement("manifest");
            rootElement.setAttribute("xmlns:android", "http://schemas.android.com/apk/res/android");
            rootElement.setAttribute("package", packageName);
            rootElement.setAttribute("android:versionName", versionName);

            Element overlayElement = document.createElement("overlay");
            overlayElement.setAttribute("android:priority", "1");
            overlayElement.setAttribute("android:targetPackage", targetPackage);
            overlayElement.setAttribute("android:isStatic", "false");
            rootElement.appendChild(overlayElement);

            Element applicationElement = document.createElement("application");
            applicationElement.setAttribute("android:label", "IconifyComponent" + themeName);
            applicationElement.setAttribute("allowBackup", "false");
            applicationElement.setAttribute("android:hasCode", "false");

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
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Helper function to easily check whether a String object is null or empty
     *
     * @param string String object
     * @return True, then it is Null or Empty
     */
    private static boolean isNotNullOrEmpty(CharSequence string) {
        return (string != null) && (string.length() != 0);
    }

    /**
     * Obtain the device ID of the device
     *
     * @param context Context...
     * @return Returns a string of the device's ID
     */
    @SuppressLint("HardwareIds")
    public static String getDeviceID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    /**
     * Create the AAPT working shell commands
     *
     * @param workArea          Working area
     * @param targetPackage     Target package to build against
     * @param overlayPackage    Overlay package
     * @param themeName         Theme name
     * @param legacySwitch      Fallback support
     * @param additionalVariant Additional variant (type2)
     * @param assetReplacement  Asset replacement (type4)
     * @param context           Context
     * @param dir               Volatile directory to keep changes in
     * @return Returns a string to allow the app to execute
     */
    // This is necessary to avoid making a massive unreadable soup
    // inside the method.
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public static String createAAPTShellCommands(String workArea,
                                                 String targetPackage,
                                                 String overlayPackage,
                                                 String themeName,
                                                 boolean legacySwitch,
                                                 CharSequence additionalVariant,
                                                 CharSequence assetReplacement,
                                                 Context context,
                                                 String dir) {
        StringBuilder sb = new StringBuilder();
        // Initialize the AAPT command
        sb.append(context.getFilesDir().getAbsolutePath() + "/aapt p ");
        // Compile with specified manifest
        sb.append("-M ").append(workArea).append("/AndroidManifest.xml ");
        // If the user picked a variant (type2), compile multiple directories
        if (isNotNullOrEmpty(additionalVariant))
            sb.append("-S ").append(workArea).append("/").append("type2_").append(additionalVariant).append("/ ");
        // If the user picked an asset variant (type4), compile multiple directories
        if (isNotNullOrEmpty(assetReplacement))
            sb.append("-A ").append(workArea).append("/assets/ ");
        // We will compile a volatile directory where we make temporary changes to
        sb.append("-S ").append(workArea).append(dir).append("/ ");
        // Build upon the system's Android framework
        sb.append("-I ").append("/system/framework/framework-res.apk ");
        // Build upon the common Overlay framework
        if (isPackageInstalled(context, COMMON_PACKAGE)) {
            sb.append("-I " + getInstalledDirectory(context, COMMON_PACKAGE) + ' ');
        }
        String[] splitLocations = getSplitLocations(context, targetPackage);
        if (splitLocations != null) {
            for (String split : splitLocations) {
                sb.append("-I ").append(split).append(" ");
            }
        }
        final String packagePath = getInstalledDirectory(context, targetPackage);
        // If running on the AppCompat commits (first run), it will build upon the app too
        if (packagePath != null && !packagePath.equals("null")) {
            if (!legacySwitch)
                sb.append("-I ").append(packagePath).append(" ");
        }
        // Specify the file output directory
        sb.append("-F ").append(workArea).append("/").append(overlayPackage)
                .append(".").append(themeName).append("-unsigned.apk ");
        // arguments to conclude the AAPT build
        if (ENABLE_AAPT_OUTPUT) {
            sb.append("-v ");
        }
        // Allow themers to append new resources
        sb.append("--include-meta-data ");
        sb.append("--auto-add-overlay ");
        // Overwrite all the files in the internal storage
        sb.append("-f ");
        sb.append('\n');

        return sb.toString();
    }

    /**
     * Determine the installed directory of the overlay for legacy mode
     *
     * @param context     Context
     * @param packageName Package name of the desired app to be checked
     * @return Returns the installation directory of the overlay
     */
    public static String getInstalledDirectory(Context context,
                                               String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return ai.sourceDir;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Returns whether the package is installed or not, with an extra flag to check if enabled or
     * disabled
     *
     * @param context     Context
     * @param packageName Package name of the desired app to be checked
     * @return True, if it fits all criteria above
     */
    public static boolean isPackageInstalled(Context context,
                                             String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            if (!new File(ai.sourceDir).exists()) return false;
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return ai.enabled;
        } catch (Exception e) {
            return false;
        }
    }

    private static String[] getSplitLocations(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, 0).splitSourceDirs;
        } catch (PackageManager.NameNotFoundException ignored) {
            return new String[0];
        }
    }

    /**
     * Create the ZipAlign shell commands
     *
     * @param context     Context
     * @param source      Source
     * @param destination Destination
     * @return Returns a string that is executable by the application
     */
    public static String createZipAlignShellCommands(Context context,
                                                     String source,
                                                     String destination) {
        // Initialize the ZipAlign command
        String ret = context.getFilesDir().getAbsolutePath() + "/zipalign 4 ";
        // Supply the source
        ret += source + ' ';
        // Supply the destination
        ret += destination;

        return ret;
    }

    /**
     * Overlay Builder Build Function
     * <p>
     * Prior to running this function, you must have copied all the files to the working directory!
     *
     * @param overlayPackage    the target package to be overlaid (e.g. com.android.settings).
     * @param themeName         the theme's name to be stripped of symbols for the new package.
     * @param variant           a String flag to tell the compiler to build variant mode. This
     *                          could be the name of the variant spinner, or a package name for
     *                          OverlayUpdater (used in conjunction with overridePackage).
     * @param additionalVariant the additional variant (type2) that gets appended during aapt
     *                          compilation phase to the main /res folder.
     * @param baseVariant       this is linked to variable baseSpinner in Overlays.java, for
     *                          type3 base /res replacements.
     * @param versionName       the version to use for compiling the overlay's version.
     * @param themeParent       the parent theme of the created overlay.
     * @param noCacheDir        where the compilation files will be placed.
     * @param type1a            String location of the type1a file
     * @param type1b            String location of the type1b file
     * @param type1c            String location of the type1c file
     * @param type2             String location of the type2 file
     * @param type3             String location of the type3 file
     * @param type4             String location of the type4 file
     * @param overridePackage   String package to tell whether we should change the package name
     * @param overlayUpdater    boolean flag to tell whether specialSnowflake should be skipped
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean beginAction(String overlayPackage,
                               String themeName,
                               String variant,
                               String additionalVariant,
                               String baseVariant,
                               String versionName,
                               String themeParent,
                               String noCacheDir,
                               String type1a,
                               String type1b,
                               String type1c,
                               String type2,
                               String type3,
                               String type4,
                               String overridePackage,
                               boolean overlayUpdater) {

        // 1. Initialize the setup
        File checkCompileFolder = new File(EXTERNAL_STORAGE_CACHE);
        if (!checkCompileFolder.exists() && !checkCompileFolder.mkdirs()) {
            Log.e(SUBSTRATUM_BUILDER, "Could not create compilation folder on external storage...");
        }
        hasErroredOut = false;
        debug = false;

        // 2. Set work area to asset chosen based on the parameter passed into this class
        String workArea = context.getCacheDir().getAbsolutePath() + SUBSTRATUM_BUILDER_CACHE;

        // 3. Parse the theme's name before adding it into the new manifest to prevent any issues

        String parse2VariantName = "";
        if (variant != null) {
            String parse1VariantName = variant.replaceAll("\\s+", "");
            parse2VariantName = parse1VariantName.replaceAll("[^a-zA-Z0-9]+", "");
        }
        if (!parse2VariantName.isEmpty()) parse2VariantName = '.' + parse2VariantName;

        String parse2BaseName = "";
        if (baseVariant != null) {
            String parse1BaseName = baseVariant.replaceAll("\\s+", "");
            parse2BaseName = parse1BaseName.replaceAll("[^a-zA-Z0-9]+", "");
        }
        if (!parse2BaseName.isEmpty()) parse2BaseName = '.' + parse2BaseName;

        String parse1ThemeName = themeName.replaceAll("\\s+", "");
        String parse2ThemeName = parse1ThemeName.replaceAll("[^a-zA-Z0-9]+", "");
        if (parse2ThemeName.isEmpty()) {
            parse2ThemeName = "no_name";
        }

        // 4. Create the manifest file based on the new parsed names
        String targetPackage = overlayPackage;
        if (allowedSettingsOverlay(overlayPackage)) {
            targetPackage = SETTINGS;
        }
        if (allowedSystemUIOverlay(overlayPackage)) {
            targetPackage = SYSTEMUI;
        }

        SharedPreferences prefs = Iconify.getPreferences();
        File workAreaArray = new File(workArea);

        if (Arrays.asList(Objects.requireNonNull(workAreaArray.list())).contains("priority")) {
            Log.e(SUBSTRATUM_BUILDER, "A specified priority file has been found for this overlay!");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(workAreaArray.getAbsolutePath() + "/priority"))))) {
            } catch (IOException ignored) {
                dumpErrorLogs(overlayPackage, "There was an error parsing priority file!");
            }
        }

        String overlayVersionCode = String.valueOf(getLiveOverlayVersion(context, themeParent, targetPackage));
        if (!overlayVersionCode.equals("0"))
            Log.e(SUBSTRATUM_BUILDER, "The version for this overlay is " + overlayVersionCode);

        if (!hasErroredOut) {
            File root = new File(workArea + "/AndroidManifest.xml");
            try (FileWriter fw = new FileWriter(root);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter pw = new PrintWriter(bw)) {
                boolean created = root.createNewFile();
                String manifest = "";
                if (!created)
                    manifest = createOverlayManifest(
                            context,
                            parse2ThemeName,
                            versionName,
                            overlayVersionCode,
                            targetPackage,
                            themeParent,
                            type1a,
                            type1b,
                            type1c,
                            type2,
                            type3,
                            type4,
                            ((overridePackage != null) &&
                                    !overridePackage.isEmpty()) ?
                                    overridePackage : "");
                pw.write(manifest);
            } catch (Exception e) {
                dumpErrorLogs(overlayPackage, Objects.requireNonNull(e.getMessage()));
                dumpErrorLogs(overlayPackage, "There was an exception creating a new Manifest file!");
                hasErroredOut = true;
                dumpErrorLogs(overlayPackage, "Installation of \"" + overlayPackage + "\" has failed.");
            }
        }

        // 5. Compile the new theme apk based on new manifest, framework-res.apk and extracted asset
        if (!hasErroredOut) {
            String commands = createAAPTShellCommands(
                    workArea,
                    targetPackage,
                    overlayPackage,
                    parse2ThemeName,
                    false,
                    additionalVariant,
                    type4,
                    context,
                    noCacheDir);

            if (ENABLE_DIRECT_ASSETS_LOGGING)
                Log.e("Direct_Access_Log", "Running commands: " + commands);

            hasErroredOut = !runAAPTShellCommands(
                    commands,
                    workArea,
                    targetPackage,
                    parse2ThemeName,
                    overlayPackage,
                    additionalVariant,
                    type4,
                    false,
                    context,
                    noCacheDir);
        }

        // 6. Zipalign the apk
        if (!hasErroredOut) {
            String source = workArea + '/' + overlayPackage + '.' + parse2ThemeName +
                    "-unsigned.apk";
            String destination = workArea + '/' + overlayPackage + '.' + parse2ThemeName +
                    "-unsigned-aligned.apk";
            String commands = createZipAlignShellCommands(context, source,
                    destination);

            Process nativeApp = null;
            try {
                nativeApp = Runtime.getRuntime().exec(commands);

                // We need this Process to be waited for before moving on to the next function.
                Log.e(SUBSTRATUM_BUILDER, "Aligning APK now...");
                nativeApp.waitFor();
                File alignedAPK = new File(destination);
                if (alignedAPK.isFile()) {
                    Log.e(SUBSTRATUM_BUILDER, "Zipalign successful!");
                } else {
                    dumpErrorLogs(overlayPackage,
                            "Zipalign has failed!");
                    hasErroredOut = true;
                    dumpErrorLogs(overlayPackage,
                            "Zipalign of \"" + overlayPackage + "\" has failed.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                dumpErrorLogs(overlayPackage,
                        "Unfortunately, there was an exception trying to zipalign a new APK");
                hasErroredOut = true;
                dumpErrorLogs(overlayPackage,
                        "Installation of \"" + overlayPackage + "\" has failed.");
            } finally {
                if (nativeApp != null) {
                    nativeApp.destroy();
                }
            }
        }

        // 7. Sign the apk
        String overlayName = "IconifyComponent" + parse2ThemeName;
        String signedOverlayAPKPath = EXTERNAL_STORAGE_CACHE + overlayName + "-signed.apk";
        if (!hasErroredOut) {
            try {
                // Delete the previous APK if it exists in the dashboard folder
                FileUtil.delete(context, signedOverlayAPKPath);

                // Sign with the built-in test key/certificate.
                String source = workArea + '/' + overlayPackage + '.' + parse2ThemeName +
                        "-unsigned-aligned.apk";

                File key = new File(context.getDataDir() + "/key");
                char[] keyPass = "overlay".toCharArray();

                if (!key.exists()) {
                    Log.e(SUBSTRATUM_BUILDER, "Loading keystore...");
                    FileUtil.copyFromAsset(context, "key", key.getAbsolutePath());
                }

                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(new FileInputStream(key), keyPass);
                PrivateKey privateKey = (PrivateKey) keyStore.getKey("key", keyPass);
                List<X509Certificate> certs = new ArrayList<>();
                certs.add((X509Certificate) keyStore.getCertificateChain("key")[0]);

                ApkSigner.SignerConfig signerConfig =
                        new ApkSigner.SignerConfig.Builder("overlay", privateKey, certs).build();
                List<ApkSigner.SignerConfig> signerConfigs = new ArrayList<>();
                signerConfigs.add(signerConfig);
                new ApkSigner.Builder(signerConfigs)
                        .setV1SigningEnabled(false)
                        .setV2SigningEnabled(true)
                        .setInputApk(new File(source))
                        .setOutputApk(new File(signedOverlayAPKPath))
                        .setMinSdkVersion(Build.VERSION.SDK_INT)
                        .build()
                        .sign();

                Log.e(SUBSTRATUM_BUILDER, "APK successfully signed!");
            } catch (Throwable t) {
                t.printStackTrace();
                dumpErrorLogs(overlayPackage,
                        "APK could not be signed. " + t.toString());
                hasErroredOut = true;
                dumpErrorLogs(overlayPackage,
                        "Installation of \"" + overlayPackage + "\" has failed.");
            }
        }

        // 8. Install the APK silently
        // Superuser needed as this requires elevated privileges to run these commands
        if (!hasErroredOut) {
            // Brute force install APKs because thanks Google
            FileUtil.mountSystemRW();
            final String overlay = "data/adb/modules/Iconify/system/product/overlay/" + overlayName + ".apk";
            FileUtil.move(context, signedOverlayAPKPath, overlay);
            FileUtil.setPermissions(644, overlay);
            FileUtil.mountSystemRO();
        }

        // Finally, clean this compilation code's cache
        if (!BYPASS_SUBSTRATUM_BUILDER_DELETION) {
            String workingDirectory = context.getCacheDir().getAbsolutePath() + SUBSTRATUM_BUILDER_CACHE;
            File deleted = new File(workingDirectory);
            FileUtil.delete(context, deleted.getAbsolutePath());
            if (!deleted.exists()) Log.e(SUBSTRATUM_BUILDER,
                    "Successfully cleared compilation cache!");
        }
        return !hasErroredOut;
    }

    /**
     * Returns a string of error logs during compilation
     *
     * @return Returns a string of error logs during compilation
     */
    public String getErrorLogs() {
        return errorLogs;
    }

    private boolean runAAPTShellCommands(String commands,
                                         String workArea,
                                         String targetPkg,
                                         String themeName,
                                         String overlayPackage,
                                         String additionalVariant,
                                         String assetReplacement,
                                         boolean legacySwitch,
                                         Context context,
                                         String noCacheDir) {
        Process nativeApp = null;
        try {
            nativeApp = Runtime.getRuntime().exec(commands);

            try (OutputStream stdin = nativeApp.getOutputStream();
                 InputStream stderr = nativeApp.getErrorStream()) {
                stdin.write(("ls\n").getBytes());
                stdin.write("exit\n".getBytes());

                boolean errored = false;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(stderr))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.contains("types not allowed") && !legacySwitch && !debug) {
                            Log.e(SUBSTRATUM_BUILDER,
                                    "This overlay was designed using a legacy theming " +
                                            "style, now falling back to legacy compiler...");
                            String newCommands = createAAPTShellCommands(workArea, targetPkg,
                                    overlayPackage, themeName, true, additionalVariant,
                                    assetReplacement, context, noCacheDir);
                            return runAAPTShellCommands(
                                    newCommands, workArea, targetPkg, themeName,
                                    overlayPackage, additionalVariant, assetReplacement,
                                    true, context, noCacheDir);
                        } else {
                            dumpErrorLogs(overlayPackage,
                                    line);
                            errored = true;
                        }
                    }
                }
                if (errored) {
                    hasErroredOut = true;
                    dumpErrorLogs(overlayPackage,
                            "Installation of \"" + overlayPackage + "\" has failed.");
                } else {
                    // We need this Process to be waited for before moving on to the next function.
                    Log.e(SUBSTRATUM_BUILDER, "Overlay APK creation is running now...");
                    nativeApp.waitFor();
                    File unsignedAPK = new File(workArea + '/' + overlayPackage + '.' +
                            themeName + "-unsigned.apk");
                    if (unsignedAPK.isFile()) {
                        Log.e(SUBSTRATUM_BUILDER, "Overlay APK creation has completed!");
                        return true;
                    } else {
                        dumpErrorLogs(overlayPackage,
                                "Overlay APK creation has failed!");
                        hasErroredOut = true;
                        dumpErrorLogs(overlayPackage,
                                "Installation of \"" + overlayPackage + "\" has failed.");
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            dumpErrorLogs(overlayPackage,
                    "Unfortunately, there was an exception trying to create a new APK");
            hasErroredOut = true;
            dumpErrorLogs(overlayPackage,
                    "Installation of \"" + overlayPackage + "\" has failed.");
        } finally {
            if (nativeApp != null) {
                nativeApp.destroy();
            }
        }
        return false;
    }

    /**
     * Save a series of error logs to be callable
     *
     * @param overlay Overlay that has failed to compile
     * @param message Failure message
     */
    private void dumpErrorLogs(String overlay, String message) {
        if (!message.isEmpty()) {
            Log.e(SUBSTRATUM_BUILDER, message);
            if (errorLogs.isEmpty()) {
                errorLogs = "» [" + overlay + "]: " + message;
            } else {
                errorLogs += '\n' + "» [" + overlay + "]: " + message;
            }
        }
    }
}