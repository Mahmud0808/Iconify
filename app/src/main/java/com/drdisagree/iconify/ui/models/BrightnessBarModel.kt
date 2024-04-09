package com.drdisagree.iconify.ui.models

class BrightnessBarModel {

    var name: String
    var brightness: Int
    var autoBrightness: Int
    var inverseColor: Boolean

    constructor(name: String, brightness: Int, autoBrightness: Int) {
        this.name = name
        this.brightness = brightness
        this.autoBrightness = autoBrightness
        inverseColor = false
    }

    constructor(name: String, brightness: Int, autoBrightness: Int, inverseColor: Boolean) {
        this.name = name
        this.brightness = brightness
        this.autoBrightness = autoBrightness
        this.inverseColor = inverseColor
    }
}
