package lam.com.locationmapservice.demo.activities

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.maps.model.LatLngBounds
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_start_up.*
import lam.com.locationmapservice.R
import lam.com.locationmapservice.demo.api.ApiService
import lam.com.locationmapservice.demo.api.interfaces.IDummyApi
import lam.com.locationmapservice.demo.dummyData.UserDummy
import lam.com.locationmapservice.demo.fragments.annotation.AnnotationFragment_
import com.lam.locationmapservicelib.fragments.map.MapFragment
import com.lam.locationmapservicelib.models.Annotation
import com.lam.locationmapservicelib.views.dialog.Dialog
import lam.com.locationmapservice.BuildConfig
import org.androidannotations.annotations.EActivity
import org.androidannotations.annotations.InstanceState
import org.androidannotations.annotations.Receiver
import java.net.UnknownHostException
import java.util.*

@SuppressLint("Registered")
@EActivity(R.layout.activity_start_up)
open class StartUpActivity : DemoActivity() {
    @JvmField
    @InstanceState
    var noInternetShown = false

    private var mapFragment: MapFragment? = null
    private var list: LinkedList<Annotation>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Build map
        mapFragment = MapFragment()
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
                                ?.subscribe({ annotationMarkerPair ->
                                    val annotationFragment = AnnotationFragment_.builder().build()
                                    annotationFragment.setup(annotationMarkerPair.first, annotationMarkerPair.second)

                                    beginTransactionTo(annotationFragment)
                                }, {
                                    Log.e("startup", "getAnnotationObserver error: $it")
                                })

                        mapFragment?.setOnCameraListener {
                            // Add dummy annotations
                            fetchAndSetAnnotations(mapFragment?.getViewportBounds())
                        }
                    }, {
                        Log.e("startup", "Map setup error: $it")
                    })
        }
    }

    @Receiver(actions = [(ConnectivityManager.CONNECTIVITY_ACTION)], registerAt = Receiver.RegisterAt.OnResumeOnPause)
    internal fun onNetworkChange() {
        val hasInternet = isNetworkAvailable()
        noInternetNotification?.height?.toFloat()?.let { from -> getNoInternetAnimation(noInternetNotification, from, 0f, hasInternet)?.start() }
        if (hasInternet) {
            noInternetShown = false
            fetchAndSetAnnotations(mapFragment?.getViewportBounds())
        }
    }

    private fun fetchAndSetAnnotations(mapViewportBounds: LatLngBounds?): Disposable? {
        return ApiService.createService(IDummyApi::class.java)
                .getDummyAnnotations(mapViewportBounds?.southwest?.latitude?.toFloat(), mapViewportBounds?.northeast?.latitude?.toFloat(), mapViewportBounds?.southwest?.longitude?.toFloat(), mapViewportBounds?.northeast?.longitude?.toFloat(), UserDummy.IS_MALE)
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ response ->
                    Log.d("retrofit", "Get annotation success: $response")
                    if (response.isSuccessful) {
                        this.list = LinkedList(response.body())
                        this.list?.let { list -> setAnnotations(list) }
                    } else {
                        Toast.makeText(this, resources?.getString(R.string.shared_notification_error_server), Toast.LENGTH_LONG).show()
                    }
                }, { error ->
                    Log.e("startup", "getDummyAnnotations error: $error")
                    if (error is UnknownHostException) {
                        if (!noInternetShown) {
                            noInternetShown = true
                            Dialog.showDialogNoInternet(this)
                        }
                        if (this.list?.isNotEmpty() == true) {
                            setAnnotations(this.list!!)
                        }
                    }
                })
    }

    private fun setAnnotations(list: LinkedList<Annotation>) {
        mapFragment?.setAnnotations(list,
                true,
                BuildConfig.BASEURLAPI,
                if (UserDummy.IS_MALE) R.drawable.as_shared_default_picture_male_round else R.drawable.as_shared_default_picture_female_round,
                R.drawable.as_shared_default_picture_offline_round)
    }

    private fun getNoInternetAnimation(view: View, from: Float, to: Float, out: Boolean = false): ObjectAnimator? {
        val frameAnimation = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, if (out) to else from, if (out) from else to)
        frameAnimation.duration = NO_INTERNET_DURATION
        frameAnimation.interpolator = FastOutSlowInInterpolator()
        frameAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                if (!out) {
                    view.visibility = View.VISIBLE
                }
            }

            override fun onAnimationEnd(animation: Animator) {
                if (out) {
                    view.visibility = View.GONE
                }
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        return frameAnimation
    }

    companion object {
        private const val NO_INTERNET_DURATION = 300L
    }
}