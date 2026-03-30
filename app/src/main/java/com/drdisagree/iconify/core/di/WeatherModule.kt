package com.drdisagree.iconify.core.di

import android.content.Context
import com.drdisagree.iconify.core.utils.OmniJawsClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WeatherModule {

    @Provides
    @Singleton
    fun provideOmniJawsClient(
        @ApplicationContext context: Context
    ): OmniJawsClient {
        return OmniJawsClient(context)
    }
}