package com.drdisagree.iconify.data.keys

interface Key {
    val name: String
    val default: Any?
}

object KeyRegistry {
    val allKeys: List<Key> = buildList {
        addAll(SettingsKey.entries)
        addAll(XposedKey.entries)
        addAll(TweaksKey.entries)
        addAll(CustomizationKey.entries)
    }
}