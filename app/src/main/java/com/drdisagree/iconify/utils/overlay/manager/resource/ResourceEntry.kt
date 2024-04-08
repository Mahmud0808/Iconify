package com.drdisagree.iconify.utils.overlay.manager.resource

@Suppress("unused")
class ResourceEntry {

    var packageName: String
    var startEndTag: String
    var resourceName: String
    var resourceValue: String
    private var isPortrait: Boolean
    var isNightMode: Boolean
    private var isLandscape: Boolean

    constructor(
        packageName: String,
        startEndTag: String,
        resourceName: String,
        resourceValue: String
    ) {
        this.packageName = packageName
        this.startEndTag = startEndTag
        this.resourceName = resourceName
        this.resourceValue = resourceValue

        isPortrait = true
        isNightMode = false
        isLandscape = false
    }

    constructor(packageName: String, startEndTag: String, resourceName: String) {
        this.packageName = packageName
        this.startEndTag = startEndTag
        this.resourceName = resourceName

        resourceValue = ""
        isPortrait = true
        isNightMode = false
        isLandscape = false
    }

    fun isPortrait(): Boolean {
        return isPortrait
    }

    fun setPortrait(portrait: Boolean) {
        isPortrait = portrait
        isLandscape = !portrait
    }

    fun isLandscape(): Boolean {
        return isLandscape
    }

    fun setLandscape(landscape: Boolean) {
        isLandscape = landscape
        isPortrait = !landscape
    }
}