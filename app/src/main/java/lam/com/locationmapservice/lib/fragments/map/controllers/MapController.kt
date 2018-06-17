package lam.com.locationmapservice.lib.fragments.map.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import io.reactivex.Observable
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.controllers.location.LocationController
import lam.com.locationmapservice.lib.models.Location

object MapController {
    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null

    private val heatmapGradient: Gradient? = null
        get() = field?.let { it }
                ?: kotlin.run {
                    mapView?.context?.let { contextInner ->
                        val colors = intArrayOf(Color.TRANSPARENT,
                                ContextCompat.getColor(contextInner, R.color.custom_map_heat_marker_end),
                                ContextCompat.getColor(contextInner, R.color.custom_map_heat_marker_middle),
                                ContextCompat.getColor(contextInner, R.color.custom_map_heat_marker_center))
                        val startPoints = floatArrayOf(0f, 0.30f, 0.9f, 1f)
                        if (colors.size == startPoints.size) {
                            Gradient(colors, startPoints)
                        } else {
                            null
                        }
                    }
                }

    fun onResume() {
        mapView?.onResume()
    }

    fun onPause() {
        mapView?.onPause()
    }

    fun onDestroy() {
        mapView?.onDestroy()
    }

    fun onLowMemory() {
        mapView?.onLowMemory()
    }

    fun onRequestPermissionsResult(requestCode: Int) {
        if (requestCode == LocationController.LOCATION_REQUEST_CODE) {
            enableMyLocation(true)
        }
    }

    fun onActivityResult(requestCode: Int) {
        if (requestCode == LocationController.LOCATION_REQUEST_CODE_RATIONAL) {
            enableMyLocation()
        }
    }

    fun setupMaps(context: Context, fragment: Fragment, mapView: MapView, savedInstanceState: Bundle?): Observable<GoogleMap> {
        return Observable.create { emitter ->
            this.mapView = mapView

            this.mapView?.onCreate(savedInstanceState)
            this.mapView?.onResume() // needed to get the map to display immediately

            try {
                MapsInitializer.initialize(context)

                this.mapView?.getMapAsync { mMap ->
                    googleMap = mMap

                    setupLocationService(fragment)
                    emitter.onNext(mMap)
                    emitter.onComplete()
                }
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    fun setupLocationService(fragment: Fragment) {
        mapView?.context?.let { contextInner ->
            if (!LocationController.hasDeviceLocationPermission(contextInner)) {
                LocationController.requestPermission(fragment, null)
            }

            if (!LocationController.isProviderEnabled(contextInner)) {
                LocationController.askUserToTurnOnLocationServices(contextInner)
            } else {
                enableMyLocation(true)
            }
        }
    }

    @SuppressLint("MissingPermission") // Checked in LocationController
    fun enableMyLocation(zoomToMyLocation: Boolean = false): Boolean? {
        mapView?.context?.let { contextInner ->
            if (LocationController.hasFullLocationPermission(contextInner) && LocationController.isProviderEnabled(contextInner) && googleMap?.isMyLocationEnabled != true) {
                googleMap?.isMyLocationEnabled = true

                if (zoomToMyLocation) {
                    LocationController.getCachedLocation(contextInner)?.let { location ->
                        zoomCameraTo(Location(-1, true, location.latitude, location.longitude), 12f)
                    } ?: kotlin.run {
                        googleMap?.isMyLocationEnabled = false
                        LocationController.askUserToTurnOnLocationServices(contextInner)
                    }
                }
            }
        }
        return googleMap?.isMyLocationEnabled
    }

    fun zoomCameraTo(location: Location?, zoom: Float?) {
        getLatLngFromLocation(location)?.let { latLgn ->
            var cameraBuilder = CameraPosition.Builder().target(latLgn)
            zoom?.let { cameraBuilder = cameraBuilder.zoom(zoom) }

            googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()))
        }
    }

    fun addUserCircle(position: LatLng): Circle? {
        mapView?.context?.let { contextInner ->
            val size = contextInner.resources.getDimensionPixelSize(R.dimen.map_user_marker_size)

            return googleMap?.addCircle(
                    CircleOptions()
                            .center(position)
                            .radius(size.toDouble())
                            .fillColor(ContextCompat.getColor(contextInner, R.color.custom_map_user_marker_fill))
                            .strokeColor(ContextCompat.getColor(contextInner, R.color.custom_map_user_marker_stroke)))
        }
        return null
    }

    fun addAnnotation(position: LatLng, icon: Int): Marker? {
        return addAnnotation(position, BitmapDescriptorFactory.fromResource(icon))
    }

    fun addAnnotation(position: LatLng, icon: BitmapDescriptor): Marker? {
        return googleMap?.addMarker(MarkerOptions()
                .position(position)
                .icon(icon))
    }

    fun addHeatmap(data: List<LatLng>): TileOverlay? {
        return googleMap?.addTileOverlay(TileOverlayOptions().tileProvider(HeatmapTileProvider.Builder()
                .data(data)
                .gradient(heatmapGradient)
                .radius(50)
                .build()))
    }

    fun setAnnotationListener(annotationListener: GoogleMap.OnMarkerClickListener) {
        googleMap?.setOnMarkerClickListener(annotationListener)
    }

    private fun getLatLngFromLocation(location: Location?): LatLng? {
        location?.position_latitude?.let { lat ->
            location.position_longitude?.let { lng ->
                return LatLng(lat, lng)
            }
        }
        return null
    }
}