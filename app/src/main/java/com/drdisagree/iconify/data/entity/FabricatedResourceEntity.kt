package com.drdisagree.iconify.data.entity

import androidx.room.Entity
import androidx.room.Index
import com.drdisagree.iconify.data.common.Resources.FABRICATED_RESOURCE_TABLE

@Entity(
    tableName = FABRICATED_RESOURCE_TABLE,
    primaryKeys = [
        "targetPackageName",
        "overlayName",
        "resourceType",
        "resourceName",
    ],
    indices = [
        Index("targetPackageName"),
        Index("overlayName"),
        Index("resourceType"),
        Index("resourceName"),
        Index("createdAt")
    ]
)
data class FabricatedResourceEntity(
    val targetPackageName: String,
    val overlayName: String,
    val resourceType: String,
    val resourceName: String,
    val resourceValue: String,
    val createdAt: Long = System.currentTimeMillis()
)