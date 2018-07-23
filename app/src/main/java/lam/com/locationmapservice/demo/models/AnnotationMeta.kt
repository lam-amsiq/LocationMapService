package lam.com.locationmapservice.demo.models

import lam.com.locationmapservice.R

class AnnotationMeta(val annotation_id: Long,
                     val age: Int,
                     val gender: String) {

    fun defaultPortrait() =
        when (gender) {
            "male" -> R.drawable.as_shared_default_picture_male_round
            "female" -> R.drawable.as_shared_default_picture_female_round
            else -> null
        }

    override fun toString(): String {
        return "Meta($annotation_id: age=$age, gender='$gender')"
    }
}