package com.lam.locationmapservicelib.fragments.map

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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.fragment_map.*
import com.lam.locationmapservicelib.R
import com.lam.locationmapservicelib.fragments.LMSFragment
import com.lam.locationmapservicelib.fragments.map.controllers.MapController
import com.lam.locationmapservicelib.models.Annotation
import com.lam.locationmapservicelib.utils.HeatmapMaths
import com.lam.locationmapservicelib.utils.LMSLog
import java.util.*

open class MapFragment : LMSFragment() {
    var isMapSetup: Boolean? = null

    private var annotationSize: Int = 0

    private val scaleTransformation: Transformation = object : Transformation {
        override fun transform(source: Bitmap?): Bitmap {
            val isTall = source?.height ?: 1 > source?.width ?: 1

            val aspectRatio = (if (isTall) source?.height?.div(source.width) else source?.width?.div(source.height)) ?: 1
            val targetHeight = if (isTall) annotationSize else annotationSize * aspectRatio
            val targetWidth = if (isTall) annotationSize * aspectRatio else annotationSize

            val result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
            if (result != source) {
                source?.recycle()
            }
            return result
        }

        override fun key(): String {
            return "cropPosterTransformation$annotationSize"
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        MapController.realmInit(context)
        MapController.setupMapView(rootView.findViewById(R.id.mapView), savedInstanceState)

        return rootView
    }

    fun setup(context: Context, annotationSize: Int = 100): Single<GoogleMap>? {
        this.annotationSize = annotationSize
        return MapController.setupMap(context)
                .doOnSuccess { isMapSetup = true }
                .doOnError { isMapSetup = false }
    }

    fun getAnnotationObserver(): Observable<Pair<Annotation, Marker>>? {
        return MapController.getMarkerObserver()
    }

    fun setAnnotations(jsonString: String?, isSortedByLatitudeAsc: Boolean, baseApiUrl: String?, defaultImage: Int, errorImage: Int) {
        jsonString?.let { jsonStr ->
            setAnnotations(Annotation.jsonStringToAnnotationList(jsonStr), isSortedByLatitudeAsc, baseApiUrl, defaultImage, errorImage)
        }
    }

    fun setAnnotations(annotationList: LinkedList<Annotation>, isSortedByLatitudeAsc: Boolean, baseApiUrl: String?, defaultImage: Int, errorImage: Int) {
        MapController.getRealm()?.let { realm ->
            // Remove old annotations from realm and map
            MapController.clearMap()
            realm.beginTransaction()
            realm.delete(Annotation::class.java)
            realm.commitTransaction()

            // Compute heatmaps
            val groupDistance = MapController.getGroupDistance(annotationSize)
            val computedAnnotations = HeatmapMaths.computeHashmaps(annotationList, groupDistance, isSortedByLatitudeAsc)

            // Add heatmaps
            val heatmapLocations = ArrayList<LatLng>()
            computedAnnotations.first.forEach { heatmap ->
                heatmapLocations.clear()
                heatmap.forEach { annotation ->
                    annotation.position?.toLatLng()?.let { latLng -> heatmapLocations.add(latLng) }
                }
                MapController.addHeatmap(heatmapLocations)
            }

            // Add annotations
            realm.beginTransaction()
            computedAnnotations.second.forEach { annotation ->
                addAnnotation(annotation)?.let { marker ->
                    annotation.marker_id = marker.id
                    annotation.store(realm)

                    setAnnotationImage(marker, baseApiUrl, annotation.thumb, defaultImage, errorImage)
                } ?: kotlin.run {
                    LMSLog.w(message="Failed to add annotation ${annotation.annotation_id}")
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

    private fun setAnnotationImage(marker: Marker, baseApiUrl: String?, imageApiExtension: String?, defaultImage: Int, errorImage: Int) {
        getAnnotationImage(Picasso.get(), baseApiUrl, imageApiExtension, defaultImage, errorImage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { bitmap ->
                    try {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        marker.isVisible = true
                    } catch (e: IllegalArgumentException) { }
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

    private fun getAnnotationImage(picasso: Picasso, baseApiUrl: String?, imageApiExtension: String?, defaultImage: Int, errorImage: Int): Observable<Bitmap> = Observable.create { emitter ->
        if (!emitter.isDisposed) {
            try {
                // Get profile picture
                imageApiExtension?.let { url ->
                    try {
                        picasso.load(baseApiUrl + url)
                                .transform(arrayListOf(CropCircleTransformation(), scaleTransformation))
                                .noFade()
                                .get()?.let { profileImage ->
                                    emitter.onNext(profileImage)
                                }
                    } catch (e: Exception) {
                        LMSLog.d(message="Picasso failed to load annotation image ${baseApiUrl + url}: $e")
                        picasso.load(errorImage)
                                .resize(annotationSize, annotationSize)
                                .noFade()
                                .get()?.let { placeholder ->
                                    emitter.onNext(placeholder)
                                }
                    }
                } ?: kotlin.run {
                    // Get placeholder/default portrait
                    picasso.load(defaultImage)
                            .resize(annotationSize, annotationSize)
                            .transform(CropCircleTransformation())
                            .noFade()
                            .get()?.let { placeholder ->
                                emitter.onNext(placeholder)
                            }
                }
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    fun getViewportBounds(): LatLngBounds? {
        return MapController.getViewportBounds()
    }

    inline fun <Unit> setOnCameraListener(crossinline body: () -> Unit) {
        MapController.setOnCameraListener { body() }
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
        MapController.onRequestPermissionsResult(context, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        MapController.onActivityResult(context, requestCode)
    }
}