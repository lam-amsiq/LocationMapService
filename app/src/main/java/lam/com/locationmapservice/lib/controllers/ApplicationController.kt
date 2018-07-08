package lam.com.locationmapservice.lib.controllers

import android.app.Application
import android.content.Context
import android.os.Build
import android.support.multidex.MultiDex
import com.squareup.picasso.LruCache
import com.squareup.picasso.Picasso
import lam.com.locationmapservice.demo.activities.DemoActivity

class ApplicationController : Application() {
    private var defaultUEH: Thread.UncaughtExceptionHandler? = null

    var isActivityVisible: Boolean = false
        private set

    var currentActivity: DemoActivity? = null
        private set(activity) {
            field = activity
        }

    //region Device arguments methods
    val brand: String
        get() = Build.BRAND.toLowerCase()

    val manifacturer: String
        get() = Build.MANUFACTURER.toLowerCase()

    val model: String
        get() = Build.MODEL

    val androidVersion: String
        get() {
            var version = Build.VERSION.RELEASE.toLowerCase()
            version = version.replace("android", "")
            version = version.trim { it <= ' ' }

            return version
        }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        instanceCompanion = this

        setupCustomExceptionHandler()
        setupPicasso()
    }

    private fun setupCustomExceptionHandler() {
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler()

        // Log uncaught Exceptions
        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            defaultUEH?.uncaughtException(thread, e)
        }
    }

    private fun setupPicasso() {
        // create Picasso.Builder object
        val picassoBuilder = Picasso.Builder(this)

        // Set cache size to 25MB
        picassoBuilder.memoryCache(LruCache(25 * 1024 * 1024))

        // Set Picasso instance
        Picasso.setSingletonInstance(picassoBuilder.build())
    }

    fun activityResumed(activity: DemoActivity) {
        currentActivity = activity
        isActivityVisible = true
    }

    fun activityPaused() {
        currentActivity = null
        isActivityVisible = false
    }

    companion object {
        private var instanceCompanion: ApplicationController? = null
        private var defaultFont: String? = null
        private var defaultBoldFont: String? = null

        fun getInstance(): ApplicationController {
            if (instanceCompanion == null) {
                instanceCompanion = ApplicationController()
            }
            return instanceCompanion!!
        }
    }
}