package lam.com.locationmapservice.lib.fragments.map

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_map.*
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.fragments.LMSFragment
import org.androidannotations.annotations.EFragment
import lam.com.locationmapservice.lib.fragments.map.controllers.MapController
import lam.com.locationmapservice.lib.models.Annotation

@EFragment(R.layout.fragment_map)
open class MapFragment : LMSFragment() {
    private val annotationMap: HashMap<String, Annotation?> = HashMap()
    var isMapSetup: Boolean? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        MapController.setupMapView(rootView.findViewById(R.id.mapView), savedInstanceState)

        return rootView
    }

    fun setup(context: Context): Single<GoogleMap>? {
        return MapController.setupMap(context)
                .doOnSuccess { isMapSetup = true }
                .doOnError { isMapSetup = false }
    }

    fun getAnnotationObserver(): Observable<Marker>? {
        return MapController.getMarkerObserver()
    }

    fun addAnnotation(position: LatLng?, info: Annotation?, icon: Int) {
        position?.let { positionInner ->
            getBitmapSingle(Picasso.get(), icon, 100)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ bitmap ->
                        val marker = MapController.addAnnotation(positionInner, BitmapDescriptorFactory.fromBitmap(bitmap))
                        marker?.id?.let { markerId ->
                            info?.annotationId = markerId
                            annotationMap[markerId] = info
                        }
                    }, Throwable::printStackTrace)
        }
    }

    fun getAnnotationFromMarker(markerId: String): Annotation? {
        return annotationMap[markerId]
    }

    fun showAnnotation(view: View, anim: AnimatorSet) {
        if (annotationPlaceholder?.childCount == 0) {
            annotationPlaceholder?.addView(view)
        }

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                annotationPlaceholder?.visibility = View.VISIBLE
            }
        })
        anim.start()
    }

    fun hideAnnotation(anim: AnimatorSet) {
        annotationPlaceholder?.removeAllViews()
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationStart(animation)
                annotationPlaceholder?.visibility = View.GONE
            }
        })
        anim.start()
    }

    private fun getBitmapSingle(picasso: Picasso, res: Int, size: Int): Single<Bitmap> = Single.create { emitter ->
        try {
            if (!emitter.isDisposed) {
                val bitmap: Bitmap = picasso.load(res).resize(size, size).get()
                emitter.onSuccess(bitmap)
            }
        } catch (e: Throwable) {
            emitter.onError(e)
        }
    }

    override fun onResume() {
        super.onResume()
        MapController.onResume()
    }

    override fun onPause() {
        super.onPause()
        MapController.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        MapController.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        MapController.onLowMemory()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        MapController.onRequestPermissionsResult(requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        MapController.onActivityResult(requestCode)
    }
}