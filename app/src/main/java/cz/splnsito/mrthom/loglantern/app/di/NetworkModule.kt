package cz.splnsito.mrthom.loglantern.app.di

import android.util.Log
import cz.splnsito.mrthom.loglantern.BuildConfig
import cz.splnsito.mrthom.loglantern.core.network.auth.SplunkAuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: SplunkAuthInterceptor): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        // Add logging interceptor for debugging
        if (BuildConfig.ENABLE_LOGGING) {
            val logging = HttpLoggingInterceptor { message ->
                Log.i("okhttp.OkHttpClient", message)
            }
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            builder.addInterceptor(logging)
        }

        // DEVELOPMENT ONLY: Allow self-signed certificates
        // Automatically disabled in release builds via BuildConfig
        if (BuildConfig.ALLOW_SELF_SIGNED_CERTS) {
            Log.w("NetworkModule", "⚠️ SSL VERIFICATION DISABLED - DEVELOPMENT MODE ONLY")
            Log.w("NetworkModule", "⚠️ This is NOT SAFE for production and will trigger Google Play Protect!")

            try {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                        Log.d("NetworkModule", "Accepting client certificate: ${chain.firstOrNull()?.subjectDN}")
                    }
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                        Log.d("NetworkModule", "Accepting server certificate: ${chain.firstOrNull()?.subjectDN}")
                    }
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                })

                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())

                builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier { hostname, _ ->
                    Log.d("NetworkModule", "Accepting hostname: $hostname")
                    true
                }
            } catch (e: Exception) {
                Log.e("NetworkModule", "Failed to setup SSL bypass", e)
                throw RuntimeException("SSL configuration failed", e)
            }
        } else {
            Log.i("NetworkModule", "✅ SSL verification ENABLED - Production mode")
        }

        return builder.build()
    }

    @Provides
    fun provideRetrofitBuilder(okHttpClient: OkHttpClient): Retrofit.Builder {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
    }
}
