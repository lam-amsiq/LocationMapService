package lam.com.locationmapservice.lib.fragments.map

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import io.reactivex.android.schedulers.AndroidSchedulers
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.fragments.LMSFragment
import org.androidannotations.annotations.EFragment
import lam.com.locationmapservice.lib.fragments.map.controllers.MapController

@EFragment(R.layout.fragment_map)
open class MapFragment : LMSFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        context?.let { contextInner ->
            MapController.setupMaps(contextInner, this, rootView.findViewById(R.id.mapView), savedInstanceState)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        MapController.setAnnotationListener(GoogleMap.OnMarkerClickListener {
                            // TODO: Add annotation click event
                            true
                        })
                    }
        }
        return rootView
    }

    override fun onResume() {
        super.onResume()
        MapController.onResume()
    }

    override fun onPause() {
        super.onPause()
        MapController.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        MapController.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        MapController.onLowMemory()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        MapController.onRequestPermissionsResult(requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        MapController.onActivityResult(requestCode)
    }
}