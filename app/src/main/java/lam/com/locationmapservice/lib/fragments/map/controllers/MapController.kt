package lam.com.locationmapservice.lib.fragments.map.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.exceptions.RealmMigrationNeededException
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.controllers.location.LocationController
import lam.com.locationmapservice.lib.models.Location
import lam.com.locationmapservice.lib.models.Annotation

object MapController {
    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null
    private var realmConfig: RealmConfiguration? = null

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
        MapController.realmConfig?.let { config ->
            val deleteRealm = Realm.deleteRealm(config)
            Log.d("realm", "Delete realm: $deleteRealm")
        }
        mapView?.onDestroy()
    }

    fun onLowMemory() {
        mapView?.onLowMemory()
    }

    fun realmInit(context: Context?) {
        context?.let { contextInner ->
            Realm.init(contextInner)
            realmConfig = RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .name("map.realm").build()
        }
    }

    fun getRealm(): Realm? {
        try {
            realmConfig?.let { config ->
                Realm.getInstance(config)?.let { realm ->
                    return realm
                }
            }
        } catch (e: RealmMigrationNeededException) {
            realmConfig?.let { config ->
                Realm.migrateRealm(config)
                Realm.getInstance(config)?.let { realm ->
                    return realm
                }
            }
        }
        return null
    }

    fun onRequestPermissionsResult(requestCode: Int) {
        if (requestCode == LocationController.LOCATION_PERMISSION_REQUEST_CODE || requestCode == LocationController.LOCATION_PERMISSION_REQUEST_CODE_RATIONAL) {
            enableMyLocation(true)
        }
    }

    fun onActivityResult(requestCode: Int) {
        if (requestCode == LocationController.LOCATION_PERMISSION_REQUEST_CODE_RATIONAL || requestCode == LocationController.LOCATION_ENABLE_REQUEST_CODE) {
            enableMyLocation(true)
        }
    }

    fun setupMapView(mapView: MapView, savedInstanceState: Bundle?) {
        this.mapView = mapView

        this.mapView?.onCreate(savedInstanceState)
        this.mapView?.onResume() // needed to get the map to display immediately
    }

    fun setupMap(context: Context?): Single<GoogleMap> {
        return Single.create<GoogleMap> { emitter ->
            try {
                context?.let { contextInner ->
                    MapsInitializer.initialize(contextInner)

                    this.mapView?.getMapAsync { mMap ->
                        googleMap = mMap

                        setupLocationService(contextInner)

                        mMap?.let { map ->
                            emitter.onSuccess(map)
                        } ?: kotlin.run {
                            emitter.onError(Throwable(NullPointerException("Failed initializing map")))
                        }
                    }
                } ?: kotlin.run {
                    emitter.onError(Throwable(NullPointerException("Context is null. Try calling setupMap from onResume()")))
                }
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    fun getMarkerObserver(): Observable<Pair<Annotation, Marker>> {
        return Observable.create<Pair<Annotation, Marker>> { emitter ->
            googleMap?.let {
                setAnnotationListener(it, GoogleMap.OnMarkerClickListener { marker ->
                    getRealm()?.let { realm ->
                        val annotation = realm.where(Annotation::class.java)
                                .equalTo("marker_id", marker.id)
                                .findFirst()?.let {
                                    val annotation = realm.copyFromRealm(it)
                                    realm.close()

                                    emitter.onNext(Pair<Annotation, Marker>(annotation, marker))
                                }
                    }
                    true
                })
            }
                    ?: kotlin.run { emitter.onError(NullPointerException("Map is null. Try calling setupMap() before calling getAnnotationObserver()")) }
        }
    }

    private fun setupLocationService(context: Context) {
        mapView?.context?.let { contextInner ->
            if (!LocationController.hasDeviceLocationPermission(contextInner)) {
                LocationController.requestPermission(context, null)
            }

            if (!LocationController.isProviderEnabled(contextInner)) {
                LocationController.askUserToTurnOnLocationServices(contextInner)
            } else {
                enableMyLocation(true)
            }
        }
    }

    @SuppressLint("MissingPermission") // Checked in LocationController
    private fun enableMyLocation(zoomToMyLocation: Boolean = false): Boolean? {
        mapView?.context?.let { contextInner ->
            if (LocationController.hasFullPermissionAndIsProviderEnable(contextInner) && googleMap?.isMyLocationEnabled != true) {
                googleMap?.isMyLocationEnabled = true

                if (zoomToMyLocation) {
                    LocationController.getCachedLocation(contextInner)?.let { location ->
                        zoomCameraTo(Location(location.latitude, location.longitude), 12f)
                    } ?: kotlin.run {
                        googleMap?.isMyLocationEnabled = false
                        LocationController.askUserToTurnOnLocationServices(contextInner)
                    }
                }
            }
        }
        return googleMap?.isMyLocationEnabled
    }

    private fun zoomCameraTo(location: Location?, zoom: Float?) {
        location?.toLatLng()?.let { latLng ->
            var cameraBuilder = CameraPosition.Builder().target(latLng)
            zoom?.let { cameraBuilder = cameraBuilder.zoom(it) }

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

    fun addAnnotation(position: Location, icon: Int): Marker? {
        return addAnnotation(position, BitmapDescriptorFactory.fromResource(icon))
    }

    fun addAnnotation(position: Location): Marker? {
        return googleMap?.addMarker(position.toLatLng()?.let { latLng ->
            MarkerOptions()
                    .position(latLng)
        })
    }

    fun addAnnotation(position: Location, icon: BitmapDescriptor): Marker? {
        return googleMap?.addMarker(position.toLatLng()?.let { latLng ->
            MarkerOptions()
                    .position(latLng)
                    .icon(icon)
        })
    }

    fun addHeatmap(data: Collection<LatLng>): TileOverlay? {
        return googleMap?.addTileOverlay(TileOverlayOptions().tileProvider(HeatmapTileProvider.Builder()
                .data(data)
                .gradient(heatmapGradient)
                .radius(50)
                .build()))
    }

    private fun setAnnotationListener(map: GoogleMap, annotationListener: GoogleMap.OnMarkerClickListener) {
        map.setOnMarkerClickListener(annotationListener)
    }
}