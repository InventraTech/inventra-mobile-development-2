package com.inventraoficial.inventra.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.inventraoficial.inventra.BuildConfig
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.data.event.BufferedClickTracker
import com.inventraoficial.inventra.data.event.ClickTracker
import com.inventraoficial.inventra.data.event.DataStoreClickCountStore
import com.inventraoficial.inventra.data.event.uiClicksDataStore
import com.inventraoficial.inventra.data.local.AndroidImageCompressor
import com.inventraoficial.inventra.data.local.AppPreferences
import com.inventraoficial.inventra.data.local.DataStoreAppPreferences
import com.inventraoficial.inventra.data.local.DataStoreSessionStorage
import com.inventraoficial.inventra.data.local.FusedLocationProvider
import com.inventraoficial.inventra.data.local.ImageCompressor
import com.inventraoficial.inventra.data.local.InventraDatabase
import com.inventraoficial.inventra.data.local.LocationProvider
import com.inventraoficial.inventra.data.local.ProfilePhotoPreview
import com.inventraoficial.inventra.data.local.SessionStorage
import com.inventraoficial.inventra.data.local.SqliteStockCache
import com.inventraoficial.inventra.data.local.StockCache
import com.inventraoficial.inventra.data.remote.api.AlertApi
import com.inventraoficial.inventra.data.remote.api.AuthApi
import com.inventraoficial.inventra.data.remote.api.ClickEventApi
import com.inventraoficial.inventra.data.remote.api.KitchenAccessApi
import com.inventraoficial.inventra.data.remote.api.KitchenApi
import com.inventraoficial.inventra.data.remote.api.PreListApi
import com.inventraoficial.inventra.data.remote.api.ProductApi
import com.inventraoficial.inventra.data.remote.api.ProductPhotoApi
import com.inventraoficial.inventra.data.remote.api.ProductSupplierApi
import com.inventraoficial.inventra.data.remote.api.RequisitionApi
import com.inventraoficial.inventra.data.remote.api.StockApi
import com.inventraoficial.inventra.data.remote.api.SupplierApi
import com.inventraoficial.inventra.data.remote.api.SupplierPhotoApi
import com.inventraoficial.inventra.data.remote.api.UserApi
import com.inventraoficial.inventra.data.remote.auth.google.GoogleAuthClient
import com.inventraoficial.inventra.data.remote.interceptor.AuthInterceptor
import com.inventraoficial.inventra.data.remote.interceptor.TokenAuthenticator
import com.inventraoficial.inventra.data.repository.AlertRepository
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.KitchenAccessRepository
import com.inventraoficial.inventra.data.repository.KitchenRepository
import com.inventraoficial.inventra.data.repository.MembersRepository
import com.inventraoficial.inventra.data.repository.NetworkAlertRepository
import com.inventraoficial.inventra.data.repository.NetworkAuthRepository
import com.inventraoficial.inventra.data.repository.NetworkKitchenAccessRepository
import com.inventraoficial.inventra.data.repository.NetworkKitchenRepository
import com.inventraoficial.inventra.data.repository.NetworkMembersRepository
import com.inventraoficial.inventra.data.repository.NetworkPasswordResetRepository
import com.inventraoficial.inventra.data.repository.NetworkPreListRepository
import com.inventraoficial.inventra.data.repository.NetworkProductPhotoRepository
import com.inventraoficial.inventra.data.repository.NetworkProductRepository
import com.inventraoficial.inventra.data.repository.NetworkProductSupplierRepository
import com.inventraoficial.inventra.data.repository.NetworkRequisitionRepository
import com.inventraoficial.inventra.data.repository.NetworkStockRepository
import com.inventraoficial.inventra.data.repository.NetworkSupplierPhotoRepository
import com.inventraoficial.inventra.data.repository.NetworkSupplierRepository
import com.inventraoficial.inventra.data.repository.PasswordResetRepository
import com.inventraoficial.inventra.data.repository.PreListRepository
import com.inventraoficial.inventra.data.repository.ProductPhotoRepository
import com.inventraoficial.inventra.data.repository.ProductRepository
import com.inventraoficial.inventra.data.repository.ProductSupplierRepository
import com.inventraoficial.inventra.data.repository.RequisitionRepository
import com.inventraoficial.inventra.data.repository.StockRepository
import com.inventraoficial.inventra.data.repository.SupplierPhotoRepository
import com.inventraoficial.inventra.data.repository.SupplierRepository
import com.inventraoficial.inventra.data.session.SessionManager
import com.inventraoficial.inventra.feature.notifications.background.AccessRequestChecker
import com.inventraoficial.inventra.feature.notifications.background.DataStoreKnownAccessRequestStore
import com.inventraoficial.inventra.feature.notifications.background.DataStoreLastNotifiedAlertStore
import com.inventraoficial.inventra.feature.notifications.background.NewAlertsChecker
import com.inventraoficial.inventra.feature.notifications.background.SystemAlertNotifier
import com.inventraoficial.inventra.feature.suppliers.create.SupplierSelection
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

