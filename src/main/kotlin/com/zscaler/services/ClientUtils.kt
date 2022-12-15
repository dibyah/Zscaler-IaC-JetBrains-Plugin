package com.zscaler.services

import com.zscaler.settings.SettingState
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit


class ClientUtils {
    companion object {
        fun createRetrofit(url: String?, interceptor: Optional<Interceptor>): Retrofit? {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val httpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .callTimeout(10, TimeUnit.MINUTES)
                .followRedirects(true)
                .addInterceptor(loggingInterceptor)

            interceptor.ifPresent { interceptor: Interceptor ->
               httpClient.addInterceptor(
                        interceptor
                    )
            }
            return Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(httpClient.build())
                .build()
        }

        fun getAuthInterceptor(): Optional<Interceptor> {

            return Optional.of(
                Interceptor { chain: Interceptor.Chain ->
                    val request = chain.request()
                    var newRequest = request
                        .newBuilder()
                        .addHeader("Authorization", "Bearer " + SettingState().getInstance()?.apiKey)
                        .build()
                    var response = chain.proceed(newRequest)
                    response
                })
        }
    }
}