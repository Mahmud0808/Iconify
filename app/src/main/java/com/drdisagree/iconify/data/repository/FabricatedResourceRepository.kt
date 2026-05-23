package com.drdisagree.iconify.data.repository

import com.drdisagree.iconify.data.dao.FabricatedResourceDao
import com.drdisagree.iconify.data.entity.FabricatedResourceEntity
import javax.inject.Inject

class FabricatedResourceRepository @Inject constructor(
    private val fabricatedResourceDao: FabricatedResourceDao
) {

    suspend fun insertResources(resources: List<FabricatedResourceEntity>) {
        fabricatedResourceDao.insertResources(resources)
    }

    suspend fun getAllResources(): List<FabricatedResourceEntity> =
        fabricatedResourceDao.getAllResources()

    suspend fun getResourcesForPackage(packageName: String): List<FabricatedResourceEntity> =
        fabricatedResourceDao.getResourcesForPackage(packageName)

    suspend fun getLatestResource(
        targetPackageName: String,
        resourceType: String,
        resourceName: String
    ): FabricatedResourceEntity? =
        fabricatedResourceDao.getLatestResource(
            targetPackageName,
            resourceType,
            resourceName
        )

    suspend fun getAllLatestResources(): List<FabricatedResourceEntity> =
        fabricatedResourceDao.getAllLatestResources()

    suspend fun deleteOverlay(overlayName: String) {
        fabricatedResourceDao.deleteOverlay(overlayName)
    }

    suspend fun deleteOverlays(vararg overlayNames: String) {
        fabricatedResourceDao.deleteOverlays(overlayNames.toList())
    }

    suspend fun clearAllResources() {
        fabricatedResourceDao.clearAllResources()
    }
}