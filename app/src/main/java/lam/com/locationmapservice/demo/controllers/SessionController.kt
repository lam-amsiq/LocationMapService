package lam.com.locationmapservice.demo.controllers

import com.lam.locationmapservicelib.models.Annotation
import io.reactivex.schedulers.Schedulers
import io.reactivex.Observable
import lam.com.locationmapservice.demo.api.ApiService
import lam.com.locationmapservice.demo.api.interfaces.IDummyApi
import lam.com.locationmapservice.demo.models.AnnotationMeta
import retrofit2.Response

object SessionController {
    var user: Annotation? = null
    var meta: AnnotationMeta? = null
    var isLoaded: Boolean = false
        get() { return user != null && meta != null}

    fun loadUserAndMeta(annotationId: Long): Observable<Response<out Any>>? {
        return Observable.concat(loadUser(annotationId), loadMeta(annotationId))
                ?.observeOn(Schedulers.io())
                ?.subscribeOn(Schedulers.io())
    }

    private fun loadUser(annotationId: Long): Observable<Response<Annotation>> {
        val observable = ApiService.createService(IDummyApi::class.java)
                .getDummyAnnotation(annotationId)

        observable.observeOn(Schedulers.io())
                .doOnNext { response ->
                    if (response.isSuccessful) {
                        user = response.body()
                    }
                }

        return observable
    }

    private fun loadMeta(annotationId: Long): Observable<Response<AnnotationMeta>> {
        val observable = ApiService.createService(IDummyApi::class.java)
                .getDummyAnnotationMeta(annotationId)

        observable.observeOn(Schedulers.io())
                .doOnNext { response ->
                    if (response.isSuccessful) {
                        meta = response.body()
                    }
                }

        return observable
    }
}