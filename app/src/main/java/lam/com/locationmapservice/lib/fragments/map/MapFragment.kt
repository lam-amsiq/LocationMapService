package lam.com.locationmapservice.lib.fragments.map

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.activities.LMSActivity
import lam.com.locationmapservice.lib.fragments.LMSFragment
import lam.com.locationmapservice.lib.fragments.annotation.AnnotationFragment_
import org.androidannotations.annotations.EFragment
import lam.com.locationmapservice.lib.fragments.map.controllers.MapController
import lam.com.locationmapservice.lib.models.Annotation

@EFragment(R.layout.fragment_map)
open class MapFragment : LMSFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        context?.let { contextInner ->
            MapController.setupMaps(contextInner, this, rootView.findViewById(R.id.mapView), savedInstanceState)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        MapController.setAnnotationListener(GoogleMap.OnMarkerClickListener {
                            // TODO: Add annotation click event
                            (contextInner as? LMSActivity)?.beginTransactionTo(AnnotationFragment_.builder()
                                    .annotationModel(annotationMap[it.id])
                                    .build())
                            true
                        })

                        addAnnotation(LatLng(55.7250, 12.4375), Annotation(1, "Amelia"), R.drawable.as_shared_default_picture_female_round)
                        addAnnotation(LatLng(55.7350, 12.4370), Annotation(2, "Bellatrix"), R.drawable.as_shared_default_picture_female_round)
                        addAnnotation(LatLng(55.7400, 12.4400), Annotation(3, "Cho"), R.drawable.as_shared_default_picture_female_round)
                        addAnnotation(LatLng(55.7200, 12.4250), Annotation(4, "Dolora"), R.drawable.as_shared_default_picture_female_round)
                        addAnnotation(LatLng(55.7500, 12.4350), Annotation(5, "Eliana"), R.drawable.as_shared_default_picture_female_round)
                    }
        }
        return rootView
    }

    private val annotationMap: HashMap<String, Annotation?> = HashMap()

    private fun addAnnotation(position: LatLng?, info: Annotation?, icon: Int) {
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