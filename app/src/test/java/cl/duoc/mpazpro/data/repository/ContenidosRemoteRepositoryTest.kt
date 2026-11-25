package cl.duoc.mpazpro.data.repository

import cl.duoc.mpazpro.data.remote.ContenidosApi
import cl.duoc.mpazpro.data.remote.dto.ActividadRequest
import cl.duoc.mpazpro.data.remote.dto.ObjetivoRequest
import com.google.gson.JsonParser
import java.io.IOException
import java.util.concurrent.LinkedBlockingQueue
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ContenidosRemoteRepositoryTest {
    private data class Peticion(val metodo: String, val ruta: String, val cuerpo: String)
    private data class Respuesta(val estado: Int, val cuerpo: String)

    private val peticiones = LinkedBlockingQueue<Peticion>()
    private val respuestas = LinkedBlockingQueue<Respuesta>()
    private lateinit var repository: ContenidosRemoteRepository

    @Before
    fun prepararCliente() {
        val cliente = OkHttpClient.Builder().addInterceptor { cadena ->
            val solicitud = cadena.request()
            val cuerpo = Buffer()
            solicitud.body()?.writeTo(cuerpo)
            val ruta = solicitud.url().encodedPath()
            val consulta = solicitud.url().encodedQuery()?.let { "?$it" }.orEmpty()
            peticiones.add(Peticion(solicitud.method(), ruta + consulta, cuerpo.readUtf8()))

            val respuesta = respuestas.poll() ?: throw IOException("Sin conexión")
            Response.Builder()
                .request(solicitud)
                .protocol(Protocol.HTTP_1_1)
                .code(respuesta.estado)
                .message("Respuesta de prueba")
                .body(ResponseBody.create(MediaType.get("application/json"), respuesta.cuerpo))
                .build()
        }.build()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://127.0.0.1:9082/")
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        repository = ContenidosRemoteRepository(retrofit.create(ContenidosApi::class.java))
    }

    @Test
    fun objetivosUsanElContratoHttpYElIdAsignadoPorElServidor() = runBlocking {
        val objetivo = ObjetivoRequest(2, "MA02 OA 01", "Resolver sumas", true)
        val json = """{"id":17,"asignaturaId":2,"codigoMineduc":"MA02 OA 01","descripcion":"Resolver sumas","autorizado":true}"""
        responder(201, json)
        assertEquals(17, repository.crearObjetivo(objetivo).getOrThrow().id)
        val cuerpo = JsonParser.parseString(peticion("POST", "/api/objetivos")).asJsonObject
        assertFalse(cuerpo.has("id"))
        assertEquals(2, cuerpo["asignaturaId"].asInt)

        responder(200, "[$json]")
        assertEquals(17, repository.listarObjetivos().getOrThrow().single().id)
        peticion("GET", "/api/objetivos")

        responder(200, json)
        assertTrue(repository.obtenerObjetivo(17).getOrThrow().autorizado)
        peticion("GET", "/api/objetivos/17")

        responder(200, json.replace("true", "false"))
        assertFalse(repository.actualizarObjetivo(17, objetivo.copy(autorizado = false)).getOrThrow().autorizado)
        assertFalse(JsonParser.parseString(peticion("PUT", "/api/objetivos/17")).asJsonObject["autorizado"].asBoolean)

        responder(204)
        assertTrue(repository.eliminarObjetivo(17).isSuccess)
        peticion("DELETE", "/api/objetivos/17")
        Unit
    }

    @Test
    fun actividadesUsanElOaRemotoYAdmitenAlternativasNulas() = runBlocking {
        val actividad = ActividadRequest(17, "Sumas", "opcion", "¿2+2?", "4", "3", "5", "")
        val json = """{"id":32,"oaId":17,"titulo":"Sumas","tipoDesafio":"opcion","pregunta":"¿2+2?","respuestaCorrecta":"4","alternativaIncorrecta1":"3","alternativaIncorrecta2":"5","alternativaIncorrecta3":null}"""
        responder(201, json)
        assertEquals(32, repository.crearActividad(actividad).getOrThrow().id)
        val cuerpo = JsonParser.parseString(peticion("POST", "/api/actividades")).asJsonObject
        assertFalse(cuerpo.has("id"))
        assertEquals(17, cuerpo["oaId"].asInt)

        responder(200, "[$json]")
        assertEquals(actividad, repository.listarActividades(17).getOrThrow().single().aSolicitud())
        peticion("GET", "/api/actividades?oaId=17")

        responder(200, "[]")
        assertTrue(repository.listarActividades().getOrThrow().isEmpty())
        peticion("GET", "/api/actividades")

        responder(200, json)
        assertEquals(32, repository.obtenerActividad(32).getOrThrow().id)
        peticion("GET", "/api/actividades/32")

        responder(200, json.replace("Sumas", "Suma simple"))
        assertEquals("Suma simple", repository.actualizarActividad(32, actividad.copy(titulo = "Suma simple")).getOrThrow().titulo)
        peticion("PUT", "/api/actividades/32")

        responder(204)
        assertTrue(repository.eliminarActividad(32).isSuccess)
        peticion("DELETE", "/api/actividades/32")
        Unit
    }

    @Test
    fun conservaElDetalleDe400Y404() = runBlocking {
        responder(400, """{"status":400,"mensaje":"El objetivo tiene actividades"}""")
        assertEquals("El objetivo tiene actividades (HTTP 400)", repository.eliminarObjetivo(17).exceptionOrNull()?.message)
        responder(404, """{"status":404,"mensaje":"Actividad no encontrada"}""")
        assertEquals("Actividad no encontrada (HTTP 404)", repository.obtenerActividad(32).exceptionOrNull()?.message)
    }

    @Test
    fun distingueLaFaltaDeConexionDeUnErrorHttp() = runBlocking {
        assertTrue(repository.listarObjetivos().exceptionOrNull() is ConexionContenidosException)
    }

    @Test
    fun crearActividadConDatosInvalidosUsaMensajePorDefectoSiLaRespuestaNoEsJson() = runBlocking {
        val solicitud = ActividadRequest(17, "", "opcion", "¿2+2?", "4", "", "", "")
        responder(400, "<html>Bad Request</html>")

        val fallo = repository.crearActividad(solicitud).exceptionOrNull()

        assertEquals("Revisa los datos ingresados. (HTTP 400)", fallo?.message)
        peticion("POST", "/api/actividades")
        Unit
    }

    @Test
    fun actualizarObjetivoInexistenteUsaMensajePorDefectoSiElDetalleEstaVacio() = runBlocking {
        responder(404, """{"mensaje":" "}""")
        val solicitud = ObjetivoRequest(2, "MA02 OA 01", "Resolver sumas", true)

        val fallo = repository.actualizarObjetivo(17, solicitud).exceptionOrNull()

        assertEquals("El contenido ya no existe en el servidor. (HTTP 404)", fallo?.message)
        peticion("PUT", "/api/objetivos/17")
        Unit
    }

    @Test
    fun obtenerActividadCon204InformaRespuestaInvalida() = runBlocking {
        responder(204)

        val fallo = repository.obtenerActividad(32).exceptionOrNull()

        assertEquals("Respuesta inválida de ms-contenidos.", fallo?.message)
        assertEquals("ms-contenidos respondió sin datos.", fallo?.cause?.message)
    }

    @Test
    fun eliminarActividadSinConexionConservaLaCausa() = runBlocking {
        val fallo = repository.eliminarActividad(32).exceptionOrNull()

        assertTrue(fallo is ConexionContenidosException)
        assertEquals("Sin conexión", fallo?.cause?.message)
        peticion("DELETE", "/api/actividades/32")
        Unit
    }

    @Test
    fun propagaLaCancelacionDeLaConsulta() = runBlocking {
        val api = mockk<ContenidosApi>()
        coEvery { api.listarObjetivos() } throws CancellationException("Consulta cancelada")

        try {
            ContenidosRemoteRepository(api).listarObjetivos()
            fail("Se esperaba CancellationException")
        } catch (e: CancellationException) {
            assertEquals("Consulta cancelada", e.message)
        }
    }

    private fun responder(estado: Int, cuerpo: String = "") {
        respuestas.add(Respuesta(estado, cuerpo))
    }

    private fun peticion(metodo: String, ruta: String): String {
        val peticion = peticiones.remove()
        assertEquals(metodo, peticion.metodo)
        assertEquals(ruta, peticion.ruta)
        return peticion.cuerpo
    }
}
