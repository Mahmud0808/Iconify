package com.drdisagree.iconify.core.di

import com.drdisagree.iconify.data.dao.DynamicResourceDao
import com.drdisagree.iconify.data.dao.FabricatedResourceDao
import com.drdisagree.iconify.data.database.ResourceDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(): ResourceDatabase {
        return ResourceDatabase.getInstance()
    }

    @Provides
    fun provideDynamicResourceDao(database: ResourceDatabase): DynamicResourceDao {
        return database.dynamicResourceDao()
    }

    @Provides
    fun provideFabricatedResourceDao(database: ResourceDatabase): FabricatedResourceDao {
        return database.fabricatedResourceDao()
    }
}