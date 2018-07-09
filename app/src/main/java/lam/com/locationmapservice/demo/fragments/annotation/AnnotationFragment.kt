package lam.com.locationmapservice.demo.fragments.annotation

import android.animation.Animator
import android.animation.ObjectAnimator
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.Property
import android.view.View
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.fragment_annotation.*
import lam.com.locationmapservice.R
import lam.com.locationmapservice.demo.activities.DemoActivity
import lam.com.locationmapservice.demo.fragments.DemoFullscreenFragment
import lam.com.locationmapservice.lib.models.Annotation
import org.androidannotations.annotations.AfterViews
import org.androidannotations.annotations.EFragment

@EFragment(R.layout.fragment_annotation)
open class AnnotationFragment : DemoFullscreenFragment() {
    private var annotation: Annotation? = null
    private var marker: Marker? = null

    @AfterViews
    internal fun after() {
        idTV?.text = "${annotation?.title} (${annotation?.annotation_id})\n" +
                "Annotationid=${annotation?.marker_id}" ?: "ANNOTATION NP"

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

    companion object {
        private val AnimateInType: Property<View, Float> = View.TRANSLATION_Y
        private const val AnimateInDuration: Long = 600L
    }
}