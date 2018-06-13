package lam.com.locationmapservice.demo.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.activities.LMSActivity
import lam.com.locationmapservice.lib.fragments.LMSFragment
import lam.com.locationmapservice.lib.fragments.map.MapFragment_
import lam.com.locationmapservice.lib.interfaces.IFragment
import org.androidannotations.annotations.EActivity

@SuppressLint("Registered")
@EActivity(R.layout.activity_start_up)
open class StartUpActivity : LMSActivity(), IFragment {
    val currentFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.fragmentPlaceholder)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainFragment = MapFragment_.builder().build()
        clearStack()
        this.beginTransactionTo(mainFragment)
    }

    override fun beginTransactionTo(fragment: LMSFragment?) {
        if (fragment?.isAdded == true) return
        val currentFrag = currentFragment

        // Set custom animation
        val ft = supportFragmentManager.beginTransaction()

        val applyAnimation = true
        if (applyAnimation) {
            ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
        }

        val fragmentHolder = R.id.fragmentPlaceholder
        ft.add(fragmentHolder, fragment, fragment?.javaClass.toString()).addToBackStack(currentFrag?.tag).commitAllowingStateLoss()
    }

    private fun clearStack() {
        try {
            supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } catch (ignore: IllegalStateException) { }
    }
}
