package com.drdisagree.iconify.data.repository

import com.drdisagree.iconify.data.dao.DynamicResourceDao
import com.drdisagree.iconify.data.entity.DynamicResourceEntity
import javax.inject.Inject

class DynamicResourceRepository @Inject constructor(
    private val dynamicResourceDao: DynamicResourceDao
) {

    suspend fun insertResources(resources: List<DynamicResourceEntity>) {
        dynamicResourceDao.insertResources(resources)
    }

    suspend fun getAllResources(): List<DynamicResourceEntity> =
        dynamicResourceDao.getAllResources()

    suspend fun getResourcesForPackage(packageName: String): List<DynamicResourceEntity> =
        dynamicResourceDao.getResourcesForPackage(packageName)

    suspend fun getLatestResource(
        packageName: String,
        resourceName: String,
        startEndTag: String,
        isPortrait: Boolean,
        isLandscape: Boolean,
        isNightMode: Boolean
    ): DynamicResourceEntity? =
        dynamicResourceDao.getLatestResource(
            packageName,
            resourceName,
            startEndTag,
            isPortrait,
            isLandscape,
            isNightMode
        )

    suspend fun getAllLatestResources(): List<DynamicResourceEntity> =
        dynamicResourceDao.getAllLatestResources()

    suspend fun getPortraitResources(): List<DynamicResourceEntity> =
        dynamicResourceDao.getPortraitResources()

    suspend fun getLandscapeResources(): List<DynamicResourceEntity> =
        dynamicResourceDao.getLandscapeResources()

    suspend fun getNightModeResources(): List<DynamicResourceEntity> =
        dynamicResourceDao.getNightModeResources()

    suspend fun deleteOverlay(overlayId: String) {
        dynamicResourceDao.deleteOverlay(overlayId)
    }

    suspend fun deleteOverlays(vararg overlayIds: String) {
        dynamicResourceDao.deleteOverlays(overlayIds.toList())
    }

    suspend fun clearAllResources() {
        dynamicResourceDao.clearAllResources()
    }
}