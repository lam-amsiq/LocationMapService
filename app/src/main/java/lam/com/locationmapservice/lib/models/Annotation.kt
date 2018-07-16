package lam.com.locationmapservice.lib.models

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

open class Annotation(
        @PrimaryKey
        var annotation_id: Long = 0,
        var title: String? = null,
        var image: String? = null,
        var position: Location? = null,
        var marker_id: String? = null) : RealmObject() {

    fun store(realm: Realm) {
        val realmPosition = realm.createObject(Location::class.java)
        realmPosition.lat = this.position?.lat
        realmPosition.lng = this.position?.lng

        val realmAnnotation = realm.createObject(Annotation::class.java, this.annotation_id)
        realmAnnotation.position = realmPosition

        realmAnnotation.image = this.image
        realmAnnotation.marker_id = this.marker_id
        realmAnnotation.title = this.title
    }

    companion object {
        const val ANNOTATION_ID_JSON_TAG = "annotation_id"
        const val TITLE_JSON_TAG = "title"
        const val IMAGE_JSON_TAG = "image"
        const val POSITION_JSON_TAG = "position"
        const val POSITION_LAT_JSON_TAG = "lat"
        const val POSITION_LNG_JSON_TAG = "lng"
        const val MARKER_ID_JSON_TAG = "marker_id"

        fun jsonStringToAnnotationList(jsonString: String): LinkedList<Annotation?> {
            val jsonArray = JSONArray(jsonString)
            val annotationList: LinkedList<Annotation?> = LinkedList()

            var jsonAnnotation: JSONObject
            var jsonPosition: JSONObject
            for (i in 0 until jsonArray.length()) {
                jsonAnnotation = jsonArray.getJSONObject(i)
                jsonPosition = jsonAnnotation.optJSONObject(Annotation.POSITION_JSON_TAG)

                annotationList.addLast( Annotation(
                        jsonAnnotation.getLong(Annotation.ANNOTATION_ID_JSON_TAG),
                        jsonAnnotation.optString(Annotation.TITLE_JSON_TAG, null),
                        jsonAnnotation.optString(Annotation.IMAGE_JSON_TAG, null),
                        jsonPosition?.let { position ->
                            Location(position.getDouble(Annotation.POSITION_LAT_JSON_TAG),
                                    position.getDouble(Annotation.POSITION_LNG_JSON_TAG))
                        },
                        jsonAnnotation.optString(Annotation.MARKER_ID_JSON_TAG, null)))
            }
            return annotationList
        }
    }
}