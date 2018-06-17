package lam.com.locationmapservice.lib.models

open class Location(
        var account_id: Long = 0,
        var enabled: Boolean? = false,
        var position_latitude: Double? = null,
        var position_longitude: Double? = null)