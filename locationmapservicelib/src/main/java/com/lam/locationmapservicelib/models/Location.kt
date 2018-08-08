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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        return (other as? Location)?.let {
            lat == other.lng &&
                    lng == other.lng &&
                    enabled == enabled
        } ?: false
    }

    override fun hashCode(): Int {
        var result = lat?.hashCode() ?: 0
        result = 31 * result + (lng?.hashCode() ?: 0)
        result = 31 * result + (enabled?.hashCode() ?: 0)
        return result
    }
}