package cl.duoc.mpazpro.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DogCeoApiTest {
    @Test
    fun consultaElEndpointPublicoYConvierteElJson() = runBlocking {
        val api = crearApi { solicitud ->
            assertEquals("GET", solicitud.method())
            assertEquals("https://dog.ceo/api/breeds/image/random", solicitud.url().toString())
            assertNull(solicitud.header("Authorization"))
            ResponseBody.create(
                MediaType.get("application/json"),
                """{"message":"https://images.dog.ceo/breeds/beagle/foto.jpg","status":"success"}"""
            )
        }

        val foto = api.obtenerFotoAleatoria()
        assertEquals("success", foto.estado)
        assertEquals("https://images.dog.ceo/breeds/beagle/foto.jpg", foto.url)
    }

    @Test
    fun descargaElCuerpoBinarioDeLaUrlAbsoluta() = runBlocking {
        val url = requireNotNull(HttpUrl.parse("https://images.dog.ceo/breeds/beagle/foto.jpg"))
        val esperados = byteArrayOf(1, 2, 3)
        val api = crearApi { solicitud ->
            assertEquals("GET", solicitud.method())
            assertEquals(url, solicitud.url())
            ResponseBody.create(MediaType.get("image/jpeg"), esperados)
        }

        val obtenidos = api.descargarFoto(url).use { it.bytes() }
        assertArrayEquals(esperados, obtenidos)
    }

    private fun crearApi(cuerpo: (Request) -> ResponseBody): DogCeoApi {
        val cliente = OkHttpClient.Builder().addInterceptor { cadena ->
            val solicitud = cadena.request()
            Response.Builder()
                .request(solicitud)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(cuerpo(solicitud))
                .build()
        }.build()
        return Retrofit.Builder()
            .baseUrl(RetrofitClient.dogCeo.baseUrl())
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DogCeoApi::class.java)
    }
}
