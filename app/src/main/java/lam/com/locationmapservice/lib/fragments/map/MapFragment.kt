package lam.com.locationmapservice.lib.fragments.map

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.controllers.location.LocationController
import lam.com.locationmapservice.lib.fragments.LMSFragment
import org.androidannotations.annotations.EFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.google.android.gms.maps.model.BitmapDescriptor
import lam.com.locationmapservice.lib.utils.ImageLoader


@EFragment(R.layout.fragment_map)
open class MapFragment : LMSFragment() {
    var mapView: MapView? = null
    private var googleMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        setupMaps(rootView.findViewById(R.id.mapView), savedInstanceState)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LocationController.LOCATION_REQUEST_CODE) {
            enableMyLocation(true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LocationController.LOCATION_REQUEST_CODE_RATIONAL) {
            enableMyLocation()
        }
    }

    private fun setupMaps(mapView: MapView, savedInstanceState: Bundle?) {
        this.mapView = mapView

        this.mapView?.onCreate(savedInstanceState)
        this.mapView?.onResume() // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(activity?.applicationContext)
        } catch (e: Exception) {

        }

        this.mapView?.getMapAsync { mMap ->
            googleMap = mMap

            // For dropping a marker at a point on the Map
            val sydney = LatLng(-34.0, 151.0)
            val herlev = LatLng(55.72, 12.44)
            val roskilde = LatLng(55.63, 12.08)
            val gilleleje = LatLng(56.13, 12.31)
            googleMap?.addMarker(MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"))
            googleMap?.uiSettings?.setAllGesturesEnabled(true)

            context?.let { contextInner ->
                googleMap?.addCircle(
                        CircleOptions()
                                .center(herlev)
                                .radius(500.0)
                                .fillColor(ContextCompat.getColor(contextInner, R.color.rose_light))
                                .strokeColor(ContextCompat.getColor(contextInner, R.color.rose_main))
                )
            }

            googleMap?.addMarker(
                    MarkerOptions()
                            .position(roskilde)
                            .flat(false)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.as_pages_match_location_illustration)))

            ImageLoader.getDrawable(context, R.drawable.shape_map_heat_map_marker)?.let { heatMapDrawable ->
                googleMap?.addGroundOverlay(
                        GroundOverlayOptions()
                                .position(gilleleje, 100f)
                                .image(getMarkerIconFromDrawable(heatMapDrawable))
                                .transparency(0.5f))
            }
//            googleMap?.addCircle()

            setupLocationService()
        }
    }

    private fun setupLocationService() {
        context?.let { contextInner ->
            if (!LocationController.hasDeviceLocationPermission(contextInner)) {
                LocationController.requestPermission(this, null)
            }

            if (!LocationController.isProviderEnabled(contextInner)) {
                LocationController.askUserToTurnOnLocationServices(contextInner)
            } else {
                enableMyLocation(true)
            }


//            if (enableMyLocation(true) != true) {
//                if (!LocationController.hasDeviceLocationPermission(contextInner)) {
//                    LocationController.requestPermission(this, null)
//                } else if (!LocationController.isProviderEnabled(contextInner)) {
//                    LocationController.askUserToTurnOnLocationServices(contextInner)
//                } else {
//                    Dialog.showSimpleDialog(context, R.string.location_dialog_error_enable_title, R.string.location_dialog_error_enable_content, R.string.location_dialog_error_enable_action)
//                    Toast.makeText(contextInner, "Setup location failed", Toast.LENGTH_LONG).show()
//                }
//            }
        }
    }

    @SuppressLint("MissingPermission") // Checked in LocationController
    private fun enableMyLocation(zoomToMyLocation: Boolean = false): Boolean? {
        if (LocationController.hasFullLocationPermission(context) && LocationController.isProviderEnabled(context) && googleMap?.isMyLocationEnabled != true) {
            googleMap?.isMyLocationEnabled = true

            if (zoomToMyLocation) {
                LocationController.getCachedLocation(context)?.let { location ->
                    zoomCameraTo(location.latitude, location.longitude, 12f)
                } ?: kotlin.run {
                    googleMap?.isMyLocationEnabled = false
                    LocationController.askUserToTurnOnLocationServices(context)
                }
            }
        }
        return googleMap?.isMyLocationEnabled
    }

    private fun zoomCameraTo(latitude: Double, longitude: Double, zoom: Float?) {
        var cameraBuilder = CameraPosition.Builder().target(LatLng(latitude, longitude))
        zoom?.let { cameraBuilder = cameraBuilder.zoom(zoom) }
        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()))
    }

    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}