package lam.com.locationmapservice.lib.fragments.map

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
import lam.com.locationmapservice.lib.utils.extensions.load

@EFragment(R.layout.fragment_map)
open class MapFragment : LMSFragment() {
    var isMapSetup: Boolean? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        MapController.realmInit(context)
        MapController.setupMapView(rootView.findViewById(R.id.mapView), savedInstanceState)

        return rootView
    }

    fun setup(context: Context): Single<GoogleMap>? {
        return MapController.setupMap(context)
                .doOnSuccess { isMapSetup = true }
                .doOnError { isMapSetup = false }
    }

    fun getAnnotationObserver(): Observable<Pair<Annotation, Marker>>? {
        return MapController.getMarkerObserver()
    }

    fun setAnnotations(jsonString: String?) {
        jsonString?.let { jsonStr ->
            setAnnotations(Annotation.jsonStringToAnnotationArray(jsonStr))
        }
    }

    fun setAnnotations(annotationArray: Array<Annotation?>) {
        MapController.getRealm()?.let { realm ->
            realm.beginTransaction()
            Log.d("realm", "Delete: isInTransaction = ${realm.isInTransaction}")
            realm.delete(Annotation::class.java)
            realm.commitTransaction()

            // Add annotations
            realm.beginTransaction()
            annotationArray.forEach { annotation ->
                addAnnotation(annotation)?.let { marker ->
                    Log.d("map", "Annotation ${annotation?.annotation_id} added=${marker.id}")
                    annotation?.marker_id = marker.id
                    annotation?.store(realm)

                    setAnnotationImage(marker, annotation?.image)
                } ?: kotlin.run {
                    Log.d("map", "Annotation ${annotation?.annotation_id} added=FAILED")
                }
            }
            realm.commitTransaction()
            realm.close()
        }
    }

    private fun addAnnotation(annotation: Annotation?): Marker? {
        return annotation?.position?.let { position ->
            MapController.addAnnotation(position)
        }
    }

    private fun setAnnotationImage(marker: Marker, imageUrl: String?) {
        getBitmapSingle(Picasso.get(),
                imageUrl?.let { it }
                        ?: kotlin.run { R.drawable.as_shared_default_picture_female_round }, 100)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{ bitmap ->
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))
                }
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

    private fun getBitmapSingle(picasso: Picasso, image: Any, size: Int): Single<Bitmap> = Single.create { emitter ->
        try {
            if (!emitter.isDisposed) {
                val bitmap: Bitmap? = picasso.load(image)?.resize(size, size)?.get()
                bitmap?.let {
                    emitter.onSuccess(it)
                } ?: kotlin.run {
                    emitter.onError(NullPointerException("Failed to get image"))
                }
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
        MapController.onDestroy()
        super.onDestroy()
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