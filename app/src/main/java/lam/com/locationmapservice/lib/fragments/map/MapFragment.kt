package lam.com.locationmapservice.lib.fragments.map

import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.fragments.LMSFragment
import org.androidannotations.annotations.EFragment

@EFragment(R.layout.fragment_map)
open class MapFragment : LMSFragment() {
    private var mapFragment: SupportMapFragment? = null
}