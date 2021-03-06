package com.lam.locationmapservicelib.models

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

open class Annotation(
        @PrimaryKey
        var annotation_id: Long = 0,
        var name: String? = null,
        var portrait: String? = null,
        var thumb: String? = null,
        var position: Location? = null,
        var marker_id: String? = null) : RealmObject() {

    fun store(realm: Realm) {
        val realmPosition = realm.createObject(Location::class.java)
        realmPosition.lat = this.position?.lat
        realmPosition.lng = this.position?.lng

        val realmAnnotation = realm.createObject(Annotation::class.java, this.annotation_id)
        realmAnnotation.position = realmPosition

        realmAnnotation.portrait = this.portrait
        realmAnnotation.thumb = this.thumb
        realmAnnotation.marker_id = this.marker_id
        realmAnnotation.name = this.name
    }

    override fun toString(): String {
        return "Annotation($annotation_id: $name - marker_id=$marker_id, position=$position, portrait=$portrait)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        return (other as? Annotation)?.let {
            annotation_id == other.annotation_id &&
                    name?.equals(other.name) == true &&
                    portrait?.equals(other.portrait) == true &&
                    thumb?.equals(other.thumb) == true &&
                    position?.equals(other.position) == true &&
                    marker_id?.equals(other.marker_id) == true
        } ?: false
    }

    override fun hashCode(): Int {
        var result = annotation_id.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (portrait?.hashCode() ?: 0)
        result = 31 * result + (thumb?.hashCode() ?: 0)
        result = 31 * result + (position?.hashCode() ?: 0)
        result = 31 * result + (marker_id?.hashCode() ?: 0)
        return result
    }

    companion object {
        const val ANNOTATION_ID_JSON_TAG = "annotation_id"
        const val TITLE_JSON_TAG = "name"
        const val PORTRAIT_JSON_TAG = "portrait"
        const val THUMB_JSON_TAG = "thumb"
        const val POSITION_JSON_TAG = "position"
        const val POSITION_LAT_JSON_TAG = "lat"
        const val POSITION_LNG_JSON_TAG = "lng"
        const val POSITION_ENABLED_TAG = "enabled"
        const val MARKER_ID_JSON_TAG = "marker_id"

        fun jsonStringToAnnotationList(jsonString: String): LinkedList<Annotation> {
            val jsonArray = JSONArray(jsonString)
            val annotationList: LinkedList<Annotation> = LinkedList()

            var jsonAnnotation: JSONObject
            var jsonPosition: JSONObject
            for (i in 0 until jsonArray.length()) {
                jsonAnnotation = jsonArray.getJSONObject(i)
                jsonPosition = jsonAnnotation.optJSONObject(Annotation.POSITION_JSON_TAG)

                annotationList.addLast( Annotation(
                        jsonAnnotation.getLong(Annotation.ANNOTATION_ID_JSON_TAG),
                        jsonAnnotation.optString(Annotation.TITLE_JSON_TAG, null),
                        jsonAnnotation.optString(Annotation.PORTRAIT_JSON_TAG, null),
                        jsonAnnotation.optString(Annotation.THUMB_JSON_TAG, null),
                        jsonPosition?.let { position ->
                            Location(position.getDouble(Annotation.POSITION_LAT_JSON_TAG),
                                    position.getDouble(Annotation.POSITION_LNG_JSON_TAG),
                                    position.getBoolean(Annotation.POSITION_ENABLED_TAG))
                        },
                        jsonAnnotation.optString(Annotation.MARKER_ID_JSON_TAG, null)))
            }
            return annotationList
        }
    }
}