package com.inventraoficial.inventra.di

import com.inventraoficial.inventra.BuildConfig
import com.inventraoficial.inventra.data.remote.api.AuthApi
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.NetworkAuthRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

interface AppContainer {
    val authRepository: AuthRepository
}

class DefaultAppContainer : AppContainer {
    private val json =
        Json {
            ignoreUnknownKeys = true
            decodeEnumsCaseInsensitive = true
        }

    private val logging =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

    private val okHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

    private val retrofit =
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    private val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }

    override val authRepository: AuthRepository by lazy { NetworkAuthRepository(authApi, json) }

    companion object {
        private const val BASE_URL = "https://ms-inventra-api.onrender.com/api/"
        private const val TIMEOUT_SECONDS = 60L
    }
}
