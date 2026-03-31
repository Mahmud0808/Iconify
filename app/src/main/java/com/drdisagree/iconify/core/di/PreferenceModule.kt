package com.drdisagree.iconify.core.di

import com.drdisagree.iconify.data.common.XposedConst.PREF_FILE_NAME
import com.drdisagree.iconify.data.storage.DataStoreStorage
import com.drdisagree.iconify.data.storage.PreferenceStorage
import com.drdisagree.iconify.data.storage.SharedPreferencesStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferenceModule {

    @Binds
    @Singleton
    @SharedPrefs
    abstract fun bindSharedPreferencesStorage(
        impl: SharedPreferencesStorage
    ): PreferenceStorage

    @Binds
    @Singleton
    @DataStore
    abstract fun bindDataStoreStorage(
        impl: DataStoreStorage
    ): PreferenceStorage

    companion object {

        @Provides
        @Singleton
        fun provideFileName(): String = PREF_FILE_NAME
    }
}