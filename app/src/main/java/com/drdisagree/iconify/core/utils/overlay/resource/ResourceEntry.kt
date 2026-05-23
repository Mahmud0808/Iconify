package com.drdisagree.iconify.core.utils.overlay.resource

@Suppress("unused")
class ResourceEntry {

    var packageName: String
    var startEndTag: String
    var resourceName: String
    var resourceValue: String

    private var _isPortrait: Boolean
    private var _isLandscape: Boolean
    private var _isNightMode: Boolean

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

        _isPortrait = true
        _isLandscape = false
        _isNightMode = false
    }

    constructor(packageName: String, startEndTag: String, resourceName: String) {
        this.packageName = packageName
        this.startEndTag = startEndTag
        this.resourceName = resourceName

        resourceValue = ""
        _isPortrait = true
        _isLandscape = false
        _isNightMode = false
    }

    var isPortrait: Boolean
        get() = _isPortrait
        set(value) {
            _isPortrait = value
        }

    var isLandscape: Boolean
        get() = _isLandscape
        set(value) {
            _isLandscape = value
        }

    var isNightMode: Boolean
        get() = _isNightMode
        set(value) {
            _isNightMode = value
        }
}