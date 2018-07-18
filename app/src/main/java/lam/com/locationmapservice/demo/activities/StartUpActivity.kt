package lam.com.locationmapservice.demo.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lam.com.locationmapservice.R
import lam.com.locationmapservice.demo.api.ApiService
import lam.com.locationmapservice.demo.api.interfaces.IDummyApi
import lam.com.locationmapservice.demo.fragments.annotation.AnnotationFragment_
import lam.com.locationmapservice.lib.fragments.map.MapFragment
import lam.com.locationmapservice.lib.fragments.map.MapFragment_
import lam.com.locationmapservice.lib.views.dialog.Dialog
import org.androidannotations.annotations.EActivity
import java.net.UnknownHostException
import java.util.*

@SuppressLint("Registered")
@EActivity(R.layout.activity_start_up)
open class StartUpActivity : DemoActivity() {
    private var mapFragment: MapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Build map
        mapFragment = MapFragment_.builder().build()
        clearStack()
        beginTransactionTo(mapFragment)
    }

    override fun onResume() {
        super.onResume()
        // Setup map
        if (mapFragment?.isMapSetup != true) {
            mapFragment?.setup(this)
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe({ _ ->
                        // Listen for click events on map annotations
                        mapFragment?.getAnnotationObserver()
                                ?.observeOn(Schedulers.io())
                                ?.subscribe ({ annotationMarkerPair ->
                                    val annotationFragment = AnnotationFragment_.builder().build()
                                    annotationFragment.setup(annotationMarkerPair.first, annotationMarkerPair.second)

                                    beginTransactionTo(annotationFragment)
                                }, {
                                    Log.e("startup", "getAnnotationObserver error: $it")
                                })

                        mapFragment?.setOnCameraListener {
                            // Get map viewport
                            val mapViewportBounds = mapFragment?.getViewportBounds()

                            // Add dummy annotations
                            ApiService.createService(IDummyApi::class.java)
                                    .getDummyAnnotations(mapViewportBounds?.southwest?.latitude?.toFloat(), mapViewportBounds?.northeast?.latitude?.toFloat(), mapViewportBounds?.southwest?.longitude?.toFloat(), mapViewportBounds?.northeast?.longitude?.toFloat())
                                    .compose(bindToLifecycle())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe ({ response ->
                                        Log.d("retrofit", "Get annotation success: $response")
                                        mapFragment?.setAnnotations(LinkedList(response), true)
                                    }, { error ->
                                        Log.e("startup", "getDummyAnnotations error: $error")
                                        if (error is UnknownHostException) {
                                            Dialog.showDialogNoInternet(this)
                                        }
                                    })
                        }
                    }, {
                        Log.e("startup", "Map setup error: $it")
                    })
        }
    }
}
