package cl.duoc.mpazpro.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Clientes Retrofit de los servicios de la app.
 * 10.0.2.2 es la direccion que el emulador usa para llegar al localhost del PC.
 */
object RetrofitClient {

    private const val URL_RESULTADOS = "http://10.0.2.2:9081/"
    private const val URL_CONTENIDOS = "http://10.0.2.2:9082/"

    private const val URL_DOG_CEO = "https://dog.ceo/"

    private fun crear(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /** ms-resultados (puerto 9081) */
    val resultados: Retrofit by lazy { crear(URL_RESULTADOS) }

    /** ms-contenidos (puerto 9082) */
    val contenidos: Retrofit by lazy { crear(URL_CONTENIDOS) }

    val dogCeo: Retrofit by lazy { crear(URL_DOG_CEO) }
}
