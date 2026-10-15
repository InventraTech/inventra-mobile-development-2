package com.inventraoficial.inventra.di

import android.content.Context
import com.inventraoficial.inventra.BuildConfig
import com.inventraoficial.inventra.data.local.AppPreferences
import com.inventraoficial.inventra.data.local.DataStoreAppPreferences
import com.inventraoficial.inventra.data.local.DataStoreSessionStorage
import com.inventraoficial.inventra.data.local.SessionStorage
import com.inventraoficial.inventra.data.remote.api.AlertApi
import com.inventraoficial.inventra.data.remote.api.AuthApi
import com.inventraoficial.inventra.data.remote.api.KitchenAccessApi
import com.inventraoficial.inventra.data.remote.api.KitchenApi
import com.inventraoficial.inventra.data.remote.api.ProductApi
import com.inventraoficial.inventra.data.remote.api.StockApi
import com.inventraoficial.inventra.data.remote.api.SupplierApi
import com.inventraoficial.inventra.data.remote.api.UserApi
import com.inventraoficial.inventra.data.remote.interceptor.AuthInterceptor
import com.inventraoficial.inventra.data.remote.interceptor.TokenAuthenticator
import com.inventraoficial.inventra.data.repository.AlertRepository
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.KitchenRepository
import com.inventraoficial.inventra.data.repository.MembersRepository
import com.inventraoficial.inventra.data.repository.NetworkAlertRepository
import com.inventraoficial.inventra.data.repository.NetworkAuthRepository
import com.inventraoficial.inventra.data.repository.NetworkKitchenRepository
import com.inventraoficial.inventra.data.repository.NetworkMembersRepository
import com.inventraoficial.inventra.data.repository.NetworkProductRepository
import com.inventraoficial.inventra.data.repository.NetworkStockRepository
import com.inventraoficial.inventra.data.repository.NetworkSupplierRepository
import com.inventraoficial.inventra.data.repository.ProductRepository
import com.inventraoficial.inventra.data.repository.StockRepository
import com.inventraoficial.inventra.data.repository.SupplierRepository
import com.inventraoficial.inventra.data.session.SessionManager
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

interface AppContainer {
    val authRepository: AuthRepository
    val kitchenRepository: KitchenRepository
    val membersRepository: MembersRepository
    val stockRepository: StockRepository
    val alertRepository: AlertRepository
    val supplierRepository: SupplierRepository
    val productRepository: ProductRepository
    val sessionManager: SessionManager
    val appPreferences: AppPreferences
}

class DefaultAppContainer(
    private val context: Context,
) : AppContainer {
    private val json =
        Json {
            ignoreUnknownKeys = true
            decodeEnumsCaseInsensitive = true
        }
    private val sessionStorage: SessionStorage by lazy { DataStoreSessionStorage(context, json) }
    override val sessionManager = SessionManager()
    override val appPreferences: AppPreferences by lazy { DataStoreAppPreferences(context) }
    private val authInterceptor: AuthInterceptor by lazy { AuthInterceptor(sessionStorage, sessionManager) }

    private val logging =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
        }

    private val refreshApi: AuthApi by lazy {
        val refreshClient =
            OkHttpClient
                .Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(refreshClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthApi::class.java)
    }

    private val tokenAuthenticator: TokenAuthenticator by lazy { TokenAuthenticator(sessionStorage, refreshApi) }

    private val okHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .authenticator(tokenAuthenticator)
            .build()

    private val retrofit =
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    private val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    private val userApi: UserApi by lazy { retrofit.create(UserApi::class.java) }
    private val kitchenApi: KitchenApi by lazy { retrofit.create(KitchenApi::class.java) }
    private val kitchenAccessApi: KitchenAccessApi by lazy { retrofit.create(KitchenAccessApi::class.java) }
    private val stockApi: StockApi by lazy { retrofit.create(StockApi::class.java) }
    private val alertApi: AlertApi by lazy { retrofit.create(AlertApi::class.java) }
    private val supplierApi: SupplierApi by lazy { retrofit.create(SupplierApi::class.java) }
    private val productApi: ProductApi by lazy { retrofit.create(ProductApi::class.java) }

    override val authRepository: AuthRepository by lazy {
        NetworkAuthRepository(
            authApi,
            userApi,
            json,
            sessionStorage,
        )
    }

    override val kitchenRepository: KitchenRepository by lazy { NetworkKitchenRepository(kitchenApi, json) }

    override val membersRepository: MembersRepository by lazy {
        NetworkMembersRepository(kitchenAccessApi, userApi, json)
    }

    override val stockRepository: StockRepository by lazy { NetworkStockRepository(stockApi, json) }

    override val alertRepository: AlertRepository by lazy { NetworkAlertRepository(alertApi, json) }
    override val supplierRepository: SupplierRepository by lazy { NetworkSupplierRepository(supplierApi, json) }

    override val productRepository: ProductRepository by lazy { NetworkProductRepository(productApi, json) }

    companion object {
        private const val BASE_URL = "https://ms-inventra-api.onrender.com/api/"
        private const val TIMEOUT_SECONDS = 60L
    }
}
