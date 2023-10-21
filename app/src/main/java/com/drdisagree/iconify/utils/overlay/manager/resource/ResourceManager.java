package com.drdisagree.iconify.utils.overlay.manager.resource;

import static com.drdisagree.iconify.common.Preferences.DYNAMIC_OVERLAY_RESOURCES;

import android.util.Xml;

import com.drdisagree.iconify.common.Const;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.overlay.compiler.DynamicCompiler;

import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;
import java.util.Iterator;

public class ResourceManager {

    public static void createResource(ResourceEntry... resourceEntries) throws Exception {
        JSONObject jsonObject = getResources();
        JSONObject jsonObject2 = generateJsonData(resourceEntries);
        JSONObject mergedJson = new JSONObject();

        if (jsonObject != null) {
            mergeJsonObjects(mergedJson, jsonObject);
        }
        mergeJsonObjects(mergedJson, jsonObject2);

        saveResources(mergedJson);

        DynamicCompiler.buildOverlay();
    }

    public static void removeResource(ResourceEntry... resourceEntries) throws Exception {
        JSONObject jsonObject = getResources();

        if (jsonObject == null) {
            return;
        }

        for (ResourceEntry resourceEntry : resourceEntries) {
            JSONObject resourceTypes = jsonObject.optJSONObject(resourceEntry.getPackageName());

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

        DynamicCompiler.buildOverlay();
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

    private static JSONObject generateJsonData(ResourceEntry... resourceEntries) throws Exception {
        JSONObject packages = new JSONObject();

        for (ResourceEntry entry : resourceEntries) {
            JSONObject resourceTypes = packages.optJSONObject(entry.getPackageName());

            if (resourceTypes == null) {
                resourceTypes = new JSONObject();
                packages.put(entry.getPackageName(), resourceTypes);
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
        JSONObject newJsonObject = new JSONObject();
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

    public static JSONObject getResources() throws Exception {
        String resources = Prefs.getString(DYNAMIC_OVERLAY_RESOURCES, null);
        if (resources == null) {
            return null;
        }
        return new JSONObject(resources);
    }

    private static void saveResources(JSONObject jsonObject) {
        Prefs.putString(DYNAMIC_OVERLAY_RESOURCES, jsonObject.toString());
    }
}
