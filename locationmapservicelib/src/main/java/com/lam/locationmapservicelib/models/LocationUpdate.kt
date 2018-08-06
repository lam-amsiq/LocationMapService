package com.lam.locationmapservicelib.models

import android.location.Location
import android.os.Bundle
import com.lam.locationmapservicelib.enums.LocationUpdateType

class LocationUpdate(
        var type: LocationUpdateType,
        var data: Bundle) {

    fun getLocation(): Location? {
        return data.getParcelable(LocationUpdate.LOCATION_BUNDLE_KEY)
    }

    fun getProvider(): String? {
        return data.getString(LocationUpdate.PROVIDER_BUNDLE_KEY)
    }

    fun isProviderEnabled(): Boolean? {
        return if (data.containsKey(LocationUpdate.PROVIDER_ENABLE_BUNDLE_KEY))
            data.getBoolean(LocationUpdate.PROVIDER_ENABLE_BUNDLE_KEY)
        else null
    }

    fun getStatus(): Int? {
        return if (data.containsKey(LocationUpdate.STATUS_BUNDLE_KEY))
            data.getInt(LocationUpdate.STATUS_BUNDLE_KEY)
        else null
    }

    fun getExtra(): Bundle? {
        return data.getBundle(LocationUpdate.EXTRA_BUNDLE_KEY)
    }

    companion object {
        const val LOCATION_BUNDLE_KEY = "location"
        const val PROVIDER_BUNDLE_KEY = "provider"
        const val PROVIDER_ENABLE_BUNDLE_KEY = "provider_enable"
        const val STATUS_BUNDLE_KEY = "status"
        const val EXTRA_BUNDLE_KEY = "extra"
    }
}