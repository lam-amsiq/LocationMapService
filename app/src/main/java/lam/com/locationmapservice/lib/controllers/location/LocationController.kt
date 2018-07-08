package lam.com.locationmapservice.lib.controllers.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.schedulers.Schedulers
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.enums.LocationUpdateType
import lam.com.locationmapservice.lib.exceptions.location.MissingDevicePermissionException
import lam.com.locationmapservice.lib.models.LocationUpdate
import lam.com.locationmapservice.lib.views.dialog.Dialog
import lam.com.locationmapservice.lib.views.dialog.DialogActionItemModel

object LocationController {
    const val TAG = "LocationController"
    const val LOCATION_ENABLE_REQUEST_CODE = 4000
    const val LOCATION_PERMISSION_REQUEST_CODE = 4001
    const val LOCATION_PERMISSION_REQUEST_CODE_RATIONAL = 4002

    private const val PROVIDER = LocationManager.NETWORK_PROVIDER
    private val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    private var isShowingNativePrompt = false

    private var emitter: ObservableEmitter<LocationUpdate>? = null
    private var observable: Observable<LocationUpdate>? = null
        get() = if (field != null) {
            field
        } else {
            Observable.create<LocationUpdate> { emitter -> this.emitter = emitter }
                    .observeOn(Schedulers.io())
        }

    private val locationListener = object : android.location.LocationListener {
        override fun onLocationChanged(location: Location?) {
            val data = Bundle()
            data.putParcelable(LocationUpdate.LOCATION_BUNDLE_KEY, location)

            emitter?.onNext(LocationUpdate(LocationUpdateType.Location, data))
        }

        override fun onStatusChanged(provider: String?, status: Int, extra: Bundle?) {
            val data = Bundle()
            data.putString(LocationUpdate.PROVIDER_BUNDLE_KEY, provider)
            data.putInt(LocationUpdate.STATUS_BUNDLE_KEY, status)
            data.putBundle(LocationUpdate.EXTRA_BUNDLE_KEY, extra)

            emitter?.onNext(LocationUpdate(LocationUpdateType.Status, data))
        }

        override fun onProviderEnabled(provider: String?) {
            val data = Bundle()
            data.putString(LocationUpdate.PROVIDER_BUNDLE_KEY, provider)
            data.putBoolean(LocationUpdate.PROVIDER_ENABLE_BUNDLE_KEY, true)

            emitter?.onNext(LocationUpdate(LocationUpdateType.Status, data))
        }

        override fun onProviderDisabled(provider: String?) {
            val data = Bundle()
            data.putString(LocationUpdate.PROVIDER_BUNDLE_KEY, provider)
            data.putBoolean(LocationUpdate.PROVIDER_ENABLE_BUNDLE_KEY, true)

            emitter?.onNext(LocationUpdate(LocationUpdateType.Status, data))

//            ApplicationController.getInstance().currentActivity?.let { activity ->
//                val manager = activity.packageManager
//                val appIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                if (appIntent.resolveActivity(manager) != null) {
//                    askUserToTurnOnLocationServices(activity)
//                } else {
//                    emitter?.onComplete()
//                }
//            }
        }
    }

    fun askUserToTurnOnLocationServices(context: Context?) {
        Dialog.show(context, null, context?.getString(R.string.location_dialog_location_is_off_title), context?.getString(R.string.location_dialog_location_is_off_content),
                DialogActionItemModel(context?.getString(R.string.shared_action_cancel), Runnable {
                    emitter?.onComplete()
                }),
                DialogActionItemModel(context?.getString(R.string.shared_action_ok), Runnable {
                    (context as? Activity)?.let { activity ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        ActivityCompat.startActivityForResult(activity, intent, LOCATION_ENABLE_REQUEST_CODE, null)
                    }
                }))
    }

    private fun goToSettings(context: Context) {
        (context as? Activity)?.let { activity ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.packageName))
            intent.addCategory(Intent.CATEGORY_DEFAULT)

