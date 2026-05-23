package com.drdisagree.iconify.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import com.drdisagree.iconify.data.common.Resources.FABRICATED_RESOURCE_TABLE
import com.drdisagree.iconify.data.entity.FabricatedResourceEntity

@Dao
interface FabricatedResourceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(
        resources: List<FabricatedResourceEntity>
    )

    @Query(
        """
        SELECT * FROM $FABRICATED_RESOURCE_TABLE
        WHERE targetPackageName = :targetPackageName
        AND resourceType = :resourceType
        AND resourceName = :resourceName
        ORDER BY createdAt DESC
        LIMIT 1
        """
    )
    suspend fun getLatestResource(
        targetPackageName: String,
        resourceType: String,
        resourceName: String
    ): FabricatedResourceEntity?

    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
    SELECT *
    FROM (
        SELECT *,
               ROW_NUMBER() OVER (
                   PARTITION BY 
                       targetPackageName,
                       resourceType,
                       resourceName
                   ORDER BY createdAt DESC
               ) AS rn
        FROM $FABRICATED_RESOURCE_TABLE
    )
    WHERE rn = 1
    """
    )
    suspend fun getAllLatestResources(): List<FabricatedResourceEntity>

    @Query("SELECT * FROM $FABRICATED_RESOURCE_TABLE")
    suspend fun getAllResources(): List<FabricatedResourceEntity>

    @Query(
        """
        SELECT * FROM $FABRICATED_RESOURCE_TABLE
        WHERE targetPackageName = :packageName
        ORDER BY createdAt ASC
        """
    )
    suspend fun getResourcesForPackage(
        packageName: String
    ): List<FabricatedResourceEntity>

    @Query(
        """
        DELETE FROM $FABRICATED_RESOURCE_TABLE
        WHERE overlayName = :overlayName
        """
    )
    suspend fun deleteOverlay(
        overlayName: String
    )

    @Query(
        """
        DELETE FROM $FABRICATED_RESOURCE_TABLE
        WHERE overlayName IN (:overlayNames)
        """
    )
    suspend fun deleteOverlays(
        overlayNames: List<String>
    )

    @Query("DELETE FROM $FABRICATED_RESOURCE_TABLE")
    suspend fun clearAllResources()
}