package cl.duoc.mpazpro.data.repository

import cl.duoc.mpazpro.data.remote.DogCeoApi
import cl.duoc.mpazpro.data.remote.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import java.io.IOException

class DogCeoRepository(
    private val api: DogCeoApi = RetrofitClient.dogCeo.create(DogCeoApi::class.java)
) {
    suspend fun obtenerFoto(): Result<ByteArray> = try {
        val foto = api.obtenerFotoAleatoria()
        val url = foto.url?.let(HttpUrl::parse)
        if (foto.estado != "success" || url == null || !url.isHttps || url.host() != "images.dog.ceo") {
            throw IOException("Dog CEO no devolvió una URL de foto válida.")
        }
        val imagen = withContext(Dispatchers.IO) {
            api.descargarFoto(url).use { cuerpo ->
                val fuente = cuerpo.source()
                if (fuente.request(MAX_BYTES + 1)) throw IOException("La foto es demasiado grande.")
                fuente.readByteArray().also {
                    if (it.isEmpty()) throw IOException("La foto está vacía.")
                }
            }
        }
        Result.success(imagen)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    private companion object {
        const val MAX_BYTES = 5L * 1024 * 1024
    }
}
