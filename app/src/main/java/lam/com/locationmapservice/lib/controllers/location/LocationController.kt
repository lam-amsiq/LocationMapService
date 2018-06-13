package lam.com.locationmapservice.lib.controllers.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.controllers.ApplicationController
import lam.com.locationmapservice.lib.exceptions.location.MissingDevicePermissionException
import lam.com.locationmapservice.lib.fragments.LMSFragment
import lam.com.locationmapservice.lib.views.dialog.Dialog
import lam.com.locationmapservice.lib.views.dialog.DialogActionItemModel
import org.joda.time.DateTime
import org.joda.time.Period
import java.net.UnknownHostException

object LocationController {
    const val LOCATION_STANDARD_VIEW = "location_standard_view"
    private const val LOCATION_DENIED_VIEW = "location_denied_view"
    const val LOCATION_STANDARD_ENABLE = "location_standard_enable"
    const val LOCATION_STANDARD_LATER = "location_standard_later"
    private const val LOCATION_ENABLE_GRANT = "location_standard_enable_grant"
    private const val LOCATION_ENABLE_DENY = "location_standard_enable_deny"
    const val LOCATION_ENABLE_SUCCESS = "location_enable_success"
    const val LOCATION_ENABLE_FAIL = "location_enable_fail"
    const val LOCATION_DISABLE_SUCCESS = "location_disable_success"
    const val LOCATION_DISABLE_FAIL = "location_disable_fail"

    const val TAG = "LocationController"
    const val LOCATION_REQUEST_CODE = 4000
    const val LOCATION_REQUEST_CODE_RATIONAL = 4001
    private const val PROVIDER = LocationManager.NETWORK_PROVIDER
    private var timestamp: DateTime? = null
    private const val UPDATE_TIME_INTERVAL = 30
    private var isShowingNativePrompt = false

    private var observer: Observer<in Location?>? = null
    private val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

    private val locationListener = object : android.location.LocationListener {
        override fun onLocationChanged(location: Location?) {
            observer?.onNext(location)
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
//            Log.i("","")
        }

        override fun onProviderEnabled(p0: String?) {
        }

        override fun onProviderDisabled(p0: String?) {
            ApplicationController.getInstance().currentActivity?.let { activity ->
                val manager = activity.packageManager
                val appIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                if (appIntent.resolveActivity(manager) != null) {
                    askUserToTurnOnLocationServices(activity)
                } else {
                    observer?.onComplete()
                }
            }
        }
    }

    fun askUserToTurnOnLocationServices(context: Context?) {
        Dialog.show(context, null, context?.getString(R.string.location_dialog_location_is_off_title), context?.getString(R.string.location_dialog_location_is_off_content),
                DialogActionItemModel(context?.getString(R.string.shared_action_cancel), Runnable {
                    observer?.onComplete()
                }),
                DialogActionItemModel(context?.getString(R.string.shared_action_ok), Runnable {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    context?.startActivity(intent)
                }))
    }

