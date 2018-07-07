package lam.com.locationmapservice.lib.models

import android.os.Bundle
import lam.com.locationmapservice.lib.enums.LocationUpdateType

class LocationUpdate(
        var type: LocationUpdateType,
        var data: Bundle) {
    companion object {
        const val LOCATION_BUNDLE_KEY = "location"
        const val PROVIDER_BUNDLE_KEY = "provider"
        const val PROVIDER_ENABLE_BUNDLE_KEY = "provider_enable"
        const val STATUS_BUNDLE_KEY = "status"
        const val EXTRA_BUNDLE_KEY = "extra"
    }
}