package com.drdisagree.iconify.data.entity

import androidx.room.Entity
import androidx.room.Index
import com.drdisagree.iconify.data.common.Resources.DYNAMIC_RESOURCE_TABLE

@Entity(
    tableName = DYNAMIC_RESOURCE_TABLE,
    primaryKeys = [
        "overlayId",
        "packageName",
        "startEndTag",
        "resourceName",
        "isPortrait",
        "isLandscape",
        "isNightMode"
    ],
    indices = [
        Index("overlayId"),
        Index("packageName"),
        Index("resourceName"),
        Index("createdAt")
    ]
)
data class DynamicResourceEntity(
    val overlayId: String,
    val packageName: String,
    val startEndTag: String,
    val resourceName: String,
    val resourceValue: String,
    val isPortrait: Boolean,
    val isLandscape: Boolean,
    val isNightMode: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)