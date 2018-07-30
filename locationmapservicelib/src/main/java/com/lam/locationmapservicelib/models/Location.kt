package com.lam.locationmapservicelib.models

import com.google.android.gms.maps.model.LatLng
import io.realm.RealmObject

open class Location(
        var lat: Double? = null,
        var lng: Double? = null,
        var enabled: Boolean? = null) : RealmObject() {

    fun toLatLng(): LatLng? {
        return lat?.let { lat ->
            lng?.let { lng ->
                LatLng(lat, lng)
            }
        }
    }

    override fun toString(): String {
        return "latLng($lat, $lng) ${if (enabled == true) {
            "Enabled"
        } else {
            "Disabled"
        }}"
    }
}