    fun runUpdateFlow() {
        val context = ApplicationController.getInstance().applicationContext
        if (!hasFullLocationPermission(context)) return

        requestLocationUpdateAndPatch(context)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ _ ->
                    Log.i(TAG, "Notify backend successful")
                }, { _ ->
                    Log.i(TAG, "Notify backend failed")
                })
    }

    fun requestLocationUpdateAndPatch(context: Context?): Observable<LocationResponse> {
        return Observable.create { emitter ->
            getLocationASync(context, Looper.getMainLooper(), LocationObserver(
                    Consumer { location ->
                        patchLocation(createLocationPatch(true, location))
                                ?.observeOn(AndroidSchedulers.mainThread())
                                ?.subscribe(
                                        { response ->
                                            if (!emitter.isDisposed) {
                                                emitter.onNext(response)
                                            }
                                        },
                                        { error ->
                                            if (error is UnknownHostException) {
                                                showDialogNoInternet(context)
                                            }

                                            if (!emitter.isDisposed) {
                                                emitter.onError(error)
                                            }
                                        },
                                        {
                                            if (!emitter.isDisposed) {
                                                emitter.onComplete()
                                            }
                                        })
                    },
                    Consumer { error ->
                        emitter.onError(error)
                    }))
        }
    }

    fun isExpired(): Boolean {
        val locationTimestamp = timestamp
                ?: DateTime()
        return locationTimestamp.plus(Period.minutes(UPDATE_TIME_INTERVAL)).isBeforeNow
    }

    fun handlePermissionsResult(fragment: LMSFragment, requestCode: Int, grantResults: IntArray): Boolean? {
        val isLocationResult = requestCode == LOCATION_REQUEST_CODE
        if (isLocationResult) {
            grantResults.forEach { grantResult ->
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    if (isShowingNativePrompt) Log.d(fragment.TAG, LOCATION_ENABLE_DENY)
                    return isLocationResult
                }
            }
            if (isShowingNativePrompt)  Log.d(fragment.TAG, LOCATION_ENABLE_GRANT)

            isShowingNativePrompt = false

            runEnableFlow(true, fragment)?.subscribe(
                    {
                        Log.d(fragment.TAG, LOCATION_ENABLE_SUCCESS)
                    },
                    {
                        Log.d(fragment.TAG, LOCATION_ENABLE_FAIL)
                    })
        }
        return isLocationResult
    }

    fun runEnableFlow(_enabled: Boolean, fragment: Fragment?): Observable<LocationResponse>? {
        return patchCachedLocation(_enabled, fragment)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnNext { response ->
                    val enabled = response.location?.enabled
                    if (enabled == true) {
                        requestLocationUpdateAndPatch(fragment?.context).subscribe({}, {})
                    }
                }
                ?.doOnError { error ->
                    if (error is UnknownHostException) {
                        showDialogNoInternet(fragment?.context)
                    } else {
                        showDialogEnabledFailed(fragment?.context)
                    }
                }
    }

    private fun patchCachedLocation(enabled: Boolean, fragment: Fragment?): Observable<LocationResponse>? {
        val location = if (enabled) {
            getCachedLocation(fragment)
        } else {
            null
        }

        if (location == null && enabled) {
            return Observable.empty()
        }

        return patchLocation(enabled, location)
    }

    private fun patchLocation(enabled: Boolean?, _location: Location?): Observable<LocationResponse>? {
        val location = createLocationPatch(enabled, _location)
        return patchLocation(location)
    }

    private fun showDialogAccessDenied(fragment: Fragment, cancelAction: Runnable? = null) {
        fragment.context?.let { context ->
            (fragment as? LMSFragment)?.let { frag ->
                Log.d(frag.TAG, LOCATION_DENIED_VIEW)
            }
            Dialog.show(context, null, context.resources?.getString(R.string.location_dialog_access_denied_title), context.resources?.getString(R.string.location_dialog_access_denied_content),
                    DialogActionItemModel(context.resources?.getString(R.string.location_dialog_access_denied_action_negative), cancelAction),
                    DialogActionItemModel(context.resources?.getString(R.string.location_dialog_access_denied_action_positive), Runnable {
                        goToSettings(fragment)
                    }))
        }
    }

    private fun showDialogNoInternet(context: Context?) {
        Dialog.show(context, null, context?.resources?.getString(R.string.location_dialog_no_internet_title), context?.resources?.getString(R.string.location_dialog_no_internet_content),
                DialogActionItemModel(context?.resources?.getString(R.string.location_dialog_no_internet_action), null)
        )
    }

    private fun showDialogEnabledFailed(context: Context?) {
        Dialog.show(context, null, context?.resources?.getString(R.string.location_dialog_error_enable_title), context?.resources?.getString(R.string.location_dialog_error_enable_content),
                DialogActionItemModel(context?.resources?.getString(R.string.location_dialog_error_enable_action), null)
        )
    }

    private fun goToSettings(fragment: Fragment) {
        fragment.context?.let { context ->
            val myAppSettings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.packageName))
            myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
            fragment.startActivityForResult(myAppSettings, LOCATION_REQUEST_CODE_RATIONAL)
        }
    }

    private fun patchLocation(json: HashMap<String, Any?>): Observable<LocationResponse>? {
        timestamp = DateTime() // Update timestamp on location

        return null // TODO: Patch location
    }

    private fun getLocationManager(context: Context?): LocationManager? {
        return context?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    @SuppressLint("MissingPermission") // hasDeviceLocationPermission method handles this issue
    fun getLocationASync(context: Context?, looper: Looper?, observer: io.reactivex.Observer<in Location?>) {
        LocationController.observer = observer

        if (hasDeviceLocationPermission(context)) {
            getLocationManager(context)?.requestSingleUpdate(PROVIDER, locationListener, looper)
        } else {
            LocationController.observer?.onError(MissingDevicePermissionException("Missing location permissions"))
        }
    }

    @SuppressLint("MissingPermission") // hasDeviceLocationPermission method handles this issue
    private fun getCachedLocation(fragment: Fragment?): Location? {
        return if (hasDeviceLocationPermission(fragment?.context)) {
            getLocationManager(fragment?.context)?.getLastKnownLocation(PROVIDER)
                    ?: Location(PROVIDER)
        } else {
            null
        }
    }

    @SuppressLint("MissingPermission") // hasDeviceLocationPermission method handles this issue
    fun getCachedLocation(context: Context?): Location? {
        return if (hasDeviceLocationPermission(context)) {
            getLocationManager(context)?.getLastKnownLocation(PROVIDER)
        } else {
            null
        }
    }

    fun requestPermission(fragment: Fragment?, cancelAction: Runnable? = null) {
        if (shouldRequestRational(fragment)) {
            fragment?.let { showDialogAccessDenied(it, cancelAction) }
        } else {
            isShowingNativePrompt = true
            fragment?.requestPermissions(
                    permissions,
                    LOCATION_REQUEST_CODE)
        }
    }

    private fun shouldRequestRational(fragment: Fragment?): Boolean {
        var showRational = false
        permissions.forEach { permission ->
            showRational = fragment?.shouldShowRequestPermissionRationale(permission) == true
        }
        return showRational
    }

    private fun createLocationPatch(enabled: Boolean?, location: Location?): HashMap<String, Any?> {
        val patch = HashMap<String, Any?>()

        if (enabled == true) {
            patch["position_latitude"] = location?.latitude ?: 0.0
            patch["position_longitude"] = location?.longitude ?: 0.0
        }
        patch["enabled"] = enabled

        return patch
    }

    fun hasDeviceLocationPermission(context: Context?): Boolean {
        context?.let { contextInner ->
            return permissions.none { permission ->
                ContextCompat.checkSelfPermission(contextInner, permission) == PackageManager.PERMISSION_DENIED
            }
        }
        return false
    }

    private fun hasUserLocationPermission(): Boolean {
        return true // TODO: Check in app location permission
    }

    fun hasFullLocationPermission(context: Context?): Boolean {
        val deviceEnabled = hasDeviceLocationPermission(context)
        val userEnabled = hasUserLocationPermission()
        val hasFullPermission = deviceEnabled && userEnabled
        return hasFullPermission
    }

    fun hasFullPermissionAndIsActive(context: Context?): Boolean {
        val hasFullPermission = hasFullLocationPermission(context)
        return hasFullPermission && isProviderEnabled(context)
    }

    fun isProviderEnabled(context: Context?): Boolean {
        return getLocationManager(context)?.isProviderEnabled(PROVIDER) == true
    }

    fun canUpload(context: Context?): Boolean {
        return hasFullLocationPermission(context) && isProviderEnabled(context)
    }
}