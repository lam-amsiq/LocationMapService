package lam.com.locationmapservice.demo.api

import com.google.gson.GsonBuilder
import lam.com.locationmapservice.BuildConfig
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

open class ApiServiceGenerator {
    private var httpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null

    internal var timeOut: Int = 0

    init {
        WEB_URL = BuildConfig.BASEURLWEB
        API_URL = BuildConfig.BASEURLAPI
    }

    private fun getHttpClient(): OkHttpClient {
        if (httpClient == null) {

            // Set Logging interceptor
            val log = HttpLoggingInterceptor()
            if (BuildConfig.DEBUG) {
                log.level = HttpLoggingInterceptor.Level.HEADERS
            } else {
                log.level = HttpLoggingInterceptor.Level.NONE
            }

            // Create Httpclient
            val builder = OkHttpClient.Builder()
                    .addInterceptor(log)
                    .connectionPool(ConnectionPool())
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(timeOut.toLong(), TimeUnit.SECONDS)
                    .readTimeout(timeOut.toLong(), TimeUnit.SECONDS)

            httpClient = builder.build()
        }
        return httpClient!!

    }

    internal fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            // Build Retrofit object
            retrofit = Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder()
                            .create()
                    ))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(getHttpClient())
                    .build()
        }

        return retrofit!!
    }

    companion object {
        var WEB_URL = BuildConfig.BASEURLWEB
        var API_URL = BuildConfig.BASEURLAPI
    }
}