package lam.com.locationmapservice.demo.api.interfaces

import io.reactivex.Observable
import lam.com.locationmapservice.lib.models.Annotation
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface IDummyApi {
    @Headers("Content-Type: application/json")
    @GET("dummy/annotations")
    fun getDummyAnnotations(@Query("latMin") latMin: Float? = -90f, @Query("latMax") latMax: Float? = 90f, @Query("lngMin") lngMin: Float? = -180f, @Query("lngMax") lngMax: Float? = 180f): Observable<ArrayList<Annotation?>>
}