            ActivityCompat.startActivityForResult(activity, intent, LOCATION_PERMISSION_REQUEST_CODE_RATIONAL, null)
        }
    }

    @SuppressLint("MissingPermission") // hasDeviceLocationPermission method handles this issue
    fun getLocationASync(context: Context?, looper: Looper?): Observable<LocationUpdate>? {
        val observer = this.observable

        if (hasDeviceLocationPermission(context)) {
            getLocationManager(context)?.requestSingleUpdate(PROVIDER, locationListener, looper)
        } else {
            emitter?.onError(MissingDevicePermissionException("Missing location permissions"))
        }
        return observer
    }

    @SuppressLint("MissingPermission")  // hasDeviceLocationPermission method handles this issue
    fun getUpdates(context: Context?, minTime: Long, minDistance: Float): Observable<LocationUpdate>? {
        val observer = this.observable

        if (hasDeviceLocationPermission(context)) {
            getLocationManager(context)?.requestLocationUpdates(PROVIDER, minTime, minDistance, locationListener)
        } else {
            emitter?.onError(MissingDevicePermissionException("Missing location permissions"))
        }

        return observer
    }

    @SuppressLint("MissingPermission") // hasDeviceLocationPermission method handles this issue
    fun getCachedLocation(context: Context?): Location? {
        return if (hasDeviceLocationPermission(context)) {
            getLocationManager(context)?.getLastKnownLocation(PROVIDER)
        } else {
            null
        }
    }

    private fun getLocationManager(context: Context?): LocationManager? {
        return context?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    fun requestPermission(context: Context?, cancelAction: Runnable? = null) {
        if (shouldRequestRational(context)) {
            showDialogAccessDenied(context, cancelAction)
        } else {
            isShowingNativePrompt = true
            (context as? Activity)?.let { activity ->
                ActivityCompat.requestPermissions(activity,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun shouldRequestRational(context: Context?): Boolean {
        var showRational = false
        (context as? Activity)?.let { activity ->
            permissions.forEach { permission ->
                showRational = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) == true
            }
        }
        return showRational
    }

    fun hasFullPermissionAndIsProviderEnable(context: Context?): Boolean {
        return hasDeviceLocationPermission(context) && isProviderEnabled(context)
    }

    fun hasDeviceLocationPermission(context: Context?): Boolean {
        context?.let { contextInner ->
            return permissions.none { permission ->
                ContextCompat.checkSelfPermission(contextInner, permission) == PackageManager.PERMISSION_DENIED
            }
        }
        return false
    }

    fun isProviderEnabled(context: Context?): Boolean {
        return getLocationManager(context)?.isProviderEnabled(PROVIDER) == true
    }

    private fun showDialogAccessDenied(context: Context?, cancelAction: Runnable? = null) {
        context?.let { contextInner ->
            Dialog.show(context, null, contextInner.resources?.getString(R.string.location_dialog_access_denied_title), contextInner.resources?.getString(R.string.location_dialog_access_denied_content),
                    DialogActionItemModel(contextInner.resources?.getString(R.string.location_dialog_access_denied_action_negative), cancelAction),
                    DialogActionItemModel(contextInner.resources?.getString(R.string.location_dialog_access_denied_action_positive), Runnable {
                        goToSettings(contextInner)
                    }))
        }
    }

    private fun showDialogNoInternet(context: Context?) {
        context?.let { contextInner ->
            Dialog.show(contextInner, null, contextInner.resources?.getString(R.string.location_dialog_no_internet_title), contextInner.resources?.getString(R.string.location_dialog_no_internet_content),
                    DialogActionItemModel(contextInner.resources?.getString(R.string.location_dialog_no_internet_action), null)
            )
        }
    }

    private fun showDialogEnabledFailed(context: Context?) {
        context?.let { contextInner ->
            Dialog.show(context, null, contextInner.resources?.getString(R.string.location_dialog_error_enable_title), contextInner.resources?.getString(R.string.location_dialog_error_enable_content),
                    DialogActionItemModel(contextInner.resources?.getString(R.string.location_dialog_error_enable_action), null)
            )
        }
    }
}