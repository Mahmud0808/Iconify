package com.drdisagree.iconify.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import com.drdisagree.iconify.data.common.Resources.DYNAMIC_RESOURCE_TABLE
import com.drdisagree.iconify.data.entity.DynamicResourceEntity

@Dao
interface DynamicResourceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(
        resources: List<DynamicResourceEntity>
    )

    @Query(
        """
        SELECT * FROM $DYNAMIC_RESOURCE_TABLE
        WHERE packageName = :packageName
        AND resourceName = :resourceName
        AND startEndTag = :startEndTag
        AND isPortrait = :isPortrait
        AND isLandscape = :isLandscape
        AND isNightMode = :isNightMode
        ORDER BY createdAt DESC
        LIMIT 1
        """
    )
    suspend fun getLatestResource(
        packageName: String,
        resourceName: String,
        startEndTag: String,
        isPortrait: Boolean,
        isLandscape: Boolean,
        isNightMode: Boolean
    ): DynamicResourceEntity?

    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
    SELECT *
    FROM (
        SELECT *,
               ROW_NUMBER() OVER (
                   PARTITION BY 
                       packageName,
                       resourceName,
                       startEndTag,
                       isPortrait,
                       isLandscape,
                       isNightMode
                   ORDER BY createdAt DESC
               ) AS rn
        FROM $DYNAMIC_RESOURCE_TABLE
    )
    WHERE rn = 1
    """
    )
    suspend fun getAllLatestResources(): List<DynamicResourceEntity>

    @Query("SELECT * FROM $DYNAMIC_RESOURCE_TABLE")
    suspend fun getAllResources(): List<DynamicResourceEntity>

    @Query(
        """
        SELECT * FROM $DYNAMIC_RESOURCE_TABLE
        WHERE packageName = :packageName
        ORDER BY createdAt ASC
        """
    )
    suspend fun getResourcesForPackage(
        packageName: String
    ): List<DynamicResourceEntity>

    @Query(
        """
        SELECT * FROM $DYNAMIC_RESOURCE_TABLE t
        WHERE isPortrait = 1
        AND createdAt = (
            SELECT MAX(createdAt)
            FROM $DYNAMIC_RESOURCE_TABLE
            WHERE packageName = t.packageName
            AND resourceName = t.resourceName
            AND startEndTag = t.startEndTag
            AND isPortrait = 1
        )
        """
    )
    suspend fun getPortraitResources(): List<DynamicResourceEntity>

    @Query(
        """
        SELECT * FROM $DYNAMIC_RESOURCE_TABLE t
        WHERE isLandscape = 1
        AND createdAt = (
            SELECT MAX(createdAt)
            FROM $DYNAMIC_RESOURCE_TABLE
            WHERE packageName = t.packageName
            AND resourceName = t.resourceName
            AND startEndTag = t.startEndTag
            AND isLandscape = 1
        )
        """
    )
    suspend fun getLandscapeResources(): List<DynamicResourceEntity>

    @Query(
        """
        SELECT * FROM $DYNAMIC_RESOURCE_TABLE t
        WHERE isNightMode = 1
        AND createdAt = (
            SELECT MAX(createdAt)
            FROM $DYNAMIC_RESOURCE_TABLE
            WHERE packageName = t.packageName
            AND resourceName = t.resourceName
            AND startEndTag = t.startEndTag
            AND isNightMode = 1
        )
        """
    )
    suspend fun getNightModeResources(): List<DynamicResourceEntity>

    @Query(
        """
        DELETE FROM $DYNAMIC_RESOURCE_TABLE
        WHERE overlayId = :overlayId
        """
    )
    suspend fun deleteOverlay(
        overlayId: String
    )

    @Query(
        """
        DELETE FROM $DYNAMIC_RESOURCE_TABLE
        WHERE overlayId IN (:overlayIds)
        """
    )
    suspend fun deleteOverlays(
        overlayIds: List<String>
    )

    @Query("DELETE FROM $DYNAMIC_RESOURCE_TABLE")
    suspend fun clearAllResources()
}