package lam.com.locationmapservice.demo.fragments.annotation

import android.animation.Animator
import android.animation.ObjectAnimator
import android.location.Address
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.Log
import android.util.Property
import android.view.View
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.fragment_annotation.*
import lam.com.locationmapservice.BuildConfig
import lam.com.locationmapservice.R
import lam.com.locationmapservice.demo.activities.DemoActivity
import lam.com.locationmapservice.demo.api.ApiService
import lam.com.locationmapservice.demo.api.interfaces.IDummyApi
import lam.com.locationmapservice.demo.fragments.DemoFullscreenFragment
import com.lam.locationmapservicelib.models.Annotation
import com.lam.locationmapservicelib.utils.extensions.fitCenter
import org.androidannotations.annotations.AfterViews
import org.androidannotations.annotations.EFragment
import android.location.Geocoder
import java.util.*

@EFragment(R.layout.fragment_annotation)
open class AnnotationFragment : DemoFullscreenFragment() {
    private var annotation: Annotation? = null
    private var marker: Marker? = null

    @AfterViews
    internal fun after() {
        nameView?.text = annotation?.name
        setMeta()

        (context as? DemoActivity)?.goToFullScreen(true)
        annotationFrame?.post {
            getFrameAnimation(annotationFrame, annotationFrame.measuredHeight.toFloat(), 0f)
                    ?.start()
        }
    }

    fun setup(annotation: Annotation, marker: Marker) {
        this.annotation = annotation
        this.marker = marker
    }

    private fun getFrameAnimation(frameView: View, from: Float, to: Float, out: Boolean = false): ObjectAnimator? {
        val frameAnimation = ObjectAnimator.ofFloat(frameView, AnimateInType, if (out) to else from, if (out) from else to)
        frameAnimation.duration = AnimateInDuration
        frameAnimation.interpolator = FastOutSlowInInterpolator()
        frameAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                if (!out) {
                    frameView.visibility = View.VISIBLE
                }
            }

            override fun onAnimationEnd(animation: Animator) {
                if (out) {
                    frameView.visibility = View.GONE
                }
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        return frameAnimation
    }

    private fun setMeta() {
        annotation?.annotation_id?.let { annotationId ->
            ApiService.createService(IDummyApi::class.java)
                    .getDummyAnnotationMeta(annotationId)
                    .compose(bindToLifecycle())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ response ->
                        Log.d("annotation", "Get annotation meta success: $response")
                        if (response.isSuccessful) {
                            response.body()?.let { meta ->
                                summaryView.text = "${meta.age}${getCityName()?.let { address -> " \u2022 ${address.locality}" }}"
                                meta.defaultPortrait()?.let { defaultPortrait ->
                                    setPortrait(defaultPortrait)
                                }
                            }
                        } else {
                            Picasso.get()
                                    .load(R.drawable.as_shared_default_picture_offline_round)
                                    .fitCenter()
                                    .into(portraitView)
                        }
                    }, { error ->
                        Log.e("annotation", "getDummyMeta error: $error")
                        Picasso.get()
                                .load(R.drawable.as_shared_default_picture_offline_round)
                                .fitCenter()
                                .into(portraitView)
                    })
        }
    }

    private fun getCityName(): Address? {
        val addresses = annotation?.position?.lat?.let { lat ->
            annotation?.position?.lng?.let { lng ->
                Geocoder(context, Locale.getDefault()).getFromLocation(lat, lng, 1)
            }
        }
        return addresses?.firstOrNull()
    }

    private fun setPortrait(default: Int) {
        annotation?.portrait?.let { portraitUrl ->
            Picasso.get()
                    .load(BuildConfig.BASEURLAPI + portraitUrl)
                    .error(R.drawable.as_shared_default_picture_offline_round)
                    .fitCenter()
                    .transform(CropCircleTransformation())
                    .into(portraitView)
        } ?: kotlin.run {
            Picasso.get()
                    .load(default)
                    .fitCenter()
                    .into(portraitView)
        }
    }

    companion object {
        private val AnimateInType: Property<View, Float> = View.TRANSLATION_Y
        private const val AnimateInDuration: Long = 600L
    }
}