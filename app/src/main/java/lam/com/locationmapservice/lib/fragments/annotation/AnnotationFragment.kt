package lam.com.locationmapservice.lib.fragments.annotation

import kotlinx.android.synthetic.main.fragment_annotation.*
import lam.com.locationmapservice.R
import lam.com.locationmapservice.lib.fragments.LMSFragment
import lam.com.locationmapservice.lib.models.Annotation
import org.androidannotations.annotations.AfterViews
import org.androidannotations.annotations.EFragment
import org.androidannotations.annotations.FragmentArg

@EFragment(R.layout.fragment_annotation)
open class AnnotationFragment : LMSFragment() {
    @JvmField
    @FragmentArg
    internal var annotationModel: Annotation? = null

    @AfterViews
    internal fun after() {
        idTV.text = annotationModel?.let { annotation ->
            "${annotationModel?.title} (${annotation.id})\n" +
                    "Annotationid=${annotation.annotationId}"
        } ?: "ID NOT SET"
    }
}