package lam.com.locationmapservice.demo.api.interfaces

import io.reactivex.Observable
import lam.com.locationmapservice.lib.models.Annotation
import retrofit2.http.GET
import retrofit2.http.Headers

interface IDummyAnnotationApi {
    @Headers("Content-Type: application/json")
    @GET("dummyannotations")
    fun getDummyAnnotations(): Observable<ArrayList<Annotation?>>
}