package lam.com.locationmapservice.lib.models

import com.google.android.gms.maps.model.LatLng
import io.realm.RealmObject

open class Location(
        var position_latitude: Double? = null,
        var position_longitude: Double? = null) : RealmObject() {

    fun toLatLng(): LatLng? {
        return position_latitude?.let { lat ->
            position_longitude?.let { lng ->
                LatLng(lat, lng)
            }
        }
    }
}