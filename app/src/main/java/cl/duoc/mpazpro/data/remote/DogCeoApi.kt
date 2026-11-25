package cl.duoc.mpazpro.data.remote

import cl.duoc.mpazpro.data.remote.dto.FotoPerroDto
import okhttp3.HttpUrl
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DogCeoApi {
    @GET("api/breeds/image/random")
    suspend fun obtenerFotoAleatoria(): FotoPerroDto

    @Streaming
    @GET
    suspend fun descargarFoto(@Url url: HttpUrl): ResponseBody
}
