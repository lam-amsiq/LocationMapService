package lam.com.locationmapservice.lib.models

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

open class Annotation(
        @PrimaryKey
        var annotation_id: Long = 0,
        var title: String? = null,
        var image: String? = null,
        var position: Location? = null,
        var marker_id: String? = null) : RealmObject() {

    fun store(realm: Realm, annotation: Annotation) {
        val realmPosition = realm.createObject(Location::class.java)
        realmPosition.position_latitude = annotation.position?.position_latitude
        realmPosition.position_longitude = annotation.position?.position_longitude

        val realmAnnotation = realm.createObject(Annotation::class.java, annotation.annotation_id)
        realmAnnotation.position = realmPosition

        realmAnnotation.image = annotation.image
        realmAnnotation.marker_id = annotation.marker_id
        realmAnnotation.title = annotation.title
    }

    companion object {
        const val ANNOTATION_ID_JSON_TAG = "annotation_id"
        const val TITLE_JSON_TAG = "title"
        const val IMAGE_JSON_TAG = "image"
        const val POSITION_JSON_TAG = "position"
        const val POSITION_LAT_JSON_TAG = "lat"
        const val POSITION_LNG_JSON_TAG = "lng"
        const val MARKER_ID_JSON_TAG = "marker_id"

        fun jsonStringToAnnotationArray(jsonString: String): Array<Annotation?> {
            val jsonArray = JSONArray(jsonString)
            val annotationArray = arrayOfNulls<Annotation>(jsonArray.length())

            var jsonAnnotation: JSONObject
            var jsonPosition: JSONObject
            for (i in 0 until jsonArray.length()) {
                jsonAnnotation = jsonArray.getJSONObject(i)
                jsonPosition = jsonAnnotation.optJSONObject(Annotation.POSITION_JSON_TAG)

                annotationArray[i] = Annotation(
                        jsonAnnotation.getLong(Annotation.ANNOTATION_ID_JSON_TAG),
                        jsonAnnotation.optString(Annotation.TITLE_JSON_TAG, null),
                        jsonAnnotation.optString(Annotation.IMAGE_JSON_TAG, null),
                        jsonPosition?.let { position ->
                            Location(position.getDouble(Annotation.POSITION_LAT_JSON_TAG),
                                    position.getDouble(Annotation.POSITION_LNG_JSON_TAG))
                        },
                        jsonAnnotation.optString(Annotation.MARKER_ID_JSON_TAG, null))
            }
            return annotationArray
        }
    }
}