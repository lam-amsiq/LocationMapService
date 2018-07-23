package lam.com.locationmapservice.demo.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.View
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import lam.com.locationmapservice.R
import lam.com.locationmapservice.demo.fragments.DemoFullscreenFragment
import org.androidannotations.annotations.EActivity

@EActivity
abstract class DemoActivity : RxAppCompatActivity() {
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

    fun beginTransactionTo(fragment: Fragment?, applyAnimation: Boolean = true) {
        if (fragment?.isAdded == true) return

        val currentFrag = currentFragment
        // Set custom animation
        val ft = supportFragmentManager.beginTransaction()

        if (applyAnimation) {
            ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
        }

        when (fragment) {
            is DemoFullscreenFragment -> {
                ft.replace(R.id.fullscreenFragmentPlaceholder, fragment, fragment.javaClass.toString()).addToBackStack(currentFrag?.tag).commitAllowingStateLoss()
            }
            else -> {
                ft.add(R.id.fragmentPlaceholder, fragment, fragment?.javaClass.toString()).addToBackStack(currentFrag?.tag).commitAllowingStateLoss()
            }
        }
    }

    fun goToFullScreen(state: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return

        val decorView = this.window?.decorView
        if (state) {
            decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        } else {
            decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    fun clearStack() {
        try {
            supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } catch (ignore: IllegalStateException) {
        }
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}