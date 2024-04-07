package com.drdisagree.iconify.ui.models

class QsShapeModel {

    var iconMarginStart: Int?
    var iconMarginEnd: Int?
    var name: String
    var enabledDrawable: Int
    var disabledDrawable: Int
    var inverseColor: Boolean

    constructor(name: String, enabledDrawable1: Int, disabledDrawable1: Int) {
        this.name = name
        this.enabledDrawable = enabledDrawable1
        this.disabledDrawable = disabledDrawable1

        inverseColor = false
        iconMarginStart = null
        iconMarginEnd = null
    }

    constructor(
        name: String,
        enabledDrawable: Int,
        disabledDrawable: Int,
        inverseColor1: Boolean
    ) {
        this.name = name
        this.enabledDrawable = enabledDrawable
        this.disabledDrawable = disabledDrawable

        inverseColor = inverseColor1
        iconMarginStart = null
        iconMarginEnd = null
    }

    constructor(
        name: String,
        enabledDrawable: Int,
        disabledDrawable: Int,
        iconMarginStart: Int?,
        iconMarginEnd: Int?
    ) {
        this.name = name
        this.enabledDrawable = enabledDrawable
        this.disabledDrawable = disabledDrawable
        this.iconMarginStart = iconMarginStart
        this.iconMarginEnd = iconMarginEnd

        inverseColor = false
    }

    constructor(
        name: String,
        enabledDrawable: Int,
        disabledDrawable: Int,
        inverseColor: Boolean,
        iconMarginStart: Int?,
        iconMarginEnd: Int?
    ) {
        this.name = name
        this.enabledDrawable = enabledDrawable
        this.disabledDrawable = disabledDrawable
        this.inverseColor = inverseColor
        this.iconMarginStart = iconMarginStart
        this.iconMarginEnd = iconMarginEnd
    }
}
