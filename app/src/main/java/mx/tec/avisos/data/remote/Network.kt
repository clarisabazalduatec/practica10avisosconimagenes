package mx.tec.avisos.data.remote

import kotlinx.serialization.json.Json
import mx.tec.avisos.BuildConfig
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object Network {

    /** Sale de `local.properties` (`avisos.api`). Ver app/build.gradle.kts. */
    const val BASE_URL = "https://statistics-requires-chance-venues.trycloudflare.com/api/"

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    /**
     * El cliente HTTP. El interceptor firma cada petición con el token; el
     * authenticator reacciona cuando el servidor responde 401.
     *
     * Es UNO para toda la app: Retrofit lo usa para las peticiones normales y
     * `AvisosStream` para la conexión larga. Así el stream también va firmado
     * y también se renueva solo.
     */
    fun crearCliente(interceptor: Interceptor? = null, authenticator: Authenticator? = null): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
            // Con Level.HEADERS o BODY, sin esta línea el token sale en el Logcat.
            redactHeader("Authorization")
        }

        return OkHttpClient.Builder().apply {
            if (interceptor != null) addInterceptor(interceptor)
            if (authenticator != null) authenticator(authenticator)
            // El de log va al final, para que vea la petición ya firmada.
            addInterceptor(logging)
        }.build()
    }

    /** Retrofit: petición y respuesta. Para lo que se queda abierto está `AvisosStream`. */
    fun crearApi(cliente: OkHttpClient): AvisosApi =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(cliente)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AvisosApi::class.java)
}
