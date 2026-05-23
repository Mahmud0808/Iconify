package com.drdisagree.iconify.features.xposed.lockscreen.location.models

data class LocationBrowseItem(
    val cityExt: String,
    private val countryId: String,
    val city: String,
    val lat: Double,
    val lon: Double
) {
    private val id: String get() = "$city,$countryId"

    override fun equals(other: Any?) = (other is LocationBrowseItem) && id == other.id
    override fun hashCode(): Int {
        var result = cityExt.hashCode()
        result = 31 * result + countryId.hashCode()
        result = 31 * result + city.hashCode()
        result = 31 * result + lat.hashCode()
        result = 31 * result + lon.hashCode()
        return result
    }
}