interface AppContainer {
    val authRepository: AuthRepository
    val passwordResetRepository: PasswordResetRepository
    val kitchenRepository: KitchenRepository
    val kitchenAccessRepository: KitchenAccessRepository
    val membersRepository: MembersRepository
    val stockRepository: StockRepository
    val alertRepository: AlertRepository
    val supplierRepository: SupplierRepository
    val supplierPhotoRepository: SupplierPhotoRepository
    val productRepository: ProductRepository
    val productSupplierRepository: ProductSupplierRepository
    val productPhotoRepository: ProductPhotoRepository
    val requisitionRepository: RequisitionRepository
    val preListRepository: PreListRepository
    val sessionManager: SessionManager
    val appPreferences: AppPreferences
    val googleAuthClient: GoogleAuthClient
    val imageCompressor: ImageCompressor
    val profilePhotoPreview: ProfilePhotoPreview
    val locationProvider: LocationProvider
    val alertNotifier: SystemAlertNotifier
    val newAlertsChecker: NewAlertsChecker
    val accessRequestChecker: AccessRequestChecker
    val clickTracker: ClickTracker
    val supplierSelection: SupplierSelection
    val stockCache: StockCache
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
    private val supplierPhotoApi: SupplierPhotoApi by lazy { retrofit.create(SupplierPhotoApi::class.java) }
    private val productApi: ProductApi by lazy { retrofit.create(ProductApi::class.java) }
    private val productSupplierApi: ProductSupplierApi by lazy { retrofit.create(ProductSupplierApi::class.java) }
    private val productPhotoApi: ProductPhotoApi by lazy { retrofit.create(ProductPhotoApi::class.java) }
    private val requisitionApi: RequisitionApi by lazy { retrofit.create(RequisitionApi::class.java) }
    private val preListApi: PreListApi by lazy { retrofit.create(PreListApi::class.java) }
    private val clickEventApi: ClickEventApi by lazy { retrofit.create(ClickEventApi::class.java) }

    private val backgroundScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, _ -> })

    override val authRepository: AuthRepository by lazy {
        NetworkAuthRepository(
            authApi,
            userApi,
            json,
            sessionStorage,
            clearExternalSession = { googleAuthClient.signOut() },
        )
    }

    override val passwordResetRepository: PasswordResetRepository by lazy {
        NetworkPasswordResetRepository(authApi, json)
    }

    override val googleAuthClient: GoogleAuthClient by lazy {
        GoogleAuthClient(context, context.getString(R.string.default_web_client_id), FirebaseAuth.getInstance())
    }

    override val imageCompressor: ImageCompressor by lazy {
        val imageClient =
            OkHttpClient
                .Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()
        AndroidImageCompressor(context, imageClient)
    }
    override val profilePhotoPreview = ProfilePhotoPreview()
    override val locationProvider: LocationProvider by lazy { FusedLocationProvider(context) }

    override val kitchenRepository: KitchenRepository by lazy { NetworkKitchenRepository(kitchenApi, json) }

    override val kitchenAccessRepository: KitchenAccessRepository by lazy {
        NetworkKitchenAccessRepository(kitchenApi, kitchenAccessApi, json)
    }

    override val membersRepository: MembersRepository by lazy {
        NetworkMembersRepository(kitchenAccessApi, userApi, json)
    }

    override val stockRepository: StockRepository by lazy { NetworkStockRepository(stockApi, json) }

    override val alertRepository: AlertRepository by lazy { NetworkAlertRepository(alertApi, json) }

    override val supplierRepository: SupplierRepository by lazy { NetworkSupplierRepository(supplierApi, json) }
    override val supplierPhotoRepository: SupplierPhotoRepository by lazy {
        NetworkSupplierPhotoRepository(supplierPhotoApi, imageCompressor, json)
    }

    override val productRepository: ProductRepository by lazy { NetworkProductRepository(productApi, json) }
    override val productSupplierRepository: ProductSupplierRepository by lazy {
        NetworkProductSupplierRepository(productSupplierApi, json)
    }
    override val productPhotoRepository: ProductPhotoRepository by lazy {
        NetworkProductPhotoRepository(productPhotoApi, imageCompressor, json)
    }

    override val requisitionRepository: RequisitionRepository by lazy {
        NetworkRequisitionRepository(requisitionApi, json)
    }

    override val preListRepository: PreListRepository by lazy { NetworkPreListRepository(preListApi, json) }

    override val alertNotifier: SystemAlertNotifier by lazy { SystemAlertNotifier(context) }

    override val newAlertsChecker: NewAlertsChecker by lazy {
        NewAlertsChecker(
            authRepository = authRepository,
            alertRepository = alertRepository,
            appPreferences = appPreferences,
            store = DataStoreLastNotifiedAlertStore(context),
            notifier = alertNotifier,
        )
    }

    override val accessRequestChecker: AccessRequestChecker by lazy {
        AccessRequestChecker(
            authRepository = authRepository,
            kitchenAccessRepository = kitchenAccessRepository,
            appPreferences = appPreferences,
            store = DataStoreKnownAccessRequestStore(context),
            notifier = alertNotifier,
        )
    }

    override val supplierSelection = SupplierSelection()

    override val stockCache: StockCache by lazy { SqliteStockCache(InventraDatabase(context), json) }

    override val clickTracker: ClickTracker by lazy {
        BufferedClickTracker(
            store = DataStoreClickCountStore(context.uiClicksDataStore),
            sessionStorage = sessionStorage,
            clickEventApi = clickEventApi,
            json = json,
            scope = backgroundScope,
        )
    }

    companion object {
        private const val BASE_URL = BuildConfig.API_BASE_URL
        private const val TIMEOUT_SECONDS = 60L
    }
}
