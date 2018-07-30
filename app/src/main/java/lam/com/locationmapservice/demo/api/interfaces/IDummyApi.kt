package lam.com.locationmapservice.demo.api.interfaces

import io.reactivex.Observable
import lam.com.locationmapservice.demo.models.AnnotationMeta
import com.lam.locationmapservicelib.models.Annotation
import retrofit2.Response
import retrofit2.http.*

interface IDummyApi {
    @Headers("Content-Type: application/json")
    @GET("dummy/annotations")
    fun getDummyAnnotations(@Query("latMin") latMin: Float? = -90f,
                            @Query("latMax") latMax: Float? = 90f,
                            @Query("lngMin") lngMin: Float? = -180f,
                            @Query("lngMax") lngMax: Float? = 180f,
                            @Query("id") annotationId: Long): Observable<Response<ArrayList<Annotation>>>

    @Headers("Content-Type: application/json")
    @GET("dummy/{id}")
    fun getDummyAnnotation(@Path("id") id: Long): Observable<Response<Annotation>>

    @Headers("Content-Type: application/json")
    @GET("dummy/meta/{id}")
    fun getDummyAnnotationMeta(@Path("id") id: Long): Observable<Response<AnnotationMeta>>

    @Headers("Content-Type: application/json")
    @PUT("dummy/position")
    fun updatePosition(@Query("lat") lat: Float,
                            @Query("lng") lng: Float,
                            @Query("enabled") enabled: Float,
                            @Query("id") annotationId: Long): Observable<Response<String>>
}