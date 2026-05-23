package com.drdisagree.iconify.core.di

import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.data.storage.DataStoreStorage
import com.drdisagree.iconify.data.storage.SharedPreferencesStorage
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PreferenceEntryPoint {
    fun dataStoreStorage(): DataStoreStorage
    fun sharedPrefsStorage(): SharedPreferencesStorage
    fun preferenceController(): PreferenceController
}