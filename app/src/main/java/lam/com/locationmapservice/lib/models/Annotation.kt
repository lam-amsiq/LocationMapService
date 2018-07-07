package lam.com.locationmapservice.lib.models

import java.io.Serializable

class Annotation(
        val id: Int,
        var title: String?,
        var annotationId: String? = null
): Serializable