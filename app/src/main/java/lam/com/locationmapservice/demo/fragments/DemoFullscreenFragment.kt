package lam.com.locationmapservice.demo.fragments

import lam.com.locationmapservice.demo.activities.DemoActivity
import org.androidannotations.annotations.EFragment

@EFragment
abstract class DemoFullscreenFragment: DemoFragment() {
    override fun onDestroy() {
        super.onDestroy()
        (activity as? DemoActivity)?.goToFullScreen(false)
    }
}