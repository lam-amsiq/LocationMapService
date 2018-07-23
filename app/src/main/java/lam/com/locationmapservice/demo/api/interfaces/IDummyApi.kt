package lam.com.locationmapservice.demo.api.interfaces

import io.reactivex.Observable
import lam.com.locationmapservice.demo.models.AnnotationMeta
import lam.com.locationmapservice.lib.models.Annotation
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface IDummyApi {
    @Headers("Content-Type: application/json")
    @GET("dummy/annotations")
    fun getDummyAnnotations(@Query("latMin") latMin: Float? = -90f, @Query("latMax") latMax: Float? = 90f, @Query("lngMin") lngMin: Float? = -180f, @Query("lngMax") lngMax: Float? = 180f, @Query("male") male: Boolean): Observable<Response<ArrayList<Annotation>>>

    @Headers("Content-Type: application/json")
    @GET("dummy/meta/{id}")
    fun getDummyAnnotationMeta(@Path("id") id: Long): Observable<Response<AnnotationMeta>>
}