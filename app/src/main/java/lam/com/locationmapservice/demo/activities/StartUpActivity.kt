package lam.com.locationmapservice.demo.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.FragmentManager
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.activities.LMSActivity
import lam.com.locationmapservice.lib.fragments.map.MapFragment_
import org.androidannotations.annotations.EActivity

@SuppressLint("Registered")
@EActivity(R.layout.activity_start_up)
open class StartUpActivity : LMSActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainFragment = MapFragment_.builder().build()
        clearStack()
        this.beginTransactionTo(mainFragment)
    }

    private fun clearStack() {
        try {
            supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } catch (ignore: IllegalStateException) { }
    }
}
