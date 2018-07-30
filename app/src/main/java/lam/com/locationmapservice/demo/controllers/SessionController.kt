package lam.com.locationmapservice.demo.controllers

import android.content.Context
import android.util.Log
import com.lam.locationmapservicelib.controllers.location.LocationController
import com.lam.locationmapservicelib.enums.LocationUpdateType
import com.lam.locationmapservicelib.models.Annotation
import com.lam.locationmapservicelib.models.LocationUpdate
import io.reactivex.schedulers.Schedulers
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import lam.com.locationmapservice.demo.activities.DemoActivity
import lam.com.locationmapservice.demo.api.ApiService
import lam.com.locationmapservice.demo.api.interfaces.IDummyApi
import lam.com.locationmapservice.demo.models.AnnotationMeta
import retrofit2.Response

object SessionController {
    var user: Annotation? = null
    var meta: AnnotationMeta? = null
    var isLoaded: Boolean = false
        get() {
            return user != null && meta != null
        }

    private var positionLoopDisposable: Disposable? = null
    private var positionTimestamp = 0L
    /**  Update interval in milliseconds */
    private const val UPDATE_INTERVAL = 10000L
    /**  Minimum distance for update in meters */
    private const val UPDATE_DISTANCE = 10f

    fun loadUserAndMeta(annotationId: Long): Observable<Response<out Any>> {
        return Observable.concat(loadUser(annotationId), loadMeta(annotationId))
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
    }

    fun enableLocation(context: Context, enable: Boolean) {
        // Update location looper
        if (user?.position?.enabled != enable) {
            user?.position?.enabled = enable

            if (enable) {
                startPositionLoop(context)
            } else {
                positionLoopDisposable?.dispose()
            }
        }

        // Patch location with enable state
        val location = LocationController.getCachedLocation(context)

        user?.annotation_id?.let { userId ->
            ApiService.createService(IDummyApi::class.java)
                    .updatePosition(location?.latitude?.toFloat() ?: 0f, location?.longitude?.toFloat() ?: 0f, enable, userId)
                    .observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ response ->
                        if (response.isSuccessful) {
                            Log.d("session", "Enable location patched: $enable, (${location?.latitude}, ${location?.longitude}) -> ${response.body()}")
                        } else {
                            Log.d("session", "Enable location patch error: ${response.code()} - ${response.errorBody()}")
                        }
                    }, { error ->
                        Log.d("session", "Enable location patch error $error")
                    })
        }
    }

    fun startPositionLoop(context: Context) {
        if (positionLoopDisposable == null || positionLoopDisposable?.isDisposed == true) {
            positionLoopDisposable = LocationController.getUpdates(context, UPDATE_INTERVAL, UPDATE_DISTANCE)
                    ?.compose((context as? DemoActivity)?.bindToLifecycle())
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(Schedulers.io())
                    ?.subscribe({ locationUpdate ->
                        if (user?.position?.enabled == true) {
                            val timestamp = System.currentTimeMillis()
                            if (positionTimestamp + UPDATE_INTERVAL < timestamp) {
                                positionTimestamp = timestamp

                                if (locationUpdate.type == LocationUpdateType.Location) {
                                    val location = locationUpdate.data.getParcelable<android.location.Location>(LocationUpdate.LOCATION_BUNDLE_KEY)

                                    user?.annotation_id?.let { userId ->
                                        ApiService.createService(IDummyApi::class.java)
                                                .updatePosition(location.latitude.toFloat(), location.longitude.toFloat(), user?.position?.enabled == true, userId)
                                                .observeOn(Schedulers.io())
                                                .subscribeOn(Schedulers.io())
                                                .subscribe({ response ->
                                                    if (response.isSuccessful) {
                                                        Log.d("session", "location update patched: (${location.latitude}, ${location.longitude}) -> ${response.body()}")
                                                    } else {
                                                        Log.d("session", "location update patch error: ${response.code()} - ${response.errorBody()}")
                                                    }
                                                }, { error ->
                                                    Log.d("session", "location update patch error $error")
                                                })
                                    }
                                }
                            }
                        }
                    }, { error ->
                        Log.d("session", "location update error $error")
                    })
        }
    }

    private fun loadUser(annotationId: Long): Observable<Response<Annotation>> {
        return ApiService.createService(IDummyApi::class.java)
                .getAnnotation(annotationId)
                .observeOn(Schedulers.io())
                .doOnNext { response ->
                    if (response.isSuccessful) {
                        user = response.body()
                    }
                }
    }

    private fun loadMeta(annotationId: Long): Observable<Response<AnnotationMeta>> {
        return ApiService.createService(IDummyApi::class.java)
                .getAnnotationMeta(annotationId)
                .observeOn(Schedulers.io())
                .doOnNext { response ->
                    if (response.isSuccessful) {
                        meta = response.body()
                    }
                }
    }
}