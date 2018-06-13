package lam.com.locationmapservice.lib.fragments.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.fragments.LMSFragment
import org.androidannotations.annotations.EFragment

@EFragment(R.layout.fragment_map)
open class MapFragment : LMSFragment() {
    var mMapView: MapView? = null
    private var googleMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        mMapView = rootView.findViewById(R.id.mapView)

        mMapView?.onCreate(savedInstanceState)
        mMapView?.onResume() // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(activity?.applicationContext)
        } catch (e: Exception) {

        }

        mMapView?.getMapAsync { mMap ->
            googleMap = mMap

            // For showing a move to my location button
            context?.let { contextInner ->
                if (ContextCompat.checkSelfPermission(contextInner, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap?.isMyLocationEnabled = true
                } else {
                    Toast.makeText(contextInner, "Missing location permission!", Toast.LENGTH_LONG).show()
                }
            }

            // For dropping a marker at a point on the Map
            val sydney = LatLng(-34.0, 151.0)
            googleMap?.addMarker(MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"))

            // For zooming automatically to the location of the marker
            val cameraPosition = CameraPosition.Builder().target(sydney).zoom(12f).build()
            googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView?.onLowMemory()
    }


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        (mapFragment as? SupportMapFragment)?.getMapAsync(this)
//    }
//
//    @AfterViews
//    internal fun afterViews() {
//        mapWrapper?.post {
//            (mapFragment as? SupportMapFragment)?.getMapAsync(this)
//        }
//    }
}