package lam.com.locationmapservice.lib.activities

import android.content.Intent
import android.support.v4.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.fragments.LMSFragment
import org.androidannotations.annotations.EActivity

@EActivity
abstract class LMSActivity: RxAppCompatActivity() {
    var currentDialog: MaterialDialog? = null

    val currentFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.fragmentPlaceholder)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        currentFragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        currentFragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun beginTransactionTo(fragment: LMSFragment?) {
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
}