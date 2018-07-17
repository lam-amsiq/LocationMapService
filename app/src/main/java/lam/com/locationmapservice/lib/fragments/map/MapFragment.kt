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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.fragment_map.*
import lam.com.locationmapservice.BuildConfig
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.fragments.LMSFragment
import org.androidannotations.annotations.EFragment
import lam.com.locationmapservice.lib.fragments.map.controllers.MapController
import lam.com.locationmapservice.lib.models.Annotation
import lam.com.locationmapservice.lib.utils.HeatmapMaths
import java.util.*

@EFragment(R.layout.fragment_map)
open class MapFragment : LMSFragment() {
    var isMapSetup: Boolean? = null

    private var annotationSize: Int = 0

    private val scaleTransformation: Transformation = object : Transformation {
        override fun transform(source: Bitmap?): Bitmap {
            val isTall = source?.height ?: 1 > source?.width ?: 1

            val aspectRatio = (if (isTall) source?.height?.div(source.width) else source?.width?.div(source.height))
                    ?: 1
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

    fun setAnnotations(jsonString: String?) {
        jsonString?.let { jsonStr ->
            setAnnotations(Annotation.jsonStringToAnnotationList(jsonStr))
        }
    }

    fun setAnnotations(annotationList: LinkedList<Annotation>) {
        MapController.getRealm()?.let { realm ->
            // Remove old annotations from realm
            realm.beginTransaction()
            Log.d("realm", "Delete: isInTransaction = ${realm.isInTransaction}")
            realm.delete(Annotation::class.java)
            realm.commitTransaction()

            // Compute heatmaps
            val computedAnnotations = HeatmapMaths.computeHashmaps(annotationList, 0.02, true)

            // Add heatmaps
            val heatmapLocations = ArrayList<LatLng>()
            computedAnnotations.first.forEach { heatmap ->
                heatmapLocations.clear()
                heatmap.forEach { annotation ->
                    annotation.position?.toLatLng()?.let { latLng ->  heatmapLocations.add(latLng) }
                }
                MapController.addHeatmap(heatmapLocations)
            }

            // Add annotations
            realm.beginTransaction()
            computedAnnotations.second.forEach { annotation ->
                addAnnotation(annotation)?.let { marker ->
                    Log.d("map", "Annotation ${annotation.annotation_id} added=${marker.id}")
                    annotation.marker_id = marker.id
                    annotation.store(realm)

                    setAnnotationImage(marker, annotation.image)
                } ?: kotlin.run {
                    Log.d("map", "Annotation ${annotation.annotation_id} added=FAILED")
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
        getAnnotationImage(Picasso.get(), imageUrl, annotationSize)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { bitmap ->
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

    private fun getAnnotationImage(picasso: Picasso, imageApiExtension: String?, size: Int): Observable<Bitmap> = Observable.create { emitter ->
        try {
            if (!emitter.isDisposed) {
                // Get placeholder/default image
                picasso.load(R.drawable.as_shared_default_picture_female_round)
                        .resize(size, size)
                        .noFade()
                        .get()?.let { placeholder ->
                            emitter.onNext(placeholder)
                        }

                // Get profile picture
                imageApiExtension?.let { url ->
                    try {
                        picasso.load(BuildConfig.BASEURLAPI + url)
                                .transform(arrayListOf(CropCircleTransformation(), scaleTransformation))
                                .noFade()
                                .get()?.let { profileImage ->
                                    emitter.onNext(profileImage)
                                }
                    } catch (e: Exception) {
                        Log.d("Picasso", "Failed to load profile picture ${BuildConfig.BASEURLAPI + url}: $e")
                    }
                }
                emitter.onComplete()
            }
        } catch (e: Exception) {
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