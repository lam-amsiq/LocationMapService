package lam.com.locationmapservice.lib.fragments.map.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.exceptions.RealmMigrationNeededException
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.controllers.location.LocationController
import lam.com.locationmapservice.lib.enums.LocationUpdateType
import lam.com.locationmapservice.lib.models.Location
import lam.com.locationmapservice.lib.models.Annotation
import lam.com.locationmapservice.lib.models.LocationUpdate

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

    fun onRequestPermissionsResult(context: Context?, requestCode: Int) {
        if (requestCode == LocationController.LOCATION_PERMISSION_REQUEST_CODE || requestCode == LocationController.LOCATION_PERMISSION_REQUEST_CODE_RATIONAL) {
            enableMyLocation(context, true)
        }
    }

    fun onActivityResult(context: Context?, requestCode: Int) {
        if (requestCode == LocationController.LOCATION_PERMISSION_REQUEST_CODE_RATIONAL || requestCode == LocationController.LOCATION_ENABLE_REQUEST_CODE) {
            enableMyLocation(context, true)
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

                        if (!emitter.isDisposed) {
                            mMap?.let { map ->
                                emitter.onSuccess(map)
                            } ?: kotlin.run {
                                emitter.onError(Throwable(NullPointerException("Failed initializing map")))
                            }
                        }
                    }
                } ?: kotlin.run {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(NullPointerException("Context is null. Try calling setupMap from onResume()")))
                    }
                }
            } catch (e: Exception) {
                if (!emitter.isDisposed) {
                    emitter.onError(e)
                }
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

                                    if (!emitter.isDisposed) {
                                        emitter.onNext(Pair<Annotation, Marker>(annotation, marker))
                                    }
                                }
                    }
                    true
                })
            } ?: kotlin.run {
                if (!emitter.isDisposed) {
                    emitter.onError(NullPointerException("Map is null. Try calling setupMap() before calling getAnnotationObserver()"))
                }
            }
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
                enableMyLocation(context, true)
            }
        }
    }

    var locationObservable: Disposable? = null

    @SuppressLint("MissingPermission") // Checked in LocationController
    private fun enableMyLocation(context: Context?, zoomToMyLocation: Boolean = false): Boolean? {
        mapView?.context?.let { contextInner ->
            if (LocationController.hasFullPermissionAndIsProviderEnable(contextInner) && googleMap?.isMyLocationEnabled != true) {

                if (zoomToMyLocation) {
                    LocationController.getCachedLocation(contextInner)?.let { location ->
                        googleMap?.isMyLocationEnabled = true
                        zoomCameraTo(Location(location.latitude, location.longitude), 12f)
                    } ?: kotlin.run {
                        googleMap?.isMyLocationEnabled = false
                        locationObservable = LocationController.getLocationASync(context, Looper.getMainLooper())
                                ?.observeOn(AndroidSchedulers.mainThread())
                                ?.subscribeOn(Schedulers.io())
                                ?.subscribe({ locationUpdate ->
                                    if (locationUpdate.type == LocationUpdateType.Location) {
                                        val location = locationUpdate.data.getParcelable<android.location.Location>(LocationUpdate.LOCATION_BUNDLE_KEY)
                                        googleMap?.isMyLocationEnabled = true
                                        zoomCameraTo(Location(location.latitude, location.longitude), 12f)
                                        locationObservable?.dispose()
                                    }
                                }, { error ->
                                    Log.d("Location", "Failed to get location async: $error")
                                })
                    }
                }
            }
        }
        return googleMap?.isMyLocationEnabled
    }

    inline fun <Unit> setOnCameraListener(crossinline body: () -> Unit) {
        getGoogleMap()?.setOnCameraIdleListener { body() }
    }

    fun getGoogleMap() = googleMap

    fun getViewportBounds(): LatLngBounds? {
        return googleMap?.projection?.visibleRegion?.latLngBounds
    }

    fun clearMap() {
        googleMap?.clear()
    }

    fun getGroupDistance(annotationSize: Int): Double {
        val viewportBounds = getViewportBounds()
        val viewportWidth = (viewportBounds?.northeast?.longitude ?: 0.0) - (viewportBounds?.southwest?.longitude ?: 100.0)
        val viewportHeight = (viewportBounds?.northeast?.longitude ?: 0.0) - (viewportBounds?.southwest?.longitude ?: 100.0)
        return (if(viewportWidth > viewportHeight) viewportWidth else viewportHeight) / (annotationSize / 8.0)
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
                    .visible(false)
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

    private fun zoomCameraTo(location: Location?, zoom: Float?) {
        location?.toLatLng()?.let { latLng ->
            var cameraBuilder = CameraPosition.Builder().target(latLng)
            zoom?.let { cameraBuilder = cameraBuilder.zoom(it) }

            googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()))
        }
    }

    private fun setAnnotationListener(map: GoogleMap, annotationListener: GoogleMap.OnMarkerClickListener) {
        map.setOnMarkerClickListener(annotationListener)
    }
}