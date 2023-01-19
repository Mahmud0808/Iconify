package com.drdisagree.iconify.utils.apksigner;

/* From
 * https://github.com/topjohnwu/Magisk/blob/master/app/signing/src/main/java/com/topjohnwu/signing/JarMap.java
 */

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public abstract class JarMap implements Closeable {

    LinkedHashMap<String, JarEntry> entryMap;

    public static JarMap open(InputStream is, boolean verify) throws IOException {
        return new StreamMap(is, verify);
    }

    public File getFile() {
        return null;
    }

    public abstract Manifest getManifest() throws IOException;

    public InputStream getInputStream(ZipEntry ze) {
        JarMapEntry e = getMapEntry(ze.getName());
        return e != null ? e.data.getInputStream() : null;
    }

    public abstract Enumeration<JarEntry> entries();

    public JarEntry getJarEntry(String name) {
        return getMapEntry(name);
    }

    JarMapEntry getMapEntry(String name) {
        JarMapEntry e = null;
        if (entryMap != null)
            e = (JarMapEntry) entryMap.get(name);
        return e;
    }

    private static class StreamMap extends JarMap {

        private final JarInputStream jis;

        StreamMap(InputStream is, boolean verify) throws IOException {
            jis = new JarInputStream(is, verify);
            entryMap = new LinkedHashMap<>();
            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                entryMap.put(entry.getName(), new JarMapEntry(entry, jis));
            }
        }

        @Override
        public Manifest getManifest() {
            return jis.getManifest();
        }

        @Override
        public Enumeration<JarEntry> entries() {
            return Collections.enumeration(entryMap.values());
        }

        @Override
        public void close() throws IOException {
            jis.close();
        }
    }

    private static class JarMapEntry extends JarEntry {

        ByteArrayStream data;

        JarMapEntry(JarEntry je, InputStream is) {
            super(je);
            data = new ByteArrayStream();
            data.readFrom(is);
        }

    }
}