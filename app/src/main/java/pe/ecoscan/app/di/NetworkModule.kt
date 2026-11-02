package pe.ecoscan.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pe.ecoscan.app.BuildConfig
import pe.ecoscan.app.data.remote.api.EcoScanApi
import pe.ecoscan.app.data.remote.api.OverpassApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// Open Food Facts (HU05): consulta de productos por código de barras en /api/v2/.
// Open Food Facts exige un User-Agent descriptivo o puede bloquear el tráfico.
private const val BASE_URL = "https://world.openfoodfacts.org/api/v2/"
private const val USER_AGENT = "EcoScan/1.0 (proyecto academico UNMSM)"
private const val TIMEOUT_SECONDS = 15L
private const val OVERPASS_CONNECT_TIMEOUT_SECONDS = 10L
private const val OVERPASS_READ_TIMEOUT_SECONDS = 30L

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val userAgentInterceptor = Interceptor { chain ->
            val requestWithUserAgent = chain.request().newBuilder()
                .header("User-Agent", USER_AGENT)
                .build()
            chain.proceed(requestWithUserAgent)
        }

        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(userAgentInterceptor)

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideEcoScanApi(retrofit: Retrofit): EcoScanApi = retrofit.create(EcoScanApi::class.java)

    // Overpass API (HU07): puntos de acopio cercanos. Comparte el OkHttp (User-Agent y logs)
    // pero con mas tiempo de lectura: una consulta de 5 km en Lima puede tardar mas de 15 s.
    // El servidor se elige por llamada con @Url (ver OverpassApi.ENDPOINTS).
    @Provides
    @Singleton
    fun provideOverpassApi(okHttpClient: OkHttpClient): OverpassApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(
            okHttpClient.newBuilder()
                .connectTimeout(OVERPASS_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(OVERPASS_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OverpassApi::class.java)
}