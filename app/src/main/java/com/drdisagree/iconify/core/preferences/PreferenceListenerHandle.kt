package com.drdisagree.iconify.core.preferences

class PreferenceListenerHandle internal constructor(
    internal val key: String?,
    internal val callback: (PreferenceChangeEvent) -> Unit,
)