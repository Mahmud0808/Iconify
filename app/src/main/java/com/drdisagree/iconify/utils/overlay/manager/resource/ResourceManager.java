package com.drdisagree.iconify.utils.overlay.manager.resource;

import static com.drdisagree.iconify.common.Preferences.DYNAMIC_OVERLAY_RESOURCES;
import static com.drdisagree.iconify.common.Preferences.DYNAMIC_OVERLAY_RESOURCES_LAND;
import static com.drdisagree.iconify.common.Preferences.DYNAMIC_OVERLAY_RESOURCES_NIGHT;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.common.Const;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.extension.TaskExecutor;
import com.drdisagree.iconify.utils.overlay.compiler.DynamicCompiler;

import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResourceManager {

    private static final String TAG = ResourceManager.class.getSimpleName();

    public static boolean buildOverlayWithResource(ResourceEntry... resourceEntries) {
        AtomicBoolean hasErroredOut = new AtomicBoolean(false);

        try {
            ResourceManager.createResource(resourceEntries);
        } catch (Exception e) {
            hasErroredOut.set(true);
            Log.e(TAG, "buildOverlayWithResource:", e);
        }

        return hasErroredOut.get();
    }

    public static void buildOverlayWithResource(Context context, ResourceEntry... resourceEntries) {
        if (!SystemUtil.hasStoragePermission()) {
            SystemUtil.requestStoragePermission(context);
        } else {
            try {
                ResourceManager.createResource(resourceEntries);
            } catch (Exception e) {
                Log.e(TAG, "buildOverlayWithResource:", e);
            }
        }
    }

    public static boolean removeResourceFromOverlay(ResourceEntry... resourceEntries) {
        AtomicBoolean hasErroredOut = new AtomicBoolean(false);

        try {
            ResourceManager.removeResource(resourceEntries);
        } catch (Exception e) {
            hasErroredOut.set(true);
            Log.e(TAG, "removeResourceFromOverlay:", e);
        }

        return hasErroredOut.get();
    }

    public static void removeResourceFromOverlay(Context context, ResourceEntry... resourceEntries) {
        if (!SystemUtil.hasStoragePermission()) {
            SystemUtil.requestStoragePermission(context);
        } else {
            try {
                ResourceManager.removeResource(resourceEntries);
            } catch (Exception e) {
                Log.e(TAG, "removeResourceFromOverlay:", e);
            }
        }
    }

    private static void createResource(ResourceEntry... resourceEntries) throws Exception {
        JSONObject[] jsonObject = getResources();
        JSONObject[] newJsonObject = generateJsonData(resourceEntries);
        JSONObject[] mergedJson = new JSONObject[3];

        for (int i = 0; i < 3; i++) {
            mergedJson[i] = initResourceIfNull(new JSONObject());
            mergeJsonObjects(mergedJson[i], jsonObject[i]);
            mergeJsonObjects(mergedJson[i], newJsonObject[i]);
        }

        saveResources(mergedJson);

        DynamicCompilerExecutor dynamicCompilerExecutor = new DynamicCompilerExecutor();
        dynamicCompilerExecutor.execute();
    }

    private static void removeResource(ResourceEntry... resourceEntries) throws Exception {
        JSONObject[] jsonObject = getResources();

        for (ResourceEntry resourceEntry : resourceEntries) {
            int index = -1;
            if (resourceEntry.isPortrait()) {
                index = 0;
            } else if (resourceEntry.isLandscape()) {
                index = 1;
            } else if (resourceEntry.isNightMode()) {
                index = 2;
            }

            JSONObject resourceTypes = jsonObject[index].optJSONObject(resourceEntry.getPackageName());

            if (resourceTypes == null) {
                continue;
            }

            JSONObject resources = resourceTypes.optJSONObject(resourceEntry.getStartEndTag());

            if (resources == null) {
                continue;
            }

            resources.remove(resourceEntry.getResourceName());
        }

        saveResources(jsonObject);

        DynamicCompilerExecutor dynamicCompilerExecutor = new DynamicCompilerExecutor();
        dynamicCompilerExecutor.execute();
    }

    private static class DynamicCompilerExecutor extends TaskExecutor<Void, Void, Void> {
        AtomicBoolean hasErroredOut = new AtomicBoolean(false);

        @Override
        protected void onPreExecute() {
            // do nothing
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                DynamicCompiler.buildOverlay();
            } catch (IOException e) {
                Log.i(TAG, "doInBackground: ", e);
                hasErroredOut.set(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (!hasErroredOut.get()) {
                Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static JSONObject[] generateJsonData(ResourceEntry... resourceEntries) throws Exception {
        JSONObject[] packages = new JSONObject[3];
        packages[0] = initResourceIfNull(packages[0]);
        packages[1] = initResourceIfNull(packages[1]);
        packages[2] = initResourceIfNull(packages[2]);

        for (ResourceEntry entry : resourceEntries) {
            if (entry.getResourceValue().isEmpty()) {
                throw new Exception("Resource value is empty.");
            }

            int index = -1;
            if (entry.isPortrait()) {
                index = 0;
            } else if (entry.isLandscape()) {
                index = 1;
            } else if (entry.isNightMode()) {
                index = 2;
            }

            JSONObject resourceTypes = packages[index].optJSONObject(entry.getPackageName());

            if (resourceTypes == null) {
                resourceTypes = new JSONObject();
                packages[index].put(entry.getPackageName(), resourceTypes);
            }

            JSONObject resources = resourceTypes.optJSONObject(entry.getStartEndTag());

            if (resources == null) {
                resources = new JSONObject();
                resourceTypes.put(entry.getStartEndTag(), resources);
            }

            resources.put(entry.getResourceName(), entry.getResourceValue());
        }

        return packages;
    }

    public static JSONObject generateJsonResource(JSONObject jsonObject) throws Exception {
        JSONObject newJsonObject = initResourceIfNull(new JSONObject());
        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);

            if (Const.SYSTEM_PACKAGES.contains(key) && value instanceof JSONObject) {
                XmlSerializer xmlSerializer = Xml.newSerializer();
                StringWriter writer = new StringWriter();
                String startEndTag = "resources";

                xmlSerializer.setOutput(writer);
                xmlSerializer.startDocument("UTF-8", null);
                xmlSerializer.startTag(null, startEndTag);

                addJsonToXml("", (JSONObject) value, xmlSerializer);

                xmlSerializer.endTag(null, startEndTag);
                xmlSerializer.endDocument();

                newJsonObject.put(key, writer.toString());
            } else {
                throw new Exception("Invalid JSON format.\n" + jsonObject);
            }
        }

        return newJsonObject;
    }

    private static void addJsonToXml(String parent, JSONObject json, XmlSerializer xmlSerializer) throws Exception {
        Iterator<String> keys = json.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object value = json.get(key);

            if (value instanceof JSONObject) {
                addJsonToXml(key, (JSONObject) value, xmlSerializer);
            } else {
                xmlSerializer.startTag(null, parent);
                xmlSerializer.attribute(null, "name", key);
                xmlSerializer.text(value.toString());
                xmlSerializer.endTag(null, parent);
            }
        }
    }

    private static void mergeJsonObjects(JSONObject mergedJson, JSONObject jsonObject) throws Exception {
        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);

            if (value instanceof JSONObject) {
                JSONObject existingValue = mergedJson.optJSONObject(key);

                if (existingValue != null) {
                    mergeJsonObjects(existingValue, (JSONObject) value);
                } else {
                    mergedJson.put(key, value);
                }
            } else {
                mergedJson.put(key, value);
            }
        }
    }

    public static JSONObject[] getResources() throws Exception {
        String resources = Prefs.getString(DYNAMIC_OVERLAY_RESOURCES, "{}");
        String resourcesLand = Prefs.getString(DYNAMIC_OVERLAY_RESOURCES_LAND, "{}");
        String resourcesNight = Prefs.getString(DYNAMIC_OVERLAY_RESOURCES_NIGHT, "{}");

        JSONObject values = initResourceIfNull(resources);
        JSONObject valuesLand = initResourceIfNull(resourcesLand);
        JSONObject valuesNight = initResourceIfNull(resourcesNight);

        return new JSONObject[]{values, valuesLand, valuesNight};
    }

    private static void saveResources(JSONObject[] resources) {
        Prefs.putString(DYNAMIC_OVERLAY_RESOURCES, resources[0].toString());
        Prefs.putString(DYNAMIC_OVERLAY_RESOURCES_LAND, resources[1].toString());
        Prefs.putString(DYNAMIC_OVERLAY_RESOURCES_NIGHT, resources[2].toString());
    }

    private static JSONObject initResourceIfNull(String json) throws Exception {
        return initResourceIfNull(new JSONObject(json));
    }

    private static JSONObject initResourceIfNull(JSONObject jsonObject) throws Exception {
        if (jsonObject == null) {
            jsonObject = new JSONObject();
        }

        JSONObject resourceTypes1 = jsonObject.optJSONObject(Const.FRAMEWORK_PACKAGE);
        JSONObject resourceTypes2 = jsonObject.optJSONObject(Const.SYSTEMUI_PACKAGE);

        if (resourceTypes1 == null) {
            resourceTypes1 = new JSONObject();
            jsonObject.put(Const.FRAMEWORK_PACKAGE, resourceTypes1);
        }

        if (resourceTypes2 == null) {
            resourceTypes2 = new JSONObject();
            jsonObject.put(Const.SYSTEMUI_PACKAGE, resourceTypes2);
        }

        JSONObject resources1 = resourceTypes1.optJSONObject("color");
        JSONObject resources2 = resourceTypes2.optJSONObject("color");

        if (resources1 == null) {
            resources1 = new JSONObject();
            resourceTypes1.put("color", resources1);
        }

        if (resources2 == null) {
            resources2 = new JSONObject();
            resourceTypes2.put("color", resources2);
        }

        resources1.put("dummy1", "#00000000");
        resources2.put("dummy2", "#00000000");

        return jsonObject;
    }
}
