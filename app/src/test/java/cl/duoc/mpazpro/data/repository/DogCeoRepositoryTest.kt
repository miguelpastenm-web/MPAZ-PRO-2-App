package cl.duoc.mpazpro.data.repository

import cl.duoc.mpazpro.data.remote.DogCeoApi
import cl.duoc.mpazpro.data.remote.dto.FotoPerroDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class DogCeoRepositoryTest {
    private val url = "https://images.dog.ceo/breeds/beagle/foto.jpg"
    private val api = mockk<DogCeoApi>()
    private val repository = DogCeoRepository(api)

    @Test
    fun obtieneLosBytesDeLaFoto() = runBlocking {
        val datos = byteArrayOf(20, 40, 60)
        coEvery { api.obtenerFotoAleatoria() } returns FotoPerroDto(url, "success")
        coEvery { api.descargarFoto(requireNotNull(HttpUrl.parse(url))) } answers {
            ResponseBody.create(null, datos)
        }

        assertArrayEquals(datos, repository.obtenerFoto().getOrThrow())
    }

    @Test
    fun rechazaUnaRespuestaFallidaOUnaUrlNoValida() = runBlocking {
        val invalidas = listOf(
            FotoPerroDto(url, "error"),
            FotoPerroDto(null, "success"),
            FotoPerroDto("http://images.dog.ceo/foto.jpg", "success"),
            FotoPerroDto("https://otro.example/foto.jpg", "success")
        )
        for (foto in invalidas) {
            coEvery { api.obtenerFotoAleatoria() } returns foto
            assertTrue(repository.obtenerFoto().isFailure)
        }
        coVerify(exactly = 0) { api.descargarFoto(any()) }
    }

    @Test
    fun rechazaUnaImagenVaciaODemasiadoGrande() = runBlocking {
        coEvery { api.obtenerFotoAleatoria() } returns FotoPerroDto(url, "success")
        for (cantidad in listOf(0, 5 * 1024 * 1024 + 1)) {
            coEvery { api.descargarFoto(any()) } answers { ResponseBody.create(null, ByteArray(cantidad)) }
            assertTrue(repository.obtenerFoto().isFailure)
        }
    }

    @Test
    fun devuelveElFalloDeConexion() = runBlocking {
        val error = IOException("sin conexión")
        coEvery { api.obtenerFotoAleatoria() } returns FotoPerroDto(url, "success")
        coEvery { api.descargarFoto(any()) } throws error

        val fallo = repository.obtenerFoto().exceptionOrNull()
        assertTrue(fallo is IOException)
        assertEquals(error.message, fallo?.message)
    }

    @Test
    fun devuelveElErrorHttpDelEndpointSinIntentarDescargar() = runBlocking {
        val cuerpo = ResponseBody.create(MediaType.get("application/json"), """{"status":"error"}""")
        coEvery { api.obtenerFotoAleatoria() } throws HttpException(Response.error<FotoPerroDto>(503, cuerpo))

        val fallo = repository.obtenerFoto().exceptionOrNull() as? HttpException

        assertEquals(503, fallo?.code())
        coVerify(exactly = 0) { api.descargarFoto(any()) }
    }

    @Test
    fun devuelve404SiLaFotoYaNoEstaDisponible() = runBlocking {
        val cuerpo = ResponseBody.create(null, "")
        coEvery { api.obtenerFotoAleatoria() } returns FotoPerroDto(url, "success")
        coEvery { api.descargarFoto(any()) } throws HttpException(Response.error<ResponseBody>(404, cuerpo))

        val fallo = repository.obtenerFoto().exceptionOrNull() as? HttpException

        assertEquals(404, fallo?.code())
    }

    @Test
    fun aceptaUnaFotoQueOcupaExactamenteElLimite() = runBlocking {
        val datos = ByteArray(5 * 1024 * 1024)
        coEvery { api.obtenerFotoAleatoria() } returns FotoPerroDto(url, "success")
        coEvery { api.descargarFoto(any()) } answers { ResponseBody.create(null, datos) }

        assertArrayEquals(datos, repository.obtenerFoto().getOrThrow())
    }

    @Test
    fun propagaLaCancelacion() = runBlocking {
        coEvery { api.obtenerFotoAleatoria() } throws CancellationException()
        try {
            repository.obtenerFoto()
            fail("Se esperaba la cancelación de la consulta")
        } catch (_: CancellationException) {
            Unit
        }
    }
}
