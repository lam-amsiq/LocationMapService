package lam.com.locationmapservice.demo.api

class ApiService private constructor() : ApiServiceGenerator() {
    init {
        super.timeOut = 60
    }

    companion object {
        private var instance: ApiService? = null

        fun <S> createService(serviceClass: Class<S>): S {
            if (instance == null) {
                instance = ApiService()
            }
            return instance?.getRetrofit()?.create(serviceClass)!!
        }
    }
}