package lam.com.locationmapservice.demo.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lam.com.locationmapservice.R
import lam.com.locationmapservice.demo.fragments.annotation.AnnotationFragment_
import lam.com.locationmapservice.lib.fragments.map.MapFragment
import lam.com.locationmapservice.lib.fragments.map.MapFragment_
import lam.com.locationmapservice.lib.models.Annotation
import org.androidannotations.annotations.EActivity

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
            mapFragment?.setup(applicationContext)
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe({ _ ->
                        // Listen for click events on map annotations
                        mapFragment?.getAnnotationObserver()
                                ?.observeOn(Schedulers.io())
                                ?.subscribe { marker ->
                                    beginTransactionTo(AnnotationFragment_.builder()
                                            .annotationModel(mapFragment?.getAnnotationFromMarker(marker.id))
                                            .build())
                                }

                        // Add some annotation
                        mapFragment?.addAnnotation(LatLng(55.7250, 12.4375), Annotation(1, "Amelia"), R.drawable.as_shared_default_picture_female_round)
                        mapFragment?.addAnnotation(LatLng(55.7350, 12.4370), Annotation(2, "Bellatrix"), R.drawable.as_shared_default_picture_female_round)
                        mapFragment?.addAnnotation(LatLng(55.7400, 12.4400), Annotation(3, "Cho"), R.drawable.as_shared_default_picture_female_round)
                        mapFragment?.addAnnotation(LatLng(55.7200, 12.4250), Annotation(4, "Dolora"), R.drawable.as_shared_default_picture_female_round)
                        mapFragment?.addAnnotation(LatLng(55.7500, 12.4350), Annotation(5, "Eliana"), R.drawable.as_shared_default_picture_female_round)
                    }, {
                        Log.d("startup", "Map setup error: $it")
                    })
        }
    }
}
